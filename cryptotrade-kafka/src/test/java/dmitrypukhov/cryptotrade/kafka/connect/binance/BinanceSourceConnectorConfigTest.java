package dmitrypukhov.cryptotrade.kafka.connect.binance;

import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static dmitrypukhov.cryptotrade.kafka.connect.binance.BinanceSourceConnectorConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BinanceSourceConnectorConfigTest {

    @Test
    public void basicParamsAreMandatory() {
        assertThrows(ConfigException.class, () -> {
            Map<String, String> props = new HashMap<>();
            new BinanceSourceConnectorConfig(props);
        });
    }

    public void checkingNonRequiredDefaults() {
        Map<String, String> props = new HashMap<>();
        BinanceSourceConnectorConfig config = new BinanceSourceConnectorConfig(props);
        assertEquals("foo", config.getString(FIRST_NONREQUIRED_PARAM_CONFIG));
        assertEquals("bar", config.getString(SECOND_NONREQUIRED_PARAM_CONFIG));
    }

}
