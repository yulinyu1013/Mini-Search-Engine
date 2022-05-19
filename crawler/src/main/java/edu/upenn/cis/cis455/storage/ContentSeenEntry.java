package edu.upenn.cis.cis455.storage;

import org.apache.commons.codec.digest.DigestUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "ContentSeen")
public class ContentSeenEntry {
	private String fileHashValue;
	private String url;
	private long crawledTime;
	private String S3Id;
	private String title;
	private String prelude;
	private String contentType;
	private long lastModifiedSince;
	public ContentSeenEntry(String url, String fileHashValue, boolean isHashed, 
			long crawledTime, String S3Id, String title, String prelude,
			long lastModifiedSince, String contentType) {
		if (isHashed)
			this.fileHashValue = fileHashValue;
		else {
			this.fileHashValue = DigestUtils.md5Hex(fileHashValue);
		}
		this.crawledTime = crawledTime;
		this.S3Id = S3Id;
		this.title = title;
		this.prelude = prelude;
		this.lastModifiedSince = lastModifiedSince;
		this.url = url;
		this.contentType = contentType;
	}
	
	public ContentSeenEntry() {}
	@DynamoDBHashKey(attributeName="DocId")
	public String getDocId() {
		return fileHashValue;
	}
	
	public void setDocId(String fileHashValue) {
		this.fileHashValue = fileHashValue;
	}
	@DynamoDBAttribute(attributeName="MyUrl")
	public String getUrl() {
		return this.url;
	}
	@DynamoDBAttribute(attributeName="CrawledTime")
	public long getCrawledTime() {
		return this.crawledTime;
	}
	@DynamoDBAttribute(attributeName="S3Id")
	public String getS3Id() {
		return this.S3Id;
	}
	
	@DynamoDBAttribute(attributeName="Title")
	public String getTitle() {
		return this.title;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	@DynamoDBAttribute(attributeName="ContentType")
	public String getContentType() {
		return this.contentType;
	}
	
	@DynamoDBAttribute(attributeName="LastModifiedSince")
	public long getLastModifiedSince() {
		return this.lastModifiedSince;
	}
	public void setLastModifiedSince(long lastModifiedSince) {
		this.lastModifiedSince = lastModifiedSince;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setCrawledTime(long crawledTime) {
		this.crawledTime = crawledTime;
	}


	public void setS3Id(String s3Id) {
		S3Id = s3Id;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public void setPrelude(String prelude) {
		this.prelude = prelude;
	}


	@DynamoDBAttribute(attributeName="Prelude")
	public String getPrelude() {
		return this.prelude;
	}
	
	public String toString() {
		return "content hash value: " + fileHashValue;
	}
	

}
