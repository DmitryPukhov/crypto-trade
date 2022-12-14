package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import java.util.Map;

public class BinanceSourceConnectorConfig extends AbstractConfig {

    public BinanceSourceConnectorConfig(final Map<?, ?> originalProps) {
        super(CONFIG_DEF, originalProps);
    }

    public static final String BINANCE_URI = "dmitrypukhov.cryptotrade.input.binance.uri";
    public static final String BINANCE_URI_DOC = "Binance web socket uri";
    public static final String MONITOR_THREAD_TIMEOUT_CONFIG = "monitor.thread.timeout";
    private static final String MONITOR_THREAD_TIMEOUT_DOC = "Timeout used by the monitoring thread";
    private static final int MONITOR_THREAD_TIMEOUT_DEFAULT = 10000;

    public static final ConfigDef CONFIG_DEF = createConfigDef();

    private static ConfigDef createConfigDef() {
        ConfigDef configDef = new ConfigDef();

        // Add kafka properties, loaded from app.properties
        PropertiesUtil.getKafkaConfiguration().forEach((key, value) -> configDef.define(key.toString(),
                Type.STRING,
                value,
                Importance.HIGH,
                key.toString()
        ));

        updateParams(configDef);
        // Add some kafka required props
        //addParams(configDef);
        return configDef;
    }

    private static void updateParams(final ConfigDef configDef) {

        configDef.define(
                        BINANCE_URI,
                        Type.STRING,
                        PropertiesUtil.getBinanceWebSocketUri(),
                        Importance.HIGH,
                        BINANCE_URI_DOC)
                .define(
                MONITOR_THREAD_TIMEOUT_CONFIG,
                Type.INT,
                MONITOR_THREAD_TIMEOUT_DEFAULT,
                Importance.LOW,
                MONITOR_THREAD_TIMEOUT_DOC);
    }

}
