package taxi.leaflet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseInsertDetail {
        public static void main(String[] args) throws Exception {
                new HBaseInsertDetail().go();
        }
 
        public void go() throws Exception {
                insertDetail();
        }

        public void insertDetail() throws Exception {
                //omnivore.kml('resources/WorkingTracks/kml/track2.kml')
                //omnivore.kml('resources/WorkingTracks/kml/track4.kml')
                //omnivore.kml('resources/WorkingTracks/kml/track5.kml')
                //omnivore.kml('resources/WorkingTracks/kml/2015-09-10_15_16_32.kml')
                //omnivore.kml('resources/WorkingTracks/kml/2015-09-10_17_59_14.kml')
                //omnivore.kml('resources/WorkingTracks/kml/2015-09-11_09_18_33.kml')
                //omnivore.kml('resources/WorkingTracks/kml/2015-09-12_13_53_50.kml')
                //omnivore.kml('resources/WorkingTracks/kml/2015-09-12_19_57_45.kml')
                //omnivore.kml('resources/WorkingTracks/kml/2015-09-14_09_48_18.kml')
                //omnivore.kml('resources/WorkingTracks/kml/2015-09-09_15_46_52.kml')

                // put 10 route files in a list for permutation
                List<String> files = new ArrayList<String>();
                {
                        final String PATH = "data/WorkingTracks/kml/";
                        String filenames = "track2.kml track4.kml track5.kml 2015-09-10_15_16_32.kml 2015-09-10_17_59_14.kml 2015-09-11_09_18_33.kml 2015-09-12_13_53_50.kml 2015-09-12_19_57_45.kml 2015-09-14_09_48_18.kml 2015-09-09_15_46_52.kml";
                        String[] splits = filenames.split(" ");
                        for (String filename: splits) {
                                BufferedReader br = new BufferedReader(new FileReader(PATH + filename));
                                StringBuilder fileString = new StringBuilder();
                                String line;
                                while ((line = br.readLine()) != null) {
                                        fileString.append(line);
                                }
                                br.close();
                                files.add(fileString.toString());
                        }
                }

                Configuration config = HBaseConfiguration.create();
                config.addResource(new Path("/usr/hdp/current/hbase-master/conf", "hbase-site.xml"));
                config.addResource(new Path("/usr/hdp/current/hbase-master/conf", "core-site.xml"));
                Connection connection = ConnectionFactory.createConnection(config);

                int count = 1;
                Table table = connection.getTable(TableName.valueOf("trip_ns:trip_table"));
                BufferedReader br = new BufferedReader(new FileReader("data/cars.csv"));
                String line;
                while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        System.out.println("doing " + count);
                        count ++;

                        String[] splits = line.split(",");
                        String rowkey = splits[0] + ":" + splits[5]; // medallion:pickup_datetime
                        Put p = new Put(Bytes.toBytes(rowkey));
                        
                        // this random number will permutation through the list of files
                        int random = Double.valueOf(Math.random() * files.size()).intValue();
                        p.addColumn(Bytes.toBytes("trip"), Bytes.toBytes("detail"), Bytes.toBytes(files.get(random)));

                        table.put(p);
                }
                br.close();
                table.close();

                connection.close();
                System.out.println("done.");
        }
}
