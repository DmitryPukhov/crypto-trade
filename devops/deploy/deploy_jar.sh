#############################################################################
# Deploy scala jar
#############################################################################
tmp_dir=./tmp/cryptotrade-spark
jar_name=cryptotrade-spark-assembly-0.1.0-SNAPSHOT.jar
jar_path="$tmp_dir/$jar_name"
app_properties_path="$tmp_dir/application.properties"
scala_code_dir=../../crypto-trade-spark
cloud_dir=s3://dmitrypukhov-cryptotrade/app/
ch_ssh_path=../.ssh/ch.CA.pem

set -e
# Build the jar, copy to current folder
echo "Build the jar"
rm $jar_path
mkdir -p $tmp_dir
cd $scala_code_dir || exit
sbt clean assembly # Build command
cd "$OLDPWD" || exit
cp -f $scala_code_dir/target/scala-2.12/$jar_name $jar_path

# Set postgres url in application.properties
pg_host_name=$(yc managed-postgresql host list --cluster-name cryptotrade-psql | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n/ /g')
pg_host_name=$(echo "$pg_host_name" | xargs)
pg_host="jdbc:postgresql:\/\/$pg_host_name\:6432\/cryptotrade"
pg_host_property="dmitrypukhov\.cryptotrade\.data\.mart\.currency\.jdbc\.psql\.uri"
echo "Set $pg_host_property=$pg_host in $app_properties_path"
sed "s/\($pg_host_property\s*\:\s*\).*/\1${pg_host}/g" application.properties.template > "$app_properties_path.tmp.1"

# Set clickhouse url in application.properties
ch_host_name=$(yc managed-clickhouse host list --cluster-name cryptotrade-clickhouse | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n/ /g')
ch_host_name=$(echo "$ch_host_name" | xargs)
ch_host="jdbc:clickhouse:\/\/$ch_host_name\:8443\/cryptotrade\?ssl=1\&sslmode=strict\&sslrootcert=ch.CA.pem"
ch_host_property="dmitrypukhov\.cryptotrade\.data\.mart\.currency\.jdbc\.click\.uri"
echo "Set $ch_host_property=$ch_host in $app_properties_path"
sed "s/\($ch_host_property\s*\:\s*\).*/\1${ch_host}/g" "$app_properties_path.tmp.1" > "$app_properties_path.tmp.2"

# Set mongo url in application.properties
mongo_host_name=$(yc managed-mongodb host list --cluster-name cryptotrade-mongodb | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n/ /g')
mongo_host_name=$(echo "$mongo_host_name" | xargs)
mongo_host="mongodb:\/\/cryptotrade:Wb2DRJq3cNOhN\@$mongo_host_name:27018\/\?replicaSet=rs01\&tls=true\&tlsCAFile=\/usr\/local\/share\/ca-certificates\/Yandex\/YandexInternalRootCA\.crt\&authSource=cryptotrade"
mongo_host_property="dmitrypukhov\.cryptotrade\.data\.mart\.currency\.mongodb\.uri"
echo "Set $mongo_host_property=$mongo_host in $app_properties_path"
sed "s/\($mongo_host_property\s*\:\s*\).*/\1${mongo_host}/g" "$app_properties_path.tmp.2" > $app_properties_path

rm "$app_properties_path.tmp.1"
rm "$app_properties_path.tmp.2"

# Pack application.properties to the jar as yandex lightweight dataproc cluster doesn't support passing the file when submitting
zip -j $jar_path $app_properties_path
# Pack clickhose certificate
zip -j $jar_path $ch_ssh_path

# Copy the jar to the cloud
echo "Copy $jar_path to $cloud_dir"
s3cmd put -f $jar_path $cloud_dir
