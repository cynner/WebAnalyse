/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package webanalyse;

import ArcFileUtils.MyRandomAccessFile;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author wiwat
 */
public class MainForm extends javax.swing.JFrame {

    private Thread TASK;
    private File histFile;
    private ArrayList<String> history = new ArrayList<>();
    private int hist_pos = 0;
    /**
     * Creates new form MainForm
     */
    public MainForm() {
        initComponents();
        SetConsole();
        SetHistory(new File("hist.txt"));
        LoadHistory();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        labCmd = new javax.swing.JLabel();
        btnRun = new javax.swing.JButton();
        txtCmd = new javax.swing.JTextField();
        btnStop = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtOutConsole = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtErrConsole = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        labCmd.setText("Command:");

        btnRun.setText("Run");
        btnRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRunActionPerformed(evt);
            }
        });

        txtCmd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCmdKeyPressed(evt);
            }
        });

        btnStop.setText("Stop");
        btnStop.setEnabled(false);
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labCmd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtCmd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRun)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnStop)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labCmd)
                    .addComponent(btnRun)
                    .addComponent(txtCmd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnStop))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtOutConsole.setEditable(false);
        txtOutConsole.setColumns(20);
        txtOutConsole.setRows(5);
        txtOutConsole.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtOutConsole.setVerifyInputWhenFocusTarget(false);
        jScrollPane1.setViewportView(txtOutConsole);

        txtErrConsole.setEditable(false);
        txtErrConsole.setColumns(20);
        txtErrConsole.setForeground(new java.awt.Color(204, 1, 1));
        txtErrConsole.setRows(5);
        txtErrConsole.setVerifyInputWhenFocusTarget(false);
        jScrollPane2.setViewportView(txtErrConsole);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRunActionPerformed
        RunCmd();
    }//GEN-LAST:event_btnRunActionPerformed

    private void txtCmdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCmdKeyPressed
        // TODO add your handling code here:
        int keyCode = evt.getKeyCode();
        switch (keyCode) {
            case KeyEvent.VK_UP:
                if(hist_pos > 0){
                    if(hist_pos == history.size() - 1){
                        history.set(hist_pos, txtCmd.getText());
                    }
                    hist_pos--;
                    txtCmd.setText(history.get(hist_pos));
                }
                // handle up 
                break;
            case KeyEvent.VK_DOWN:
                if(hist_pos < history.size() - 1){
                    hist_pos++;
                    txtCmd.setText(history.get(hist_pos));
                }
                // handle down 
                break;
            case KeyEvent.VK_ENTER:
                RunCmd();
                break;
        }
    }//GEN-LAST:event_txtCmdKeyPressed

    private void btnStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopActionPerformed
        // TODO add your handling code here:
        TASK.interrupt();
        btnRun.setEnabled(true);
        btnStop.setEnabled(false);
    }//GEN-LAST:event_btnStopActionPerformed

    private void RunCmd(){
        if (btnRun.isEnabled()) {
            btnRun.setEnabled(false);
            btnStop.setEnabled(true);
            Runnable r = new Runnable() {
                public void run() {
                    exeCmd();
                }
            };

            TASK = new Thread(r);
            TASK.start();
        }
        
    }
    
    private void exeCmd(){
        
        ArrayList<String> matchList = new ArrayList<>();
        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(txtCmd.getText());
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }

        String[] args = new String[matchList.size()];
        matchList.toArray(args);
        System.out.println("*** " + args[0] + " ***");
        
        AddHistory(txtCmd.getText());

        try {
            // TODO add your handling code here:
            Class<?> cls = Class.forName(args[0]);
            Method m = cls.getMethod("main", String[].class);
            String[] params = Arrays.copyOfRange(args, 1, args.length);;
            m.invoke(null, (Object) params);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("*** Finished ***");
        btnRun.setEnabled(true);
        btnStop.setEnabled(false);

    }
    
    private void SetConsole(){
        PrintStream out = new PrintStream(new ByteArrayOutputStream() {
            @Override
            public synchronized void flush() throws IOException {
                txtOutConsole.setText(toString());
            }
        }, true);
        
        PrintStream err = new PrintStream(new ByteArrayOutputStream() {
            @Override
            public synchronized void flush() throws IOException {
                txtErrConsole.setText(toString());
            }
        }, true);

        System.setErr(err);
        System.setOut(out);
    }
    
    private void SetHistory(File HistFile){
        this.histFile = HistFile;
    }
    
    private void LoadHistory() {
        if (histFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(histFile))) {
                String Line;
                while ((Line = br.readLine()) != null) {
                    history.add(Line);
                }
                hist_pos = history.size() - 1;
                if(!history.get(hist_pos).isEmpty()){
                    history.add("");
                    hist_pos++;
                }
                txtCmd.setText(history.get(hist_pos));
            } catch (IOException ex) {
                Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void AddHistory(String cmd){
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(histFile, true))){
            bw.write(cmd + "\n");
        }catch (IOException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(history.size() > 0){
            history.set(history.size() - 1, cmd);
            history.add("");
        }else{
            history.add(cmd);
            history.add("");
        }
        hist_pos = history.size() - 1;
        txtCmd.setText(history.get(hist_pos));
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRun;
    private javax.swing.JButton btnStop;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel labCmd;
    private javax.swing.JTextField txtCmd;
    private javax.swing.JTextArea txtErrConsole;
    private javax.swing.JTextArea txtOutConsole;
    // End of variables declaration//GEN-END:variables
}
