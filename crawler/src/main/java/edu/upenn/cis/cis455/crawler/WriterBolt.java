package edu.upenn.cis.cis455.crawler;

import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import edu.upenn.cis.cis455.storage.ContentSeenEntry;
import edu.upenn.cis.cis455.storage.CrawlerStorage;
import edu.upenn.cis.cis455.storage.DocumentEdgesEntry;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

public class WriterBolt implements IRichBolt {
	static final Logger logger = LogManager.getLogger(WriterBolt.class);
	String executorId = UUID.randomUUID().toString();
	Fields schema = new Fields();
	CrawlerStorage storage;
	private OutputCollector collector;
	private HashMap<String, String> cachedContent;
	private String currentS3Id;
	private TopologyContext context;
	private CrawlerConfig config;
	@Override
	public String getExecutorId() {
		return this.executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(getSchema());
	}

	@Override
	public void cleanup() {
		this.storage.writeHashMapContentToS3(cachedContent, this.currentS3Id);
	}

	@Override
	public void execute(Tuple input) {
		logger.debug("entering writer bolt");
		//content seen entry remaining value to be set : 
		//logger.info("writer receive tuple for " + input.getStringByField("for_whom") + " parent: " + input.getStringByField("parent_doc_id"));
		if (input.getStringByField("for_whom").equals("writer")) {
			ContentSeenEntry entry = (ContentSeenEntry) input.getObjectByField("content_seen_entry");
			String parentDocId = input.getStringByField("parent_doc_id");
			String docId = entry.getDocId();
			if (cachedContent.size() == 0) {
				this.currentS3Id = docId;
			}
			String content = input.getStringByField("content_str");
			this.cachedContent.put(docId, content);
			DynamoDBMapper mapper = this.storage.getDynamoDBMapper();
			mapper.save(new DocumentEdgesEntry(parentDocId, docId));
			entry.setS3Id(currentS3Id);
			mapper.save(entry);
			logger.info("a file has been cached current size "+ cachedContent.size());
			if (this.cachedContent.size()>=config.getWriterCachedSize()) {
				logger.info("writer start writing to s3");
				this.storage.writeHashMapContentToS3(cachedContent, this.currentS3Id+".txt");
				this.context.incSavedDocCounter(this.cachedContent.size());
				this.cachedContent.clear();
				logger.info("writer end writing to s3");
			}
		}
		logger.debug("leaving writer bolt");

	}

	@Override
	public void prepare(CrawlerConfig conf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.storage = context.getStorage();
		this.cachedContent = new HashMap<String, String>();
		this.context = context;
		this.config = conf;
	}

	@Override
	public void setRouter(IStreamRouter router) {
		// TODO Auto-generated method stub
		this.collector.setRouter(router);
	}

	@Override
	public Fields getSchema() {
		// TODO Auto-generated method stub
		return this.schema;
	}

}
