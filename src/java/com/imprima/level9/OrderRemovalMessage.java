/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.level9;

/**
 *
 * @author henrik
 */
public class OrderRemovalMessage extends Message {

    public OrderRemovalMessage(Integer ordernumber) {
                
        message.put("type", "orderremovalmessage");
        message.put("body", ordernumber);
        
    }
    
}
