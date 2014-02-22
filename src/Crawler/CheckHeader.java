/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

import java.util.List;
import java.util.Map;

/**
 *
 * @author malang
 */
public class CheckHeader {
    public static void main(String [] args){
        Fetcher f = new Fetcher();
        f.getHeader("http://fp2.fsanook.com/2/demo/flag-01.png");
        
        System.out.println("URL: " + f.Url);
        System.out.println("Code: " + f.ResponseCode);
        System.out.println("Encode: " + f.ContentEncoding);
        System.out.println("ContentTypeOrg: " + f.ContentTypeOrg);
        System.out.println("Header Content --------------------");
        
        for(Map.Entry<String,List<String>> e : f.Headers.entrySet()){
            System.out.print(e.getKey() + ": ");
            for(String s : e.getValue()){
                System.out.print(s + ", ");
            }
            System.out.println();
        }
    }
}
