package DBoperations;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;


public class DBDop {
    public static DynamoDBMapper dbmapper;
    private static EntityStore store;
    private static PrimaryIndex<String, TermIndex> termIndexInfo;
    
    public static DynamoDBMapper DBDsetup() {
        AWSCredentialsProvider creds = new AWSStaticCredentialsProvider(new BasicAWSCredentials(Env.ACCESS_KEY, Env.ACCESS_SECRET));
        
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(creds)
                .withRegion("us-east-1")
                .build();
        
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        dbmapper = mapper;
        
        if(dbmapper == null) {
            System.out.println("fail to create dbmapper");
        }
        
        return dbmapper;
        //BDB
//        BerkerlyDB berDB = new BerkerlyDB("/database");
//        berDB.setUpEnv();
//        store = berDB.getEntityStore();
//        openAccessors();
    }
    
    public static void uploadData(String term, double tf, double idf, String url) {
        TermModel termIndex = new TermModel();
        termIndex.setTerm(term);
        termIndex.setTf(tf);
        //termIndex.setIdf(idf);
        termIndex.setDocurl(url);
        
        dbmapper.save(termIndex);
    }
    
    public synchronized static void batchUploadData(List<TermModel> list) {
        //or get directly from db?
        if(dbmapper == null) {
            System.out.println("fail to create dbmapper");
        }
        dbmapper.batchSave(list);
    }
    
    private static void openAccessors() {
        termIndexInfo = store.getPrimaryIndex(String.class, TermIndex.class);
    }
    
//    public static void batchUploadFromBDB(String reducerId) {
//        //upload 2000 items a time
//        PrimaryIndex<String,TermIndex> pi = 
//                store.getPrimaryIndex(String.class, TermIndex.class);
//        EntityCursor<TermIndex> pi_cursor = pi.entities();
//        
//        List<TermInfo> list = new ArrayList<>();
//        try {
//            for (TermIndex c : pi_cursor) {
//                
//            }
//           } finally {
//            pi_cursor.close();
//           }
//        
//    }
    
    public static void addItemIntoBDB(String reducerId, TermInfo terminfo) {
        TermIndex  t = termIndexInfo.get(reducerId);
        if(t == null) {
            t = new TermIndex(reducerId);
        }
        
        t.addItemIntoList(terminfo);
        
        termIndexInfo.put(t);
    }
    
    public static void removeallDataRealted(String reducerId) {
        termIndexInfo.delete(reducerId);
    }
    

}
