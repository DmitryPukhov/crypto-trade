root_dir=../../..
jar_name=cryptotrade-kafka-1.0-SNAPSHOT.jar
src_jar="$root_dir/cryptotrade-kafka/target/components/$jar_name"
tmp_dir=./tmp
mkdir -p $tmp_dir

echo "Copy $src_jar to $tmp_dir"
ls $root_dir
ls $src_jar
cp -f $src_jar "$tmp_dir/$jar_name"
ls $tmp_dir

echo "Create worker.properties in $tmp_dir"
host_name=$(yc managed-kafka cluster list-hosts --name cryptotrade-kafka | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n/ /g')
host_name=$(echo "$host_name" | xargs)
host="$host_name:9091"
bootstrap_servers_property="bootstrap\.servers"
echo "Set $bootstrap_servers_property=$host in worker.properties"
sed "s/\($bootstrap_servers_property\s*\=\s*\).*/\1${host}/g" "worker.properties.template" > "$tmp_dir/worker.properties"

echo "Create binance.connector.properties in $tmp-dir"
cp -f binance.connector.properties.template "$tmp_dir/binance.connector.properties"


echo "Build docker image"
sudo docker build .