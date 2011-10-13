/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

/**
 *
 * @author henrik
 */
public class Difference {

    private String oldValue;
    private String newValue;
    
    public Difference(String oldValue, String newValue) {
        
        this.oldValue = oldValue;
        this.newValue = newValue;
                
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
}
