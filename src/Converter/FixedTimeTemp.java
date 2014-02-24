/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Converter;

import ArcFileUtils.CompressedWebArcWriter;
import ArcFileUtils.WebArcReader_Old;
import Crawler.Fetcher;
import Crawler.SiteCrawler;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class FixedTimeTemp {
    
    
    public static void main(String[] args) {
        String InDirname = "data/crawldata2";
        String OutDirname = "data/crawlfixed";
        if(args.length > 1){
            InDirname = args[0];
        }else if(args.length == 1){
            OutDirname = args[1];
        }

        File OutDir = new File(OutDirname);
        if (!OutDir.exists()) {
            OutDir.mkdir();
        }

        FilenameFilter ff = new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".arc.gz");
            }
        };

        String Filename;
        File OutArc, OutArcBGZF, IN2;
        Long Diff;
        //boolean run = false;
        Fetcher fet = new Fetcher(SiteCrawler.UserAgent);
        for (File f : (new File(InDirname)).listFiles(ff)) {
            try (WebArcReader_Old war = new WebArcReader_Old(f, "utf-8")) {
                Filename = war.ArcFile.getName();
                System.out.println(Filename);
                OutArcBGZF = new File(OutDirname + "/." + Filename );
                IN2 = new File(OutDirname + "/" + Filename );
                if (IN2.exists()) {
                    try (WebArcReader_Old war2 = new WebArcReader_Old(IN2, "utf-8") ; CompressedWebArcWriter waw = new CompressedWebArcWriter(OutArcBGZF)) {
                        waw.HostIP = war.FileIP;
                        try {
                            if (war2.Next()) {
                                war.Next();
                                war2.Record.ArchiveDate = war.Record.ArchiveDate;
                                waw.WriteRecordKeepDate(war.Record);
                            }
                        } catch (Exception e) {
                            System.err.println("Skip error...");
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(FixedTimeTemp.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    IN2.delete();
                    OutArcBGZF.renameTo(IN2);
                }
            } catch (IOException ex) {
                Logger.getLogger(FixedTimeTemp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
