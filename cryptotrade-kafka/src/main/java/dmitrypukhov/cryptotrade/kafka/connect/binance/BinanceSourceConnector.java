package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.kafka.common.config.Config;
import org.apache.kafka.common.config.ConfigDef;
//import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.connector.Task;
//import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.apache.kafka.connect.util.ConnectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static dmitrypukhov.cryptotrade.kafka.connect.binance.BinanceSourceConnectorConfig.*;

public class BinanceSourceConnector extends SourceConnector {

    private final Logger log = LoggerFactory.getLogger(BinanceSourceConnector.class);

    private Map<String, String> originalProps;
    private BinanceSourceConnectorConfig config;
    private SourceMonitorThread sourceMonitorThread;

    @Override
    public String version() {
        return PropertiesUtil.getConnectorVersion();
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    @Override
    public Class<? extends Task> taskClass() {
        return BinanceSourceTask.class;
    }

    @Override
    public Config validate(Map<String, String> connectorConfigs) {
        Config config = super.validate(connectorConfigs);
//        List<ConfigValue> configValues = config.configValues();
//        boolean missingTopicDefinition = true;
//        for (ConfigValue configValue : configValues) {
//            if (configValue.name().equals(BINANCE_URI)) {
//                if (configValue.value() != null) {
//                    missingTopicDefinition = false;
//                    break;
//                }
//            }
//        }
//        if (missingTopicDefinition) {
//            throw new ConnectException(String.format(
//                "There is no definition of [XYZ] in the "
//                + "configuration. Either the property "
//                + "'%s'must be set in the configuration.",
//                BINANCE_URI));
//        }
        return config;
    }

    @Override
    public void start(Map<String, String> originalProps) {
        this.originalProps = originalProps;
        config = new BinanceSourceConnectorConfig(originalProps);
        int monitorThreadTimeout = config.getInt(MONITOR_THREAD_TIMEOUT_CONFIG);
        //String binanceUri  = config.getString(BINANCE_URI);
        String binanceUri = "wss://testnet.binance.vision";
        sourceMonitorThread = new SourceMonitorThread(
            context, binanceUri, monitorThreadTimeout);
        sourceMonitorThread.start();
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>();
        // The partitions below represent the source's part that
        // would likely to be broken down into tasks... such as
        // tables in a database.
        List<String> partitions = sourceMonitorThread.getCurrentSources();
        if (partitions.isEmpty()) {
            taskConfigs = Collections.emptyList();
            log.warn("No tasks created because there is zero to work on");
        } else {
            int numTasks = Math.min(partitions.size(), maxTasks);
            List<List<String>> partitionSources = ConnectorUtils.groupPartitions(partitions, numTasks);
            for (List<String> source : partitionSources) {
                Map<String, String> taskConfig = new HashMap<>(originalProps);
                taskConfig.put("sources", String.join(",", source));
                taskConfigs.add(taskConfig);
            }
        }
        return taskConfigs;
    }

    @Override
    public void stop() {
        sourceMonitorThread.shutdown();
    }

}
