/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.util.HashMap;

/**
 *
 * @author malang
 */
public class ArgUtils {
    public static HashMap<String,String> Parse(String[] args){
        HashMap<String,String> Data = new HashMap<String,String>();
        int cnt = 0;
        for(int i=0;i<args.length;i++){
            if(args[i].startsWith("--")){
                Data.put(args[i].substring(2),"");
            }else if(args[i].startsWith("-")){
                Data.put(args[i].substring(1), args[++i]);
            }else{
                Data.put("" + ++cnt,args[i]);
            }
        }
        return Data;
    }
}
