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
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author wiwat
 */
public class SCCDynamic {
    // Status 4 = inlink, 8 = outlink, 1 = Trendril (Point from IN) , 2 = Trendril (Point to OUT) , 3 = Tube
    public static final byte INLINK = 4;
    public static final byte OUTLINK = 8;
    public static final byte ITRENDRIL = 1;
    public static final byte OTRENDRIL = 2;
    public static final byte TUBE = 3;
    public static final byte CORE = 16;
    public static final byte ISLAND = 0;
    public static final byte ACTIVE = 0;
    public static final byte INACTIVE = -1;
    
    public static final String[] MAP = new String[]{"Isla","TreI","TreO","Tube","In",
                                                         null, null, null, "Out",
                                                         null, null, null, null,
                                                         null, null, null, "Core"};
    
    public int cntTRENDRIL=0, cntTUBE=0, cntCORE=0, cntIN=0, cntOUT=0, cntISLAND=0;
    
    public static class Node{
        int GroupNo;
        int curNodeNo;
        int InSize;
        int OutSize;
        byte Status;
        ArrayList<Node> Link;
        ArrayList<Node> LinkReverse;
        Node Back;
        
        public Node(){
            Link = new ArrayList<>();
            LinkReverse = new ArrayList<>();
            GroupNo = 0;
            curNodeNo = 0;
            Status = INACTIVE;
            //Back = null;
        }
    }
    
    public int NodeSize = 0;
    public int GroupNo = 1;
    public int GroupCnt;
    public int MaxSizeGroupNo;
    public int MaxSize;
    public ArrayList<Node> Graph = new ArrayList<>();
    public ArrayList<Integer> GroupSize = new ArrayList<>();
    public Stack<Node> LastAccess = new Stack<>();
    //public Stack<Node> StackList = new Stack<>();
    
    public void MarkOutLink(Node N){
        Node n;
        N.Back = null;
        while (true){
            if(N.curNodeNo < N.Link.size()){
                N.Status = OUTLINK;
                n = N.Link.get(N.curNodeNo++);
                if(n.Status == ISLAND){
                    n.Back = N;
                    N = n;
                }
            }else{
                N = N.Back;
                if(N == null)
                    break;
            }
        }
    }
    
    public void MarkInLink(Node N){
        Node n;
        N.Back = null;
        while (true){
            if(N.curNodeNo < N.LinkReverse.size()){
                N.Status = INLINK;
                n = N.LinkReverse.get(N.curNodeNo++);
                //if(n.visited != MaxSizeGroupNo){ // Never not Happen
                if(n.Status == ISLAND){
                    n.Back = N;
                    N = n;
                }
            }else{
                N = N.Back;
                if(N == null)
                    break;
            }
        }
    }
    
    public void MakeTrendrilOut(Node N){
        Node n;
        N.Back = null;
        N.Status = OTRENDRIL;
        while (true){
            if(N.curNodeNo < N.LinkReverse.size()){
                n = N.LinkReverse.get(N.curNodeNo++);
                if(n.Status == ISLAND){
                    n.Back = N;
                    N = n;
                    n.Status = OTRENDRIL;
                }else if(n.Status == INLINK && N.Back != null){
                    N.Status = TUBE;
                    N.Back.Status = TUBE;
                }
            }else{
                if(N.Back == null){
                    break;
                }else{
                    if(N.Status == TUBE)
                        N.Back.Status = TUBE;
                    N = N.Back;
                }
            }
        }
    }
    
    public void MakeTrendrilIn(Node N){
        Node n;
        N.Back = null;
        N.Status = ITRENDRIL;
        while (true){
            if(N.curNodeNo < N.Link.size()){
                n = N.Link.get(N.curNodeNo++);
                if(n.Status == ISLAND){
                    n.Back = N;
                    N = n;
                    n.Status = ITRENDRIL;
                }else if(n.Status == OUTLINK && N.Back != null){
                    N.Status = TUBE;
                    N.Back.Status = TUBE;
                }
            }else{
                if(N.Back == null){
                    break;
                }else{
                    if(N.Status == TUBE)
                        N.Back.Status = TUBE;
                    N = N.Back;
                }
            }
        }
    }
    
    public void FindTubeTendril(){
        for(Node N : Graph){
            switch(N.Status){
                case OUTLINK:
                    for (Node n : N.LinkReverse){
                        if(n.Status == ISLAND)
                            MakeTrendrilOut(n);
                    }
                    break;
                case INLINK:
                    for (Node n : N.Link){
                        if(n.Status == ISLAND)
                            MakeTrendrilIn(n);
                    }
                    break;
            }
        }
    }
    
    public void FindInOutLinkStatus(){
        for(Node N : Graph)
            N.curNodeNo = 0;
        
        for(Node N : Graph){
            if(N.GroupNo == MaxSizeGroupNo){
                N.Status = CORE;
                // In Link
                for(Node n : N.LinkReverse){
                    if(n.Status == ACTIVE && n.GroupNo != MaxSizeGroupNo)
                        MarkInLink(n);
                }
                
                // Out Link
                for(Node n : N.Link){
                    if(n.Status == ACTIVE && n.GroupNo != MaxSizeGroupNo)
                        MarkOutLink(n);
                }
            }
        }
    }
    
    public void FindBowTieAfterSCC(){
        FindInOutLinkStatus();
        FindTubeTendril();
        cntTRENDRIL=0;cntTUBE=0;cntCORE=0;cntIN=0;cntOUT=0;cntISLAND=0;
        for(Node n : Graph){
            switch (n.Status){
                case ITRENDRIL: case OTRENDRIL:
                    cntTRENDRIL++;
                    break;
                case TUBE:
                    cntTUBE++;
                    break;
                case CORE:
                    cntCORE++;
                    break;
                case OUTLINK:
                    cntOUT++;
                    break;
                case INLINK:
                    cntIN++;
                    break;
                case ISLAND:
                    cntISLAND++;
                    break;
            }
        }
        System.out.println("CORE: " + cntCORE);
        System.out.println("IN: " + cntIN);
        System.out.println("OUT: " + cntOUT);
        System.out.println("Trendril: " + cntTRENDRIL);
        System.out.println("Tube: " + cntTUBE);
        System.out.println("Island: " + cntISLAND);
    }
    
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
        N.Back = null;
        if(N.GroupNo == 0) {
            N.GroupNo = -1;
            while (true) {
                if (N.curNodeNo < N.Link.size()) {
                    n = N.Link.get(N.curNodeNo);
                    N.curNodeNo++;
                    if(n.Status != INACTIVE && n.GroupNo == 0){
                        n.GroupNo = -1;
                        n.Back = N;
                        N = n;
                    }
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
        N.Back = null;
        if(N.GroupNo == -1) {
            N.GroupNo = GroupNo;
            GroupCnt = 1;
            while (true) {
                if (N.curNodeNo < N.LinkReverse.size()) {
                    n = N.LinkReverse.get(N.curNodeNo++);
                    if(n.Status != INACTIVE && n.GroupNo == -1){
                        GroupCnt++;
                        n.GroupNo = GroupNo;
                        n.Back = N;
                        N = n;
                    }
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
     * @param sec_sep Separated 2nd string
     */
    public void ImportCSV(String FileName, String sep, String sec_sep){
        ImportCSV(new File(FileName), sep, sec_sep);
    }
    
    /**
     * Import CSV File<br />
     * in format: <br />
     * <b>SRCn&lt;sep&gt;DSTn1&lt;sep&gt;DSTn2...</b> 
     *  
     * @param file CSV File
     * @param sep Separated string
     * @param sec_sep Separated 2nd string
     */
    public void ImportCSV(File file, String sep, String sec_sep){
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            String Line;
            String[] strs,subs;
            int src,dst,i,tmpo;
            Node NSrc,NDst;
            while((Line = br.readLine()) != null){
                strs = Line.split(sep);
                src = Integer.parseInt(strs[0]);
                while(NodeSize <= src){
                    Graph.add(new Node());
                    NodeSize++;
                }
                NSrc = Graph.get(src);
                NSrc.Status = ACTIVE;
                for (i = 1; i < strs.length; i++) {
                    subs = strs[i].split(sec_sep);
                    dst = Integer.parseInt(subs[0]);
                    
                    while(NodeSize <= dst){
                        Graph.add(new Node());
                        NodeSize++;
                    }
                    
                    // Linker
                    NDst = Graph.get(dst);
                    NSrc.Link.add(NDst);
                    NDst.LinkReverse.add(NSrc);
                    
                    if(subs.length > 1){
                        tmpo = Integer.parseInt(subs[1]);
                        NSrc.OutSize += tmpo;
                        NDst.InSize += tmpo;
                    }else{
                        NSrc.OutSize ++;
                        NDst.InSize ++;
                    }
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
            int src,dst,i,tmpo;
            Node NSrc,NDst;
            while((Line = br.readLine()) != null) {
                strs = Line.split(sep);
                src = Integer.parseInt(strs[0]);
                while (NodeSize <= src) {
                    Graph.add(new Node());
                    NodeSize++;
                }
                NSrc = Graph.get(src);
                NSrc.Status = ACTIVE;
                dst = Integer.parseInt(strs[1]);
                while (NodeSize <= dst) {
                    Graph.add(new Node());
                    NodeSize++;
                }
                NDst = Graph.get(dst);
                NSrc.Link.add(NDst);
                NDst.LinkReverse.add(NSrc);
                if(strs.length >= 3){
                    tmpo = Integer.parseInt(strs[2]);
                    NSrc.OutSize += tmpo;
                    NDst.InSize += tmpo;
                }else{
                    NSrc.OutSize++;
                    NDst.InSize++;
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
            if(n.Status == ACTIVE)
                TraverseGraph(n);
        }
        Node n;
        
        System.out.println("Reverse Traverse...");
        while(!LastAccess.empty()){
            n = LastAccess.pop();
            if(n.GroupNo == -1){
                GroupCnt = 0;
                TraverseGraphReverse(n);
                GroupSize.add(GroupCnt);
                if(GroupCnt > MaxSize){
                    MaxSize = GroupCnt;
                    MaxSizeGroupNo = GroupNo;
                }
                    
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
            bw.write("id:Group:InDegree:OutDegree:BowtieStatus\n");
            Node n;
            for(int i=0;i<Graph.size();i++){
                n = Graph.get(i);
                
                if(n.Status != INACTIVE){
                    bw.write(i + ":" + n.GroupNo + ":" + n.InSize + ":" + n.OutSize + ":" + MAP[n.Status]  + "\n");
                }
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
            bw.write("...SCC sumary..." + "\n");
            bw.write("Component = " + (this.GroupNo-1) + "\n") ;
            bw.write("MaxGroupSize = " + (this.MaxSize) + "\n") ;
            bw.write("MaxGroupID = " + (this.MaxSizeGroupNo) + "\n") ;
            bw.write("...Bowtie sumary..." + "\n");
            bw.write("CORE = " + cntCORE + "\n");
            bw.write("IN = " + cntIN + "\n");
            bw.write("OUT = " + cntOUT + "\n");
            bw.write("Trendril = " + cntTRENDRIL + "\n");
            bw.write("Tube = " + cntTUBE + "\n");
            bw.write("Island = " + cntISLAND + "\n");
            bw.write("...Group size dist..." + "\n");
            bw.write("GroupID:GroupSize" + "\n");
            int n;
            for(int i=0;i<GroupSize.size();i++){
                bw.write((i+1) + ":" + GroupSize.get(i) + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(SCCDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Deprecated
    /**
     * Write scc info
     * @param file Output File
     */
    public void WriteInOutLink(File file){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))){
            Node n;
            for(int i=0;i<NodeSize;i++){
                n = Graph.get(i);
                bw.write(i + ":" + n.InSize + ":" + n.OutSize + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(SCCDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void main(String[] args){
        // SET PROPERTIES
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("file.encoding", "UTF-8");
        
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Analyse.SCCDynamic").defaultHelp(true)
                .description("Determine SCC and BowTie model");
        parser.addArgument("-d")
                .dest("delim")
                .metavar("DELIM")
                .type(String.class)
                .setDefault(";")
                .help("Node delimiter separate");
        parser.addArgument("-sd")
                .dest("sub_delim")
                .metavar("SUB_DELIM")
                .type(String.class)
                .setDefault(":")
                .help("Sub delimiter separated between dest and weight");
        parser.addArgument("-o")
                .dest("file_out")
                .metavar("FILE")
                .type(String.class)
                .help("Output filename")
                .required(true);
        parser.addArgument("FILE_GRAPH")
                .dest("file_graph")
                .nargs("+")
                .type(String.class)
                .help("Graph file, each line format: SOURCE;d1:w1;d2:w2; ... ;dn:wn");
        
        try {
            Namespace res = parser.parseArgs(args);
            String FileInfo = res.getString("file_out") + ".info";
            
            SCCDynamic scc = new SCCDynamic();
            System.out.println("Reading Graph...");
            for (String strFile : (List<String>) res.get("file_graph")) {
                System.out.println("Reading " + strFile + " ...");
                scc.ImportCSV(strFile, res.getString("delim"), res.getString("sub_delim"));
            }
            System.out.println("Read Graph Finished");
            
            //System.out.println("Filtering node...");
            //PageRank.filterNode(linkedList);
            //System.out.println("Filtering node finished");
            
            System.out.println("Computing...");
            scc.Compute();
            scc.FindBowTieAfterSCC();
            System.out.println("Writting INFo...");
            scc.WriteInfo(FileInfo);
            System.out.println("Writting Map...");
            scc.WriteMap(res.getString("file_out"));
            System.out.println("=== Success ===");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
    }
}
