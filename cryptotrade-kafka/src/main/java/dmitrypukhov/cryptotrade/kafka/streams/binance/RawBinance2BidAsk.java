package dmitrypukhov.cryptotrade.kafka.streams.binance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dmitrypukhov.cryptotrade.kafka.connect.binance.PropertiesUtil;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class RawBinance2BidAsk {
    private RawBinance2BidAsk() { }

    private static Logger log = LoggerFactory.getLogger(RawBinance2BidAsk.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private static TypeReference<Map<String, String>> bidAskTypeRef = new TypeReference<>() {
    };

    private static String inputTopic = "raw.btcusdt.ticker";
    private static String outputTopic = "btcusdt.bidask";

    /**
     * The Streams application as a whole can be launched like any normal Java application that has a `main()` method.
     */
    public static void main(final String[] args) {

        final String bootstrapServers = PropertiesUtil.propertiesMap().get("dmitrypukhov.cryptotrade.kafka.bootstrapservers");

        // Configure the Streams application.
        final Properties streamsConfiguration = getStreamsConfiguration(bootstrapServers);

        // Define the processing topology of the Streams application.
        final StreamsBuilder builder = new StreamsBuilder();
        createStream(builder);
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration);

        // Always (and unconditionally) clean local state prior to starting the processing topology.
        // We opt for this unconditional call here because this will make it easier for you to play around with the example
        // when resetting the application for doing a re-run (via the Application Reset Tool,
        // https://docs.confluent.io/platform/current/streams/developer-guide/app-reset-tool.html).
        //
        // The drawback of cleaning up local state prior is that your app must rebuilt its local state from scratch, which
        // will take time and will require reading all the state-relevant data from the Kafka cluster over the network.
        // Thus in a production scenario you typically do not want to clean up always as we do here but rather only when it
        // is truly needed, i.e., only under certain conditions (e.g., the presence of a command line flag for your app).
        // See `ApplicationResetExample.java` for a production-like example.
        streams.cleanUp();

        // Now run the processing topology via `start()` to begin processing its input data.
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close the Streams application.
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    /**
     * Configure the Streams application.
     * <p>
     * Various Kafka Streams related settings are defined here such as the location of the target Kafka cluster to use.
     * Additionally, you could also define Kafka Producer and Kafka Consumer settings when needed.
     *
     * @param bootstrapServers Kafka cluster address
     * @return Properties getStreamsConfiguration
     */
    static Properties getStreamsConfiguration(final String bootstrapServers) {
        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, MethodHandles.lookup().lookupClass().getSimpleName());
        // Where to find Kafka broker(s).
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Specify default (de)serializers for record keys and for record values.
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // Records should be flushed every 10 seconds. This is less than the default
        // in order to keep this example interactive.
        //streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        // For illustrative purposes we disable record caches.
        //streamsConfiguration.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        // Use a temporary directory for storing state, which will be automatically removed after the test.
        //streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getAbsolutePath());
        return streamsConfiguration;
    }

    /**
     * Define the processing topology for Word Count.
     *
     * @param builder StreamsBuilder to use
     */
    static void createStream(final StreamsBuilder builder) {
        final KStream<String, String> textLines = builder.stream(inputTopic);
        textLines.mapValues(RawBinance2BidAsk::raw2BidAsk).to(outputTopic);
    }

    static Map<String, String> raw2BidAsk(Map<String, String> rawMap) {
        Map<String, String> processedMap = new HashMap<>();
        processedMap.put("datetime", LocalDateTime.now().toString());
        processedMap.put("symbol", rawMap.get("s"));
        processedMap.put("bid", rawMap.get("b"));
        processedMap.put("bidQty", rawMap.get("B"));
        processedMap.put("ask", rawMap.get("a"));
        processedMap.put("askQty", rawMap.get("A"));
        return processedMap;
    }

    /**
     * Transform raw binance json string to bid ask
     */
    static String raw2BidAsk(String rawString) {
        String out = "";
        try {
            Map<String, String> rawMap = mapper.readValue(rawString, bidAskTypeRef);
            Map<String, String> bidAskMap = raw2BidAsk(rawMap);
            out = mapper.writeValueAsString(bidAskMap);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return out;
    }
}
