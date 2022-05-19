package edu.upenn.cis.cis455.crawler.utils;

import java.util.HashMap;

public class HttpParser {

	public static HashMap<String, String> parseRegisterForm(String content){
		HashMap<String, String> resMap = new HashMap<String, String>();
		for (String i: content.split("&")) {
			String [] sa = i.split("=");
			resMap.put(sa[0], sa[1]);
		}
		return resMap;
	}
	
	public static HashMap<String, String> parseLoginForm(String content){
		HashMap<String, String> resMap = new HashMap<String, String>();
		for (String i: content.split("&")) {
			String [] sa = i.split("=");
			resMap.put(sa[0], sa[1]);
		}
		return resMap;
	}
}
