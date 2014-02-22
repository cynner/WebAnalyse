/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author malang
 */
public class ExampleThread implements Runnable{
    private Exception InterruptedException;
    
    public ExampleThread(){
        
    }
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName()+" Start.");
        processCommand();
        System.out.println(Thread.currentThread().getName()+" End.");
    }
 
    private void processCommand() {
        try {
            while(true){
                Thread.sleep(1000);
                if(Thread.interrupted()){
                    System.out.println("gug" + Thread.currentThread());
                    break;
                }
            }
        } catch (java.lang.InterruptedException ex) {
            Logger.getLogger(ExampleThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
