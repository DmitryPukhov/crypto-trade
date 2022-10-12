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

# Copy the jar to the cloud

echo "Copy $tmp_dir to $cloud_dir"
s3cmd sync -f $tmp_dir $cloud_dir



