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
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
@Deprecated
public class FixedBGZ {
    
    
    public static void main(String[] args) {
        
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        
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
        File OutArc, OutArcBGZF;
        Long Diff;
        //boolean run = false;
        Fetcher fet = new Fetcher(SiteCrawler.UserAgent);
        for (File f : (new File(InDirname)).listFiles(ff)) {
            try (WebArcReader_Old war = new WebArcReader_Old(f, "utf-8")) {
                Filename = war.ArcFile.getName();
                System.out.println(Filename);
                OutArcBGZF = new File(OutDirname + "/" + Filename);
                if (!OutArcBGZF.exists()) {
                    try (CompressedWebArcWriter waw = new CompressedWebArcWriter(OutArcBGZF, war.FileIP)) {
                        try {
                            if (war.Next()) {
                                Diff = fet.diffServerDateTime(war.Record.URL);
                                if (Diff != null) {
                                    war.Record.ServerTime = war.Record.ArchiveDate.getTime() + Diff;
                                }
                                waw.WriteRecordKeepDate(war.Record);
                                while (war.Next()) {
                                    if (Diff != null) {
                                        war.Record.ServerTime = war.Record.ArchiveDate.getTime() + Diff;
                                    }
                                    waw.WriteRecordKeepDate(war.Record);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Skip error...");
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(FixedBGZ.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(FixedBGZ.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
