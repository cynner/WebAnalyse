/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 *
 * @author malang
 */
public class WebUtils {

    public static String CharsetPredictor(String charset) {
        try {
            if (!Charset.isSupported(charset)) {
                if (charset.startsWith("window-")) {
                    charset = charset.replaceFirst("window-", "windows-");
                } else if (charset.startsWith("win-")) {
                    charset = charset.replaceFirst("win-", "windows-");
                } else if (charset.startsWith("win")) {
                    charset = "windows-874";
                }

                if (!Charset.isSupported(charset)) {
                    charset = "utf-8";
                }
            }
        } catch (IllegalCharsetNameException e) {
            charset = "utf-8";
        }
        return charset;
    }

    public static String CharsetFromWebContentType(String WebContentType) {
        String charset = "utf-8";
        String[] tmp;

        for (String x : WebContentType.split(";")) {
            tmp = x.trim().toLowerCase().split("=");
            if (tmp.length == 2 && tmp[0].trim().equals("charset")) {
                charset = tmp[1].replaceAll(",.*", "").trim();
                break;
            }
        }
        return CharsetPredictor(charset);
    }

    public static void AnalyseCharsetFromData(WebArcRecord Record) {
        String oldCharSet;
        if(Record.charset != null)
            oldCharSet = Record.charset;
        else
            oldCharSet = Record.charset = Charset.defaultCharset().name();
        
        Record.charset = oldCharSet;
        
        try {
            Record.WebContent = new String(Record.Data, Charset.forName(oldCharSet));
        }catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
            System.err.println(ex.getMessage());
            oldCharSet = Record.charset = Charset.defaultCharset().name();
            Record.WebContent = new String(Record.Data, Charset.defaultCharset());
        }

        Record.Doc = Jsoup.parse(Record.WebContent);
        Elements list = Record.Doc.select("meta");
        for (Element e : list) {
            if ("content-type".equalsIgnoreCase(e.attr("http-equiv")) && !e.attr("content").equals("")) {
                Record.charset = WebUtils.CharsetFromWebContentType(e.attr("content"));
                break;
            } else if (!e.attr("charset").equals("")) {
                Record.charset = e.attr("charset").toLowerCase().trim();
                Record.charset = WebUtils.CharsetPredictor(Record.charset);
                break;
            }
        }
        if (!oldCharSet.equalsIgnoreCase(Record.charset)) {
            try {
                String tmp = new String(Record.Data, Charset.forName(Record.charset));
                Record.WebContent = tmp;
                Record.Doc = Jsoup.parse(Record.WebContent);
            } catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
                Record.charset = oldCharSet;
                System.err.println(ex.getMessage());
            }
        }

    }
    
    public Document doc;
    public int FileSize;
    public int CommentSize;    
    public int ScriptSize;    
    public int StyleSize;  
    public int ContentSize;

    
    public WebUtils(String Content){
        doc = Jsoup.parse(Content);
    }
    
    public WebUtils(Document doc){
        this.doc = doc;
    }
    
     public WebUtils(){
         
     }
    
    public String HTMLCompress(){
        Elements eScripts,eStyles;
        String result;
        doc.outputSettings().prettyPrint(false);
        FileSize = doc.html().getBytes().length;
        eScripts = doc.select("script");
        eStyles = doc.select("style");
        JSoupRemoveComments(doc);
        ScriptSize = eScripts.outerHtml().getBytes().length;
        StyleSize = eStyles.outerHtml().getBytes().length;
        eScripts.remove();
        eStyles.remove();
        doc.outputSettings().prettyPrint(true);
        doc.outputSettings().indentAmount(0);
        result = doc.html().replaceAll("\n", "");
        ContentSize = result.getBytes().length;
        return result;
    }
    
    public String HTMLPrettyCompress(){
        Elements eScripts,eStyles;
        doc.outputSettings().prettyPrint(false);
        FileSize = doc.html().getBytes().length;
        eScripts = doc.select("script");
        eStyles = doc.select("style");
        JSoupRemoveComments(doc);
        ScriptSize = eScripts.outerHtml().getBytes().length;
        StyleSize = eStyles.outerHtml().getBytes().length;
        eScripts.remove();
        eStyles.remove();
        doc.outputSettings().prettyPrint(true);
        doc.outputSettings().indentAmount(1);
        ContentSize = doc.html().getBytes().length;
        return doc.html();
    }
    
    public static void main(String [] args){
        Document doc = Jsoup.parse("gugggg<htMl><!--Remove this--> \t\t<a href = '\thttp://www.HHH.com/gug'>ma ma  ma </a>\n<a>ddd</a>  </html>");
        //doc.select("script,style").remove();
        //JSoupRemoveComments(doc);
        //doc.outputSettings().indentAmount(0);
        //doc.outputSettings().prettyPrint(false);
        //System.out.println(doc.html().replaceAll("\n", ""));
        WebUtils wu = new WebUtils();
        System.out.println(wu.HTMLCompress(doc));
    }
    
    public String HTMLCompress(Document doc){
        this.doc = doc;
        return HTMLCompress();
    }
    
    public String HTMLPrettyCompress(Document doc){
        this.doc = doc;
        return HTMLPrettyCompress();
    }
    
    // Input are Documents or Node
    public void JSoupRemoveComments(Node node){
        CommentSize = 0;
        _JSoupRemoveComments(node);
    }
    
    
    private void _JSoupRemoveComments(Node node){
        int i = 0;
        
        while (i < node.childNodes().size()) {
            Node child = node.childNode(i);
            if (child.nodeName().equals("#comment")){
                CommentSize += child.toString().getBytes().length;
                child.remove();
            } else {
                JSoupRemoveComments(child);
                i++;
            }
        }
    }
}
