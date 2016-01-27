package taxi.leaflet.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

@WebServlet({ "/vin-data", "/trip-data", "/logback", "/set" })
public class DemoMapServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        //private String hbaseServer = "192.168.1.114";
        private String hbaseServer = "sandbox.hortonworks.com";
        private String hbasePort = "12345";
        private String tripInfoTable = "alex_sr.sre_trip_info_hbase";
        private String tripMinuteTable = "alex_sr.sre_trip_minute_aggregates_hbase";

        public DemoMapServlet() {
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                String result = "";
                String path = request.getServletPath();
                if (path.equals("/vin-data")) {
                        String vin = request.getParameter("vin");
                        System.out.println("vin=" + vin);
                        result = getVinData(vin);
                        //System.out.println(result);
                } else if (path.equals("/trip-data")) {
                        String vin = request.getParameter("vin");
                        String pickupTime = request.getParameter("pickupTime");
                        System.out.println("vin=" + vin + ",pickupTime=" + pickupTime);
                        result = getVinTripData(vin, pickupTime);
                } else if (path.equals("/logback")) {
                        String value = request.getParameter("value");
                        System.out.println("[logback] " + value);
                } else if (path.equals("/set")) {
                        Map<String, String[]> parameterMap = request.getParameterMap();
                        setParameter(parameterMap);
                } else {
                }
                response.getWriter().print(result);
        }

        private void setParameter(Map<String, String[]> parameterMap) {
                for (String key: parameterMap.keySet()) {
                        String value = parameterMap.get(key)[0];
                        if (key.equalsIgnoreCase("hbase-server")) {
                                hbaseServer = value;
                                System.out.println("hbaseServer=" + hbaseServer);
                        } else if (key.equalsIgnoreCase("hbase-port")) {
                                hbasePort = value;
                                System.out.println("hbasePort=" + hbasePort);
                        } else if (key.equalsIgnoreCase("trip-info-table")) {
                                tripInfoTable = value;
                                System.out.println("tripInfoTable=" + tripInfoTable);
                        } else if (key.equalsIgnoreCase("trip-minute-table")) {
                                tripMinuteTable = value;
                                System.out.println("tripMinuteTable=" + tripMinuteTable);
                        }
                }
        }

        private String getVinData(String vin) throws IOException {
                String result = null;
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet("http://" + hbaseServer + ":" + hbasePort + "/trip_ns:trip_table/" + vin + ":/trip:total");
                //HttpGet httpGet = new HttpGet("http://192.168.1.114:12345/alex_sr.sre_trip_info_hbase/" + vin + "*");
                // http://192.168.1.114:12345/alex_sr.sre_trip_info_hbase/0507011~2121005091404781691
                httpGet.setHeader("accept", "application/json");
                CloseableHttpResponse response = httpclient.execute(httpGet);
                try {
                        HttpEntity entity = response.getEntity();
                        result = EntityUtils.toString(entity);
                        EntityUtils.consume(entity);
                } finally {
                        response.close();
                }

                return result;
        }

        private String getVinTripData(String vin, String pickupTime) throws IOException {
                String result = null;
                CloseableHttpClient httpclient = HttpClients.createDefault();
                String rowkey = vin + ":" + pickupTime;
                String url = "http://" + hbaseServer + ":" + hbasePort + "/trip_ns:trip_table/" + rowkey + "/trip:detail";
                url = url.replaceAll(" ", "%20");
                HttpGet httpGet = new HttpGet(url);
                //HttpGet httpGet = new HttpGet("http://" + hbaseServer + ":" + hbasePort + "/" + tripMinuteTable + "/" + vin + trip + "*");
                //HttpGet httpGet = new HttpGet("http://192.168.1.114:12345/alex_sr.sre_trip_minute_aggregates_hbase/" + vin + trip + "*");
                // http://192.168.1.114:12345/alex_sr.sre_trip_info_hbase/0507011~2121005091404781691
                httpGet.setHeader("accept", "application/json");
                CloseableHttpResponse response = httpclient.execute(httpGet);
                try {
                        HttpEntity entity = response.getEntity();
                        result = EntityUtils.toString(entity);
                        EntityUtils.consume(entity);
                } finally {
                        response.close();
                }

                //System.out.println(result);
                return result;
        }

}
