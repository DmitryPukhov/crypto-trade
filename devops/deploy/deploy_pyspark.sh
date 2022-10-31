#############################################################################
# Deploy python
#############################################################################
app_dir=../../cryptotrade-pyspark
tmp_dir=./tmp/cryptotrade-pyspark
tmp_app_dir=$tmp_dir/app
cloud_dir=s3://dmitrypukhov-cryptotrade/app/cryptotrade-pyspark
rm -r $tmp_dir
mkdir -p $tmp_dir

# Copy python app
echo "Copy $app_dir to $tmp_app_dir"
cp -r $app_dir $tmp_app_dir

# Remove not required files
rm -r $tmp_app_dir/__pycache__
rm -r $tmp_app_dir/*/__pycache__
rm $tmp_app_dir/cfg/application.dev.conf
rm $tmp_app_dir/*.iml

# Pack python libs
venv_lib_dir=../../venv/lib/python3.8/site-packages
dist_lib_dir=/usr/lib/python3/dist-packages
rm -f $tmp_dir/cryptotrade_libs.zip

# Copy config
cp -f app_conf.py.template $tmp_app_dir/cfg/app_conf.py

# Pack libraries
# huobi lib
cp -r -f $venv_lib_dir/huobi ./huobi
zip -r $tmp_dir/cryptotrade_libs.zip huobi
rm -r ./huobi
# yaml lib
cp -r -f $dist_lib_dir/yaml ./yaml
zip -r $tmp_dir/cryptotrade_libs.zip yaml
rm -r ./yaml

#app
cd $tmp_app_dir || exit
zip -r cryptotrade-pyspark.zip ./
cd "$OLDPWD" || exit
mv $tmp_app_dir/cryptotrade-pyspark.zip $tmp_dir/

# Copy the app to the cloud
echo "Copy $tmp_dir to $cloud_dir"
s3cmd rm -r $cloud_dir
s3cmd sync -f $tmp_app_dir/ $cloud_dir/cryptotrade-pyspark/
s3cmd put -f $tmp_dir/*.zip $cloud_dir/




