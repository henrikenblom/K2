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
    private Timestamp orderdate = null;
    private DateUtility dateUtility = DateUtility.getInstance();
    private boolean productionOrder = false;
    private ProductionPlan productionPlan;
    private Integer cacheDBId;
    private int gksReferenceId;
    private int ordernumber;
    private String name = "";
    
    public Order(Integer ordernumber, String name) {

        this.ordernumber = ordernumber;       
        setName(name);

    }

    public Order(Integer ordernumber, String name, Timestamp updated) {
        
        this(ordernumber, name);
        setUpdated(updated);
        
    }
    
    public Order(Integer ordernumber, String name, Timestamp updated, boolean productionOrder, Timestamp orderdate) {
        
        this(ordernumber, name, updated);
        setProductionOrder(productionOrder);
        setOrderdate(orderdate);
        
    }
    
    public Order(Integer ordernumber, String name, Timestamp updated, boolean productionOrder, Timestamp orderdate, Integer cacheDBId) {
        
        this(ordernumber, name, updated, productionOrder, orderdate);
        setCacheDBId(cacheDBId);
        
    }
    
    public void setName(String name) {
        
        this.name = (name != null) ? name : "";
        
    }
    
    public void setUpdated(Timestamp updated) {
        
        this.updated = updated;
        
    }
    
    public Timestamp getUpdated() {
        
        return updated;
        
    }
    
    public Integer getOrdernumber() {
        
        return ordernumber;
        
    }
    
    public String getName() {
        
        return name;
        
    }

    public Timestamp getOrderdate() {
        return orderdate;
    }
    
    public void setOrderdate(Timestamp orderdate) {
        this.orderdate = orderdate;
    }

    public boolean isProductionOrder() {
        return productionOrder;
    }

    public void setProductionOrder(boolean productionOrder) {   
        this.productionOrder = productionOrder;
    }
    
    public ArrayList<String> getRelationshipUsernames() {
        
        return relationshipUsernames;
        
    }

    public int getGksReferenceId() {
        return gksReferenceId;
    }

    public void setGksReferenceId(int gksReferenceId) {
        this.gksReferenceId = gksReferenceId;
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

    public ProductionPlan getProductionPlan() {
        return productionPlan;
    }

    public void setProductionPlan(ProductionPlan productionPlan) {
        this.productionPlan = productionPlan;
    }

    public Integer getCacheDBId() {
        return cacheDBId;
    }

    public void setCacheDBId(Integer cacheDBId) {
        this.cacheDBId = cacheDBId;
    }
    
    public HashMap<String, String> toMap(Locale locale) {

        HashMap<String, String> map = new HashMap<String, String>();
        
        map.put("_updated", dateUtility.getRelativeDateTimeString(updated, locale));
        map.put("_timestamp", String.valueOf(updated.getTime()));
        map.put("_name", name);
        map.put("_ordernumber", String.valueOf(ordernumber));
        
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
