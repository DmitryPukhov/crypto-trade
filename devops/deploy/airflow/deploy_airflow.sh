src_dags=../../../cryptotrade-airflow/dags
dst_dags=/home/airflow/dags
tmp_dir=./tmp
tmp_dags=$tmp_dir/dags
tmp_pylibs=$tmp_dir/pylibs
common_module=../../../cryptotrade-pycommon
zip_file="pylibs.zip"

############## Copy dags to tmp dir ####################
echo "Copy dags to temp dir"
rm -rf $tmp_dir
mkdir -p $tmp_dir
echo "Copy $src_dags to $tmp_dags"
cp -rf src_dags $tmp_dags

venv_lib_dir=../../venv/lib/python3.8/site-packages
dist_lib_dir=/usr/lib/python3/dist-packages
rm -f $tmp_dir/$zip_file


# Copy python libs
echo "Packing libs"
for module in  "yaml" ; do
  echo "Copy $module module to $tmp_pylibs"
  mkdir -p $tmp_pylibs/$module
  cp -r -f $dist_lib_dir/$module $tmp_pylibs
done

# Copy common pytools project
echo "Copy common pytools to $tmp_dags"
cp -r -f $common_module $tmp_dags
ls ./tmp/pylibs/cryptotrade-pycommon
for pattern in "*.iml" "requirements.txt"; do rm ./tmp/pylibs/cryptotrade-pycommon/$pattern ; done
#zip -r $tmp_dir/$zip_file yaml # zip python module to folder in zip file
#app
echo "Packing $tmp_dir/$zip_file"
cd $tmp_pylibs || exit
zip -r ../$zip_file ./
#mv $tmp_dir/$zip_file $tmp_dir/
cd "$OLDPWD" || exit

# Copy dags to tmp dir
echo "Copy $src_dags to $tmp_dir"
cp -r -f $src_dags $tmp_dir
mv $tmp_dir/$zip_file $tmp_dir/dags

# Copy application.conf to tmp dir
echo "Copy application.conf.template to $tmp_dir/dags/cfg"
cp -f application.conf.template $tmp_dir/dags/cfg/application.conf
# Copy to remote airflow server
airflow_address=$(yc compute instance list | grep airflow | awk '{ print $10}')
echo "Got airflow address: $airflow_address"
dst_uri=ubuntu@$airflow_address:$dst_dags
echo "Airflow dags uri: $dst_uri"

echo "Copy dags from $tmp_dir to $dst_uri"
rsync -r --rsync-path="sudo rsync" $tmp_dir/dags/ $dst_uri