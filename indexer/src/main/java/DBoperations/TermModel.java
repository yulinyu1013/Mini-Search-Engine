package DBoperations;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "InvertedIndexTable")
public class TermModel {
    
    public TermModel() {}
    
    @DynamoDBHashKey(attributeName  = "singleterm")
    private String term;
    //rangekey & GSI ?
    
    @DynamoDBRangeKey(attributeName="docurl")
    private String docurl;
    
    @DynamoDBAttribute(attributeName = "tf")
    private double tf;
    
    @DynamoDBAttribute(attributeName = "idf")
    private double idf;
    //private List<Integer> position;
    @DynamoDBAttribute(attributeName = "tfidf")
    private double tfidf;
    
    
    /**
     * @return the tfidf
     */
    public double getTfidf() {
        return tfidf;
    }

    /**
     * @param tfidf the tfidf to set
     */
    public void setTfidf(double tfidf) {
        this.tfidf = tfidf;
    }

    /**
     * @return the url
     */
    public String getDocurl() {
        return docurl;
    }

    /**
     * @param url the url to set
     */
    public void setDocurl(String url) {
        this.docurl = url;
    }

    /**
     * @return the tf
     */
    public double getTf() {
        return tf;
    }

    /**
     * @param tf the tf to set
     */
    public void setTf(double tf) {
        this.tf = tf;
    }

    /**
     * @return the idf
     */
    public double getIdf() {
        return idf;
    }

    /**
     * @param idf the idf to set
     */
    public void setIdf(double idf) {
        this.idf = idf;
    }

    /**
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * @param term the term to set
     */
    public void setTerm(String term) {
        this.term = term;
    }

    @Override
    public String toString() {
        return "TermModel [term=" + term + ", url=" + docurl + ", tf=" + tf + ", idf=" + idf + ", tfidf=" + tfidf + "]";
    }

    
    
    
    
}
