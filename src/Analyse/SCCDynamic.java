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
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiwat
 */
public class SCCDynamic {
    
    public static class Node{
        int visited;
        ArrayList<Node> Link;
        ArrayList<Node> LinkReverse;
        
        public Node(){
            Link = new ArrayList<>();
            LinkReverse = new ArrayList<>();
            visited = 0;
        }
    }
    
    public int NodeSize = 0;
    public int GroupNo = 1;
    public int GroupCnt;
    public ArrayList<Node> Graph = new ArrayList<>();
    public ArrayList<Integer> GroupSize = new ArrayList<>();
    public Stack<Node> LastAccess = new Stack<>();
    
    public void TraverseGraph(Node N){
        if(N.visited == 0){
            N.visited = -1;
            for(Node n : N.Link){
                TraverseGraph(n);
            }
            LastAccess.push(N);
        }
    }
    
    public void TraverseGraphReverse(Node N){
        if(N.visited == -1){
            N.visited = GroupNo;
            GroupCnt++;
            for(Node n : N.LinkReverse){
                TraverseGraphReverse(n);
            }
        }
    }
    
    /**
     * Import CSV File
     * in format: <b>SRCn&lt;sep&gt;DSTn1&lt;sep&gt;DSTn2...</b> 
     *  
     * @param FileName CSV File Path
     * @param sep Separated string
     */
    public void ImportCSV(String FileName, String sep){
        ImportCSV(new File(FileName),sep);
    }
    
    /**
     * Import CSV File
     * in format: <b>SRCn&lt;sep&gt;DSTn1&lt;sep&gt;DSTn2...</b> 
     *  
     * @param file CSV File
     * @param sep Separated string
     */
    public void ImportCSV(File file,String sep){
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String Line;
            String[] strs;
            int src,dst,i;
            Node NSrc,NDst;
            while((Line = br.readLine()) != null){
                strs = Line.split(";");
                src = Integer.parseInt(strs[0]);
                while(NodeSize <= src){
                    Graph.add(new Node());
                    NodeSize++;
                }
                NSrc = Graph.get(src);
                for (i = 1; i < strs.length; i++) {
                    dst = Integer.parseInt(strs[i]);
                    while(NodeSize <= dst){
                        Graph.add(new Node());
                        NodeSize++;
                    }
                    NDst = Graph.get(dst);
                    NSrc.Link.add(NDst);
                    NDst.LinkReverse.add(NSrc);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SCCDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SCCDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void Compute(){
        for(Node n : Graph){
            TraverseGraph(n);
        }
        Node n;
        
        System.out.println("Reverse Traverse...");
        while(!LastAccess.empty()){
            n = LastAccess.pop();
            if(n.visited == -1){
                GroupCnt = 0;
                TraverseGraphReverse(n);
                GroupSize.add(GroupCnt);
                GroupNo++;
            }
        }
    }
    
    /**
     * Write output in format <b>NodeID:SccNo</b> 
     * @param FileName Output Path
     */
    public void WriteMap(String FileName){
        WriteMap(new File(FileName));
    }
    
    /**
     * Write output in format <b>NodeID:SccNo</b> 
     * @param file Output File
     */
    public void WriteMap(File file){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))){
            Node n;
            for(int i=0;i<Graph.size();i++){
                n = Graph.get(i);
                bw.write(i + ":" + n.visited + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(SCCDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Write scc info
     * @param FileName Output Path
     */
    public void WriteInfo(String FileName){
        WriteInfo(new File(FileName));
    }
    
    
    /**
     * Write scc info
     * @param file Output File
     */
    public void WriteInfo(File file){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))){
            bw.write("Component:" + this.GroupNo);
            int n;
            for(int i=0;i<GroupSize.size();i++){
                bw.write((i+1) + ":" + GroupSize.get(i) + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(SCCDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void main(String[] args){
        String FileInName = args.length > 0 ? args[0] : "data/graph.result.webpage";
        String FileOutName = args.length > 1 ? args[1] : "data/scc.result.webpage";
        String FileInfo = FileOutName + ".info";
        SCCDynamic scc = new SCCDynamic();
        System.out.println("Importing...");
        scc.ImportCSV(FileInName, ";");
        int i =1;
        int inl=0,outl=0;
        
        for(Node n : scc.Graph){
            inl += n.LinkReverse.size();
            outl += n.Link.size();
            System.err.println(i++ + ":" + n.Link.size() + ":" + n.LinkReverse.size());
        }
        System.out.println("In: " + inl + ", Out: " + outl);
        System.out.println("Computing...");
        scc.Compute();
        System.out.println("Writting...");
        scc.WriteMap(FileOutName);
        scc.WriteInfo(FileInfo);
    }
}
