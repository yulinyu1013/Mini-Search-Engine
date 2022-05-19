package edu.upenn.cis.cis455.crawler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CrawlerConfig {
	
	public String[] startUrls = {"https://en.wikipedia.org/wiki/Main_Page",
			"https://www.cis.upenn.edu/"};
	public  int numCharToSave = 400;
	public  String agent = "CIS555_CRAWLER";
	public  Set<String> acceptableContentType = new HashSet<String>
	(Arrays.asList("text/html","text/xml", "text/html; charset=UTF-8"));
	public  int headTimeOutMili = 5000;
	public  int getTimeOutMili = 10000;
	public  int connectTimeOutMili = 5000;
	public  int maxDelaySec = 5;
	public  int accessDecideThreshold = 300;
	public  double badAccessLimit = 0.8;
	public  int queueSpoutNum = 2;
	public  int checkTimeBoltNum = 5;
	public int getDefaultDomainCrawlDelay() {
		return defaultDomainCrawlDelay;
	}
	public void setDefaultDomainCrawlDelay(int defaultDomainCrawlDelay) {
		this.defaultDomainCrawlDelay = defaultDomainCrawlDelay;
	}
	public  int docFetcherBoltNum = 200;
	public  int parseBoltNum = 100;
	public  int urlCheckerBoltNum = 200;
	public  int writerBoltNum = 1;
	public  int threadNum = 500;
	public int reportTimePeriodMili = 1000;
	public int defaultDomainCrawlDelay = 0;
	public String awsKey = "";
	public String awsPassword = "";
	public String awsRegion = "us-east-1";
	public String awsQueueName = "crawler_url_queue";
	public String awsBucketName = "crawler-content-bucket";
	public String fileSperator = "-----xinyilu-----yulinyu-----guanwenqiu-----yuxiaotang-----";
	public boolean getByPassLinkExtract() {
		return byPassLinkExtract;
	}
	public void setByPassLinkExtract(boolean byPassLinkExtract) {
		this.byPassLinkExtract = byPassLinkExtract;
	}
	public boolean byPassLinkExtract = false;
	public String getAwsKey() {
		return awsKey;
	}
	public void setAwsKey(String awsKey) {
		this.awsKey = awsKey;
	}
	public String getAwsPassword() {
		return awsPassword;
	}
	public void setAwsPassword(String awsPassword) {
		this.awsPassword = awsPassword;
	}
	public String getAwsRegion() {
		return awsRegion;
	}
	public void setAwsRegion(String awsRegion) {
		this.awsRegion = awsRegion;
	}
	public String getAwsQueueName() {
		return awsQueueName;
	}
	public void setAwsQueueName(String awsQueueName) {
		this.awsQueueName = awsQueueName;
	}
	public String getAwsBucketName() {
		return awsBucketName;
	}
	public void setAwsBucketName(String awsBucketName) {
		this.awsBucketName = awsBucketName;
	}
	public String getFileSperator() {
		return fileSperator;
	}
	public void setFileSperator(String fileSperator) {
		this.fileSperator = fileSperator;
	}
	public int getWriterCachedSize() {
		return writerCachedSize;
	}
	public void setWriterCachedSize(int writerCachedSize) {
		this.writerCachedSize = writerCachedSize;
	}
	public int writerCachedSize = 10;
	public int getReportTimePeriodMili() {
		return reportTimePeriodMili;
	}
	public void setReportTimePeriodMili(int reportTimePeriodMili) {
		this.reportTimePeriodMili = reportTimePeriodMili;
	}
	public String masterIp = null;
	public int masterPort = 0;
	public String[] getStartUrls() {
		return startUrls;
	}
	public String getMasterIp() {
		return masterIp;
	}
	public void setMasterIp(String masterIp) {
		this.masterIp = masterIp;
	}
	public int getMasterPort() {
		return masterPort;
	}
	public void setMasterPort(int masterPort) {
		this.masterPort = masterPort;
	}
	public void setStartUrls(String[] startUrls) {
		this.startUrls = startUrls;
	}
	public int getNumCharToSave() {
		return numCharToSave;
	}
	public void setNumCharToSave(int numCharToSave) {
		this.numCharToSave = numCharToSave;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public Set<String> getAcceptableContentType() {
		return acceptableContentType;
	}
	public void setAcceptableContentType(Set<String> acceptableContentType) {
		this.acceptableContentType = acceptableContentType;
	}
	public int getHeadTimeOutMili() {
		return headTimeOutMili;
	}
	public void setHeadTimeOutMili(int headTimeOutMili) {
		this.headTimeOutMili = headTimeOutMili;
	}
	public int getGetTimeOutMili() {
		return getTimeOutMili;
	}
	public void setGetTimeOutMili(int getTimeOutMili) {
		this.getTimeOutMili = getTimeOutMili;
	}
	public int getConnectTimeOutMili() {
		return connectTimeOutMili;
	}
	public void setConnectTimeOutMili(int connectTimeOutMili) {
		this.connectTimeOutMili = connectTimeOutMili;
	}
	public int getMaxDelaySec() {
		return maxDelaySec;
	}
	public void setMaxDelaySec(int maxDelaySec) {
		this.maxDelaySec = maxDelaySec;
	}
	public int getAccessDecideThreshold() {
		return accessDecideThreshold;
	}
	public void setAccessDecideThreshold(int accessDecideThreshold) {
		this.accessDecideThreshold = accessDecideThreshold;
	}
	public double getBadAccessLimit() {
		return badAccessLimit;
	}
	public void setBadAccessLimit(double badAccessLimit) {
		this.badAccessLimit = badAccessLimit;
	}
	public int getQueueSpoutNum() {
		return queueSpoutNum;
	}
	public void setQueueSpoutNum(int queueSpoutNum) {
		this.queueSpoutNum = queueSpoutNum;
	}
	public int getCheckTimeBoltNum() {
		return checkTimeBoltNum;
	}
	public void setCheckTimeBoltNum(int checkTimeBoltNum) {
		this.checkTimeBoltNum = checkTimeBoltNum;
	}
	public int getDocFetcherBoltNum() {
		return docFetcherBoltNum;
	}
	public void setDocFetcherBoltNum(int docFetcherBoltNum) {
		this.docFetcherBoltNum = docFetcherBoltNum;
	}
	public int getParseBoltNum() {
		return parseBoltNum;
	}
	public void setParseBoltNum(int parseBoltNum) {
		this.parseBoltNum = parseBoltNum;
	}
	public int getUrlCheckerBoltNum() {
		return urlCheckerBoltNum;
	}
	public void setUrlCheckerBoltNum(int urlCheckerBoltNum) {
		this.urlCheckerBoltNum = urlCheckerBoltNum;
	}
	public int getWriterBoltNum() {
		return writerBoltNum;
	}
	public void setWriterBoltNum(int writerBoltNum) {
		this.writerBoltNum = writerBoltNum;
	}
	public int getThreadNum() {
		return threadNum;
	}
	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}
	
}
