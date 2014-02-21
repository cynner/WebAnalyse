/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Example;

import com.almworks.sqlite4java.*;
import java.io.File;

/**
 *
 * @author malang
 */
public class ExSQLite {
    public static void main(String [] args) throws Exception{
        SQLiteConnection db = new SQLiteConnection(new File("/var/www/web/testj.sqlite3"));
        db.open(true);
        //db.exec("CREATE TABLE test(id INTEGER PRIMARY KEY, x VARCHAR(16),y VARCHAR(16))");
        //db.exec("INSERT INTO test(id,x,y) values(1,'aaa','AAA');");
        /*db.exec("INSERT INTO test(id,x,y) values(2,'bbb','BBB');");
        db.exec("INSERT INTO test(id,x,y) values(4,'ccc','CCC');");
        db.exec("INSERT INTO test(id,x,y) values(8,'ddd','DDD');");
        db.exec("INSERT INTO test(id,x,y) values(16,'eee','EEE');");
        db.exec("INSERT INTO test(id,x,y) values(32,'fff','FFF');");
        db.exec("INSERT INTO test(id,x,y) values(64,'ggg','GGG');");
        db.exec("INSERT INTO test(id,x,y) values(128,'hhh','HHH');");
        db.exec("INSERT INTO test(id,x,y) values(256,'iii','III');");
        db.exec("INSERT INTO test(id,x,y) values(512,'jjj','JJJ');");
        */
        //db.exec("ALTER TABLE test ADD COLUMN newcols VARCHAR(16)");
        //db.exec("INSERT INTO test(id,x,y,newcols) values(2048,'kkk','KKK','|kKkK|');");
        db.exec("UPDATE test SET newcols='|iIiI|' WHERE id=256;");
        
        SQLiteStatement st = db.prepare("SELECT * FROM test");
        
        try {
            //st.bind(1, 1024);
            //st.bindStream(1);
            //st.bindStream(1);
            while (st.step()) {
                //System.out.println(st.columnInt(0));
                System.out.println(st.columnString(0));
                System.out.println(st.columnValue(1));
            }
        } finally {
            st.dispose();
        }
        db.dispose();
    }
}



