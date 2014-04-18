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
import java.io.File;
//import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
//import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class PageRank {

    private static class MutableInt {

        public int value; // note that we start at 1 since we're counting

        public MutableInt() {
            value = 1;
        }

        public MutableInt(int value) {
            this.value = value;
        }
    }

    /*
     * linkedlist : webpage-i <interger> ==> webpage <j> with score <double>
     * maxSize : number of webpage in this graph
     */
    public static final double DEFAULT_DAMPING_FACTOR = 0.85;
    public static final double DEFAULT_EPSILON = 1E-7;
    public static final String DEFAULT_OUTFILE = "data/result.pagerank.txt";

    private static HashSet<Integer> getZeroOutdegree(HashMap<Integer, HashMap<Integer, Double>> linkedList) {
        HashSet<Integer> zeroOutdegree = new HashSet<>();
        for (Map.Entry<Integer, HashMap<Integer, Double>> t1 : linkedList.entrySet()) {
            if (t1.getValue().isEmpty()) {
                zeroOutdegree.add(t1.getKey());
            }
        }
        return zeroOutdegree;
    }

    private static double diff(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(Math.abs(a[i] - b[i]), 2);
        }
        return Math.sqrt(sum);
    }

    public static double[] cal(HashMap<Integer, HashMap<Integer, Double>> linkedList, int maxSize, double DAMPING_FACTOR, double EPSILON) {
        int Size = linkedList.size();
        maxSize = maxSize + 1;
        HashSet<Integer> zeroOutdegree = getZeroOutdegree(linkedList);

        System.out.println("ZERO OUTLINK SIZE: " + zeroOutdegree.size());

        double[] source = new double[maxSize];
        for (Integer k : linkedList.keySet()) {
            source[k] = (double) 1.0 / Size;
        }

        double[] dest;
        double err;
        do {
            dest = new double[maxSize];
            for (Map.Entry<Integer, HashMap<Integer, Double>> t1 : linkedList.entrySet()) {
                Integer iID = t1.getKey();
                for (Map.Entry<Integer, Double> t2 : t1.getValue().entrySet()) {
                    Integer jID = t2.getKey();
                    //dest[jID] += source[iID] * ((double) 1.0 / t1.getValue().size()); // uniform
                    dest[jID] += source[iID] * t2.getValue();
                }
            }
            double zeroInlinkScoreSum = 0;
            for (Integer temp : zeroOutdegree) {
                zeroInlinkScoreSum += source[temp] * ((double) 1.0 / Size);
            }
            for (Integer j : linkedList.keySet()) {
                dest[j] += zeroInlinkScoreSum;
            }
            for (Integer i : linkedList.keySet()) {
                dest[i] = DAMPING_FACTOR * dest[i] + (1.0 - DAMPING_FACTOR) / Size;
            }
            err = diff(source, dest);
            source = dest;
            System.out.println("Error " + err);
        } while (err > EPSILON);
        return source;
    }

    /**
     * Import csv file
     *
     * @param CSVFile each line has format SOURCE;d1:w1;d2:w2;...;dn:wn
     * @param linkedList data will append CSVFile
     * @param prevMaxSize previous maxSize
     * @return
     * @throws FileNotFoundException
     */
    public static int importCSVFreq(File CSVFile, HashMap<Integer, HashMap<Integer, Double>> linkedList, int prevMaxSize) throws FileNotFoundException {
        HashMap<Integer, Double> SubLink;
        HashMap<Integer, MutableInt> tmpSubLink = new HashMap<>();
        String Line = null;
        String[] strs, substr;
        Integer source;
        Integer dest;
        Integer weight;
        int sumWeight;
        int maxSize = prevMaxSize;
        try (BufferedReader br = new BufferedReader(new FileReader(CSVFile))){
            while ((Line = br.readLine()) != null) {
                strs = Line.split(";");
                source = Integer.parseInt(strs[0]);
                sumWeight = 0;
                tmpSubLink.clear();
                // Sum Weight OutLink
                for (int i = 1; i < strs.length; i++) {
                    substr = strs[i].split(":");
                    dest = Integer.parseInt(substr[0]);
                    weight = substr.length > 1 ? Integer.parseInt(substr[1]) : 1;
                    MutableInt v = tmpSubLink.put(dest, new MutableInt(weight));
                    if (v != null) {
                        tmpSubLink.get(dest).value += v.value;
                    }
                    sumWeight += weight;
                }

                SubLink = new HashMap<>();
                for (Map.Entry<Integer, MutableInt> e : tmpSubLink.entrySet()) {
                    SubLink.put(e.getKey(), (double) e.getValue().value / sumWeight);
                }

                linkedList.put(source, SubLink);
                if (source > maxSize) {
                    maxSize = source;
                }
            }
        } catch (IOException ex) {
            System.err.println("At : '" + Line + "'");
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
        return maxSize;
    }

    public static void saveResult(File f, double[] d) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {

            for (int i = 0; i < d.length; i++) {
                if (d[i] > 0) {
                    bw.write(i + ":" + d[i] + "\n");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void filterNode(HashMap<Integer, HashMap<Integer, Double>> linkedList) {
        for (HashMap<Integer, Double> v : linkedList.values()) {
            Integer[] arr = new Integer[v.size()];
            v.keySet().toArray(arr);
            for (Integer k : arr) {
                if (!linkedList.containsKey(k)) {
                    v.remove(k);
                }
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        // SET PROPERTIES
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("file.encoding", "UTF-8");
        
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Analyse.PageRank").defaultHelp(true)
                .description("PageRank calculation from graph file");
        parser.addArgument("-d", "--damping")
                .dest("DAMPING")
                .metavar("DAMPING")
                .type(Double.class)
                .setDefault(DEFAULT_DAMPING_FACTOR)
                .help("Damping factor [0-1]");
        parser.addArgument("-e", "--epsilon")
                .dest("EPSILON")
                .metavar("EPSILON")
                .type(Double.class)
                .setDefault(DEFAULT_EPSILON)
                .help("Maximum error value");
        parser.addArgument("-o")
                .dest("OUTFILE")
                .metavar("OUTFILE")
                .type(String.class)
                .setDefault(DEFAULT_OUTFILE)
                .help("Output filename");
        parser.addArgument("GRAPH_FILE")
                .nargs("+")
                .type(String.class)
                .help("Graph file, each line format: SOURCE;d1:w1;d2:w2; ... ;dn:wn");
        
        HashMap<Integer, HashMap<Integer, Double>> linkedList = new HashMap<>();
        int maxSize = 0;
        try {
            Namespace res = parser.parseArgs(args);

            System.out.println("Reading CSV...");
            for (String strFile : (List<String>) res.get("GRAPH_FILE")) {
                System.out.println("Reading " + strFile + " ...");
                maxSize = PageRank.importCSVFreq(new File(strFile), linkedList, maxSize);
            }

            System.out.println("Read CSV Finished");
            System.out.println("Filtering node...");
            PageRank.filterNode(linkedList);
            System.out.println("Filtering node finished");
            System.out.println("Calculation...");
            double[] d = PageRank.cal(linkedList, maxSize, res.getDouble("DAMPING"), res.getDouble("EPSILON"));
            System.out.println("Calculation Finished");
            System.out.println("Writing Result File...");
            PageRank.saveResult(new File(res.getString("OUTFILE")), d);
            System.out.println("Writing Result File Finished");
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        }

        /*
         SQLiteConnection db = new SQLiteConnection(DBDriver.TableConfig.FileWebPageDB);
         try {
         db.open();
         db.exec("BEGIN;");
         int trig = 50000, step = 50000;
         for (int i = 0; i < d.length; i++) {
         if (i > trig) {
         System.out.println("id: " + i);
         db.exec("COMMIT;");
         db.exec("BEGIN;");
         trig += step;
         }
         db.exec("UPDATE webpage SET pagerank=" + d[i] + " WHERE id=" + i + ";");
         }
         db.exec("COMMIT;");
         } catch (SQLiteException ex) {
         Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
         db.dispose();
         }
         */
    }

}
