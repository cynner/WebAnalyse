/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ArcFileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import net.sf.samtools.util.BlockCompressedFilePointerUtil;
import net.sf.samtools.util.BlockCompressedInputStream;
import org.archive.io.GzippedInputStream;

/**
 *
 * @author malang
 */
public class BGZFReader implements AutoCloseable{
     private RandomAccessFile txt = null; //text reader
    private BlockCompressedInputStream bgzf = null; //Block compressed gzip reader
     //private GzippedInputStream bgzf = null;
     //private FileInputStream fis = null;
    //BufferedReader in;
    public boolean isASCII = true;
    public long file_size = 0;

    
    public BGZFReader (final File arcfile) throws FileNotFoundException, IOException {
        //Determine if file is ascii
        if(isBGZF(arcfile)){
            //fis = new FileInputStream(arcfile);
            isASCII = false;
            bgzf = new BlockCompressedInputStream(arcfile);
            bgzf.seek(0);
            //bgzf = new GZIPInputStream(fis);
            
            file_size = arcfile.length();
            
            txt = null;
        }else {
            isASCII = true;
            txt = new RandomAccessFile(arcfile, "r");
            bgzf = null;
        }
        
    }
    /*

    private boolean isText(final File arcfile) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(new FileInputStream(arcfile));
        byte[] buffer = new byte[1024];
        int numchars = bufferedInput.read(buffer);
        for(int i = 0; i < numchars; i++){
            char c = (char)buffer[i];
            if((c < 32 || c > 126) && !Character.isWhitespace(c)){
                return false;
            }
        }
        
        bufferedInput.close();
        return true;
    }
    */
    
    private boolean isBGZF(final File arcfile) throws FileNotFoundException, IOException {
        boolean isValid = false;
        try (BufferedInputStream bufferedInput = new BufferedInputStream(new FileInputStream(arcfile))) {
            isValid = GzippedInputStream.isCompressedStream(bufferedInput);
        }catch(RuntimeException re){
            System.out.println("Cannot test non-buffered stream.");
        }
        return isValid;
    }

     @Override
    public void close() throws IOException {
        if(bgzf != null) {
            bgzf.close();
            bgzf = null;
        }
        if(txt != null){
            txt.close();
            txt = null;
        }
    }

    public long getFilePointer() throws IOException {
        if(isASCII){
            if(txt != null) 
                return txt.getFilePointer();
        }else if(bgzf != null){ 
            return bgzf.getFilePointer();
        }
        return 0;
    }

    public long getRealOffset() throws IOException {
        if(isASCII){
            if(txt != null) 
                return txt.getFilePointer();
        }else if(bgzf != null){ 
            return BlockCompressedFilePointerUtil.getBlockAddress(bgzf.getFilePointer());
        }
        return 0;
    }

    public long getRealOffset(long fp) throws IOException {
        if(isASCII){
            if(txt != null) 
                return fp;
        }else if(bgzf != null){ 
            return BlockCompressedFilePointerUtil.getBlockAddress(fp);
        }
        return 0;
    }
    
    public long getAddRealOffset(long fp)throws IOException {
        if(isASCII){
            if(txt != null) 
                return txt.getFilePointer() + fp;
        }else if(bgzf != null){ 
            return BlockCompressedFilePointerUtil.getBlockAddress(bgzf.getFilePointer() + fp);
        }
        return 0;
    }
    
    public long length() throws IOException { 
        if(isASCII){
            if(txt != null) 
                return txt.length();
        }else if(bgzf != null){ 
            return file_size;
        }
        return 0; 
    }

    public int read(byte[] b) throws IOException {
        if(isASCII){
            if(txt != null) 
                return txt.read(b);
        }else if(bgzf != null){ 
            return bgzf.read(b);
        }
        return 0;
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        if(isASCII){
            if(txt != null) 
                return txt.read(b, off, len);
        }else if(bgzf != null) {
            return bgzf.read(b, off, len);
        }
        return 0;
    }

    public long lastPos;
    public String readLine() throws IOException {
        if(isASCII){
            if(txt != null)
                return txt.readLine();
        } else if(bgzf != null) {
            String ans = "";
            int i;
            int v;
            lastPos = bgzf.getFilePointer();
            byte[] b = new byte[8192];
                while((v = bgzf.read()) != -1){
                    if(v >= 0){
                        if(v != '\n'){
                            ans += (char)v;
                        }else{
                                //System.out.println(ans);
                            return ans;
                        }
                    }else{
                        i=0;
                        b[i++] = (byte)v;
                        //System.err.println("+"+v+"+");
                        while((v = bgzf.read()) < -1){
                            
                            //System.err.println("++"+v+"+");
                            b[i++] = (byte)v;
                        }
                        ans += new String(b, 0, i, "utf-8");
                        if(v != -1){
                            if(v != '\n')
                                ans += (char)v;
                            else{
                                
                                //System.out.println(ans);
                                return ans;
                            }
                        }else{
                            break;
                        }
                    }
                }
                //lastTT = v;
                //System.err.println(ans);
        }
        
                    //System.err.println("|"+lastTT+"|");
        return null;
    }

    public void seek(long pos) throws IOException {
        if(isASCII){
            if(txt != null)
                txt.seek(pos);
        } else if(bgzf != null) {
            bgzf.seek(pos);
        }
    }
    


    public void skip(long pos) throws IOException {
        if(isASCII){
            if(txt != null)
                txt.seek(pos + txt.getFilePointer());
                //txt.skipBytes((int)pos);
        } else if(bgzf != null) {
            bgzf.skip(pos);
        }
    }

    public int read() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
