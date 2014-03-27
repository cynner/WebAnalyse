/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Utils;

import ArcFileUtils.ArcFilenameFilter;
import ArcFileUtils.WebArcReader;
import Crawler.MyURL;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author wiwat
 */
public class ExtractLink {
    
    class MutableInt {
        public int value = 1; // note that we start at 1 since we're counting
        public void increment () { ++value;      }
    }
    
    
    private final File OutHostLink, OutWebLink;
            
    private BufferedWriter bwWebLink;
    private BufferedWriter bwHostLink;
    
    private final SQLiteConnection dbWeb = new SQLiteConnection(DBDriver.TableConfig.FileWebPageDB);
    private final SQLiteConnection dbHost= new SQLiteConnection(DBDriver.TableConfig.FileWebSiteDB);
    
    public ExtractLink(File OutHostLink, File OutWebLink){
        this.OutWebLink = OutWebLink;
        this.OutHostLink = OutHostLink;
    }
    
    public void runExtractLinkDir(File Dir,String fileskipto) throws IOException{
        boolean skipfordebug = (fileskipto != null);
        HashMap <String,MutableInt> HOSTs = new HashMap<>();
        HashMap <String,MutableInt> URLs;
        MyURL src;
        String srcDomain,srcURL = "";
        bwWebLink = new BufferedWriter(new FileWriter(OutWebLink));
        bwHostLink = new BufferedWriter(new FileWriter(OutHostLink));
        
        try {
            dbWeb.openReadonly();
            dbHost.openReadonly();
            
            System.out.println("===Start runExtractLinkDir===");

            for (File f : Dir.listFiles(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.ArcOnly))) {
                if(skipfordebug){
                    if(f.getName().equals(fileskipto)){
                        skipfordebug = false;
                    }else{
                        continue;
                    }
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
                            WriteWebLink(src, URLs);
                        } catch (Exception ex) {
                            Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    WriteHostLink(srcDomain, HOSTs);
                    bwHostLink.flush();
                    bwWebLink.flush();
                } catch (IOException ex) {
                    System.err.println("Error At FILE : " + f.getName());
                    System.err.println("Error At URL  : " + srcURL);
                    Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                }
                HOSTs.clear();
            }
            System.out.println("===runExtractLinkDir SUCCESS===");
        } catch (SQLiteException ex) {
            Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            dbWeb.dispose();
            dbHost.dispose();
        }
        bwWebLink.close();
        bwHostLink.close();
    }
    
    private void WriteWebLink(MyURL src, HashMap<String, MutableInt> URLs) {
        String srcID, dstID;
        SQLiteStatement st;
        try {
            st = dbWeb.prepare("SELECT id FROM webpage WHERE url='" + src.UniqURL.replaceAll("'", "''") + "';");
            if (st.step()) {
                srcID = st.columnString(0);
                st.dispose();
                bwWebLink.write(srcID);

                for (Map.Entry<String, MutableInt> url : URLs.entrySet()) {
                    try {
                        st = dbWeb.prepare("SELECT id FROM webpage WHERE url='" + url.getKey().replaceAll("'", "''") + "';");
                        if (st.step()) {
                            dstID = st.columnString(0);
                            bwWebLink.write(";" + dstID + ":" + url.getValue().value );
                        }
                    } catch (SQLiteException | IOException ex) {
                        Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        st.dispose();
                    }
                }
                bwWebLink.write("\n");
            } else {
                st.dispose();
            }
        } catch (SQLiteException | IOException ex) {
            Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void WriteHostLink(String srcDomain, HashMap<String, MutableInt> HOSTs) {
        String srcID, dstID;
        SQLiteStatement st;
        try {
            st = dbHost.prepare("SELECT id FROM website WHERE hostname = '" + srcDomain.replaceAll("'", "''") + "';");
            if (st.step()) {
                srcID = st.columnString(0);
                st.dispose();
                bwHostLink.write(srcID);
                for (Map.Entry<String, MutableInt> host : HOSTs.entrySet()) {
                    try {
                        st = dbHost.prepare("SELECT id FROM website WHERE hostname = '" + host.getKey().replaceAll("'", "''") + "';");
                        if (st.step()) {
                            dstID = st.columnString(0);
                            bwHostLink.write(";" + dstID + ":" + host.getValue().value);
                        }
                    } catch (SQLiteException | IOException ex) {
                        Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        st.dispose();
                    }
                }
                bwHostLink.write("\n");
            } else {
                st.dispose();
            }
        } catch (SQLiteException | IOException ex) {
            Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private HashMap<String,MutableInt> ExtractWebPageNHostCount(MyURL src, Document doc, HashMap<String,MutableInt> HOSTs){
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
    
    private HashMap<String,MutableInt> ExtractWebPage(MyURL src, Document doc ){
        String href;
        MutableInt mi;
        HashMap<String,MutableInt> URLs = new HashMap<>();
        for(Element e : doc.getElementsByTag("a")){
            href = e.attr("href");
            if(href != null && !href.isEmpty()){
                try {
                    href = src.resolve(href).UniqURL;
                    mi = URLs.get(href);
                    if(mi == null){
                        URLs.put(href,new MutableInt());
                    }else{
                        mi.increment();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return URLs;
    }
    
    private void ExtractHostCount(MyURL src, Document doc, HashMap<String,MutableInt> HOSTs){
        String href;
        MutableInt mi;
        for(Element e : doc.getElementsByTag("a")){
            href = e.attr("href");
            if(href != null && !href.isEmpty()){
                try {
                    href = src.resolve(href).getHost();
                    mi = HOSTs.get(href);
                    if(mi == null){
                        HOSTs.put(href,new MutableInt());
                    }else{
                        mi.increment();
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static void main(String[] args){
        String FNDirIn = args.length > 0 ? args[0] : "data/testarc";
        String FNOut = args.length > 1 ? args[1] : "data/graph.result";
        String FNSkipto = args.length > 2 ? args[2] : null;
        
        ExtractLink el = new ExtractLink(new File(FNOut + ".host"),new File(FNOut + ".webpage"));
        try {
            el.runExtractLinkDir(new File(FNDirIn),FNSkipto);
        } catch (IOException ex) {
            Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
