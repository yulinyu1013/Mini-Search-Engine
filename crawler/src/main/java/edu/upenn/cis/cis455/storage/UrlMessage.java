package edu.upenn.cis.cis455.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.crawler.UrlQueueSpout;

public class UrlMessage {

	private String url;
	private String parentDocId;
	static Logger logger = LogManager.getLogger(UrlMessage.class);
	public String encodeToStr() {
		return this.url + " " + this.parentDocId;
	}
	
	public UrlMessage(String message) throws RuntimeException {
		this.decodeFromStr(message);
	}
	
	public UrlMessage(String url, String parentDocId) {
		this.url = url;
		this.parentDocId = parentDocId;
	}
	
	public void decodeFromStr(String message) throws RuntimeException {
		
		String[] res = message.split(" ");
		if (res.length!=2){
			logger.debug("decode message: " + message);
			throw new RuntimeException("Message not in right format <url> <next crawl time> <parent doc Id>");
		}
		else {
			this.url = res[0];
			this.parentDocId = res[1];
		}
	}
	public String getUrl() {
		return this.url;
	}
	public void setDocId(String url) {
		this.url = url;
	}
	
	public String getParentDocId() {
		return this.parentDocId;
	}
	
	public void setParentUrl(String parentDocId) {
		this.parentDocId = parentDocId;
	}

	
}
