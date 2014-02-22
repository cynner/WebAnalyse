/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author malang
 */
public class Lexical {

    public Lexical() {
        this.WhiteSpace = new ArrayList<>();
    }
    public class CharNode{
        public char key;
        public boolean val;
        public CharNode next;
        public CharNode child;
        
        public CharNode(char key, boolean val){
            this.key = key;
            this.val = val;
            this.next = null;
            this.child = null;
        }
        
        public CharNode(char key){
            this.key = key;
            this.val = false;
            this.next = null;
            this.child = null;
        }
    }
    
    private CharNode WordTree = null;
    private CharNode _curPtr;
    
    //private HashMap<Character, Integer> Mid;
    //private HashMap<Character, Integer> Last;
    public ArrayList<Character> WhiteSpace;
    
    public void InitialWhiteSpace(){
        WhiteSpace.add(' ');
        WhiteSpace.add('\n');
        WhiteSpace.add('\'');
        WhiteSpace.add('\"');
        WhiteSpace.add(',');
        WhiteSpace.add('.');
        WhiteSpace.add('(');
        WhiteSpace.add(')');
        WhiteSpace.add('[');
        WhiteSpace.add(']');
        WhiteSpace.add('+');
        WhiteSpace.add('“');
        WhiteSpace.add('”');
        
    }
    
    private void BeginInsert(){
        if(WordTree == null){
            WordTree = new CharNode(' ');
        }else if(WordTree.key != ' '){
            _curPtr = new CharNode(' ');
            _curPtr.child = WordTree;
            WordTree = _curPtr;
        }
    }
    
    private void InsertWordTree(String Words){
        char c;
        CharNode tmpPtr = null;
        CharNode parentPtr = null;
        int lenm1 = Words.length();
        
        parentPtr = WordTree;
        
        for(int i = 0; i < lenm1; i++) {
            c = Words.charAt(i);
            if(parentPtr.child == null){
                parentPtr.child = new CharNode(c);
                parentPtr = parentPtr.child;
            } else {
                _curPtr = parentPtr.child;
                if (c > _curPtr.key) {
                    while (_curPtr.next != null && c > _curPtr.next.key) {
                        _curPtr = _curPtr.next;
                    }
                    if (_curPtr.next == null) {
                        _curPtr.next = new CharNode(c);
                        _curPtr = _curPtr.next;
                    } else if (c < _curPtr.next.key) {
                        tmpPtr = new CharNode(c);
                        tmpPtr.next = _curPtr.next;
                        _curPtr.next = tmpPtr;
                        _curPtr = tmpPtr;
                    } else {
                        _curPtr = _curPtr.next;
                    }
                } else if (c < _curPtr.key) {
                    _curPtr = new CharNode(c);
                    _curPtr.next = parentPtr.child;
                    parentPtr.child = _curPtr;
                } //else Do nothing
                parentPtr = _curPtr;
            }
        }
        parentPtr.val = true;
    }
    
    private void EndInsert(){
        if(WordTree != null && WordTree.key == ' '){
            _curPtr = WordTree;
            WordTree = WordTree.child;
            _curPtr = null;
        } // else {/*do nothing*/}
    }
    
    public void ConstructWordTree(File f) {
        BeginInsert();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String Line;
            while ((Line = br.readLine()) != null) {
                if (!Line.equals("")) {
                    InsertWordTree(Line);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Lexical.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Lexical.class.getName()).log(Level.SEVERE, null, ex);
        } 
        EndInsert();
        InitialWhiteSpace();
    }
    
    public void TestConstructWordTree(){
        BeginInsert();
        InsertWordTree("มะแลง");
        InsertWordTree("มะไฟ");
        InsertWordTree("มะพลา");
        InsertWordTree("มะแล");
        InsertWordTree("มะแลด");
        InsertWordTree("มาแลง");
        InsertWordTree("บาราม");
        InsertWordTree("มะ");
        InsertWordTree("มะนาว");
        EndInsert();
        
    }
    
    private boolean PushFirstChar(char c){
        _curPtr = WordTree;
        while(_curPtr != null && c > _curPtr.key)
            _curPtr = _curPtr.next;
        return (_curPtr != null && c == _curPtr.key);
    }
    
    private boolean PushNextChar(char c){
        _curPtr = _curPtr.child;
        while(_curPtr != null && c > _curPtr.key)
            _curPtr = _curPtr.next;
        return (_curPtr != null && c == _curPtr.key);
    }
    
    public String SeparateWord(String str, char Space){
        int len = str.length();
        int startP = 0, curP = 0, lastP = 0, bestP = 0;
        String output = "";
        while (startP < len) {
            if( WhiteSpace.contains(str.charAt(startP))) {
                if (lastP != startP) {
                    if (lastP > 0) {
                        output += Space + str.substring(lastP, startP);
                    } else {
                        output = str.substring(lastP, startP);
                    }
                }
                lastP = ++startP;
                continue;
            } else if (PushFirstChar(str.charAt(startP))) {
                bestP = 0;
                curP = startP + 1;
                if (_curPtr.val) {
                    bestP = curP;
                }
                while (curP < len && PushNextChar(str.charAt(curP++))) {
                    if (_curPtr.val) {
                        bestP = curP;
                    }
                }
                if (bestP > 0) {
                    if (lastP > 0) {
                        if (lastP != startP) {
                            output += Space + str.substring(lastP, startP);
                        }
                        output += Space + str.substring(startP, bestP);
                    } else {
                        if (lastP != startP) {
                            output = str.substring(lastP, startP) + Space + str.substring(startP, bestP);
                        } else {
                            output = str.substring(startP, bestP);
                        }
                    }
                    startP = lastP = bestP;
                    continue;
                } // else { /* Do nothing */ }
            } // else { /* Do nothing */ }
            startP++;
        }
        if(lastP == 0) {
            return str;
        } else if(lastP != len) {
            return output + Space + str.substring(lastP);
        } else {
            return output;
        }
        
    }
    
    public void PrintTree(CharNode c, String prefix){
        while(c!=null){
            System.out.println(prefix + c.key + " - " + c.val);
            PrintTree(c.child, prefix + " ");
            c = c.next;
        }
    }
    
    public static void main(String[] args){
        Lexical l = new Lexical();
        //l.TestConstructWordTree();
        l.ConstructWordTree(new File("lexitron.txt"));
        System.out.println(l.SeparateWord("วันนี้ WiFi by TrueMove H ได้เปลี่ยนชื่อสัญญาณใหม่(SSID) เป็น .@  TRUEWIFI โดยคุณสามารถเชื่อมต่อ WiFi ด้วย Username และ Password เดิม ได้ในห้างสรรพสินค้า และ อาคารสำนักงานชั้นนำ 73 แห่งทั่วประเทศ เพิ่มเติม www.truewifi.net ",'\n'));
        //l.PrintTree(l.WordTree,"");
        //System.out.println(l.SeparateWord("มมมะมมมม"));
    }
    
}
