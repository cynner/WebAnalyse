/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import ArcFileUtils.BGZFWriter;
import ArcFileUtils.WebArcReader;
import ArcFileUtils.WebArcRecord;
import ArcFileUtils.WebArcWriter;
import ArcFileUtils.WebUtils;
import Crawler.MyURL;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 * 
 * 
 */


public class ConvertOld2NewARC {
    public static WebUtils wu = new WebUtils();
    public static void main(String[] args){
        if(args.length == 0){
            args = new String[]{"data/oldcrawl/","data/newcrawl/"};
        }
        
        File InDir = new File(args[0]);
        File OutDir= new File(args[1]);
        
        if(!InDir.exists()){
            System.err.println("No such directory -> " + args[0]);
            System.exit(1);
        }
        
        if(!OutDir.exists()){
            OutDir.mkdir();
        }
        
        FilenameFilter ff = new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".arc");
            }
        };
        
        WebArcReader war;
        WebArcWriter waw;
        String Filename;
        File OutArc,OutArcBGZF;
        for(File f : (new File(args[0])).listFiles(ff)){
            war = new WebArcReader(f, true);
            Filename = war.ArcFile.getName().replaceAll("-2013\\d{10}-00000.arc", ".arc");
            
            OutArc = new File(args[1] + Filename);
            OutArcBGZF = new File(args[1] + Filename + ".bgz");
            waw = new WebArcWriter(OutArc, Filename, false, war.FileIP);
            while(war.Next()){
                war.Record.WebContent = wu.HTMLCompress(war.Record.Doc);
                try {
                    war.Record.URL = (new MyURL(war.Record.URL)).UniqURL;
                } catch (Exception ex) {
                    Logger.getLogger(ConvertOld2NewARC.class.getName()).log(Level.SEVERE, null, ex);
                }
                waw.WriteRecord(war.Record);
            }
            waw.close();
            war.close();
            BGZFWriter.Compress(OutArc, OutArcBGZF);
            OutArc.delete();
        }
    }

}

