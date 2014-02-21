/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author malang
 */
public class WebArcWriter {
    
    public static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public static DateFormat webDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
    
    
    public RandomAccessFile bw;
    public String FileName;
    public String HostIP;
    
    //public BlockCompressedInputStream bcf;
    
    public WebArcWriter(File ArchiveFile, long Pos){
        try {
            bw = new RandomAccessFile(ArchiveFile, "rw");
            bw.seek(Pos);
            
        } catch (IOException ex) {}
        
    }
    
    
    public WebArcWriter(File ArchiveFile, String FileName, boolean Append, String HostIP){
        boolean FileExist = ArchiveFile.exists();
        this.FileName = FileName;
        this.HostIP = HostIP;
        try {
            bw = new RandomAccessFile(ArchiveFile, "rw");
            if(Append)
                bw.seek(bw.length());
        } catch (IOException ex) {}
        if(!Append || !FileExist){
            WriteHeaderFile();
        }
        
    }
    
    public WebArcWriter(File ArchiveFile, boolean Append){
        this(ArchiveFile, ArchiveFile.getName(), Append, "0.0.0.0");
    }

    public void WriteHeaderFile() {
        try {
            String HeaderFileContent =  "1 0 InternetArchive\n" +
                    "URL IP-address Archive-date Content-type Archive-length\n";
            bw.write(("filedesc://" + FileName + " "
                    + HostIP + " "
                    + dateFormat.format(new Date()) + " text/plain "
                    + HeaderFileContent.getBytes("utf-8").length + "\n").getBytes());
            bw.write(HeaderFileContent.getBytes());
        } catch (IOException ex) {
            
        }
    }
    
    public void WriteRecord(WebArcRecord record){
        try {
            /* Prepare Content */
            record.ContentLength = record.WebContent.getBytes("utf-8").length;
            
            /* Prepare Content Header */
            String ContentHeader = record.FirstLineContentHeader + "\n"
                //+ "Date: " + webDateFormat.format(new Date(Fetch.Date)) + "\n"
                + "Server: " + record.Server + "\n"
                + "Content-Type: " + record.WebContentType;
            if (record.LastModified != null) 
                ContentHeader += " Last-Modified: " + record.LastModified;
            ContentHeader += "\n" + "Content-Length: " + record.ContentLength + "\n";
        
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
        }
    }
    
    public void close(){
        try {
            bw.close();
        } catch (IOException ex) {
            //Logger.getLogger(ArcWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
