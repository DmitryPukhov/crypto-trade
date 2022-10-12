package dmitrypukhov.cryptotrade.btcusdt

import dmitrypukhov.cryptotrade.AppTool
import BtcUsdtEtl.BinanceTransformDf
import org.apache.hadoop.fs.Path
import org.apache.log4j.Logger
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.util.Properties

/**
 * 1. Read btc/usdt data from external system to raw
 * 2. Raw -> datamart etl
 */
object BtcUsdtJob {

  private val log = Logger.getLogger(getClass)
  private implicit val spark: SparkSession = AppTool.initSpark()


  /** Raw layer data location */
  private val rawDir = spark.conf.get("dmitrypukhov.cryptotrade.data.raw.dir")
  private val rawUriOf = (symbol: String, interval: String) => f"${rawDir}/${symbol}_$interval"

  /** Hive database */
  private val dbName = spark.conf.get("dmitrypukhov.cryptotrade.data.db_name")
  private val jdbcUri = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.btcusdt.jdbc.uri")
  private val jdbcUser = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.btcusdt.jdbc.user")
  private val jdbcPassword = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.btcusdt.jdbc.password")

  /** *
   * The job
   */
  def main(args: Array[String]): Unit = {
    // Set database
    AppTool.ensureHiveDb

    // Raw -> processed -> datamart
    val (symbol, interval) = ("btcusdt", "1min")
    raw2Processed(symbol, interval)
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
  def raw2Processed(symbol: String, interval: String): Unit = {
    val rawUri = rawUriOf(symbol, interval)
    val dstTableName = f"${symbol}_$interval"
    log.info(s"Transform $rawUri to $dstTableName table")

    spark
      .read.json(rawUri)
      .raw2Ohlcv(symbol)
      .withMacd()
      .write.mode(SaveMode.Overwrite).saveAsTable(dstTableName)
  }
}
