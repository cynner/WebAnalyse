/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Analyse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author malang
 */
public class SCCTree {
    //public static int[][] mat1;
    public static ArrayList<Integer>[] mat1_o;
    public static ArrayList<Integer>[] mat2;
    public static int[] seq;
    public static int seq_idx;
    public static int gr;
    public static int step;
    public static int mat_size;
    public static int SCC_SIZE;
    public static String SCC_str;
    
    //public static BufferedWriter[] bwGroup = new BufferedWriter[7];
    
    public static void ImportHavalivalaCSV(String FileName){
    }
    
    public static void ImportNESD(String FileName){
        
    }
    
    
    
    /*
    public static void dfs1(int id){
        int[] Node = mat1[id];
        if(Node[0] <= 0){
            Node[0] = step++;
            for(int i=2; i < Node.length; i++){
                dfs1(Node[i]);
            }
            Node[1] = step++;
            seq[seq_idx--] = id;
        }
    }
    */
    
    public static void dfs1(int id){
        ArrayList<Integer> Node = mat1_o[id];
        ///try{
        if(Node.get(0) <= 0){
            Node.set(0,step++);
            for(int i=2; i < Node.size(); i++){
                dfs1(Node.get(i));
            }
            Node.set(1,step++);
            seq[seq_idx--] = id;
            if(id > 281889)
                System.err.println(id);
        }
        //}catch(IndexOutOfBoundsException e){
        //    System.err.println("id:" + id + ",Seq_idx:" + seq_idx+",matsize:"+mat_size+",Seqsize:"+seq.length);
        //}
    }
    
    public static void dfs2(int id){
        ArrayList<Integer> Node = mat2[id];
        if(Node.get(0) <= 0){
            Node.set(0,step++);
            SCC_str += id+";";
            SCC_SIZE++;
            for(int i=2; i < Node.size(); i++){
                dfs2(Node.get(i));
            }
            Node.set(1,gr);
        }
    }
    
    
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        mat_size = 5000000;
        int edge;
        int src=0,tmpv,i;
        /*
        String strInputCSV = args.length >= 1 ? args[0] : "data/Graph/PageLink.csv"; 
        String strOutput = args.length >= 2 ? args[1] : "data/Graph/SCC.txt";
        String strDirSCC = args.length >= 3 ? args[1] : "data/Graph";
        */
        String strInputCSV = args.length >= 1 ? args[0] : "/home/wiwat/gr.txt"; 
        String strOutput = args.length >= 2 ? args[1] : "/home/wiwat/res00.txt";
        String strDirSCC = args.length >= 3 ? args[1] : "data/Graph";
        File fileInputCsv = new File(strInputCSV);
        String[] strs;
        String Line;
        
        try (BufferedReader br = new BufferedReader(new FileReader(fileInputCsv))) {
            
            
            
            /*//METHOD 1------------------------------------------
            
            mat1 = new int[mat_size][];
            mat2 = new ArrayList[mat_size];
            for( i=1;i<mat_size;i++){
                mat2[i] = new ArrayList<>();
                mat2[i].add(0); // Foreward Tick
                mat2[i].add(0); // Backward Tick
            }
            
            // Matrix Format Group,ptr,Forward,Backword,Dst1,Dst2,Dst3,...
            // Size 2(F/B) + n(D) = n + 2;
            
            while((Line = br.readLine()) != null){
                strs = Line.split(";");
                
                src = Integer.parseInt(strs[0]);
                mat1[src] = new int[strs.length+1];
                //mat1[src].add(0); // Foreward Tick
                //mat1[src].add(0); // Backward Tick
                for(i=1;i<strs.length;i++){
                    tmpv = Integer.parseInt(strs[i]);
                    mat1[src][i+1] = tmpv;
                    mat2[tmpv].add(src);
                }
                
            }
            //----------------------------------------------------
            */
            //METHOD 2------------------------------------------
            mat_size = Integer.parseInt(Line = br.readLine());
            edge = Integer.parseInt(Line = br.readLine());
            mat1_o= new ArrayList[mat_size];
            mat2 = new ArrayList[mat_size];
            for( i=0;i<mat_size;i++){
                mat2[i] = new ArrayList<>();
                mat2[i].add(0); // Foreward Tick
                mat2[i].add(0); // Backward Tick
                mat1_o[i] = new ArrayList<>();
                mat1_o[i].add(0); // Foreward Tick
                mat1_o[i].add(0); // Backward Tick
            }
            
            for(int j =0 ;j < edge;j++){
                strs = br.readLine().split("\t");
                src = Integer.parseInt(strs[0]);
                for(i=1;i<strs.length;i++){
                    tmpv = Integer.parseInt(strs[i]);
                    mat1_o[src].add(tmpv);
                    mat2[tmpv].add(src);
                }
            }
            //----------------------------------------------------
            
            
        }
        
        
        System.out.println("Read File Success");

        seq = new int[mat_size];
        
        // Fill Missing box
        /*
        for(i=1;i<mat_size;i++){
            if(mat1[i] == null){
                mat1[i] = new int[]{0,0};
            }
        }
        */
        
        System.out.println("Allocation Success");
        
        // Init Config 
        seq_idx = mat_size - 1;
        step = 1;
        
        for(i=0;i<mat_size;i++){
            dfs1(i);
        }
        
        
        System.out.println("Finished 1st Trace...");
        
        
        
        // Init Config 
        seq_idx++;
        System.out.println("SEQ idx : " + seq_idx);
        step = 1;
        gr = 1;
        int org_step;
        
        while(seq_idx < mat_size){
            org_step = step;
            SCC_str = "";
            SCC_SIZE = 0;
            dfs2(seq[seq_idx]);
            if(org_step != step){
                gr++;
                /*
                if(SCC_SIZE < 10){
                    bwGroup[0].write(SCC_str + "\n");
                }else if(SCC_SIZE < 100){
                    bwGroup[1].write(SCC_str + "\n");
                }else if(SCC_SIZE < 1000){
                    bwGroup[2].write(SCC_str + "\n");
                }else if(SCC_SIZE < 10000){
                    bwGroup[3].write(SCC_str + "\n");
                }else if(SCC_SIZE < 100000){
                    bwGroup[4].write(SCC_str + "\n");
                }else if(SCC_SIZE < 1000000){
                    bwGroup[5].write(SCC_str + "\n");
                }else{
                    bwGroup[6].write(SCC_str + "\n");
                }*/
            }
            seq_idx++;
        }
        
        
        System.out.println("Finished 2nd Trace...");
        gr--;
        System.out.println("Group no: " + gr);
        System.out.println("Writing Result...");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(strOutput))) {
            for(i=1;i<mat_size;i++){
                //if(mat1[i] != null){
                bw.write(i + ":" + mat2[i].get(1) + "\n");
                //}
            }
        }
        System.out.println("Success.");
    }
}
