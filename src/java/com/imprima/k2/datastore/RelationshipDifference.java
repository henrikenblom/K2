/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

/**
 *
 * @author henrik
 */
public final class RelationshipDifference extends Difference {

    private int type;

    public RelationshipDifference(String oldValue, String newValue, int type) {
        
        super(oldValue, newValue);
        setType(type);

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    
}
