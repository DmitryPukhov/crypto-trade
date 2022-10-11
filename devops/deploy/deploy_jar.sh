jar_name=crypto-trade-spark-assembly-0.1.0-SNAPSHOT.jar
code_dir=../../crypto-trade-spark

set -e
# Build the jar, copy to current folder
echo "Build the jar"
cd $code_dir || exit
sbt clean assembly
cd "$OLDPWD" || exit
cp -f $code_dir/target/scala-2.12/$jar_name ./$jar_name

# Set postgres url in application.properties
pg_host_name=$(yc managed-postgresql host list --cluster-name cryptotrade-psql | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n/ /g')
pg_host_name=$(echo "$pg_host_name" | xargs)
pg_host="jdbc:postgresql:\/\/$pg_host_name\:6432\/cryptotrade"
pg_host_property="dmitrypukhov\.cryptotrade\.data\.mart\.btcusdt\.jdbc\.uri"
echo "Set $pg_host_property=$pg_host"
sed "s/\($pg_host_property\s*=\s*\).*/\1${pg_host}/g" application.properties.template > application.properties

# Pack application.properties to the jar as yandex lightweight dataproc cluster doesn't support passing the file when submitting
zip $jar_name application.properties

# Copy the jar to the cloud
cloud_dir=s3://dmitrypukhov-cryptotrade/app/
echo "Copy $jar_name to $cloud_dir"
s3cmd put -f $jar_name s3://dmitrypukhov-cryptotrade/app/$jar_name



