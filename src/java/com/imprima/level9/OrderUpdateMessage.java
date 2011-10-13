/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.level9;

import com.imprima.k2.datastore.Difference;
import com.imprima.k2.datastore.OrderComparison;
import java.util.HashMap;

/**
 *
 * @author henrik
 */
public class OrderUpdateMessage extends Message {
        
    public OrderUpdateMessage(Integer ordernumber, OrderComparison orderComparison) {
                
        HashMap<String, String> messageBody = new HashMap<String, String>();
        
        HashMap<String, Difference> differences = orderComparison.getDifferences();
                
        messageBody.put("ordernumber", ordernumber.toString());
        
        for (String field : differences.keySet()) {
            
            messageBody.put(field, differences.get(field).getNewValue());
                        
        }
        
        differences = null;
        
        message.put("type", "orderupdatemessage");
        message.put("body", messageBody);
        
    }
    
}
