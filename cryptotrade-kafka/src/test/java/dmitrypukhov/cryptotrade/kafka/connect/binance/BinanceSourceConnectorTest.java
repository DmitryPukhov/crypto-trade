package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinanceSourceConnectorTest {
    /**
     * Not a test, just an entry point for dev
     */
    @Test
    @Ignore
    void startDev() throws InterruptedException {
        SourceConnector connector = new BinanceSourceConnector();
        connector.start(PropertiesUtil.getPropMap());
        Thread.currentThread().join(10000);
        connector.stop();
    }
    @Test
    public void connectorVersionShouldMatch() {
        String version = PropertiesUtil.getConnectorVersion();
        assertEquals(version, new BinanceSourceConnector().version());
    }

    @Test
    public void checkClassTask() {
        Class<? extends Task> taskClass = new BinanceSourceConnector().taskClass();
        assertEquals(BinanceSourceTask.class, taskClass);
    }

//    @Test
//    public void checkMissingRequiredParams() {
//        assertThrows(ConnectException.class, () -> {
//            Map<String, String> props = new HashMap<>();
//            new BinanceSourceConnector().validate(props);
//        });
//    }

}
