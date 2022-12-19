import logging
from datetime import datetime
from airflow import DAG
from airflow.providers.yandex.operators.yandexcloud_dataproc import (
    DataprocCreateClusterOperator,
    DataprocCreatePysparkJobOperator,
    DataprocDeleteClusterOperator,
)
from airflow.utils.trigger_rule import TriggerRule
from AppTool import AppTool
from CloudTool import CloudTool


with DAG(dag_id="batch_huobi_import",
         start_date=datetime(2021, 1, 1),
         tags=["cryptotrade"],
         catchup=False) as dag:
    # Read config
    cfg_path = ["/home/airflow/dags/cfg/application.defaults.conf", "/home/airflow/dags/cfg/application.conf"]
    cfg = AppTool.read_config(*cfg_path)
    logging.info(f"Loaded config: {cfg}")
    cloud_tool = CloudTool(token=cfg["dmitrypukhov.cryptotrade.token"])
    app_dir = cfg["dmitrypukhov.cryptotrade.app_dir"]

    # Create hadoop cluster
    create_cluster = DataprocCreateClusterOperator(
        task_id='create_hadoop_cluster',
        cluster_name=cfg["dmitrypukhov.cryptotrade.hadoop.cluster_name"],
        zone=cfg["dmitrypukhov.cryptotrade.hadoop.zone_id"],
        s3_bucket=cfg["dmitrypukhov.cryptotrade.bucket"],
        computenode_count=2,
        datanode_count=0,
        services=["SPARK", "YARN"],
        service_account_id=cloud_tool.get_sa_id("cryptotrade-hadoop"),
        ssh_public_keys=cfg["dmitrypukhov.cryptotrade.hadoop.ssh_pub_key"]
    )
    # Import
    currency_import_spark_job = DataprocCreatePysparkJobOperator(
        task_id="batch_huobi_import",
        main_python_file_uri=f"{app_dir}/cryptotrade-pyspark/cryptotrade-pyspark/input/BatchHuobiImport.py",
        python_file_uris=[f"{app_dir}/cryptotrade-pyspark/cryptotrade-pyspark.zip",
                          f"{app_dir}/cryptotrade-pyspark/cryptotrade_libs.zip"],
        properties={"spark.pyspark.python": "python3"}
    )
    # Delete cluster an the end
    delete_cluster = DataprocDeleteClusterOperator(
        task_id="delete_hadoop_cluster", trigger_rule=TriggerRule.ALL_DONE
    )

    # Workflow: create cluster - job - delete cluster
    create_cluster >> currency_import_spark_job >> delete_cluster
