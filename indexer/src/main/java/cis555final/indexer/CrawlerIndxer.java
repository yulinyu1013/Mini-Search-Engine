package cis555final.indexer;

import java.io.IOException;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import DBoperations.DBDop;
import DBoperations.Env;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class CrawlerIndxer {

   public static class WholeFileInputFormat extends FileInputFormat<NullWritable, Text> {
        @Override
        protected boolean isSplitable(JobContext context, Path filename) {
            return false;
        }

        @Override
        public RecordReader<NullWritable, Text> createRecordReader(
          InputSplit split, TaskAttemptContext context) {
            return new WholeFileRecordReader();
        }
    }
    
    public static class WholeFileRecordReader extends RecordReader<NullWritable, Text>{
        private FileSplit fileSplit;
        private Configuration conf;
        private boolean processed = false;
      
        private NullWritable key = NullWritable.get();
        //private BytesWritable value = new BytesWritable();
        private  Text value = new Text();

        public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
            this.fileSplit = (FileSplit) inputSplit;
            this.conf = taskAttemptContext.getConfiguration();
        }

        public boolean nextKeyValue() throws IOException {
            if (!processed) {
                byte[] contents = new byte[(int) fileSplit.getLength()];

                Path file = fileSplit.getPath();
                FileSystem fs = file.getFileSystem(conf);

                FSDataInputStream in = null;
                try {
                    in = fs.open(file);
                    IOUtils.readFully(in, contents, 0, contents.length);                
                    value.set(contents, 0, contents.length);
                } finally {
                    IOUtils.closeStream(in);
                }
                processed = true;
                return true;
            }
            return false;
        }

        @Override
        public NullWritable getCurrentKey() throws IOException, InterruptedException {
            return key;
        }

        @Override
        public Text getCurrentValue() throws IOException, InterruptedException {
            return value;
        }
        
        @Override
        public float getProgress() throws IOException, InterruptedException  {
            return processed ? 1.0f : 0.0f;
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }
    }

    public static void main(String[] args) throws Exception {
        //setup mapper for db
        System.out.println("setting up dynamo db...");
        DBDop.DBDsetup();
        
        
        System.out.println("start mapreduce job...");
        String fileSeperator = "-----xinyilu-----yulinyu-----guanwenqiu-----yuxiaotang-----";
        Configuration conf = new Configuration();
        
        conf.set("textinputformat.record.delimiter", fileSeperator);
        conf.set("totalFilesNum", args[2]);
        
        FileSystem fs = FileSystem.get(conf);
        
        Job job = Job.getInstance(conf, "testjob");
        job.setJarByClass(CrawlerIndxer.class);
        
        //job.getConfiguration().set("fs.s3a.access.key", Env.ACCESS_KEY);
        //job.getConfiguration().set("fs.s3a.secret.key", Env.ACCESS_SECRET );
        //job.getConfiguration().set("fs.default.name","s3a://new-crawler-content/");
        
        
        //job.setInputFormatClass(WholeFileInputFormat.class);
        job.setMapperClass(FileMapper.class);
        job.setReducerClass(TermReducer.class);
        
        job.setNumReduceTasks(5);
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
        
        System.out.println("finish setting dirs");
        
        boolean ret=job.waitForCompletion(true);
        if(!ret){
            throw new Exception("Job Failed");
        }
        
        System.out.println("job finished!!!");

    }
}
