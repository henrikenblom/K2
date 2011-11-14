/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore.util;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author henrik
 */
public final class ProductionPlanListEntry extends HashMap<String, Object> implements Comparable<ProductionPlanListEntry> {

    @Override
    public int compareTo(ProductionPlanListEntry comparedProductionPlanListEntry) {

        Timestamp comparedFrom;
        Timestamp thisFrom;
        HashMap<String, Object> thisTask;
        HashMap<String, Object> comparedTask;

        try {

            comparedTask = (HashMap<String, Object>) ((Collection<HashMap<String, Object>>) comparedProductionPlanListEntry.get("values")).toArray()[0];

        } catch (Exception ex) {

            comparedTask = (HashMap<String, Object>) comparedProductionPlanListEntry.get("skipspan");

        }

        comparedFrom = comparedTask.get("from") != null
                ? new Timestamp((Long) comparedTask.get("from"))
                : new Timestamp((Long) comparedTask.get("skip_from"));

        try {

            thisTask = (HashMap<String, Object>) ((Collection<HashMap<String, Object>>) get("values")).toArray()[0];

        } catch (Exception ex) {

            thisTask = (HashMap<String, Object>) get("skipspan");

        }
        
        thisFrom = thisTask.get("from") != null
                ? new Timestamp((Long) thisTask.get("from"))
                : new Timestamp((Long) thisTask.get("skip_from"));

        if (thisFrom.before(comparedFrom)) {
            return -1;
        } else {
            return 1;
        }

    }
}
