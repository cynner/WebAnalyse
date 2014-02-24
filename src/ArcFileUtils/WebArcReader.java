/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;

/**
 *
 * @author malang
 */
public class WebArcReader extends ArcReader{

    public final WebArcRecord Record;
    public boolean AnalyseCharset ;
    public String FixedCharset = null ;
    public boolean AnalyseWebContent = true;

    public WebArcReader(File ArcFile, String FixedCharset, boolean AnalyseWebContent) throws FileNotFoundException, IOException{
        this(ArcFile,FixedCharset);
        this.AnalyseWebContent = AnalyseWebContent;
        this.AnalyseWebContent = AnalyseWebContent;
    }
    
    public WebArcReader(File ArcFile, boolean AnalyseCharset, boolean AnalyseWebContent) throws FileNotFoundException, IOException{
        this(ArcFile,null);
        this.AnalyseCharset = AnalyseCharset;
        this.AnalyseWebContent = AnalyseWebContent;
    }
    
    public WebArcReader(File ArcFile, boolean AnalyseCharset) throws FileNotFoundException, IOException{
        this(ArcFile,null);
        this.AnalyseCharset = AnalyseCharset;
    }
    
    public WebArcReader(File ArcFile, String FixedCharset) throws FileNotFoundException, IOException {
        super(ArcFile);
        this.Record = new WebArcRecord();
        this.FixedCharset = FixedCharset;
    }

    @Override
    public boolean Next() {
        int HeaderLength,ContentLength;
        String Line, Content;
        String[] Fields = null;

        try {
            while ((Line = BGZFR.readLine()) != null) {
                Fields = Line.trim().split(" ");
                if (Fields.length >= 5) {
                    break;
                }
                //System.out.println("!!!"+Line);
                //System.out.println("Didn't Match offset!!!");
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
                    cal.set(
                        Integer.parseInt(Fields[i].substring(0, 4)),
                        Integer.parseInt(Fields[i].substring(4, 6)),
                        Integer.parseInt(Fields[i].substring(6, 8)),
                        Integer.parseInt(Fields[i].substring(8, 10)),
                        Integer.parseInt(Fields[i].substring(10, 12)),
                        Integer.parseInt(Fields[i].substring(12, 14)));
                    Record.ArchiveDate = cal.getTime();
                }catch(Exception ex){
                    Logger.getLogger(WebArcReader.class.getName()).log(Level.SEVERE, null, ex);
                }
                i++;
                Record.ArchiveContentType = Fields[i++];
                Record.ArchiveLength = Long.parseLong(Fields[i++]);
                if(BGZFR.getAddRealOffset(Record.ArchiveLength) > BGZFR.length())
                    return false;

                if(AnalyseWebContent){
                    HeaderLength = Record.ParseArchiveHeader(BGZFR);
                
                    Record.Data = null;
                
                
                    ContentLength = (int)(Record.ArchiveLength - HeaderLength);
                    Record.Data = new byte[ContentLength];
                    BGZFR.read(Record.Data, 0, ContentLength);
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
                    BGZFR.skip((int)Record.ArchiveLength);
                }
                LastPos = BGZFR.getRealOffset();
            } else {
                return false;
            }
        } catch (IOException ex) {
            Logger.getLogger(WebArcReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            
        }
        return true;
    }
}
