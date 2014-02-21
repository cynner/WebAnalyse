/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
/**
 *
 * @author malang
 */
public class GetOwnPID {
    public static void main(String[] args) throws IOException, NoSuchFieldException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException{
        gg();
        gg();
        while(true){
            
        }
    }
    
    public static void gg()throws IOException, NoSuchFieldException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException{
        java.lang.management.RuntimeMXBean runtime = 
        java.lang.management.ManagementFactory.getRuntimeMXBean();
        java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
        jvm.setAccessible(true);
        sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
        java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
        pid_method.setAccessible(true);
        int pid = (Integer) pid_method.invoke(mgmt);
        System.err.println(pid);
    }
}
