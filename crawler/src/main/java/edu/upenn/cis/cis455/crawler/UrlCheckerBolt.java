package edu.upenn.cis.cis455.crawler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.MessageProducer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import edu.upenn.cis.cis455.crawler.utils.RobotTxtInfo;
import edu.upenn.cis.cis455.storage.CrawlerStorage;
import edu.upenn.cis.cis455.storage.DomainSeenWithRobotTxtInfoEntry;
import edu.upenn.cis.cis455.storage.URLSeenEntry;
import edu.upenn.cis.cis455.storage.UrlMessage;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

public class UrlCheckerBolt implements IRichBolt {
	//this is the destination of any link
	//1. check if we have seen this url if not add it to the table
	//2. check header content type
	//3. check if robot txt for the url exist
	//4. if not download and save it on db
	//5. send the link to queue with appropriate delay
	static Logger logger = LogManager.getLogger(UrlCheckerBolt.class);
	String executorId = UUID.randomUUID().toString();
	Fields schema = new Fields();
	CrawlerStorage storage;
	private CrawlerConfig config;
	private OutputCollector collector;
	private TopologyContext context;
	private MessageProducer producer;
	@Override
	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(getSchema());
	}

	@Override
	public void cleanup() {
		
	}

	@Override
	public void execute(Tuple input) {
		logger.debug("entering parse bolt");
		if (input.getStringByField("for_whom").equals("url_checker")) {
			try {
				URL url = new URL(input.getStringByField("content_seen_entry"));
				String parentDocId = input.getStringByField("parent_doc_id");
				DynamoDBMapper mapper = this.storage.getDynamoDBMapper();
				//url has seen?
				URLSeenEntry seenEntry = mapper.load(URLSeenEntry.class, url.toString());
				if (seenEntry == null) {
					//url has not seen. save url to url seen table
					mapper.save(new URLSeenEntry(url.toString(), System.currentTimeMillis()));
					//make sure domain entry exist
					this.ensureDomainRecordExist(url);
					if (allowedContentTypeAndReachable(url)) {
						DomainSeenWithRobotTxtInfoEntry domainEntry = mapper.load(DomainSeenWithRobotTxtInfoEntry.class,
								url.getHost());
						long nextAvailableTime = domainEntry.nextAvailableTime();
						domainEntry.setReserveredQueueTime(nextAvailableTime);
						this.storage.sendUrlCrawlRequest(this.producer, new UrlMessage(url.toString(), parentDocId));
						mapper.save(domainEntry);
						this.context.incUrlQueuedCounter();
					}
				}
			} catch (IOException | JMSException e) {
				e.printStackTrace();
				logger.warn("Error happen when handling robot txt and queue new link " + e.getMessage()
				+ "at url " + input.getStringByField("content_seen_entry"));
			}
			this.context.incUrlCheckedCounter();
		}
		
		logger.debug("leaving url checker bolt");
	}

	@Override
	public void prepare(CrawlerConfig config, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.storage = context.getStorage();
		this.context = context;
		this.config = config;
		try {
			this.producer = this.storage.getNewQueueProducer();
		}
		catch(Exception e) {
			logger.error("fatal error met in preparing bolt: " + e.getMessage());
		}
	}

	@Override
	public void setRouter(IStreamRouter router) {
		// TODO Auto-generated method stub
		this.collector.setRouter(router);
	}
	private boolean allowedContentTypeAndReachable(URL url) {
		String contentType;
		DynamoDBMapper mapper = this.storage.getDynamoDBMapper();
		DomainSeenWithRobotTxtInfoEntry domainEntry = mapper.load(DomainSeenWithRobotTxtInfoEntry.class,
				url.getHost());
		long accessTime = domainEntry.getTotalAccessTime();
		long badRequestTime = domainEntry.getBadRequestTime();
		if (accessTime>this.config.accessDecideThreshold && badRequestTime*1.0/accessTime > this.config.badAccessLimit) {
			logger.info("blacklist functioning on: " + url.getHost());
			return false;
		}
		if (url.getProtocol().equals("https") || url.getProtocol().equals("http")){
			try {
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestProperty("User-Agent", this.config.agent);
				connection.setRequestMethod("HEAD");
				connection.setConnectTimeout(this.config.connectTimeOutMili);
		        connection.setReadTimeout(this.config.headTimeOutMili);
				connection.connect();
				if (connection.getResponseCode() == 200 || connection.getResponseCode() == 304)
				{
					contentType = connection.getContentType();
					domainEntry.setTotalAccessTime(accessTime+1);
					mapper.save(domainEntry);
					return allowedContentType(contentType);
				}
				else
				{
					//TODO whether keep this
					domainEntry.setBadRequestTime(badRequestTime+1);
					domainEntry.setTotalAccessTime(accessTime+1);
					mapper.save(domainEntry);
					return false;
				}
				
			}
			catch (IOException e) {
				domainEntry.setBadRequestTime(badRequestTime+1);
				domainEntry.setTotalAccessTime(accessTime+1);
				mapper.save(domainEntry);
				return false;
			}
		}
		else
			return false;
	}
	
	private boolean isReachable(URL url) {
		if (url.getProtocol().equals("https") || url.getProtocol().equals("http")){
			try {
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestProperty("User-Agent", this.config.agent);
				connection.setRequestMethod("HEAD");
				connection.setConnectTimeout(this.config.connectTimeOutMili);
		        connection.setReadTimeout(this.config.headTimeOutMili);
				connection.connect();
				return (connection.getResponseCode() == 200 || connection.getResponseCode() == 304);
			}
			catch(IOException e) {
				return false;
			}
		}
		else
			return false;
	}
	
	private byte[] getContent(URL url) {
		if (url.getProtocol().equals("https") || url.getProtocol().equals("http")){
			try {
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				connection.setRequestProperty("User-Agent", this.config.agent);
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(this.config.connectTimeOutMili);
		        connection.setReadTimeout(this.config.headTimeOutMili);
				connection.connect();
				if (connection.getResponseCode() == 200 || connection.getResponseCode() == 304) {
					return connection.getInputStream().readAllBytes();
				}
				else
					return null;
			}
			catch(IOException e) {
				return null;
			}
		}
		else
			return null;
	}
	
	private void ensureDomainRecordExist(URL url) throws IOException {
		// given an URL if its domain record is already in the dataset do nothing
		// otherwise update its domain info
		DynamoDBMapper mapper = this.storage.getDynamoDBMapper();
		DomainSeenWithRobotTxtInfoEntry domainRobotEntry = mapper.load(DomainSeenWithRobotTxtInfoEntry.class,
				url.getHost());
		if (domainRobotEntry == null) {
			URL robotUrl = new URL(robotTxtUrl(url));
			byte[] robotContent;
			long currentTime = System.currentTimeMillis();
			DomainSeenWithRobotTxtInfoEntry newDomainRobotEntry;
			if (isReachable(robotUrl)) {
				robotContent = getContent(robotUrl);
				if (robotContent != null) {
					try {
						logger.info("start parsing robot txt from: " + url.getHost());
						RobotTxtInfo robotTxtInfo = new RobotTxtInfo(this.config.agent,
								new String(robotContent, "utf-8"));
						logger.info("end parsing robot txt from: " + url.getHost());
						newDomainRobotEntry = new DomainSeenWithRobotTxtInfoEntry(this.config.agent, robotUrl.getHost(),
								currentTime, robotTxtInfo.getDisallowedSite(), robotTxtInfo.getCrawledDelayInSeconds(),
								currentTime, currentTime);
						}
					catch (Exception e) {
						logger.warn("prasing robot url failed on domain: " + robotUrl.getHost());
						newDomainRobotEntry = new DomainSeenWithRobotTxtInfoEntry(this.config.agent, robotUrl.getHost(),
								currentTime, null, config.getDefaultDomainCrawlDelay(), currentTime,
								System.currentTimeMillis());
					}
				} else {
					newDomainRobotEntry = new DomainSeenWithRobotTxtInfoEntry(this.config.agent, robotUrl.getHost(),
							currentTime, null, config.getDefaultDomainCrawlDelay(), currentTime,
							System.currentTimeMillis());
				}
			} else {
				newDomainRobotEntry = new DomainSeenWithRobotTxtInfoEntry(this.config.agent, robotUrl.getHost(),
						currentTime, null, config.getDefaultDomainCrawlDelay(), currentTime,
						System.currentTimeMillis());
			}
			mapper.save(newDomainRobotEntry);
		} 
	}

	@Override
	public Fields getSchema() {
		return this.schema;
	}
	public static String robotTxtUrl(URL url) {
		return url.getProtocol() + "://" + url.getHost() + url.getPath();
	}
	public boolean allowedContentType(String contentType) {
		for (String allowedContent: this.config.acceptableContentType) {
			if (contentType.startsWith(allowedContent))
				return true;
		}
		return false;
	}

}
