package myapps;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;


import java.util.Properties;

public class Pipe {
    public static Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kart-stream");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    public static void configureStream(StreamsBuilder builder,Serde<KartModel> kartSerde,boolean useextractor) {
        KStream<String, KartModel> source = builder.stream("cars",Consumed.with(Serdes.String(), kartSerde));
        if (useextractor) {
            source.to(new DynamicTopicNameExtractor());
        } else { //simple testing
            source.mapValues(record -> {
                System.out.println("Processing record: " + record);
                record.setStatus("Processed");
                return record;
            }).to("default",Produced.with(Serdes.String(), kartSerde));
        }
    }

    public static void configureStream(StreamsBuilder builder) { //simple test stream
        KStream<String, String> source = builder.stream("cars",Consumed.with(Serdes.String(), Serdes.String()));
        source.mapValues(record -> {
            System.out.println("Processing record: " + record);
            return record;
        }).to("testout",Produced.with(Serdes.String(), Serdes.String()));
    }

    public static void main(String[] args) {
        //Configuring stream
        System.out.println("Getting properties...\n");
        Properties props = getProperties();
        Serde<KartModel> kartSerde = new JsonSerde<>(KartModel.class);
        final StreamsBuilder builder = new StreamsBuilder();
        System.out.print("Configuring stream...\n");
        configureStream(builder,kartSerde,true);
        
        //Building and starting stream
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        System.out.println("Starting stream...\n");
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));      
    }
    
}