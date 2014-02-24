/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Analyse;

import ArcFileUtils.ArcFilenameFilter;
import ArcFileUtils.WebArcReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class CheckSizePage {
    
    public static String DBPath = "resource/website.sqlite3";
    public static String WebArcDir = "data/crawldata2";// "data/arc-org";
    
    public static void main(String[] args){
        
        File wad = new File(WebArcDir);
        for(File f : wad.listFiles(new ArcFilenameFilter())){
            System.out.println(f.getName());
            try (WebArcReader war = new WebArcReader(f, true)) {
                while(war.Next()){
                    System.out.println(war.Record.URL);
                }
            } catch (IOException ex) {
                Logger.getLogger(CheckSizePage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
