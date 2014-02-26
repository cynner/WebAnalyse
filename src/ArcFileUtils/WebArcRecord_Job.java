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
public class WebArcRecord_Job extends WebArcRecord{
    
    
    //@Override
    //private String ArchiveContent;
    
    public WebArcRecord_Job(){
        
    }
    
    public WebArcRecord_Job(ArcRecord Orig){
        //this.ArchiveContent = Orig.ArchiveContent;
        this.ArchiveDate = Orig.ArchiveDate;
        this.ArchiveLength = Orig.ArchiveLength;
        this.ArchiveContentType = Orig.ArchiveContentType;
        this.IPAddress = Orig.IPAddress;
        this.URL = Orig.URL;
        this.ParseArchiveHeaderByContent(Orig.ArchiveContent);
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
                            Logger.getLogger(WebArcRecord_Job.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (FieldName.equalsIgnoreCase("Content-Type")) {
                    tmpbeg = Value.indexOf(':');
                    if (tmpbeg >= 0) {
                        tmpend = Value.lastIndexOf(' ', tmpbeg) + 1;
                        if (Value.substring(tmpend, tmpbeg).equalsIgnoreCase("Last-Modified")) {
                            tmp = Value.substring(tmpbeg + 1).trim();
                            if(!tmp.equalsIgnoreCase("null")){
                                
                                tmpbeg = tmp.indexOf(':');
                                if (tmpbeg >= 0) {
                                    if (tmp.substring(0, tmpbeg).equalsIgnoreCase("Last-Modified")) {
                                        tmp = tmp.substring(tmpbeg + 1).trim();
                                    }
                                }
                                try{
                                    LastModified = webDateFormat.parse(tmp).getTime();
                                }catch(ParseException ex){
                                    Logger.getLogger(WebArcRecord_Job.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
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
            Logger.getLogger(WebArcRecord_Job.class.getName()).log(Level.SEVERE, null, ex);
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
                            Logger.getLogger(WebArcRecord_Job.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else if (FieldName.equalsIgnoreCase("Content-Type")) {
                    tmpbeg = Value.indexOf(':');
                    if (tmpbeg >= 0) {
                        tmpend = Value.lastIndexOf(' ', tmpbeg) + 1;
                        if (Value.substring(tmpend, tmpbeg).equalsIgnoreCase("Last-Modified")) {
                            tmp = Value.substring(tmpbeg + 1).trim();
                            if(!tmp.equalsIgnoreCase("null")){
                                try{
                                    LastModified = webDateFormat.parse(tmp).getTime();
                                }catch(ParseException ex){
                                    Logger.getLogger(WebArcRecord_Job.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
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
            Logger.getLogger(WebArcRecord_Job.class.getName()).log(Level.SEVERE, null, ex);
            WebContent = ArchiveContent;
        }
    }
    
    
}
