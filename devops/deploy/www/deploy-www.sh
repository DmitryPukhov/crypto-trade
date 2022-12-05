root_dir=../../..
src_dir=$root_dir/www/cryptotrade
tmp_dir=./tmp
rm -r -f $tmp_dir
mkdir -p $tmp_dir

echo "Copy files"
cp -r $src_dir/* $tmp_dir/
cp Dockerfile $tmp_dir

echo "Building docker"
cd $tmp_dir
docker build -t cryptotrade-www .
cd "$OLDPWD" || exit