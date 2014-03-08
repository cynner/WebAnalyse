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
    public static GeoIPNode[] Mem;
    //public static long [] MinValList, MaxValList;
    //public static char [][] CountryCodeList;
    
    public static class GeoIPNode{
        long beg;
        long end;
        String ISOCode;
    }
    
    public static void main(String[] args) throws IOException{
        //ConvertCSV2Bin("/home/malang/Desktop/GeoIPCountryWhois.csv", 82078);
        LoadToMem();
        //203.158.177.12
        String HostName = "denis.darzacq.revue.com";
        //System.out.println(IP2ISOCountry("128.0.0.1"));
        try{
            InetAddress address = InetAddress.getByName(HostName);
            System.out.println(address.getHostAddress());
        }catch(UnknownHostException ex){
            System.err.println(ex.getMessage());
        }
        
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
    public static long Domain2Num (String HostName){
        try{
            InetAddress address = InetAddress.getByName(HostName);
            String [] addrs = address.getHostAddress().split("\\.");
            return Integer.parseInt(addrs[0]) * 16777216L + 
                Integer.parseInt(addrs[1]) * 65536L + 
                Integer.parseInt(addrs[2]) * 256L +
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
    
    public static long IP2Num(String IP){
        String [] addrs = IP.split("\\.");
        return Integer.parseInt(addrs[0]) * 16777216L + 
            Integer.parseInt(addrs[1]) * 65536L + 
            Integer.parseInt(addrs[2]) * 256L +
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
    
    public static String Num2ISOCountry(long Num) {
        if (Num < 0) {
            return null;
        }
        String Result = null;
        if (isLoadToMem) {
            int ORDER, POS, addr;
            GeoIPNode node;
            try{
                ORDER = 2;
                POS = 0;
                addr = 0;
                while (true) {
                    node = Mem[addr];
                    if (Num < node.beg) {
                        POS = (POS << 1) + 1;
                    } else if (Num > node.end) {
                        POS = (POS << 1);
                    } else {
                        Result = node.ISOCode;
                        break;
                    }
                    ORDER = ORDER << 1;
                    addr = (ORDER - POS - 2);
                }
                return Result;
            }catch(IndexOutOfBoundsException ex){
                Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            long CMin, CMax, POS, ORDER, ADDR;
            try (RandomAccessFile raf = new RandomAccessFile(BinPath, "r")) {
                ORDER = 2;
                POS = 0;
                while (true) {
                    CMin = raf.readLong();
                    CMax = raf.readLong();

                    if (Num < CMin) {
                        POS = (POS << 1) + 1;
                    } else if (Num > CMax) {
                        POS = (POS << 1);
                    } else {
                        Result = "" + raf.readChar() + raf.readChar();
                        break;
                    }
                    ORDER = ORDER << 1;
                    ADDR = (ORDER - POS - 2) * RecordSize;
                    raf.seek(ADDR);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return Result;
    }
    
    public static void LoadToMem() {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        if (!isLoadToMem) {
            File BinFile = new File(BinPath);
            Mem = new GeoIPNode[(int) (BinFile.length() / RecordSize)];
            try (DataInputStream dos = new DataInputStream(new FileInputStream(BinFile))) {
                for (int i = 0; i < Mem.length; i++) {
                    Mem[i] = new GeoIPNode();
                    Mem[i].beg = dos.readLong();
                    Mem[i].end = dos.readLong();
                    Mem[i].ISOCode = "" + dos.readChar() + dos.readChar();
                }
                isLoadToMem = true;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GeoIP.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
