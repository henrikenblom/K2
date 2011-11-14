/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.util;

import java.sql.Timestamp;

/**
 *
 * @author henrik
 */
public final class Timespan implements Comparable<Timespan> {
    
    private Timestamp from;
    private Timestamp to;

    public Timespan(Timestamp from, Timestamp to) {
        
        setFrom(from);
        setTo(to);
        
    }
    
    public Timestamp getFrom() {
        return from;
    }

    public void setFrom(Timestamp from) {
        this.from = from;
    }

    public Timestamp getTo() {
        return to;
    }

    public void setTo(Timestamp to) {
        this.to = to;
    }
    
    public long getLength() {
        
        return to.getTime() - from.getTime();
        
    }
    
    public boolean covers(Timestamp comparedTimestamp) {
        
        return (getFrom().equals(comparedTimestamp) || getTo().equals(comparedTimestamp))
                || (comparedTimestamp.after(getFrom()) && comparedTimestamp.before(getTo()));
        
    }
    
    @Override
    public boolean equals(Object comparedTimespanObject) {
        
        return (comparedTimespanObject instanceof Timespan) && comparedTimespanObject.hashCode() == hashCode();
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.from != null ? this.from.hashCode() : 0);
        hash = 79 * hash + (this.to != null ? this.to.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Timespan comparedTimespan) {

        Timestamp comparedTimestamp = comparedTimespan.getFrom();

        if (this.getFrom().after(comparedTimestamp)) {
            return 1;
        } else if (this.getFrom().after(comparedTimestamp)) {
            return -1;
        } else {
            return 1;
        }

    }
    
}
