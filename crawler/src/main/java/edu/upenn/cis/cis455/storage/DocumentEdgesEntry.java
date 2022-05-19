package edu.upenn.cis.cis455.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "DocumentEdges")
public class DocumentEdgesEntry {

	private String fromDocId;
	private String toDocId;
	
	public DocumentEdgesEntry(String fromDocId, String toDocId) {
		this.fromDocId = fromDocId;
		this.toDocId = toDocId;
		
	}
	public DocumentEdgesEntry() {}
	@DynamoDBHashKey(attributeName="FromDocId")
	public String getFromDocId() {
		return this.fromDocId;
	}
	public void setFromDocId(String fromDocId) {
		this.fromDocId = fromDocId;
	}
	
	@DynamoDBRangeKey(attributeName="ToDocId")
	public String getToDocId() {
		return this.toDocId;
	}
	public void setToDocId(String toDocId) {
		this.toDocId = toDocId;
	}
}
