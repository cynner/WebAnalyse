/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Crawler;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    
    public static void ConvertCSV2Bin(String CSVPath, int N) throws FileNotFoundException, IOException {
        int LC, RC, CC, CEN = N / 2;
        int POS, ORDER;
        long beg, end;
        String ISOCode;
        //RandomAccessFile raf;
        try (BufferedReader br = new BufferedReader(new FileReader(CSVPath))) {
            try (RandomAccessFile raf = new RandomAccessFile(BinPath, "rw")) {
                String strLine;
                String[] strFields;
                for (int i = 0; i < N; i++) {
                    LC = 0;
                    RC = N;
                    CC = CEN;
                    ORDER = 1;
                    POS = 0;
                    while (i != CC) {
                        if (i < CC) {
                            RC = CC;
                            POS = (POS << 1) + 1;
                        } else {
                            LC = CC;
                            POS = (POS << 1);
                        }
                        CC = (LC + RC) / 2;
                        ORDER++;
                    }

                    POS = ((1 << ORDER) - POS - 2) * RecordSize;

                    strLine = br.readLine();
                    System.out.println(strLine);
                    strFields = strLine.split(",", 6);

                    beg = Long.parseLong(strFields[2].replaceAll("\"", ""));
                    end = Long.parseLong(strFields[3].replaceAll("\"", ""));
                    ISOCode = strFields[4].replaceAll("\"", "");

                    raf.seek(POS);
                    raf.writeLong(beg);
                    raf.writeLong(end);
                    raf.writeChars(ISOCode);
                }
            }
        }
    }
    
    //"www.yahoo.com"
    public static int Domain2Num (String HostName){
        try{
            InetAddress address = InetAddress.getByName(HostName);
            String [] addrs = address.getHostAddress().split("\\.");
            return Integer.parseInt(addrs[0]) * 16777216 + 
                Integer.parseInt(addrs[1]) * 65536 + 
                Integer.parseInt(addrs[2]) * 256 +
                Integer.parseInt(addrs[3]);
        }catch(UnknownHostException ex){
            System.err.println("Unknown host name : " + HostName);
            Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
    }
    
    public static String IP2ISOCountry(String IP){
        return Num2ISOCountry(IP2Num(IP));
    }
    
    public static int IP2Num(String IP){
        String [] addrs = IP.split("\\.");
        return Integer.parseInt(addrs[0]) * 16777216 + 
            Integer.parseInt(addrs[1]) * 65536 + 
            Integer.parseInt(addrs[2]) * 256 +
            Integer.parseInt(addrs[3]);
    }
    
    public static String Domain2IP(String HostName){
        try{
            InetAddress address = InetAddress.getByName(HostName);
            return address.getHostAddress();
        }catch(UnknownHostException ex){
            System.err.println("Unknown host name : " + HostName);
            Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
            
    }
    
    public static String Domain2ISOCountry (String HostName){
        return Num2ISOCountry(Domain2Num(HostName));
    }
    
    public static String Num2ISOCountry (int Num){
        if(Num < 0){
            return null;
        }
        long CMin,CMax,POS,ORDER,ADDR;
        String Result;
        try(RandomAccessFile raf = new RandomAccessFile(BinPath, "r")){
            ORDER = 2;
            POS = 0;
            while(true){
                CMin = raf.readLong();
                CMax = raf.readLong();
                
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
            return Result;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex){
            Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    
}
