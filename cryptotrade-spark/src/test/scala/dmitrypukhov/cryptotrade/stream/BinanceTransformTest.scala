package dmitrypukhov.cryptotrade.stream

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class BinanceTransformTest extends AnyFlatSpec {


  "BinanceTransform" should "convert string to json" in {
    val actual = BinanceTransform.binance2BidAsk(
      "{\"b\":\"0.16\"," +
        "\"B\":\"10\"," +
        "\"a\":\"2\"," +
        "\"A\":\"20\"," +
        "\"s\":\"BTCUSDT\", " +
        "\"u\":1}")

    actual.symbol should be("BTCUSDT")
    actual.bid  should be(0.16)
    actual.bidQty  should be(10)
    actual.ask should be(2)
    actual.askQty should be(20)
  }
}