/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import static ArcFileUtils.ArcRecord.dateFormat;
import static ArcFileUtils.WebArcRecord.webDateFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class WebArcWriter extends ArcWriter{
    
    //public BlockCompressedInputStream bcf;
    
    public WebArcWriter(File ArchiveFile, long Pos) throws FileNotFoundException, IOException{
        super(ArchiveFile, Pos);
    }
    
    public WebArcWriter(File ArchiveFile, String FileName, boolean Append, String HostIP) throws FileNotFoundException, IOException{
        super(ArchiveFile, FileName, Append, HostIP);
    }
    
    public WebArcWriter(File ArchiveFile, boolean Append) throws FileNotFoundException, IOException{
        super(ArchiveFile, Append);
    }

    public void WriteRecord(WebArcRecord record){
        try {
            /* Prepare Content */
            record.ContentLength = record.WebContent.getBytes("utf-8").length;
            
            /* Prepare Content Header */
            String ContentHeader = record.FirstLineContentHeader + "\n"
                + "Date: " + (record.ServerTime != 0 ? 
                    webDateFormat.format(new Date(record.ServerTime)) : null ) + "\n"
                + "Server: " + record.Server + "\n"
                + "Content-Type: " + record.WebContentType + "\n" + (record.LastModified != 0 ?
                    "Last-Modified: " + webDateFormat.format(new Date(record.LastModified)) + "\n" : "");
            ContentHeader += "Content-Length: " + record.ContentLength + "\n";
        
            /* Prepare Record Header */
            record.ArchiveLength = ContentHeader.getBytes("utf-8").length + record.ContentLength;
            
            /* Write Record Header */
            bw.write(("\n" + record.URL + " "
                        + record.IPAddress + " "
                        + dateFormat.format(new Date()) + " "
                        + record.ArchiveContentType + " " // DEFAULT "text/html"
                        + record.ArchiveLength + "\n").getBytes());
            
            /* Write Content Header */
            bw.write(ContentHeader.getBytes());
            
            /* Write Content */
            bw.write(record.WebContent.getBytes());
            
            
        } catch (IOException ex) {
            Logger.getLogger(WebArcWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void WriteRecordKeepDate(WebArcRecord record){
        try {
            /* Prepare Content */
            record.ContentLength = record.WebContent.getBytes("utf-8").length;
            
            /* Prepare Content Header */
            String ContentHeader = record.FirstLineContentHeader + "\n"
                + "Date: " + (record.ServerTime != 0 ? 
                    webDateFormat.format(new Date(record.ServerTime)) : null ) + "\n"
                + "Server: " + record.Server + "\n"
                + "Content-Type: " + record.WebContentType + "\n" + (record.LastModified != 0 ?
                    "Last-Modified: " + webDateFormat.format(new Date(record.LastModified)) + "\n" : "");
            ContentHeader += "\n" + "Content-Length: " + record.ContentLength + "\n";
        
            /* Prepare Record Header */
            record.ArchiveLength = ContentHeader.getBytes("utf-8").length + record.ContentLength;
            
            /* Write Record Header */
            bw.write(("\n" + record.URL + " "
                        + record.IPAddress + " "
                        + dateFormat.format(record.ArchiveDate) + " "
                        + record.ArchiveContentType + " " // DEFAULT "text/html"
                        + record.ArchiveLength + "\n").getBytes());
            
            /* Write Content Header */
            bw.write(ContentHeader.getBytes());
            
            /* Write Content */
            bw.write(record.WebContent.getBytes());
            
            
        } catch (IOException ex) {
            Logger.getLogger(WebArcWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
