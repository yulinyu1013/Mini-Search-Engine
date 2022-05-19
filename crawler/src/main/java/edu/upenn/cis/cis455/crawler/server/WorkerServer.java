package edu.upenn.cis.cis455.crawler.server;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.MessageProducer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis.cis455.crawler.CrawlerConfig;
import edu.upenn.cis.cis455.crawler.DocumentFetcherBolt;
import edu.upenn.cis.cis455.crawler.ParseBolt;
import edu.upenn.cis.cis455.crawler.UrlCheckTimeBolt;
import edu.upenn.cis.cis455.crawler.UrlCheckerBolt;
import edu.upenn.cis.cis455.crawler.UrlQueueSpout;
import edu.upenn.cis.cis455.crawler.WriterBolt;
import edu.upenn.cis.cis455.storage.CrawlerStorage;
import edu.upenn.cis.cis455.storage.UrlMessage;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.TopologyContext;
import spark.Spark;

public class WorkerServer {
	int port;
	private CrawlerConfig config;
	static final ObjectMapper om = new ObjectMapper();
	private CrawlerStorage storage;
	private Thread reporterThread;
	private LocalCluster cluster;
    private Topology crawlerTopo; 
    private TopologyContext context;
    
	private static final String URL_QUEUE_SPOUT = "URL_QUEUE_SPOUT";
    private static final String DOCUMENT_FETCHER_BOLT = "DOCUMENT_FETCHER_BOLT";
    private static final String PARSE_BOLT = "Parse_BOLT";
    private static final String URL_CHECK_TIME_BOLT = "URL_CHECK_TIME_BOLT";
    private static final String URL_CHECKER_BOLT= "URL_CHECKER_BOLT";
    private static final String WRITER_BOLT =  "WRITER_BOLT";
    private static final String CRAWLER_TOPO_NAME = "CRAWLER";
    final static Logger logger = LogManager.getLogger(WorkerServer.class);
    private AtomicBoolean isRunning;

    
	public WorkerServer(int port) {
		this.isRunning = new AtomicBoolean(false);
		this.port = port;
		Spark.port(port);
		Spark.post("/start", (req, res) -> {
			try {
				if (isRunning.get()) {
					return "Worker is running. Must stop first";
				}
				logger.info("message received");
				logger.info(req.body());
				this.config = om.readValue(req.body(), CrawlerConfig.class);
				this.storage = new CrawlerStorage(this.config);
				UrlQueueSpout urlQueueSpout = new UrlQueueSpout();
				DocumentFetcherBolt documentFetcherBolt = new DocumentFetcherBolt();
				ParseBolt parseBolt = new ParseBolt();
				WriterBolt writerBolt = new WriterBolt();
				UrlCheckerBolt urlCheckerBolt = new UrlCheckerBolt();
				UrlCheckTimeBolt urlCheckTimeBolt = new UrlCheckTimeBolt();

				MessageProducer producer = storage.getNewQueueProducer();
				TopologyBuilder builder = new TopologyBuilder();
				builder.setSpout(URL_QUEUE_SPOUT, urlQueueSpout, this.config.queueSpoutNum);
				builder.setBolt(URL_CHECK_TIME_BOLT, urlCheckTimeBolt, this.config.checkTimeBoltNum)
						.shuffleGrouping(URL_QUEUE_SPOUT);

				builder.setBolt(DOCUMENT_FETCHER_BOLT, documentFetcherBolt, this.config.docFetcherBoltNum)
						.shuffleGrouping(URL_CHECK_TIME_BOLT);
				builder.setBolt(PARSE_BOLT, parseBolt, this.config.parseBoltNum).shuffleGrouping(DOCUMENT_FETCHER_BOLT);
				builder.setBolt(URL_CHECKER_BOLT, urlCheckerBolt, this.config.urlCheckerBoltNum)
						.shuffleGrouping(PARSE_BOLT);
				builder.setBolt(WRITER_BOLT, writerBolt, this.config.writerBoltNum).shuffleGrouping(PARSE_BOLT);
				for (String startUrl : this.config.startUrls) {
					storage.sendUrlCrawlRequest(producer, new UrlMessage(startUrl, "THE_START_URL"));
				}
				this.cluster = new LocalCluster(this.config);
				this.crawlerTopo = builder.createTopology();
				this.context = cluster.submitTopology(CRAWLER_TOPO_NAME, config, this.crawlerTopo, storage);

				
				WorkerReporter reporter = new WorkerReporter(this.context, port, this.config.masterIp,
						this.config.masterPort, this.config.reportTimePeriodMili);
				this.reporterThread = new Thread(reporter);
				this.reporterThread.start();
				this.isRunning.set(true);
				return "success";
			} catch (Exception e) {
				logger.error("fatal error happened. Worker Server Could not start " + e.getMessage());
				e.printStackTrace();
				return "failed";
			}
		});
		Spark.get("/stop", (req, res)->{
			if (!this.isRunning.get()) {
				return "worker is not running. You must run it first";
			}
			this.reporterThread.interrupt();
			this.cluster.killTopology(CRAWLER_TOPO_NAME);
			this.cluster.shutdown();
			this.isRunning.set(false);
			this.storage.close();
			
			return "success";
		});
	}

	
	public static void main(String[] args) {
    	org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.INFO);
    	int port = Integer.valueOf(args[0]);
    	new WorkerServer(port);
	}
}
