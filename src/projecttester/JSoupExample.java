/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

/**
 *
 * @author malang
 */
public class JSoupExample {
    public static void RunExample(String FileName){
        
        JSoupExample jsex = new JSoupExample();
        try {
            ArcUtils.AnalyseAll(new File(FileName), jsex, JSoupExample.class.getMethod("GetHtml", String.class, String.class)) ;
            //ArcUtils.AnalyseContent(FileName, jsex, JSoupExample.class.getMethod("GetHtmlC", String.class)) ;
            URI s = new URI("http://www.ubukuk.com/a/b/c/../e/55");
            s = s.resolve("../../../../kuu");
            System.out.println(s.toString());
            
        } catch (NoSuchMethodException | SecurityException | URISyntaxException ex) {
            Logger.getLogger(JSoupExample.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    public void GetHtml(String Header, String Content){
        System.out.println(ArcUtils.URLFromHeader(Header) + " :-------------: vvvv");
        Document doc = Jsoup.parse(Content.replaceFirst("^.*([^\n]*\n){6}", ""));
        doc.select("script,style,.hidden").remove();
        removeComments(doc);
        doc.outputSettings().indentAmount(0);
        doc.outputSettings().prettyPrint(false);
        System.out.println(doc.html());
        System.out.println("<--------------------------------------------------------->");
    }
    
    public void GetHtmlC(String Content){
        //System.out.println(ArcUtils.URLFromHeader(Header) + " :-------------: vvvv");
        Document doc = Jsoup.parse(Content);
        System.out.println(doc.text());
        System.out.println("<--------------------------------------------------------->");
    }
    
    public void removeComments(Node node){
        int i = 0;
        while (i < node.childNodes().size()) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment"))
                child.remove();
            else {
                removeComments(child);
                i++;
            }
        }
    }
    
    public static void main(String[] args){
        try {
            Document doc = Jsoup.parse(new File("www.payakorn.com-astrolink.txt"), "utf-8");
            System.out.println("Finished");
            
        } catch (IOException ex) {
            Logger.getLogger(JSoupExample.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
