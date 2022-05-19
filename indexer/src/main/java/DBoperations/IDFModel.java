package DBoperations;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

//testidfOnly
@DynamoDBTable(tableName = "testidfOnly")
public class IDFModel {
    public IDFModel() {}
    
    @DynamoDBHashKey(attributeName  = "singleterm")
    private String singleterm;
    //rangekey & GSI ?
    
    @DynamoDBRangeKey(attributeName="idf")
    private double idf;

    /**
     * @return the singleterm
     */
    public String getSingleterm() {
        return singleterm;
    }

    /**
     * @param singleterm the singleterm to set
     */
    public void setSingleterm(String singleterm) {
        this.singleterm = singleterm;
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
    
}
