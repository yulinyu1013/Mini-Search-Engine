package edu.upenn.cis.cis455.storage;

import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="DomainSeenWithRobotTxtInfo")
public class DomainSeenWithRobotTxtInfoEntry {
	String domain;
	long robotTxtCrawledTime;
	HashSet<String> disallowedSites;
	int crawDelayInSeconds;
	String agent;
	long domainLastCrawledTime;
	long reserveredQueueTime;
	long totalAccessTime;
	long badRequestTime;
	public DomainSeenWithRobotTxtInfoEntry(String agent, String domain, long robotTxtCrawledTime,
			HashSet<String> disallowedSites, int crawlDelayInSeconds, long domainLastCrawledTime, long reserveredQueueTime) {
		this.agent = agent;
		this.domain = domain;
		this.crawDelayInSeconds = crawlDelayInSeconds;
		this.robotTxtCrawledTime = robotTxtCrawledTime;
		this.disallowedSites = disallowedSites;
		this.domainLastCrawledTime = domainLastCrawledTime;
		this.reserveredQueueTime = reserveredQueueTime;
		this.badRequestTime = 0;
		this.totalAccessTime = 0;
	}
	public DomainSeenWithRobotTxtInfoEntry(String agent, String domain, long robotTxtCrawledTime,
			String robotTxt) {
		this.agent = agent;
		this.domain = domain;
		this.robotTxtCrawledTime = robotTxtCrawledTime;
		this.parseRobotTxt(robotTxt);
	}
	
	public boolean allowPath(URL url) {
		if (this.disallowedSites == null) {
			return true;
		}
		for (String prefix : this.disallowedSites) {
			if (url.getPath().startsWith(prefix))
				return false;
		}
		return true;
	}
	
	public long nextAvailableTime() {
		return Math.max(domainLastCrawledTime, reserveredQueueTime) + this.crawDelayInSeconds * 1000;
	}
	@DynamoDBAttribute(attributeName="TotalAccessTime")
	public long getTotalAccessTime() {
		return this.totalAccessTime;
	}
	
	public void setTotalAccessTime(long totalAccessTime) {
		this.totalAccessTime = totalAccessTime;
	}
	
	@DynamoDBAttribute(attributeName="BadReqestTime")
	public long getBadRequestTime() {
		return this.badRequestTime;
	}
	public void setBadRequestTime(long badRequestTime) {
		this.badRequestTime = badRequestTime;
	}
	
	public DomainSeenWithRobotTxtInfoEntry() {}
	
	public boolean isAllowed(String path) {
		for (String prefix : this.disallowedSites) {
			if (path.startsWith(prefix))
				return false;
		}
		return true;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public void setRobotTxtCrawledTime(long robotTxtCrawledTime) {
		this.robotTxtCrawledTime = robotTxtCrawledTime;
	}
	public void setDisallowedSites(HashSet<String> disallowedSites) {
		this.disallowedSites = disallowedSites;
	}
	public void setCrawledDelayInSeconds(int crawDelayInSeconds) {
		this.crawDelayInSeconds = crawDelayInSeconds;
	}
	@DynamoDBHashKey(attributeName="Domain")
	public String getDomain() {
		return this.domain;
	}
	
	@DynamoDBAttribute(attributeName="ReserveredQueueTime")
	public long getReserveredQueueTime() {
		return this.reserveredQueueTime;
	}
	public long setReserveredQueueTime(long reserveredQueueTime) {
		return this.reserveredQueueTime = reserveredQueueTime;
	}
	
	@DynamoDBAttribute(attributeName="DisallowedSites")
	public HashSet<String> getDisallowedSites() {
		if (this.disallowedSites==null || this.disallowedSites.size() == 0) 
			return null;
		else 
			return this.disallowedSites;
	}
	
	/*
	 * Get crawl delay in seconds 0 if not specified in robots.txt
	 */
	@DynamoDBAttribute(attributeName="CrawlDelayTimeSec")
	public int getCrawledDelayInSeconds() {
		return this.crawDelayInSeconds;
	}
	
	@DynamoDBAttribute(attributeName="RobotTxtCrawledTime")
	public long getRobotTxtCrawledTime() {
		return this.robotTxtCrawledTime;
	}
	
	@DynamoDBAttribute(attributeName="DomainLastCrawledTime")
	public long getDomainLastCrawledTime() {
		return this.domainLastCrawledTime;
	}
	public void setDomainLastCrawledTime(long domainLastCrawledTime) {
		this.domainLastCrawledTime = domainLastCrawledTime;
	}
	
	@DynamoDBIgnore
	public int getCrawledDelayInMili() {
		return this.crawDelayInSeconds*1000;
	}
	
	private void parseRobotTxt(String robotTxt) {
		HashSet<String> disallowedSiteForAllAgent = new HashSet<String> ();
		HashSet<String> disallowedSiteForAgent = new HashSet<String>();
				
		int allCrawDelayInSeconds = 0;
		int agentCrawDelayInSeconds = 0;
		Scanner scanner = new Scanner(robotTxt);
		String line;
		String[] lineSplit;
		boolean seeAgent = false;
		boolean seeAll = false;
		boolean inAgentBlock = false;
		boolean inAllBlock = false;
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			lineSplit = line.split(":");
			if (lineSplit[0].strip().toLowerCase().equals("user-agent")) {
				if (lineSplit[1].strip().equals(agent)) {
					seeAgent = true;
					inAgentBlock = true;
					inAllBlock = false;
				}
				else if (lineSplit[1].strip().equals("*")) {
					seeAll = true;
					inAllBlock = true;
					inAgentBlock = false;
				}
				else {
					inAllBlock = false;
					inAgentBlock = false;
				}
			}
			
			else if (lineSplit[0].strip().toLowerCase().equals("crawl-delay")) {
				int crawDelayInSeconds = Integer.valueOf(lineSplit[1].strip());
				if (inAgentBlock)
					agentCrawDelayInSeconds = crawDelayInSeconds;
				else if (inAllBlock)
					allCrawDelayInSeconds = crawDelayInSeconds;
			}
			else if (lineSplit[0].strip().toLowerCase().equals("disallow")) {
				if (inAgentBlock)
					disallowedSiteForAgent.add(lineSplit[1].strip());
				else if (inAllBlock)
					disallowedSiteForAllAgent.add(lineSplit[1].strip());
			}
		}
		if (seeAgent) {
			this.crawDelayInSeconds = agentCrawDelayInSeconds;
			this.disallowedSites = disallowedSiteForAgent;
		}
		else if (seeAll) {
			this.crawDelayInSeconds = allCrawDelayInSeconds;
			this.disallowedSites = disallowedSiteForAllAgent;
		}
		else {
			this.crawDelayInSeconds = 0;
			this.disallowedSites = new HashSet<String>();
		}
		scanner.close();
	}
}
