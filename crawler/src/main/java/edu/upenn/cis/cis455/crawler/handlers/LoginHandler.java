//package edu.upenn.cis.cis455.crawler.handlers;
//
//import spark.Request;
//import spark.Route;
//import spark.Response;
//import spark.HaltException;
//import spark.Session;
//
//import java.util.HashMap;
//
//import edu.upenn.cis.cis455.crawler.utils.HttpParser;
//import edu.upenn.cis.cis455.storage.StorageInterface;
//
//public class LoginHandler implements Route {
//    StorageInterface db;
//
//    public LoginHandler(StorageInterface db) {
//        this.db = db;
//    }
//
//    @Override
//    public String handle(Request req, Response resp) throws HaltException {
//        HashMap<String, String> params = HttpParser.parseLoginForm(req.body());
//        String user = params.get("username");
//        String pass = params.get("password");
//        System.err.println("Login request for " + user + " and " + pass);
//        if (db.getSessionForUser(user, pass)) {
//            System.err.println("Logged in!");
//            Session session = req.session();
//            session.maxInactiveInterval(5*60);
//            session.attribute("user", user);
//            session.attribute("password", pass);
//            resp.redirect("/index.html");
//        } else {
//            System.err.println("Invalid credentials");
//            resp.redirect("/login-form.html");
//        }
//        return "";
//    }
//}
