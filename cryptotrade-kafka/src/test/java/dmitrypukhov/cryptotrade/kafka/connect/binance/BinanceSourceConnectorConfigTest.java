package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static dmitrypukhov.cryptotrade.kafka.connect.binance.BinanceSourceConnectorConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BinanceSourceConnectorConfigTest {

//    @Test
//    public void basicParamsAreMandatory() {
//        assertThrows(ConfigException.class, () -> {
//            Map<String, String> props = new HashMap<>();
//            new BinanceSourceConnectorConfig(props);
//        });
//    }

    @Test
    public void binanceParamsShoulBeTakenFromResources() {
        //Map<String, String> props = Map.of(BINANCE_URI, "binance_uri_value");
        Map<String,String> props = new HashMap<>();
        BinanceSourceConnectorConfig config = new BinanceSourceConnectorConfig(props);
        // Assert value from application.properties
        assertEquals("wss://testnet.binance.vision", config.getString(BINANCE_URI));
    }

}
