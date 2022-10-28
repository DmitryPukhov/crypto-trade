package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.kafka.connect.source.SourceRecord;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dmitrypukhov.cryptotrade.kafka.connect.binance.BinanceSourceConnectorConfig.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinanceSourceTaskTest {

    @Test
    public void taskVersionShouldMatch() {
        String version = PropertiesUtil.getConnectorVersion();
        assertEquals(version, new BinanceSourceTask().version());
    }

    @Test
    public void checkNumberOfRecords() {
        Map<String, String> connectorProps = new HashMap<>();
        connectorProps.put(FIRST_REQUIRED_PARAM_CONFIG, "Kafka");
        connectorProps.put(SECOND_REQUIRED_PARAM_CONFIG, "Connect");
        Map<String, String> taskProps = getTaskProps(connectorProps);
        BinanceSourceTask task = new BinanceSourceTask();
        assertDoesNotThrow(() -> {
            task.start(taskProps);
            List<SourceRecord> records = task.poll();
            assertEquals(3, records.size());
        });
    }

    private Map<String, String> getTaskProps(Map<String, String> connectorProps) {
        BinanceSourceConnector connector = new BinanceSourceConnector();
        connector.start(connectorProps);
        List<Map<String, String>> taskConfigs = connector.taskConfigs(1);
        return taskConfigs.get(0);
    }
    
}
