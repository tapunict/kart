package myapps;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerde<T> implements Serializer<T>, Deserializer<T>, Serde<T> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Class<T> targetType;


    public JsonSerde(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            String raw = new String(data);
            System.out.println("Raw data: " + raw);
            // Cerco primo } di {name=logstash}
            int firstJsonEndIndex = raw.indexOf('}') + 1;
            if (firstJsonEndIndex == 0 || firstJsonEndIndex >= raw.length()) {
                return null; // Non trovato o fuori dai limiti
            }
            
            // Cerco vero messaggio
            int secondJsonStartIndex = raw.indexOf('{', firstJsonEndIndex);
            if (secondJsonStartIndex == -1) {
                return null; // Non è stato trovato un secondo oggetto JSON valido
            }
            
            // Isola il secondo oggetto JSON
            String cleanJson = raw.substring(secondJsonStartIndex);
            return OBJECT_MAPPER.readValue(cleanJson, targetType);
        } catch (Exception e) {
            throw new IllegalStateException("Error deserializing value", e);
        }
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return OBJECT_MAPPER.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new IllegalStateException("Error serializing value", e);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public Serializer<T> serializer() {
        return this;
    }

    @Override
    public Deserializer<T> deserializer() {
        return this;
    }
}
