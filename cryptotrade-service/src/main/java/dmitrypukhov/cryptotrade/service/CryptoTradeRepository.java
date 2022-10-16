package dmitrypukhov.cryptotrade.service;

import dmitrypukhov.cryptotrade.service.model.Ohlcv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public interface CryptoTradeRepository extends JpaRepository<Ohlcv, Timestamp> {

}