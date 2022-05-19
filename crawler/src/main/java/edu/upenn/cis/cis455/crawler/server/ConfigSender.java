package edu.upenn.cis.cis455.crawler.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis.cis455.crawler.CrawlerConfig;
import edu.upenn.cis.cis455.crawler.UrlCheckerBolt;

public class ConfigSender {

	static final ObjectMapper om = new ObjectMapper();
	static Logger logger = LogManager.getLogger(ConfigSender.class);

	public static void main(String[] args) throws IOException {
    	org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.INFO);
		CrawlerConfig config = new CrawlerConfig();
		String[] workerIps = new String[] {
						"18.215.187.103", 
						"44.198.185.120",
						"3.231.157.146",
						"3.216.125.48",
						"44.195.46.119",
						"44.201.30.102",
						"44.195.32.91",
						"3.236.183.149",
						"44.200.250.27",
						"44.202.194.78",
						"3.236.190.216",
						"44.202.233.9",
						"3.235.84.117",
						"3.220.167.75",
						"3.235.151.94",
						"3.238.70.89",
						"localhost",
						"44.192.122.78",
						"44.205.22.132",
						"35.170.53.210",
						"3.237.180.246",
						"18.207.183.35",
						"18.207.97.66",
						"3.92.42.71",
						"44.204.255.74"
						};
		String[] workerIps2 = new String[] {
				"44.192.107.112",
				"3.215.174.187",
				"54.237.132.255",
				"3.238.224.6",
				"44.203.234.54",
				"44.197.119.227",
				"3.235.90.99",
				"44.204.203.192"
		};
		int workerPort = 45556;
		String masterIp = "18.215.187.103";
		int masterPort = 45555;
		config.setMasterIp(masterIp);
		config.setThreadNum(500);
		config.setCheckTimeBoltNum(10);
		config.setQueueSpoutNum(20);
		config.setDocFetcherBoltNum(100);
		config.setParseBoltNum(80);
		config.setUrlCheckerBoltNum(200);
		config.setWriterBoltNum(1);
		config.setConnectTimeOutMili(2000);
		config.setGetTimeOutMili(3000);
		config.setHeadTimeOutMili(2000);
		config.setAccessDecideThreshold(99999);
		config.setBadAccessLimit(1);
		config.setMasterPort(masterPort);
		config.setByPassLinkExtract(true);
		config.setAwsBucketName("crawler-content-bucket-5");
		//sendStop(workerIps, workerPort);
		sendStop(new String[] {"localhost"}, workerPort);
		//sendStart(workerIps2, workerPort,config);
//		sendStart(new String[] {"18.215.187.103", 
//				"44.198.185.120",
//				"3.231.157.146",
//				"3.216.125.48",
//				"44.195.46.119",
//				"44.201.30.102",
//				"44.195.32.91",
//				"3.236.183.149",
//				"44.200.250.27",
//				"44.202.194.78",
//				"3.236.190.216",
//				"44.202.233.9",
//				"3.235.84.117",
//				"3.220.167.75",
//				"3.235.151.94",
//				"3.238.70.89",
//				"localhost",}, workerPort, config);
	}
	
	public static void sendStart(String[] workerIps, int workerPort,
			CrawlerConfig config){
		for (String workerIp : workerIps) {
			try {
				logger.info("connect to worker: " + workerIp);
				URL workerUrl = new URL("http://" + workerIp + ":" + workerPort + "/start");
				HttpURLConnection connection = (HttpURLConnection) workerUrl.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
				out.write(om.writeValueAsString(config));
				out.flush();
				out.close();
				if (connection.getResponseCode() == 200) {
					logger.info("connection to worker " + workerIp + " success");
				} else {
					logger.warn("connection to worker " + workerIp + " failed");
				}
			} catch (Exception e) {
				logger.warn("connection to worker " + workerIp + " failed");
			}
		}
		logger.info("end sending");
	}

	public static void sendStop(String[] workerIps, int workerPort){
		for (String workerIp : workerIps) {
			try {
				logger.info("connect to worker: " + workerIp);
				URL workerUrl = new URL("http://" + workerIp + ":" + workerPort + "/stop");
				HttpURLConnection connection = (HttpURLConnection) workerUrl.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(5000);
				connection.connect();
				if (connection.getResponseCode() == 200) {
					logger.info("connection to worker " + workerIp + " success");
				} else {
					logger.warn("connection to worker " + workerIp + " failed");
				}
			} catch (Exception e) {
				logger.warn("connection to worker " + workerIp + " failed");
			}
		}
		logger.info("end sending");
	}

}
