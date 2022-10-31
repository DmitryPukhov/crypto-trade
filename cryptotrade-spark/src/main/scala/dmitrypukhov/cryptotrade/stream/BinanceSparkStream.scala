package dmitrypukhov.cryptotrade.stream

import com.binance.connector.client.impl.WebsocketClientImpl
import com.binance.connector.client.utils.WebSocketCallback
import dmitrypukhov.cryptotrade.AppTool
import org.apache.spark.internal.Logging
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}


/**
 * Read binance web socket stream to spark
 * ToDo: implement Binance stream with continuous streaming
 */
class BinanceSparkStream(socketUri: String) extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) with WebSocketCallback with Serializable with Logging {

  def onStart(): Unit = {
    val symbol = "BTCUSDT"
    log.info(s"Streaming currency $symbol, socket: $socketUri")
    val binance = new WebsocketClientImpl(socketUri)
    binance.bookTicker(symbol, this) // Stream from binance
    log.info("Socket initialized")
  }

  override def onReceive(data: String): Unit = store(data)

  def onStop(): Unit = {}
}

object BinanceSparkStream extends App with Serializable {
  implicit val ssc: StreamingContext = new StreamingContext(AppTool.config.setAppName(this.getClass.getSimpleName), Seconds(1))
  val socketUri = ssc.sparkContext.getConf.get("dmitrypukhov.cryptotrade.input.binance.uri")

  val binanceStream = new BinanceSparkStream(socketUri)
  ssc.receiverStream[String](binanceStream)
    .map(BinanceTransform.binance2BidAsk) // convert binance json string to model
    .print() // todo: pass further

  ssc.start()
  ssc.awaitTermination()
}