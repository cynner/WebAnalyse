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
        int curNodeNo;
        ArrayList<Node> Link;
        ArrayList<Node> LinkReverse;
        Node Back;
        
        public Node(){
            Link = new ArrayList<>();
            LinkReverse = new ArrayList<>();
            visited = 0;
            curNodeNo = 0;
            Back = null;
        }
    }
    
    public int NodeSize = 0;
    public int GroupNo = 1;
    public int GroupCnt;
    public ArrayList<Node> Graph = new ArrayList<>();
    public ArrayList<Integer> GroupSize = new ArrayList<>();
    public Stack<Node> LastAccess = new Stack<>();
    public Stack<Node> StackList = new Stack<>();
    
    public void TraverseGraph(Node N){
        /*
        if(N.visited == 0){
            N.visited = -1;
            for(Node n : N.Link){
                TraverseGraph(n);
            }
            LastAccess.push(N);
        }
        */
        Node n;
        if(N.visited == 0) {
            N.visited = -1;
            while (true) {
                if (N.curNodeNo < N.Link.size()) {
                    n = N.Link.get(N.curNodeNo);
                    N.curNodeNo++;
                    if(n.visited != 0)
                        continue;
                    else 
                        n.visited = -1;
                    n.Back = N;
                    N = n;
                } else {
                    LastAccess.push(N);
                    N.curNodeNo = 0;
                    if(N.Back == null)
                        break;
                    else
                        N = N.Back;
                }
            }
        }
    }
    
    public void TraverseGraphReverse(Node N){
        /*
        if(N.visited == -1){
            N.visited = GroupNo;
            GroupCnt++;
            for(Node n : N.LinkReverse){
                TraverseGraphReverse(n);
            }
        }
        */
        Node n;
        if(N.visited == -1) {
            N.visited = GroupNo;
            GroupCnt = 1;
            while (true) {
                if (N.curNodeNo < N.LinkReverse.size()) {
                    n = N.LinkReverse.get(N.curNodeNo);
                    N.curNodeNo++;
                    if(n.visited != -1)
                        continue;
                    else {
                        n.visited = GroupNo;
                        GroupCnt++;
                    }
                    n.Back = N;
                    N = n;
                } else {
                    if(N.Back == null)
                        break;
                    else
                        N = N.Back;
                }
            }
        }
        
    }
    
    /**
     * Import CSV File<br/>
     * in format: <br/>
     * <b>SRCn&lt;sep&gt;DSTn1&lt;sep&gt;DSTn2...</b> 
     *  
     * @param FileName CSV File Path
     * @param sep Separated string
     */
    public void ImportCSV(String FileName, String sep){
        ImportCSV(new File(FileName),sep);
    }
    
    /**
     * Import CSV File<br />
     * in format: <br />
     * <b>SRCn&lt;sep&gt;DSTn1&lt;sep&gt;DSTn2...</b> 
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
                strs = Line.split(sep);
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
    
    
    /**
     * Import Src Dst File<br/>
     * in format: <b><br/>
     * SRC1&lt;sep&gt;DST1<br/>
     * SRC2&lt;sep&gt;DST2<br/>
     * ...</b> 
     *  
     * @param FileName SD File Path
     * @param sep Separated string
     */
    public void ImportSD(String FileName, String sep){
        ImportSD(new File(FileName),sep);
    }
    
     /**
     * Import Src Dst File<br/>
     * in format: <b><br/>
     * SRC1&lt;sep&gt;DST1<br/>
     * SRC2&lt;sep&gt;DST2<br/>
     * ...</b> 
     *  
     * @param file SD File
     * @param sep Separated string
     */
    public void ImportSD(File file,String sep){
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String Line;
            String[] strs;
            int src,dst,i;
            Node NSrc,NDst;
            while((Line = br.readLine()) != null) {
                strs = Line.split(sep);
                src = Integer.parseInt(strs[0]);
                while (NodeSize <= src) {
                    Graph.add(new Node());
                    NodeSize++;
                }
                NSrc = Graph.get(src);
                dst = Integer.parseInt(strs[1]);
                while (NodeSize <= dst) {
                    Graph.add(new Node());
                    NodeSize++;
                }
                NDst = Graph.get(dst);
                NSrc.Link.add(NDst);
                NDst.LinkReverse.add(NSrc);
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
            bw.write("Component:" + (this.GroupNo-1) + "\n") ;
            int n;
            for(int i=0;i<GroupSize.size();i++){
                bw.write((i+1) + ":" + GroupSize.get(i) + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(SCCDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void main(String[] args){
        //Mode has sd or csv mode
        String Mode = args.length > 0 ? args[0] : "sd";
        String FileInName = args.length > 1 ? args[1] : "web-Stanford.txt";
        String FileOutName = args.length > 2 ? args[2] : "data/testscc.result";
        String FileInfo = FileOutName + ".info";
        SCCDynamic scc = new SCCDynamic();
        
        System.out.println("Importing...");
        switch(Mode.toLowerCase()){
            case "csv":
                scc.ImportCSV(FileInName, ";");
                break;
            case "sd":
                scc.ImportSD(FileInName, ";");
                break;
            default:
                System.err.println("ERROR: No Input Type '" + Mode + "' in {csv,sd}." );
                System.exit(1);
                break;
        }
        System.out.println("Computing...");
        scc.Compute();
        System.out.println("Writting...");
        scc.WriteMap(FileOutName);
        scc.WriteInfo(FileInfo);
    }
}
