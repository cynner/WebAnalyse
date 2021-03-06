/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ArcFileUtils;

import static ArcFileUtils.ArcRecord.dateFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.BlockCompressedOutputStream;

/**
 *
 * @author malang
 */
public class CompressedArcWriter implements AutoCloseable{
    
    
    public BlockCompressedOutputStream bw;
    public String FileName;
    public String HostIP="0.0.0.0";
    
    public CompressedArcWriter(File ArchiveFile) throws IOException{
        FileName = ArchiveFile.getName();
        bw = new BlockCompressedOutputStream(ArchiveFile);
        WriteHeaderFile();
    }
    
    public CompressedArcWriter(File ArchiveFile, String FileName, String HostIP) throws FileNotFoundException, IOException{
        this.FileName = FileName;
        this.HostIP = HostIP;
        bw = new BlockCompressedOutputStream(ArchiveFile);
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
            Logger.getLogger(CompressedArcWriter.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CompressedArcWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void close() throws IOException{
        bw.close();
    }
    
}
