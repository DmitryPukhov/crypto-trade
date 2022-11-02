package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.kafka.connect.source.SourceRecord;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinanceSourceTaskTest {

    @Test
    @Ignore
    /**
     * Not a test, just a placeholder for debugging
     */
    public void shouldConnectToBinance() {
        Map<String, String> taskProps = getTaskProps(PropertiesUtil.getPropMap());

        BinanceSourceTask task = new BinanceSourceTask();
        assertDoesNotThrow(() -> {
            task.start(taskProps);
            Thread.currentThread().join(5000);
            List<SourceRecord> records = task.poll();
            System.out.println(String.format("Got %d records", records.size()));
            //assertEquals(3, records.size());
        });
    }

    @Test
    public void taskVersionShouldMatch() {
        String version = PropertiesUtil.getConnectorVersion();
        assertEquals(version, new BinanceSourceTask().version());
    }

//    @Test
//    @Ignore
//    public void checkNumberOfRecords() {
//        Map<String, String> connectorProps = new HashMap<>();
//        Map<String, String> taskProps = getTaskProps(connectorProps);
//        BinanceSourceTask task = new BinanceSourceTask();
//        assertDoesNotThrow(() -> {
//            task.start(taskProps);
//            List<SourceRecord> records = task.poll();
//            assertEquals(3, records.size());
//        });
//    }

    private Map<String, String> getTaskProps(Map<String, String> connectorProps) {
        BinanceSourceConnector connector = new BinanceSourceConnector();
        connector.start(connectorProps);
        List<Map<String, String>> taskConfigs = connector.taskConfigs(1);
        return taskConfigs.get(0);
    }
}
