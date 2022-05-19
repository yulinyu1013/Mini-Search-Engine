package DBoperations;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "IndexedDocID")
public class IndexedDoc {
    public IndexedDoc() {}
    
    @DynamoDBHashKey(attributeName  = "docID")
    private String docID;

    /**
     * @return the docID
     */
    public String getDocID() {
        return docID;
    }

    /**
     * @param docID the docID to set
     */
    public void setDocID(String docID) {
        this.docID = docID;
    }
}
