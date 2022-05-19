package edu.upenn.cis.cis455.crawler;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.MessageProducer;

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

public class UrlCheckTimeBolt implements IRichBolt{
	//Steps should be completed at this task
	//check whether the nextCrawlerTime is before current time if not re queue it and end
	static Logger logger = LogManager.getLogger(UrlCheckTimeBolt.class);
	String executorId = UUID.randomUUID().toString();
	private OutputCollector collector;
	private static Fields schema = new Fields("url_message");
	private CrawlerStorage storage;
	private TopologyContext context;
	private MessageProducer producer;
	@Override
	public String getExecutorId() {
		return executorId;
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
		// logger.info(input);
		UrlMessage urlMessage = (UrlMessage) input.getObjectByField("urlMessage");
		DynamoDBMapper mapper = this.storage.getDynamoDBMapper();
		try {
			URL url = new URL(urlMessage.getUrl());
			DomainSeenWithRobotTxtInfoEntry domainEntry = mapper.load(DomainSeenWithRobotTxtInfoEntry.class,
					url.getHost());
			if (domainEntry != null) {
				if (System.currentTimeMillis() - domainEntry.getDomainLastCrawledTime() >= domainEntry
						.getCrawledDelayInMili()) {
					this.collector.emit(new Values<Object>(urlMessage));
				} else {
					logger.info("message enter requeue phase");
					try {
						storage.sendUrlCrawlRequest(this.producer, urlMessage);
						this.context.incRequeueCounter();
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						logger.error("message failed to requeue" + e.getMessage());
					}
				}
			} else {
				logger.warn("url come in with no domain record. This should rarely happen");
				this.collector.emit(new Values<Object>(urlMessage));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void prepare(CrawlerConfig stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.storage = context.getStorage();
		this.context = context;
		try {
			this.producer = this.storage.getNewQueueProducer();
		}
		catch (Exception e) {
			logger.error("fatal error in prepare bolt :" + e.getMessage());
		}
	}

	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);
		
	}

	@Override
	public Fields getSchema() {
		return schema;
	}
	
	

}
