package edu.upenn.cis.cis455.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;

import java.util.HashSet;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;


import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazon.sqs.javamessaging.SQSSession;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

public class create_amazon_storage {
	public static void main(String[] args) throws JMSException {
		BasicAWSCredentials cred = new BasicAWSCredentials("AKIASHG6A5EB3LI3OYFQ", 
				"IzZs2i9BYEgW5WxRZC63/PliyaNpkKs1coBvXtb7");
//		SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
//				new ProviderConfiguration(),
//				AmazonSQSClient.builder().withRegion("us-east-1").
//				withCredentials(new AWSStaticCredentialsProvider(cred)).build()
//				);
//		SQSConnection connection = connectionFactory.createConnection();
//		AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();
//		if (!client.queueExists("crawler_url_queue")) {
//			System.out.println("Queue do not exist. Will create One");
//			client.createQueue("crawler_url_queue");
//		}
//		String queueUrl = client.getQueueUrl("crawler_url_queue").getQueueUrl();
//		Session session = connection.createSession(false, SQSSession.AUTO_ACKNOWLEDGE);
//		Queue queue = session.createQueue("crawler_url_queue");
//		MessageProducer producer = session.createProducer(queue);
//		TextMessage message = session.createTextMessage("something");
//		producer.send(message);
//		MessageConsumer consumer = session.createConsumer(queue);
//		connection.start();
//		Message receivedMessage = consumer.receive(1000);
//		if (receivedMessage != null) {
//		    System.out.println("Received: " + ((TextMessage) receivedMessage).getText());
//		}
//		SendMessageRequest message2 = new SendMessageRequest()
//				.withQueueUrl(queueUrl)
//				.withMessageBody("something")
//				.withDelaySeconds(1);
//		SendMessageResult res= client.sendMessage(message2);
//		Message receivedMessage2 = consumer.receive(3000);
//		if (receivedMessage != null) {
//		    System.out.println("Received: " + ((TextMessage) receivedMessage2).getText());
//		}
//		connection.close();
//		
//		CreateTableRequest docEdgeRequest = new CreateTableRequest().withAttributeDefinitions(
//					new AttributeDefinition("FromDocId", ScalarAttributeType.S),
//					new AttributeDefinition("ToDocId", ScalarAttributeType.S))
//				.withKeySchema(
//						new KeySchemaElement("FromDocId", KeyType.HASH),
//						new KeySchemaElement("ToDocId", KeyType.RANGE))
//				.withBillingMode(BillingMode.PAY_PER_REQUEST)
////				.withBillingMode(BillingMode.PROVISIONED)
////				.withProvisionedThroughput(
////						new ProvisionedThroughput(new Long(100), new Long(100)))
//				.withTableName("DocumentEdges");
//		
//		CreateTableRequest contentSeenRequest = new CreateTableRequest().withAttributeDefinitions(
//				new AttributeDefinition("DocId", ScalarAttributeType.S))
//			.withKeySchema(
//					new KeySchemaElement("DocId", KeyType.HASH))
//			.withBillingMode(BillingMode.PAY_PER_REQUEST)
////			.withBillingMode(BillingMode.PROVISIONED)
////			.withProvisionedThroughput(
////					new ProvisionedThroughput(new Long(100), new Long(100)))
//			.withTableName("ContentSeen");
//		
//		
//		CreateTableRequest domainSeenRequest = new CreateTableRequest().withAttributeDefinitions(
//				new AttributeDefinition("Domain", ScalarAttributeType.S))
//			.withKeySchema(
//					new KeySchemaElement("Domain", KeyType.HASH))
//			.withBillingMode(BillingMode.PAY_PER_REQUEST)
////			.withBillingMode(BillingMode.PROVISIONED)
////			.withProvisionedThroughput(
////					new ProvisionedThroughput(new Long(100), new Long(100)))
//			.withTableName("DomainSeenWithRobotTxtInfo");
//		
//		CreateTableRequest urlSeenRequest = new CreateTableRequest().withAttributeDefinitions(
//				new AttributeDefinition("MyUrl", ScalarAttributeType.S))
//			.withKeySchema(
//					new KeySchemaElement("MyUrl", KeyType.HASH))
//			.withBillingMode(BillingMode.PAY_PER_REQUEST)
////			.withBillingMode(BillingMode.PROVISIONED)
////			.withProvisionedThroughput(
////					new ProvisionedThroughput(new Long(100), new Long(100)))
//			.withTableName("UrlSeen");
//		
//		final AmazonDynamoDB ddb = AmazonDynamoDBClient
//				.builder().withRegion("us-east-1")
//				.withCredentials(new AWSStaticCredentialsProvider(cred)).build();
//		try {
//			CreateTableResult result = ddb.createTable(urlSeenRequest);
//			System.out.println(result.getTableDescription().getTableName());
//			result = ddb.createTable(domainSeenRequest);
//			System.out.println(result.getTableDescription().getTableName());
//			result = ddb.createTable(contentSeenRequest);
//			System.out.println(result.getTableDescription().getTableName());
//			result = ddb.createTable(docEdgeRequest);
//			System.out.println(result.getTableDescription().getTableName());
//		} catch (AmazonServiceException e) {
//			System.err.println(e.getErrorMessage());
//		}
//		DynamoDBMapper mapper = new DynamoDBMapper(ddb);
//		HashSet<String> testDisallowedSites = new HashSet<String>();
//		testDisallowedSites.add("someSItes");
//		testDisallowedSites.add("testSItes");
//		testDisallowedSites.add("anotherSItes");
//		DomainSeenWithRobotTxtInfoEntry  testEntry = new DomainSeenWithRobotTxtInfoEntry ("test_agent", "www.test.com",
//				System.currentTimeMillis(), testDisallowedSites, 2, System.currentTimeMillis(), System.currentTimeMillis());
//		mapper.save(testEntry);
//		DomainSeenWithRobotTxtInfoEntry  retrievedTestEntry = mapper.load(DomainSeenWithRobotTxtInfoEntry.class, "www.test.com");
//		System.out.println(retrievedTestEntry.getDisallowedSites().toString());
		AmazonS3 s3client = AmazonS3Client.builder().withRegion("us-east-1").
				withCredentials(new AWSStaticCredentialsProvider(cred)).build();
		if (!s3client.doesBucketExistV2("crawler-content-bucket-5")) {
			Bucket bucket = s3client.createBucket("crawler-content-bucket-5");
			System.out.println("bucket created");
		}
	}
	
}
