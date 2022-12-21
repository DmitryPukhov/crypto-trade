package dmitrypukhov.cryptotrade.process

import com.mongodb.spark.MongoSpark
import com.mongodb.spark.config.WriteConfig
import dmitrypukhov.cryptotrade.AppTool
import dmitrypukhov.cryptotrade.process.HuobiTransform.Functions
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
  private lazy val rawRootDir = spark.conf.get("dmitrypukhov.cryptotrade.data.raw.dir")
  private lazy val dataMartsDir = spark.conf.get("dmitrypukhov.cryptotrade.data.marts.dir")
  /** Currency symbol and candles interval */
  val symbol = "btcusdt"
  val interval = "1min"

  /** Macd parameters */
  private val (signal: Int, slow: Int, fast: Int) = (9, 12, 26)

  /** Raw data folder Uri */
  private lazy val rawDir = f"$rawRootDir/${symbol}_$interval"

  /** Hive and psql table name for candles */
  private lazy val ohlcvTableName = f"${symbol}_$interval"

  /** Hive and psql table name for macd indicator */
  private lazy val macdTableName = f"${symbol}_macd_${signal}_${slow}_$fast"

  //  /** Map jobname -> job func, keys are in default execution order */
  //  val jobMap: Map[String, () => Unit] = ListMap("raw2ohlcv" -> raw2Ohlcv, "ohlcv2macd" -> ohlcv2Macd, "ohlcv2psql" -> ohlcv2Psql, "macd2psql" -> macd2Psql, "ohlcv2click" -> ohlcv2Click, "macd2click" -> macd2Click)
  private val defaultProcessSeq = Seq(
    "raw2ohlcv",
    "ohlcv2macd",
    "ohlcv2psql",
    "raw2csv",
    "macd2psql",
    "ohlcv2click",
    "macd2click",
    "ohlcv2mongo",
    "macd2mongo")

  /**
   * Transform raw data, fill in 2 data marts: candles and macd
   */
  def main(args: Array[String]): Unit = {

    val jobNames: Seq[String] = if (args.nonEmpty) {
      log.info("Got job from args")
      args.flatMap(arg => arg.split("\\s*,\\s*"))
    } else {
      log.info(f"Jobs are not set, using default sequence $defaultProcessSeq")
      defaultProcessSeq
    } //.orElse(defaultProcessSeq)

    log.info(s"Jobs to run: ${jobNames.mkString(",")}")
    // Set database
    AppTool.ensureHiveDb
    // Run jobs one by one
    jobNames.foreach(runJob)
  }

  def runJob(name: String): Unit = {
    name.toLowerCase match {
      case "raw2ohlcv" => raw2Ohlcv()
      case "raw2csv" => raw2Csv()
      case "ohlcv2macd" => ohlcv2Macd()
      case "ohlcv2psql" => hive2Psql(ohlcvTableName)
      case "macd2psql" => hive2Psql(macdTableName)
      case "ohlcv2click" => hive2Click(ohlcvTableName)
      case "macd2click" => hive2Click(ohlcvTableName)
      case "ohlcv2mongo" => hive2Mongo(ohlcvTableName)
      case "macd2mongo" => hive2Mongo(macdTableName)
      case x => log.info(s"Not found the job with name: $x")
    }
  }

  /**
   * Hive -> mongo, preserve table name
   */
  def hive2Mongo(tableName: String): Unit = {
    val uri = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.currency.mongodb.uri")
    val collection = tableName

    log.info(s"Read hive $tableName, write to mongodb $collection. Uri: $uri")
    val df = spark.read.table(tableName)
    val writeConfig = WriteConfig(Map(
      "spark.mongodb.output.uri" -> uri,
      "spark.mongodb.output.database" -> "cryptotrade",
      "spark.mongodb.output.collection" -> tableName))

    MongoSpark.save(df, writeConfig) // Write to mongo
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
      .mode(SaveMode.Append)
      .jdbc(url = jdbcUri, table = tableName, connectionProperties = props)
  }

  def hive2Click(tableName: String): Unit = {
    log.info(s"Read hive $tableName, write to click $tableName")

    val jdbcUri = spark.conf.get("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.click.uri")
    log.info(s"Read hive $tableName, write to clickhouse $tableName. Jdbc uri: $jdbcUri")

    // Set up properties
    val props = new Properties()
    props.put("Driver", "ru.yandex.clickhouse.ClickHouseDriver")
    spark.conf.getAll.filter(_._1.startsWith("dmitrypukhov.cryptotrade.data.mart.currency.jdbc.click"))
      .foreach(t => props.put(t._1.split("\\.").last, t._2)) // Filter props from config, set them up


    log.info(s"Properties: ${props.keys().asScala.toList.map(_.toString).filter(_ != "password").map(key => s"$key=${props.getProperty(key)}")}")
    // Read Hive, wite postgres
    spark.read.table(tableName)
      .write
      .mode(SaveMode.Append)
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
   * Raw hdfs -> csv ohlcv hdfs data mart
   */
  def raw2Csv(): Unit = {
    val ohlcvDir = s"$dataMartsDir/${symbol}_$interval"
    log.info(s"Transform $rawDir to $ohlcvDir table")
    spark.read.json(path = rawDir)
      .huobi2Ohlcv(symbol) // raw -> ohlcv
      .write.mode(SaveMode.Overwrite).csv(ohlcvDir)
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
