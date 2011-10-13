/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.level9;

import com.imprima.k2.datastore.Order;
import java.util.Locale;

/**
 *
 * @author henrik
 */
public class AddOrderMessage extends Message {
    
    public AddOrderMessage(Order order) {
                
        message.put("type", "addordermessage");
        message.put("body", order.toMap(new Locale("sv")));
        
    }
    
}
