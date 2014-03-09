/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ArcFileUtils;

import static ArcFileUtils.ArcRecord.dateFormat;
import static ArcFileUtils.WebArcRecord.webDateFormat;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.BlockCompressedOutputStream;

/**
 *
 * @author malang
 */
public class CompressedWebArcWriter implements AutoCloseable{
    
    
    
    public BlockCompressedOutputStream bw;
    public String FileName;
    public String HostIP="0.0.0.0";
    
    public CompressedWebArcWriter(File ArchiveFile,String FileName,String HostIP) throws IOException{
        this.FileName = FileName;
        bw = new BlockCompressedOutputStream(ArchiveFile);
        this.HostIP = HostIP;
        WriteHeaderFile();
    }

    public void WriteHeaderFile() throws IOException {
            String HeaderFileContent =  "1 0 InternetArchive\n" +
                    "URL IP-address Archive-date Content-type Archive-length\n";
            bw.write(("filedesc://" + FileName + " "
                    + HostIP + " "
                    + dateFormat.format(new Date()) + " text/plain "
                    + HeaderFileContent.getBytes("utf-8").length + "\n").getBytes());
            bw.write(HeaderFileContent.getBytes());
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
            Logger.getLogger(CompressedWebArcWriter.class.getName()).log(Level.SEVERE, null, ex);
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
            ContentHeader += "Content-Length: " + record.ContentLength + "\n";
        
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
            Logger.getLogger(CompressedWebArcWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void close() throws IOException{
        bw.close();
    }
    
    public static void main(String[] args){
        String FileIn = args.length > 0 ? args[0] : "crawl-100kg.diaryclub.com.arc";
        String FileOut = args.length >= 2 ? args[1] : FileIn + ".gz";
        try(WebArcReader war = new WebArcReader(new File(FileIn), "utf-8")){
            try(CompressedWebArcWriter caw = new CompressedWebArcWriter(new File(FileOut),FileOut.substring(0, FileOut.length() - 3),war.FileIP)){
                while(war.Next()){
                    caw.WriteRecordKeepDate(war.Record);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CompressedWebArcWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
