/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Crawler;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
//import java.nio.ByteBuffer;
//import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class GeoIP {
    //"GeoIPCountry.csv"
    static final int RecordSize = 20;
    static String BinPath = "resource/GeoIPCountry.bin";
    public static boolean isLoadToMem = false;
    public static long [] MinValList, MaxValList;
    public static char [][] CountryCodeList;
    
    public static void main(String[] args) throws IOException{
        //ConvertCSV2Bin("/home/malang/Desktop/GeoIPCountryWhois.csv", 82078);
        System.out.println(IP2ISOCountry("202.183.235.2"));
    }
    
    public static void ConvertCSV2Bin(String CSVPath, int N) throws FileNotFoundException, IOException{
        int LC, RC, CC, CEN=N/2;
        int POS,ORDER;
        long beg,end;
        String ISOCode; 
        
        FileInputStream is = new FileInputStream(CSVPath);
        DataInputStream dis = new DataInputStream(is);
        BufferedReader br = new BufferedReader(new InputStreamReader(dis));
        
        //FileOutputStream os = new FileOutputStream(BinPath,true);
        //DataOutputStream dos = new DataOutputStream(os);
        RandomAccessFile raf = new RandomAccessFile(BinPath, "rw");
        
        //ByteBuffer b = ByteBuffer.allocate(RecordSize);
        
        String strLine;
        String[] strFields = null;
        
        
                //Scanner scan = new Scanner(System.in);
        //while ((strLine = br.readLine()) != null)   {
        
        for(int i=0; i<N; i++){
            LC = 0;
            RC = N;
            CC = CEN;
            ORDER = 1;
            POS = 0;
            while(i!=CC){
                if(i<CC){
                    RC = CC;
                    POS = (POS << 1) + 1;
                }else{
                    LC = CC;
                    POS = (POS << 1);
                }
                CC = (LC + RC) / 2;
                ORDER++;
            }
            //Address = (2 ^ ORDER) - POS - 2
            POS = ((1 << ORDER) - POS - 2) * RecordSize;
            
            
            strLine = br.readLine();
            System.out.println(strLine);
            strFields = strLine.split(",", 6);
            
            
            
            beg = Long.parseLong(strFields[2].replaceAll("\"", ""));
            end = Long.parseLong(strFields[3].replaceAll("\"", ""));
            ISOCode = strFields[4].replaceAll("\"", "");
            
            //b.putInt(0,beg);
            //b.putInt(end);
            //b.putChar(ISOCode.charAt(0));
            //b.putChar(ISOCode.charAt(1));
            
            
            //System.out.println(POS + " " + b.array().length);
            //os.write(b.array(), POS, RecordSize);
            raf.seek(POS);
            raf.writeLong(beg);
            raf.writeLong(end);
            raf.writeChars(ISOCode);
            
                
        }
        
        //b.close();
        dis.close();
        raf.close();
        //dos.close();
            
    }
    
    //"www.yahoo.com"
    public static long Domain2Num (String HostName){
        try{
            InetAddress address = InetAddress.getByName(HostName);
            String [] addrs = address.getHostAddress().split("\\.");
            return Long.parseLong(addrs[0]) * 16777216L + 
                Long.parseLong(addrs[1]) * 65536L + 
                Long.parseLong(addrs[2]) * 256L +
                Long.parseLong(addrs[3]);
        }catch(UnknownHostException e){
            System.err.println("Unknown host name : " + HostName);
            return -1;
        }
    }
    
    public static String IP2ISOCountry(String IP){
        return Num2ISOCountry(IP2Num(IP));
    }
    
    public static long IP2Num(String IP){
        String [] addrs = IP.split("\\.");
        return Long.parseLong(addrs[0]) * 16777216L + 
            Long.parseLong(addrs[1]) * 65536L + 
            Long.parseLong(addrs[2]) * 256L +
            Long.parseLong(addrs[3]);
    }
    
    public static String Domain2IP(String HostName){
        try{
            InetAddress address = InetAddress.getByName(HostName);
            return address.getHostAddress();
        }catch(UnknownHostException e){
            return null;
        }
            
    }
    
    public static String Domain2ISOCountry (String HostName){
        return Num2ISOCountry(Domain2Num(HostName));
    }
    
    public static String Num2ISOCountry (long Num){
        if(Num < 0){
            return null;
        }
        //FileInputStream is = new FileInputStream(BinPath);
        //DataInputStream dis = new DataInputStream(is);
        long CMin,CMax,POS,ORDER,ADDR;
        RandomAccessFile raf;
        String Result;
        try {
            raf = new RandomAccessFile(BinPath, "r");
            ADDR = 0;
            ORDER = 2;
            POS = 0;
            while(true){
                CMin = raf.readLong();
                CMax = raf.readLong();
                //System.out.println(CMin + " " + CMax);
                if(Num < CMin){
                    POS = (POS << 1) + 1;
                }else if(Num > CMax){
                    POS = (POS << 1);
                }else{
                    Result = "" + raf.readChar() + raf.readChar(); 
                    break;
                }
                ORDER = ORDER << 1;
                ADDR = (ORDER - POS - 2) * RecordSize;
                raf.seek(ADDR);
            }
            raf.close();
            return Result;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex){
            //ex.printStackTrace();
        }
        return null;
    }
    
    public static void LoadToMem(){
        FileInputStream is;
        try {
            is = new FileInputStream(BinPath);
            DataInputStream dis = new DataInputStream(is);
            isLoadToMem = true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
