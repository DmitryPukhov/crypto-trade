package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BinanceSourceConnectorTest {

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

    @Test
    public void checkMissingRequiredParams() {
        assertThrows(ConnectException.class, () -> {
            Map<String, String> props = new HashMap<>();
            new BinanceSourceConnector().validate(props);
        });
    }

}
