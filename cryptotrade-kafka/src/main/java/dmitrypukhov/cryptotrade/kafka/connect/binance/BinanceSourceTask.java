package dmitrypukhov.cryptotrade.kafka.connect.binance;

import com.binance.connector.client.impl.WebsocketClientImpl;
import lombok.Getter;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

import static dmitrypukhov.cryptotrade.kafka.connect.binance.BinanceSourceConnectorConfig.BINANCE_URI;
import static dmitrypukhov.cryptotrade.kafka.connect.binance.BinanceSourceConnectorConfig.MONITOR_THREAD_TIMEOUT_CONFIG;

public class BinanceSourceTask extends SourceTask {
    @Getter
    private static String ticker = "btcusdt";

    private static Logger log = LoggerFactory.getLogger(BinanceSourceTask.class);

    private BinanceSourceConnectorConfig config;
    private int monitorThreadTimeout;
    private List<String> sources;
    private Deque<String> messages = new ConcurrentLinkedDeque<>();

    @Override
    public String version() {
        return PropertiesUtil.getConnectorVersion();
    }

    @Override
    public void start(Map<String, String> properties) {
        config = new BinanceSourceConnectorConfig(properties);
        monitorThreadTimeout = config.getInt(MONITOR_THREAD_TIMEOUT_CONFIG);

        // Create binance web socket client
        //String uri = properties.get("dmitrypukhov.cryptotrade.input.binance.uri");

        //String uri = "wss://testnet.binance.vision"; // todo: remove hard code
        String uri = config.getString(BINANCE_URI);
        log.info(String.format("Creating Binance source task from %s", uri));
        WebsocketClientImpl client = new WebsocketClientImpl(uri); // defaults to production environment unless stated,

        client.symbolTicker(ticker, ((event) -> {
            // Add received message to the queue
            log.debug(String.format("Got the message. Ticker: %s, message: %s", ticker, event));
            messages.add(event);
        }));
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        List<SourceRecord> records = new ArrayList<>();
        while (!messages.isEmpty()) {
            String msg = messages.pop();
            log.debug("Adding source message %s", msg);

            records.add(new SourceRecord(
                    Collections.singletonMap("source", 0),
                    Collections.singletonMap("offset", 0),
                    ticker, null, null, null, Schema.BYTES_SCHEMA,
                    msg.getBytes()));
        }
        return records;
    }

    @Override
    public void stop() {
    }
}
