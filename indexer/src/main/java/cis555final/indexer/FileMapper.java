package cis555final.indexer;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;

import DBoperations.DBDop;
import DBoperations.IndexedDoc;
import cis555final.indexerutil.DocParser;
import cis555final.indexerutil.TermFilter;

public class FileMapper extends Mapper<Object, Text, Text, Text>{
    static final int limit = 30000;
    
    DynamoDBMapper m;
    
    public void setup(Context context) {
         m = DBDop.DBDsetup();
    }
    
    public void map(Object key, Text val, Context context) throws IOException, InterruptedException {
//        System.out.println("i am in mapper");
//        System.out.println("val is");
//        System.out.println(val);
//        System.out.println("---------------");
////        
        
        if(val.getLength() == 0 || val == null || val.toString().equals("\n") ||  val.toString().equals(" ")) return;
        
        Text term = new Text();
        Text tfandurl = new Text();
        
        String[] urlAndContent = val.toString().trim().split("\n", 2);
        
        if(urlAndContent.length < 2) {
            System.out.println("----no url or content provided!!----");
            return;
        }

        System.out.println("url is " + urlAndContent[0].trim());
        if(urlAndContent[0] == null || urlAndContent[0].length() == 0) return;
        System.out.println("content is " + urlAndContent[1]);
        if(urlAndContent[1] == null || urlAndContent[1].length() == 0) return;
        
        String seperator = ";";
        
        double alpha = 0.4;
        
        int maxFreq = 0;
        
        //-------------get url/id----------------

        String url = urlAndContent[0];
        
        System.out.println("checking if indexed before....");
        IndexedDoc indexed = new IndexedDoc();
        indexed.setDocID(url);
        
        try {
            IndexedDoc res = m.load(indexed); 
            
            if(res != null) {
                System.out.println("indexed before, skipping...");
                return;
            }
        }catch (DynamoDBMappingException e) {
            System.out.println("fail to get docid in mapper");
            System.out.println(e);
        }
         

        
        //-------------get content--------------
        //DocParser parser = new DocParser(urlAndContent[1]);
        String content = urlAndContent[1];
        
        if(content == null || content.length() == 0) return;
        
        //------------- tokenize + remove stop word + stemming -------------
        Map<String, Integer> termFrequency = new HashMap<>();
        
        // keep only characters -> tokenize TODO could use other packages
        //String[] terms = content.split("[^a-zA-Z0-9']+");
        String[] terms = TermFilter.removeInvalidChars(content);
        
        int termcnt = 0;
        for(String word: terms) {
            // stem
            String stemmedword = TermFilter.wordStemming(word.toLowerCase());
            if(TermFilter.isInStopList(stemmedword)) {
                continue;
            }
            
            int count = 0;
            if(!termFrequency.containsKey(stemmedword)) {
                count = 1;
            } else {
                count = termFrequency.get(stemmedword) + 1;
            }
            
            termFrequency.put(stemmedword, count);
            maxFreq = Math.max(maxFreq, count);
            termcnt++;
            
            if(termcnt > limit) break;
        }
        
        //double formatter
//        DecimalFormat df = new DecimalFormat("#.######");
//        df.setRoundingMode(RoundingMode.CEILING);
        
        for(String w: termFrequency.keySet()) {
            int cnt = termFrequency.get(w);
            
            Double tfd;
            if(maxFreq != 0) {
                tfd = alpha + (alpha * ((double) cnt / maxFreq));
            } else {
                tfd = 0.0;
            }
            
            String tf = tfd.toString();
            
            
            //set key and value
            term.set(w);
            tfandurl.set(url + seperator + tf);
            
            //System.out.println("sending to reducer...");
            //System.out.println("key: " + term + " val" + tfandurl);
            context.write(term, tfandurl);
            
        }
        
        //save this docid into dynamo
        IndexedDoc indexeddoc = new IndexedDoc();
        indexed.setDocID(url);
        
        try {
            m.save(indexeddoc);
        } catch (DynamoDBMappingException e){
            System.out.println("fail to send docid");
            e.printStackTrace();
            return;
        }
    }
}
