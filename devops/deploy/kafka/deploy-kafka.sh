root_dir=../../..
jar_name_only="cryptotrade-kafka-1.0-SNAPSHOT"
src_jar="$root_dir/cryptotrade-kafka/target/components/$jar_name_only.jar"
src_lib_dir="$root_dir/cryptotrade-kafka/target/components/packages/dmitrypukhov-$jar_name_only/dmitrypukhov-$jar_name_only/lib"
tmp_dir=./tmp

set -e

rm -r -f $tmp_dir
mkdir -p $tmp_dir

echo "Build the jar"
cd $root_dir/cryptotrade-kafka
mvn clean package
cd "$OLDPWD" || exit


echo "Copy $src_jar with libs to $tmp_dir"
ls $src_lib_dir/*
cp $src_lib_dir/*.jar $tmp_dir

echo "Create worker.properties in $tmp_dir"
host_name=$(yc managed-kafka cluster list-hosts --name cryptotrade-kafka | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n/ /g')
host_name=$(echo "$host_name" | xargs)
host="$host_name:9091"
bootstrap_servers_property="bootstrap\.servers"
echo "Set $bootstrap_servers_property=$host in worker.properties"
sed "s/\($bootstrap_servers_property\s*\=\s*\).*/\1${host}/g" "worker.properties.template" > "$tmp_dir/worker.properties"

echo "Create binance.connector.properties in $tmp-dir"
cp -f binance.connector.properties.template "$tmp_dir/binance.connector.properties"

echo "Copy certs"
cp -r .ssh $tmp_dir

echo "Build kafka-connect docker image"
registry_id=$(yc container registry list | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n//g')
kc_image_tag="cr.yandex/$registry_id/cryptotrade-kafka-connect"
sudo docker build -t "$kc_image_tag" -f Dockerfile-kafka-connect .

echo "Build kafka-streams docker image"
ks_image_tag="cr.yandex/$registry_id/cryptotrade-kafka-streams"
sudo docker build -t "$ks_image_tag" -f Dockerfile-kafka-streams .

echo "Pushing docker images"
docker push "$kc_image_tag"
docker push "$ks_image_tag"
