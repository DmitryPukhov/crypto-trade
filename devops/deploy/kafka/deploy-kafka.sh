root_dir=../../..
jar_name_only="cryptotrade-kafka-1.0-SNAPSHOT"
jar="$jar_name_only.jar"
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

host_name=$(yc managed-kafka cluster list-hosts --name cryptotrade-kafka | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n/ /g')
host_name=$(echo "$host_name" | xargs)
host="$host_name:9091"
echo "Got mamaged kafka host:  $host"

echo "Create application.properties in $tmp_dir"
bootstrap_servers_property="dmitrypukhov\.cryptotrade\.kafka\.bootstrap\.servers"
echo "Set $bootstrap_servers_property=$host in application.properties"
sed "s/\($bootstrap_servers_property\s*\=\s*\).*/\1${host}/g" "application.properties.template" > "$tmp_dir/application.properties"
echo "Pack application.properties to jar"
zip -j "$tmp_dir/$jar" "$tmp_dir/application.properties"
zip -d "$tmp_dir/$jar" "application.dev.properties" # delete dev.properties

echo "Create worker.properties in $tmp_dir"
bootstrap_servers_property="bootstrap\.servers"
echo "Set $bootstrap_servers_property=$host in worker.properties"
sed "s/\($bootstrap_servers_property\s*\=\s*\).*/\1${host}/g" "worker.properties.template" > "$tmp_dir/worker.properties"

echo "Create binance.connector.properties in $tmp_dir"
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

# todo: use k8s manifests
set +e # Ignore deleting old pods errors if the pod does not exists in k8s
echo "Running container $kc_image_tag"
kubectl delete pod "cryptotrade-kafka-binance-connector"
kubectl run "cryptotrade-kafka-binance-connector" --image "$kc_image_tag"

echo "Running container $ks_image_tag"
kubectl delete pod "cryptotrade-kafka-streams"
kubectl run "cryptotrade-kafka-streams" --image "$ks_image_tag"