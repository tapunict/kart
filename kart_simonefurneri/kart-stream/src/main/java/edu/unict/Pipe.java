package edu.unict;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.TopicNameExtractor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Pipe {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kart-stream");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> source = builder.stream("events");
        TopicNameExtractor<String,String> topicNameExtractor = (key, value, recordContext) -> {
            System.out.println("Event: " + value);
            String topicName = "recoverable";
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonObject = value.split("\\s+", 3)[2];
                if (jsonObject.charAt(0) != '{') jsonObject = "{" + jsonObject;
                System.out.println("Json: " + jsonObject);
                JsonNode jsonNode = objectMapper.readTree(jsonObject);
                String carStatus = jsonNode.get("status").asText();
                System.out.println("Car status: " + carStatus);
                topicName = (carStatus.equals("scrap")) ? "scrap" : "recoverable";
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Sending to: " + topicName);
            return topicName;
        };
        source.to(topicNameExtractor);

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}