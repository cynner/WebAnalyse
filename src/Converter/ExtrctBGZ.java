/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import ArcFileUtils.ArcReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author malang
 */
public class ExtrctBGZ {
    public static void main(String[] args) throws IOException{
        String FileIn = "data/arc/crawl-026916789.nalueng.com.arc.bgz";
        String FileOut = "data/gg.txt";
        try (ArcReader ar = new ArcReader(new File(FileIn)); FileWriter fw = new FileWriter(FileOut)) {
            
            while(ar.Next()){
                fw.write("\n" + ar.Record.URL + "\n");
                fw.write(ar.Record.ArchiveContent);
                fw.flush();
            }
        }
                
        
    }
}
