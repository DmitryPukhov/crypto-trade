package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public final class PropertiesUtil {

    public static final String BINANCE_URI = BinanceSourceConnectorConfig.BINANCE_URI;
    private static final String CONNECTOR_VERSION = "connector.version";
    private static Logger log = LoggerFactory.getLogger(PropertiesUtil.class);
    private static String propertiesFile = "/application.properties";


    private static Properties properties;

    static {
        try (InputStream stream = PropertiesUtil.class.getResourceAsStream(propertiesFile)) {
            properties = new Properties();
            properties.load(stream);
        } catch (Exception ex) {
            log.warn("Error while loading properties: ", ex);
        }
    }
    private PropertiesUtil() {
    }

    public static Map<String, String> propertiesMap()  {
            return properties.entrySet().stream().collect(Collectors.toMap(
                    e -> String.valueOf(e.getKey()),
                    e -> String.valueOf(e.getValue().toString()),
                    (prev, next) -> next, HashMap::new
            ));
    }

    public static String getConnectorVersion() {
        return properties.getProperty(CONNECTOR_VERSION);
    }

    public static String getBinanceWebSocketUri() {
        return properties.getProperty(BINANCE_URI);
    }

}
