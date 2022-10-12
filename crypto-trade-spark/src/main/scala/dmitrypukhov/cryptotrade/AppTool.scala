package dmitrypukhov.cryptotrade

import com.typesafe.config.{ConfigFactory, ConfigParseOptions}
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import scala.collection.JavaConverters.asScalaSetConverter


/**
 * Common tools
 */
object AppTool extends Serializable {
  private val log = Logger.getLogger(getClass)

  /**
   * Database set up
   */
  def ensureHiveDb(implicit spark: SparkSession): Unit = {
    val dbName = "cryptotrade"
    log.info(s"Hive db $dbName will be used")
    spark.sql(s"CREATE DATABASE IF NOT EXISTS $dbName")
    spark.sql(s"USE $dbName")
  }

  /**
   * Spark session unitialization util
   */
  def initSpark(): SparkSession =
    // Create spark session
    SparkSession.builder().enableHiveSupport().config(config).getOrCreate()

  /**
   * SparkConf with all spark and typesafe config properties
   * Priority (left is more important): System env, application.conf, default spark conf
   * todo: reduce custom code, simplify
   */
  private lazy val config = {
    // Start from default spark conf
    val conf = new SparkConf(true)
    // Application.conf overrides default spark conf
    val typesafeConf =
      ConfigFactory.load(ConfigParseOptions.defaults())
        // application.dev.conf is for local run only, not included in prod assembly
        .withFallback(ConfigFactory.load("application.dev.conf"))
        .withFallback(ConfigFactory.load("application.defaults.conf"))
    conf.setAll(typesafeConf.entrySet().asScala.map(e => (e.getKey, e.getValue.unwrapped().toString)))

    // System environment overrides spark+typesafe
    conf.setAll(System.getenv().entrySet().asScala.map(e => (e.getKey, e.getValue)))

    // Print config to log
    println("Actual config")
    conf.getAll.map(keyVal => f"${keyVal._1}=${keyVal._2}").foreach(println)

    conf
  }

}
