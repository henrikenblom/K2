/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.level9;

import java.util.logging.Level;

/**
 *
 * @author henrik
 */
public final class UserMessage extends Message {
        
    public UserMessage(String text, Level level) {
        
        message.put("type", "usermessage");
        message.put("level", level.getName());
        message.put("body", text);
        
    }
    
    public UserMessage(String text) {
           
        this(text, Level.INFO);
        
    }
    
}
