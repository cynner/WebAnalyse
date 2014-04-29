/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Types;

/**
 *
 * @author wiwat
 */
public class MutableInt {

    public int value; // note that we start at 1 since we're counting

    public MutableInt() {
        value = 1;
    }

    public MutableInt(int value) {
        this.value = value;
    }

    public void increment() {
        value++;
    }
}
