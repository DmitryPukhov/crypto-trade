package dmitrypukhov.cryptotrade.process

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{avg, from_unixtime, lit, window}

object CurrencyEtl extends Serializable {
  implicit class BinanceTransformDf(df: DataFrame) {
    /**
     * Transform raw huobi model to ohlcv
     */
    def huobi2Ohlcv(symbol: String): DataFrame = df
      .withColumn("id", from_unixtime(df("id")))
      .withColumn("symbol", lit(symbol))
      .withColumnRenamed("id", "datetime")
      .select("datetime", "symbol", "open", "high", "low", "close", "vol")


    /**
     * Add MACD indicator to the dataframe
     */
    def toMacd(signal: Int = 9, fast: Int = 12, slow: Int = 26): DataFrame = {
      val winSpec = (interval: Int) => Window.partitionBy(window(df("datetime"), f"$interval minutes"))
      // With macd columns
      df.withColumn("signal", avg("close").over(winSpec(signal)))
        .withColumn("slow", avg("close").over(winSpec(slow)))
        .withColumn("fast", avg("close").over(winSpec(fast)))
        // Return only time, price and macd columns
        .withColumn("price", df("close"))
        .select("datetime", "price", "signal", "slow", "fast")
    }
  }
}
