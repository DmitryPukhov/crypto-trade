package dmitrypukhov.cryptotrade.process

import dmitrypukhov.cryptotrade.AppTool
import dmitrypukhov.cryptotrade.process.CurrencyEtl.Functions
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
  private val (symbol: String, signal: Int, slow: Int, fast: Int) = ("btcusdt", 9, 12, 26)

  private def macdTableName = f"${symbol}_macd_${signal}_${slow}_${fast}"


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
    hive2Psql(macdTableName)
  }

  /**
   * Processed Hive -> postgres datamart
   */
  def hive2Psql(tableName: String): Unit = {
    log.info(s"Read hive $tableName, write to psql $tableName. Jdbc uri: $jdbcUri")
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
    val rawUri = f"$rawDir/${symbol}_$interval"

    log.info(s"Transform $rawUri to $macdTableName table")
    spark
      .read.json(rawUri)
      .huobi2Ohlcv(symbol) // raw -> ohlcv
      .toMacd(signal, fast, slow) // ohlcv -> macd indicator
      .write.mode(SaveMode.Overwrite).saveAsTable(macdTableName)
  }
}
