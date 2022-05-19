package frontend;

public class IndexerController {
	public String word;
	public String url;
	public double tf_score;

	public IndexerController(String w, String u, double s, String p) {
		word = w;
		url = u;
		tf_score = s;
	}

	public void setWord(String word) {
		this.word = word;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public void setScore(double score) {
		this.tf_score = score;
	}
}
