//package edu.upenn.cis.cis455.crawler;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.Level;
//
//import static spark.Spark.*;
//
//import edu.upenn.cis.cis455.crawler.handlers.CreateChannelHandler;
//import edu.upenn.cis.cis455.crawler.handlers.LoginFilter;
//import edu.upenn.cis.cis455.crawler.handlers.LoginHandler;
//import edu.upenn.cis.cis455.crawler.handlers.MainPageHandler;
//import edu.upenn.cis.cis455.storage.ServerStorageInterface;
//import edu.upenn.cis.cis455.storage.StorageFactory;
//import edu.upenn.cis.cis455.crawler.handlers.RegisterHandler;
//import edu.upenn.cis.cis455.crawler.handlers.ShowChannelHandler;
//
//public class WebInterface {
//	final static Logger logger = LogManager.getLogger(WebInterface.class);
//    public static void main(String args[]) {
//        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.INFO);
//        if (args.length < 1 || args.length > 2) {
//            System.out.println("Syntax: WebInterface {path} {root}");
//            System.exit(1);
//        }
//
//        if (!Files.exists(Paths.get(args[0]))) {
//            try {
//                Files.createDirectory(Paths.get(args[0]));
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                System.exit(1);
//            }
//        }
//
//        port(45555);
//        ServerStorageInterface database = StorageFactory.getDatabaseInstance(args[0]);
//
//        LoginFilter testIfLoggedIn = new LoginFilter(database);
//
//        if (args.length == 2) {
//            staticFiles.externalLocation(args[1]);
//            staticFileLocation(args[1]);
//        }
//
//
//        before("/*", "*/*", testIfLoggedIn);
//        // TODO:  add /register, /logout, /index.html, /, /lookup
//        post("/login", new LoginHandler(database));
//        get("/login", (req, res) ->{
//        	res.redirect("/login-form.html");
//        	return null;});
//        post("/register", new RegisterHandler(database));
//        get("/register", (req, res) ->{
//        	res.redirect("/register.html");
//        	return null;});
//        get("/", new MainPageHandler(database));
//        redirect.get("/index.html", "/");
//        get("/logout", (req, res) -> {
//        	req.session().invalidate();
//        	res.redirect("/login");
//        	return null;
//        	});
//        get("/lookup", (req, res) -> {
//        	String lookingUrl = req.queryParams("url");
//        	logger.info(lookingUrl);
//        	if (database.hasDocument(lookingUrl)) {
//        		return database.getDocument(lookingUrl);
//        	}
//        	else {
//        		halt(404);
//        	}
//        	return null ;
//        });
//        
//        get("/create/:name", new CreateChannelHandler(database));
//        get("/show", new ShowChannelHandler(database));
//        awaitInitialization();
//    }
//}
