/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import Lexto.LuceneLexicalTH;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import projecttester.ArgUtils;

/**
 *
 * @author malang
 */
public class ArcFileConverter {
    public LuceneLexicalTH thlexical = new LuceneLexicalTH();
    
    public static String StrIn = "Thai/การพนัน";
    public static String StrOut = "TxtThai/การพนัน";
    //public static String SkipTo = "Thai/สังคม/ศาสนา/การทำนายดวงชะตา/crawl-www.payakorn.com.arc ";
    public static String StrStartAt = null;
    
    public boolean skip = true;
    public boolean AnalyseCharset = false;
    
    public ArcFileConverter(){
        
    }
    
    public ArcFileConverter(boolean AnalyseCharset, boolean skip){
        this.AnalyseCharset = AnalyseCharset;
        this.skip = skip;
    }
    
    public void WebToTextFile(File src, File dst){
        try (WebArcReader war = new WebArcReader(src, AnalyseCharset) ; ArcWriter aw = new ArcWriter(dst, false)) {
            ArcRecord ar = new ArcRecord();
            while(war.Next()){
                //doc = Jsoup.parse(war.Record.WebContent);
                //System.out.println(war.Record.WebContent);
                //System.out.println(war.Record.URL);
                ar.ArchiveContent = thlexical.strSplitContent(war.Record.Doc.text());
                ar.ArchiveDate = war.Record.ArchiveDate;
                ar.ArchiveContentType = war.Record.ArchiveContentType;
                ar.IPAddress = war.Record.IPAddress;
                ar.URL = war.Record.URL;
                aw.WriteRecord(ar);
            }
        } catch (IOException ex) {
            Logger.getLogger(ArcFileConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void WebToTextFile(File src, File dst, boolean ShowInfo){
        try (WebArcReader war = new WebArcReader(src, AnalyseCharset); ArcWriter aw= new ArcWriter(dst, false)) {

            ArcRecord ar = new ArcRecord();
            while(war.Next()){
                if(ShowInfo){
                    System.out.println(war.Record.URL);
                    System.out.println(war.Record.FirstLineContentHeader);
                    System.out.println("Server: " + war.Record.Server);
                    System.out.println("ContentType: " + war.Record.WebContentType);
                    System.out.println("Charset: " + war.Record.charset);
                    System.out.println("LastModified: " + war.Record.LastModified);
                    System.out.println("ArchiveLength: " + war.Record.ArchiveLength);
                    System.out.println("ContentLength: " + war.Record.ContentLength);
                    System.out.println("---------------------------------------");
                }
                
                //doc = Jsoup.parse(war.Record.WebContent);
                //System.out.println(war.Record.WebContent);
                ar.ArchiveContent = thlexical.strSplitContent(war.Record.Doc.text());
                ar.ArchiveDate = war.Record.ArchiveDate;
                ar.ArchiveContentType = war.Record.ArchiveContentType;
                ar.IPAddress = war.Record.IPAddress;
                ar.URL = war.Record.URL;
                aw.WriteRecord(ar);
            }
        } catch (IOException ex) {
            Logger.getLogger(ArcFileConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args){
        args = new String[]{"-i","crawldata/crawl-www.dek-d.com.arc","-o","testtxt.arc"};
        HashMap<String,String> Args = ArgUtils.Parse(args);
        ArcFileConverter afc = new ArcFileConverter();
        
        //if(!Args.get("1").equals("netbeans")){
            StrIn = Args.get("i");
            StrOut = Args.get("o");
            afc.skip = !Args.containsKey("all");
            afc.AnalyseCharset = Args.containsKey("autocharset");
            
        //}
        File f = new File(StrIn);
        if (f.isDirectory())
            afc.WebToTextDir(f, StrOut + "/");
        else 
            afc.WebToTextFile(f, new File(StrOut));
    }
    
    
    public void WebToTextDir(File Dir, String OutPath){
        File OD = new File(OutPath);
        if(!OD.isDirectory())
            OD.mkdirs();
        for(File f : Dir.listFiles()){
            System.out.println(f.getPath());
            if(f.isDirectory()){
                WebToTextDir(f, OutPath + f.getName() + "/");
            }else{
                File OutFile = new File(OutPath + f.getName());
                if(!skip || !OutFile.exists()){
                    File TmpFile = new File(OutPath + "." + f.getName());
                    WebToTextFile(f, TmpFile);
                    TmpFile.renameTo(OutFile);
                }
            }
        }
    }
}
