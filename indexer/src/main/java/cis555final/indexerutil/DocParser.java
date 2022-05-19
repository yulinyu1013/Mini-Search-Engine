package cis555final.indexerutil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DocParser {
    Document doc;
    String rawcontent;
    
    public DocParser(String rawcontent) {
        this.rawcontent = rawcontent;
    }   
    
    //TODO consider when file is too large
    public  String getFileContent() {
        try {
            this.doc = Jsoup.parse(rawcontent);
        } catch (IllegalArgumentException e){
            System.out.println("Document is invalid");
            return "";
        }
        
        if (doc == null || doc.body() == null) {  
            System.out.println("Document is invalid"); 
            return ""; 
        }
        
        doc.select("script,jscript,style").remove();
        
        String content = this.doc.text();
        
        //System.out.println(content);
        return content;
    }
    
    public boolean checkIsEnglishFile() {
        Elements html = doc.select("html[lang]");
        if(html != null) {
            String language = html.attr("lang");
            if(language != null && language.length() > 0) {
                if (language != "en" && language != "en-US" && language != "en-GB")
                {
                    return false;
                } 
            }
        }
        
        return true;
    }
}
