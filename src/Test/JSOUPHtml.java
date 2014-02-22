/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author malang
 */
public class JSOUPHtml {
    public static void main(String[] args){
        String Content="<html><head>Bukuk dd er lang</head><!-- BBHH --><body><div  id='gug'> <!-- GGYY -->  <style>    f\nhh</style></div></body></html>";
        Document doc = Jsoup.parse(Content);
        doc.outputSettings().prettyPrint(false);
        Elements es = doc.select("div");
        //doc.outputSettings().prettyPrint(false);
        //Elements es = doc.select("div");
        System.out.println(es.outerHtml());
        es.remove();
        doc.outputSettings().prettyPrint(true);
        //Elements es = doc.select("div");
        
        System.out.println(es.outerHtml());
        System.out.println(doc.html());
        
    }
    
}
