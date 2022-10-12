package dmitrypukhov.cryptotrade.process

import dmitrypukhov.cryptotrade.AppTool
import CurrencyEtl.BinanceTransformDf
import org.apache.hadoop.fs.Path
import org.apache.log4j.Logger
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.util.Properties

/**
 * 1. Read btc/usdt data from external system to raw
 * 2. Raw -> datamart etl
 */
object CurrencyJob {

  private val log = Logger.getLogger(getClass)
  private implicit val spark: SparkSession = AppTool.initSpark()

  /** Raw layer data location */
  private val rawDir = spark.conf.get("dmitrypukhov.cryptotrade.data.raw.dir")

  /** Hive database */
  private val jdbcUri = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.uri")
  private val jdbcUser = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.user")
  private val jdbcPassword = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.password")

  /** *
   * The job
   */
  def main(args: Array[String]): Unit = {
    // Set database
    AppTool.ensureHiveDb

    // Raw -> processed -> datamart
    val (symbol, interval) = ("btcusdt", "1min")
    raw2Macd(symbol, interval)
    hive2Psql(symbol)
  }

  /**
   * Processed Hive -> postgres datamart
   */
  def hive2Psql(symbol: String): Unit = {
    val tableName = symbol // postgres and hive tables are named equally
    log.info(s"Read hive $tableName, write to postgres datamart. Url: $jdbcUri, table: $tableName")
    val props = new Properties()
    props.put("Driver", "org.postgresql.Driver")
    props.put("user", jdbcUser)
    props.put("password", jdbcPassword)
    spark.read.table(tableName)
      .write.mode(SaveMode.Overwrite).jdbc(url = jdbcUri, table = tableName, connectionProperties = props)
  }

  /**
   * Raw -> processed read,transform,write
   */
  def raw2Macd(symbol: String, interval: String): Unit = {
    val rawUri = f"${rawDir}/${symbol}_$interval"
    val(signal,fast,slow)=(9,12,26)
    val dstTableName = f"${symbol}_macd_${slow}_${12}_${26}"

    log.info(s"Transform $rawUri to $dstTableName table")
    spark
      .read.json(rawUri)
      .huobi2Ohlcv(symbol)      // raw -> ohlcv
      .toMacd(signal,fast,slow) // ohlcv -> macd indicator
      .write.mode(SaveMode.Overwrite).saveAsTable(dstTableName)
  }
}
