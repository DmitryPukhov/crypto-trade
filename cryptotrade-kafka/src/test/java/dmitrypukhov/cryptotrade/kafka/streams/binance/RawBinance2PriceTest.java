package dmitrypukhov.cryptotrade.kafka.streams.binance;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RawBinance2PriceTest {

    @Test
    void raw2Price() {
        Map<String, String> actual = RawBinance2Price.raw2Price(Map.of(
                "E", "1671775459969", // mi
                "s", "ticker1",
                "c", "10.0",
                "Q", "2.0"));
        assertEquals(Set.of("datetime","symbol","price","volume"), actual.keySet());
        assertEquals("2022-12-23T06:04:19.969", actual.get("datetime"));
        assertEquals("ticker1", actual.get("symbol"));
        assertEquals("10.0", actual.get("price"));
        assertEquals("2.0", actual.get("volume"));
    }
}