/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Test;

import java.io.IOException;

/**
 *
 * @author malang
 */
public class KillProcess {
    public static void main(String[] args) throws IOException{
        int PID = 100;
        if(OSValidator.isWindows()){
            Runtime.getRuntime().exec("taskkill /F /PID " + PID);
        }else{
            Runtime.getRuntime().exec("kill -9 " + PID);
        }
    }
}
