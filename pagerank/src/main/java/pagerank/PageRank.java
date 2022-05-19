package pagerank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import scala.Tuple2;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.common.collect.Iterables;


import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.dynamodb.DynamoDBItemWritable;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * PageRank
 * 
 * @author yulinyu
 *
 */
public class PageRank {
	
	private static final String APP = "PageRank";
	private static final Pattern SPACES = Pattern.compile("\\s+");
	  
	private static class Cumsum implements Function2<Double, Double, Double> {
		private static final long serialVersionUID = 8246247507991561359L;

		@Override
	    public Double call(Double a, Double b) {
	      return a + b;
	    }
	}
	
	public static void main(String[] args) throws Exception {
	    
	    System.out.println("configuring job and context...");
        JavaSparkContext sc = Config.buildSparkContext(APP);
        sc.hadoopConfiguration().set("fs.s3n.awsAccessKeyId", "");
        sc.hadoopConfiguration().set("fs.s3n.awsSecretAccessKey", "");
        
        JobConf jobConf = Config.setDynamoDbJobConf(sc);
        
        System.out.println("building a RDD pointing to the DynamoDB table..."); 
        JavaRDD<String> links = sc.textFile("s3n://cis555-pagerank/edges.txt");
        
        
        System.out.println("creating adj list representation...");
        Function<Tuple2<String, String>, Boolean> filter = t -> (!t._1.equals(t._2));
        JavaPairRDD<String, Iterable<String>> adjList = links.mapToPair(s -> {
            String[] parts = SPACES.split(s);
            return new Tuple2<>(parts[0], parts[1]);
          }).distinct().filter(filter).groupByKey().cache();
  
        
 
        System.out.println("initializing ranks ..."); 
        JavaPairRDD<String, Double> pageRanks = adjList.mapValues(pr -> 1.0);
        
        System.out.println("mapping & reducing...");
        for (int i = 0; i < 5; i++) {
          	System.out.println((i+1) + " iterations...");
            // Calculates URL contributions 
            JavaPairRDD<String, Double> contribs =adjList.leftOuterJoin(pageRanks).values()
              .flatMapToPair(s -> {
                int urlCount = Iterables.size(s._1());
                List<Tuple2<String, Double>> results = new ArrayList<>();
                for (String n : s._1) {
                	if(s._2().isPresent()) {
                		results.add(new Tuple2<>(n, s._2().get() / urlCount));
                	}  else {
                		results.add(new Tuple2<>(n, 0.0 / urlCount));
                	}
                }
                return results.iterator();
              });

          // Re-calculates URL ranks based on neighbor contributions.
          pageRanks = contribs.reduceByKey(new Cumsum()).mapValues(sum -> 0.15 + sum * 0.85);

        }
     
        System.out.println("collecting pagerank...:" + pageRanks.count());
        JavaPairRDD<Text, DynamoDBItemWritable> resultDb = pageRanks.mapToPair( x -> {
        	
        	Map<String, AttributeValue> attributes = new HashMap<>();
        	attributes.put("link", new AttributeValue(x._1().toString()));
        	AttributeValue res = new AttributeValue();
        	res.setN(x._2().toString());
        	attributes.put("score", res);
        	
        	DynamoDBItemWritable dynamoDBItemWritable = new DynamoDBItemWritable();
            dynamoDBItemWritable.setItem(attributes);
            return new Tuple2<>(new Text(""), dynamoDBItemWritable);
        });
        
        
        
        System.out.println("writing to db...");
        resultDb.saveAsHadoopDataset(jobConf);
        
        System.out.println("done!");
        
        sc.stop();
 
	}
}
