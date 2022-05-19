package edu.upenn.cis.cis455.crawler.server;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis.stormlite.TopologyContext;

public class WorkerReporter implements Runnable{
	static final Logger logger = LogManager.getLogger(WorkerReporter.class);
	static final int masterConnectionTimeoutMili = 3000;
	private TopologyContext context;
	private int listeningPort;
	private long startTime;
	private static final ObjectMapper om = new ObjectMapper();
	private String masterIp;
	private int masterListeningPort;
	private int reportTimePeriod;
	
	public WorkerReporter(TopologyContext context, int listeningPort, String masterIp, int masterListeningPort
			,int reportTimePeriod) {
		this.context = context;
		this.listeningPort = listeningPort;
		this.masterIp = masterIp;
		this.masterListeningPort = masterListeningPort;
		this.reportTimePeriod = reportTimePeriod;
	}
	@Override
	public void run() {
		this.startTime = System.currentTimeMillis();
		try {
			while (true) {
				int listeningPort = this.listeningPort;
				long startTime = this.startTime;
				long crawledCount = this.context.getCrawledCount();
				long urlCheckedCount = this.context.getUrlCheckedCount();
				long urlQueuedCount = this.context.getUrlQueuedCount();
				long parsedCount = this.context.getParsedCount();
				long savedDocCount = this.context.getSavedDocCount();
				long inQueueCount = this.context.getInQueueCount();
				long requeueCount = this.context.getRequeueCounter();
				ReportEntry sendingEntry = 
						new ReportEntry(listeningPort, startTime, crawledCount, urlCheckedCount,
								urlQueuedCount, parsedCount, savedDocCount, inQueueCount, requeueCount);
				try {
					URL masterUrl = new URL("http://" + this.masterIp + ":" + this.masterListeningPort + "/report");
					HttpURLConnection connection = (HttpURLConnection) masterUrl.openConnection();
					connection.setRequestMethod("POST");
					connection.setConnectTimeout(masterConnectionTimeoutMili);
					connection.setDoOutput(true);
					connection.connect();
					OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
					out.write(om.writeValueAsString(sendingEntry));
					out.flush();
					out.close();
					if (connection.getResponseCode()==200) {
						logger.warn("Reporter successfully send message to " + masterUrl.getHost());
					}
					else {
						logger.warn("Reporter failed to send message");
					}
				}
				catch (Exception e) {
					logger.warn("Reporter failed to send message");
				}
				
				Thread.sleep(this.reportTimePeriod);
			}
		}
		catch(Exception e) {
			logger.warn("reporter get interrupted and shutdown");
		}
	}

	
}
