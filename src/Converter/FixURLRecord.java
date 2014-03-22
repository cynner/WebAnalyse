/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import ArcFileUtils.ArcFilenameFilter;
import ArcFileUtils.ArcReader;
import ArcFileUtils.ArcWriter;
import ArcFileUtils.CompressedArcWriter;
import Crawler.MyURL;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.filefilter.FileFileFilter;

/**
 *
 * @author wiwat
 */
public class FixURLRecord {
    public static void main(String [] args){
        String InDirName = args.length > 0 ? args[0] : "crawldata2";
        String OutDirName = args.length > 1 ? args[1] : InDirName + "-fixurl";
        File InDir = new File(InDirName);
        File OutDir = new File(OutDirName);
        HashSet<String> hs = new HashSet<>();
        hs.addAll(Arrays.asList(OutDir.list(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.All))));

        for(File f : InDir.listFiles(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.All))){
            File tmp = new File(OutDirName + "/." + f.getName() + ".fixurl");
            File out = new File(OutDirName + "/" + f.getName() );
            MyURL url;
            try(ArcReader ar = new ArcReader(f);){
                try(CompressedArcWriter aw = new CompressedArcWriter(tmp, f.getName(), ar.FileIP)){
                    while(ar.Next()){
                        try{
                            url = new MyURL(ar.Record.URL);
                            ar.Record.URL = url.UniqURL;
                            aw.WriteRecordFromData(ar.Record);
                        }catch (Exception ex){
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
