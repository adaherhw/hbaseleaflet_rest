hbase shell -n <<EOF
disable 'trip_ns:trip_table'
drop 'trip_ns:trip_table'
drop_namespace 'trip_ns'
EOF

rm -rf car_key.csv
hdfs dfs -rm -r -f -skipTrash /tripstaging


if [ `ps -eaf | grep 1234[5] | wc -l` == 1 ]; then
kill -9 `ps -eaf | grep 1234[5] | awk '{print $2}'`
fi

if [ `ps -eaf | grep tomca[t] | grep leaflet | wc -l` == 1 ]; then
kill -9 `ps -eaf | grep tomca[t] | grep leaflet | awk '{print $2}'`
fi

