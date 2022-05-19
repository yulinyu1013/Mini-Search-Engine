package frontend;

public class PageRankController {
	public String url;
	public double score;
	public String bodyText;
	public String pageTitle;

	public void setUrl(String url) {
		this.url = url;
	}


	public void setScore(double score) {
		this.score = score;
	}


	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}


	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public PageRankController(String u, double s, String b, String p) {
		url = u;
		score = s;
		bodyText = b;
		pageTitle = p;
	}

}
