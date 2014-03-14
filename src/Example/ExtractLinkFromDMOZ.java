/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Example;

import ArcFileUtils.WebArcReader;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import Crawler.SiteCrawler;
import Crawler.MyURL;
import java.io.IOException;

/**
 *
 * @author malang
 */
public class ExtractLinkFromDMOZ {
    static int MaxPage = 20;
    public static ExecutorService executor;
    public static boolean isUTF8(String Dir, String Host){
        File f = new File(Dir + "/crawl-" + Host + ".arc" );
        if(f.exists()){
            try (WebArcReader war = new WebArcReader(new File(Dir + "/crawl-" + Host + ".arc" ), true)) {
                if(war.Next()){
                    war.close();
                    return war.Record.charset.equals("utf-8");
                }
            } catch (IOException ex) {
                Logger.getLogger(ExtractLinkFromDMOZ.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    public static boolean isUTF8(File f){
        //File f = new File(Dir + "/crawl-" + Host + ".arc" );
        //if(f.exists()){
        try(WebArcReader war = new WebArcReader(f, true)){
            if(war.Next()){
                war.close();
                return war.Record.charset.equals("utf-8");
            }
        } catch (IOException ex) {
            Logger.getLogger(ExtractLinkFromDMOZ.class.getName()).log(Level.SEVERE, null, ex);
        }
        //}
        return false;
    }
    
    public static void parseLink(){
        String InFile = "crawldata/crawl-www.dmoz.org.arc";
        String OutFile = "dmoz.link.txt";
        String Dir, Link;
        int skip = 0;
        MyURL URL;
        File D;
        
        int cnt = 0;
        try (WebArcReader ar = new WebArcReader(new File(InFile), false)) {
            while (ar.Next()) {
                //if (cnt >= skip) {
                Elements es = ar.Record.Doc.select("ul.directory-url a");
                try {
                    Dir = URLDecoder.decode(ar.Record.URL, "utf-8");
                    Dir = Dir.substring(Dir.indexOf('/', 20)).substring(1);
                    System.out.println(Dir);
                    D = new File(Dir);
                    if (!D.isDirectory()) {
                        D.mkdirs();
                    }
                    SiteCrawler crwl;
                    for (Element e : es) {
                        cnt++;
                        
                        if (cnt <= skip) {
                            System.out.println("skip: " + cnt + ar.Record.URL);
                        } else {
                            try {
                                URL = new MyURL(e.attr("href"));
                                
                                Link = URL.getHost();
                                System.out.println(Dir + " " + cnt + " " + Link);

                                
                                //Runnable worker = new SiteCrawler(Link, Dir, MaxPage, "/", true);
                                //executor.execute(worker);
                                
                            } catch (Exception ex) {
                                Logger.getLogger(ExtractLinkFromDMOZ.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(ExtractLinkFromDMOZ.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ExtractLinkFromDMOZ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void Reload(File Dir){
        String Link;
        for(File f : Dir.listFiles()){
            if(f.isDirectory()){
                Reload(f);
            }else{
                if(isUTF8(f))
                    System.out.println("Skip : " + f.getPath());
                else{
                    Link = f.getName();
                    Link = Link.substring(6,Link.length() - 4);
                    //Runnable worker = new SiteCrawler(Link,null, f, MaxPage, "/", false, null, SiteCrawler.Mode.Crawl);
                    //executor.execute(worker);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Result:
        // <Dirname><Space><Link> 
        int ThreadNo = 10;
        
        executor = Executors.newFixedThreadPool(ThreadNo);
        //parseLink();
        Reload(new File("Thai"));
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

    }
}
