package edu.upenn.cis.cis455.crawler;

import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import javax.naming.Context;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

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
import edu.upenn.cis.stormlite.tuple.Values;

public class ParseBolt implements IRichBolt{
	//1. check content seen
	//2. check content type
	//3. save doc
	//4.save record
	//5. parse url and send to checkBolt
	static Logger logger = LogManager.getLogger(ParseBolt.class);
	String executorId = UUID.randomUUID().toString();
	Fields schema = new Fields("content_seen_entry", "content_str", "parent_doc_id", "for_whom");
	CrawlerConfig config;
	TopologyContext context;
	private OutputCollector collector;
	private CrawlerStorage storage;
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
		logger.debug("entering parse bolt");
		URL url = (URL)input.getObjectByField("url");
		byte[] content = (byte[]) input.getObjectByField("content");
		String md5Hash = DigestUtils.md5Hex(content);
		DynamoDBMapper mapper = this.storage.getDynamoDBMapper();
		ContentSeenEntry entry = mapper.load(ContentSeenEntry.class, md5Hash);
		try {

			String contentStr = new String(content, "utf-8");
			if (entry == null) {
				Document doc = Jsoup.parse(contentStr, url.toString(), Parser.xmlParser());
				Elements allHref = doc.select("a[href]");
				long crawledTime = (long)input.getObjectByField("crawled_time");
				long lastModifiedSince = (long)input.getObjectByField("last_modified_since");
				String newUrl;
				String title = doc.title();
				doc.select("script,jscript,style").remove();
				String bodyText = doc.text();
				String prelude = bodyText.substring(0, Math.min(this.config.numCharToSave, doc.text().length()));
				String contentType = input.getStringByField("content_type");
				String parentDocId = input.getStringByField("parent_doc_id");
				this.collector.emit(new Values<Object>(new ContentSeenEntry(url.toString(), md5Hash, true, crawledTime, null, title, prelude,
						lastModifiedSince, contentType), doc.text(), parentDocId, "writer"));
				if (!this.config.getByPassLinkExtract()) {
					for (Element e : allHref) {
						newUrl = e.attr("abs:href");
						if (newUrl == null) {
							logger.info("this shouldn't happen");
						}
						this.collector.emit(new Values<Object>(newUrl, null, md5Hash, "url_checker"));
					}
					
					this.context.incParsedCounter();
					
				}
			}
		} catch (Exception e) {
			logger.warn("failure when parsing content " + e.getMessage());
			e.printStackTrace();
		}
		logger.debug("leaving parse bolt");
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
		return schema;
	}

	

}
