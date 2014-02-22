/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ArcFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class GetWebArc {
    public static void main(String[] args){
        if(args.length == 0)
            args = new String[]{"data/crawldata/crawl-www.dek-d.com.arc.bgz","11"};
        int pageNO = Integer.parseInt(args[1]);
        //System.out.println("dddddddddddddd");
        try (WebArcReader war = new WebArcReader(new File(args[0]), false)) {
            for(int i=1;i<pageNO;i++)
                war.Skip();
            if(war.Next()){
                System.out.println(war.Record.URL);
                System.out.println(war.Record.WebContent);
            }
        } catch (IOException ex) {
            Logger.getLogger(GetWebArc.class.getName()).log(Level.SEVERE, null, ex);
        }
                
    }
}
