package dmitrypukhov.cryptotrade.btcusdt

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.from_unixtime

object BtcUsdtEtl extends Serializable {
  implicit class BinanceTransformDf(df: DataFrame) {
    /**
     * Transform binance model to ohlcv
     */
    def binance2Ohlcv(): DataFrame = df
      .withColumn("t", from_unixtime(df("t") / 1000))
      .withColumnRenamed("t", "datetime")
      .withColumnRenamed("o", "open")
      .withColumnRenamed("h", "high")
      .withColumnRenamed("l", "low")
      .withColumnRenamed("c", "close")
      .withColumnRenamed("v", "volume")
  }
}
