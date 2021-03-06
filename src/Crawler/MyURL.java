/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Crawler;

import java.io.CharArrayWriter;
import java.io.UnsupportedEncodingException;
import java.net.IDN;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class MyURL {

    private String Protocol;
    private String Host;
    private String Port;
    private String Path;
    private String Query;
    private String Target;
    public String UniqURL;
    //public String Pos

    private MyURL() {
    }

    public MyURL(String URL) throws Exception {
        URL = URL.trim();
        URL = URL.replaceAll("\\r", "");
        if (URL.contains("\n")) {
            throw new MalformedURLException("Newline char could not be contain in url.");
        }
        URL = StandardURLEncode(URL);
        this.SepURL(URL);
        this.toAbsolutePath();
        this.MakeUniqueURL();
    }

    public MyURL resolve(String URL) throws Exception {
        URL = URL.trim();
        URL = URL.replaceAll("\\r", "");
        if (URL.contains("\n")) {
            throw new MalformedURLException("Newline char could not be contain in url.");
        }
        if (URL.equals("")) {
            return this;
        } else if (isDirectURL(URL)) {
            return new MyURL(URL);
        } else {
            MyURL rel = new MyURL();
            URL = StandardURLEncode(URL);
            rel.SepPathQueryTarget(URL);
            if (URL.charAt(0) == '?' || URL.charAt(0) == '#') {
                rel.Path = this.Path;
            } else if (URL.charAt(0) != '/') {
                rel.Path = this.Path.substring(0, this.Path.lastIndexOf('/') + 1) + rel.Path;
            }

            rel.Protocol = this.Protocol;
            rel.Host = this.Host;
            rel.Port = this.Port;
            rel.toAbsolutePath();
            rel.MakeUniqueURL();
            return rel;
        }
    }

    private void SepURL(String URL) throws Exception {
        int a, b;
        a = URL.indexOf("://");
        if (a < 0) {
            throw new Exception("Empty protocol.");
        }
        this.Protocol = URL.substring(0, a).toLowerCase();
        if(Protocol.contains(":") || Protocol.contains("/")){
            throw new Exception("Wrong scheme : " + Protocol);
        }
        a += 3;
        if (URL.length() <= a || URL.charAt(a) == '/' || URL.charAt(a) == ':') {
            throw new Exception("Empty host name.");
        }
        b = URL.indexOf('/', a);
        if (b < 0) {
            SepHostPort(URL.substring(a));
            a = URL.length();
        } else {
            SepHostPort(URL.substring(a, b));
            a = b;
        }

        SepPathQueryTarget(URL.substring(a));
    }

    private void SepHostPort(String HostPort) {
        int idx = HostPort.indexOf(':');
        if (idx < 0) {
            this.Host = HostPort;
            this.Port = "";
        } else {
            this.Host = HostPort.substring(0, idx);
            this.Port = HostPort.substring(idx);
            if (this.Port.equals(":80") || this.Port.equals(":")) {
                this.Port = "";
            }
        }
        this.Host = IDN.toASCII(this.Host).toLowerCase();
    }

    private void SepPathQueryTarget(String PQT) {
        int a, b;
        a = PQT.indexOf('?');
        b = PQT.indexOf('#');

        if (b >= 0) {
            if (a >= 0 && b > a) {
                this.Path = PQT.substring(0, a);
                this.Query = "?" + NormQuery(PQT.substring(a+1, b));
            } else {
                this.Path = PQT.substring(0, b);
                this.Query = "";
            }
            this.Target = PQT.substring(b);
        } else if (a >= 0) {
            this.Path = PQT.substring(0, a);
            this.Query = "?" + NormQuery(PQT.substring(a+1));
            this.Target = "";
        } else {
            if (PQT.equals("")) {
                this.Path = "/";
            } else {
                this.Path = PQT;
            }
            this.Query = "";
            this.Target = "";
        }
    }

    private static boolean isDirectURL(String URL) {
        char c;
        int n = URL.length();
        for (int i = 0; i < n && (c = URL.charAt(i)) != '/'; i++) {
            if (c == ':') {
                return true;
            }
        }
        return false;
    }

    private void toAbsolutePath() {
        String[] dir = Path.split("/");
        String Result;
        int cur = 0;
        for (String dir1 : dir) {
            if (!(dir1.equals("") || dir1.equals("."))) {
                if (dir1.equals("..")) {
                    if (cur > 0) {
                        cur--;
                    }
                } else {
                    dir[cur++] = dir1;
                }
            }
        }
        Result = "";
        for (int i = 0; i < cur; i++) {
            Result += "/" + dir[i];
        }
        /*
        if (Path.charAt(Path.length() - 1) == '/') {
            Result += "/";
        }
        */
        // Add slash tail
        if(cur <= 0 || !dir[cur-1].contains(".")){
            Result += "/";
        }
        Path = Result;
    }

    private void MakeUniqueURL() {
        this.UniqURL = this.Protocol + "://" + this.Host + this.Port + this.Path + this.Query;
    }

    public String getProtocol() {
        return this.Protocol;
    }

    public String getHost() {
        return this.Host;
    }

    public String getPath() {
        return this.Path;
    }

    public String getPort() {
        if (this.Port.equals("")) {
            return "80";
        } else {
            return this.Port.substring(1);
        }
    }

    

    public static String NormQuery(String query){
        String [] s = query.split("&");
        Arrays.sort(s);
        StringBuilder sb = new StringBuilder(s[0]);
        
        for(int i=1;i<s.length;i++){
            sb.append("&").append(s[i]);
        }
        return sb.toString();
    }
    
    public static String StandardURLEncode(String str) throws MalformedURLException{
        
        int numChars = str.length();
        StringBuilder sb = new StringBuilder(numChars > 500 ? numChars / 2 : numChars);
        int i = 0;

        char c,t;
        int v;
        int maxblen=2042,blen=2048,pos=0;
        byte[] bytes = new byte[blen];
        
        while (i < numChars) {
            c = str.charAt(i);
            switch (c) {
                case ' ':
                    sb.append("%20");
                    i++;
                    break;
                case '%':
                    /*
                     * Starting with this instance of %, process all
                     * consecutive substrings of the form %xy. Each
                     * substring %xy will yield a byte. Convert all
                     * consecutive  bytes obtained this way to whatever
                     * character(s) they represent in the provided
                     * encoding.
                     */
                    try {
                        if((i + 2) >= numChars)
                            throw new MalformedURLException ("Wrong % position in URL");
                            
                        while (((i + 2) < numChars)
                                && (c == '%')) {
                            v = Integer.parseInt(str.substring(i + 1, i + 3), 16);
                            switch(v){
                                case 0x0A:
                                    bytes[pos++] = '%';
                                    bytes[pos++] = 0x30; // 0x30 = '0' //
                                    bytes[pos++] = 0x41; // 0x41 = 'A' //
                                    break;
                                case 0x0D:
                                    bytes[pos++] = '%';
                                    bytes[pos++] = 0x30; // 0x30 = '0' //
                                    bytes[pos++] = 0x44; // 0x44 = 'D' //
                                    break;
                                case 0x20: 
                                    bytes[pos++] = '%';
                                    bytes[pos++] = 0x32; // 0x32 = '2' //
                                    bytes[pos++] = 0x30; // 0x30 = '0' //
                                    break;
                                case 0x25: 
                                    bytes[pos++] = '%';
                                    bytes[pos++] = 0x32; // 0x32 = '2' //
                                    bytes[pos++] = 0x35; // 0x30 = '5' //
                                    break;
                                default:
                                    bytes[pos++] = (byte)v;
                                    break;
                            }
                            i += 3;
                            if (i < numChars) {
                                c = str.charAt(i);
                            }
                            
                            if(pos >= maxblen){
                                sb.append(new String(bytes, 0, pos));
                                pos = 0;
                            }
                            
                        }
                        if(pos > 0){
                            sb.append(new String(bytes, 0, pos));
                            pos = 0;
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException (
                                "URLDecoder: Illegal hex characters in escape (%) pattern - "
                                + e.getMessage());
                    } catch (IndexOutOfBoundsException ex) {
                        try {
                            throw new Exception("Index out of bond.");
                        } catch (Exception ex1) {
                            Logger.getLogger(MyURL.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    }
                    break;
                default:
                    sb.append(c);
                    i++;
                    break;
            }
        }
        //if(needToChange)
        return sb.toString();
    }
    
    
     /**
     * Decodes a <code>application/x-www-form-urlencoded</code> string using a
     * specific encoding scheme. The supplied encoding is used to determine what
     * characters are represented by any consecutive sequences of the form
     * "<code>%<i>xy</i></code>".
     * <p>
     * <em><strong>Note:</strong> The <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * World Wide Web Consortium Recommendation</a> states that UTF-8 should be
     * used. Not doing so may introduce incompatibilites.</em>
     *
     * @param s the <code>String</code> to decode
     * @param enc The name of a supported
     * <a href="../lang/package-summary.html#charenc">character encoding</a>.
     * @return the newly decoded <code>String</code>
     * @exception UnsupportedEncodingException If character encoding needs to be
     * consulted, but named character encoding is not supported
     * @see URLEncoder#encode(java.lang.String, java.lang.String)
     * @since 1.4
     */
    public static String decode(String s, String enc) throws UnsupportedEncodingException {

        boolean needToChange = false;
        int numChars = s.length();
        StringBuilder sb = new StringBuilder(numChars > 500 ? numChars / 2 : numChars);
        int i = 0;

        if (enc.length() == 0) {
            throw new UnsupportedEncodingException("URLDecoder: empty string enc parameter");
        }

        char c;
        byte[] bytes = null;
        while (i < numChars) {
            c = s.charAt(i);
            switch (c) {
                case '%':
                    /*
                     * Starting with this instance of %, process all
                     * consecutive substrings of the form %xy. Each
                     * substring %xy will yield a byte. Convert all
                     * consecutive  bytes obtained this way to whatever
                     * character(s) they represent in the provided
                     * encoding.
                     */

                    try {

                    // (numChars-i)/3 is an upper bound for the number
                        // of remaining bytes
                        if (bytes == null) {
                            bytes = new byte[(numChars - i) / 3];
                        }
                        int pos = 0;

                        while (((i + 2) < numChars)
                                && (c == '%')) {
                            int v = Integer.parseInt(s.substring(i + 1, i + 3), 16);
                            if (v < 0) {
                                throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - negative value");
                            }
                            bytes[pos++] = (byte) v;
                            i += 3;
                            if (i < numChars) {
                                c = s.charAt(i);
                            }
                        }

                    // A trailing, incomplete byte encoding such as
                        // "%x" will cause an exception to be thrown
                        if ((i < numChars) && (c == '%')) {
                            throw new IllegalArgumentException(
                                    "URLDecoder: Incomplete trailing escape (%) pattern");
                        }

                        sb.append(new String(bytes, 0, pos, enc));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "URLDecoder: Illegal hex characters in escape (%) pattern - "
                                + e.getMessage());
                    }
                    needToChange = true;
                    break;
                default:
                    sb.append(c);
                    i++;
                    break;
            }
        }

        return (needToChange ? sb.toString() : s);
    }

    static BitSet dontNeedEncoding;
    static final int caseDiff = ('a' - 'A');

    /**
     * Translates a string into <code>application/x-www-form-urlencoded</code>
     * format using a specific encoding scheme. This method uses the supplied
     * encoding scheme to obtain the bytes for unsafe characters.
     * <p>
     * <em><strong>Note:</strong> The <a href=
     * "http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars">
     * World Wide Web Consortium Recommendation</a> states that UTF-8 should be
     * used. Not doing so may introduce incompatibilites.</em>
     *
     * @param s   <code>String</code> to be translated.
     * @param enc The name of a supported
     * <a href="../lang/package-summary.html#charenc">character encoding</a>.
     * @return the translated <code>String</code>.
     * @exception UnsupportedEncodingException If the named encoding is not
     * supported
     * @see URLDecoder#decode(java.lang.String, java.lang.String)
     * @since 1.4
     */
    public static String encode(String s, String enc)
            throws UnsupportedEncodingException {

        boolean needToChange = false;
        StringBuilder out = new StringBuilder(s.length());
        Charset charset;
        CharArrayWriter charArrayWriter = new CharArrayWriter();

        if (enc == null) {
            throw new NullPointerException("charsetName");
        }

        try {
            charset = Charset.forName(enc);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException(enc);
        }

        for (int i = 0; i < s.length();) {
            int c = (int) s.charAt(i);
            //System.out.println("Examining character: " + c);
            if (dontNeedEncoding.get(c)) {
                if (c == ' ') {
                    /*out.append('%');
                     out.append('2');
                     out.append('0');*/
                    out.append("%20");
                    needToChange = true;
                } else {
                    //System.out.println("Storing: " + c);
                    out.append((char) c);
                }
                i++;
            } else {
                // convert to external encoding before hex conversion
                do {
                    charArrayWriter.write(c);
                    /*
                     * If this character represents the start of a Unicode
                     * surrogate pair, then pass in two characters. It's not
                     * clear what should be done if a bytes reserved in the
                     * surrogate pairs range occurs outside of a legal
                     * surrogate pair. For now, just treat it as if it were
                     * any other character.
                     */
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        /*
                         System.out.println(Integer.toHexString(c)
                         + " is high surrogate");
                         */
                        if ((i + 1) < s.length()) {
                            int d = (int) s.charAt(i + 1);
                            /*
                             System.out.println("\tExamining "
                             + Integer.toHexString(d));
                             */
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                /*
                                 System.out.println("\t"
                                 + Integer.toHexString(d)
                                 + " is low surrogate");
                                 */
                                charArrayWriter.write(d);
                                i++;
                            }
                        }
                    }
                    i++;
                } while (i < s.length() && !dontNeedEncoding.get((c = (int) s.charAt(i))));

                charArrayWriter.flush();
                String str = new String(charArrayWriter.toCharArray());
                byte[] ba = str.getBytes(charset);
                for (int j = 0; j < ba.length; j++) {
                    out.append('%');
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                }
                charArrayWriter.reset();
                needToChange = true;
            }
        }

        return (needToChange ? out.toString() : s);
    }
    
    
    
    public static void main(String[] args) throws Exception {
        String URLLL= "http://edutainment.ku.ac.th./";
        MyURL A = new MyURL(URLLL);
        MyURL B = A.resolve("gg@jabber/index.php");
        System.out.println(A.UniqURL);
        System.out.println(B.UniqURL);   
    }
}
