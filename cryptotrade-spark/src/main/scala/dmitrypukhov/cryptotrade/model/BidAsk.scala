package dmitrypukhov.cryptotrade.model

import java.time.LocalDateTime

case class BidAsk(time: LocalDateTime, symbol: String, bid: BigDecimal,bidQty: BigDecimal, ask: BigDecimal, askQty:BigDecimal)
