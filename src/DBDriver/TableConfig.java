/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DBDriver;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Malang
 */
public class TableConfig {
    public static final String FileNameWebSiteDB = "resource/crawler.sqlite3";
    public static final String FileNameWebPageDB = "resource/webpage.sqlite3";
    public static final File FileWebSiteDB = new File(FileNameWebSiteDB);
    public static final File FileWebPageDB = new File(FileNameWebPageDB);
    public static final String CreateTableWebSiteDB = "CREATE TABLE IF NOT EXISTS website (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, hostname VARCHAR(255) UNIQUE NOT NULL, ip VARCHAR(40), location VARCHAR(2), page_count INTEGER, status TINYINT, lastupdate DATETIME);";
    public static final String CreateTableWebPageDB = "CREATE TABLE IF NOT EXISTS webpage (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, url VARCHAR(2048) UNIQUE NOT NULL, language VARCHAR(2), file_size INTEGER, comment_size INTEGER, js_size INTEGER, style_size INTEGER, content_size INTEGER, indegree INTEGER, outdegree INTEGER, pagerank DOUBLE, title_length INTEGER, title_max_freq INTEGER, content_max_freq INTEGER, content_length INTEGER, scc_no INTEGER, category VARCHAR(16));";
    public static void main(String[] args){
        createAllDB();
    }
    public static void createAllDB(){
        SQLiteConnection connPage = new SQLiteConnection(FileWebPageDB);
        try {
            connPage.open(true);
            connPage.exec(CreateTableWebPageDB);
        } catch (SQLiteException ex) {
            Logger.getLogger(TableConfig.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            
            connPage.dispose();
        }
        
        SQLiteConnection connSite = new SQLiteConnection(FileWebSiteDB);
        try {
            connSite.open(true);
            connSite.exec(CreateTableWebSiteDB);
        } catch (SQLiteException ex) {
            Logger.getLogger(TableConfig.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            
            connSite.dispose();
        }
    }
}
