/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiwat
 */
public class SDW2SVG {
    public static void main(String[] args){
        String InName = args.length > 0 ? args[0] : "data/graph/converted.host";
        String OutName = args.length > 1 ? args[1] : InName + ".csv";
        String[] strs;
        String Line;
        String src;
        try(BufferedReader br = new BufferedReader(new FileReader(InName));
            BufferedWriter bw = new BufferedWriter(new FileWriter(OutName))){
            strs = br.readLine().split(";");
            src = strs[0];
            bw.write(strs[0] + ";" + strs[1] + ":" + strs[2]); 
            while((Line = br.readLine()) != null){
                strs = Line.split(";");
                if(src.equals(strs[0])){
                    bw.write(";" + strs[1] + ":" + strs[2]); 
                }else{
                    bw.write("\n" + strs[0] + ";" + strs[1] + ":" + strs[2]); 
                    src = strs[0];
                }
            }
            System.out.println("=== Success ===");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SDW2SVG.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex){
            Logger.getLogger(SDW2SVG.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
