/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DBDriver;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class QjobPrepare extends SQLiteJob<Object>{
        public String Command;
        
        public QjobPrepare(String Command){
            this.Command = Command;
            
        } 
        
        @Override
        protected Object job(SQLiteConnection connection) throws Throwable {
            return connection.prepare(Command);
        }
        
        public static void main(String[] args){
            //example
            
            SQLiteQueue dbq;
            String DBName = "";
            dbq = new SQLiteQueue(new File(DBName));
            dbq.start();
            dbq.execute(new QjobPrepare("UPDATE host SET lastupdate=datetime('now','localtime');"));
            try {
                dbq.stop(true).join();
            } catch (InterruptedException ex) {
                Logger.getLogger(QjobPrepare.class.getName()).log(Level.SEVERE, null, ex);
            }
    
        }
        
}
