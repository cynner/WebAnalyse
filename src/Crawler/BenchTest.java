/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Crawler;

/**
 *
 * @author malang
 */
public class BenchTest {
    public static void main(String arguments[]) {


        //Build a long string
        StringBuilder sb = new StringBuilder();
        for(int j = 0; j < 10000; j++) {
            sb.append("a really, really long string");
        }
        String str = sb.toString();
        for (int testscount = 0; testscount < 10; testscount ++) {


            //Test 1
            long start = System.currentTimeMillis();
            for(int c = 0; c < 10000000; c++) {
                for (int i = 0, n = str.length(); i < n; i++) {
                    char chr = str.charAt(i);
                    doSomethingWithChar(chr);//To trick JIT optimistaion
                }
            }

            System.out.println("1: " + (System.currentTimeMillis() - start));

            //Test 2
            start = System.currentTimeMillis();
            char[] chars = str.toCharArray();
            for(int c = 0; c < 10000000; c++) {
                for (int i = 0, n = chars.length; i < n; i++) {
                    char chr = chars[i];
                    doSomethingWithChar(chr);//To trick JIT optimistaion
                }
            }
            System.out.println("2: " + (System.currentTimeMillis() - start));
            System.out.println();
        }


    }


    public static void doSomethingWithChar(char chr) {
        int newInt;
        /*
        switch(chr){
            case 'a': 
                newInt = chr << 2;
                break;
            case 'b':
                newInt = chr << 2;
                break;
            case 'c':
                newInt = chr << 2;
                break;
            case 'd':
                newInt = chr << 2;
                break;
            case 'e':
                newInt = chr << 2;
                break;
            default:
                newInt = chr << 2;
                break;
                
        }*/
        
        if(chr == 'a'){
            
                newInt = chr << 2;
        }else if(chr == 'b'){
            
                newInt = chr << 2;
        }else if(chr == 'c'){
            
                newInt = chr << 2;
        }else if(chr == 'd'){
            
                newInt = chr << 2;
        }else if(chr == 'e'){
            
                newInt = chr << 2;
        }else{
            
                newInt = chr << 2;
        }
    }
}
