package dmitrypukhov.cryptotrade.btcusdt

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{from_unixtime, lit}

object BtcUsdtEtl extends Serializable {
  implicit class BinanceTransformDf(df: DataFrame) {
    /**
     * Transform binance model to ohlcv
     */
    def raw2Ohlcv(symbol: String): DataFrame = df
      .withColumn("id", from_unixtime(df("id")))
      .withColumn("symbol", lit(symbol))
      .withColumnRenamed("id", "datetime")
      .select("datetime", "symbol", "open", "high", "low", "close", "vol")


    def withMacd(): DataFrame = {
      df
    }
  }
}
