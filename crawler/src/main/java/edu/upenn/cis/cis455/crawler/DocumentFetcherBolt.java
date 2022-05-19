package edu.upenn.cis.cis455.crawler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import edu.upenn.cis.cis455.storage.CrawlerStorage;
import edu.upenn.cis.cis455.storage.DomainSeenWithRobotTxtInfoEntry;
import edu.upenn.cis.cis455.storage.UrlMessage;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;

public class DocumentFetcherBolt implements IRichBolt{
	//1.download and send to praser bolt
	private OutputCollector collector;
	private static Fields schema = new Fields("url", "content", "last_modified_since", 
			"content_type", "crawled_time", "parent_doc_id");
	String executorId = UUID.randomUUID().toString();
	static Logger logger = LogManager.getLogger(DocumentFetcherBolt.class);
	private CrawlerConfig config;
	private CrawlerStorage storage;
	private TopologyContext context;
	@Override
	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(schema);
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void execute(Tuple input) {
		logger.debug("entering document fetcher bolt");
		try {
			UrlMessage urlMessage = (UrlMessage) input.getObjectByField("url_message");
			URL url = new URL(urlMessage.getUrl());
			DynamoDBMapper mapper = this.storage.getDynamoDBMapper();
			DomainSeenWithRobotTxtInfoEntry domainEntry = mapper.load(DomainSeenWithRobotTxtInfoEntry.class,
					url.getHost());
			if (domainEntry != null) {
				long badRequestTime = domainEntry.getBadRequestTime();
				String parentDocId = urlMessage.getParentDocId();
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestProperty("User-Agent", this.config.agent);
				connection.setConnectTimeout(this.config.connectTimeOutMili);
				connection.setReadTimeout(this.config.getTimeOutMili);
				connection.setRequestMethod("GET");
				long currentTime = System.currentTimeMillis();
				connection.connect();
				if (connection.getResponseCode() == 200 || connection.getResponseCode() == 304) {
					byte[] content = connection.getInputStream().readAllBytes();
					String contentType = connection.getContentType();
					long lastModifiedSince = connection.getLastModified();
					this.collector.emit(
							new Values<Object>(url, content, lastModifiedSince, contentType, currentTime, parentDocId));
					context.incCrawledCounter();
					if (domainEntry != null) {
						domainEntry.setDomainLastCrawledTime(currentTime);
						mapper.save(domainEntry);
					}
				} else {

					domainEntry.setBadRequestTime(badRequestTime + 1);
					domainEntry.setDomainLastCrawledTime(currentTime);
					mapper.save(domainEntry);
				}
			} else {
				logger.warn("url received but domain not exist this should happen in rare case");
				String parentDocId = urlMessage.getParentDocId();
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestProperty("User-Agent", this.config.agent);
				connection.setConnectTimeout(this.config.connectTimeOutMili);
				connection.setReadTimeout(this.config.getTimeOutMili);
				connection.setRequestMethod("GET");
				long currentTime = System.currentTimeMillis();
				connection.connect();
				if (connection.getResponseCode() == 200 || connection.getResponseCode() == 304) {
					byte[] content = connection.getInputStream().readAllBytes();
					String contentType = connection.getContentType();
					long lastModifiedSince = connection.getLastModified();
					
					this.collector.emit(
							new Values<Object>(url, content, lastModifiedSince, contentType, currentTime, parentDocId));
					context.incCrawledCounter();
				}
			}
		} catch (IOException e) {
			logger.error("get content failed");
			e.printStackTrace();
			UrlMessage urlMessage = (UrlMessage) input.getObjectByField("url_message");
			try {
				URL url = new URL(urlMessage.getUrl());
				DynamoDBMapper mapper = this.storage.getDynamoDBMapper();
				DomainSeenWithRobotTxtInfoEntry domainEntry = mapper.load(DomainSeenWithRobotTxtInfoEntry.class,
						url.getHost());
				long badRequestTime = domainEntry.getBadRequestTime();
				domainEntry.setBadRequestTime(badRequestTime + 1);
				mapper.save(domainEntry);
			} catch (IOException me) {
				logger.error("this should not happen url is not in right form");
			}
		}
		logger.debug("leaving document fetcher bolt");
	}

	@Override
	public void prepare(CrawlerConfig config, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.storage = context.getStorage();
		this.config = config;
		this.context = context;
	}

	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);
	}

	@Override
	public Fields getSchema() {
		// TODO Auto-generated method stub
		return schema;
	}

}
