root_dir=../../..
src_dir=$root_dir/www/cryptotrade
tmp_dir=./tmp
rm -r -f $tmp_dir
mkdir -p $tmp_dir

echo "Copy files"
cp -r $src_dir/* $tmp_dir/
cp Dockerfile $tmp_dir

echo "Build docker image"
registry_id=$(yc container registry list | awk '{print $2}' | tail -n 3 | sed ':a;N;$!ba;s/\n//g')
web_image_tag="cr.yandex/$registry_id/cryptotrade-web"
cp -f Dockerfile $tmp_dir/
cd $tmp_dir || exit
docker build -t "$web_image_tag" .
cd "$OLDPWD" || exit
docker push "$web_image_tag"

echo "Running container $web_image_tag"
kubectl delete pod "cryptotrade-web"
kubectl run "cryptotrade-web" --image "$web_image_tag"