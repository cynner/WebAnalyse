/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Analyse;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class OutDegree {
    
    public static void main(String[] args){
        
    }
    
    private HashMap<Integer, HashMap<Integer, Double>> importCSVFreq() throws FileNotFoundException{
            String CSVPath = "data/Graph/PageLink.csv";
            HashMap<Integer, HashMap<Integer, Double>> linkedList = new HashMap<>();
            HashMap<Integer, Double> SubLink;
            BufferedReader br = new BufferedReader(new FileReader(CSVPath));
            String Line = null;
            String[] strs;
            ArrayList<Integer> k = new ArrayList<>();
            ArrayList<Double> v = new ArrayList<>();
            Integer val;
            double pts;
            int idx;
            try {
                while((Line = br.readLine())!=null){
                    strs = Line.split(";");
                    k.clear();
                    v.clear();
                    pts = 1.0 / (strs.length - 1);
                    for(int i=1;i<strs.length;i++){
                        val = Integer.parseInt(strs[i]);
                        idx = k.indexOf(val);
                        if(idx >= 0){
                            v.set(idx, v.get(idx) + pts);
                        }else{
                            k.add(val);
                            v.add(pts);
                        }
                    }
                    SubLink = new HashMap<>();
                    for(int i=0;i<k.size();i++){
                        SubLink.put(k.get(i), v.get(i));
                    }
                    linkedList.put(Integer.parseInt(strs[0]), SubLink);
                }
            } catch (IOException ex) {
                System.err.println("At : '" + Line + "'");
                Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return linkedList;
        }
}
