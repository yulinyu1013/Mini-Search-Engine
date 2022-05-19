package frontend;

import java.util.ArrayList;
import java.util.List;
import opennlp.tools.stemmer.PorterStemmer;


public class Normalizer {
    public static boolean DEBUG = true;

    public static void error_message(String s) {
    	System.out.println(s);
    }

    
public static String[] query_to_keyWord(String input) {
        StringBuilder sb = new StringBuilder();
        input = input.trim();
        int len = input.length();
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (StopWords.punctuations.contains(c)) {
                continue;
            }
            // not a letter or digit or space
            if (!Character.isLetter(c) && !Character.isDigit(c) && !Character.isSpaceChar(c)) {
                continue;
            }
            // if none-English letter
            if (Character.isLetter(c)) {
                if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z')) {
                    continue;
                }
            }
            // convert to lower case
            if (Character.isUpperCase(c)) {
                c = Character.toLowerCase(c);
            }

            sb.append(c);
        }
        String[] res = sb.toString().split("\\s+");

        return res;
    }
    
	public static List<String> query_to_keyWord_list(String input) {
		PorterStemmer stemmer = new PorterStemmer();
		String[] res = query_to_keyWord(input);
		List<String> result = stemTheWords(res, stemmer);
		return result;
	};

    public static List<String> stemTheWords(String[] strArr, PorterStemmer stemmer) {
    	List<String> stemmedWords = new ArrayList<>();
        // stemming
        for (String word: strArr) {

            // remove stop words before stemming
            if (StopWords.stopWords.contains(word)) {
                continue;
            }

            String stemmedWord = stemmer.stem(word);

            if (StopWords.stopWords.contains(stemmedWord)) {
                continue;
            }
            stemmedWords.add(stemmedWord);
        }
        return stemmedWords;
    }
}