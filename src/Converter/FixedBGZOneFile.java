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
import net.sf.samtools.util.BlockCompressedOutputStream;

/**
 *
 * @author malang
 */
public class FixedBGZOneFile {
     public static void main(String[] args){
        String InDirname = "data/arc/crawl-202.142.219.162.arc.bgz";
        String OutDirname = "data/arcgz";
        
        ArcReader war;
        CompressedArcWriter waw;
        String Filename;
        File OutArcBGZF;
        File f = new File(InDirname);
            BlockCompressedOutputStream BCOS;
            war = new ArcReader(f);
            Filename = war.ArcFile.getName().replaceAll(".bgz", "");
            System.out.println(Filename);
            //OutArc = new File(OutDirname + "/" + Filename);
            OutArcBGZF = new File(OutDirname + "/" + Filename + ".gz");
            
            waw = new CompressedArcWriter(OutArcBGZF);
            try{
                while(war.Next()){
                    waw.WriteRecordFromData(war.Record);
                }
            }catch(Exception e){
                System.err.println("Skip error...");
            }
            waw.close();
            war.close();
            //BGZFWriter.Compress(OutArc, OutArcBGZF);
            //OutArc.delete();
            
        
    }
}
