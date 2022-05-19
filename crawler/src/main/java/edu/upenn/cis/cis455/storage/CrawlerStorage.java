package edu.upenn.cis.cis455.storage;

import java.util.HashMap;
import java.util.Map;

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
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import edu.upenn.cis.cis455.crawler.CrawlerConfig;

public class CrawlerStorage {
	private Queue queue;
	private Session session;
	private SQSConnection sqsConnection;
	private DynamoDBMapper mapper;
	private AmazonS3 s3Client;
	private String queueUrl;
	private CrawlerConfig config;
	private AmazonSQSMessagingClientWrapper sqsClient;
	public CrawlerStorage(CrawlerConfig config) throws JMSException {
		this.config = config;
		BasicAWSCredentials cred = new BasicAWSCredentials(config.awsKey, 
				config.awsPassword);

		SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
				new ProviderConfiguration(),
				AmazonSQSClient.builder().withRegion(config.getAwsRegion()).
				withCredentials(new AWSStaticCredentialsProvider(cred)).build()
				);
		this.sqsConnection = connectionFactory.createConnection();
		this.sqsConnection.start();
		this.sqsClient = this.sqsConnection.getWrappedAmazonSQSClient();
		if (!sqsClient.queueExists(config.getAwsQueueName())) {
			System.out.println("Queue do not exist. Will create One");
			sqsClient.createQueue(config.getAwsQueueName());
		}
		this.queueUrl = sqsClient.getQueueUrl(config.getAwsQueueName()).getQueueUrl();
		this.session = this.sqsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		this.queue = session.createQueue(config.awsQueueName);
		
		final AmazonDynamoDB ddb = AmazonDynamoDBClient
				.builder().withRegion(config.awsRegion)
				.withCredentials(new AWSStaticCredentialsProvider(cred)).build();
		this.mapper = new DynamoDBMapper(ddb);
		s3Client = AmazonS3Client.builder().withRegion(config.awsRegion).
				withCredentials(new AWSStaticCredentialsProvider(cred)).build();
	}
	public MessageProducer getNewQueueProducer() throws JMSException {
		return session.createProducer(this.queue);
	}
	
	public MessageConsumer getNewQueueConsumer() throws JMSException {
		return session.createConsumer(this.queue);
	}
	
	public UrlMessage receiveUrlCrawlRequest(MessageConsumer consumer, long timeoutMili) throws JMSException{
		Message receivedMessage = consumer.receive(timeoutMili);
		if (receivedMessage == null) 
			return null;
		else
			return new UrlMessage(((TextMessage)receivedMessage).getText());
	}
	
	public void sendUrlCrawlRequest(MessageProducer producer, UrlMessage urlRequest) throws JMSException {
		producer.send(this.session.createTextMessage(urlRequest.encodeToStr()));
	}
	
	public DynamoDBMapper getDynamoDBMapper() {
		return this.mapper;
	}
	
	public void writeHashMapContentToS3(HashMap<String, String> contents, String fileName) {
		String resStr = "";
		for (Map.Entry<String, String> pair : contents.entrySet()){
			String docId = pair.getKey();
			String content = pair.getValue();
			resStr += docId;
			resStr += "\r\n";
			resStr += content;
			resStr += "\r\n";
			resStr += this.config.getFileSperator();
			resStr += "\r\n";
		}
		this.s3Client.putObject(this.config.getAwsBucketName(),
				fileName, resStr);
	}
	public void close() throws JMSException {
		this.sqsConnection.close();
		this.session.close();
	}
}
