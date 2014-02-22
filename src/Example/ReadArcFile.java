/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Example;

import ArcFileUtils.ApacheArchReader;
import ArcFileUtils.ArcReader;
import ArcFileUtils.ArcRecord;
import ArcFileUtils.WebArcReader;
import ArcFileUtils.WebArcRecord;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class ReadArcFile {
    
    public static void main(String[] args){
        try (WebArcReader aar = new WebArcReader(new File("consump.arc"),false)) {
            int i = 0;
            while(aar.Next()){
                WebArcRecord w = aar.Record;
                System.out.println(w.ArchiveLength);
                System.out.println(w.charset);
                System.out.println("---------------------------");
                System.out.println(w.WebContent);
                System.out.println("---------------------------");
                i++;
            }
        } catch (IOException ex) {
            Logger.getLogger(ReadArcFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
