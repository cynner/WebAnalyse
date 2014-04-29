/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import ArcFileUtils.MyRandomAccessFile;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class UpdateSiteDB {
    
    public static SQLiteConnection db;
    
    public static void main(String[] args){
        // SET PROPERTIES
        System.setProperty("sun.jnu.encoding", "UTF-8");
        System.setProperty("file.encoding", "UTF-8");

        ArgumentParser parser = ArgumentParsers.newArgumentParser("Crawler.ImportHostDB").defaultHelp(true)
                .description("Create or import website Sqlite3 database.");

        parser.addArgument("-o", "--database")
                .dest("database")
                .metavar("DBPATH")
                .type(String.class)
                .setDefault(DBDriver.TableConfig.FileNameWebSiteDB)
                .help("Database path");
        parser.addArgument("-d")
                .dest("dir")
                .metavar("DIR")
                .type(String.class)
                .setDefault(Crawler.Main.DefaultWorkingDirectory)
                .help("Working Directory for crawler");
        //parser.addArgument("-f","--filename")
        //        .dest("filename")
        //        .metavar("NAME")
        //        .type(String.class)
        //        .setDefault(Crawler.CrawlerConfigList.strMergeWebInfo)
        //        .help("Web info file name");
        parser.addArgument("TaskName")
                .nargs("?")
                .type(String.class)
                .setDefault("default")
                .help("To identify job");
        try {
            Namespace res = parser.parseArgs(args);
            String strWorkDir = res.getString("dir");
            String TaskName = res.getString("TaskName");

            CrawlerConfigList cfg = new CrawlerConfigList(TaskName, strWorkDir);

            try (MyRandomAccessFile raf = new MyRandomAccessFile(cfg.fileHostInfo, "r")) {
                long length;
                int i,lastcol;
                String Line, cmd;
                String[] cols, colnames;
                File dbFile = new File(res.getString("database"));
                db = new SQLiteConnection(dbFile);
                db.open(true);
                db.exec(DBDriver.TableConfig.CreateTableWebSiteDB); //CREATE TABLE IF NOT EXISTS
                db.exec("BEGIN;");
                System.out.println("BEGIN");
                length = raf.readLong();
                colnames = raf.readLine().split(",");
                lastcol = colnames.length - 1;
                while (raf.getFilePointer() < length) {
                    Line = raf.readLine();
                    cols = Line.split(",");
                    cmd = "UPDATE OR IGNORE website SET ";
                    for(i = 1; i < lastcol; i++)
                        cmd += colnames[i] + "=" + cols[i] + ",";
                    cmd += colnames[lastcol] + "=" + cols[lastcol];
                    cmd += " WHERE " + colnames[0] + "=" + cols[0] + ";";
                    db.exec(cmd);
                }
                System.out.println("COMMIT");
                db.exec("COMMIT;");
                System.out.println("SUCCESS");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UpdateSiteDB.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLiteException | IOException ex) {
                Logger.getLogger(UpdateSiteDB.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                db.dispose();
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (IOException ex) {
            Logger.getLogger(UpdateSiteDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
