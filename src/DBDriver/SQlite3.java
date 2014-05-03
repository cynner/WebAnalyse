/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DBDriver;

import static Crawler.ImportPageDB.db;
import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class SQlite3 {
    public void insert(){
        
    }
    public void replace(){
        
    }
    public void delete(){
        
    }
    public void select(){
        
    }
    
    public static String [] SplitVals(String Vals){
        ArrayList<String> result = new ArrayList<>();
        String val;
        char quote = 0;
        int cur;
        val = "";
        for(char c : Vals.toCharArray()){
            switch(c){
                case ',' :
                    if (quote == 0){
                        result.add(val);
                        val = "";
                    } else {
                        val += c;
                    }
                    break;
                case '"' : case '\'':
                    if (quote == c)
                        quote = 0;
                    else
                        quote = c;
                    val += c;
                    break;
                default:
                    val += c;
                    break;
            }
        }
        result.add(val);
        String[] arr = new String[result.size()];
        result.toArray(arr);
        return arr;
    }
    
    public static void InsertORUpdate(SQLiteConnection db, String table, String cols, String vals) {
        String Query;
        try {
            Query = "INSERT INTO webpage(" + cols + ") VALUES(" + vals + ");";
            db.exec(Query);
        } catch (SQLiteException ex) {
            try {
                String[] Cols = cols.split(",");
                String[] Vals = SplitVals(vals);
                Query = "UPDATE OR IGNORE " + table + " SET " + Cols[1] + "=" + Vals[1];
                for (int i = 2; i < Cols.length; i++) {
                    Query += "," + Cols[i] + "=" + Vals[i];
                }
                Query += " WHERE " + Cols[0] + "=" + Vals[0] + ";";
                db.exec(Query);
            } catch (SQLiteException ex2) {
                Logger.getLogger(QjobInsertOrUpdate.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
    }
    
    public static void InsertORUpdate(SQLiteConnection db, String table, String[] Cols, String[] Vals) {
        String Query;
        try {
            String cols = Cols[0];
            String vals = Vals[0];
            for (int i = 1; i < Cols.length; i++) {
                cols += "," + Cols[i];
                vals += "," + Vals[i];
            }
            
            Query = "INSERT INTO webpage(" + cols + ") VALUES(" + vals + ");";
            db.exec(Query);
        } catch (SQLiteException ex) {
            try {
                Query = "UPDATE OR IGNORE " + table + " SET " + Cols[1] + "=" + Vals[1];
                for (int i = 2; i < Cols.length; i++) {
                    Query += "," + Cols[i] + "=" + Vals[i];
                }
                Query += " WHERE " + Cols[0] + "=" + Vals[0] + ";";
                db.exec(Query);
            } catch (SQLiteException ex2) {
                Logger.getLogger(QjobInsertOrUpdate.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
    }
}
