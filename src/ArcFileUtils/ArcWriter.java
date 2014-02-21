/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author malang
 */
public class ArcWriter {
    
    public static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public static DateFormat webDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
    
    
    public RandomAccessFile bw;
    public String FileName;
    public String HostIP="0.0.0.0";
    
    
    public ArcWriter(File ArchiveFile, long pos){
        boolean FileExist = ArchiveFile.exists();
        FileName = ArchiveFile.getName();
        try {
            bw = new RandomAccessFile(ArchiveFile, "rw");
            bw.seek(pos);
        } catch (IOException ex) {}
    }
    
    public ArcWriter(File ArchiveFile, boolean Append){
        boolean FileExist = ArchiveFile.exists();
        FileName = ArchiveFile.getName();
        try {
            bw = new RandomAccessFile(ArchiveFile, "rw");
            if(Append){
                bw.seek(bw.length());
            }
        } catch (IOException ex) {}
        if(!Append || !FileExist){
            WriteHeaderFile();
        }
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
    
    
    public void WriteRecord(ArcRecord record){
        try {
            /* Prepare Record Header */
            byte[] bcontent = record.ArchiveContent.getBytes("utf-8");
            record.ArchiveLength = bcontent.length;
            
            /* Write Record Header */
            bw.write(("\n" + record.URL + " "
                        + record.IPAddress + " "
                        + dateFormat.format(record.ArchiveDate) + " "
                        + record.ArchiveContentType + " " // DEFAULT "text/html"
                        + record.ArchiveLength + "\n").getBytes());
            
            /* Write Content */
            bw.write(bcontent);
            
            
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
