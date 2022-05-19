package indexerwithparser;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import DBoperations.DBDop;

public class ParserReducer extends Reducer<Text, Text, Text, Text>{
    
    public void reduce(Text key, Iterable<Text> vals, Context context) throws IOException, InterruptedException {
        System.out.println(" i am at reducer ...");
        String seperator = " ";
        String lineSep = "---------";
        
        
        
        StringJoiner joiner = new StringJoiner(seperator);
        
        Text docid = new Text(key);
        Text allTerms = new Text();
        
        for(Text val: vals) {
            joiner.add(val.toString());
        }
        
        //String termWithLineSep = joiner.toString() + "\n" + lineSep;
        String termWithLineSep = joiner.toString();
        
        allTerms.set(termWithLineSep);
        context.write(docid, allTerms);
       
    }
}

