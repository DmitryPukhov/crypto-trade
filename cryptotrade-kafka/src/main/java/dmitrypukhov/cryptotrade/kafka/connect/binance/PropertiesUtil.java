package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.combined.CombinedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.OverrideCombiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PropertiesUtil {

    public static final String BINANCE_URI = BinanceSourceConnectorConfig.BINANCE_URI;
    private static final String CONNECTOR_VERSION = "connector.version";
    private static Logger log = LoggerFactory.getLogger(PropertiesUtil.class);
    private static String propertiesFile = "application.default.properties";
    private static String devPropsFile = "application.dev.properties";

    private static Map<String,String> propMap = null;
    public static Map<String, String> getPropMap() {
            if(propMap == null) {
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
        List<String> propFiles = Arrays.asList(devPropsFile, propertiesFile);
        for (String propFilePath : propFiles) {
            try {
                // Read properties file
                config.addConfiguration(configs.properties(propFilePath));
            } catch (ConfigurationException cex) {
                log.info(String.format("Cannot read properties from file %s. It can be normal situation.", propFilePath));
            }
        }

        // Form properties map
        config.getKeys().forEachRemaining(key -> propMap.put(key, config.getString(key)));

        // Print config to log
        log.info("Got properties:");
        propMap.entrySet().stream().map(e->String.format("%s=%s\n",e.getKey(), e.getValue())).sorted().forEach(log::info);
    }
    {initProps();}

    private PropertiesUtil() {
    }

    public static String getConnectorVersion() {
        return getPropMap().get(CONNECTOR_VERSION);
    }

    public static String getBinanceWebSocketUri() {
        return getPropMap().get(BINANCE_URI);
    }

}
