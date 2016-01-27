package taxi.leaflet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class HBaseInsertAggregate {
        public static void main(String[] args) throws Exception {
                new HBaseInsertAggregate().go();
        }
 
        private Map<String, Map<String, Map<String, Trip>>> group = new HashMap<String, Map<String, Map<String, Trip>>>();

        public void go() throws Exception {
                sortData();
                insertAggregate();
        }

        public void sortData() throws Exception {
                BufferedReader br = new BufferedReader(new FileReader("data/cars.csv"));
                String line;
                while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        Trip trip = new Trip(line);
                        if (!group.containsKey(trip.getMedallion())) {
                                group.put(trip.getMedallion(), new TreeMap<String, Map<String, Trip>>());
                        }
                        if (!group.get(trip.getMedallion()).containsKey(trip.getTripDate())) {
                                group.get(trip.getMedallion()).put(trip.getTripDate(), new TreeMap<String, Trip>());
                        }
                        group.get(trip.getMedallion()).get(trip.getTripDate()).put(trip.getTripDatetime(), trip);
                }
                br.close();
        }

        private JsonObject wrapJson(Trip trip) {
                JsonObject tripJson = new JsonObject();
                tripJson.add("trip:pickup_datetime", new JsonPrimitive(trip.get("trip:pickup_datetime")));
                tripJson.add("trip:dropoff_datetime", new JsonPrimitive(trip.get("trip:dropoff_datetime")));
                tripJson.add("trip:passenger_count", new JsonPrimitive(Integer.valueOf(trip.get("trip:passenger_count"))));
                tripJson.add("trip:trip_time_in_secs", new JsonPrimitive(Integer.valueOf(trip.get("trip:trip_time_in_secs"))));
                tripJson.add("trip:trip_distance", new JsonPrimitive(Double.valueOf(trip.get("trip:trip_distance"))));
                tripJson.add("trip:pickup_location", new JsonArray());
                tripJson.get("trip:pickup_location").getAsJsonArray().add(new JsonPrimitive(Double.valueOf(trip.get("trip:pickup_latitude"))));
                tripJson.get("trip:pickup_location").getAsJsonArray().add(new JsonPrimitive(Double.valueOf(trip.get("trip:pickup_longitude"))));
                tripJson.add("trip:dropoff_location", new JsonArray());
                tripJson.get("trip:dropoff_location").getAsJsonArray().add(new JsonPrimitive(Double.valueOf(trip.get("trip:dropoff_latitude"))));
                tripJson.get("trip:dropoff_location").getAsJsonArray().add(new JsonPrimitive(Double.valueOf(trip.get("trip:dropoff_longitude"))));
                return tripJson;
        }

        public void insertAggregate() throws Exception {
                Configuration config = HBaseConfiguration.create();
                config.addResource(new Path("/usr/hdp/current/hbase-master/conf", "hbase-site.xml"));
                config.addResource(new Path("/usr/hdp/current/hbase-master/conf", "core-site.xml"));
                Connection connection = ConnectionFactory.createConnection(config);

                Table table = connection.getTable(TableName.valueOf("trip_ns:trip_table"));
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                for (String medallion: group.keySet()) {
                        JsonObject days = new JsonObject();
                        for (String date: group.get(medallion).keySet()) {
                                days.add(date, new JsonArray());
                                for (String datetime: group.get(medallion).get(date).keySet()) {
                                        Trip trip = group.get(medallion).get(date).get(datetime);
                                        days.get(date).getAsJsonArray().add(wrapJson(trip));
                                }
                        }
                        System.out.println(medallion);

                        String rowkey = medallion + ":";
                        Put p = new Put(Bytes.toBytes(rowkey));
                        p.addColumn(Bytes.toBytes("trip"), Bytes.toBytes("total"), Bytes.toBytes(gson.toJson(days)));
                        table.put(p);
                }
                table.close();

                connection.close();
                System.out.println("done.");
        }
}
