## This demo demonstrates HBase utlizing REST API, angularjs, leafletjs, bootstrap


#### Demo overview

- The Data-Set Overview and Assumptions -
  - Data-set description -> http://chriswhong.com/open-data/foil_nyc_taxi/
  - Download NY City Freedon of Information Law (FOIL) data set from [here](http://chriswhong.com/open-data/foil_nyc_taxi/)
  - As described in the 'data-set description' (above), all personal Taxi data (Medallion and License) has been obfuscated.
  - The original data provided from the NY City cab data is aggregated per trip data, and does not provide second-by-second
     GPS details. 
  - However, for the sake of this project only, I provided few (only few) second-by-second GPS trips of my own, and
     inserted those into the Hbase trip table that I created for this project. This was done in order to augment the aggregate trip data with second-by-second GPS information.
  - The intent here is to demonstrate 'random access' of HBase using various GUI frameworks such as AngularJS, OpenMaps, Bootstrap, jqury, etc. 
  - It is not the intent of this project to  demonstrate the non-functional or administrative capabilities of HBase.
  - The data-set I am using in this demo is limited to 1000 rows only. 
  - There will be two projects one using the HBase REST API and the other will be using Phoenix;   

  - Author: Alexander Daher, Solution Engineer, Hortonworks.

##### Setup 

These setup steps are only needed first time you deploy the project

- Download HDP 2.3.2 sandbox VM image (Sandbox_HDP_2.3.2_VMware.ova) from [Hortonworks website](http://hortonworks.com/products/hortonworks-sandbox/#install)
- Import Sandbox_HDP_2.3.2_VMware.ova into VMWare and configure its memory size to be at least 8GB RAM 
- Find the IP address of the VM and add an entry into your machines hosts file e.g.
```
192.168.15.171 sandbox.hortonworks.com sandbox    
```
- Connect to the VM via SSH (password hadoop or whatever you set it to while installing the hortonworks sandbox)
```
ssh root@sandbox.hortonworks.com
```
- Pull latest code/sample documents and of HBase-leaflet
```
cd /root
git clone https://github.com/adaherhw/hbaseleaflet_rest.git
~/hbaseleaflet/leaflet-hbase-rest/demo.sh
```
#Step 1: start hbase in ambari
## Make sure HBase has been started

#Step 2: create hbase table, consisting of two Column Families (trip, fare); fare is not being used in these projects (as of now).
## creating hbase table
```
hbase shell -n 
create_namespace 'trip_ns'
create 'trip_ns:trip_table', {NAME => 'trip', VERSIONS => 1}, {NAME => 'fare', VERSIONS => 1}
```

#Step 3: insert into hbase table 
## First remove the header row from file cars_key.csv
```
awk 'BEGIN {FS=OFS=","} {print $1":"$6,$1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14}' data/cars.csv > car_key.csv
hdfs dfs -mkdir /tripstaging
hdfs dfs -put car_key.csv /tripstaging
```

## Second, ingest cars_key.csv into Hbase trip_table in name space trip_ns; Here schema attributes are define 'on-read', using HBase import Utility.
```
hbase org.apache.hadoop.hbase.mapreduce.ImportTsv -Dimporttsv.separator=, -Dimporttsv.columns=HBASE_ROW_KEY,trip:medallion,trip:hack_license,trip:vendor_id,trip:rate_code,trip:store_and_fwd_flag,trip:pickup_datetime,trip:dropoff_datetime,trip:passenger_count,trip:trip_time_in_secs,trip:trip_distance,trip:pickup_longitude,trip:pickup_latitude,trip:dropoff_longitude,trip:dropoff_latitude trip_ns:trip_table /tripstaging/car_key.csv
```
#Step 4: build java project to generate jar and war files
###building java project
```

ln -s /root/leaflet-hbase-rest/apache-ant-1.9.6/bin/ant /usr/bin/ant
cd leaflet-hbase-rest
ant clean build
cd ..
```
#Step 5: insert trip route data and aggregate json in hbase table
```
"insert trip route data and and aggregate json in hbase table"
java -cp leaflet-hbase-rest/leaflet-hbase-rest.jar:/usr/hdp/current/hadoop-client/hadoop-common.jar:/usr/hdp/current/hadoop-client/hadoop-annotations.jar:/usr/hdp/current/hadoop-client/hadoop-auth.jar:/usr/hdp/current/hadoop-client/lib/*:/usr/hdp/current/hadoop-hdfs-client/hadoop-hdfs.jar:/usr/hdp/current/hbase-client/lib/* taxi.leaflet.HBaseInsertDetail
```
```
java -cp leaflet-hbase-rest/leaflet-hbase-rest.jar:/usr/hdp/current/hadoop-client/hadoop-common.jar:/usr/hdp/current/hadoop-client/hadoop-annotations.jar:/usr/hdp/current/hadoop-client/hadoop-auth.jar:/usr/hdp/current/hadoop-client/lib/*:/usr/hdp/current/hadoop-hdfs-client/hadoop-hdfs.jar:/usr/hdp/current/hbase-client/lib/* taxi.leaflet.HBaseInsertAggregate
```

#Step 5: start hbase rest server
"start hbase rest server"
```

start-hbase-rest.sh
```
#Step 6: start tomcat server at port 8086
#tomcat conf/server.xml has been modified
```

echo "start tomcat server at 8086"
start-tomcat.sh
```
#Step 7: deploy java war to tomcat
"deploy java project to tomcat"
```

cp leaflet-hbase-rest/leaflet-hbase-rest.war apache-tomcat-7.0.67/webapps
```

goto http://sandbox.hortonworks.com:8086/leaflet-hbase-rest/carmap.html or
(goto http://YOUR_OWN_SANBBOX_IP:8086/leaflet-hbase-rest/carmap.html)

#Step 8: Enter a medallion in the input box, and click query button. The result will be displayed in the Angularjs Directives .
Below are some sample medallions:
```
171FD91CBF0D6FABF499A9452A4950D5
0B3D3D51C78E944F68DC04209E86D5F7
BB8B5987269FE82C5A9CAA78DE6E2F8D
```
