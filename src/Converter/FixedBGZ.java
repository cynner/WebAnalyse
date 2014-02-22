/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Converter;

import ArcFileUtils.ArcReader;
import ArcFileUtils.CompressedArcWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.BlockCompressedOutputStream;

/**
 *
 * @author malang
 */
public class FixedBGZ {

    public static void main(String[] args) {
        String InDirname = "data/arc";
        String OutDirname = "data/arcgz";

        File OutDir = new File(OutDirname);
        if (!OutDir.exists()) {
            OutDir.mkdir();
        }

        FilenameFilter ff = new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".arc.bgz");
            }
        };

        String Filename;
        File OutArc, OutArcBGZF;
        boolean run = false;
        for (File f : (new File(InDirname)).listFiles(ff)) {
            if (run) {
                BlockCompressedOutputStream BCOS;
                try (ArcReader war = new ArcReader(f)) {
                    Filename = war.ArcFile.getName().replaceAll(".bgz", "");
                    System.out.println(Filename);
                    OutArcBGZF = new File(OutDirname + "/" + Filename + ".gz");
                    try (CompressedArcWriter waw = new CompressedArcWriter(OutArcBGZF)) {
                        try {
                            while (war.Next()) {
                                waw.WriteRecordFromData(war.Record);
                            }
                        } catch (Exception e) {
                            System.err.println("Skip error...");
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(FixedBGZ.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(FixedBGZ.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (f.getName().equalsIgnoreCase("crawl-bb2112.truelife.com.arc.bgz")) {
                    run = true;
                }
            }
        }
    }
}
