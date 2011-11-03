/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

import java.util.TreeSet;

/**
 *
 * @author henrik
 */
public class ProductionPlan extends TreeSet<ProductionStep> {

    private int ordernumber;
    
    public ProductionPlan(int ordernumber) {

        super();
        this.ordernumber = ordernumber;

    }

    public int getOrdernumber() {

        return ordernumber;

    }

    @Override
    public boolean equals(Object comparedProductionPlan) {

//        return (comparedProductionPlan instanceof ProductionPlan) && comparedProductionPlan.hashCode() == hashCode();
        
        if (!(comparedProductionPlan instanceof ProductionPlan)
                || this.size() != ((ProductionPlan) comparedProductionPlan).size()
                || getOrdernumber() != ((ProductionPlan) comparedProductionPlan).getOrdernumber()) {
            return false;
        }
        
        boolean retval = true;

        ProductionStep[] thisProductionStepArray = toArray(new ProductionStep[0]);
        ProductionStep[] comparedProductionStepArray = ((ProductionPlan) comparedProductionPlan).toArray(new ProductionStep[0]);

        for (int c = 0; c < thisProductionStepArray.length; c++) {

            ProductionStep thisProductionStep = thisProductionStepArray[c];
            ProductionStep comparedProductionStep = comparedProductionStepArray[c];
//
//            if (getOrdernumber() == 277103) {
//            System.err.println(getOrdernumber() + " : " + ((ProductionPlan) comparedProductionPlan).getOrdernumber());
//            System.err.println("details " + thisProductionStep.getDetails() + " " + comparedProductionStep.getDetails());
//            System.err.println("imposition " + thisProductionStep.getImposition() + " " + comparedProductionStep.getImposition());
//            System.err.println("laststarted " + thisProductionStep.getLaststarted() + " " + comparedProductionStep.getLaststarted());
//            System.err.println("ordering " + thisProductionStep.getOrdering() + " " + comparedProductionStep.getOrdering());
//            System.err.println("paperinfo " + thisProductionStep.getPaperinfo() + " " + comparedProductionStep.getPaperinfo());
//            System.err.println("queueid " + thisProductionStep.getQueueid() + " " + comparedProductionStep.getQueueid());
//            System.err.println("starttime " + thisProductionStep.getStarttime() + " " + comparedProductionStep.getStarttime());
//            System.err.println("stoptime " + thisProductionStep.getStoptime() + " " + comparedProductionStep.getStoptime());
//            System.err.println("subcontractor " + thisProductionStep.getSubcontractor() + " " + comparedProductionStep.getSubcontractor());
//            }
            retval = retval
                    && thisProductionStep.getDetails().equals(comparedProductionStep.getDetails())
                    && thisProductionStep.getImposition().equals(comparedProductionStep.getImposition())
                    && thisProductionStep.getLaststarted().equals(comparedProductionStep.getLaststarted())
                    && thisProductionStep.getOrdering().equals(comparedProductionStep.getOrdering())
                    && thisProductionStep.getPaperinfo().equals(comparedProductionStep.getPaperinfo())
                    && thisProductionStep.getPrintpart().equals(comparedProductionStep.getPrintpart())
                    && thisProductionStep.getStarttime().equals(comparedProductionStep.getStarttime())
                    && thisProductionStep.getState().equals(comparedProductionStep.getState())
                    && thisProductionStep.getStoptime().equals(comparedProductionStep.getStoptime())
                    && thisProductionStep.getSubcontractor().equals(comparedProductionStep.getSubcontractor());

        }

        return retval;

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
