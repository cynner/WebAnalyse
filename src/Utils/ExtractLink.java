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
import java.util.HashSet;
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
    
    
    private File OutHostLink, OutWebLink;
            
    private BufferedWriter bwWebLink;
    private BufferedWriter bwHostLink;
    
    private SQLiteConnection dbWeb = new SQLiteConnection(DBDriver.TableConfig.FileWebPageDB);
    private SQLiteConnection dbHost= new SQLiteConnection(DBDriver.TableConfig.FileWebSiteDB);
    
    public ExtractLink(File OutHostLink, File OutWebLink){
        this.OutWebLink = OutWebLink;
        this.OutHostLink = OutHostLink;
    }
    
    public void runExtractLinkDir(File Dir) throws IOException{
        HashMap <String,MutableInt> HOSTs = new HashMap<>();
        HashSet <String> URLs;
        MyURL src;
        String srcDomain;
        bwWebLink = new BufferedWriter(new FileWriter(OutWebLink));
        bwHostLink = new BufferedWriter(new FileWriter(OutHostLink));
        try {
            dbWeb.openReadonly();
            dbHost.openReadonly();

            for (File f : Dir.listFiles(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.ArcOnly))) {
                System.out.println(f.getName());
                try (WebArcReader war = new WebArcReader(f, "utf-8")) {
                    srcDomain = "";
                    while (war.Next()) {
                        try {
                            src = new MyURL(war.Record.URL);
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
                } catch (IOException ex) {
                    Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                }
                HOSTs.clear();
            }
        } catch (SQLiteException ex) {
            Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            dbWeb.dispose();
            dbHost.dispose();
        }
        bwWebLink.close();
        bwHostLink.close();
    }
    
    private void WriteWebLink(MyURL src, HashSet<String> URLs) {
        String srcID, dstID;
        SQLiteStatement st;
        try {
            st = dbWeb.prepare("SELECT id FROM webpage WHERE url='" + src.UniqURL.replaceAll("'", "''") + "';");
            if (st.step()) {
                srcID = st.columnString(0);
                st.dispose();
                bwWebLink.write(srcID);

                for (String url : URLs) {
                    try {
                        st = dbWeb.prepare("SELECT id FROM webpage WHERE url='" + url.replaceAll("'", "''") + "';");
                        if (st.step()) {
                            dstID = st.columnString(0);
                            bwWebLink.write(";" + dstID);
                        }
                        st.dispose();
                    } catch (SQLiteException | IOException ex) {
                        Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
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
                for (Map.Entry<String, MutableInt> host : HOSTs.entrySet()) {
                    try {
                        st = dbHost.prepare("SELECT id FROM website WHERE hostname = '" + host.getKey().replaceAll("'", "''") + "';");
                        if (st.step()) {
                            dstID = st.columnString(0);
                            bwHostLink.write(srcID + ";" + dstID + ";" + host.getValue().value + "\n");
                            st.dispose();
                        }
                    } catch (SQLiteException | IOException ex) {
                        Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                st.dispose();
            }
        } catch (SQLiteException ex) {
            Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private HashSet<String> ExtractWebPageNHostCount(MyURL src, Document doc, HashMap<String,MutableInt> HOSTs){
        MyURL lnk;
        String href,host;
        MutableInt mi;
        HashSet<String> URLs = new HashSet<>();
        for(Element e : doc.getElementsByTag("a")){
            href = e.attr("href");
            if(href != null && !href.isEmpty()){
                try {
                    lnk = src.resolve(href);
                    if(!URLs.contains(lnk.UniqURL)){
                        URLs.add(lnk.UniqURL);
                    }
                    
                    host = lnk.getHost();
                    mi = HOSTs.get(host);
                    if(mi == null){
                        HOSTs.put(host,new MutableInt());
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
    
    private HashSet<String> ExtractWebPage(MyURL src, Document doc){
        String href;
        HashSet<String> URLs = new HashSet<>();
        for(Element e : doc.getElementsByTag("a")){
            href = e.attr("href");
            if(href != null && !href.isEmpty()){
                try {
                    href = src.resolve(href).UniqURL;
                    if(!URLs.contains(href)){
                        URLs.add(href);
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
        
        ExtractLink el = new ExtractLink(new File(FNOut + ".host"),new File(FNOut + ".webpage"));
        try {
            el.runExtractLinkDir(new File(FNDirIn));
        } catch (IOException ex) {
            Logger.getLogger(ExtractLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
