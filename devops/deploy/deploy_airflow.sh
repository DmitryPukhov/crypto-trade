src_dags=../../cryptotrade-airflow/dags
dst_dags=/home/airflow/dags
ls $src_dags

airflow_address=$(yc compute instance list | grep airflow | awk '{ print $10}')
echo "Got airflow address: $airflow_address"
dst_uri=ubuntu@$airflow_address:$dst_dags
echo "Airflow dags uri: $dst_uri"
rsync -r --rsync-path="sudo rsync" $src_dags/ $dst_uri