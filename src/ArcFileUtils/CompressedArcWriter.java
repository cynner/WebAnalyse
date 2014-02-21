/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ArcFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.sf.samtools.util.BlockCompressedOutputStream;

/**
 *
 * @author malang
 */
public class CompressedArcWriter {
    public static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    public static DateFormat webDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
    
    
    public BlockCompressedOutputStream bw;
    public String FileName;
    public String HostIP="0.0.0.0";
    
    public CompressedArcWriter(File ArchiveFile){
        boolean FileExist = ArchiveFile.exists();
        FileName = ArchiveFile.getName();
        bw = new BlockCompressedOutputStream(ArchiveFile);
        WriteHeaderFile();
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
    
    
    public void WriteRecordFromData(ArcRecord record){
        try {
            /* Prepare Record Header */
            //if(record.Data == null)
            //    record.Data = record.ArchiveContent.getBytes("utf-8");
            record.ArchiveLength = record.Data.length;
            
            /* Write Record Header */
            bw.write(("\n" + record.URL + " "
                        + record.IPAddress + " "
                        + dateFormat.format(record.ArchiveDate) + " "
                        + record.ArchiveContentType + " " // DEFAULT "text/html"
                        + record.ArchiveLength + "\n").getBytes());
            
            /* Write Content */
            bw.write(record.Data);
            
            
        } catch (IOException ex) {
        }
    }
    
     public void WriteRecordFromContent(ArcRecord record){
        try {
            /* Prepare Record Header */

            record.Data = record.ArchiveContent.getBytes("utf-8");
            record.ArchiveLength = record.Data.length;
            
            /* Write Record Header */
            bw.write(("\n" + record.URL + " "
                        + record.IPAddress + " "
                        + dateFormat.format(record.ArchiveDate) + " "
                        + record.ArchiveContentType + " " // DEFAULT "text/html"
                        + record.ArchiveLength + "\n").getBytes());
            
            /* Write Content */
            bw.write(record.Data);
            
            
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
