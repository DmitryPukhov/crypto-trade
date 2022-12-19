from datetime import datetime
from airflow import DAG
from airflow.providers.yandex.operators.yandexcloud_dataproc import DataprocDeleteClusterOperator
from airflow.utils.trigger_rule import TriggerRule
from DagTool import DagTool

with DAG(dag_id="batch_raw_ohlcv_macd",
         start_date=datetime(2021, 1, 1),
         tags=["cryptotrade"],
         catchup=False) as dag:
    # Create hadoop cluster
    create_cluster = DagTool.create_cluster_operator(services=["SPARK", "YARN", "HIVE"])

    # Jobs
    raw2ohlcv = DagTool.spark_currency_job(args="raw2ohlcv")
    ohlcv2macd = DagTool.spark_currency_job(args="ohlcv2macd")

    # Delete cluster at the end
    delete_cluster = DataprocDeleteClusterOperator(
        task_id="delete_hadoop_cluster", trigger_rule=TriggerRule.ALL_DONE)

    # Dag workflow
    create_cluster >> raw2ohlcv >> ohlcv2macd >> delete_cluster
