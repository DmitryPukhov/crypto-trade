echo "Getting image tags"
registry_id=$(yc container registry list | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n//g')
svc_img_tag="cr.yandex/$registry_id/cryptotrade-service"

# todo: use k8s manifests
set +e # Ignore deleting old pods errors if the pod does not exists in k8s
echo "Restarting pod $svc_img_tag"
kubectl delete pod "cryptotrade-service"
kubectl run "cryptotrade-service" --image "$svc_img_tag"

# Display pods for check
kubectl get pods