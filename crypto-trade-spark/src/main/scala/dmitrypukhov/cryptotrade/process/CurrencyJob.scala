package dmitrypukhov.cryptotrade.process

import dmitrypukhov.cryptotrade.AppTool
import dmitrypukhov.cryptotrade.process.CurrencyEtl.Functions
import org.apache.log4j.Logger
import org.apache.spark.sql.{SaveMode, SparkSession}

import java.util.Properties
import scala.collection.JavaConverters.enumerationAsScalaIteratorConverter

/**
 * 1. Read btc/usdt data from external system to raw
 * 2. Raw -> datamart etl
 */
object CurrencyJob {

  private val log = Logger.getLogger(getClass)
  private implicit val spark: SparkSession = AppTool.initSpark()

  /** Raw layer data location */
  private val rawRootDir = spark.conf.get("dmitrypukhov.cryptotrade.data.raw.dir")

  /** Currency symbol and candles interval */
  val symbol = "btcusdt"
  val interval = "1min"

  /** Macd parameters */
  private val (signal: Int, slow: Int, fast: Int) = (9, 12, 26)

  /** Raw data folder Uri */
  private val rawDir = f"$rawRootDir/${symbol}_$interval"

  /** Hive and psql table name for candles */
  private val ohlcvTableName = f"${symbol}_$interval"

  /** Hive and psql table name for macd indicator */
  private def macdTableName = f"${symbol}_macd_${signal}_${slow}_${fast}"


  /**
   * Transform raw data, fill in 2 data marts: candles and macd
   */
  def main(args: Array[String]): Unit = {
    // Set database
    AppTool.ensureHiveDb

    // Raw -> hive processed
    raw2Ohlcv()
    ohlcv2Macd()
    // Processed -> postgres datamarts
    hive2Psql(ohlcvTableName)
    hive2Psql(macdTableName)
    //Processed -> clickhouse datamarts
    hive2Click(ohlcvTableName)
  }

  /**
   * Hive -> postgres, preserve table name
   */
  def hive2Psql(tableName: String): Unit = {
    /** Jdbc database */
    val jdbcUri = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.psql.uri")
    val jdbcUser = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.psql.user")
    val jdbcPassword = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.psql.password")

    log.info(s"Read hive $tableName, write to psql $tableName. Jdbc uri: $jdbcUri")
    val props = new Properties()
    props.put("Driver", "org.postgresql.Driver")
    props.put("user", jdbcUser)
    props.put("password", jdbcPassword)
    props.put("secure", "true")
    props.put("compression", "false")
    spark.read.table(tableName)
      .write
      .mode(SaveMode.Overwrite)
      .jdbc(url = jdbcUri, table = tableName, connectionProperties = props)
  }

  def hive2Click(tableName: String): Unit = {
    log.info(s"Read hive $tableName, write to click $tableName")

    val jdbcUri = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.click.uri")
    log.info(s"Read hive $tableName, write to clickhouse $tableName. Jdbc uri: $jdbcUri")

    // Set up properties
    val props = new Properties()
    //props.put("Driver", "com.clickhouse.jdbc.ClickHouseDriver")
    props.put("Driver", "ru.yandex.clickhouse.ClickHouseDriver")
    spark.conf.getAll.filter(_._1.startsWith("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.click"))
      .foreach(t => props.put(t._1.split("\\.").last, t._2)) // Filter props from config, set them up


    log.info(s"Properties: ${props.keys().asScala.toList.map(_.toString).filter(_ != "password").map(key => s"$key=${props.getProperty(key)}")}")
    // Read Hive, wite postgres
    spark.read.table(tableName)
      .write
      .mode(SaveMode.Overwrite)
      .option("createTableOptions", "ENGINE=Log()") // Clickhouse specific option
      .jdbc(url = jdbcUri, table = tableName, connectionProperties = props)
  }

  /**
   * Raw hdfs -> hive ohlcv
   */
  def raw2Ohlcv(): Unit = {
    log.info(s"Transform $rawDir to $macdTableName table")
    spark.read.json(path = rawDir)
      .huobi2Ohlcv(symbol) // raw -> ohlcv
      .write.mode(SaveMode.Overwrite).saveAsTable(ohlcvTableName)
  }

  /**
   * Hive ohlcv -> hive macd
   */
  def ohlcv2Macd(): Unit = {
    log.info(s"Transform hive tables from $ohlcvTableName to $macdTableName")
    spark
      .read.table(ohlcvTableName)
      .toMacd(signal, fast, slow) // ohlcv -> macd indicator
      .write.mode(SaveMode.Overwrite).saveAsTable(macdTableName)
  }
}
