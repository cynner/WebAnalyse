/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.io.*;
import java.net.IDN;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author malang
 */
public class HostExtractor implements Runnable {

    //public static final int BuffSize = 1000;
    public static HashMap<String, Integer> HostName = new HashMap<String, Integer>();
    //public static String[] HostBuff = new String[BuffSize];
    //public static BufferedWriter bw;
    //public static boolean HostLock = false;
    //public static int HostIdx = 0;
    public static String OutFileName;

    public static void initial(String OutputFile) {
        HostExtractor.OutFileName = OutputFile;
        /*
         try {
         //HostName = new HashMap<String, Integer>();
         bw = new BufferedWriter(new FileWriter(OutputFile));
         } catch (IOException ex) {
         Logger.getLogger(HostExtractor.class.getName()).log(Level.SEVERE, null, ex);
         }
         * 
         */
    }

    public static void finishing() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(OutFileName, false))) {
            for (String k : HostName.keySet()) {
                bw.write(k + "\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(HostExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public HostExtractor(File InputFile) {
        this.InputFile = InputFile;
    }
    private final File InputFile;

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " Start-Anlysefile " + InputFile);
        processCommand();
        System.out.println(Thread.currentThread().getName() + " End-Anlysefile " + InputFile);
    }

    public void processCommand() {
        ArcUtils au = new ArcUtils(InputFile);
        while (au.Next()) { // SIGTERM handle !SIGTERM &&
            Construct(au.GetContent());
            //System.out.println(au.GetContent());
        }
    }

    public void Construct(String Content) {
        Document doc = Jsoup.parse(Content);
        Elements es = doc.select("a");
        int idx;
        for (Element e : es) {
            addHostFromLink(e.attr("href"));

        }
    }

    public void addHostFromLink(String Link) {
        int lidx, idx = Link.indexOf(":"), v;
        if (idx >= 0) {
            idx += 3;
            if (Link.length() > idx) {
                lidx = Link.indexOf('/', idx);
                try {
                    if (lidx < 0) {
                        Link = Link.substring(idx);
                    } else {
                        Link = Link.substring(idx, lidx);
                    }
                    if (Link.indexOf(':') == -1
                            && Link.indexOf('\'') == -1
                            && Link.indexOf('"') == -1
                            && Link.indexOf('@') == -1
                            && Link.indexOf('?') == -1
                            && Link.indexOf('&') == -1
                            && Link.indexOf('=') == -1
                            && Link.indexOf('(') == -1
                            && Link.indexOf(')') == -1) {
                        addHost(IDN.toASCII(Link).toLowerCase());
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    public void addHost(String Host) {
        if (!HostExtractor.HostName.containsKey(Host)) {

            HostExtractor.HostName.put(Host, 0);
            /*
             try {
             while (HostLock);
             bw.write(Host + "\n");
             } catch (IOException ex) {
             Logger.getLogger(HostExtractor.class.getName()).log(Level.SEVERE, null, ex);
             }
             * 
             */

        }
    }
}
