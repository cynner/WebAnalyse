/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class ProjectTester {

    public static int ThreadNo = 10;
    public static int WorkBufferSize = 20;
    // System.getProperty("user.dir")
    // getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
    // Profile
    public static String hostName;
    public static boolean SIGINT = false;
    public static boolean SIGTERM = false;
    // --------
    public static String jobName = null;
    public static String baseDir = null;
    public static String inputFile = null;
    public static String inputDir = null;
    // IN baseDIR --------------------------------------------------------------
    // IN outputDir
    public static String outputDir = "output/";
    public static String outputFile = "output.txt";
    // IN stateDir
    public static String stateDir = "state/";
    public static String assignedFile = "assigned";
    public static String assignedPath = stateDir + assignedFile;
    // IN confDir
    public static String confDir = "config/";
    public static String confPath = confDir + "settings.conf";
    public static String keyPath = confDir + "key";

    // END baseDir -------------------------------------------------------------
    
    public static boolean MkConfigMode = false;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //MainRunner.RunCommand(ReadOption(args));
        /*
        // TODO code application logic here
        //System.getenv("USERNAME");
        
        myhandler();
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            Logger.getLogger(ProjectTester.class.getName()).log(Level.SEVERE, null, ex);
        }
        String mode = "";


        if (args.length > 0) {
            mode = args[0];
            baseDir = mode + "/";

            try {
                ReadConf();
            } catch (FileNotFoundException ex) {
                System.err.println("Warning : No config file to read");
            }


            try {
                for (int i = 1; i < args.length; i++) {
                    if (args[i].equals("-i")) {
                        inputFile = args[++i];
                    } else if (args[i].equals("-id")) {
                        inputDir = args[++i];
                    } else if (args[i].equals("-o")) {
                        outputFile = args[++i];
                    } else if (args[i].equals("-od")) {
                        outputDir = args[++i];
                    } else if (args[i].equals("-t")) {
                        ThreadNo = Integer.parseInt(args[++i]);
                        WorkBufferSize = ThreadNo * 2;
                    } else if (args[i].equals("-mkconfig")){
                        MkConfigMode = true;
                    }
                }
                
            } catch (Exception e) {
                System.err.println("Error: Invalid argument.");
                help();
                return;
            }
            if(MkConfigMode){
                MakeConfig();
                return;
            }
        }
        //System.setProperty("user.dir", System.getProperty("user.dir") + "/" + baseDir);
        //System.out.println(System.getProperty("user.dir"));

        if (mode.equals("test")) {
            try {
                //HTMLCompression.Test();
                //ThreadPool.RunHostGraph("arc/", "outHOST/", Thread);
                //ThreadPool.RunExampleThread(10, 20);
                //ThreadPool.RunLinkExtractor("arc/", "out/", Thread);
                //JSoupExample.RunExample("test01.arc");
                //long x = TestRead("key", 5729);
                //TestRead("key", (long)x);
            } catch (Exception ex) {
                Logger.getLogger(ProjectTester.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (mode.equals("readarc")) {
        } else if (mode.equals("languagedetect")) {
        } else if (mode.equals("linkextract")) {
        } else if (mode.equals("reducegraph")) {

            ThreadPool.RunReduceGraph(outputDir, outputDir);

        } else if (mode.equals("hostgraphextract")) {
            ThreadPool.RunHostGraph(inputDir, outputDir);
        } else if (mode.equals("hostextract")) {
            ThreadPool.RunHostExtract();
        } else if (mode.equals("crawler")) {
            NormalizePath();
            ThreadPool.RunCrawler(100);
        } else {
            System.err.println("Error: Invalid mode.");
            help();
            return;
        }
        
        */
        
        

    }

    public static HashMap<String, String> ReadOption (String[] args){
        HashMap<String,String> dictArgs = new HashMap<String, String>();
        dictArgs.put("mode", args[0]);
        int idx=1;
        for(int i=1; i<args.length; i++){
            if(args[i].charAt(0) == '-'){
                dictArgs.put(args[i].substring(1), args[++i]);
            }else{
                dictArgs.put(idx + "", args[i]);
                idx++;
            }
        }
        return dictArgs;
    }
    
    public static void help() {
        String message = "usage: java -jar _\n"
                + "ProjectTest.jar <MODE>  [-i InputFile] [-o OutputFile] [-mkconfig]\n"
                + "                       [-id InputDir] [-od OutputDir] [-t ThreadNo]\n\n"
                + "Mode: readarc, languagedetect, linkextract, hostgraphextract";
        System.out.println(message);
    }

    public static void ReadConf() throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(baseDir + confPath));
        String Line;
        String[] Sp;
        HashMap<String, String> kv = new HashMap<>();
        try {
            while ((Line = br.readLine()) != null) {
                Sp = Line.split("=", 2);
                if (Sp.length == 2) {
                    kv.put(Sp[0], Sp[1]);
                }
            }
            jobName = kv.get("JOBNAME");
            baseDir = kv.get("BASEDIR");
            inputDir = kv.get("INPUTDIR");
            inputFile = kv.get("INPUTFILE");
            outputDir = baseDir + kv.get("OUTPUTDIR");
            outputFile = kv.get("OUTPUTFILE");
            keyPath = baseDir + confDir + kv.get("KEYFILE");
            stateDir = baseDir + kv.get("STATEDIR");
            assignedFile = kv.get("ASSIGNEDFILE");
            assignedPath = stateDir + assignedFile;
        } catch (IOException ex) {
            Logger.getLogger(ProjectTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void WriteConf() {
    }

    public static long TestRead(String Filename, int LineNo) {
        long a = 0;
        try (RandomAccessFile raf = new RandomAccessFile(Filename, "r")) {
            String Line;
            while ((Line = raf.readLine()) != null) {
                if (LineNo == 1) {
                    a = raf.getFilePointer();
                    System.out.println(a);
                    break;
                }
                LineNo--;
            }
            System.out.println(Line);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProjectTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProjectTester.class.getName()).log(Level.SEVERE, null, ex);
        }
        return a;
    }
    
    public static void MakeConfig(){
        File bd = new File(baseDir);
        if(!bd.exists()){
            bd.mkdir();
        }
        File op = new File(baseDir + outputDir);
        if(!op.exists()){
            op.mkdir();
        }
        File st = new File(baseDir + stateDir);
        if(!st.exists()){
            st.mkdir();
        }
        File cf = new File(baseDir + confDir);
        if(!cf.exists()){
            cf.mkdir();
        }
        /*
        File f;
        f = new File(baseDir + confPath);
        try {
            if(!f.exists())
                f.createNewFile();
            
        } catch (IOException ex) {
            Logger.getLogger(ProjectTester.class.getName()).log(Level.SEVERE, null, ex);
        }
        WriteConf();
         * 
         */
    }
    
    public static void NormalizePath(){
        //jobName = kv.get("JOBNAME");
            //baseDir = kv.get("BASEDIR");
            //inputDir = kv.get("INPUTDIR");
            //inputFile = kv.get("INPUTFILE");
            outputDir = baseDir + outputDir;
            //outputFile = kv.get("OUTPUTFILE");
            keyPath = baseDir + confDir + "key";
            stateDir = baseDir + stateDir;
            //assignedFile = kv.get("ASSIGNEDFILE");
            assignedPath = stateDir + assignedFile;
    }

    public static void TestRead(String Filename, long LineNo) {
        try (RandomAccessFile raf = new RandomAccessFile(Filename, "r")) {
                raf.seek(LineNo);
                String Line = raf.readLine();
                System.out.println(Line);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProjectTester.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ProjectTester.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public static void myhandler() {
        /*
        SignalHandler handle;
        handle = Signal.handle(new Signal("INT"), new SignalHandler() {
            
            @Override
            public void handle(Signal sig) {
                System.out.println("Received SIGINT: Process will run only remaining assigned...");
                SIGINT = true;
            }
        });
                */
    }
}
