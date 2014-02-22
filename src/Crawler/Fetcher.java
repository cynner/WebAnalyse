/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Crawler;

import ArcFileUtils.WebArcRecord;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author malang
 */
public class Fetcher {

    
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    

    public boolean isSuccess;
    public Map<String , List<String>> Headers;
    public HttpURLConnection uc;
    
    /* Details */
    public WebArcRecord Details = new WebArcRecord();
    public int ResponseCode;
    //public long ContentLength;
    public long LastModified;
    public long Date;
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
            this.Date = uc.getDate();
            this.Details.Server = uc.getHeaderField("Server");

            this.LastModified = uc.getLastModified();
            this.Details.ContentLength = uc.getContentLengthLong();
            this.Headers = uc.getHeaderFields();
            this.Details.FirstLineContentHeader = uc.getHeaderField(0);
            
            this.Details.LastModified = dateFormat.format(new Date(this.LastModified));

            // Find Charset

            this.Details.WebContentType = this.ContentTypeOrg;
            this.Details.ArchiveContentType = this.ContentTypeOrg.replaceAll(";.*", "").trim().toLowerCase();
            this.Details.AnalyseCharset();
        }catch (SocketTimeoutException ex) {
            // this.ResponseCode = 408;
            System.err.println(ex.getMessage());
            return false;
        } catch (ProtocolException ex) {
            // Never happen
            System.err.println(ex.getMessage());
            return false;
        } catch (IOException ex) {
            // IO
            System.err.println(ex.getMessage());
            return false;
        } catch (Exception e){
            System.err.println(e.getMessage());
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
            InputStream is = uc.getInputStream();
            this.Details.Data = IOUtils.toByteArray(is);
            is.close();
            uc.disconnect();
            return true;
        } catch (SocketTimeoutException ex) {
            //this.ResponseCode = 408;
            System.err.println(ex.getMessage());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
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
        getHeader();
        getDocument();
    }
    
    public String getArcHeaderV1(){
        return String.format("%s\nDate: %s\nServer: %s\nContent-Type: %s Last-Modified: %s\nContent-Length: %d", 
                this.Details.FirstLineContentHeader,
                dateFormat.format(new Date(this.Date)),
                this.Details.Server,
                this.Details.WebContentType,
                dateFormat.format(new Date(this.LastModified)),
                this.Details.ContentLength
                );
    }
    
    
    
    public static void main(String[] args){
        Fetcher f = new Fetcher();
        //f.fetch("http://octknight.com/download/DBProposalBalloonie.pdf");
        //f.fetch("http://127.0.0.1/test.html");
        //f.fetch("http://www.facebook.com/robots.txt");
        //f.fetch("http://www.splendith.com/");
        //f.fetch("https://pirun.ku.ac.th/pirun-tools/pirun-chkquota.php");
        //f.fetch("https://pirun.ku.ac.th/pirun-tools/pirun-chkquota.php");
        String url =  "https://portal.trueinternet.co.th/wifiauthen/mobile/wifi-login.php?param=Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg==";
        f.getHeader(url);
        
            for(Entry<String,List<String>> e : f.Headers.entrySet()){
                System.out.print(e.getKey() + " : {");
                for(String v : e.getValue())
                    System.out.print(v + ", ");
                System.out.println("}");
            }
        f.getDocument();
        
        /*
        null : {HTTP/1.0 302 Moved Temporarily, }
        MIME-Version : {1.0, }
Allow : {GET,POST,HEAD, }
Location : {https://portal.trueinternet.co.th/wifiauthen/login.do?wlanuserip=10.66.96.111&Wifi=&nasip=202.176.86.202&wlanparameter=c8-3a-35-ce-3e-8a&VLAN=eth/4/0/0:773.222, }
Server : {NetEngine Server 1.0, }
Pragma : {No-Cache, }
--------------------------------
--------------------------------
Response: 302
--------------------------------
Charset: UTF-8
WebContenttype: null
--------------------------------
<!--
<?xml version="1.0" encoding="UTF-8"?>
<WISPAccessGatewayParam xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://www.acmewisp.com/WISPAccessGatewayParam.xsd">
<Proxy>
<MessageType>110</MessageType>
<NextURL>https://portal.trueinternet.co.th/wifiauthen/login.do?wlanuserip=10.66.96.111&Wifi=&nasip=202.176.86.202&wlanparameter=c8-3a-35-ce-3e-8a</NextURL>
<ResponseCode>200</ResponseCode>
</Proxy>
</WISPAccessGatewayParam>
-->

        */
        
        
        /*
        Step 2
        <META HTTP-EQUIV="Refresh" CONTENT="0;url=https://portal.trueinternet.co.th/wifiauthen/mobile/wifi-login.php?param=Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg=="></head>
        */
        /*
        run:
null : {HTTP/1.1 200 OK, }
Date : {Wed, 01 Jan 2014 00:51:41 GMT, }
Content-Length : {8000, }
Keep-Alive : {timeout=5, max=100, }
Content-Type : {text/html, }
Connection : {Keep-Alive, }
Server : {Apache, }
--------------------------------
--------------------------------
Response: 200
--------------------------------
Charset: utf-8
WebContenttype: text/html
--------------------------------
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
<title>Truewifi</title>
<link href="css/layout.css" rel="stylesheet" type="text/css">
<script language="JavaScript">
			var click=0;
			function submitForm() {
				if( form.username.value=="" || form.password.value=="") {
					alert("Username & Password Required");
					form.username.focus();
					return false;
				}
				submitted = true;
			}

			function buywifiForm() {
				document.getElementById('username2').value = document.getElementById('username').value;
				document.form2.submit();
			}

			function signupForm() {
				document.getElementById('username3').value = document.getElementById('username').value;
				document.form3.submit();
			}

			function forgetpasswordForm() {
				document.getElementById('username4').value = document.getElementById('username').value;
				document.form4.submit();
			}

			function clearForm() {
				document.form.username.value = "";
				document.form.password.value = "";
				document.form.remember.checked = false;
			}
</script>
</head>
<!--
<WISPAccessGatewayParam
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:noNamespaceSchemaLocation="http://www.acmewisp.com/WISPAccessGatewayParam.xsd">
	<Redirect>
		<AccessProcedure>1.0</AccessProcedure>
		<AccessLocation>Default Location ID</AccessLocation>
		<LocationName>Default Location Name</LocationName>
		<LoginURL>https://portal.trueinternet.co.th/wifiauthen/authen.do</LoginURL>
		<AbortLoginURL>https://portal.trueinternet.co.th/wifiauthen/logout_result.php</AbortLoginURL>
		<MessageType>100</MessageType>
		<ResponseCode>0</ResponseCode>
	</Redirect>
</WISPAccessGatewayParam>
-->
<body>
<form name="form" method="post" action="../login_result.php" onSubmit="return submitForm()">
<div class="content">
<!--logo-->
<div class="logo">
<img src="images/logo300x150.png" alt="" width="195" height="150" title="truewifi"/>
					<img src="images/711logo.jpg" />
</div>
<!--logo-->

<!--username-->
<div class="wrap">
<label> Username | ชื่อผู้ใช้ </label>
<div class="field_box"><input name="username"  type="text" class="field" id = "username" onkeypress="return(event.keyCode != 32&&event.which!=32)" value = "" onkeyup="sync()"/>
</div>
</div>
<!--username-->
<!--username-->
<div class="wrap">
<div class="field_box text_remark float-left"><strong>TrueMove/Truemove H No./True ID or Registered Login</strong><br />
  ใส่หมายเลข TrueMove/TrueMove H/True ID หรือ ชื่อผู้ใช้
</div>
</div>
<!--username-->

<div class="wrap">
<label >Password | รหัสผ่าน</label>
<div class="field_box"><input name="password" type="password" class="field" value = ""/>
</div>
</div>
<!--forgetpassword
<div class="wrap">
<div class="forgetpass"><a href="#" rel="nofollow" title="forgetpassword">Forget Password | ลืมรหัสผ่าน</a></div>
</div>
-->

<!--rememberme-->

<div class="wrap">
<div class="rememberme">
<div class="text float-left">
						<input name="remember" class="choice" type="checkbox" id="remember" value="1" />
Remember me | จำชื่อผู้ใช้ 
</div>

<div class="clear"></div>

<a href="faq.php?param=Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg==" class="forgotpass_btn float-left">&nbsp;&nbsp;&nbsp;&nbsp;Instruction Click here | ข้อแนะนำ คลิกที่นี่</a>

<div class="clear"></div>

<div style="height:5px"></div>

<input name="confirm" type="image" class="float-left" id="confirm" border="0" src="images/btn_login.png" onclick="if(submitted)this.disabled = true"/>

			<a href="javascript:buywifiForm();" class="float-left"><img src="images/btn_wifipackage.png" alt="btn_login" title="btn_login" /></a>
	<div class="clear"></div>
</div>
<!--rememberme-->
<!--Login-->
<div class="btn_wrap">
</div>
<div class="clear"></div>
<!--Login-->
<!--forgetpass-->
<div style="height:30px"></div>
<div class="wrap" >
<a href="javascript:forgetpasswordForm();" class="forgotpass"  >Forgot password | ลืมรหัสผ่าน คลิ๊กที่นี่</a>
</div>
<!--forgetpass-->
<!--btn-->
<div class="btn_wrap">
<a href="javascript:signupForm();" class="float-left"><img src="images/btn_forgetpass.png" alt="btn_login"  height="50" title="btn_login" /></a><div class="clear"></div>
</div>
<!--btn-->
<!--link-->
<div class="frame_wrapper">
<a href="ksc-login.php?param=Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg=="  class="frame"><span class="text_yellow"><strong>KSC Customers</strong></span><br /><br />
ลูกค้า KSC
</a>

<a href="ir-login.php?param=Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg=="  class="frame"><span class="text_yellow"><strong>International Roaming</strong></span><br /><br />บริการ WiFi ระหว่างประเทศ</a>
	<a href="buywifi-cashcard.php?param=Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg=="  class="frame" ><span class="text_red"><strong>Buy WiFi with True Money Cash Card</strong></span><br /><br />ซื้อชั่วโมง WiFi ด้วย ทรูมันนี่ แคช การ์ด</>
	<!--
<a href="http://portal.trueinternet.co.th/dial_conv_wifi/home.php"  class="frame">
<span class="text_yellow">
<strong>Buy WiFi with NetKit by True Online</strong>
</span>
<br /><br />
ซื้อชั่วโมง WiFi ด้วยเน็ตคิทจากทรูออนไลน์</a>
-->
<a href="redeem/redeem.php?param=Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg=="  class="frame">
<span class="text_yellow">
<strong>Redeem Code</strong>
</span>
<br /><br />
แลกรับรหัส WiFi</a>
					<a href="http://services.trueinternet.co.th/greenbkk/index_year2.php?MAC=c8:3a:35:ce:3e:8a"  class="frame"><span class="text_yellow"><strong>Register Bangkok WiFi</strong></span><br />ลงทะเบียน BangKok WiFi</a>
			<a href="http://services.trueinternet.co.th/greenbkk/extend.php"  class="frame"><span class="text_yellow"><strong>Renew Bangkok WiFi Account</strong></span><br />ต่ออายุการใช้งาน Bangkok WiFi</a>
		

<a href="http://support2.truecorp.co.th/detail.aspx?document_id=110"  class="frame"><span class="text_yellow"><strong>How to use WiFi</strong></span><br /><br />
วิธีการใช้งาน WiFi</a>

<a href="http://www.truewifi.net/2011/support.htm"  class="frame"><span class="text_yellow"><strong>Troubleshooting


</strong></span><br /><br />
การแก้ไขปัญหา</a>

</div>
<!--link-->


</div>
<input type ="hidden" name ="param" value="Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg=="/>
</form>

<form name="form2" method="post" action="../verify_user_buywifi.php">
	<input type ="hidden" name = "uname" id = "username2"/>
	<input type ="hidden" name = "param" id = "param" value = "Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg=="/>
</form>

<form name="form3" method="post" action="../verify_user_signup.php">
	<input type ="hidden" name = "username3" id = "username3"/>
	<input type ="hidden" name = "param" id = "param" value = "Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg=="/>
	<input type ="hidden" name = "page" id = "page" value = "wifi-login"/>
</form>

<form name="form4" method="post" action="../verify_user_forgot.php">
	<input type ="hidden" name = "username4" id = "username4"/>
	<input type ="hidden" name = "param" id = "param" value = "Yzg6M2E6MzU6Y2U6M2U6OGF8MjAxNDAxMDEwNzUwCg=="/>
	<input type ="hidden" name = "page" id = "page" value = "wifi-login"/>
</form>
</body>
</html>
BUILD SUCCESSFUL (total time: 1 second)

        */
        System.out.println("--------------------------------");
        System.out.println("--------------------------------");
        System.out.println("Response: " + f.ResponseCode);
        System.out.println("--------------------------------");
        System.out.println("Charset: " + f.Details.charset);
        System.out.println("WebContenttype: " + f.Details.WebContentType);
        System.out.println("--------------------------------");
        System.out.print(f.Details.WebContent);
        
        /*
        try {
            System.out.print(new String(f.Data,"windows-874"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Fetcher.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        /*
        if(f.isSuccess){
            System.out.println("Success -----------------------");
            //System.out.println(f.getArcHeaderV1());
            
            //System.out.println("CacheControl : " + f.CacheControl);
            System.out.println("ResponseCode : " + f.ResponseCode);
            System.out.println("Date : " + f.Date);
            System.out.println("LastModified : " + f.LastModified);
            System.out.println("Server : " + f.Server);
            System.out.println("Encoding : " + f.ContentEncoding);
            System.out.println("ContentTypeOrg : " + f.ContentTypeOrg);
            System.out.println("ContentType : " + f.ContentType);
            System.out.println("ContentLength : " + f.ContentLength);
            
            for(Entry<String,List<String>> e : f.Headers.entrySet()){
                System.out.print(e.getKey() + " : {");
                for(String v : e.getValue())
                    System.out.print(v + ", ");
                System.out.println("}");
            }
            try {
                //System.out.println("Content : ----------------- ");
                //System.out.println(f.Content);
                f.Content = new String(f.Content.getBytes("windows-874"), "utf-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Fetcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Jsoup : -----------------");
            Document doc = Jsoup.parse(f.Content);
            doc.select("script,style").remove();
            doc.outputSettings().indentAmount(1);
            doc.outputSettings().prettyPrint(true);
            System.out.println(doc.html());
            
        }else{
            /*
            System.out.println("Fail -----------------------");
            System.out.println("ResponseCode : " + f.ResponseCode);
            for(Entry<String,List<String>> e : f.Headers.entrySet()){
                System.out.print(e.getKey() + " : {");
                for(String v : e.getValue())
                    System.out.print(v + ", ");
                System.out.println("}");
            }
             * 
             */
        //}
        /*
        try {
            String x = "Hello";
            System.out.println(x + ":");
            System.out.println(x.getBytes("utf-8").length);
            x = "บากกาก";
            System.out.println(x + ":");
            System.out.println(x.getBytes("utf-8").length);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Fetcher.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        
    }
    /*
    String post(String Url, String[] PostVar, String[] PostVal){
    String content = "";
    try {
    // Construct data
    String data = URLEncoder.encode("passwd", "UTF-8") + "=" + URLEncoder.encode("xBzU3eNxQtK", "UTF-8");
    for (int i=0;i<PostVar.length;i++){
    data += "&" + URLEncoder.encode(PostVar[i], "UTF-8") + "=" + URLEncoder.encode(PostVal[i], "UTF-8");
    }
    
    // Send data
    URL url = new URL(Url);
    URLConnection conn = url.openConnection();
    
    conn.setDoOutput(true);
    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
    wr.write(data);
    wr.flush();
    
    // Get the response
    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String line;
    while ((line = rd.readLine()) != null) {
    // Process line...
    content += line + "\n";
    }
    wr.close();
    rd.close();
    } catch (Exception e) {
    content = null;
    }
    System.err.println(content);
    return content;
    }
     * 
     */
}
