from datetime import datetime
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.utils.trigger_rule import TriggerRule
from DagTool import DagTool

with DAG(dag_id="batch_raw_ohlcv_macd",
         start_date=datetime(2021, 1, 1),
         tags=["cryptotrade"],
         catchup=False) as dag:
    # Create hadoop cluster
    # create_cluster = DagTool.create_cluster_operator(services=["SPARK", "YARN", "HIVE"])
    # Start hadoop cluster if is not
    ensure_cluster_running = PythonOperator(task_id="ensure_cluster_running", python_callable=DagTool.ensure_cluster_running)

    # Jobs
    raw2ohlcv = DagTool.spark_currency_job(args="raw2ohlcv")
    ohlcv2macd = DagTool.spark_currency_job(args="ohlcv2macd")

    # Stop cluster, save money
    stop_cluster = PythonOperator(task_id="stop_cluster", python_callable=DagTool.stop_cluster, trigger_rule=TriggerRule.ALL_DONE)
    # Delete cluster at the end
    # delete_cluster = DataprocDeleteClusterOperator(
    #     task_id="delete_hadoop_cluster", trigger_rule=TriggerRule.ALL_DONE)

    # Dag workflow
    ensure_cluster_running >> raw2ohlcv >> ohlcv2macd >> stop_cluster
