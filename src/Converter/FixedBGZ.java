/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import ArcFileUtils.ArcReader;
import ArcFileUtils.ArcWriter;
import ArcFileUtils.BGZFWriter;
import ArcFileUtils.CompressedArcWriter;
import ArcFileUtils.WebArcReader;
import ArcFileUtils.WebArcWriter;
import ArcFileUtils.WebUtils;
import Crawler.MyURL;
import Crawler.SiteCrawler;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    public static void main(String[] args){
        String InDirname = "data/arc";
        String OutDirname = "data/arcgz";
        
        File OutDir = new File(OutDirname);
        if(!OutDir.exists()){
            OutDir.mkdir();
        }
        
        FilenameFilter ff = new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".arc.bgz");
            }
        };
        
        ArcReader war;
        CompressedArcWriter waw;
        String Filename;
        File OutArc,OutArcBGZF;
        boolean run=false;
        for(File f : (new File(InDirname)).listFiles(ff)){
            if(run){
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
            }else{
                if(f.getName().equalsIgnoreCase("crawl-bb2112.truelife.com.arc.bgz"))
                    run = true;
            }
        }
        
        
        
        /*
        BlockCompressedOutputStream BCOS;
            try {
                br = new DataInputStream(new FileInputStream(Src));
                BCOS = new BlockCompressedOutputStream(Dst);
                byte[] bytes = new byte[4096];
                int len;
                while( (len = br.read(bytes)) >= 0){
                    BCOS.write(bytes,0,len);
                }
                BCOS.close();
                br.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SiteCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
                */
            
    }
}
