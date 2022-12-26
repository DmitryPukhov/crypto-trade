#############################################################################
# Create jar
#############################################################################
root_dir=../../..
java_code_dir=$root_dir/cryptotrade-service
tmp_dir=./tmp
jar_name=cryptotrade-service-0.0.1-SNAPSHOT.jar
jar_path="$tmp_dir/$jar_name"
app_properties_path="$tmp_dir/application.properties"
cloud_dir=s3://dmitrypukhov-cryptotrade/app/service

set -e
# Build the jar, copy to current folder
echo "Build the jar"
rm -f $jar_path
mkdir -p $tmp_dir
cd $java_code_dir || exit
mvn clean package # Build command
cd "$OLDPWD" || exit
cp -f $java_code_dir/target/$jar_name $jar_path

# Set postgres url in application.properties
pg_host_name=$(yc managed-postgresql host list --cluster-name cryptotrade-psql | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n/ /g')
pg_host_name=$(echo "$pg_host_name" | xargs)
pg_host="jdbc:postgresql:\/\/$pg_host_name\:6432\/cryptotrade"
pg_host_property="spring\.datasource\.url"
echo "Set $pg_host_property=$pg_host in $app_properties_path"
sed "s/\($pg_host_property\s*\=\s*\).*/\1${pg_host}/g" application.properties.template > "$app_properties_path"


# Pack application.properties to the jar
zip -j $jar_path $app_properties_path

# Copy the jar to the cloud for serverless
#echo "Copy $jar_path to $cloud_dir"
#s3cmd put -f $jar_path $cloud_dir

echo "Build docker image"
registry_id=$(yc container registry list | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n//g')
service_image_tag="cr.yandex/$registry_id/cryptotrade-service"
cp -f Dockerfile $tmp_dir/
cd $tmp_dir
docker build -t "$service_image_tag" .
cd "$OLDPWD" || exit
docker push "$service_image_tag"

echo "Running container $service_image_tag"
kubectl delete pod "cryptotrade-service"
kubectl run "cryptotrade-service" --image "$service_image_tag"


