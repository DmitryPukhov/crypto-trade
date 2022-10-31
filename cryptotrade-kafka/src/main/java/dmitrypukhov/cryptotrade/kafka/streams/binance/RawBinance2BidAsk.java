package dmitrypukhov.cryptotrade.kafka.streams.binance;

public class RawBinance2BidAsk {

//    static final String JSON_SOURCE_TOPIC = "json-source";
//    static final String AVRO_SINK_TOPIC = "avro-sink";
//
//    public static void main(final String[] args) {
//        final String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
//        final String schemaRegistryUrl = args.length > 1 ? args[1] : "http://localhost:8081";
//        final KafkaStreams streams = buildJsonToAvroStream(
//                bootstrapServers,
//                schemaRegistryUrl
//        );
//        streams.start();
//
//        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
//        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
//    }
//
//    static KafkaStreams buildJsonToAvroStream(final String bootstrapServers,
//                                              final String schemaRegistryUrl) {
//        final Properties streamsConfiguration = new Properties();
//
//        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "json-to-avro-stream-conversion");
//        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "json-to-avro-stream-conversion-client");
//        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        // Where to find the Confluent schema registry instance(s)
//        streamsConfiguration.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
//
//        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
//        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
//        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100 * 1000);
//
//        final ObjectMapper objectMapper = new ObjectMapper();
//
//        final StreamsBuilder builder = new StreamsBuilder();
//
//        // read the source stream
//        final KStream<String, String> jsonToAvroStream = builder.stream(JSON_SOURCE_TOPIC,
//                Consumed.with(Serdes.String(), Serdes.String()));
//        jsonToAvroStream.mapValues(v -> {
//            WikiFeed wikiFeed = null;
//            try {
//                final JsonNode jsonNode = objectMapper.readTree(v);
//                wikiFeed = new WikiFeed(jsonNode.get("user").asText(),
//                        jsonNode.get("is_new").asBoolean(),
//                        jsonNode.get("content").asText());
//            } catch (final IOException e) {
//                throw new RuntimeException(e);
//            }
//            return wikiFeed;
//        }).filter((k,v) -> v != null).to(AVRO_SINK_TOPIC);
//
//        return new KafkaStreams(builder.build(), streamsConfiguration);
//    }

}
