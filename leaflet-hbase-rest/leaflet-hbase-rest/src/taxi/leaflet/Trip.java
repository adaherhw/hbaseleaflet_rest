package taxi.leaflet;

import java.util.HashMap;
import java.util.Map;

public class Trip {

        private static final String columnString = "trip:medallion,trip:hack_license,trip:vendor_id,trip:rate_code,trip:store_and_fwd_flag,trip:pickup_datetime,trip:dropoff_datetime,trip:passenger_count,trip:trip_time_in_secs,trip:trip_distance,trip:pickup_longitude,trip:pickup_latitude,trip:dropoff_longitude,trip:dropoff_latitude";
        public static final Map<String, Integer> COLUMN = new HashMap<String, Integer>();
        static {
                String[] columns = columnString.split(",");
                for (int index = 0; index <columns.length; index ++) {
                        COLUMN.put(columns[index].trim(), index);
                }
        }

        private String[] splits;
        public Trip(String record) {
                splits = record.split(",");
        }

        public String get(String column) {
                Integer index = COLUMN.get(column);
                return index == null? null: splits[index];
        }

        public String getTripDatetime() {
                return splits[COLUMN.get("trip:pickup_datetime")];
        }

        public String getTripDate() {
                return getTripDatetime().split(" ")[0];
        }

        public String getMedallion() {
                return splits[COLUMN.get("trip:medallion")];
        }
}
