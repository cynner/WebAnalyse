/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import static ArcFileUtils.ArcRecord.dateFormat;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class ArcReader implements AutoCloseable{

    //public RandomAccessFile RAF;
    public final BGZFReader BGZFR;
    public File ArcFile;
    public String FileDesc;
    public String FileIP;
    public String FileDate;
    public String FileContentType;
    public long FileLength;
    public long LastPos;
    public String FileVersion;
    public String FileReserved;
    public String FileOrigin;
    public final ArcRecord Record;
    


    public ArcReader(File ArcFile) throws FileNotFoundException, IOException {
        this.ArcFile = ArcFile;
        Record = new ArcRecord();
        BGZFR = new BGZFReader(this.ArcFile);
        this.LastPos = 0;
        String Line, Content;
        String[] Fields;

        // Header
        if ((Line = BGZFR.readLine()) != null) {
            System.out.println(Line);
            Fields = Line.split(" ");
            FileDesc = Fields[0];
            FileIP = Fields[1];
            FileDate = Fields[2];
            FileContentType = Fields[3];
            FileLength = Long.parseLong(Fields[4]);

            Record.Data = null;
            Record.Data = new byte[(int) FileLength];
            BGZFR.read(Record.Data, 0, (int) FileLength);
            Content = new String(Record.Data, "utf-8");

            Fields = Content.split("\n")[0].split(" ");
            FileVersion = Fields[0];
            FileReserved = Fields[1];
            FileOrigin = Fields[2];
            this.LastPos = BGZFR.getFilePointer();
        }
    }
    
    public boolean hasNext(){
        String Line;
        String[] Fields;
        long orgPos;

        try {
            orgPos = BGZFR.getFilePointer();
            Fields = null;
            while ((Line = BGZFR.readLine()) != null) {
                Fields = Line.split(" ");
                if (Fields.length == 5) {
                    break;
                }
            }
            
            if (Line != null && Fields.length == 5) {
                BGZFR.seek(orgPos);
                return true;
            }
        }catch(IOException ex){
            Logger.getLogger(ArcReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public boolean Next() {
        String Line, Content;
        String[] Fields = null;
        //byte[] buf;
        try {
            
            //System.out.println(BGZFR.getFilePointer() + " " + BGZFR.getRealOffset());
            
            //System.out.println(BGZFR.getRealOffset() + "/" + BGZFR.length());
            while ((Line = BGZFR.readLine()) != null) {
                Fields = Line.trim().split(" ");
                //System.out.println(Line);
                //System.out.println(BGZFR.getRealOffset() + "/" + BGZFR.length());
                if (Fields.length == 5) {
                    break;
                }else if (Fields.length > 5){
                    return false;
                }
            }
            //System.out.println(Line);
            if (Line != null && Fields.length >= 5) {
                Record.URL = Fields[0];
                
                //for(i=1; i < Fields.length - 4; i++){
                //    Record.URL += " " + Fields[i];
                //}
               // System.out.println(Record.URL);
                Record.IPAddress = Fields[1];
                try{
                    Record.ArchiveDate = dateFormat.parse(Fields[2]);
                }catch(Exception ex){
                    Logger.getLogger(ArcReader.class.getName()).log(Level.SEVERE, null, ex);
                }
               
                Record.ArchiveContentType = Fields[3];
                Record.ArchiveLength = Long.parseLong(Fields[4]);
                
               // System.out.println("LEN:" + Record.ArchiveLength);
                
                if( BGZFR.getAddRealOffset(Record.ArchiveLength) > BGZFR.length())
                    return false;

                Record.Data = null;
                Record.Data = new byte[(int)Record.ArchiveLength];
                BGZFR.read(Record.Data, 0, (int)Record.ArchiveLength);
                Record.ArchiveContent = new String(Record.Data, "utf-8");
                LastPos = BGZFR.getFilePointer();
            } else {
                return false;
            }
        } catch (IOException ex) {
            Logger.getLogger(ArcReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (Exception ex){
            Logger.getLogger(ArcReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    public boolean Skip() {
        String Line;
        String[] Fields = null;
        byte[] buf;
        try {
            while ((Line = BGZFR.readLine()) != null) {
                Fields = Line.trim().split(" ");
                if (Fields.length >= 5) {
                    break;
                }
            }
            //System.out.println(Line);
            if (Fields != null && Fields.length >= 5) {
                int i;
                Record.URL = Fields[0];
                for(i=1; i < Fields.length - 4; i++){
                    Record.URL += " " + Fields[i];
                }
                Record.IPAddress = Fields[i++];
                try{
                    Record.ArchiveDate = dateFormat.parse(Fields[i]);
                }catch(Exception ex){
                    Logger.getLogger(ArcReader.class.getName()).log(Level.SEVERE, null, ex);
                }
                i++;
                Record.ArchiveContentType = Fields[i++];
                Record.ArchiveLength = Long.parseLong(Fields[i++]);
                //System.out.println( "pos " + BGZFR.getFilePointer() + "; cur " + BGZFR.getRealOffset() + "; Add " + BGZFR.getAddRealOffset(Record.ArchiveLength) +  "/" + BGZFR.length());
                if(BGZFR.getAddRealOffset(Record.ArchiveLength) > BGZFR.length())
                    return false;
                //BGZFR.seek(Record.ArchiveLength + BGZFR.getFilePointer());
                BGZFR.skip(Record.ArchiveLength);
                LastPos = BGZFR.getFilePointer();
            } else {
                return false;
            }
        } catch (IOException ex) {
            Logger.getLogger(ArcReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
    
    
    

    @Override
    public void close() throws IOException{
        BGZFR.close();
    }
    
    
    public static void main(String[] args) throws IOException {
        try (ArcReader ar = new ArcReader(new File("data/data/crawl-amphan.com.arc.bgz"))) {
            System.out.println(ar.FileOrigin);
            try {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter("data/test.arc"))) {
                    while (ar.Next()) {
                        System.out.println(ar.Record.URL);
                        bw.write(ar.Record.URL + "\n");
                        bw.write(ar.Record.ArchiveContent + "\n");
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ArcReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.println(ar.Record.ArchiveContent);
        }
    }
    
    
    
    
   
}
