package edu.upenn.cis.cis455.crawler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class testurl {

	public static void main(String[] args) throws IOException {
		URL url = new URL("https://www.baidu.com/robots.txt");
		System.out.println(url.getProtocol());
		System.out.println(url.getHost());
		System.out.println(url.getPath());
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.connect();
		System.out.println(conn.getResponseCode());
		System.out.println(conn.getInputStream().readAllBytes());
	}
}
