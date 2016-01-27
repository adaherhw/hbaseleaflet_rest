#step 1: start hbase in ambari
echo "make sure HBase is started"

#step 2: create hbase table
echo "creating hbase table"
hbase shell -n <<EOF
create_namespace 'trip_ns'
create 'trip_ns:trip_table', {NAME => 'trip', VERSIONS => 1}, {NAME => 'fare', VERSIONS => 1}
EOF

#step 3: insert hbase table
echo "insert hbase table"
awk 'BEGIN {FS=OFS=","} {print $1":"$6,$1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14}' data/cars.csv > car_key.csv
hdfs dfs -mkdir /tripstaging
hdfs dfs -put car_key.csv /tripstaging
hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.separator=, -Dimporttsv.columns=HBASE_ROW_KEY,trip:medallion,trip:hack_license,trip:vendor_id,trip:rate_code,trip:store_and_fwd_flag,trip:pickup_datetime,trip:dropoff_datetime,trip:passenger_count,trip:trip_time_in_secs,trip:trip_distance,trip:pickup_longitude,trip:pickup_latitude,trip:dropoff_longitude,trip:dropoff_latitude trip_ns:trip_table /tripstaging/car_key.csv

#step 3.5: build java project to generate jar and war files
echo "building java project"
ln -s /root/leaflet-hbase-rest/apache-ant-1.9.6/bin/ant /usr/bin/ant
cd leaflet-hbase-rest
ant clean build
cd ..

#step 4: insert trip route data and aggregate json in hbase table
echo "insert trip route data and and aggregate json in hbase table"
java -cp leaflet-hbase-rest/leaflet-hbase-rest.jar:/usr/hdp/current/hadoop-client/hadoop-common.jar:/usr/hdp/current/hadoop-client/hadoop-annotations.jar:/usr/hdp/current/hadoop-client/hadoop-auth.jar:/usr/hdp/current/hadoop-client/lib/*:/usr/hdp/current/hadoop-hdfs-client/hadoop-hdfs.jar:/usr/hdp/current/hbase-client/lib/* taxi.leaflet.HBaseInsertDetail

java -cp leaflet-hbase-rest/leaflet-hbase-rest.jar:/usr/hdp/current/hadoop-client/hadoop-common.jar:/usr/hdp/current/hadoop-client/hadoop-annotations.jar:/usr/hdp/current/hadoop-client/hadoop-auth.jar:/usr/hdp/current/hadoop-client/lib/*:/usr/hdp/current/hadoop-hdfs-client/hadoop-hdfs.jar:/usr/hdp/current/hbase-client/lib/* taxi.leaflet.HBaseInsertAggregate

#step 5: start hbase rest server
echo "start hbase rest server"
./start-hbase-rest.sh

#step 6: start tomcat server at port 8086
#tomcat conf/server.xml has been modified
echo "start tomcat server at 8086"
./start-tomcat.sh

#step 7: deploy java war to tomcat
echo "deploy java project to tomcat"
cp leaflet-hbase-rest/leaflet-hbase-rest.war apache-tomcat-7.0.67/webapps

echo "goto http://sandbox.hortonworks.com:8086/leaflet-hbase-rest/carmap.html"

