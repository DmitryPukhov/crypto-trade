import logging
from typing import List
from airflow.providers.yandex.operators.yandexcloud_dataproc import (
    DataprocCreateClusterOperator,
    DataprocCreatePysparkJobOperator,
    DataprocCreateSparkJobOperator
)
from AppTool import AppTool
from CloudTool import CloudTool


class DagTool:
    """   Reusable dag elements   """

    # Read config
    cfg_path = ["/home/airflow/dags/cfg/application.defaults.conf", "/home/airflow/dags/cfg/application.conf"]
    cfg = AppTool.read_config(*cfg_path)
    logging.info(f"Loaded config: {cfg}")
    cloud_tool = CloudTool(token=cfg["dmitrypukhov.cryptotrade.token"])
    app_dir = cfg["dmitrypukhov.cryptotrade.app_dir"]

    @staticmethod
    def create_cluster_operator(services: List[str]):
        return DataprocCreateClusterOperator(
            task_id='create_hadoop_cluster',
            cluster_name=DagTool.cfg["dmitrypukhov.cryptotrade.hadoop.cluster_name"],
            zone=DagTool.cfg["dmitrypukhov.cryptotrade.hadoop.zone_id"],
            s3_bucket=DagTool.cfg["dmitrypukhov.cryptotrade.bucket"],
            computenode_count=2,
            datanode_count=0,
            services=services,
            service_account_id=DagTool.cloud_tool.get_sa_id("cryptotrade-hadoop"),
            ssh_public_keys=DagTool.cfg["dmitrypukhov.cryptotrade.hadoop.ssh_pub_key"]
        )

    @staticmethod
    def spark_currency_job(args: str):
        return DataprocCreateSparkJobOperator(
            task_id=args,
            main_jar_file_uri=f"{DagTool.app_dir}/cryptotrade-spark-assembly-0.1.0-SNAPSHOT.jar",
            main_class="dmitrypukhov.cryptotrade.process.CurrencyJob",
            args=[args]
        )

    @staticmethod
    def pyspark_job(name: str, main_python_file_uri: str):
        return DataprocCreatePysparkJobOperator(
            task_id=name,
            # main_python_file_uri=f"{DagTool.app_dir}/cryptotrade-pyspark/cryptotrade-pyspark/input/BatchHuobiImport.py",
            main_python_file_uri=main_python_file_uri,
            python_file_uris=[f"{DagTool.app_dir}/cryptotrade-pyspark/cryptotrade-pyspark.zip",
                              f"{DagTool.app_dir}/cryptotrade-pyspark/cryptotrade_libs.zip"],
            properties={"spark.pyspark.python": "python3"}
        )
