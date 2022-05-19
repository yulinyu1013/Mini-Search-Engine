package cis555final.indexer;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;

import DBoperations.DBDop;
import DBoperations.IndexedDoc;
import DBoperations.TermModel;

public class TermReducer extends Reducer<Text, Text, Text, Text>{
    
    static final String executorId = UUID.randomUUID().toString();
    DynamoDBMapper m;
    
    public void setup(Context context) {
         m = DBDop.DBDsetup();
    }
    
    public void reduce(Text key, Iterable<Text> vals, Context context) throws IOException, InterruptedException {
        //System.out.println(" i am at reducer ...!!");
        String seperator = ";";
        String docid = null;

        //DynamoDBMapper dbmapper = DBDop.dbmapper;
        //val: url + tf
        
        Text term = new Text(key);
        Text tfIdf = new Text();
        
        //TODO hardcode for now!!!!
        //should get from S3
        int totalDocs = Integer.parseInt(context.getConfiguration().get("totalFilesNum"));
        int currentTermCount = 0;
        
        List<String> tfurlInfo = new ArrayList<>();
        
        for(Text val: vals) {
            tfurlInfo.add(val.toString());
            currentTermCount++;
        }
        
        //double formatter
//        DecimalFormat df = new DecimalFormat("#.######");
//        df.setRoundingMode(RoundingMode.CEILING);
        
        Double idf = Math.log10( (double) (totalDocs / currentTermCount));
        
        List<TermModel> modelList = new ArrayList<>();
        
        
        for(String info: tfurlInfo) {
           // System.out.println("info is" + info);
            String[] urlandtf = info.split(seperator);
            double tf = -1.0;
            try {
                tf = Double.parseDouble(urlandtf[1]);
            } catch (Exception e) {
                System.out.println(e);
            }
            
            if(tf == -1.0) {
                System.out.println("fail to parse tf value");
            }
            String url = urlandtf[0];
            docid = url;
            
            TermModel termIndex = new TermModel();
            termIndex.setTerm(key.toString());
            termIndex.setTf( tf);
            termIndex.setIdf(idf);
            termIndex.setDocurl(url);
            termIndex.setTfidf((double) tf * idf);
            
            //System.out.println("adding term object.." + termIndex.toString());
            
            modelList.add(termIndex);
            //send data to dynamo
            //DBDop.uploadData(key.toString(), Double.parseDouble(urlandtf[1]) , idf , urlandtf[0]);
            
            //context.write(key, tfIdf);
        }
        
        System.out.println("sending to dynamodb");
        try {
            DBDop.batchUploadData(modelList);
        } catch (DynamoDBMappingException e) {
            System.out.println("fail to send data");
            e.printStackTrace();
            return;
        }
       

        
    }
}
