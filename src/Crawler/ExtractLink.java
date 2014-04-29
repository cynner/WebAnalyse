/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.ArcFilenameFilter;
import ArcFileUtils.ArcRecord;
import ArcFileUtils.ArcWriter;
import ArcFileUtils.MyRandomAccessFile;
import ArcFileUtils.WebArcReader;
import Types.MutableInt;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author wiwat
 */

/** 
 * BIN format
 * |<length>|<count>|<MaxID>|<src1>|<n1 >|<dest1-1>|<w1-1>|...|<dst1-n1>|<w1-n1>|<src2>|<n2 >|<dst2-1>|...|<dst2-n2>|<w2-n2>|...|
 * |<-long->|<-int->|<-Int->|<-Int>|<Int>|<--Int-->|<-int>|...|<--Int-->|<-Int->|<-Int>|<Int>|<-Int-->|...|<--Int-->|<-Int->|...|
 * 
 * HIST format
 * |<length>|<len_page1>|<len_site1>|<len_remain1>|<str_file1>|...|
 * |<-long->|<--long--->|<--long--->|<---long---->|<-~-~-~-~->|...|
 * 
 * ARC remain format in record
 * <URL1><Tab><Count1><newLine>
 * <URL2><Tab><Count2><newLine>
 * |...|
 * 
 * CSV format
 * <src1>;<dst1-1>:<w1-1>;<dst1-2>:<w1-2>;...<dst1-n1>:<w1-n1>;<newLine>
 * <src2>;<dst2-1>:<w2-1>;<dst2-2>:<w2-2>;...<dst2-n2>:<w2-n2>;<newLine>
 * ...
 * 
 */
public class ExtractLink {
    public static SQLiteConnection db;
    
    public static final String DEFAULT_SUFFIX_SITE = ".site";
    public static final String DEFAULT_SUFFIX_PAGE = ".page";
    public static final String DEFAULT_SUFFIX_HIST = ".hist";
    //public static final String DEFAULT_SUFFIX_BIN = ".bin";
    public static final String DEFAULT_SUFFIX_CSV = ".csv";
    public static final String DEFAULT_SUFFIX_ARC = ".arc";
    
    
    private final File OutSiteLink, OutPageLink, OutHistFile, OutRemain;
    
    private final HashSet <String> SkipList = new HashSet<>();
    
    private final SQLiteConnection dbPage;
    private final SQLiteConnection dbSite;
    
    private long posSite=0, posPage=0, posRemain=0, posHist;
    
    public ExtractLink(File dbPage, File dbSite, File OutPageLink, File OutSiteLink, File OutPageRemain, File OutHistFile){
        this.dbPage = new SQLiteConnection(dbPage);
        this.dbSite = new SQLiteConnection(dbSite);
        this.OutPageLink = OutPageLink;
        this.OutSiteLink = OutSiteLink;
        this.OutRemain = OutPageRemain;
        this.OutHistFile = OutHistFile;
    }
    
    public void loadHistFile() {
        if (OutHistFile.exists()) {
            try (MyRandomAccessFile br = new MyRandomAccessFile(OutHistFile,"r")) {
                posHist = br.readLong();
                while (br.getFilePointer() < posHist) {
                    posPage = br.readLong();
                    posSite = br.readLong();
                    posRemain = br.readLong();
                    SkipList.add(br.readUTF());
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void DirExtractAll2CSV(File Dir) throws IOException{
        HashMap <String,MutableInt> HOSTs = new HashMap<>();
        HashMap <String,MutableInt> URLs;
        MyURL src;
        String srcDomain, srcURL = "";
        
        try (MyRandomAccessFile bwPageLink = new MyRandomAccessFile(OutPageLink,"rw");
                MyRandomAccessFile bwSiteLink = new MyRandomAccessFile(OutSiteLink,"rw");
                ArcWriter awRemain = new ArcWriter(OutRemain, posRemain);
                MyRandomAccessFile bwHistFile = new MyRandomAccessFile(OutHistFile,"rw")){
            
            dbPage.openReadonly();
            dbSite.openReadonly();
            
            bwPageLink.seek(posPage);
            bwSiteLink.seek(posSite);
            bwHistFile.seek(posHist);
            
            System.out.println("===Start runExtractLinkDir===");
            
            for (File f : Dir.listFiles(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.ArcOnly))) {
                if(SkipList.contains(f.getName())){
                    continue;
                }
                System.out.println(f.getName());
                try (WebArcReader war = new WebArcReader(f, "utf-8")) {
                    srcDomain = "";
                    while (war.Next()) {
                        try {
                            srcURL = war.Record.URL;
                            src = new MyURL(srcURL);
                            if (srcDomain.isEmpty()) {
                                srcDomain = src.getHost();
                            }
                            URLs = ExtractWebPageNHostCount(src, war.Record.Doc, HOSTs);
                            WritePageLink(bwPageLink, awRemain, src, URLs);
                        } catch (Exception ex) {
                            Logger.getLogger(Utils.ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    WriteSiteLink(bwSiteLink, srcDomain, HOSTs);
                    bwHistFile.writeLong(posPage);
                    bwHistFile.writeLong(posSite);
                    bwHistFile.writeLong(posRemain);
                    bwHistFile.writeUTF(f.getName());
                    posHist = bwHistFile.getFilePointer();
                    bwHistFile.seek(0);
                    bwHistFile.writeLong(posHist);
                    bwHistFile.seek(posHist);
                } catch (IOException ex) {
                    System.err.println("Error At FILE : " + f.getName());
                    System.err.println("Error At URL  : " + srcURL);
                    Logger.getLogger(Utils.ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                }
                HOSTs.clear();
            }
            System.out.println("===runExtractLinkDir SUCCESS===");
        } catch (SQLiteException ex) {
            Logger.getLogger(Utils.ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            dbPage.dispose();
            dbSite.dispose();
        }
    }
    
    private void WritePageLink(MyRandomAccessFile bwPageLink, ArcWriter awRemain, MyURL src, HashMap<String, MutableInt> URLs) {
        String srcID, dstID;
        SQLiteStatement st;
        try {
            st = dbPage.prepare("SELECT id FROM webpage WHERE url='" + src.UniqURL.replaceAll("'", "''") + "';");
            if (st.step()) {
                String strRemain = "";
                srcID = st.columnString(0);
                st.dispose();
                bwPageLink.write(srcID.getBytes());

                for (Map.Entry<String, MutableInt> url : URLs.entrySet()) {
                    try {
                        st = dbPage.prepare("SELECT id FROM webpage WHERE url='" + url.getKey().replaceAll("'", "''") + "';");
                        if (st.step()) {
                            dstID = st.columnString(0);
                            bwPageLink.write((";" + dstID + ":" + url.getValue().value).getBytes() );
                        } else {
                            strRemain += url.getKey() + "\t" + url.getValue() + "\n";
                        }
                    } catch (SQLiteException | IOException ex) {
                        Logger.getLogger(Utils.ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        st.dispose();
                    }
                }
                if(!strRemain.isEmpty()){
                    ArcRecord ar = new ArcRecord();
                    ar.ArchiveContent = strRemain;
                    ar.ArchiveContentType = "text/plain";
                    ar.ArchiveDate = new Date();
                    ar.IPAddress = "0.0.0.0";
                    ar.URL = src.UniqURL;
                    awRemain.WriteRecord(ar);
                }
                bwPageLink.write("\n".getBytes());
                posPage = bwPageLink.getFilePointer();
                posRemain = awRemain.bw.getFilePointer();
            } else {
                st.dispose();
            }
        } catch (SQLiteException | IOException ex) {
            Logger.getLogger(Utils.ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void WriteSiteLink(MyRandomAccessFile bwSiteLink, String srcDomain, HashMap<String, MutableInt> HOSTs) {
        String srcID, dstID;
        SQLiteStatement st;
        try {
            st = dbSite.prepare("SELECT id FROM website WHERE hostname = '" + srcDomain.replaceAll("'", "''") + "';");
            if (st.step()) {
                srcID = st.columnString(0);
                st.dispose();
                bwSiteLink.write(srcID.getBytes());
                for (Map.Entry<String, MutableInt> host : HOSTs.entrySet()) {
                    try {
                        st = dbSite.prepare("SELECT id FROM website WHERE hostname = '" + host.getKey().replaceAll("'", "''") + "';");
                        if (st.step()) {
                            dstID = st.columnString(0);
                            bwSiteLink.write((";" + dstID + ":" + host.getValue().value).getBytes());
                        }
                    } catch (SQLiteException | IOException ex) {
                        Logger.getLogger(Utils.ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        st.dispose();
                    }
                }
                bwSiteLink.write("\n".getBytes());
                posSite = bwSiteLink.getFilePointer();
            } else {
                st.dispose();
            }
        } catch (SQLiteException | IOException ex) {
            Logger.getLogger(Utils.ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private HashMap<String, MutableInt> ExtractWebPageNHostCount(MyURL src, Document doc, HashMap<String,MutableInt> HOSTs){
        MyURL lnk;
        String href,host;
        MutableInt mi;
        HashMap<String,MutableInt> URLs = new HashMap<>();
        for(Element e : doc.getElementsByTag("a")){
            href = e.attr("href");
            if(href != null && !href.isEmpty()){
                try {
                    lnk = src.resolve(href);
                    mi = URLs.get(lnk.UniqURL);
                    if(mi == null){
                        URLs.put(lnk.UniqURL,new MutableInt());
                    }else{
                        mi.increment();
                    }
                    
                    host = lnk.getHost();
                    mi = HOSTs.get(host);
                    if(mi == null){
                        HOSTs.put(host,new MutableInt());
                    }else{
                        mi.increment();
                    }
                } catch (Exception ex) {
                    //System.err.println("At : " + src.UniqURL + " -> " + href);
                }
            }
        }
        return URLs;
    }
    
    /*
    private HashMap<String,Utils.ExtractLink.MutableInt> ExtractPage(MyURL src, Document doc ){
        String href;
        Utils.ExtractLink.MutableInt mi;
        HashMap<String,Utils.ExtractLink.MutableInt> URLs = new HashMap<>();
        for(Element e : doc.getElementsByTag("a")){
            href = e.attr("href");
            if(href != null && !href.isEmpty()){
                try {
                    href = src.resolve(href).UniqURL;
                    mi = URLs.get(href);
                    if(mi == null){
                        URLs.put(href,new Utils.ExtractLink.MutableInt());
                    }else{
                        mi.increment();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Utils.ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return URLs;
    }
    
    private void ExtractHostCount(MyURL src, Document doc, HashMap<String,Utils.ExtractLink.MutableInt> HOSTs){
        String href;
        Utils.ExtractLink.MutableInt mi;
        for(Element e : doc.getElementsByTag("a")){
            href = e.attr("href");
            if(href != null && !href.isEmpty()){
                try {
                    href = src.resolve(href).getHost();
                    mi = HOSTs.get(href);
                    if(mi == null){
                        HOSTs.put(href,new Utils.ExtractLink.MutableInt());
                    }else{
                        mi.increment();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Utils.ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    */
    

    public static void main(String[] args) {
        // SET PROPERTIES
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("file.encoding", "UTF-8");

        ArgumentParser parser = ArgumentParsers.newArgumentParser("Crawler.ExtractLink").defaultHelp(true)
                .description("Create or import webpage Sqlite3 database.");
        
        parser.addArgument("-o", "--out-prefix")
                .dest("out_prefix")
                .metavar("PREFIX")
                .type(String.class)
                .help("Prefix output file name")
                .required(true);
        parser.addArgument("--pagedb")
                .dest("pagedb")
                .metavar("DBPATH")
                .type(String.class)
                .setDefault(DBDriver.TableConfig.FileNameWebPageDB)
                .help("Webpage database path");
        parser.addArgument("--sitedb")
                .dest("sitedb")
                .metavar("DBPATH")
                .type(String.class)
                .setDefault(DBDriver.TableConfig.FileNameWebSiteDB)
                .help("Website database path");
        parser.addArgument("-d")
                .dest("dir")
                .metavar("DIR")
                .type(String.class)
                .setDefault(Crawler.Main.DefaultWorkingDirectory)
                .help("Working Directory for crawler");
        parser.addArgument("-e","--extract")
                .dest("extract")
                .choices("all","page","site")
                .type(String.class)
                .setDefault("all")
                .help("Select option to extract link of page, site or all");
        parser.addArgument("-ss", "--suffix-site")
                .dest("suffix_site")
                .metavar("SUFFIX")
                .type(String.class)
                .setDefault(DEFAULT_SUFFIX_SITE)
                .help("Suffix output website file name");
        parser.addArgument("-sp", "--suffix-page")
                .dest("suffix_page")
                .metavar("SUFFIX")
                .type(String.class)
                .setDefault(DEFAULT_SUFFIX_PAGE)
                .help("Suffix output webpage file name");
        parser.addArgument("-sh", "--suffix-hist")
                .dest("suffix_hist")
                .metavar("SUFFIX")
                .type(String.class)
                .setDefault(DEFAULT_SUFFIX_PAGE)
                .help("Suffix store converted history file name");
        //parser.addArgument("-sb", "--suffix-bin")
        //        .dest("suffix_bin")
        //        .metavar("SUFFIX")
        //        .type(String.class)
        //        .setDefault(DEFAULT_SUFFIX_BIN)
        //        .help("Suffix output bin");
        parser.addArgument("-sc", "--suffix-csv")
                .dest("suffix_csv")
                .metavar("SUFFIX")
                .type(String.class)
                .setDefault(DEFAULT_SUFFIX_CSV)
                .help("Suffix output csv");
        parser.addArgument("-sa", "--suffix-arc")
                .dest("suffix_arc")
                .metavar("SUFFIX")
                .type(String.class)
                .setDefault(DEFAULT_SUFFIX_ARC)
                .help("Suffix remaining link output arc");
        //parser.addArgument("-m")
        //        .dest("memory")
        //        .action(Arguments.storeTrue())
        //        .help("Load database to memory");
        parser.addArgument("TaskName")
                .nargs("?")
                .type(String.class)
                .setDefault("default")
                .help("To identify job");
        try {
            Namespace res = parser.parseArgs(args);
            String strWorkDir = res.getString("dir");
            String TaskName = res.getString("TaskName");

            CrawlerConfigList cfg = new CrawlerConfigList(TaskName, strWorkDir);

            try (MyRandomAccessFile raf = new MyRandomAccessFile(cfg.fileMergeWebInfo, "r")) {
                long length;
                String Line, cmd, cols;
                File dbFile = new File(res.getString("database"));
                db = new SQLiteConnection(dbFile);
                db.open(true);
                db.exec(DBDriver.TableConfig.CreateTableWebPageDB); //CREATE TABLE IF NOT EXISTS
                db.exec("BEGIN;");
                System.out.println("BEGIN");
                length = raf.readLong();
                cols = raf.readLine();
                while (raf.getFilePointer() < length) {
                    Line = raf.readLine();
                    cmd = "INSERT OR REPLACE INTO webpage(" + cols + ") VALUES(" + Line + ");";
                    System.out.println(Line);
                    db.exec(cmd);
                }
                System.out.println("COMMIT");
                db.exec("COMMIT;");
                System.out.println("SUCCESS");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLiteException | IOException ex) {
                Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                db.dispose();
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (IOException ex) {
            Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
