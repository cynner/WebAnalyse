/*
 * To change this template, choose Tools | Templates
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

/**
 *
 * @author malang
 */
public class ArcWriter implements AutoCloseable{
    
    public final RandomAccessFile bw;
    public String FileName;
    public String HostIP="0.0.0.0";
    
    
    public ArcWriter(File ArchiveFile, long pos) throws FileNotFoundException, IOException{
        FileName = ArchiveFile.getName();
        bw = new RandomAccessFile(ArchiveFile, "rw");
        if(pos > 0){
            bw.seek(pos);
        } else {
            WriteHeaderFile();
        }
    }
    
    public ArcWriter(File ArchiveFile, boolean Append) throws FileNotFoundException, IOException{
        FileName = ArchiveFile.getName();
        bw = new RandomAccessFile(ArchiveFile, "rw");
        if(Append){
            bw.seek(bw.length());
        }
        if(!Append || !ArchiveFile.exists()){
            WriteHeaderFile();
        }
    }
    
    public ArcWriter(File ArchiveFile, String FileName, boolean Append, String HostIP) throws FileNotFoundException, IOException{
        boolean FileExist = ArchiveFile.exists();
        this.FileName = FileName;
        this.HostIP = HostIP;
        bw = new RandomAccessFile(ArchiveFile, "rw");
        if(Append)
            bw.seek(bw.length());
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
            Logger.getLogger(ArcWriter.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ArcWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void close() throws IOException{
        bw.close();
    }
    
}
