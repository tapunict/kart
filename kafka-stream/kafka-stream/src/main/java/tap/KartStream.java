package tap;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class KartStream {
    public static void main(String[] args){

        //configuro le properties
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,"karttopic");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"kafkaServer:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());

        final StreamsBuilder builder = new StreamsBuilder();

        TopicNameExtractor<String, String> triageTopicExtractor = (key, value, recordContext) -> {
            String dispatchTopic="kart-garage";
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(value);
                String message = jsonNode.get("message").asText();
                
                if(message.equals("AIUTO")){
                    dispatchTopic="kart-emergency";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return(dispatchTopic);
        };

        builder.<String,String>stream("karttopic").to(triageTopicExtractor);

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology,props);

        final CountDownLatch latch = new CountDownLatch(1);
          // per gestire il ctrl-c 
          Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();  //aspetta che si è finito il countdown per capire se sid deve fermare 
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    } 
}
