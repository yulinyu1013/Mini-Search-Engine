package pagerank;

import org.apache.hadoop.mapred.JobConf;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;


/**
 * 
 * DynamoDB and Spark Configuration
 * 
 * @author yulinyu
 *
 */
public class Config {

	/**
	 * DynamoDB config
	 * @param javaSparkContext
	 * @param AWSAccessKeyId
	 * @param AWSSecretKey
	 * @return
	 */
	public static JobConf setDynamoDbJobConf(JavaSparkContext sc) {
	    
		final JobConf conf = new JobConf(sc.hadoopConfiguration());
	    conf.set("dynamodb.servicename", "dynamodb");
	    conf.set("dynamodb.endpoint", "dynamodb.us-east-1.amazonaws.com");
	    conf.set("dynamodb.regionid", "us-east-1");
	    conf.set("fs.s3n.awsAccessKeyId", "");
	    conf.set("fs.s3n.awsSecretAccessKey", "");
	    conf.set("dynamodb.output.tableName", "PageRank");
	    conf.set("dynamodb.awsAccessKeyId", "");
	    conf.set("dynamodb.awsSecretAccessKey", "");
	    conf.set("mapred.output.format.class", "org.apache.hadoop.dynamodb.write.DynamoDBOutputFormat");
	    return conf;
	}
	
	/**
	 * Setup spark context
	 * @param app
	 * @return
	 * @throws ClassNotFoundException
	 */
    public static JavaSparkContext buildSparkContext(String app) throws ClassNotFoundException {
        SparkConf conf = new SparkConf()
                .setAppName(app)
                .registerKryoClasses(new Class<?>[]{
                	Class.forName("org.apache.hadoop.io.Text"),
                    Class.forName("org.apache.hadoop.dynamodb.DynamoDBItemWritable")
                });
        
        return new JavaSparkContext(conf);
    }
    
    /**
     * Setup local spark context
     * @param app
     * @return
     * @throws ClassNotFoundException
     */
    public static JavaSparkContext buildLocalSparkContext(String app) throws ClassNotFoundException {
        SparkConf conf = new SparkConf()
                .setMaster("local[2]")
                .setAppName(app)
                .registerKryoClasses(new Class<?>[]{
                    Class.forName("org.apache.hadoop.io.Text"),
                    Class.forName("org.apache.hadoop.dynamodb.DynamoDBItemWritable")
                });
        
        return new JavaSparkContext(conf);
    }
	
}
