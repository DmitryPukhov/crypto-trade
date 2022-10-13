#############################################################################
# Deploy python
#############################################################################
app_dir=../../cryptotradespark
tmp_dir=./tmp/cryptotradespark
cloud_dir=s3://dmitrypukhov-cryptotrade/app/
mkdir -p $tmp_dir

# Copy python app
echo "Copy $app_dir to $tmp_dir"
rm -r $tmp_dir
cp -r $app_dir $tmp_dir

# Remove not required files
rm -r $tmp_dir/__pycache__
rm $tmp_dir/cfg/application.dev.conf
rm $tmp_dir/*.iml

# Pack python libs
venv_lib_dir=../../venv/lib/python3.8/site-packages
dist_lib_dir=/usr/lib/python3/dist-packages
tmp_lib_dir=$tmp_dir/lib
mkdir -p $tmp_lib_dir
rm -f $tmp_lib_dir/pytrade_libs.zip
# Pack libraries
cp -r -f $venv_lib_dir/huobi ./huobi
zip -r $tmp_lib_dir/pytrade_libs.zip huobi
rm -r ./huobi

cp -r -f $dist_lib_dir/yaml ./yaml
zip -r $tmp_lib_dir/pytrade_libs.zip yaml
rm -r ./yaml


# Copy the app to the cloud
echo "Copy $tmp_dir to $cloud_dir"
s3cmd sync -f $tmp_dir $cloud_dir




