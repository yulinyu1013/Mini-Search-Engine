package frontend;


public class ResultPage {

	String title;
	String link;
	String pcontent;
	String docId;
	int indexer_count = 0;
	double indexer_score = 0;
	double pageRank_score = 1;
	double table_score = 1;
	double final_score = 1;
	private String prelude;
	int titleIndex = 100000;
	
	public int getTitleIndex() {
		return titleIndex;
	}

	public void setTitleIndex(int titleIndex) {
		this.titleIndex = titleIndex;
	}

	public ResultPage(String url, String bodytext, String title, double table_score, String position, 
			double indexer_score, double pageRank_score) {
		link = url;
		pcontent = bodytext;
		this.title = title;
		this.indexer_score = indexer_score;
		this.pageRank_score = pageRank_score;
		this.final_score = 0.5*pageRank_score+0.5*indexer_score; // TUNE PARAM
	}
	
	// add indexer score
	public ResultPage(String docid, double score, String word) {
		this.docId = docid;
		indexer_score = indexer_score + score;
	}
	
	public double getIndexer_score() {
		return indexer_score;
	}
	
	// add word
	public ResultPage addWord(String word, double score) {
		indexer_score = indexer_score + score;
		return this;
	}
		
	// add pagerank score
	public void setPageRank_score(double pageRank_score) {
		this.pageRank_score = 100*(pageRank_score-0.15);
	}
	public double getPageRank_score() {
		return pageRank_score;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPcontent() {
		return pcontent;
	}
	public void setPcontent(String pcontent) {
		this.pcontent = pcontent;
	}
	public void setPrelude(String prelude) {
		this.prelude = prelude;
	}
	public void setUrl(String link) {
		this.link = link;
	}
	public String getLink() {
		return link;
	}
	public String getPrelude() {
		return prelude;
	}
	public String getId() {
		return docId;
	}
	// for worker
	public void setFinal_score(double final_score) {
		this.final_score = final_score;
	}
	public double getFinal_score() {
		return final_score;
	}
}