package edu.upenn.cis.cis455.crawler.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class RobotTxtInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2300761889456719824L;
	ArrayList<String> disallowedSite;
	int crawDelayInSeconds;
	String agent;
	public RobotTxtInfo(String agent, String robotTxt) {
		ArrayList<String> disallowedSiteForAllAgent = new ArrayList<String> ();
		ArrayList<String> disallowedSiteForAgent = new ArrayList<String>();
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
			this.disallowedSite = disallowedSiteForAgent;
		}
		else if (seeAll) {
			this.crawDelayInSeconds = allCrawDelayInSeconds;
			this.disallowedSite = disallowedSiteForAllAgent;
		}
		else {
			this.crawDelayInSeconds = 0;
			this.disallowedSite = new ArrayList<String>();
		}
		scanner.close();
	}
	
	
	public boolean isAllowed(String path) {
		for (String prefix : this.disallowedSite) {
			if (path.startsWith(prefix))
				return false;
		}
		return true;
	}
	
	public HashSet<String> getDisallowedSite() {
		return new HashSet<String>(this.disallowedSite);
	}
	
	/*
	 * Get crawl delay in seconds 0 if not specified in robots.txt
	 */
	public int getCrawledDelayInSeconds() {
		return this.crawDelayInSeconds;
	}
	
	public int getCrawledDelayInMili() {
		return this.crawDelayInSeconds*1000;
	}
}
