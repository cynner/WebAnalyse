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
@Deprecated
public class SCC {
    public static int[][] mat1;
    public static ArrayList<Integer>[] mat2;
    public static int[] TraceMap;
    public static int[] seq;
    public static int seq_idx;
    public static int ptrTM;
    public static int gr;
    public static int step;
    public static int mat_size;
    
    public static int getMaxIdx(){
        while(seq_idx < mat_size){
            if(mat2[seq[seq_idx]].get(1) <= 0){
                return seq[seq_idx];
            }
            seq_idx++;
        }
        return -1;
        /*
        int max = -1, tmp, idx = -1;
        for(int i = 1; i < mat_size; i++){
            if(mat1[i] != null && mat2[i].get(1) <= 0) {
                tmp = mat1[i][2];
                if(max < tmp){
                    max = tmp;
                    idx = i;
                }
            }
        }
        return idx;
                */
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException{
        mat_size = 5000000;
        int[] tmprow;
        int src=0,tmpv,i;
        String strInputCSV = args.length >= 1 ? args[0] : "data/Graph/PageLink.csv"; 
        String strOutput = args.length >= 2 ? args[1] : "data/Graph/SCC.txt";
        File fileInputCsv = new File(strInputCSV);
        String[] strs;
        String Line;
        try (BufferedReader br = new BufferedReader(new FileReader(fileInputCsv))) {
            mat1 = new int[mat_size][];
            mat2 = new ArrayList[mat_size];
            for( i=1;i<mat_size;i++){
                mat2[i] = new ArrayList<>();
                mat2[i].add(3); // PTR
                mat2[i].add(0); // Foreward Tick
                mat2[i].add(0); // Backward Tick
            }
            
            // Matrix Format Group,ptr,Forward,Backword,Dst1,Dst2,Dst3,...
            // Size 1(ptr) + 2(F/B) + 1(Group) + n(D) = n + 4;
            
            while((Line = br.readLine()) != null){
                strs = Line.split(";");
                tmprow = new int[strs.length + 3];
                src = Integer.parseInt(strs[0]);
                for(i=1;i<strs.length;i++){
                    tmpv = Integer.parseInt(strs[i]);
                    tmprow[i+3] = tmpv;
                    mat2[tmpv].add(src);
                }
                tmprow[0] = 4;
                mat1[src] = tmprow;
            }
            mat_size = src + 1;
            seq = new int[mat_size];
            seq_idx = mat_size - 1;
        }
        System.out.println("Read File Success");
        TraceMap = new int[mat_size];
        step=1;
        
        // Fill Missing box
        for(i=1;i<mat_size;i++){
            if(mat1[i] == null){
                mat1[i] = new int[]{4,0,0,0};
            }
        }
        
        
        int ptr,tmp_ptr=0,tmp_cur;
        ptrTM = 0;
        
        for(i=1;i<mat_size;i++){
            //if(mat1[i] != null) {
                if (mat1[i][0] < mat1[i].length) {
                    //tmprow = mat1[i];
                    ptr = i;
                    TraceMap[ptrTM++] = ptr;
                    while (ptrTM > 0) {
                        if (mat1[ptr][0] < mat1[ptr].length) {
                            if (mat1[ptr][1] <= 0) { // FW
                                mat1[ptr][1] = step++; // Add Step
                                // }else{ // Do nothing

                            }
                            tmp_cur = mat1[ptr][0];
                            while(tmp_cur < mat1[ptr].length){
                                tmp_ptr = mat1[ptr][tmp_cur];
                                if(mat1[tmp_ptr][1] <= 0)
                                    break;
                                tmp_cur++;
                            }
                            mat1[ptr][0] = tmp_cur; // Change cursur in row
                            if(tmp_cur < mat1[ptr].length){
                                mat1[ptr][0]++;
                                TraceMap[ptrTM++] = tmp_ptr; // Update Trace
                                ptr = tmp_ptr; // Change pointer
                            }
                            //System.out.println(ptr);

                        } else {
                            if (mat1[ptr][1] <= 0) { // Leaf Node
                                mat1[ptr][1] = step++;
                                mat1[ptr][2] = step++;
                                seq[seq_idx--] = ptr;
                                ptr = TraceMap[--ptrTM];
                            } else if (mat1[ptr][2] <= 0) { //Back Ward
                                mat1[ptr][2] = step++;
                                seq[seq_idx--] = ptr;
                                ptr = TraceMap[--ptrTM];
                        // }else{ // Its visited node
                                // Do nothing
                            }
                        }
                    }
                } else {
                    // Single?
                    if (mat1[i][1] == 0) {
                        mat1[i][1] = step++;
                        mat1[i][2] = step++;
                        seq[seq_idx--] = i;
                    }
                }
            //}
        }
        System.out.println(seq_idx);
        seq_idx++;
        System.out.println("Finished 1st Trace...");
        step = 1;
        ptrTM = 0;
        gr = 1;
        while((i=getMaxIdx()) >= 0){
            //if(mat1[i] != null) {
                if (mat2[i].get(0) < mat2[i].size()) {
                    //tmprow = mat1[i];
                    ptr = i;
                    TraceMap[ptrTM++] = ptr;
                    while (ptrTM > 0) {
                        if (mat2[ptr].get(0) < mat2[ptr].size()) {
                            if (mat2[ptr].get(1) <= 0) { // FW
                                mat2[ptr].set(1, step++); // Add Step
                                // }else{ // Do nothing

                            }
                            tmp_cur = mat2[ptr].get(0);
                            while(tmp_cur < mat2[ptr].size()){
                                tmp_ptr = mat2[ptr].get(tmp_cur);
                                if(mat2[tmp_ptr].get(1) <= 0)
                                    break;
                                tmp_cur++;
                            }
                            mat2[ptr].set(0, tmp_cur); // Change cursur in row
                            if(tmp_cur < mat2[ptr].size()){
                                mat2[ptr].set(0, tmp_cur + 1);
                                TraceMap[ptrTM++] = tmp_ptr; // Update Trace
                                ptr = tmp_ptr; // Change pointer
                            }
                            //System.out.println(ptr);

                        } else { // Back
                            if (mat2[ptr].get(1) <= 0) { // Leaf Node
                                mat2[ptr].set(1, step++);
                                mat2[ptr].set(2, step++);
                            } else if (mat1[ptr][2] <= 0) { //Back Ward
                                mat2[ptr].set(2, step++);
                        // }else{ // Its visited node
                                // Do nothing
                            }
                            mat1[ptr][3] = gr;
                            ptr = TraceMap[--ptrTM];
                        }
                    }
                } else {
                    // Single?
                    if (mat2[i].get(1) == 0) {
                        mat2[i].set(1,step++);
                        mat2[i].set(2,step++);
                    }
                    mat1[i][3] = gr;
                }
                gr++;
            //}
        }
        
        
        System.out.println("Finished 2nd Trace...");
        gr--;
        System.out.println("Group no: " + gr);
        System.out.println("Writing Result...");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(strOutput))) {
            for(i=1;i<mat_size;i++){
                if(mat1[i] != null){
                    bw.write(i + ":" + mat1[i][3] + "\n");
                }
            }
        }
        System.out.println("Success.");
    }
}
