/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Converter;

import ArcFileUtils.ArcReader;
import Crawler.MyURL;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class IDF {

    //public static String DirName = "data/Graph/";
    public String IDFPath = "data/IDF.bin";
    public String LexDir = "data/lexicalorg";

    //public static String InputDirName = "data/arc/";
    public String DBPath = "data/Graph/PageLabel.sqlite3";
    public String DBWordPath = "data/Words.sqlite3";

    public SQLiteConnection db = new SQLiteConnection(new File(DBPath));
    public SQLiteConnection dbWord = new SQLiteConnection(new File(DBWordPath));
    public SQLiteStatement st;

    public class MyInt {

        public int val;

        public MyInt() {
            val = 1;
        }
    }

    public class IDFInf {

        public int DocNo;
        public int Freq;

        public IDFInf(int DocNo) {
            this.DocNo = DocNo;
            this.Freq = 1;
        }

        public IDFInf(int DocNo, int Freq) {
            this.DocNo = DocNo;
            this.Freq = Freq;
        }
    }

    public class SectionArray {

        public ArrayList<IDFInf> Title;
        public ArrayList<IDFInf> Content;

        public SectionArray() {
            Title = new ArrayList<>();
            Content = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        IDF idf = new IDF();
        idf.CreateIDF();
    }

    public int getID(String URL) {
        MyURL src;
        int id;
        try {
            src = new MyURL(URL);
        } catch (Exception ex) {
            return -1;
        }
        try {
            st = db.prepare("SELECT id FROM webpage WHERE url=\""
                    + src.UniqURL.replace("\"", "\"\"") + "\";");

            if (st.step()) {
                id = st.columnInt(0);
            } else {
                id = -2;
            }
            st.dispose();
        } catch (SQLiteException ex) {
            id = -3;
            Logger.getLogger(IDF.class.getName()).log(Level.SEVERE, null, ex);
        }
        return id;

    }

    public void CreateIDF() {
        try {

            db.open(false);
            //db.exec("BEGIN;");

            //db.exec("CREATE TABLE IF NOT EXISTS webpage(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, url VARCHAR(2048) UNIQUE NOT NULL);");
            //db.exec("COMMIT;");
            File SD = new File(LexDir);
            String[] TitleContent;
            String[] LexStr;
            HashMap<String, SectionArray> MKV = new HashMap<>();
            HashMap<String, MyInt> SKV = new HashMap<>();
            //ArrayList<SectionArray> IDF = new ArrayList<SectionArray>();
            SectionArray SArr;
            Integer idx;
            Integer size = 0;
            MyInt MI;
            int cntTitle, cntContent, TitleMaxFreq, ContentMaxFreq, tmpv;
            int DocNo;

            db.exec("BEGIN;");
            for (File f : SD.listFiles()) {
                try (ArcReader ar = new ArcReader(f)) {
                    System.out.println(f.getName());
                    /*
                     while (ar.Next()) {
                     DocNo = getID(ar.Record.URL);
                     if (DocNo >= 0) {
                     System.out.println(ar.Record.URL);
                     TitleContent = ar.Record.ArchiveContent.split("\n");
                     cntTitle = cntContent = TitleMaxFreq = ContentMaxFreq = 0;

                     // ------------------ Title Compute ------------------- //
                     if (TitleContent.length >= 1) {
                     SKV.clear();
                     LexStr = TitleContent[0].split(" ");
                     for (String Lex : LexStr) {
                     if (!Lex.equals("")) {
                     MI = SKV.get(Lex);
                     if (MI != null) {
                     MI.val++;
                     } else {
                     SKV.put(Lex, new MyInt());
                     }
                     cntTitle++;
                     }
                     }
                        
                     // Switch Local To Global & Save Some Statistical
                     for (Map.Entry<String, MyInt> e : SKV.entrySet()) {
                     SArr = MKV.get(e.getKey());
                     tmpv = e.getValue().val;
                        
                     if (SArr == null) {
                     SArr = new SectionArray();
                     SArr.Title.add(new IDFInf(DocNo, tmpv));
                     MKV.put(e.getKey(), SArr);
                     } else {
                     SArr.Title.add(new IDFInf(DocNo, tmpv));
                     }
                        
                     if (tmpv > TitleMaxFreq) {
                     TitleMaxFreq = tmpv;
                     }
                     }
                     }
                        
                     // ------------------ Content Compute ------------------- //
                     if (TitleContent.length >= 2) {
                     SKV.clear();
                     LexStr = TitleContent[1].split(" ");
                     for (String Lex : LexStr) {
                     if (!Lex.equals("")) {
                     MI = SKV.get(Lex);
                     if (MI != null) {
                     MI.val++;
                     } else {
                     SKV.put(Lex, new MyInt());
                     }
                     cntContent++;
                     }
                     }
                        
                     // Switch Local To Global & Save Some Statistical
                     for (Map.Entry<String, MyInt> e : SKV.entrySet()) {
                     SArr = MKV.get(e.getKey());
                     tmpv = e.getValue().val;
                        
                     if (SArr == null) {
                     SArr = new SectionArray();
                     SArr.Content.add(new IDFInf(DocNo, tmpv));
                     MKV.put(e.getKey(), SArr);
                     } else {
                     SArr.Content.add(new IDFInf(DocNo, tmpv));
                     }
                        
                     if (tmpv > ContentMaxFreq) {
                     ContentMaxFreq = tmpv;
                     }
                     }
                     }
                     // ---------------------------------------------------- //

                     try {
                     db.exec("UPDATE webpage SET TitleLength=" + cntTitle
                     + ",ContentLength=" + cntContent
                     + ",TitleMaxFreq=" + TitleMaxFreq
                     + ",ContentMaxFreq=" + ContentMaxFreq
                     + " WHERE id=" + DocNo + ";");
                     } catch (SQLiteException e) {
                     System.err.println("UPDATE webpage SET TitleLength=" + cntTitle
                     + ",ContentLength=" + cntContent
                     + ",TitleMaxFreq=" + TitleMaxFreq
                     + ",ContentMaxFreq=" + ContentMaxFreq
                     + " WHERE id=" + DocNo + ";");
                     e.printStackTrace();
                     }
                     } else {
                     System.err.println("Err: " + ar.Record.URL);
                     }
                     }
                     */
                    System.out.println("Closed.");
                } catch (IOException ex) {
                    Logger.getLogger(IDF.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

            db.exec("COMMIT;");
            db.dispose();

            System.out.println("IDF Saving to " + LexDir + " ...");
            dbWord.open(true);
            dbWord.exec("CREATE TABLE IF NOT EXISTS tbl_word(id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, word VARCHAR(32) UNIQUE NOT NULL, IDFPos INTEGER);");
            dbWord.exec("BEGIN;");
            try {

                RandomAccessFile bw = new RandomAccessFile(IDFPath, "rw");
                for (Map.Entry<String, SectionArray> e : MKV.entrySet()) {
                    dbWord.exec("INSERT INTO tbl_word(word,IDFPos) VALUES(\"" + e.getKey().replaceAll("\"", "\"\"") + "\"," + bw.getFilePointer() + ");");
                    bw.writeInt(e.getValue().Title.size());
                    for (IDFInf i : e.getValue().Title) {
                        bw.writeInt(i.DocNo);
                        bw.writeInt(i.Freq);
                    }
                    bw.writeInt(e.getValue().Content.size());
                    for (IDFInf i : e.getValue().Content) {
                        bw.writeInt(i.DocNo);
                        bw.writeInt(i.Freq);
                    }
                }
                bw.close();

                /*
                 DataOutputStream dos = new DataOutputStream(new FileOutputStream(IDFPath,false));
                 for(Map.Entry<String,SectionArray> e : MKV.entrySet()){                    
                 dbWord.exec("INSERT INTO tbl_word(word,IDFPos) VALUES(\""+ e.getKey().replaceAll("\"", "\"\"") + "\"," + dos.size() + ");");
                 dos.writeInt(e.getValue().Title.size());
                 for(IDFInf i : e.getValue().Title){
                 dos.writeInt(i.DocNo);
                 dos.writeInt(i.Freq);
                 }
                 dos.writeInt(e.getValue().Content.size());
                 for(IDFInf i : e.getValue().Content){
                 dos.writeInt(i.DocNo);
                 dos.writeInt(i.Freq);
                 }
                 }
                 dos.close();
                 */
            } catch (FileNotFoundException ex) {
                Logger.getLogger(IDF.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(IDF.class.getName()).log(Level.SEVERE, null, ex);
            }
            dbWord.exec("COMMIT;");
            System.out.println("Success");
        } catch (SQLiteException ex) {
            Logger.getLogger(IDF.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            dbWord.dispose();
        }
    }
}
