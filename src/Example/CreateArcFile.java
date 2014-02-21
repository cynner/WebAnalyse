/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Example;

import ArcFileUtils.ArcRecord;
import ArcFileUtils.ArcWriter;
import ArcFileUtils.WebArcRecord;
import ArcFileUtils.WebArcWriter;
import java.io.File;
import java.util.Date;

/**
 *
 * @author malang
 */
public class CreateArcFile {
    public static void main(String[] args){
        /*
        String FileName="bukuk.arc";
        File f = new File(FileName);
        WebArcRecord arcrec = new WebArcRecord() {} ; 
        arcrec.URL = "http://www.huhuuh.com/dscsdc";
        arcrec.WebContent = "โคกระโดขโยะขะโยะ โคกระโด่ขยะโขยะ";
        arcrec.ArchiveDate = new Date();
        arcrec.ContentType = "text/html";
        arcrec.FirstLineContentHeader = "200 ok";
        arcrec.IPAddress = "192.168.1.1";
        //arcrec.LastModified = "ddddd";
        WebArcWriter aw = new WebArcWriter(f, true);
        aw.WriteRecord(arcrec);
        aw.WriteRecord(arcrec);
        aw.WriteRecord(arcrec);
        aw.close();
         * 
         */
        
        
        String FileName="bukuk.arc";
        File f = new File(FileName);
        ArcRecord arcrec = new ArcRecord() {} ; 
        arcrec.URL = "http://www.huhuuh.com/dscsdc";
        arcrec.ArchiveContent = "โคกระโดขโยะขะโยะ โคกระโด่ขยะโขยะ";
        arcrec.ArchiveDate = new Date();
        arcrec.ArchiveContentType = "text/html";
        arcrec.IPAddress = "192.168.1.1";
        //arcrec.LastModified = "ddddd";
        ArcWriter aw = new ArcWriter(f, true);
        aw.WriteRecord(arcrec);
        aw.close();
    }
}
