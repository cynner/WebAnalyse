/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.index.Fields;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedFilePointerUtil;

/**
 *
 * @author malang
 */
public class WebArcReader {

    public BGZFReader BGZF;
    public File ArcFile;
    public String FileDesc;
    public String FileIP;
    public String FileDate;
    public String FileContentType;
    public int FileLength;
    public long LastPos;
    public String FileVersion;
    public String FileReserved;
    public String FileOrigin;
    public WebArcRecord Record = new WebArcRecord();
    public boolean AnalyseCharset ;
    public String FixedCharset = null ;
    public boolean AnalyseWebContent = true;

    public WebArcReader(File ArcFile, String FixedCharset, boolean AnalyseWebContent){
        this(ArcFile,FixedCharset);
        this.AnalyseWebContent = AnalyseWebContent;
        this.AnalyseWebContent = AnalyseWebContent;
    }
    
    public WebArcReader(File ArcFile, boolean AnalyseCharset, boolean AnalyseWebContent){
        this(ArcFile,null);
        
        this.AnalyseCharset = AnalyseCharset;
        this.AnalyseWebContent = AnalyseWebContent;
    }
    
    public WebArcReader(File ArcFile, boolean AnalyseCharset){
        this(ArcFile,null);
       
        this.AnalyseCharset = AnalyseCharset;
    }
    
    public WebArcReader(File ArcFile, String FixedCharset) {
        this.ArcFile = ArcFile;
        this.FixedCharset = FixedCharset;
         try {
            BGZF = new BGZFReader(this.ArcFile);
            String Line, Content;
            String[] Fields;

            LastPos = 0;
            // Header
            if ((Line = BGZF.readLine()) != null) {
                //System.err.println(Line);
                Fields = Line.split(" ");
                FileDesc = Fields[0];
                FileIP = Fields[1];
                FileDate = Fields[2];
                FileContentType = Fields[3];
                FileLength = Integer.parseInt(Fields[4]);

                Record.Data = null;
                Record.Data = new byte[FileLength];
                BGZF.read(Record.Data, 0, FileLength);
                Content = new String(Record.Data, "utf-8");

                Fields = Content.split("\n")[0].split(" ");
                FileVersion = Fields[0];
                FileReserved = Fields[1];
                FileOrigin = Fields[2];
                LastPos = BGZF.getRealOffset();
            }
        } catch (IOException ex) {
            System.err.println("IO err");
            ex.printStackTrace();
        }
    }

    public boolean Next() {
        int HeaderLength,ContentLength;
        String Line, Content;
        String[] Fields = null;

        try {
            while ((Line = BGZF.readLine()) != null) {
                Fields = Line.trim().split(" ");
                if (Fields.length >= 5) {
                    break;
                }
                //System.out.println("!!!"+Line);
                //System.out.println("Didn't Match offset!!!");
            }
            //System.out.println(Line);
            if (Line != null && Fields.length >= 5) {
                int i;
                Record.URL = Fields[0];
                for(i=1; i < Fields.length - 4; i++){
                    Record.URL += " " + Fields[i];
                }
                Record.IPAddress = Fields[i++];
                try{
                    Record.ArchiveDate = new Date(
                        Integer.parseInt(Fields[i].substring(0, 4)),
                        Integer.parseInt(Fields[i].substring(4, 6)),
                        Integer.parseInt(Fields[i].substring(6, 8)),
                        Integer.parseInt(Fields[i].substring(8, 10)),
                        Integer.parseInt(Fields[i].substring(10, 12)),
                        Integer.parseInt(Fields[i].substring(12, 14)));
                }catch(Exception E){
                    
                }
                i++;
                Record.ArchiveContentType = Fields[i++];
                Record.ArchiveLength = Long.parseLong(Fields[i++]);
                if(BGZF.getAddRealOffset(Record.ArchiveLength) > BGZF.length())
                    return false;

                if(AnalyseWebContent){
                    HeaderLength = Record.ParseArchiveHeader(BGZF);
                
                    Record.Data = null;
                
                
                    ContentLength = (int)(Record.ArchiveLength - HeaderLength);
                    Record.Data = new byte[ContentLength];
                    BGZF.read(Record.Data, 0, ContentLength);
                    // Skip new line
                    //RAF.skipBytes(2);
                    if(FixedCharset == null){
                        Record.AnalyseCharset();
                        if(AnalyseCharset){
                            /*
                            System.out.println(Record.ArchiveLength);
                        System.out.println(Record.FirstLineContentHeader);
                        System.out.println(Record.ArchiveContentType);
                        System.out.println(Record.ArchiveDate);
                        System.out.println(Record.charset);
                        System.out.println(Record.ContentLength);
                        */

                            //Record.WebContent = (new String(Data, Record.Charset));
                            WebUtils.AnalyseCharsetFromData(Record);
                        }else{
                            Record.WebContent = new String(Record.Data, "utf-8");
                            Record.Doc = Jsoup.parse(Record.WebContent);
                        }
                    }else{
                        Record.charset = FixedCharset;
                        Record.WebContent = new String(Record.Data, FixedCharset);
                        Record.Doc = Jsoup.parse(Record.WebContent);
                    }
                }else{
                    BGZF.skip((int)Record.ArchiveLength);
                }
                LastPos = BGZF.getRealOffset();
            } else {
                return false;
            }
        } catch (IOException e) {
            System.out.println(Record.URL);
            return false;
            
        }
        return true;
    }


    public void close() {
        try {
            BGZF.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public boolean Skip() {
        String Line;
        String[] Fields = null;
        byte[] buf;
        try {
            while ((Line = BGZF.readLine()) != null) {
                Fields = Line.trim().split(" ");
                if (Fields.length >= 5) {
                    break;
                }
            }
            //System.out.println(Line);
            if (Line != null && Fields.length >= 5) {
                int i;
                Record.URL = Fields[0];
                for(i=1; i < Fields.length - 4; i++){
                    Record.URL += " " + Fields[i];
                }
                Record.IPAddress = Fields[i++];
                try{
                    Record.ArchiveDate = new Date(
                        Integer.parseInt(Fields[i].substring(0, 4)),
                        Integer.parseInt(Fields[i].substring(4, 6)),
                        Integer.parseInt(Fields[i].substring(6, 8)),
                        Integer.parseInt(Fields[i].substring(8, 10)),
                        Integer.parseInt(Fields[i].substring(10, 12)),
                        Integer.parseInt(Fields[i].substring(12, 14)));
                }catch(Exception E){
                    
                }
                i++;
                Record.ArchiveContentType = Fields[i++];
                Record.ArchiveLength = Long.parseLong(Fields[i++]);
                //System.out.println( "pos " + BGZF.getFilePointer() + "; cur " + BGZF.getRealOffset() + "; Add " + BGZF.getAddRealOffset(Record.ArchiveLength) +  "/" + BGZF.length());
                if(BGZF.getAddRealOffset(Record.ArchiveLength) > BGZF.length())
                    return false;
                //BGZFR.seek(Record.ArchiveLength + BGZFR.getFilePointer());
                BGZF.skip(Record.ArchiveLength);
                LastPos = BGZF.getFilePointer();
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
