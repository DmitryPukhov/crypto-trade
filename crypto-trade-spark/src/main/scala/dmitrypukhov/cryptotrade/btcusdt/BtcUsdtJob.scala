package dmitrypukhov.cryptotrade.btcusdt

import dmitrypukhov.cryptotrade.JobTool
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
  private implicit val spark: SparkSession = JobTool.initSpark()

  /** External data uri for import */
  private val inputUri = spark.conf.get("dmitrypukhov.cryptotrade.input.btcusdt.uri")

  /** Raw layer data location */
  private val rawUri = new Path(spark.conf.get("dmitrypukhov.cryptotrade.data.raw.dir"), "btcusdt").toString

  /** Hive database */
  private val dbName = spark.conf.get("dmitrypukhov.cryptotrade.data.db_name")

  /** Base table name to use with layer prefix */
  private val baseTable = spark.conf.get("dmitrypukhov.cryptotrade.data.btcusdt.table")

  /** Hive processed table */
  private val processedTable = s"$dbName.processed_$baseTable"

  /** Postgres datamart table */
  private val datamartPsqlTable = baseTable

  private val jdbcUri = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.btcusdt.jdbc.uri")
  private val jdbcUser = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.btcusdt.jdbc.user")
  private val jdbcPassword = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.btcusdt.jdbc.password")

  /** *
   * The job
   */
  def main(args: Array[String]): Unit = {
    // Set database
    JobTool.ensureDb

    // Import and transform
    external2Raw()
    raw2Processed()
    processedToMart()
  }

  /**
   * Processed Hive -> postgres datamart
   */
  def processedToMart(): Unit = {
    log.info(s"Read hive $processedTable, write to postgres datamart. Url: $jdbcUri, table: $datamartPsqlTable")
    val props = new Properties()
    props.put("Driver","org.postgresql.Driver")
    props.put("user",jdbcUser)
    props.put("password",jdbcPassword)
    spark.read.table(processedTable)
      .write.mode(SaveMode.Overwrite).jdbc(url = jdbcUri, table=baseTable, connectionProperties = props)

    spark.read.jdbc(url = jdbcUri, table=baseTable, properties = props).show()

  }

  /**
   * Raw -> processed read,transform,write
   */
  def raw2Processed(): Unit = {
    log.info(s"Transform $rawUri to $processedTable table")

    spark
      .read.option("header", value = true).csv(rawUri)
      .binance2Ohlcv() // ETL
      .write.mode(SaveMode.Overwrite).saveAsTable(processedTable)
  }

  /**
   * Import csv from external system to csv raw layer
   */
  def external2Raw(): Unit = {
    // Import to raw layer
    log.info(s"Import from external system $inputUri to raw layer $rawUri")
    spark
      .read.option("header", value = true).csv(inputUri)
      .write.option("header", value = true).mode(SaveMode.Overwrite).csv(rawUri)
  }
}
