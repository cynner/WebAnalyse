/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Converter;

import ArcFileUtils.ArcFilenameFilter;
import ArcFileUtils.WebArcReader_Job;
import ArcFileUtils.WebArcWriter;
import ArcFileUtils.WebUtils;
import Crawler.MyURL;
import DBDriver.TableConfig;
//import LanguageUtils.LanguageDetector;
import com.almworks.sqlite4java.SQLiteConnection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 *
 *
 */
public class ConvertOld2NewARC {

    public static BufferedWriter bwWeb;
    public static String WebDBName = TableConfig.FileNameWebPageDB;
    public static String SiteDBName = TableConfig.FileNameWebSiteDB;
    public static WebUtils wu = new WebUtils();
    public static SQLiteConnection dbweb,dbsite;

    public static void main(String[] args) throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        dbweb = new SQLiteConnection(new File(WebDBName));
        dbsite = new SQLiteConnection(new File(SiteDBName));
        //String val,lang;
        //int pagecount;
        try {
            dbweb.open();
            dbsite.open();

            File InDir = args.length > 0 ? new File(args[0]) : new File("data/arc/");
            File OutDir = args.length > 1 ? new File(args[1]) : new File("data/converted/");

            if (!InDir.exists()) {
                System.err.println("No such directory -> " + args[0]);
                System.exit(1);
            }

            if (!OutDir.exists()) {
                OutDir.mkdir();
            }

            String Filename;
            File OutArcBGZF;
            for (File f : (new File(args[0])).listFiles(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.ArcOnly))) {

                // bwWeb = new BufferedWriter(new FileWriter("data/newcrawl/.db." + f.getName()));
                Filename = f.getName().replaceAll("-2013\\d{10}-00000.arc", ".arc");
                OutArcBGZF = new File(args[1] + Filename + ".gz");
                System.out.println(f.getName());
                if(!OutArcBGZF.exists()){
                try (WebArcReader_Job war = new WebArcReader_Job(f, true);
                        WebArcWriter waw = new WebArcWriter(OutArcBGZF, Filename, false, war.FileIP)) {
                    //dbweb.exec("BEGIN;");
                    //pagecount = 0;
                    while (war.Next()) {
                        war.Record.WebContent = wu.HTMLCompress(war.Record.Doc);
                        try {
                            war.Record.URL = (new MyURL(war.Record.URL)).UniqURL;
                        } catch (Exception ex) {
                            Logger.getLogger(ConvertOld2NewARC.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        //lang = LanguageDetector.Detect(war.Record.Doc.text());
                        //val = "\"" + war.Record.URL.replaceAll("\"", "\"\"") + "\"," + (lang == null ? "null" : "\"" + lang + "\"") + "," + wu.FileSize + "," + wu.CommentSize + "," + wu.ScriptSize + "," + wu.StyleSize + "," + wu.ContentSize;
                        waw.WriteRecordKeepDate(war.Record);
                        //dbweb.exec("INSERT OR IGNORE INTO webpage(url,language,file_size,comment_size,js_size,style_size,content_size) VALUES(" + val + ");");
                        //pagecount++;
                    }
                    //dbweb.exec("COMMIT;");
                    //dbsite.exec("UPDATE website SET page_count=" + PageCount + " WHERE hostname='"+HostName+"';");
                }
                }
                // bwWeb.close();

            }
        } catch (Exception e) {

        } finally {
            dbweb.dispose();
            dbsite.dispose();
        }
        
    }
}
