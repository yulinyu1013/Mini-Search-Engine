package edu.upenn.cis.cis455.storage;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "UrlSeen")
public class URLSeenEntry {

	private String url;
	private long checkedTime;
	
	public URLSeenEntry(String url, long checkedTime) {
		this.url = url;
		this.checkedTime = checkedTime;
	}
	public URLSeenEntry() {}
	@DynamoDBHashKey(attributeName="MyUrl")
	public String getUrl() {
		return this.url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	@DynamoDBAttribute(attributeName="CheckedTime")
	public long getCheckedTime() {
		return this.checkedTime;
	}
	public void setCheckedTime(long checkedTime) {
		this.checkedTime = checkedTime;
	}
}
