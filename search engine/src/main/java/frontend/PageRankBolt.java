package frontend;


import java.util.Iterator;
import java.util.List;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;


public class PageRankBolt extends Thread {

	int i;
	List<ResultPage> li;
	static DynamoDB dynamoDB;

	public PageRankBolt(List<ResultPage> li, int i, DynamoDB con) {
		this.li = li;
		this.i = i;
		dynamoDB = con;
	}

	public void run() {
		try {
			
			ResultPage d = li.get(i);
			if (d.getId() == null || d.getId().isEmpty())
				return;
			String tableName = "PageRank";
			Table table = dynamoDB.getTable(tableName);
			String docid = d.getId();
			QuerySpec spec = new QuerySpec()
				    .withKeyConditionExpression("link = :id")
				    .withValueMap(new ValueMap()
				        .withString(":id", docid));

			ItemCollection<QueryOutcome> items = table.query(spec);
			Iterator<Item> iterator = items.iterator();
			Item item = null;
			
			while (iterator.hasNext()) {
				item = iterator.next();
				double score = item.getInt("score");

				d.setPageRank_score(score);

				li.set(i, d);
				break;
			}
			
			String tableName2 = "ContentSeen";
			Table table2 = dynamoDB.getTable(tableName2);
			QuerySpec spec2 = new QuerySpec()
				    .withKeyConditionExpression("DocId = :id")
				    .withValueMap(new ValueMap()
				        .withString(":id", docid));

			ItemCollection<QueryOutcome> items2 = table2.query(spec2);
			Iterator<Item> iterator2 = items2.iterator();
			Item item2 = null;
			
			while (iterator2.hasNext()) {
				item2 = iterator2.next();
				String bodyText = item2.getString("Prelude");
				String pageTitle = item2.getString("Title");
				String url = item2.getString("MyUrl");
				String prelude = item2.getString("Prelude");
				d.setPcontent(bodyText);
				d.setTitle(pageTitle);
				d.setUrl(url);
				d.setPrelude(prelude);
				li.set(i, d);
				break;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
