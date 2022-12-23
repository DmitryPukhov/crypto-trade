package dmitrypukhov.cryptotrade.kafka.streams.binance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dmitrypukhov.cryptotrade.kafka.connect.binance.PropertiesUtil;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class RawBinance2Price {
    private RawBinance2Price() {
    }

    private static final Logger log = LoggerFactory.getLogger(RawBinance2Price.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final TypeReference<Map<String, String>> bidAskTypeRef = new TypeReference<>() {    };

    /**
     * The Streams application as a whole can be launched like any normal Java application that has a `main()` method.
     */
    public static void main(final String[] args) {
        log.info("Starting Binance stream processing");

        // Configure the Streams application.
        final Properties streamsConfiguration = PropertiesUtil.getKafkaConfiguration();


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
     * Define the processing topology for Word Count.
     *
     * @param builder StreamsBuilder to use
     */
    static void createStream(final StreamsBuilder builder) {
        String inputTopic = "raw.btcusdt.ticker";
        final KStream<String, String> textLines = builder.stream(inputTopic);
        String outputTopic = "btcusdt.price";
        textLines.mapValues(RawBinance2Price::raw2Price).to(outputTopic);
    }

    /**
     * Extract price and volume from raw data
     * Raw format: <a href="https://github.com/binance/binance-spot-api-docs/blob/master/web-socket-streams.md#individual-symbol-ticker-streams">...</a>
     */
    static Map<String, String> raw2Price(Map<String, String> rawMap) {
        Map<String, String> processedMap = new HashMap<>();
        processedMap.put("datetime", LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(rawMap.get("E"))), ZoneId.of("UTC")).toString());
        processedMap.put("symbol", rawMap.get("s"));
        processedMap.put("price", rawMap.get("c"));
        processedMap.put("volume", rawMap.get("Q"));
//        processedMap.put("bid", rawMap.get("b"));
//        processedMap.put("bidQty", rawMap.get("B"));
//        processedMap.put("ask", rawMap.get("a"));
//        processedMap.put("askQty", rawMap.get("A"));
        return processedMap;
    }

    /**
     * Transform raw binance json string to bid ask
     */
    static String raw2Price(String rawString) {
        String out;
        try {
            Map<String, String> rawMap = mapper.readValue(rawString, bidAskTypeRef);
            Map<String, String> bidAskMap = raw2Price(rawMap);
            out = mapper.writeValueAsString(bidAskMap);

        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        return out;
    }
}
