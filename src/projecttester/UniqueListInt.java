/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package projecttester;

import java.util.ArrayList;

/**
 *
 * @author malang
 */
public class UniqueListInt{
    public ArrayList<UElementInt> UList;
    
    public UniqueListInt(){
        UList = new ArrayList<>();
    }
    
    public void Clear(){
        this.UList.clear();
    }
    
    public void AddOnce(int v){
        int L=0, R = UList.size(), C=R/2;
        if (R==0)
            UList.add(new UElementInt(v));
        else{
            while(true){
                if(L < R){
                    if( v < UList.get(C).ID )
                        R = C;
                    else if(v > UList.get(C).ID )
                        L = C + 1;
                    else{
                        break; // Do nothing
                    }
                    C = (L+R)/2;
                }else{
                    UList.add(C, new UElementInt(v));
                    break;
                }
            }
        }
    }
    
    public void Add(int v){
        int L=0, R = UList.size(), C=R/2;
        if (R==0)
            UList.add(new UElementInt(v));
        else{
            while(true){
                if(L < R){
                    if( v < UList.get(C).ID )
                        R = C;
                    else if(v > UList.get(C).ID )
                        L = C + 1;
                    else{
                        UList.get(C).count++;
                        break; // Do nothing
                    }
                    C = (L+R)/2;
                }else{
                    UList.add(C, new UElementInt(v));
                    break;
                }
            }
        }
    }
    
    public class UElementInt{
        public UElementInt(int ID){
            this.ID = ID;
            this.count = 1;
        }
        public int ID;
        public int count;
    }

}
