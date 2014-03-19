/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;

/**
 *
 * @author malang
 */
public class WebArcRecord extends ArcRecord{
    
    public static DateFormat webDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.getDefault());
    
    public static int NewLineLength = "\n".getBytes().length;
    
    /* ---------- Content Header ---------- */
    public String FirstLineContentHeader;
    //public Date FetchDate;
    public String Server;
    public String WebContentType;
    public long ServerTime;
    public long LastModified;
    public long ContentLength;
    
    /* ---------- Web Content ---------- */
    public String WebContent;
    
    /* ---------- Content Addition ---------- */
    public String charset;
    public Document Doc;
    
    //@Override
    //private String ArchiveContent;
    
    public WebArcRecord(){
        
    }
    
    public WebArcRecord(ArcRecord Orig){
        //this.ArchiveContent = Orig.ArchiveContent;
        this.ArchiveDate = Orig.ArchiveDate;
        this.ArchiveLength = Orig.ArchiveLength;
        this.ArchiveContentType = Orig.ArchiveContentType;
        this.IPAddress = Orig.IPAddress;
        this.URL = Orig.URL;
        this.ParseArchiveHeaderByContent(Orig.ArchiveContent);
    }
    
    public void AnalyseCharset(){
        if(this.WebContentType != null)
            this.charset = WebUtils.CharsetFromWebContentType(this.WebContentType);
        else
            this.charset = "utf-8";
    }
    
    public int ParseArchiveHeader(BGZFReader BGZF){
        int beg,end,tmpbeg,tmpend, HeaderLength;
        boolean End = false;
        String FieldName, Value, Line, tmp;
        
        FirstLineContentHeader = null;
        Server = null;
        LastModified = 0;
        ServerTime = 0;
        WebContentType = null;
        ContentLength = -1;
        HeaderLength = 0;
        
        
        try{
            FirstLineContentHeader = BGZF.readLine();
            HeaderLength += FirstLineContentHeader.getBytes("utf-8").length + NewLineLength;
            while (!End) {
                beg = 0;
                Line = BGZF.readLine();
                HeaderLength += Line.getBytes("utf-8").length + NewLineLength;
                end = Line.indexOf(':');
                FieldName = Line.substring(beg, end);
                beg = end + 1;
                Value = Line.substring(beg);

                if (FieldName.equalsIgnoreCase("Server")) {
                    Server = Value.trim();
                } else if (FieldName.equalsIgnoreCase("Date")) {
                    tmp = Value.trim();
                    if (!tmp.equalsIgnoreCase("null")) {
                        try {
                            ServerTime = webDateFormat.parse(tmp).getTime();
                        } catch (ParseException ex) {
                            System.err.println("Error At: " + URL);
                            Logger.getLogger(WebArcRecord.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                 } else if (FieldName.equalsIgnoreCase("Last-Modified")) {
                    tmp = Value.trim();
                    if (!tmp.equalsIgnoreCase("null")) {
                        try {
                            LastModified = webDateFormat.parse(tmp).getTime();
                        } catch (ParseException ex) {
                            System.err.println("Error At: " + URL);
                            Logger.getLogger(WebArcRecord.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (FieldName.equalsIgnoreCase("Content-Type")) {
                    WebContentType = Value.trim();
                } else if (FieldName.equalsIgnoreCase("Content-Length")) {
                    ContentLength = Long.parseLong(Value.trim());
                    End = true;
                }
            }
        }catch(Exception ex){
            System.err.println("Error At: " + URL);
            Logger.getLogger(WebArcRecord.class.getName()).log(Level.SEVERE, null, ex);
        }
        return HeaderLength;
    }
    
    
    public void ParseArchiveHeaderByContent(String ArchiveContent){
        int beg=0,end,tmpbeg,tmpend;
        boolean End = false;
        String FieldName, Value, tmp;
        
        FirstLineContentHeader = null;
        Server = null;
        LastModified = 0;
        ServerTime = 0;
        WebContentType = null;
        ContentLength = -1;
        
        end = ArchiveContent.indexOf('\n');
        
        try{
            FirstLineContentHeader = ArchiveContent.substring(beg, end);

            while (!End) {
                beg = end + 1;
                end = ArchiveContent.indexOf(':', beg);
                FieldName = ArchiveContent.substring(beg, end);
                beg = end + 1;
                end = ArchiveContent.indexOf('\n', beg);
                Value = ArchiveContent.substring(beg, end);

                if (FieldName.equalsIgnoreCase("Server")) {
                    Server = Value.trim();
                } else if (FieldName.equalsIgnoreCase("Date")) {
                    tmp = Value.trim();
                    if (!tmp.equalsIgnoreCase("null")) {
                        try {
                            ServerTime = webDateFormat.parse(tmp).getTime();
                        } catch (ParseException ex) {
                            System.err.println("Error At: " + URL);
                            Logger.getLogger(WebArcRecord.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (FieldName.equalsIgnoreCase("Last-Modified")) {
                    tmp = Value.trim();
                    if (!tmp.equalsIgnoreCase("null")) {
                        try {
                            LastModified = webDateFormat.parse(tmp).getTime();
                        } catch (ParseException ex) {
                            System.err.println("Error At: " + URL);
                            Logger.getLogger(WebArcRecord.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (FieldName.equalsIgnoreCase("Content-Type")) {
                    WebContentType = Value.trim();
                } else if (FieldName.equalsIgnoreCase("Content-Length")) {
                    ContentLength = Long.parseLong(Value.trim());
                    End = true;
                }
            }
            beg = end + 1;
            WebContent = ArchiveContent.substring(beg);
            //return new WebArcRecord(this);
        }catch(Exception ex){
            System.err.println("Error At: " + URL);
            Logger.getLogger(WebArcRecord.class.getName()).log(Level.SEVERE, null, ex);
            WebContent = ArchiveContent;
        }
    }
    
    
}
