/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ContentClassifier;

import ArcFileUtils.MalletArcWebIterator;
import cc.mallet.classify.*;
import cc.mallet.types.Instance;
import cc.mallet.types.Labeling;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public final class MalletClassifier {
    public static String StrDirIn = "data/converted";
    public static String StrFileClassifier = "resource/TH.naive.class";
    public static String StrFileWebpage = "data/category/converted.web";
    public static String StrFileWebsite = "data/category/converted.host";
    
    public BufferedWriter bwWebpage;
    public BufferedWriter bwWebsite;
    public Classifier classifier;
    
    public SQLiteConnection db ;//= new SQLiteConnection(DBDriver.TableConfig.FileWebPageDB);
    public HashSet<String> SkipFileList = new HashSet<>();
    
    public MalletClassifier(Classifier classifier, File WebPage, File WebSite) throws IOException{
        this.bwWebpage = new BufferedWriter(new FileWriter(WebPage));
        this.bwWebsite = new BufferedWriter(new FileWriter(WebSite));
        this.classifier = classifier;
    }
    
    public MalletClassifier(File classifier, File WebPage, File WebSite) throws IOException, FileNotFoundException, ClassNotFoundException{
        if(WebSite.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(WebSite))) {
                String Line;
                String[] strs;
                while ((Line = br.readLine()) != null) {
                    strs = Line.split(" ", 2);
                    if (strs.length > 1) {
                        SkipFileList.add(strs[1]);
                    }
                }
            }
        }
        this.bwWebpage = new BufferedWriter(new FileWriter(WebPage));
        this.bwWebsite = new BufferedWriter(new FileWriter(WebSite, true));
        this.classifier = MalletUtils.loadClassifier(classifier);
    }
    
    public void Classified(File file) throws IOException{
        if(file.isDirectory()){
            ClassifiedDir(file);
        }else{
            ClassifiedFile(file);
        }
    }
    
    public void ClassifiedFile(File file) throws IOException {
        int [] sum = new int[classifier.getLabelAlphabet().toArray().length];
        
        if (!SkipFileList.contains(file.getName())) {

            MalletArcWebIterator reader = new MalletArcWebIterator(file);

            Iterator<Instance> instances = classifier.getInstancePipe().newIteratorFrom(reader);
            Instance ins;
            while (instances.hasNext()) {
                ins = instances.next();
                Labeling labeling = classifier.classify(ins).getLabeling();

                // print the labels with their weights in descending order (ie best first)
                sum[labeling.getBestIndex()]++;
                //System.out.println(labeling.getBestLabel()+" "+ins.getName());
                bwWebpage.write(labeling.getLabelAtRank(0) + " " + ins.getName().toString() + "\n");
                //System.out.println(labeling.getLabelAtRank(0)+" "+reader.AR.Record.URL.replaceAll("\"", "\"\"") );
            }
            reader.remove();
            int max = 0;
            for (int i = 0; i < sum.length; i++) {
                if (sum[i] > sum[max]) {
                    max = i;
                }
            }
            bwWebsite.write(classifier.getLabelAlphabet().lookupLabel(max) + " " + file.getName() + "\n");
            bwWebpage.flush();
            bwWebsite.flush();
        }
        
    }
    
    public void ClassifiedDir(File dir) throws IOException{
        for(File f : dir.listFiles()){
            if(f.isDirectory())
                ClassifiedDir(f);
            else
                ClassifiedFile(f);
        }
    }
    
    public void saveSQL(File file){
        try {
            db.open();
            if(file.isDirectory()){
                saveSQLDir(file);
            }else{
                saveSQLFile(file);
            }
        } catch (SQLiteException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            db.dispose();
        }
        
    }
    
    private void saveSQLFile(File file){
        try {
            System.out.println(file.getName());
            MalletArcWebIterator reader = new MalletArcWebIterator(file);
            
            Iterator<Instance> instances = classifier.getInstancePipe().newIteratorFrom(reader);
            Instance ins;
            
            db.exec("BEGIN;");
            while (instances.hasNext()) {
                ins = instances.next();
                Labeling labeling = classifier.classify(ins).getLabeling();
                //System.out.println(labeling.getLabelAtRank(0)+" "+ins.getName().toString().replaceAll("\"", "\"\"") );
                db.exec("UPDATE webpage SET category=\"" + labeling.getLabelAtRank(0) + "\" WHERE url=\"" + ins.getName().toString().replaceAll("\"", "\"\"") + "\";");
                
            }
            db.exec("COMMIT;");
            reader.remove();
            
        } catch (IOException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLiteException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void saveSQLDir(File file){
        for(File f : file.listFiles()){
            if(f.isDirectory())
                saveSQLDir(f);
            else
                saveSQLFile(f);
        }
    }
    
    
    public void close(){
        try {
            bwWebsite.close();
            bwWebpage.close();
        } catch (IOException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void main(String[] args){
        MalletClassifier MC = null;
        //HashMap<String,String> Args = ArgUtils.Parse(args);
        /*
        StrFileIn = Args.get("i");
        StrFileOut = Args.get("o");
        StrFileSummary = Args.get("s");
        StrFileClassifier = Args.get("c");
        */ 
        //StrFileIn = args.length > 0 ? args[0] : "data/crawldata";
        StrDirIn = args.length > 0 ? args[0] : StrDirIn;
        StrFileClassifier = args.length > 1 ? args[1] : StrFileClassifier;
        StrFileWebpage = args.length > 2 ? args[2] : StrFileWebpage;
        StrFileWebsite = args.length > 3 ? args[3] : StrFileWebsite;
        
        try {        
            MC = new MalletClassifier(new File(StrFileClassifier) , new File(StrFileWebpage), new File(StrFileWebsite));
            
            MC.classifier = MalletUtils.loadClassifier(new File(StrFileClassifier));
            MC.Classified(new File(StrDirIn));
            MC.close();
            
        } catch ( IOException | ClassNotFoundException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex){
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if(MC != null)
                MC.close();
        }
        
    }
    
}
