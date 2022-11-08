
echo "Create serverless container if does not exist"
binance_connector_name="binance-kafka-connector"
if [ -z "$(yc serverless container list | grep "$binance_connector_name")" ]
then
  yc serverless container create --name $binance_connector_name
fi


echo "Deploy image $image_tag to serverless container"
service_account_id="$(yc iam service-account get cryptotrade-hadoop | head -n 1 | awk '{print $2}')"
yc serverless container revision deploy \
  --container-name $binance_connector_name \
  --image $image_tag \
  --cores 1 \
  --memory 1GB \
  --concurrency 1 \
  --execution-timeout 30s \
  --service-account-id $service_account_id