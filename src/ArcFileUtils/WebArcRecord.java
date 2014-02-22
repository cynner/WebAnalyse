/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;

/**
 *
 * @author malang
 */
public class WebArcRecord extends ArcRecord{
    
    public static int NewLineLength = "\n".getBytes().length;
    
    /* ---------- Content Header ---------- */
    public String FirstLineContentHeader;
    //public Date FetchDate;
    public String Server;
    public String WebContentType;
    public String LastModified;
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
        this.ParseArchiveContent(Orig.ArchiveContent);
    }
    
    public void AnalyseCharset(){
        if(this.WebContentType != null)
            this.charset = WebUtils.CharsetFromWebContentType(this.WebContentType);
        else
            this.charset = "utf-8";
    }
    
    public int ParseArchiveHeader(BGZFReader BGZF){
        int beg=0,end,tmpbeg,tmpend, HeaderLength;
        boolean End = false;
        String FieldName, Value, Line;
        
        FirstLineContentHeader = null;
        Server = null;
        LastModified = null;
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
                } else if (FieldName.equalsIgnoreCase("Content-Type")) {
                    tmpbeg = Value.indexOf(':');
                    if (tmpbeg >= 0) {
                        tmpend = Value.lastIndexOf(' ', tmpbeg) + 1;
                        if (Value.substring(tmpend, tmpbeg).equalsIgnoreCase("Last-Modified")) {
                            LastModified = Value.substring(tmpbeg + 1).trim();
                        }
                        WebContentType = Value.substring(0, tmpend).trim();
                    } else {
                        WebContentType = Value.trim();
                    }
                } else if (FieldName.equalsIgnoreCase("Content-Length")) {
                    ContentLength = Long.parseLong(Value.trim());
                    End = true;
                }
            }
        }catch(Exception ex){
            Logger.getLogger(WebArcRecord.class.getName()).log(Level.SEVERE, null, ex);
        }
        return HeaderLength;
    }
    
    public void ParseArchiveContent(String ArchiveContent){
        int beg=0,end,tmpbeg,tmpend;
        boolean End = false;
        String FieldName, Value;
        
        FirstLineContentHeader = null;
        Server = null;
        LastModified = null;
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
                } else if (FieldName.equalsIgnoreCase("Content-Type")) {
                    tmpbeg = Value.indexOf(':');
                    if (tmpbeg >= 0) {
                        tmpend = Value.lastIndexOf(' ', tmpbeg) + 1;
                        if (Value.substring(tmpend, tmpbeg).equalsIgnoreCase("Last-Modified")) {
                            LastModified = Value.substring(tmpbeg + 1).trim();
                        }
                        WebContentType = Value.substring(0, tmpend).trim();
                    } else {
                        WebContentType = Value.trim();
                    }
                } else if (FieldName.equalsIgnoreCase("Content-Length")) {
                    ContentLength = Long.parseLong(Value.trim());
                    End = true;
                }
            }
            beg = end + 1;
            WebContent = ArchiveContent.substring(beg);
            //return new WebArcRecord(this);
        }catch(Exception ex){
            Logger.getLogger(WebArcRecord.class.getName()).log(Level.SEVERE, null, ex);
            WebContent = ArchiveContent;
        }
    }
    
}
