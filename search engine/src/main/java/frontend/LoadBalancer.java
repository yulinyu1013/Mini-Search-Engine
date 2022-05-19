package frontend;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class LoadBalancer {

    private static List<String> worker_li = new LinkedList<>();
    private static Map<String, SEController> worker_map = new HashMap<>();
    private static Semaphore update_lock = new Semaphore(1);
    public static String LOADBALANCER_IP;
    public static String LOADBALANCER_ADDRESS;

    public static void main(String[] args) {

        String currentDir = System.getProperty("user.dir");
        staticFiles.externalLocation(currentDir + "/src/main/resources/public");
        port(PageNum.LBPort);

        HBChecker chb = new HBChecker();
        chb.start();

        get("/workerPort", (request, response) -> {
            String w_port = request.queryParams("port");
            String w_addr = request.ip() + ":" + w_port;
            update(w_addr);
            return "OK";
        });

        get("/", (request, response) -> {

            String nextOne = nextWorker();
            if (nextOne == null) {

                return "<html><body><p>No available worker right now. Please Wait</p></body></html>";
            }
            return "<!DOCTYPE html>\n<html>\n" + "<head>\n" + "<meta name=\"author\" content=\"Group202\">\n"
                    + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
                    + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n"
                    + "<link href=\"css/loadBalancer.css\" rel=\"stylesheet\" />" + "</head>\n" + "<body>\n"
                    + "<div class=\"searchBar\">\n" + "<form action=\"http://" + nextOne
                    + "/InitQuery\" method = \"GET\">\n" + "<fieldset>\n" + "<div class=\"mainPageLogo\">\n"
                    + "<img src=\"./images/main_page_logo.jpg\" >\n" + "</div>\n" + "<div class=\"inner\">\n"
                    + "<div class=\"input\">" + "<input class=\"form-control\" type=\"text\" name=\"querySentence\"/>\n"
                    + "<button class=\"searchButton\" type=\"button\">\n"
                    + "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\">\n"
                    + "<path d=\"M12.5,11 L11.7,11 L11.4,10.7 C12.4,9.6 13,8.1 13,6.5 C13,2.9 10.1,0 6.5,0 C2.9,0 0,2.9 0,6.5 C0,10.1 2.9,13 6.5,13 C8.1,13 9.6,12.4 10.7,11.4 L11,11.7 L11,12.5 L16,17.5 L17.5,16 L12.5,11 L12.5,11 Z M6.5,11 C4,11 2,9 2,6.5 C2,4 4,2 6.5,2 C9,2 11,4 11,6.5 C11,9 9,11 6.5,11 L6.5,11 Z\">\n</path>\n </svg>\n</button>\n"
                    + "</div>\n" + "</div>\n" + "</fieldset>\n" + "</form>\n" + "</div>\n" + "</body>\n" + "</html>\n";

        });

        get("/redirect", (request, response) -> {
            String sentence = request.queryParams("querySentence");
            String nextOne = nextWorker();
            if (nextOne == null) {
                return "<html><body><p>No available worker right now. Please Wait</p></body></html>";
            }
            response.redirect("http://" + nextOne + "/InitQuery?querySentence=" + sentence);
            Normalizer.error_message("redirect to url: " + "http://" + nextOne + "/InitQuery?querySentence=" + sentence);
            return "";
        });

    }

    public static String nextWorker() {

        try {
            update_lock.acquire();
            if (worker_li.isEmpty()) {
                update_lock.release();
                return null;
            }
            String s = worker_li.remove(0);
            worker_li.add(s);
            update_lock.release();
            return s;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void update(String w_addr) {
        try {
            update_lock.acquire();
            if (worker_map.containsKey(w_addr)) {
                SEController info = worker_map.get(w_addr);
                info.setLastAccessTime();
                worker_map.put(w_addr, info);

            } else {
                SEController info = new SEController(w_addr);
                worker_map.put(w_addr, info);
                worker_li.add(w_addr);
            }
            update_lock.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static List<String> getWorker_li() {
        return worker_li;
    }

    public static Map<String, SEController> getWorker_map() {
        return worker_map;
    }

    public static Semaphore getUpdate_lock() {
        return update_lock;
    }
}