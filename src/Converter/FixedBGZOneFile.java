/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import ArcFileUtils.ArcReader;
import ArcFileUtils.CompressedArcWriter;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.BlockCompressedOutputStream;

/**
 *
 * @author malang
 */
@Deprecated
public class FixedBGZOneFile {
     public static void main(String[] args){
        String InDirname = "data/arc/crawl-202.142.219.162.arc.bgz";
        String OutDirname = "data/arcgz";
        
        String Filename;
        File OutArcBGZF;
        File f = new File(InDirname);
            BlockCompressedOutputStream BCOS;
         try (ArcReader war = new ArcReader(f)) {
             Filename = war.ArcFile.getName().replaceAll(".bgz", "");
             System.out.println(Filename);
             //OutArc = new File(OutDirname + "/" + Filename);
             OutArcBGZF = new File(OutDirname + "/" + Filename + ".gz");
            try (CompressedArcWriter waw = new CompressedArcWriter(OutArcBGZF)) {
                try{
                    while(war.Next()){
                        waw.WriteRecordFromData(war.Record);
                    }
                }catch(Exception e){
                    System.err.println("Skip error...");
                }
            } catch (IOException ex) {
                Logger.getLogger(FixedBGZOneFile.class.getName()).log(Level.SEVERE, null, ex);
            }
         } catch (IOException ex) {
             Logger.getLogger(FixedBGZOneFile.class.getName()).log(Level.SEVERE, null, ex);
         }
            
        
    }
}
