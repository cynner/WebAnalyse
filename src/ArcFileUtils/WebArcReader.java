/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import static ArcFileUtils.ArcRecord.dateFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;

/**
 *
 * @author malang
 */
public class WebArcReader extends ArcReader{

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
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
                    Record.ArchiveDate = dateFormat.parse(Fields[i]);
                }catch(Exception ex){
                    System.err.println("Error At: " + Record.URL);
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
    
    public static void main(String[] args){
        String FileName = args.length > 0 ? args[0] : "crawl-100kg.diaryclub.com.arc";
        File f = new File(FileName);
        try(WebArcReader war = new WebArcReader(f,"utf-8")){
            System.out.println("-------------------- HEADER --------------------");
            System.out.println("Content type: " + war.FileContentType);
            System.out.println("Date: " + war.FileDate);
            System.out.println("Desc: " + war.FileDesc);
            System.out.println("IP: " + war.FileIP);
            System.out.println("Origin: " + war.FileOrigin);
            System.out.println("Version: " + war.FileVersion);
            System.out.println("------------------------------------------------");
            System.out.println();
            
            while(war.Next()){
                System.out.println("URL: " + war.Record.URL);
                System.out.println("DATE: " + war.Record.ArchiveDate);
                System.out.println("ARC LENGTH: " + war.Record.ArchiveLength);
                System.out.println("WEB LENGTH: " + war.Record.ContentLength);
                System.out.println("CHARSET: " + war.Record.charset);
                System.out.println("SERVER: " + war.Record.Server);
                //System.out.println("JSOUP --------------------------------------");
                //System.out.println("TITLE: " + war.Record.Doc.title());
                //System.out.println("BODY: " + war.Record.Doc.body().text());
                System.out.println("--------------------------------------------");
            }
        } catch (IOException ex) {
            Logger.getLogger(WebArcReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
