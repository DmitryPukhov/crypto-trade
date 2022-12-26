package dmitrypukhov.cryptotrade.service.model;
import lombok.Getter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "btcusdt_price")
public class TickerTick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Timestamp datetime;

    @Getter
    private String symbol;

    @Getter
    private Double price;

    @Getter
    private Double volume;

}