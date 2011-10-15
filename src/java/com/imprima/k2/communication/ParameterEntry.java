/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.communication;

import java.util.Map.Entry;

/**
 *
 * @author henrik
 */
public class ParameterEntry implements Entry {

    private String key;
    private String value;

    public ParameterEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }
        
    @Override
    public String getKey() {
        
        return key;
        
    }

    @Override
    public String getValue() {
        
        return value;
        
    }

    @Override
    public String setValue(Object value) {
        
        this.value = (String) value;
        return getValue();
        
    }
    
}
