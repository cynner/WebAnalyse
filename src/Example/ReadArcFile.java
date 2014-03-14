/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Example;

import ArcFileUtils.WebArcReader;
import ArcFileUtils.WebArcRecord;
import Crawler.MyURL;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author malang
 */
public class ReadArcFile {
    
    public static void main(String[] args){
        String FileName = args.length > 0 ? args[0] : "data/crawldata/crawl-gamecenter.kapook.com.arc.gz";
        String FileOut = args.length > 1 ? args[1] : "data/testarc.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FileOut))) {
            try (WebArcReader aar = new WebArcReader(new File(FileName), false)) {
                int i = 1;
                MyURL src,dst;
                while (aar.Next()) {
                    
                    WebArcRecord w = aar.Record;
                    System.out.println(w.ArchiveLength);
                    System.out.println(w.charset);
                    System.out.println("---------------------------");
                    System.out.println(i++ + " " +w.URL);
                    
                    ////System.out.println(w.WebContent);
                    System.out.println("---------------------------");
                    // link
                    try {
                        src = new MyURL(w.URL);
                        Elements es = w.Doc.getElementsByTag("A");
                        for(Element e : es){
                            try {
                                dst = src.resolve(e.attr("href"));
                                bw.write(dst.UniqURL + "\n");
                            } catch (Exception ex) {
                                Logger.getLogger(ReadArcFile.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(ReadArcFile.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // -----
                }
            } catch (IOException ex) {
                Logger.getLogger(ReadArcFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(ReadArcFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
