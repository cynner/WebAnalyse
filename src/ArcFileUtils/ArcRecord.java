/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class ArcRecord implements Cloneable{
    
    public static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
    //public String Header;
    
    /* ---------- Record Header ---------- */
    /* URL IP-address Archive-date Content-type Archive-length */
    public String URL;
    public String IPAddress;
    public Date ArchiveDate;
    public String ArchiveContentType;
    public long ArchiveLength;
    public byte[] Data;
    
    
    /* ---------- Archive Content ---------- */
    public String ArchiveContent;
    
    public ArcRecord(){
        
    }
    
    @Override
    public ArcRecord clone() throws CloneNotSupportedException {
        try {
            return (ArcRecord) super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(ArcRecord.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException();
        }
    }
    
}
