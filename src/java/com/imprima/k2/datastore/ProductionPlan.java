/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

import com.imprima.k2.datastore.util.ProductionPlanListEntry;
import com.imprima.util.Timespan;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.TreeSet;

/**
 *
 * @author henrik
 */
public class ProductionPlan extends TreeSet<ProductionStep> {

    private static final int ONEHOUR = 3600000;
    private static final int ONEDAY = ONEHOUR * 24;
    private static final int SKIPLIMIT = ONEDAY * 2;
    private static final int SKIPSPANPADDING = ONEDAY;
    private int ordernumber;
    private Timestamp minDate = null;
    private Timestamp maxDate = null;
    private ProductionPlanUtility productionPlanUtility = ProductionPlanUtility.getInstance();

    public ProductionPlan(int ordernumber) {

        super();
        this.ordernumber = ordernumber;

    }

    public int getOrdernumber() {

        return ordernumber;

    }

    public ArrayList<ProductionPlanListEntry> toList(Locale locale, boolean grouped) {

        HashMap<String, ProductionPlanListEntry> groups = new HashMap<String, ProductionPlanListEntry>();

        for (ProductionStep productionStep : this) {

            String groupname = productionPlanUtility.getLocalizedGroupName(productionStep.getQueueid(), locale);
            ProductionPlanListEntry group;

            if (groups.containsKey(groupname)) {

                group = groups.get(groupname);

                HashMap<String, Object> task = ((ArrayList<HashMap<String, Object>>) group.get("values")).get(0);

                if (productionStep.getStarttime().getTime() < (Long) task.get("from")) {

                    task.put("from", productionStep.getStarttime().getTime());

                }

                if (productionStep.getStoptime().getTime() > (Long) task.get("to")) {

                    task.put("to", productionStep.getStoptime().getTime());

                }

            } else {

                group = new ProductionPlanListEntry();

                group.put("name", groupname);
                group.put("desc", "beskrivning");

                ArrayList<HashMap<String, Object>> values = new ArrayList<HashMap<String, Object>>();

                HashMap<String, Object> task = new HashMap<String, Object>();

                task.put("from", productionStep.getStarttime().getTime());
                task.put("to", productionStep.getStoptime().getTime());
                task.put("desc", productionStep.getDetails());

                values.add(task);

                group.put("values", values);

                groups.put(groupname, group);

            }

        }

        addSkipspans(groups);

        //TODO: Optimize sorting

        ArrayList<ProductionPlanListEntry> values = new ArrayList<ProductionPlanListEntry>(Arrays.asList(groups.values().toArray(new ProductionPlanListEntry[0])));

        Collections.sort(values);

        return values;

    }

    private void addSkipspans(HashMap<String, ProductionPlanListEntry> groups) {

        HashMap<String, Timespan> candidates = new HashMap<String, Timespan>();
        HashSet<Timespan> allTimespans = new HashSet<Timespan>();
        int skipSpanCounter = 0;

        for (String groupname : groups.keySet()) {

            ProductionPlanListEntry productionPlanListEntry = groups.get(groupname);
            
            for (HashMap<String, Object> task : (Collection<HashMap<String, Object>>) productionPlanListEntry.get("values")) {

                Timespan currentTimespan = new Timespan(new Timestamp((Long) task.get("from")), new Timestamp((Long) task.get("to")));

                if (!allTimespans.isEmpty()) {

                    Timespan previousTimespan = allTimespans.toArray(new Timespan[0])[allTimespans.size() - 1];

                    if (currentTimespan.getFrom().getTime() - previousTimespan.getTo().getTime() > SKIPLIMIT) {

                        candidates.put("_skipspan_" + skipSpanCounter++, new Timespan(previousTimespan.getTo(), currentTimespan.getFrom()));

                    }

                }

                allTimespans.add(currentTimespan);

                if (currentTimespan.getLength() > SKIPLIMIT) {

                    candidates.put(groupname, currentTimespan);

                }

            }

        }

        for (String groupname : candidates.keySet()) {

            Timespan candidateTimespan = candidates.get(groupname);

            for (Timespan timespan : allTimespans) {

                if (!candidateTimespan.equals(timespan)
                        && timespan.covers(candidateTimespan.getFrom())
                        && timespan.covers(candidateTimespan.getTo())) {

                    candidateTimespan.setFrom(timespan.getTo());

                }
                
            }

            candidateTimespan.setFrom(new Timestamp(candidateTimespan.getFrom().getTime() + SKIPSPANPADDING));
            candidateTimespan.setTo(new Timestamp(candidateTimespan.getTo().getTime() - SKIPSPANPADDING));

            if (candidateTimespan.getLength() > SKIPLIMIT) {

                try {

                    ProductionPlanListEntry productionPlanListEntry = groups.get(groupname);
                    HashMap<String, Object> task = (HashMap<String, Object>) ((Collection<HashMap<String, Object>>) productionPlanListEntry.get("values")).toArray()[0];

                    task.put("skip_from", candidateTimespan.getFrom().getTime());
                    task.put("skip_to", candidateTimespan.getTo().getTime());
                    
                } catch (NullPointerException ex) {

                    ProductionPlanListEntry productionPlanListEntry = new ProductionPlanListEntry();

                    HashMap<String, Object> task = new HashMap<String, Object>();

                    task.put("name", groupname);
                    task.put("skip_from", candidateTimespan.getFrom().getTime());
                    task.put("skip_to", candidateTimespan.getTo().getTime());

                    productionPlanListEntry.put("skipspan", task);

                    groups.put(groupname, productionPlanListEntry);

                }

            }

        }

    }

    public Timestamp getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Timestamp maxDate) {

        if (productionPlanUtility.timeIsValid(maxDate)) {
            this.maxDate = maxDate;
        }

    }

    public Timestamp getMinDate() {
        return minDate;
    }

    public void setMinDate(Timestamp minDate) {

        if (productionPlanUtility.timeIsValid(minDate)) {
            this.minDate = minDate;
        }

    }

    @Override
    public boolean equals(Object comparedProductionPlan) {

        return (comparedProductionPlan instanceof ProductionPlan) && comparedProductionPlan.hashCode() == hashCode();

    }

    @Override
    public int hashCode() {

        int hash = this.ordernumber;

        for (ProductionStep productionStep : this) {

            hash += productionStep.getDetails().hashCode()
                    + productionStep.getImposition().hashCode()
                    + productionStep.getLaststarted().hashCode()
                    + productionStep.getOrdering()
                    + productionStep.getPaperinfo().hashCode()
                    + productionStep.getPrintpart().hashCode()
                    + productionStep.getQueueid().hashCode()
                    + productionStep.getStarttime().hashCode()
                    + productionStep.getState()
                    + productionStep.getStoptime().hashCode()
                    + productionStep.getSubcontractor().hashCode();

        }

        return 11 * hash;

    }
}
