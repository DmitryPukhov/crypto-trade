package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;

public final class PropertiesUtil {

    public static final String BINANCE_URI = BinanceSourceConnectorConfig.BINANCE_URI;
    private static final String CONNECTOR_VERSION = "connector.version";
    private static Logger log = LoggerFactory.getLogger(PropertiesUtil.class);
    private static String propertiesFile = "application.default.properties";
    private static String devPropsFile = "application.dev.properties";

    private static Map<String, String> propMap = null;

    public static Map<String, String> getPropMap() {
        if (propMap == null) {
            initProps();
        }
        return propMap;
    }

    /**
     * Fill in application properties
     */
    private static void initProps() {
        propMap = new HashMap<>();
        Configurations configs = new Configurations();
        CombinedConfiguration config = new CombinedConfiguration(new OverrideCombiner());

        // Read application.*.properties in priority order. First - highest priority, last - lowest
        List<String> propFiles = Arrays.asList("application.dev.properties", "application.properties", "application.default.properties", "application.default.yaml");
        for (String propFilePath : propFiles) {
            try {
                // Read properties file
                config.addConfiguration(configs.properties(propFilePath));
                log.info(String.format("Success reading properties from file %s", propFilePath));
            } catch (/*ConfigurationException | /*IOException | */Exception ex) {
                log.info(String.format("Cannot read properties from file %s. It can be normal situation. %s", propFilePath, ex));
            }
        }

        // Form properties map
        config.getKeys().forEachRemaining(key -> propMap.put(key, config.getString(key)));

        // Print config to log
        log.info("Got properties:\n");
        log.info(propMap.entrySet().stream().map(e ->
                        String.format("%s=%s\n", e.getKey(), e.getValue()))
                .sorted().collect(Collectors.joining("\n")));
    }

    {
        initProps();
    }

    private PropertiesUtil() {
    }

    public static String getConnectorVersion() {
        return getPropMap().get(CONNECTOR_VERSION);
    }

    public static String getBinanceWebSocketUri() {
        return getPropMap().get(BINANCE_URI);
    }

    /**
     * Configure Kafka streams or connector application.
     * <p>
     * Various Kafka Streams related settings are defined here such as the location of the target Kafka cluster to use.
     * Additionally, you could also define Kafka Producer and Kafka Consumer settings when needed.
     *
     * @return Properties getStreamsConfiguration
     */
    public static Properties getKafkaConfiguration() {
        final Properties streamsConfiguration = new Properties();


        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, MethodHandles.lookup().lookupClass().getSimpleName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // Put kafka related properties, replacing not-kafka prefixes
        PropertiesUtil.getPropMap().keySet().stream()
                .filter(key -> key.startsWith("dmitrypukhov.cryptotrade.kafka"))
                .forEach(key -> streamsConfiguration.put(
                        key.replaceFirst("dmitrypukhov.cryptotrade.kafka.", ""),
                        PropertiesUtil.getPropMap().get(key)));

        return streamsConfiguration;
    }

}
