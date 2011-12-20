package com.imprima.k2.datastore;

import java.util.ArrayList;

/**
 *
 * @author henrik
 */
public final class OrderUserRelationship {
    
    public static final int SALES = 0;
    public static final int PROJECTMANAGER = 1;
    public static final int CLIENT = 2;
    public static final int PREPRESS = 3;
    public static final int ASSIGNED = 5;
    public static final int CUSTOM = 5;
    
    public static final String[] TYPENAME = {"_sales", "_projectmanager", "_client", "_prepress", "_assigned", "_custom"};
    
    private String username;
    private String fullname;
    private int relationship;

    public OrderUserRelationship(String username, int relationship) {
        
        setUsername(username);
        setRelationship(relationship);
        
    }

    public OrderUserRelationship(String username, String fullname, int relationship) {
        
        setUsername(username);
        setRelationship(relationship);
        setFullname(fullname);
        
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRelationship() {
        return relationship;
    }

    public void setRelationship(int relationship) {
        this.relationship = relationship;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
    
    public static ArrayList<Integer> getTypeList() {
        
        ArrayList<Integer> list = new ArrayList<Integer>();
        
        for (int i = 0;i < 5;i++) {
            
            list.add(i);
            
        }
        
        return list;
        
    }
    
}
