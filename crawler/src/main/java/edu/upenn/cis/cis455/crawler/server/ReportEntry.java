package edu.upenn.cis.cis455.crawler.server;


public class ReportEntry {

	private int listeningPort;
	private long startTime;
	private long crawledCount;
	private long requeueCount;
	public long getRequeueCount() {
		return requeueCount;
	}

	public void setRequeueCount(long requeueCount) {
		this.requeueCount = requeueCount;
	}

	public ReportEntry(int listeningPort, long startTime, long crawledCount, long urlCheckedCount,
			long urlQueuedCount, long parsedCount, long savedDocCount, long inQueueCount, long requeueCount) {
		super();
		this.listeningPort = listeningPort;
		this.startTime = startTime;
		this.crawledCount = crawledCount;
		this.urlCheckedCount = urlCheckedCount;
		this.urlQueuedCount = urlQueuedCount;
		this.parsedCount = parsedCount;
		this.savedDocCount = savedDocCount;
		this.inQueueCount = inQueueCount;
		this.requeueCount = requeueCount;
	}
	
	public ReportEntry() {}

	public long getCrawledCount() {
		return crawledCount;
	}

	public void setCrawledCount(long crawledCount) {
		this.crawledCount = crawledCount;
	}

	public long getUrlCheckedCount() {
		return urlCheckedCount;
	}

	public void setUrlCheckedCount(long urlCheckedCount) {
		this.urlCheckedCount = urlCheckedCount;
	}

	public long getUrlQueuedCount() {
		return urlQueuedCount;
	}

	public void setUrlQueuedCount(long urlQueuedCount) {
		this.urlQueuedCount = urlQueuedCount;
	}

	public long getParsedCount() {
		return parsedCount;
	}

	public void setParsedCount(long parsedCount) {
		this.parsedCount = parsedCount;
	}

	public long getSavedDocCount() {
		return savedDocCount;
	}

	public void setSavedDocCount(long savedDocCount) {
		this.savedDocCount = savedDocCount;
	}

	public long getInQueueCount() {
		return inQueueCount;
	}

	public void setInQueueCount(long inQueueCount) {
		this.inQueueCount = inQueueCount;
	}

	private long  urlCheckedCount;
	private long  urlQueuedCount;
	private long  parsedCount;
	private long  savedDocCount;
	private long  inQueueCount;


	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	


	public int getListeningPort() {
		return listeningPort;
	}

	public void setListeningPort(int listeningPort) {
		this.listeningPort = listeningPort;
	}
}
