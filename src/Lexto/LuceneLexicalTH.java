/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Lexto;

//import org.apache.lucene.analysis;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

/**
 *
 * @author malang
 */
public class LuceneLexicalTH {
    
    public Analyzer analyzer;
    
    public LuceneLexicalTH(){
        analyzer = new ThaiAnalyzer(Version.LUCENE_45, CharArraySet.EMPTY_SET );
    }
    
    public LuceneLexicalTH(boolean UseStopWords){
        if(UseStopWords)
            analyzer = new ThaiAnalyzer(Version.LUCENE_45 );
        else
            analyzer = new ThaiAnalyzer(Version.LUCENE_45, CharArraySet.EMPTY_SET );
    }
    
    /*
     * Using CharArraySet.EMPTY_SET for none stopwords
     */
    
    public LuceneLexicalTH(CharArraySet StopWords){
        analyzer = new ThaiAnalyzer(Version.LUCENE_45, StopWords);
    }
    
    public String strSplitContent(String Content){
        String result="";
        try (TokenStream tokenStream = analyzer.tokenStream(null, Content)){
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                result += charTermAttribute.toString() + " ";
            }
            tokenStream.end();
        } catch (IOException e){}
        return result;
    }
    
    public String strSplitContent(File F){
        String result = "";
        try (BufferedReader br = new BufferedReader(new FileReader(F));
                TokenStream tokenStream = analyzer.tokenStream(null, br)) {
            CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                result += charTermAttribute.toString() + " ";
            }
            tokenStream.end();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LuceneLexicalTH.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LuceneLexicalTH.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public TokenStream getTokenStream(String Content) throws IOException{
        return analyzer.tokenStream(null, Content);
    }
    
    public static void main(String[] args){
        File f = new File("test.txt");
        LuceneLexicalTH l = new LuceneLexicalTH(true);
        System.out.println(l.strSplitContent(f));
        
    }
    
    
}
