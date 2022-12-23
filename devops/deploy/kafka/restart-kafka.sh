echo "Getting image tags"
registry_id=$(yc container registry list | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n//g')
kc_image_tag="cr.yandex/$registry_id/cryptotrade-kafka-connect"
ks_image_tag="cr.yandex/$registry_id/cryptotrade-kafka-streams"

# todo: use k8s manifests
set +e # Ignore deleting old pods errors if the pod does not exists in k8s
echo "Restarting pod $kc_image_tag"
kubectl delete pod "cryptotrade-kafka-binance-connector"
kubectl run "cryptotrade-kafka-binance-connector" --image "$kc_image_tag"

echo "Restarting pod $ks_image_tag"
kubectl delete pod "cryptotrade-kafka-streams"
kubectl run "cryptotrade-kafka-streams" --image "$ks_image_tag"

# Display pods for check
kubectl get pods