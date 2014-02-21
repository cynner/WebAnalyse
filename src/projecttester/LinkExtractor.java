/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import Crawler.MyURL;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author malang
 */
public class LinkExtractor implements Runnable {
    private File InputFile;
    private File OutputFile;
    private BufferedWriter bw;
     
    public LinkExtractor(File InputFile, File OutputFile){
        this.InputFile = InputFile;
        this.OutputFile = OutputFile;
    }
 
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" Start Extractfile " + InputFile);
        processCommand();
        System.out.println(Thread.currentThread().getName()+" End.");
    }
 
    private void processCommand() {
        
        try {
            bw = new BufferedWriter(new FileWriter(OutputFile, false));
            ArcUtils.AnalyseAll(InputFile, this, LinkExtractor.class.getMethod("Extract", String.class, String.class)) ;
            bw.close();
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(JSoupExample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(JSoupExample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex){
            
        }
        /*
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        
    }
    
    public void Extract(String Header, String Content){
        try {
            String srcURL = ArcUtils.URLFromHeader(Header);
            MyURL src = new MyURL(srcURL);
            MyURL dst = null;
            ArrayList<String> chkUniq = new ArrayList<String>();
            
            bw.write(src.UniqURL);
            Document doc = Jsoup.parse(Content);
            Elements es = doc.select("a");
            for(Element e : es){
                try{
                    dst = src.resolve(e.attr("href"));
                    if(!chkUniq.contains(dst.UniqURL)){
                        chkUniq.add(dst.UniqURL);
                        bw.write("\t" + dst.UniqURL);
                    }
                    dst = null;
                }catch (Exception ex) {
                    Logger.getLogger(LinkExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            es = null;
            doc = null;
            chkUniq = null;
            bw.newLine();
            // MALANG TEST ONLY
            bw.flush();
        } catch (Exception ex) {
            Logger.getLogger(LinkExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 /*
    @Override
    public String toString(){
        return this.command;
    }
   */ 
    
}
