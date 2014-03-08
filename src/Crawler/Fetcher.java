/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Crawler;

import static ArcFileUtils.ArcRecord.dateFormat;
import ArcFileUtils.WebArcRecord;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author malang
 */
public class Fetcher {
    

    public boolean isSuccess;
    public Map<String , List<String>> Headers;
    public HttpURLConnection uc;
    
    /* Details */
    public WebArcRecord Details = new WebArcRecord();
    public int ResponseCode;
    //public long ContentLength;
    //public long LastModified;
    //public long Date;
    //public String FirstHeader;
    //public byte[] Data;
    //public Document Doc;
    //public String Content;
    //public String ContentType;
    public String ContentTypeOrg;
    public String ContentEncoding;
    //public String Server;
    //public String CharSet;
    public URL Url;    
    
    
    /* ConFig. */
    public int Timeout = 3000;
    public int ReadTimeout = 3000;
    public String UserAgent = "Mojfabbs-princeofvamp@gmail.com";
    public long MaxContentLength = 2000000;
    
    public Fetcher() {}
    
    
    public Fetcher(String UserAgent,int Timeout) {
        this.UserAgent = UserAgent;
        this.Timeout = Timeout;
        this.Details.IPAddress = GetMyIP();
    }
    
    public Fetcher(String UserAgent) {
        this.UserAgent = UserAgent;
        this.Details.IPAddress = GetMyIP();
        
    }

    public Fetcher(int Timeout) {
        this.Timeout = Timeout;
        this.Details.IPAddress = GetMyIP();
    }
    


    public void fetch(String Url) {
        try {
            this.Details.URL = Url;
            this.Url = new URL(Url);
            fetch();
        } catch (MalformedURLException ex) {
            this.isSuccess = false;
        }
    }
    
    public static String GetMyIP(){
        String s = null;
        try {
            s = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            System.err.println("Unknown Loacal IP");
        }
        return s;
    }
    
    
    public boolean getHeader(String Url){
        try {
            this.Details.URL = Url;
            this.Url = new URL(Url);
            return getHeader();
        } catch (MalformedURLException ex) {
            this.isSuccess = false;
        }
        return false;
    }
    
    public boolean getHeader(){
        this.ResponseCode = 0;
        try {
            uc = null;
            uc = (HttpURLConnection) Url.openConnection();
            

            uc.setRequestProperty("User-agent", UserAgent);
            //uc.setRequestProperty("Content-Language", "en-US"); 
            uc.setConnectTimeout(Timeout);
            uc.setReadTimeout(ReadTimeout);
            uc.setRequestMethod("GET");


            this.ResponseCode = uc.getResponseCode();
            this.ContentTypeOrg = uc.getContentType();
            this.ContentEncoding = uc.getContentEncoding();
            this.Details.Server = uc.getHeaderField("Server");

            this.Details.ServerTime = uc.getDate();
            this.Details.LastModified = uc.getLastModified();
            this.Details.ContentLength = uc.getContentLengthLong();
            this.Headers = uc.getHeaderFields();
            this.Details.FirstLineContentHeader = uc.getHeaderField(0);

            // Find Charset

            this.Details.WebContentType = this.ContentTypeOrg;
            this.Details.ArchiveContentType = this.ContentTypeOrg.replaceAll(";.*", "").trim().toLowerCase();
            this.Details.AnalyseCharset();
        }catch (SocketTimeoutException ex) {
            // this.ResponseCode = 408;
            System.err.println("Error: Header gathering: " + ex.getMessage());
            return false;
        } catch (ProtocolException ex) {
            // Never happen
            System.err.println("Error: Header gathering: " + ex.getMessage());
            return false;
        } catch (IOException ex) {
            // IO
            System.err.println("Error: Header gathering: " + ex.getMessage());
            return false;
        } catch (Exception e){
            System.err.println("Error: Header gathering: " + e.getMessage());
            return false;
        }
        return true;
    }
    
    public boolean getData(){
        this.Details.Data = null;
        try{
        
            uc.connect();
            /*
            if (this.ResponseCode < 200 || this.ResponseCode >= 300 || this.ContentLength > this.MaxContentLength) {
                return;
            }
             * 
             */
            try(InputStream is = uc.getInputStream()){
                this.Details.Data = IOUtils.toByteArray(is);
            }
            uc.disconnect();
            return true;
        } catch (SocketTimeoutException ex) {
            System.err.println("Error: Content gathering: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("Error: Content gathering: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("Error: Content gathering: " + ex.getMessage());
        }
        uc.disconnect();
        return false;
    }
    
    /*
    public void getContent(){
        this.Details.WebContent = null;
        try{
        
            uc.connect();
            InputStream is = uc.getInputStream();
            BufferedReader br;
            try{
                br = new BufferedReader(new InputStreamReader(is, this.Details.charset));
            }catch(UnsupportedEncodingException ex){
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            }
            String tmp;

            this.Details.WebContent = br.readLine();

            while ((tmp = br.readLine()) != null) {
                this.Details.WebContent += "\n" + tmp;
            }

            br.close();
            is.close();
            uc.disconnect();
            this.isSuccess = true;
        } catch (SocketTimeoutException ex) {
            //this.ResponseCode = 408;
        } catch (IOException ex) {
        } catch (Exception ex) {
        }
        
    }
*/
    
    public boolean getDocument(){
        if (getData()) {
            ArcFileUtils.WebUtils.AnalyseCharsetFromData(Details);
            if (this.Details.ContentLength <= 0){
                this.Details.ContentLength = this.Details.Data.length;
            } 
            this.isSuccess = true;
            //System.out.println(Details);
            return true;
        }else{
            return false;
        }
    }

    public void fetch() {
        this.isSuccess = false;
        if(getHeader())
            getDocument();
    }
    
   
   private String GetPrintHeadArc(){
       String head = null;
        try {
            /* Prepare Content */
            Details.ContentLength = Details.WebContent.getBytes("utf-8").length;
            
            /* Prepare Content Header */
            String ContentHeader = Details.FirstLineContentHeader + "\n"
                + "Date: " + (Details.ServerTime != 0 ? 
                    WebArcRecord.webDateFormat.format(new Date(Details.ServerTime)) : null ) + "\n"
                + "Server: " + Details.Server + "\n"
                + "Content-Type: " + Details.WebContentType + (Details.LastModified != 0 ?
                    " Last-Modified: " + WebArcRecord.webDateFormat.format(new Date(Details.LastModified)) : "");
            ContentHeader += "\n" + "Content-Length: " + Details.ContentLength + "\n";
        
            /* Prepare Record Header */
            Details.ArchiveLength = ContentHeader.getBytes("utf-8").length + Details.ContentLength;
            
            /* Write Record Header */
            head = Details.URL + " "
                        + Details.IPAddress + " "
                        + dateFormat.format(new Date()) + " "
                        + Details.ArchiveContentType + " " // DEFAULT "text/html"
                        + Details.ArchiveLength + "\n";
            
            /* Write Content Header */
            head += ContentHeader;
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Fetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        return head;
    }
    
    public Long diffServerDateTime(String Url){
        Date dt = new Date();
        this.getHeader(Url);
        if(this.Details.ServerTime > 0){
            return this.Details.ServerTime - dt.getTime();
        }else{
            return null;
        }
    }
    
    public static void main(String[] args){
        
        
        Fetcher f = new Fetcher();
        //f.fetch("http://octknight.com/download/DBProposalBalloonie.pdf");
        //f.fetch("http://127.0.0.1/test.html");
        //f.fetch("http://www.facebook.com/robots.txt");
        //f.fetch("http://www.splendith.com/");
        //f.fetch("https://pirun.ku.ac.th/pirun-tools/pirun-chkquota.php");
        //f.fetch("https://pirun.ku.ac.th/pirun-tools/pirun-chkquota.php");
        String url =  "http://doggy.igetweb.com/modules/product/addtocart.php?product_id=1002284&wid=101769";
        f.getHeader(url);
        
            for(Entry<String,List<String>> e : f.Headers.entrySet()){
                System.out.print(e.getKey() + " : {");
                for(String v : e.getValue())
                    System.out.print(v + ", ");
                System.out.println("}");
            }
        f.getDocument();
        System.out.println(f.Details.LastModified);
        
        System.out.println("--------------------------------");
        System.out.println("--------------------------------");
        System.out.println("Response: " + f.ResponseCode);
        System.out.println("--------------------------------");
        System.out.println("Charset: " + f.Details.charset);
        System.out.println("WebContenttype: " + f.Details.WebContentType);
        System.out.println("--------------------------------");
        System.out.println("==================== BEG ARC HEADER ====================");
        System.out.println(f.GetPrintHeadArc());
        System.out.println("==================== END ARC HEADER ====================");
        
        //System.out.print(f.Details.WebContent);
    }
    
    
}
