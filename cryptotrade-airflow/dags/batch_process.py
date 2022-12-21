from datetime import datetime
from airflow import DAG
from airflow.operators.python import PythonOperator
from airflow.utils.trigger_rule import TriggerRule
from DagTool import DagTool

with DAG(dag_id="batch_process",
         start_date=datetime(2021, 1, 1),
         tags=["cryptotrade"],
         catchup=False) as dag:
    # Start hadoop cluster if is not
    ensure_cluster_running = PythonOperator(task_id="ensure_cluster_running",
                                            python_callable=DagTool.ensure_cluster_running)

    # Jobs
    raw2ohlcv = DagTool.spark_currency_job(args="raw2ohlcv")
    raw2csv = DagTool.spark_currency_job(args="raw2csv")
    ohlcv2macd = DagTool.spark_currency_job(args="ohlcv2macd")
    ohlcv2psql = DagTool.spark_currency_job(args="ohlcv2psql")
    macd2psql = DagTool.spark_currency_job(args="macd2psql")
    ohlcv2click = DagTool.spark_currency_job(args="ohlcv2click")
    macd2click = DagTool.spark_currency_job(args="macd2click")
    ohlcv2mongo = DagTool.spark_currency_job(args="ohlcv2mongo")
    macd2mongo = DagTool.spark_currency_job(args="macd2mongo")

    # Stop cluster, save money
    stop_cluster = PythonOperator(task_id="stop_cluster", python_callable=DagTool.stop_cluster,
                                  trigger_rule=TriggerRule.ALL_DONE)

    # Dag workflow
    ensure_cluster_running >> raw2ohlcv >> raw2csv >> ohlcv2macd >> ohlcv2psql >> macd2psql >> ohlcv2click \
    >> macd2click >> ohlcv2mongo >> macd2mongo >> stop_cluster
