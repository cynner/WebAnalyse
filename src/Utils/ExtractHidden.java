/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Utils;

import ArcFileUtils.ArcFilenameFilter;
import ArcFileUtils.ArcRecord;
import ArcFileUtils.ArcWriter;
import ArcFileUtils.WebArcReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author wiwat
 */
public class ExtractHidden {
    public static void main(String[] args){
        Elements elHide;
        //for(Element e : es){
        //    System.out.println(e.toString());
        //}
        
        String InDirName = args.length > 0 ? args[0] : "data/test";
        String OutFileName = args.length > 1 ? args[1] : "data/hiddencontent.arc";
        File InDir = new File(InDirName);
        File OutFile = new File(OutFileName);
        ArcRecord ar = new ArcRecord();
        try (ArcWriter aw = new ArcWriter(OutFile, false)) {
            for (File f : InDir.listFiles(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.All))) {
                try (WebArcReader war = new WebArcReader(f, "utf-8")) {
                    while (war.Next()) {
                        elHide = war.Record.Doc.select("*[style~=(?i)(display *: *none|visibility *: *hidden|(width|height) *: *0|(indent|left|top) *: *-\\d{3}\\d*)]");
                        if(!elHide.isEmpty()){
                            System.out.println(war.Record.URL);
                            war.Record.ArchiveContent = elHide.toString();
                            aw.WriteRecord(war.Record);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ExtractHidden.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("=== Success ===");
        } catch (IOException ex) {
            Logger.getLogger(ExtractHidden.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
}
