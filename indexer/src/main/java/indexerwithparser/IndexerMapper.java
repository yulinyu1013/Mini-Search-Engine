package indexerwithparser;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import DBoperations.DBDop;
import DBoperations.IndexedDoc;

public class IndexerMapper extends Mapper<Object, Text, Text, Text>{
    //---------
    
    //get all the terms from file
    //get docid
    // store <term, frequency> in map
    // record max Fre
    // send out term + docid + idf
    DynamoDBMapper m;
    
    public void setup(Context context) {
         m = DBDop.DBDsetup();
    }
    
    public void map(Object key, Text val, Context context) throws IOException, InterruptedException {
//        System.out.println("i am in indexer mapper...");
//        System.out.println("-----terms in doc ------");
//        System.out.println(val.getLength());
//        System.out.println(val);
        
        
        if(val.getLength() == 0 || val.toString().equals("\n") || val.toString().equals(" ")) return;
        
        double alpha = 0.4;
        String seperator = ";";
        
        String[] values = val.toString().split(";");
        
        String url = values[0];
        System.out.println("url is :" + url);
        
        System.out.println("checking if indexed before....");
        IndexedDoc indexed = new IndexedDoc();
        indexed.setDocID(url);
        
        IndexedDoc res = m.load(indexed);
         
        if(res != null) {
            System.out.println("indexed before, skipping...");
            return;
        }
        
        String termsvals = values[1];
        String[] all = termsvals.split(" ");
       
        
        Text term = new Text();
        Text tfurlInfo = new Text();
        
        
        Map<String, Integer> termFreqMap = new HashMap<>();
        int maxFreq = 0;
        
        for(String t: all) {
            System.out.println(t);
            t = t.trim();
            int cnt = 0;
            if(!termFreqMap.containsKey(t)) {
                cnt = 1;
            } else {
                cnt = termFreqMap.get(t) + 1;
                
            }
            
            termFreqMap.put(t, cnt);
            maxFreq = Math.max(maxFreq, cnt);
        }
        
        
      //double formatter
//        DecimalFormat df = new DecimalFormat("#.######");
//        df.setRoundingMode(RoundingMode.CEILING);
        
        for(String w: termFreqMap.keySet()) {
            int cnt = termFreqMap.get(w);
            
            Double tfd;
            if(maxFreq != 0) {
                tfd = alpha + (alpha * ((double) cnt / maxFreq));
            } else {
                tfd = 0.0;
            }
            
            
            //set key and value
            term.set(w);
            tfurlInfo.set(url + seperator + tfd);
            
            context.write(term, tfurlInfo);
            
        }  
    }
}
