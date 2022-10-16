package dmitrypukhov.cryptotrade.service.model;
import lombok.Getter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

//.select("datetime", "symbol", "open", "high", "low", "close", "vol")
@Entity
@Table(name = "btcusdt_1min")
public class Ohlcv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Timestamp datetime;

    @Getter
    private String symbol;

    @Getter
    private Double open;

    @Getter
    private Double high;

    @Getter
    private Double low;

    @Getter
    private Double close;

    @Getter
    private Double vol;

}