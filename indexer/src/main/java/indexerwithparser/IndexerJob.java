package indexerwithparser;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import DBoperations.DBDop;
import cis555final.indexer.CrawlerIndxer;
import cis555final.indexer.FileMapper;
import cis555final.indexer.TermReducer;

public class IndexerJob {
    public static boolean startIndexerJob(String inputDir, String outputDir, String totalNum) throws IOException, Exception, InterruptedException {
        System.out.println("setting up dynamo db...");
        DynamoDBMapper m = DBDop.DBDsetup();
        if(m == null) {
            System.out.println("fail to connect to dynamodb");
        }
        
        
        System.out.println("start indexer job...");
        Configuration conf = new Configuration();
        conf.set("textinputformat.record.delimiter", "\n<==>");
        conf.set("totalFilesNum", totalNum);
        
        FileSystem fs = FileSystem.get(conf);
        
        Job job = Job.getInstance(conf, "indexerjob");
        job.setJarByClass(IndexerJob.class);
        job.getConfiguration().set("mapreduce.output.textoutputformat.separator",";");
        
        
        //job.setInputFormatClass(WholeFileInputFormat.class);
        job.setMapperClass(IndexerMapper.class);
        job.setReducerClass(IndexerReducer.class);
        
        
        //job.setNumReduceTasks(3);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        
        FileInputFormat.addInputPath(job, new Path(inputDir));
        
        Path output=new Path(outputDir);
//        try {
//            fs.delete(output, true);
//        } catch (IOException e) {
//            System.out.println("fail to delete output dir");
//        }
        
        FileOutputFormat.setOutputPath(job, output);
        
        boolean ret=job.waitForCompletion(true);
        if(!ret){
            throw new Exception("Job Failed");
        } 
        
        return ret;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("setting up dynamo db...");
        DynamoDBMapper m = DBDop.DBDsetup();
        if(m == null) {
            System.out.println("fail to connect to dynamodb");
        }
        
        
        System.out.println("start indexer job...");
        Configuration conf = new Configuration();
        conf.set("textinputformat.record.delimiter", "\n<==>");
        conf.set("totalFilesNum", args[2]);
        
        FileSystem fs = FileSystem.get(conf);
        
        Job job = Job.getInstance(conf, "indexerjob");
        job.setJarByClass(IndexerJob.class);
        job.getConfiguration().set("mapreduce.output.textoutputformat.separator",";");
        
        
        //job.setInputFormatClass(WholeFileInputFormat.class);
        job.setMapperClass(IndexerMapper.class);
        job.setReducerClass(IndexerReducer.class);
        
        
        //job.setNumReduceTasks(3);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        
        FileInputFormat.addInputPath(job, new Path(args[0]));
        
        Path output=new Path(args[1]);
//        try {
//            fs.delete(output, true);
//        } catch (IOException e) {
//            System.out.println("fail to delete output dir");
//        }
        
        FileOutputFormat.setOutputPath(job, output);
        
        boolean ret=job.waitForCompletion(true);
        if(!ret){
            throw new Exception("Job Failed");
        }
        
        System.out.println("indexer job finished!");
    }

}

