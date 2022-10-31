package dmitrypukhov.cryptotrade.stream

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import dmitrypukhov.cryptotrade.model.BidAsk
import java.time.LocalDateTime

/**
 * Transform binance input to model
 */
object BinanceTransform extends Serializable {
  private val mapper = new ObjectMapper().registerModule(DefaultScalaModule)

  /**
   * Transform raw binance json string to the model
   */
  def binance2BidAsk(jsonString: String): BidAsk = {
    // Convert json string to map then construct BidAsk model
    val bidAskMap = mapper.readValue(jsonString, classOf[Map[String, String]])
    BidAsk(
      time = LocalDateTime.now(),
      symbol = bidAskMap("s"),
      bid = BigDecimal(bidAskMap("b")),
      bidQty = bidAskMap("B").toInt,
      ask = BigDecimal(bidAskMap("a")),
      askQty = bidAskMap("A").toInt
    )
  }
}
