//package edu.upenn.cis.cis455.crawler.handlers;
//
//import java.util.Set;
//
//import edu.upenn.cis.cis455.storage.ServerStorageInterface;
//import edu.upenn.cis.cis455.storage.channel.ChannelQueryKey;
//import spark.Request;
//import spark.Response;
//import spark.Route;
//
//public class MainPageHandler implements Route{
//
//	ServerStorageInterface db;
//	public MainPageHandler(ServerStorageInterface db) {
//		this.db = db;
//	}
//	@Override
//	public Object handle(Request request, Response response) throws Exception {
//		Set<ChannelQueryKey> channelNames = this.db.getAllChannelQueryName();
//		String res = "";
//		String name;
//		for (ChannelQueryKey key: channelNames) {
//			name = key.getChannelName();
//			res += "<li><a href=\"show?channel=" + name + 
//					"\">" + name + "</a></li>\n";
//		}
//		res = "<!DOCTYPE html>\n"
//				+ "<html>\n"
//				+ "<head>\n"
//				+ "<title>Main Page</title>\n"
//				+ "</head>\n"
//				+ "<body>\n"
//				+ "<h1>Welcome "
//				+ request.attribute("user")
//				+ "</h1>\n"
//				+ "<ul>\n"
//				+ res 
//				+ "</ul>\n"
//				+ "</body>\n"
//				+ "</html>";
//		return res;
//	}
//
//}
