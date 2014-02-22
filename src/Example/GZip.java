/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Example;

/**
 *
 * @author malang
 */
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.BlockCompressedInputStream;
import net.sf.samtools.util.BlockCompressedFilePointerUtil;
import java.util.zip.GZIPInputStream;


public class GZip {
    
    
    public static void main(String[] args){
        try {
            
            //GZip gz = new GZip("txt.gz");
            
            byte[] buffer = new byte[32];
            //InputStream is = new RandomAccessInputStream(new File("txt.arc.gz"));
            //GzippedInputStream g = new GzippedInputStream(is);
            InputStream is = new FileInputStream(new File("txt.arc.gz"));
            
            GZIPInputStream s = new GZIPInputStream(is);
            
            s.read(buffer,0,32);
            System.err.println(new String(buffer));
            s.skip(32);
            s.read(buffer,0,32);
            System.err.println(new String(buffer));
            //g.skip(32);
            
            /*GZIPInputStream x = new GZIPInputStream(new InputStream() {

                @Override
                public int read() throws IOException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            */
            /*
            BlockCompressedInputStream gz = new BlockCompressedInputStream(new File("txt.arc.gz"));
            //gz.seek(0);
            byte[] buffer = new byte[32];
            System.out.println(gz.markSupported());
            
            gz.getFilePointer();
            gz.read(buffer,0,4);
            System.out.println(new String(buffer));
            gz.close();
                    */
            //System.out.println(gz.readLine());
            
        } catch (IOException ex) {
            Logger.getLogger(GZip.class.getName()).log(Level.SEVERE, null, ex);
        }
        testdoc(0);
    }
    
    /**
     * Skips specified number of bytes of uncompressed data.
     *
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped.
     * @exception IOException if an I/O error has occurred
     * @exception IllegalArgumentException if n < 0
     */
    public static int testdoc(int n){
       return 0; 
    }
    
    private RandomAccessFile txt = null; //text reader
    private BlockCompressedInputStream bgzf = null; //Block compressed gzip reader
    //BufferedReader in;
    boolean isASCII = true;
    long file_size = 0;
    
    public GZip(final String filename) throws IOException {
        //Determine if file is ascii
        if(isText(filename)){
            isASCII = true;
            txt = new RandomAccessFile(filename, "r");
            bgzf = null;
        }else if(isBGZF(filename)){
            isASCII = false;
            bgzf = new BlockCompressedInputStream(new File(filename));
            bgzf.seek(0);
            file_size = (new File(filename)).length();
            //bgzf = new BlockCompressedInputStream(new RandomAccessFile(filename, "r"));
            txt = null;
        }
        
    }

    private boolean isText(final String filename) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(new FileInputStream(filename));
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

    private boolean isBGZF(final String filename) throws IOException {
        BufferedInputStream bufferedInput = new BufferedInputStream(new FileInputStream(filename));
        try {
            return BlockCompressedInputStream.isValidFile(bufferedInput);
            
        }catch(RuntimeException re){
            System.out.println("Cannot test non-buffered stream.");
        }
        return false;
    }

    public void close() throws IOException {
        if(bgzf != null) bgzf.close();
        if(txt != null) txt.close();
    }

    public long getFilePointer() throws IOException {
        if(isASCII && txt != null) return txt.getFilePointer();
        if(!isASCII && bgzf != null){ 
            try {
                return bgzf.getFilePointer();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return 0;
    }

    public long getRealOffset() throws IOException {
        if(isASCII && txt != null) return txt.getFilePointer();
        if(!isASCII && bgzf != null){
            return BlockCompressedFilePointerUtil.getBlockAddress(bgzf.getFilePointer());
        }
        
        return 0;

    }

    public long length() throws IOException { 
        if(isASCII && txt != null) return txt.length();
        if(!isASCII && bgzf != null) return file_size;
        return 0; 
    }

    public int read(byte[] b) throws IOException {
        if(isASCII && txt != null) return txt.read(b);
        if(!isASCII && bgzf != null) return bgzf.read(b);
        return 0;
    }

    public String readLine() throws IOException {
        if(isASCII && txt != null){
            return txt.readLine();
        }
        if(!isASCII && bgzf != null){
            String ans = "";
            char c;
            while((c = (char)bgzf.read()) < 255){
                                
                if(c != '\n') ans += c;
                else return ans;
            }
        }
        return null;
    }

    public void seek(long pos) throws IOException {
        if(isASCII && txt != null) txt.seek(pos);
        if(!isASCII && bgzf != null) bgzf.seek(pos);
    }

}