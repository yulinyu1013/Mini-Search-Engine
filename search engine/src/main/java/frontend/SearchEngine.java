package frontend;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;




public class SearchEngine {
	private static List<ResultPage> display_li;
	private static String search_bar;
	private static String search_hint;
	private static String jsForObtainingWiki = "";
	private static String jsForAmazonSearch = "";
	public static String LB_ADDRESS;
	public static int MY_LISTEN_PORT;
	
	public static String[] keywords_original;
	private static DynamoDB dynamoDB_con;
	private static String previousQuery = "";
	private static String cleanQuery="";

	public static void main(String[] args) {
		// PageNum PageNum = new PageNum();

		MY_LISTEN_PORT = 10087;
		String currentDir = System.getProperty("user.dir");
		System.out.println(currentDir);
		staticFiles.externalLocation(currentDir + "/src/main/resources/public");
		port(MY_LISTEN_PORT);
		LB_ADDRESS = "http://34.227.229.91" + ":" + PageNum.LBPort;
		
		Heartbeat heartbeat = new Heartbeat(LB_ADDRESS, MY_LISTEN_PORT);
		heartbeat.start();
		dynamoDB_con = dynamoDb_con();
		String htmlHead = "<head>\n<link href=\"./css/frontendWorker.css\" rel=\"stylesheet\" />\n"
				+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js\"></script>\n"
				+ "<script type=\"text/javascript\" src=\"./js/wikiPlugin.js\"></script>\n";
		String pageEnd = "<script>" + "function clickTab(event, tabId) { var i, tabContent, tabLinks; "
				+ "tabContent = document.getElementsByClassName(\"tabContent\"); for (i=0; i < tabContent.length; i++) { tabContent[i].style.display = \"none\";}"
				+ "tablinks = document.getElementsByClassName(\"tablinks\"); for (i=0; i < tablinks.length; i++) { tablinks[i].className = tablinks[i].className.replace(\" active\", \"\");"
				+ "} document.getElementById(tabId).style.display = \"block\"; event.currentTarget.className += \" active\";} document.getElementById(\"defaultOpen\").click();\n "
				+ "document.getElementById('switchLocation').addEventListener('change', function(){"
				+ "var weatherWidget = document.querySelector('.weatherwidget-io');"
				+ "weatherWidget.href = 'https://forecast7.com/en/'+this.value;"
				+ "weatherWidget.dataset.label_1 = this.options[this.selectedIndex].text;" + "__weatherwidget_init();" + "});"
				+ " </script>\n" + "</body></html>";

		get("/InitQuery", (request, response) -> {
			String sentence = request.queryParams("querySentence");

			search_bar = display_searchBar();
			String correctionDiv = "";
			previousQuery = sentence;
			correctionDiv = "<div class=\"correctedSearch\">"
					+ "<div class=\"Query\">Showing Results for <a href=\"/InitQuery?querySentence="
					+ sentence + "\">" + sentence + "</a></div>\n</div>\n";
			search_hint = display_searchHint(sentence);
			display_li = getDisPlayItem(sentence);

			jsForAmazonSearch = "<script type=\"text/javascript\">\n" + "amzn_assoc_placement = \"adunit0\";\n"
					+ "amzn_assoc_tracking_id = \"preject455-09\";\n" + "amzn_assoc_ad_mode = \"search\";\n"
					+ "amzn_assoc_ad_type = \"smart\";\n" + "amzn_assoc_marketplace = \"amazon\";\n"
					+ "amzn_assoc_region = \"US\";\n" + "amzn_assoc_default_search_phrase = \"" + sentence + "\";\n"
					+ "amzn_assoc_default_category = \"All\";\n"
					+ "amzn_assoc_linkid = \"112791a6b28b1450633b6364619622ec\";\n"
					+ "amzn_assoc_search_bar = \"true\";\n" + "amzn_assoc_search_bar_position = \"top\";\n"
					+ "amzn_assoc_title = \"Shop Related Products\";\n" + "</script>\n"
					+ "<script src=\"//z-na.amazon-adsystem.com/widgets/onejs?MarketPlace=US\"></script>";
			String page_content = display_content(display_li);
			String pageNum_bar = display_pageNum_bar(1);
			return "<html>\n" + htmlHead + jsForObtainingWiki + "<body>\n" + search_bar + "<br/>\n" + search_hint
					+ "<br/>\n" + correctionDiv + "<br/>\n" + page_content + pageNum_bar + jsForAmazonSearch + pageEnd;

		});

		get("/Page", (request, response) -> {
			int page_num = Integer.parseInt(request.queryParams("num"));
			String page_content = display_content_page(page_num);
			String pageNum_bar = display_pageNum_bar(page_num);
			return "<html>\n" + htmlHead + jsForObtainingWiki + "<body>\n" + search_bar + search_hint + page_content
					+ pageNum_bar + jsForAmazonSearch + pageEnd;
		});


	}

	// connect to DynamoDB
	public static DynamoDB dynamoDb_con() {
		AWSCredentialsProvider creds = new AWSStaticCredentialsProvider(new BasicAWSCredentials(Env.ACCESS_KEY, Env.ACCESS_SECRET));
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(creds)
                .withRegion("us-east-1")
                .build();
        DynamoDB dynamoDB = new DynamoDB(client);
        return dynamoDB;
	}
	

	// get item
	public static List<ResultPage> getDisPlayItem(String query) {
		List<String> keywords = Normalizer.query_to_keyWord_list(query);
		keywords_original = Normalizer.query_to_keyWord(query);
		for(String q : keywords_original) {
			cleanQuery += q + " "; 
		}
		System.out.println("query: " +cleanQuery);
		
		
		if (keywords == null || keywords.isEmpty()) {
			Normalizer.error_message("Keywods is null or empaty");
			return null;
		}
		if (dynamoDB_con == null) {
			dynamoDB_con = dynamoDb_con();
			if (dynamoDB_con == null) {
				Normalizer.error_message("Dynamo Connection is null");
				return null;
			}
		}
		// First get all components from indexer table, pick top 500 based on count of
		// words
		List<ResultPage> li = new ArrayList<ResultPage>();
		Instant start = Instant.now();
		
		generate_from_indexer(li, dynamoDB_con, keywords);
		Instant end = Instant.now();
		Duration interval = Duration.between(start, end);
		Normalizer.error_message(
				"Inside FrontendWorker class, Running time of generate_from_indexer is: " + interval.toMillis());

		// Then query the PageRank table to get the pageRank score for each url
		start = Instant.now();
		addAttribute_from_pageRank(li, dynamoDB_con);
		end = Instant.now();
		interval = Duration.between(start, end);
		Normalizer.error_message(
				"Inside FrontendWorker class, Running time of addAttribute_from_pageRank is: " + interval.toMillis());

		// Then use Ranking function to sort element of li and return up to top 100
		start = Instant.now();
		List<ResultPage> result = ranking_sort(li);
		end = Instant.now();
		interval = Duration.between(start, end);
		Normalizer.error_message("Inside FrontendWorker class, Running time of ranking_sort is: " + interval.toMillis());

		return result;
	}
	
	public static void generate_from_indexer(List<ResultPage> li, DynamoDB con, List<String> keywords) {
		int n = keywords.size();
		ConcurrentHashMap<String, ResultPage> url_map = new ConcurrentHashMap<>();
		IndexerBolt[] query_bolt_li = new IndexerBolt[n];

		Instant start = Instant.now();
		for (int i = 0; i < n; i++) {
			query_bolt_li[i] = new IndexerBolt(url_map, con, keywords.get(i));
			query_bolt_li[i].start();

		}
		for (int i = 0; i < n; i++) {
			try {
				query_bolt_li[i].join(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Instant end = Instant.now();
		Duration interval = Duration.between(start, end);
		Normalizer.error_message(
				"Inside FrontendWorker class, Method: generate_from_indexer. Running time of indexer_query_bolt is: "
						+ interval.getSeconds());

		// sort urls based on indexer count and pick up to top 500
		List<ResultPage> temp_li = new ArrayList<>();
		Collection<ResultPage> map_c = url_map.values();
		Iterator<ResultPage> iterator = map_c.iterator();
		while (iterator.hasNext()) {
			temp_li.add((ResultPage) iterator.next());
		}
		url_map.clear();
		Collections.sort(temp_li, new Comparator<ResultPage>() {
			@Override
			public int compare(ResultPage d1, ResultPage d2) {
				return Double.compare(d2.getIndexer_score(),d1.getIndexer_score());
			}
		});
		for (int i = 0; i < temp_li.size(); i++) {
			if (i > PageNum.Indexer_record_threshold)
				break;
			li.add(temp_li.get(i));
		}

	}
	
	public static void addAttribute_from_pageRank(List<ResultPage> dis_arr, DynamoDB con) {
		// DisplayItem[] dis_arr = (DisplayItem[]) li.toArray();
		int n = dis_arr.size();
		PageRankBolt[] filter_bolt_li = new PageRankBolt[n];

		for (int i = 0; i < n; i++) {
			filter_bolt_li[i] = new PageRankBolt(dis_arr, i, con);
			filter_bolt_li[i].start();
		}
		for (int i = 0; i < n; i++) {
			try {
				filter_bolt_li[i].join(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @yulinyu1013
	 * @param li
	 * @return
	 */
	public static List<ResultPage> ranking_sort(List<ResultPage> li) {
		  int n = Math.min(li.size(), PageNum.DISPLAY_threshold);
		  List<ResultPage> result = new ArrayList<>();
		  List<ResultPage> result1 = new ArrayList<>(); 
		  List<ResultPage> result2 = new ArrayList<>();
		  
		  for(int i = 0; i<li.size(); i++) {
		   if(li.get(i).getTitle() != null && li.get(i).getTitle().toLowerCase().contains(cleanQuery)) {
		    li.get(i).setTitleIndex(li.get(i).getTitle().toLowerCase().indexOf(cleanQuery));
		    if(li.get(i).getTitle().contains("The Reaper > Marine Corps Training and Education Command > News Article Display")) {
		     System.out.println(li.get(i).getId());
		    }
		    result1.add(li.get(i));
		   }else {
		    result2.add(li.get(i));
		   }

		  }
		  
		  cleanQuery="";

		Collections.sort(result1, new Comparator<ResultPage>() {
		@Override
		public int compare(ResultPage d1, ResultPage d2) {
			
			if(d1.getTitleIndex() < d2.getTitleIndex()) {
				return -1;
			}

			return Double.compare(d2.getFinal_score(),d1.getFinal_score());
		}
		});
		
		
		Collections.sort(result2, new Comparator<ResultPage>() {
			@Override
			public int compare(ResultPage d1, ResultPage d2) {

				return Double.compare(d2.getFinal_score(),d1.getFinal_score());
			}
			});
		for (int i = 0; i < result1.size(); i++) {
			result.add(result1.get(i));
		}
		int k = Math.max(n-result1.size(), 0);
		for (int i = 0; i < k; i++) {
			result.add(result2.get(i));
		}
		return result;
	}
	
	public static String display_content(List<ResultPage> li) {
		if (li == null || li.size() == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		int firstPage_display_link_num = Math.min(PageNum.Each_page_link_num_limit, li.size());
		sb.append("<div class=\"ResutlMain\">\n<div class = \"DisplayContent\">");
		for (int i = 0; i < firstPage_display_link_num; i++) {
			ResultPage item = li.get(i);
			String title = item.getTitle();
			String pcontent = item.getPrelude();
			String link = item.getLink();
			if (title == null || pcontent == null || link == null) {
				Normalizer.error_message("encounter null link inside display_content");
				continue;
			}
			sb.append("<div class = \"DisplayItem\">\n");
			sb.append("<div class = \"DisplayTitle\">\n");
			sb.append(" <a href=\"" + link + "\">" + title + "</a>\n");
			sb.append("</div>\n");
			sb.append("<div class = \"LinkInfo\">");
			sb.append(link);
			sb.append("</div>\n");
			sb.append("<div class = \"DisplayInfo\">\n");

			sb.append("\n");
			if (pcontent.length() > 100) {
				pcontent = pcontent.substring(0, 100) + "...";
			}
			sb.append(pcontent);
			sb.append("</div>\n");
			sb.append("</div>\n");
		}
		sb.append("</div>\n<div class=\"Plugins\"><div class=\"PluginsTab\">\n");

		sb.append("<button class=\"tablinks\" onclick=\"clickTab(event, 'weatherTab')\">Weather</button>\n");
		sb.append("</div>\n");

		sb.append("<div id=\"weatherTab\" class=\"tabContent\">"
				+ "<div class=\"locationSwitchWrapper\">\n<select id=\"switchLocation\">"
				+ "<option value=\"39d95n75d17/philadelphia/\">PHILADELPHIA</option>\n"
				+ "<option value=\"40d71n74d01/new-york/\">NEW YORK</option>\n"
				+ "<option value=\"42d36n71d06/boston/\">BOSTON</option>\n"
				+ "<option value=\"37d77n122d42/san-francisco/\">SAN FRANCISCO</option>\n"
				+ "<option value=\"41d31n72d93/new-haven/\">NEW HAVEN</option>\n"
				+ "<option value=\"39d36n74d42/atlantic-city/\">ATLANTIC CITY</option>\n" + "</select>\n</div>\n"
				+ "<div class=\"weatherWidget\">"
				+ "<a class=\"weatherwidget-io\" href=\"https://forecast7.com/en/39d95n75d17/philadelphia/\" data-label_1=\"Philadelphia\" data-label_2=\"Weather\" data-theme=\"original\" >PHILADELPHIA WEATHER</a>"
				+ "<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src='https://weatherwidget.io/js/widget.min.js';fjs.parentNode.insertBefore(js,fjs);}}(document,'script','weatherwidget-io-js');</script>"
				+ "</div>\n" + "</div>\n");
		sb.append("</div>\n</div>\n");
		return sb.toString();
	}

	public static String display_searchBar() {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class = \"searchBarMain\">");

		String s = "<div class=\"logo\">\n<img src=\"./images/logo.jpg\" >\n</div>\n"
				+ "<div class=\"searchBar\">\n<form action=\"" + LB_ADDRESS + "/redirect\" method = \"GET\">\n"
				+ "<input class=\"searchQuery\" type=\"text\" id=\"querySentence\" name=\"querySentence\"><br><br>\n"
				+ "</form>\n</div>\n";
		sb.append(s);
		sb.append("</div>");
		return sb.toString();
	}
	
	public static String display_searchHint(String query) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class = \"searchHint\">");
		List<String> keywords = Normalizer.query_to_keyWord_list(query);
		if (keywords.isEmpty()) {
			sb.append("Your Input isn't valid, please try another search");
		} else {
			sb.append("Displaying Result for keywords: \n");
			String t = keywords.stream().map(Object::toString).collect(Collectors.joining(" "));
			sb.append(t);
		}
		sb.append("</div>");
		sb.append("\n");
		return sb.toString();
	}
	
	public static String display_pageNum_bar(int current_page) {
		if (display_li == null || display_li.size() == 0)
			return "";
		int links_num = display_li.size();
		if (links_num <= PageNum.Each_page_link_num_limit)
			return "";
		StringBuilder sb = new StringBuilder();
		sb.append("<div class = \"pageNumBar\">");

		int page_num = links_num / PageNum.Each_page_link_num_limit;
		if (links_num % PageNum.Each_page_link_num_limit != 0) {
			page_num++;
		}
		sb.append("<p>");
		for (int i = 1; i <= page_num; i++) {

			if (current_page == i) {
				sb.append("" + i + "  ");
			} else {
				sb.append(" <a href=\"/Page?num=" + i + "\">" + i + "</a> ");
				sb.append("  ");
			}
		}
		sb.append("</p>");
		sb.append("</div>");
		return sb.toString();
	}
	
	public static String display_content_page(int page_num) {
		int n = display_li.size();
		int counter = 1;
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"ResutlMain\">\n<div class = \"DisplayContent\">");
		for (int i = (page_num - 1) * PageNum.Each_page_link_num_limit; i < n; i++) {
			if (counter > PageNum.Each_page_link_num_limit)
				break;
			ResultPage item = display_li.get(i);
			String title = item.getTitle();
			String pcontent = item.getPcontent();
			String link = item.getLink();
			if (title == null || pcontent == null || link == null) {
				Normalizer.error_message("encounter null link inside display_content");
				continue;
			}
			sb.append("<div class = \"DisplayItem\">\n");

			sb.append("<div class = \"DisplayTitle\">\n");
			sb.append(" <a href=\"" + link + "\">" + title + "</a>\n");
			sb.append("</div>\n");

			sb.append("<div class = \"LinkInfo\">");
			sb.append(link);
			sb.append("</div>\n");

			sb.append("<div class = \"DisplayInfo\">");

			if (pcontent.length() > 100) {
				pcontent = pcontent.substring(0, 100) + "...";
			}
			sb.append(pcontent);
			sb.append("</div>\n");
			sb.append("</div>\n");
			counter++;
		}
		sb.append("</div>\n<div class=\"Plugins\"><div class=\"PluginsTab\">\n");
//		sb.append(
//				"<button class=\"tablinks\" onclick=\"clickTab(event, 'wikiTab')\" id=\"defaultOpen\">Wikipedia</button>\n");

		sb.append("<button class=\"tablinks\" onclick=\"clickTab(event, 'weatherTab')\">Weather</button>\n");
		sb.append("</div>\n");
//		sb.append(
//				"<div id=\"wikiTab\" class=\"tabContent\"><div id=\"wiki\" class=\"wikipediaDivision\"></div><div class=\"wikiBackground\"></div>\n</div>\n");

		sb.append("<div id=\"weatherTab\" class=\"tabContent\">"
				+ "<div class=\"locationSwitchWrapper\">\n<select id=\"switchLocation\">"
				+ "<option value=\"39d95n75d17/philadelphia/\">PHILADELPHIA</option>\n"
				+ "<option value=\"40d71n74d01/new-york/\">NEW YORK</option>\n"
				+ "<option value=\"42d36n71d06/boston/\">BOSTON</option>\n"
				+ "<option value=\"37d77n122d42/san-francisco/\">SAN FRANCISCO</option>\n"
				+ "<option value=\"41d31n72d93/new-haven/\">NEW HAVEN</option>\n"
				+ "<option value=\"39d36n74d42/atlantic-city/\">ATLANTIC CITY</option>\n" + "</select>\n</div>\n"
				+ "<div class=\"weatherWidget\">"
				+ "<a class=\"weatherwidget-io\" href=\"https://forecast7.com/en/39d95n75d17/philadelphia/\" data-label_1=\"Philadelphia\" data-label_2=\"Weather\" data-theme=\"original\" >PHILADELPHIA WEATHER</a>"
				+ "<script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src='https://weatherwidget.io/js/widget.min.js';fjs.parentNode.insertBefore(js,fjs);}}(document,'script','weatherwidget-io-js');</script>"
				+ "</div>\n" + "</div>\n");
		sb.append("</div>\n</div>\n");
		return sb.toString();
	}
}
