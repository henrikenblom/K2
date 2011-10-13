/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.level9;

import com.google.gson.Gson;
import com.imprima.util.DateUtility;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 *
 * @author henrik
 */
public abstract class Message {

    private Gson gson = new Gson();
    private DateUtility dateUtility = DateUtility.getInstance();
    
    public HashMap<String, Object> message = new HashMap<String, Object>();
    
    public String toJson() {
                
        addTimestamp();
        
        return gson.toJson(message);
        
    }
    
    public HashMap<String, String> getBody() {
                
        return (HashMap<String, String>) message.get("body");
        
    }
    
    private void addTimestamp() {
        
        Date now = new Date();
        
        message.put("time", dateUtility.getTimeString(now, new Locale("sv")));
        message.put("date", dateUtility.getDateString(now, new Locale("sv")));
        
    }
    
}
