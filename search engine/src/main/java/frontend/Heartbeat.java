package frontend;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class Heartbeat extends Thread {
    String LB_ADDR;
    int PORT;

    public Heartbeat(String LB_ADDRESS, int MY_LISTEN_PORT) {
        LB_ADDR = LB_ADDRESS;
        PORT = MY_LISTEN_PORT;
    }
    
    public static HttpURLConnection sendJob_http(String urls, String reqType) {
    	  try {
    	      URL url = new URL(urls);
    	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    	      conn.setDoOutput(true);
    	      conn.setRequestMethod(reqType);
    	      return conn;
    	  } catch (IOException e) {
    	      return null;
    	  }
    	}
    
    public void run() {
        while (true) {
            try {
                Thread.sleep(3000);
                String url = LB_ADDR + "/workerPort?port=" + PORT;
                URL urls = new URL(url);
                HttpURLConnection con = (HttpURLConnection) urls.openConnection();
      	      	con.setDoOutput(true);
      	      	con.setRequestMethod("GET");
                if (con == null || con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                	Normalizer.error_message("a" + con.getResponseCode() + url);
                    Normalizer.error_message("heartbeat failed");
                    continue;
                }
                Normalizer.error_message("heartbeat success" + con.getResponseCode() + url);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
    }
}
