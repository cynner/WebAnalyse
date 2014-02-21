/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.util.Date;

/**
 *
 * @author malang
 */
public class ArcRecord implements Cloneable{
    
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
    public ArcRecord clone() {
        try {
            return (ArcRecord) super.clone();
        } catch (CloneNotSupportedException e) {        
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    
}
