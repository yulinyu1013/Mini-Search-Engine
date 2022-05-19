//package edu.upenn.cis.cis455.storage;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicLong;
//
//import com.amazonaws.auth.AWSStaticCredentialsProvider;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.iterable.S3Objects;
//import com.amazonaws.services.s3.model.Bucket;
//import com.amazonaws.services.s3.model.ListObjectsV2Result;
//import com.amazonaws.services.s3.model.S3ObjectSummary;
//
//public class RenameS3 {
//
//	public static void main(String[] args) {
//		String new_name = "new-crawler-content";
//		String old_name = args[1];
//		AmazonS3 s3client = AmazonS3Client.builder().withRegion(DatabaseConfig.REGION).
//				withCredentials(new AWSStaticCredentialsProvider(DatabaseConfig.CREDS)).build();
//		if (s3client.doesBucketExistV2(new_name)) {
//			Bucket bucket = s3client.createBucket(new_name);
//		}
//		final AtomicLong counter = new AtomicLong(0);
//		
//		S3Objects.inBucket(s3client, old_name).forEach((S3ObjectSummary os) -> {
//			String old_file_name = os.getKey();
////			if (filenames.contains(old_file_name)) 
////				System.out.println("repeat file name");
////			else
////				filenames.add(old_file_name);
//			String new_file_name = old_file_name + ".txt";
//			while (true) {
//				try {
//					s3client.copyObject(old_name, old_file_name, new_name, new_file_name);
//					break;
//				}
//				catch(Exception e) {
//					System.out.println("error happened during copy");
//				}
//			}
//			counter.incrementAndGet();
//			System.out.println(new_file_name + " size: " + counter.get());
//		});
//		
//		System.out.println("sucessful end");
//		
//	}
//}
