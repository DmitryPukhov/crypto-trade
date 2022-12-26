package dmitrypukhov.cryptotrade.service;

import dmitrypukhov.cryptotrade.service.model.TickerTick;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;

public interface CryptoTradeRepository extends JpaRepository<TickerTick, Timestamp> {

}