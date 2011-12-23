/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

/**
 *
 * @author henrik
 */
public final class OrderDataEntry implements Comparable<OrderDataEntry> {
    
    private String column1;
    private String column2;
    private String column3;
    private int id;
    
    public OrderDataEntry() {
        
    }
    
    public OrderDataEntry(String column1, String column2, String column3, int id) {
        
        this.setColumn1(column1);
        this.setColumn2(column2);
        this.setColumn3(column3);
        this.setId(id);
        
    }

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

    public String getColumn2() {
        return column2;
    }

    public void setColumn2(String column2) {
        this.column2 = column2;
    }

    public String getColumn3() {
        return column3;
    }

    public void setColumn3(String column3) {
        this.column3 = column3;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public String toString() {
        
        return getColumn1() + "\t" + getColumn2() + "\t" + getColumn3();
        
    }

    @Override
    public int compareTo(OrderDataEntry comparedOrderDataEntry) {
        
        if (getId() < comparedOrderDataEntry.getId()) {
            return -1;
        } else {
            return 1;
        }
        
    }
    
}
