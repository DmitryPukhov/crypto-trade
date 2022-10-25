name := "crypto-trade-spark"

// Versions
scalaVersion := "2.12.10"
javacOptions ++= Seq("-source", "11", "-target", "11")
val sparkVersion = "3.0.3"
val typesafeConfigVersion = "1.4.2"
val postgresqlVersion = "42.5.0"
val clickhouseVersion = "0.3.2"
val mongodbVersion = "3.0.2"

// Libraries
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-hive" % sparkVersion % "provided"

libraryDependencies += "com.typesafe" % "config" % typesafeConfigVersion
libraryDependencies += "org.postgresql" % "postgresql" % postgresqlVersion
libraryDependencies += "com.clickhouse" % "clickhouse-jdbc" % clickhouseVersion
libraryDependencies += "ru.yandex.clickhouse" % "clickhouse-jdbc" % clickhouseVersion
libraryDependencies += "org.mongodb.spark" %% "mongo-spark-connector" % mongodbVersion

// Include minimum libs to the jar
ThisBuild / assemblyMergeStrategy := {
  case PathList("dmitrypukhov", xs@_*) => MergeStrategy.last
  case PathList("com", "typesafe", xs@_*) => MergeStrategy.last
  case PathList("org", "postgresql", xs@_*) => MergeStrategy.last
  case PathList("com", "clickhouse", xs@_*) => MergeStrategy.last
  case PathList("ru", "yandex", xs@_*) => MergeStrategy.last
  case PathList("org", "mongodb", xs@_*) => MergeStrategy.last
  case PathList("com", "mongodb", xs@_*) => MergeStrategy.last
  case PathList("org", "bson", xs@_*) => MergeStrategy.last
  case PathList("application.defaults.conf", xs @ _*) => MergeStrategy.last
  case PathList("application.dev.conf", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.discard
}