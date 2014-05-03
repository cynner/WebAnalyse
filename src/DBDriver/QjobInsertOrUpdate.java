/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DBDriver;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wiwat
 */
public class QjobInsertOrUpdate extends SQLiteJob<Object>{
    String[] Cols;
    String[] Vals;
    String table;

    public QjobInsertOrUpdate(String table,String[] Cols,String[] Vals) {
        this.table = table;
        this.Cols = Cols;
        this.Vals = Vals;
    }
    
    

    @Override
    protected Object job(SQLiteConnection sqlc)  {
        String strCols = Cols[0];
        for (int i = 1; i < Cols.length; i++) {
            strCols += "," + Cols[i];
        }
        String strVals = Vals[0];
        for (int i = 1; i < Vals.length; i++) {
            strVals += "," + Vals[i];
        }
        
        try {
            sqlc.exec("INSERT INTO " + table + " (" + strCols + ") VALUES (" + strVals + ");"  );
        } catch (SQLiteException ex) {
            try {
                String Query = "UPDATE OR IGNORE " + table + " SET " + Cols[1] + "=" + Vals[1];
                for(int i=2;i<Cols.length;i++){
                    Query += "," + Cols[i] + "=" + Vals[i];
                }
                Query += " WHERE " + Cols[0] + "=" + Vals[0] + ";";
                sqlc.exec(Query);
            } catch (SQLiteException ex2) {
                Logger.getLogger(QjobInsertOrUpdate.class.getName()).log(Level.SEVERE, null, ex2);
            }
        }
        return null;
    }
    
}
