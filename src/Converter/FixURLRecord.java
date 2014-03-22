/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Converter;

import ArcFileUtils.ArcFilenameFilter;
import ArcFileUtils.ArcReader;
import ArcFileUtils.CompressedArcWriter;
import ArcFileUtils.CompressedWebArcWriter;
import ArcFileUtils.WebArcReader;
import Crawler.MyURL;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiwat
 */
public class FixURLRecord {

    public static void main(String[] args) {
        String InDirName = args.length > 0 ? args[0] : "test";
        String OutDirName = args.length > 1 ? args[1] : InDirName + "-fixurl";
        File InDir = new File(InDirName);
        File OutDir = new File(OutDirName);
        HashSet<String> hs = new HashSet<>();
        if(OutDir.exists())
            hs.addAll(Arrays.asList(OutDir.list(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.All))));
        else
            OutDir.mkdirs();
        MyURL url;
        for (File f : InDir.listFiles(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.All))) {
            File tmp = new File(OutDirName + "/." + f.getName() + ".fixurl");
            File out = new File(OutDirName + "/" + f.getName());

            if (!hs.contains(out.getName())) {
                try (WebArcReader ar = new WebArcReader(f, "utf-8");) {
                    try (CompressedWebArcWriter aw = new CompressedWebArcWriter(tmp, f.getName(), ar.FileIP)) {
                        while (ar.Next()) {
                            try {
                                url = new MyURL(ar.Record.URL);
                                ar.Record.URL = url.UniqURL;
                                aw.WriteRecordKeepDate(ar.Record);
                            } catch (Exception ex) {
                                System.err.println("At : " + ar.Record.URL);
                                Logger.getLogger(FixURLRecord.class.getName()).log(Level.SEVERE, null, ex);
                                System.exit(2);
                            }
                        }
                    }
                    tmp.renameTo(out);
                } catch (IOException ex) {
                    Logger.getLogger(FixURLRecord.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }
}
