/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Test;

import ArcFileUtils.WebArcReader;
import java.io.File;
import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Malang
 */
public class ReadArcFile {
    public static void main(String [] args){
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        String Filename = "data/crawl-www.saraburi.m-society.go.th.arc.gz";
        
        File f = new File(Filename);
        int cnt = 0;
        try(WebArcReader war = new WebArcReader(f, "utf-8")){
            System.out.println(war.FileDesc + " " + war.FileIP + " " + war.FileDate + " " + war.FileContentType + " " + war.FileLength);
            while(war.Next()){
                cnt++;
                System.out.println(war.Record.URL + " " + war.Record.IPAddress + " " + war.Record.ArchiveDate.toString() + " " + war.Record.WebContentType + " " + war.Record.ArchiveLength);
            }
            System.out.println("Read Finished " + cnt + " Record.");
        } catch (IOException ex) {
            Logger.getLogger(ReadArcFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
