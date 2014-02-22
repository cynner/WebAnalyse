/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Analyse;

/**
 *
 * @author pramote
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PageRank {
	/*
	 * linkedlist : webpage-i <interger> ==> webpage <j> with score <double>
	 * maxSize : number of webpage in this graph
	 */
	private HashMap<Integer, HashMap<Integer, Double>> linkedlist;
	private int maxSize;
	private HashSet<Integer> zeroOutdegree;

	private void getZeroOutdegree() {
		HashSet<Integer> hasOutlink = new HashSet<>();
		for (Map.Entry<Integer, HashMap<Integer, Double>> t1 : linkedlist.entrySet())
			if (!hasOutlink.contains(t1.getKey()) && t1.getValue().size() > 0)
				hasOutlink.add(t1.getKey());
		for (int i = 0; i < maxSize; i++)
			if (!hasOutlink.contains(i))
				zeroOutdegree.add(i);
	}

	private double diff(double[] a, double[] b) {
		double sum = 0;
		for (int i = 0; i < maxSize; i++)
			sum += Math.pow(Math.abs(a[i] - b[i]) ,2);
		return Math.sqrt(sum);
	}

	public double[] cal(HashMap<Integer, HashMap<Integer, Double>> linkedList, int Size) {
		linkedlist = linkedList;
		maxSize = Size;
		zeroOutdegree = new HashSet<>();
		getZeroOutdegree();

		System.out.println("ZERO OUTLINK SIZE: " + zeroOutdegree.size());

		double[] source = new double[maxSize];
		for (int i = 0; i < maxSize; i++)
			source[i] = (double) 1.0 / maxSize;

		double[] dest;
		double threshold = 1E-7;
		double c = 0.85;
		double err;
		do {
			dest = new double[maxSize];
			for (int i = 0; i < maxSize; i++)
				dest[i] = 0;
			for (Map.Entry<Integer, HashMap<Integer, Double>> t1 : linkedlist.entrySet()) {
				Integer iID = t1.getKey();
				for (Map.Entry<Integer, Double> t2 : t1.getValue().entrySet()) {
					Integer jID = t2.getKey();
					dest[jID] += source[iID] * ((double) 1.0 / t1.getValue().size());
				}
			}
			double zeroInlinkScoreSum = 0;
			for (Integer temp : zeroOutdegree)
				zeroInlinkScoreSum += source[temp] * ((double) 1.0 / maxSize);
			for (int j = 0; j < maxSize; j++)
				dest[j] += zeroInlinkScoreSum;
			for (int i = 0; i < maxSize; i++)
				dest[i] = c * dest[i] + (1.0 - c) / maxSize;
			err = diff(source, dest);
			source = dest;
			System.out.println("Error " + err);
		} while (err > threshold);
		return source;
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
        
        public void exportHavalivala(HashMap<Integer, HashMap<Integer, Double>> linkedList){
            
        }
        
        public static void main(String[] args) throws FileNotFoundException{
            PageRank pr = new  PageRank();
            HashMap<Integer, HashMap<Integer, Double>>HM = pr.importCSVFreq();
            System.out.println("ReadCSV Finished" );
            double[] d = pr.cal(HM,4001000);
            try {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter("data/PR.txt"))) {
                    for(int i=0;i<d.length;i++){
                        bw.write(d[i]+"\n");
                    }
                }
            } catch (IOException ex) {
                
                Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
}