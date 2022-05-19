//package edu.upenn.cis.cis455.crawler.handlers;
//
//import java.util.HashMap;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import edu.upenn.cis.cis455.crawler.utils.HttpParser;
//import edu.upenn.cis.cis455.storage.ServerDataBase;
//import edu.upenn.cis.cis455.storage.ServerStorageInterface;
//import edu.upenn.cis.cis455.storage.StorageInterface;
//import spark.Request;
//import spark.Response;
//import spark.Route;
//
//public class RegisterHandler implements Route{
//	final static Logger logger = LogManager.getLogger(RegisterHandler.class);
//	StorageInterface db;
//	public RegisterHandler(StorageInterface db) {
//		this.db = db;
//	}
//	@Override
//	public Object handle(Request request, Response response) throws Exception {
//		HashMap<String, String>  paramsMap = HttpParser.parseRegisterForm(request.body());
//		logger.info(paramsMap.get("username"));
//		logger.info(paramsMap.get("password"));
//		if (db.isUserExist(paramsMap.get("username"))) {
//			return "Username exist. Try a different one";
//		}
//		else {
//			db.addUser(paramsMap.get("username"), paramsMap.get("password"));
//			return "<!DOCTYPE html>\n"
//					+ "<html>\n"
//					+ "<head>\n"
//					+ "    <title>Registriation Successful!</title>\n"
//					+ "</head>\n"
//					+ "<body>\n"
//					+ "<h1>Registriation Successful!</h1>\n"
//					+ "<ul>\n"
//					+ "<li><a href=\"/\">Main Menu</a></li>\n"
//					+ "<li><a href=\"/login-form.html\">Login Page</a></li>\n"
//					+ "</ul>\n"
//					+ "</body></html>";
//		}
//	}
//	
//	
//	
//}
