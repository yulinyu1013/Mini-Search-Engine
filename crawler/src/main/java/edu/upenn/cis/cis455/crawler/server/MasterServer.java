package edu.upenn.cis.cis455.crawler.server;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis.cis455.crawler.CrawlerConfig;
import spark.Spark;

public class MasterServer {
	static final long workerAliveTimeoutMili = 60000;
	static final int workerConnectionTimeoutMili = 5000;
	static final Logger logger = LogManager.getLogger(MasterServer.class);
	HashMap<String, ReportEntry> lastReport = new HashMap<String, ReportEntry>();
	HashMap<String, Integer> savedShutDownWorkers = new  HashMap<String, Integer>();
	HashMap<String, Long> lastReceivedTime = new HashMap<String, Long>();
	static final ObjectMapper om = new ObjectMapper();
	private AtomicBoolean isRunning = new AtomicBoolean(false);
	CrawlerConfig config;
	int port;
	public MasterServer(int port) {
		this.port = port;
		Spark.port(port);
		Spark.post("/resume_all", (req, res)->{
//			if (isRunning.get()) {
//				return "Worker is running. You must stop it first";
//			}
			ArrayList<String> failedWorkers = new ArrayList<String>();
			this.config = om.readValue(req.body(), CrawlerConfig.class);
			for (Map.Entry<String, Integer> entry : this.savedShutDownWorkers.entrySet()) {
				try {
					URL workerUrl = new URL("http://" + entry.getKey()+":"+entry.getValue()+"/start");
					HttpURLConnection connection = (HttpURLConnection) workerUrl.openConnection();
					connection.setRequestMethod("POST");
					connection.setConnectTimeout(workerConnectionTimeoutMili);
					connection.setDoOutput(true);
					connection.connect();
					OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
					out.write(om.writeValueAsString(this.config));
					out.flush();
					out.close();
					if (connection.getResponseCode()==200) {
						logger.info("connection to worker: " + entry.getKey() + ":" + 
								entry.getValue() + " success");
					}
					else {
						logger.warn("connection to worker: " + entry.getKey() + ":" + 
								entry.getValue()+ "failed");
						failedWorkers.add(entry.getKey() + ":" + entry.getValue());
					}
				}
				catch(Exception e) {
					logger.warn("connection to worker: " + entry.getKey() + ":" + 
				entry.getValue()+ "failed\n" + e.getMessage());
					failedWorkers.add(entry.getKey() + ":" + entry.getValue());
				}
			}
//			this.isRunning.set(true);
			if (failedWorkers.size() == 0)
				return "all success";
			else
			{
				String res_str = "";
				for (String worker:failedWorkers) {
					res_str += "failed on worker " + worker + "\n";
				}
				return res_str;
			}
		});
		
		Spark.get("/stop_all", (req, res) -> {
//			if (!this.isRunning.get()) {
//				return "Worker is not running. You must run it first";
//			}
			this.update();
			ArrayList<String> failedWorkers = new ArrayList<String>();
			for (Map.Entry<String, ReportEntry> entry: this.lastReport.entrySet()) {
				try {
					URL workerUrl = new URL("http://" + entry.getKey()+":"+entry.getValue().getListeningPort()+"/stop");
					HttpURLConnection connection = (HttpURLConnection) workerUrl.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(workerConnectionTimeoutMili);
					connection.connect();
					if (connection.getResponseCode() == 200) {
					this.savedShutDownWorkers.put(entry.getKey(), entry.getValue().getListeningPort());
					}
					else {
						logger.warn("connection to worker: " + entry.getKey() + ":" + 
								entry.getValue()+ "failed");
						failedWorkers.add(entry.getKey() + ":" + entry.getValue());
					}
				}
				catch(Exception e) {
					logger.warn("connection to worker: " + entry.getKey() + ":" + 
				entry.getValue()+ "failed\n" + e.getMessage());
					failedWorkers.add(entry.getKey() + ":" + entry.getValue());
					e.printStackTrace();
				}
			}
//			this.isRunning.set(false);
			if (failedWorkers.size() == 0)
				return "all success";
			else
			{
				String res_str = "";
				for (String worker:failedWorkers) {
					res_str += "failed on worker " + worker + "\n" ;
				}
				return res_str;
			}	
		});
		
		Spark.post("/report", (req, res) ->{
			try {
				logger.info("report received from worker " + req.ip());
				ReportEntry report = om.readValue(req.body(), ReportEntry.class);
				long currentTime = System.currentTimeMillis();
				lastReport.put(req.ip(), report);
				lastReceivedTime.put(req.ip(), currentTime);
				return "success";
			}
			catch (Exception e) {
				logger.error("report received from worker but format is not right " + req.ip() + "\n" +
			e.getMessage());
				return "failed";
			}
		});
		
		Spark.get("/status", (req, res) -> {
			this.update();
			String result = "<!DOCTYPE html>\n"
					+"<html>\n"
					+"<head>\n"
					+"<title>Crawler Master Status Report Page</title>\n"
					+"</head>\n"
					+"<body>\n"
					+"<ul>\n";
			
			long totalSaved = 0;
			long currentTime = System.currentTimeMillis();
			for (Map.Entry<String, ReportEntry> entry : this.lastReport.entrySet()) {
				ReportEntry report = entry.getValue();
				logger.info("time elapse: " + (currentTime - report.getStartTime()));
				result += "<li>ip: " + entry.getKey() 
				+ " url dequeue: " + report.getInQueueCount()
				+ " url add to queue: " + report.getUrlQueuedCount()
				+ " url requeue: " + report.getRequeueCount()
				+ " total url checked: " + report.getUrlCheckedCount() + "</li>\n"
				+ "<li>crawled pages: " + report.getCrawledCount()
				+ " parsed pages: " + report.getParsedCount() 
				+ " pages save to S3: " + report.getSavedDocCount() + "</li>\n"
				+ "averge saved page per minute: "
				+ report.getSavedDocCount() / ((currentTime - report.getStartTime()) / 60000.0)
				+ "<li> last report received on: " + this.lastReceivedTime.get(entry.getKey())
				+ " reporter starting report on: " + report.getStartTime() + "</li>\n";
				totalSaved += report.getSavedDocCount();
			}
			result += "<li>total crawled from all workers: " + totalSaved + "</li>\n";
			result += "</ul>\n"
					+ "</body>\n"
					+ "</html>";
			return result;
		});
		
		Spark.get("/final_shutdown_server", (req, res) -> {
			Spark.stop();
			return "success";
		});
		
		Spark.awaitInitialization();
	}
	private void update() {
		ArrayList<String> removingIp = new ArrayList<String>();
		long currentTime = System.currentTimeMillis();
		for (Map.Entry<String, Long> entry: this.lastReceivedTime.entrySet()) {
			if (currentTime - entry.getValue() > workerAliveTimeoutMili) {
				removingIp.add(entry.getKey());
			}
		}
		for (String ip: removingIp) {
			this.lastReceivedTime.remove(ip);
			this.lastReport.remove(ip);
		}
	}
	
	public static void main(String[] args) {
    	org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.INFO);
    	int port = Integer.valueOf(args[0]);
    	new MasterServer(port);
	}
}
