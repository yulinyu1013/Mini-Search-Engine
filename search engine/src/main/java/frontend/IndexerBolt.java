package frontend;


import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;


public class IndexerBolt extends Thread {
	ConcurrentHashMap<String, ResultPage> map;
	String word;
	static DynamoDB dynamoDB;
	public IndexerBolt(ConcurrentHashMap<String, ResultPage> m, DynamoDB con, String word) {
		map = m;
		dynamoDB = con;
		this.word = word;

	}


	public void run() {
		try {
			String tableName = "InvertedIndexTable";
			Index table = dynamoDB.getTable(tableName).getIndex("singleterm-tfidf-index");

			QuerySpec spec = new QuerySpec()
				    .withKeyConditionExpression("singleterm = :word")
				    .withValueMap(new ValueMap()
				        .withString(":word", word))
				    .withScanIndexForward(false);
			
			ItemCollection<QueryOutcome> items = table.query(spec);
			Iterator<Item> iterator = items.iterator();
			Item item = null;
			int i = 0;
			while (iterator.hasNext() && i < 500) {
				i++;
			    item = iterator.next();
			    String docid = item.getString("docurl").trim();
				double tf_score = item.getDouble("tfidf");
				map.compute(docid, (k, v) -> {
				if (v == null) {
					return new ResultPage(docid, tf_score, word);
				} else
					return v.addWord(word, tf_score);
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}