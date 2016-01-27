nohup hbase rest start --infoport 12346 --port 12345 > /tmp/hbase-rest.log 2>&1 &
echo "hbase rest server listening at port 12345"
