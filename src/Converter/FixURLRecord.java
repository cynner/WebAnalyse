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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiwat
 */
public class FixURLRecord {
    public static void main(String [] args){
        String ConvDir = args.length > 0 ? args[0] : "";
        File dir = new File(ConvDir);
        String[] ss = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".fixurl");
            }
        });
        String s = ss.length > 0 ? ss[ss.length - 1].replaceAll("\\.fixurl", "") : null;
        boolean skip = (s != null) ;
        for(File f : dir.listFiles(new ArcFilenameFilter(ArcFilenameFilter.AcceptType.All))){
            if(skip){
                if(f.getName().equals(s)){
                    skip = false;
                }else{
                    continue;
                }
            }
            File o = new File(f.getPath() + ".fixurl");
            MyURL url;
            try(ArcReader ar = new ArcReader(f);){
                try(CompressedArcWriter aw = new CompressedArcWriter(o, f.getName(), ar.FileIP)){
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
            } catch (IOException ex) {
                Logger.getLogger(FixURLRecord.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
         
    }
}
