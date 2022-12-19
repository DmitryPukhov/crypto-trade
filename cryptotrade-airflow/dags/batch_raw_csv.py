import logging
from datetime import datetime
from airflow import DAG
from airflow.providers.yandex.operators.yandexcloud_dataproc import (
    DataprocCreateClusterOperator,
    DataprocDeleteClusterOperator,
    DataprocCreateSparkJobOperator
)
from airflow.utils.trigger_rule import TriggerRule
from AppTool import AppTool
from CloudTool import CloudTool

# Read config
cfg_path = ["/home/airflow/dags/cfg/application.defaults.conf", "/home/airflow/dags/cfg/application.conf"]
cfg = AppTool.read_config(*cfg_path)
app_dir = cfg["dmitrypukhov.cryptotrade.app_dir"]
cloud_tool = CloudTool(token=cfg["dmitrypukhov.cryptotrade.token"])

with DAG(dag_id="batch_raw_csv",
         start_date=datetime(2021, 1, 1),
         tags=["cryptotrade"],
         catchup=False) as dag:
    create_cluster = DataprocCreateClusterOperator(
        task_id='create_hadoop_cluster',
        cluster_name=cfg["dmitrypukhov.cryptotrade.hadoop.cluster_name"],
        zone=cfg["dmitrypukhov.cryptotrade.hadoop.zone_id"],
        s3_bucket=cfg["dmitrypukhov.cryptotrade.bucket"],
        services=["SPARK", "YARN"],
        computenode_count=2,
        datanode_count=0,
        service_account_id=cloud_tool.get_sa_id("cryptotrade-hadoop"),
        ssh_public_keys=cfg["dmitrypukhov.cryptotrade.hadoop.ssh_pub_key"]
    )
    # Spark job
    process = DataprocCreateSparkJobOperator(
        task_id="batch_raw_csv",
        main_jar_file_uri=f"{app_dir}/cryptotrade-spark-assembly-0.1.0-SNAPSHOT.jar",
        main_class="dmitrypukhov.cryptotrade.process.CurrencyJob",
        args=["raw2csv"]
    )

    delete_cluster = DataprocDeleteClusterOperator(
        task_id="delete_hadoop_cluster", trigger_rule=TriggerRule.ALL_DONE
    )

# Workflow: create cluster - job - delete cluster
create_cluster >> process >> delete_cluster
