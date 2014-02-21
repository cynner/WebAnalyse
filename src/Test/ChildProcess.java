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
public class ChildProcess {
    public static void main(String[] args) throws IOException{
        Runtime.getRuntime().exec("cmd");
    }
}
