/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

import com.imprima.util.DateUtility;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

/**
 *
 * @author henrik
 */
public final class Order extends HashMap<String, Object> {

    private HashMap<Integer, OrderUserRelationship> relationships = new HashMap<Integer, OrderUserRelationship>();
    private ArrayList<String> relationshipUsernames = new ArrayList<String>();
    private Timestamp updated = new Timestamp(System.currentTimeMillis());
    private DateUtility dateUtility = DateUtility.getInstance();

    public Order(Integer ordernumber, String name) {

        put("ordernumber", ordernumber);        
        setName(name);

    }

    public Order(Integer ordernumber, String name, Timestamp updated) {
        
        this(ordernumber, name);
        setUpdated(updated);
        
    }
    
    public void setName(String name) {
        
        put("name", (name != null) ? name : "");
        
    }
    
    public void setUpdated(Timestamp updated) {
        
        this.updated = updated;
        
    }
    
    public Timestamp getUpdated() {
        
        return updated;
        
    }
    
    public Integer getOrdernumber() {
        
        return (Integer) get("ordernumber");
        
    }
    
    public String getName() {
        
        return (String) get("name");
        
    }
    
    public ArrayList<String> getRelationshipUsernames() {
        
        return relationshipUsernames;
        
    }
    
    public void putOrderUserRelationship(OrderUserRelationship orderUserRelationship) {
        
        relationships.put(orderUserRelationship.getRelationship(), orderUserRelationship);
        
        relationshipUsernames.clear();
        for (OrderUserRelationship relationship : getRelationships().values()) {
            
            relationshipUsernames.add(relationship.getUsername());
            
        }
        
    }

    public void removeRelationship(Integer type) {
        relationships.remove(type);
    }
    
    public OrderUserRelationship getRelationShip(int type) {
        
        return relationships.get(type);
        
    }

    public HashMap<Integer, OrderUserRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(HashMap<Integer, OrderUserRelationship> relationships) {
        this.relationships = relationships;
    }
    
    public HashMap<String, String> toMap(Locale locale) {
        
        HashMap<String, String> map = new HashMap<String, String>();
        
        map.put("updated", dateUtility.getRelativeDateTimeString(updated, locale));
        map.put("timestamp", String.valueOf(updated.getTime()));
        
        for (Entry<String, Object> entry : this.entrySet()) {
            
            map.put(entry.getKey(), entry.getValue().toString());
            
        }
                
        for (Integer type : relationships.keySet()) {
            
            map.put(OrderUserRelationship.TYPENAME[type] + "_username", relationships.get(type).getUsername());
            map.put(OrderUserRelationship.TYPENAME[type] + "_fullname", relationships.get(type).getFullname());
            
        }
        
        return map;
        
    }
    
}
