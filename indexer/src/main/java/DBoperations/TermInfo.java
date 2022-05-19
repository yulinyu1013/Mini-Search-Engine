package DBoperations;

public class TermInfo {
    String term;
    double tf;
    double idf;
    double multi;
    String url;
    
    public TermInfo(String term, double tf, double idf, double multi, String url) {
        this.term = term;
        this.tf = tf;
        this.idf=idf;
        this.multi = multi;
        this.url = url;
    }
}
