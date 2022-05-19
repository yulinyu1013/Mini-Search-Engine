package indexerwithparser;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import DBoperations.DBDop;
import cis555final.indexerutil.DocParser;
import cis555final.indexerutil.TermFilter;

public class ParserMapper extends Mapper<Object, Text, Text, Text>{
    static final int limit = 30000;
    
    public void map(Object key, Text val, Context context) throws IOException, InterruptedException {
//        System.out.println("i am in mapper");
//        System.out.println("content is");
//        System.out.println(val);
//        System.out.println("---------------");
        
        if(val.getLength() == 0 || val == null || val.toString().equals("\n") ||  val.toString().equals(" ")) return;
        
        Text docid = new Text();
        Text cleanedterm = new Text();
        
        
        String[] urlAndContent = val.toString().trim().split("\n", 2);
        if(urlAndContent.length < 2) return;
        System.out.println("url is " + urlAndContent[0].trim());
        if(urlAndContent[0] == null || urlAndContent[0].length() == 0) return;
        System.out.println("content is " + urlAndContent[1]);
        if(urlAndContent[1] == null || urlAndContent[1].length() == 0) return;

        
        //-------------get url/id----------------
        //Path filePath = ((FileSplit) context.getInputSplit()).getPath();
        //String filePathString = ((FileSplit) context.getInputSplit()).getPath().toString();
        //String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
        //TODO get url or id
        String url = urlAndContent[0];
        docid.set(url);
        
        //-------------get content--------------
        //DocParser parser = new DocParser(urlAndContent[1]);
        String content = urlAndContent[1];
        
        
        // keep only characters -> tokenize TODO could use other packages
        //String[] terms = content.split("[^a-zA-Z0-9']+");
        String[] terms = TermFilter.removeInvalidChars(content);
        
        int termcnt = 0;
        for(String word: terms) {
            // stem
            String stemmedword = TermFilter.wordStemming(word.trim().toLowerCase());
            if(TermFilter.isInStopList(stemmedword)) {
                continue;
            }
            
            cleanedterm.set(stemmedword);
            termcnt++;
            if(termcnt > limit) break;
            
            context.write(docid, cleanedterm);
        }

    }
}