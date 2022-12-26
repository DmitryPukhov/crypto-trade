package dmitrypukhov.cryptotrade.service;

import dmitrypukhov.cryptotrade.service.model.TickerTick;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("btcusdt_price")
public class CryptoTradeController {
    @Autowired
    private CryptoTradeRepository cryptoTradeRepo;

    @CrossOrigin(origins = {"*"})
    @GetMapping("list")
    public List<TickerTick> list() {
        List<TickerTick> candles = cryptoTradeRepo.findAll();
        return candles;
    }
}