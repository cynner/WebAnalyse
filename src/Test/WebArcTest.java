/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Test;

import ArcFileUtils.WebArcReader;
import java.io.File;

/**
 *
 * @author malang
 */
public class WebArcTest {
    public static void main(String[] args){
        File dir = new File("data/crawldata2");
        int j=1;
        for(int i=0;i<100;i++){
            for(File f : dir.listFiles()){
                if(f.getName().endsWith(".gz")){
                    System.out.println("File No. " + j);
                    System.out.println(f.getName() + " Open. Round " + i);
                    WebArcReader war = new WebArcReader(f, "utf-8");
                    war.close();
                    System.out.println(f.getName() + " Closed.");
                    j++;
                }
            }
        }
    }
}
