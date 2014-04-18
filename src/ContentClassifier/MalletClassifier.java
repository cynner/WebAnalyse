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
import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 *
 * @author malang
 */
public final class MalletClassifier {

    //public static String StrDirIn = "data/converted";
    //public static String StrFileClassifier = "resource/TH.naive.class";
    //public static String StrFileWebpage = "data/category/converted.web";
    //public static String StrFileWebsite = "data/category/converted.host";
    public static final String DEFAULT_SUFFIX_SITE = ".site";
    public static final String DEFAULT_SUFFIX_PAGE = ".page";
    //public static final String DEFAULT_FILE_CLASSIFIER = "resource/th.mallet.class";

    public BufferedWriter bwWebpage;
    public BufferedWriter bwWebsite;
    public Classifier classifier;

    public SQLiteConnection db;//= new SQLiteConnection(DBDriver.TableConfig.FileWebPageDB);
    public HashSet<String> SkipFileList = new HashSet<>();

    public MalletClassifier(Classifier classifier, File WebPage, File WebSite) throws IOException {
        this.bwWebpage = new BufferedWriter(new FileWriter(WebPage));
        this.bwWebsite = new BufferedWriter(new FileWriter(WebSite));
        this.classifier = classifier;
    }

    public MalletClassifier(File classifier, File WebPage, File WebSite) throws IOException, FileNotFoundException, ClassNotFoundException {
        if (WebSite.exists()) {
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

    public void Classified(File file) throws IOException {
        if (file.isDirectory()) {
            ClassifiedDir(file);
        } else {
            ClassifiedFile(file);
        }
    }

    public void ClassifiedFile(File file) throws IOException {
        int[] sum = new int[classifier.getLabelAlphabet().toArray().length];

        if (!SkipFileList.contains(file.getName())) {
            System.out.println(file.getName());
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

    public void ClassifiedDir(File dir) throws IOException {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                ClassifiedDir(f);
            } else {
                ClassifiedFile(f);
            }
        }
    }

    /*
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
            
     } catch (IOException | SQLiteException ex) {
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
     */
    public void close() {
        try {
            bwWebsite.close();
            bwWebpage.close();
        } catch (IOException ex) {
            Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        // SET PROPERTIES
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("file.encoding", "UTF-8");

        ArgumentParser parser = ArgumentParsers.newArgumentParser("Analyse.PageRank").defaultHelp(true)
                .description("PageRank calculation from graph file");
        parser.addArgument("-c", "--class")
                .dest("class")
                .metavar("FILE")
                .type(Double.class)
                .help("Classifier file")
                .required(true);
        parser.addArgument("-o", "--out-prefix")
                .dest("out_prefix")
                .metavar("PREFIX")
                .type(String.class)
                .help("Prefix output file name")
                .required(true);
        parser.addArgument("-ss", "--suffix-site")
                .dest("suffix_site")
                .metavar("SUFFIX")
                .type(Double.class)
                .setDefault(DEFAULT_SUFFIX_SITE)
                .help("Suffix output website file name");
        parser.addArgument("-sp", "--suffix-page")
                .dest("suffix_page")
                .metavar("SUFFIX")
                .type(Double.class)
                .setDefault(DEFAULT_SUFFIX_PAGE)
                .help("Suffix output webpage file name");
        parser.addArgument("FILE_IN")
                .dest("file_in")
                .nargs("+")
                .type(String.class)
                .help("Web archives file[s] or directory[s] input");

        MalletClassifier MC = null;
        try {
            Namespace res = parser.parseArgs(args);

            System.out.println("Starting ...");
            String StrFilePage = res.getString("out_prefix") + res.get("suffix_page");
            String StrFileSite = res.getString("out_prefix") + res.get("suffix_site");
            try {
                MC = new MalletClassifier(new File(res.getString("class")), new File(StrFilePage), new File(StrFileSite));
                for (String strFile : (List<String>) res.get("file_in")) {
                    System.out.println("Classified " + strFile + " ...");
                    MC.Classified(new File(strFile));
                }
                MC.close();
                System.out.println("Success.");
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(MalletClassifier.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (MC != null) {
                    MC.close();
                }
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        System.out.println("End.");
    }

}
