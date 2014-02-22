/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class Robotstxt {
    
    private final RobotstxtAgent RA;
    public String Host;
    public String UserAgent;
    
    public Robotstxt(String Host, String UserAgent){
        this.Host = Host;
        this.UserAgent = UserAgent;
        RA = new RobotstxtAgent(UserAgent);
    }
    
    public void AnalyseRobots(String Content){
        boolean isInScope = false;
        String LastMatchPattern = "";
        String[] Lines = Content.split("\n");
        for (String Line : Lines) {
            String[] cols = Line.split(":", 2);
            if(cols.length == 2){
                cols[0] = cols[0].trim();
                cols[1] = cols[1].trim();
                if(cols[1].length() > 0){
                    if(cols[0].equals("User-agent")){
                        if(LastMatchPattern.length() < cols[1].length() && 
                                UserAgent.matches(cols[1].replaceAll("\\?","\\\\?")
                                        .replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*").replaceAll("\\+", ".+"))){
                            LastMatchPattern = cols[1];
                            RA.Clear();
                            isInScope = true;
                        }else{
                            isInScope = false;
                        }
                    }else if(isInScope){
                        switch (cols[0]) {
                            case "Disallow":
                                RA.AddPattern(PatternToRegex(cols[1]),false);
                                break;
                            case "Allow":
                                RA.AddPattern(PatternToRegex(cols[1]),true);
                                break;
                        }
                    }
                }
            }
        }
    }
    
    public boolean isAllowPath(String Path){
        return RA.isAllow(Path);
    }
    
    private String PatternToRegex(String Pattern) {
        if (!Pattern.endsWith("$")) {
            return Pattern.replaceAll("\\?", "\\\\?").replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*").replaceAll("\\+", ".+") + ".*";
        } else {
            return Pattern.replaceAll("\\?", "\\\\?").replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*").replaceAll("\\+", ".+");
        }
    }
    
    public class RobotstxtAgent{
        public int length = 0;
        public ArrayList<String> SortedPattern = new ArrayList<>();
        public ArrayList<Boolean> SortedAllowP = new ArrayList<>();
        public String UserAgent;
        
        
        
        public RobotstxtAgent(String UserAgent){
            this.UserAgent = UserAgent;
        }
        
        public void Clear(){
            SortedAllowP.clear();
            SortedPattern.clear();
            length = 0;
        }
        
        /*
        
        public void AddRule(String Line){
            String[] cols = Line.split(":",2);
            if(cols.length == 2){
                cols[0] = cols[0].trim();
                cols[1] = cols[1].trim();
                if(cols[1].length() > 0){
                    if(cols[0].equals("Disallow")){
                        AddPattern(PatternToRegex(cols[1]),false);
                    }else if(cols[0].equals("Allow")){
                        AddPattern(PatternToRegex(cols[1]),true);
                    }
                }
            }
        }
        */
        
        public void AddPattern(String RegexPattern, Boolean isAllow){
            int i=0;
            int RLENGTH = RegexPattern.length();
            while(i < this.length && this.SortedPattern.get(i).length() > RLENGTH){
                i++;
            }
            this.SortedPattern.add(i,RegexPattern);
            this.SortedAllowP.add(i,isAllow);
            this.length++;
        }
        
        public boolean isAllow(String Path){
            for(int i=0; i < length; i++){
                if(Path.matches(SortedPattern.get(i)))
                    return SortedAllowP.get(i);
            }
            return true;
        }
    }
    
    public static void main(String[] args){
        BufferedReader br = null;
        try { 
            br = new BufferedReader(new FileReader("googlebots.txt"));
            String Line,Content="";
            Robotstxt R = new Robotstxt("google.com", "malang");
            while((Line = br.readLine()) != null){
                Content += Line + "\n";
            }
            R.AnalyseRobots(Content);
            System.out.println(R.isAllowPath("/search/"));
            System.out.println(R.isAllowPath("/catalogs"));
            System.out.println(R.isAllowPath("/catalogs/abae"));
            System.out.println(R.isAllowPath("/catalogs/about/"));
            System.out.println(R.isAllowPath("/books?zxxx&q=xxss"));
            System.out.println(R.isAllowPath("/books?zxxx&q=related:s"));
            //for(String s : R.RA.SortedPattern){
            //    System.out.println(s);
            //}
        } catch (IOException ex) {
            Logger.getLogger(Robotstxt.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(Robotstxt.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
