package myapps;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.TopicNameExtractor;

public class DynamicTopicNameExtractor implements TopicNameExtractor<String, KartModel> {
    
    @Override
    public String extract(String key, KartModel value, RecordContext recordContext) {
        // Logica per scegliere il topic in base al contenuto del record
        if (value.getStatus().equals("recoverable")) {
            return "recoverable";
        } else if (value.getStatus().equals("scrap")) {
            return "scrap";
        }
        else { return "default";}
    }
}