/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author malang
 */
public class ConvertLinkPage {
    /*  === Pre Process ===
        *** Check Unique Exists Link ***
        
    */
    
    public static String DirName = "data/Graph/";
    public static String OutputName = "PageLink.csv";
    public static String InputDirName = "data/arc/";
    public static String DBName = "PageLabel.sqlite3";
    
    /*  === Pre Process ===
        *** Check Unique Exists Link ***
    */
    public static void CreateUniqueDB(){
        File Dir = new File(DirName);
        if(!Dir.isDirectory()){
            Dir.mkdir();
        }
        
        SQLiteConnection db = new SQLiteConnection(new File(DirName + DBName));
        File[] files = (new File(InputDirName)).listFiles();
        ArcFileUtils.WebArcReader war = null;
        
        try {
            db.open(true);
            db.exec("BEGIN;");
           
            db.exec("CREATE TABLE IF NOT EXISTS webpage(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, url VARCHAR(2048) UNIQUE NOT NULL);");
                
            for(File f : files){
                war = new WebArcReader(f, false, false);
                while(war.Next()){
                    db.exec("INSERT OR IGNORE INTO webpage(url) VALUES(\"" 
                            + war.Record.URL.replace("\"", "\"\"")
                            + "\");");
                }
            }
            System.out.println("Executing. ");
            db.exec("COMMIT;");
            System.out.println("Finished. ");
        } catch (SQLiteException ex) {
            if(war != null)
                System.err.println("AT URL: " + war.Record.URL.replace("\"", "\"\""));
            ex.printStackTrace();    
        } finally{
            if(war != null)
                war.close();
            db.dispose();
        }
            
    }
    
    /*  == Main Process ==
        Require OutputFile,InputDir,DBFile
    */
    public static void Convert(){
        WebArcReader war = null;
        MyURL src,dst ;
        SQLiteConnection db = new SQLiteConnection(new File(DirName + DBName));
        SQLiteStatement st;
        String str;
        try {
            
            db.openReadonly();
            BufferedWriter bw = new BufferedWriter(new FileWriter(DirName + OutputName,false));
            File[] files = (new File(InputDirName)).listFiles();
        
            for ( File f : files){
                war = new WebArcReader(f, false);
                while(war.Next()){
                    st = db.prepare("SELECT id FROM webpage WHERE url=\"" + 
                            war.Record.URL.replace("\"", "\"\"") + "\";");
                    str = null;
                    if(st.step())
                        str = st.columnString(0);
                    else
                        continue;
                    st.dispose();
                    Elements es = war.Record.Doc.getElementsByTag("a");
                    try {
                        src = new MyURL(war.Record.URL);
                        for(Element e : es){
                            try{
                                dst = src.resolve(e.attr("href"));
                                st = db.prepare("SELECT id FROM webpage WHERE url=\"" + 
                                dst.UniqURL.replace("\"", "\"\"") + "\";");
                                if(st.step())
                                    str += ";" + st.columnString(0);
                                st.dispose();
                            } catch (Exception ex) {
                                //Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
                            }        
                        }
                        bw.write(str + "\n");
                    } catch (Exception ex) {
                        Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                war.close();
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLiteException ex) {
            Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(war != null){
                war.close();
            }
        }
    }
    
    public static void ConvertHybride(){
        HashMap<String, Integer> HM = new HashMap<String, Integer>();
        WebArcReader war = null;
        MyURL src,dst ;
        SQLiteConnection db = new SQLiteConnection(new File(DirName + DBName));
        SQLiteStatement st;
        String lnk;
        String str;
        try {
            
            db.openReadonly();
            BufferedWriter bw = new BufferedWriter(new FileWriter(DirName + OutputName,true));
            File[] files = (new File(InputDirName)).listFiles();
        
            boolean skip = true;
            for ( File f : files){
                if(skip){
                    if(f.getName().equals("crawl-camera.garagegadget.com.arc.gz")){
                        skip = false;
                        System.out.println("Stop Skipppppppp");
                    }else{
                        continue;
                    }
                }
                war = new WebArcReader(f, false);
                while(war.Next()){
                    try {
                        src = new MyURL(war.Record.URL);
                        
                        Elements es = war.Record.Doc.getElementsByTag("a");
                        str = "";
                        if(HM.containsKey(src.UniqURL)){
                            str += HM.get(src.UniqURL);
                        }else{
                        
                            st = db.prepare("SELECT id FROM webpage WHERE url=\"" + 
                                src.UniqURL.replace("\"", "\"\"") + "\";");
                    
                            if(st.step()){
                                 HM.put(src.UniqURL, st.columnInt(0));
                                str += st.columnString(0);
                            }else
                                continue;
                            st.dispose();
                        }
                        
                        for(Element e : es){
                            try{
                                dst = src.resolve(e.attr("href"));
                                if(HM.containsKey(dst.UniqURL)){
                                    str += ";" + HM.get(dst.UniqURL);
                                }else{
                                    //try{
                                        st = db.prepare("SELECT id FROM webpage WHERE url=\"" + 
                                        dst.UniqURL.replace("\"", "\"\"") + "\";");
                                        if(st.step()){
                                            HM.put(dst.UniqURL, st.columnInt(0));
                                            str += ";" + st.columnString(0);
                                        }
                                        st.dispose();
                                    //}catch(Exception Ex){
                                    //    Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, Ex);
                                    //}
                                }
                            }catch(Exception Ex){
                                //Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, Ex);
                                
                            }
                            
                        }
                        bw.write(str + "\n");
                    } catch (Exception ex) {
                        Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                war.close();
                bw.flush();
                System.out.println(f.getName());
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLiteException ex) {
            Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(war != null){
                war.close();
            }
        }
    }
    
    public static void ConvertOnMem(){
        HashMap<String, Integer> HM = new HashMap<String, Integer>();
        WebArcReader war = null;
        MyURL src,dst ;
        SQLiteConnection db = new SQLiteConnection(new File(DirName + DBName));
        SQLiteStatement st;
        String lnk;
        String str;
        
        
        try {
            
            db.openReadonly();
            
            
            st = db.prepare("SELECT id,url FROM webpage ;");
                    
            while(st.step())
                HM.put(st.columnString(1), st.columnInt(0));
            
            System.out.println("Load on mem success.");
            
            st.dispose();
            db.dispose();
            
            
            
            BufferedWriter bw = new BufferedWriter(new FileWriter(DirName + OutputName,true));
            File[] files = (new File(InputDirName)).listFiles();
        
            boolean skip = true;
            for ( File f : files){
                if(skip){
                    if(f.getName().equals("crawl-camera.garagegadget.com.arc.gz")){
                        skip = false;
                        System.out.println("Stop Skipppppppp");
                    }else{
                        continue;
                    }
                }
                war = new WebArcReader(f, false);
                while(war.Next()){
                    try {
                        src = new MyURL(war.Record.URL);
                        
                        Elements es = war.Record.Doc.getElementsByTag("a");
                        str = "";
                        if(HM.containsKey(src.UniqURL)){
                            str += HM.get(src.UniqURL);
                        }else{
                            continue;
                        }
                        
                        for(Element e : es){
                            try{
                                dst = src.resolve(e.attr("href"));
                                if(HM.containsKey(dst.UniqURL)){
                                    str += ";" + HM.get(dst.UniqURL);
                                }
                            }catch(Exception Ex){
                                //Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, Ex);
                                
                            }
                            
                        }
                        bw.write(str + "\n");
                    } catch (Exception ex) {
                        Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                war.close();
                bw.flush();
                System.out.println(f.getName());
            }
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLiteException ex) {
            Logger.getLogger(ConvertLinkPage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(war != null){
                war.close();
            }
        }
    }
    
    public static void main(String[] args){
        //CreateUniqueDB();
        ConvertHybride();
    }
    
}
