package edu.upenn.cis.cis455.crawler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.storage.CrawlerStorage;
import edu.upenn.cis.cis455.storage.UrlMessage;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;

public class UrlQueueSpout implements IRichSpout{

	static Logger logger = LogManager.getLogger(UrlQueueSpout.class);
	String executorId = UUID.randomUUID().toString();
	SpoutOutputCollector collector;
	MessageConsumer consumer;
	CrawlerStorage storage;
	TopologyContext context;
	static final Fields schema = new Fields("urlMessage");
	@Override
	public String getExecutorId() {
		return executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(schema);
		
	}

	@Override
	public void open(CrawlerConfig config, TopologyContext context, SpoutOutputCollector collector) {
		this.collector = collector;
		try {
			this.storage = context.getStorage();
			this.consumer = context.getStorage().getNewQueueConsumer();
			this.context = context;
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			logger.error("fatal error met when creating message consumer" + e.getMessage());;
		}
	}

	@Override
	public void close() {
		
	}

	@Override
	public void nextTuple() {
		try {
			UrlMessage urlMessage = this.storage.receiveUrlCrawlRequest(consumer, 100);
			if (urlMessage!=null)
			{
				logger.info("new message entering queue: " + urlMessage.encodeToStr());
				this.collector.emit(new Values<Object>(urlMessage));
				this.context.incInQueueCounter();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);
		
	}
	

}
