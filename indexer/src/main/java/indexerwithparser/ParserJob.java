package indexerwithparser;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

import cis555final.indexer.CrawlerIndxer;
import cis555final.indexer.FileMapper;
import cis555final.indexer.TermReducer;
import cis555final.indexer.CrawlerIndxer.WholeFileInputFormat;
import cis555final.indexer.CrawlerIndxer.WholeFileRecordReader;

/**
 * class to parse docs into terms
 * return docid (url) with list of terms
 * @author lxy
 *
 */
public class ParserJob {
    
    public static class OutputFormat<K,V> extends FileOutputFormat<K, V> {
        public static String FIELD_SEPARATOR = "mapreduce.output.textoutputformat.separator";
        public static String RECORD_SEPARATOR = "mapreduce.output.textoutputformat.recordseparator";
        @Override
        public RecordWriter<K, V> getRecordWriter(TaskAttemptContext job) throws IOException, InterruptedException {
            Configuration conf = job.getConfiguration();
            boolean isCompressed = getCompressOutput(job);
            String fieldSeprator = conf.get(FIELD_SEPARATOR, "\t");
            //custom record separator, \n used as a default
            String recordSeprator = conf.get(RECORD_SEPARATOR, "\n");
            //compress output logic
            CompressionCodec codec = null;
            String extension = "";
            if (isCompressed) {
                Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(job, GzipCodec.class);
                codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, conf);
                extension = codec.getDefaultExtension();
            }
            Path file = this.getDefaultWorkFile(job, extension);
            FileSystem fs = file.getFileSystem(conf);
            FSDataOutputStream fileOut = fs.create(file, false);
            if(isCompressed){
                return new LineRecordWriter<>(new DataOutputStream(codec.createOutputStream(fileOut)), fieldSeprator,recordSeprator);
            }else{
                return new LineRecordWriter<>(fileOut, fieldSeprator,recordSeprator);
            }
        }
//        @Override
//        public RecordWriter<K, V> getRecordWriter(TaskAttemptContext job) throws IOException, InterruptedException {
//            // TODO Auto-generated method stub
//            return null;
//        }
    }
    
    public static class LineRecordWriter<K, V> extends RecordWriter<K, V> {
        protected DataOutputStream out;
        private final byte[] recordSeprator;
        private final byte[] fieldSeprator;
        public LineRecordWriter(DataOutputStream out, String fieldSeprator,String recordSeprator) {
            this.out = out;
            this.fieldSeprator = fieldSeprator.getBytes(StandardCharsets.UTF_8);
            this.recordSeprator = recordSeprator.getBytes(StandardCharsets.UTF_8);
        }
        public LineRecordWriter(DataOutputStream out) {
            this(out, "\t","\n");
        }
        private void writeObject(Object o) throws IOException {
            if (o instanceof Text) {
                Text to = (Text)o;
                this.out.write(to.getBytes(), 0, to.getLength());
            } else {
                this.out.write(o.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        public synchronized void write(K key, V value) throws IOException {
            boolean nullKey = key == null || key instanceof NullWritable;
            boolean nullValue = value == null || value instanceof NullWritable;
            if (!nullKey || !nullValue) {
                if (!nullKey) {
                    this.writeObject(key);
                }
                if (!nullKey && !nullValue) {
                    this.out.write(this.fieldSeprator);
                }
                if (!nullValue) {
                    this.writeObject(value);
                }
                this.out.write(recordSeprator);//write custom record separator instead of NEW_LINE
            }
        }
        public synchronized void close(TaskAttemptContext context) throws IOException {
            this.out.close();
        }
    }
    
    
    
    public static boolean startParseJob(String inputDir, String outputDir) throws Exception {
        Configuration conf = new Configuration();
        System.out.println("start mapreduce job -- parse job ...");
        String fileSeperator = "-----xinyilu-----yulinyu-----guanwenqiu-----yuxiaotang-----";
        conf.set("textinputformat.record.delimiter", fileSeperator);
        
        FileSystem fs = FileSystem.get(conf);
        
        Job job = Job.getInstance(conf, "parserjob");
        job.setJarByClass(ParserJob.class);
        
        
        
        job.setOutputFormatClass(OutputFormat.class);
        job.getConfiguration().set("mapreduce.output.textoutputformat.recordseparator","\n<==>");
        job.getConfiguration().set("mapreduce.output.textoutputformat.separator",";");;
        
        
        //job.setInputFormatClass(WholeFileInputFormat.class);
        job.setMapperClass(ParserMapper.class);
        job.setReducerClass(ParserReducer.class);
        
        //job.setNumReduceTasks(2);
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
            System.out.println("job failed");
            throw new Exception("Job Failed");
        } 
        
        return ret;
    }
    
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        System.out.println("start mapreduce job -- parse job ...");
        String fileSeperator = "-----xinyilu-----yulinyu-----guanwenqiu-----yuxiaotang-----";
        conf.set("textinputformat.record.delimiter", fileSeperator);
        
        FileSystem fs = FileSystem.get(conf);
        
        Job job = Job.getInstance(conf, "parserjob");
        job.setJarByClass(ParserJob.class);
        
        
        
        job.setOutputFormatClass(OutputFormat.class);
        job.getConfiguration().set("mapreduce.output.textoutputformat.recordseparator","\n<==>");
        job.getConfiguration().set("mapreduce.output.textoutputformat.separator",";");;
        
        
        //job.setInputFormatClass(WholeFileInputFormat.class);
        job.setMapperClass(ParserMapper.class);
        job.setReducerClass(ParserReducer.class);
        
        //job.setNumReduceTasks(2);
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
            System.out.println("job failed");
            throw new Exception("Job Failed");
        } 
    }

}
