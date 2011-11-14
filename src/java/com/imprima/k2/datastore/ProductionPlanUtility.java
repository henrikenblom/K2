/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

import java.sql.Timestamp;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;

// TODO: Gruppera INTE prepress och atelje
/**
 *
 * @author henrik
 */
public class ProductionPlanUtility {

    private static Timestamp ORDERTIMELIMIT = new Timestamp(0xdc6be25480l);
    private static long HOUR = 3600000l;
    public static int QUEUEID_PREPRESS = 4150;
    public static int QUEUEID_PLAT = 4151;
    public static int QUEUEID_KBA8F = 4152;
    public static int QUEUEID_KBA5FL = 4171;
    public static int QUEUEID_SKAR = 4174;
    public static int QUEUEID_FALS = 4189;
    public static int QUEUEID_FALS32 = 4190;
    public static int QUEUEID_FALSKLAMMER = 4191;
    public static int QUEUEID_STAL52 = 4193;
    public static int QUEUEID_KLAMMER = 4194;
    public static int QUEUEID_LEGO = 4195;
    public static int QUEUEID_EFTERBEHANDLING = 4196;
    public static int QUEUEID_EXPEDITIONLEVERANS = 4197;
    public static int QUEUEID_KLEGOTRYCK = 4283;
    public static int QUEUEID_ATELJE = 4150;
    public static int QUEUEID_LENNGRENS = 4355;
    public static int QUEUEID_LACKLAMINERING = 13079;
    public static int QUEUEID_FAKTURERING = 14664;
    public static int QUEUEID_DIGITALTRYCK = 23496;
    public static int QUEUEID_FALSFOLDER = 33040;
    public static int QUEUEID_LAMINERING = 45896;
    public static int QUEUEID_PERFNING = 45902;
    public static int QUEUEID_CLIENT = 1;
    public static int QUEUEID_DRYING = 2;

    public static enum TYPE {

        PREPRESS, PLATE, PRINT, POSTPRESS, DELIVERY, LEGO, INVOICE, DIGITAL_PRINT, CLIENT
    }
    private HashMap<String, EnumMap<TYPE, String>> localizedGroupNames = new HashMap<String, EnumMap<TYPE, String>>();
    private HashMap<Integer, TYPE> typeMap = new HashMap<Integer, TYPE>();

    private ProductionPlanUtility() {

        typeMap.put(QUEUEID_PREPRESS, TYPE.PREPRESS);
        typeMap.put(QUEUEID_PLAT, TYPE.PLATE);
        typeMap.put(QUEUEID_KBA8F, TYPE.PRINT);
        typeMap.put(QUEUEID_KBA5FL, TYPE.PRINT);
        typeMap.put(QUEUEID_SKAR, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_FALS, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_FALS32, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_FALSKLAMMER, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_STAL52, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_KLAMMER, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_LEGO, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_LENNGRENS, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_EFTERBEHANDLING, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_EXPEDITIONLEVERANS, TYPE.DELIVERY);
        typeMap.put(QUEUEID_KLEGOTRYCK, TYPE.LEGO);
        typeMap.put(QUEUEID_ATELJE, TYPE.PREPRESS);
        typeMap.put(QUEUEID_LACKLAMINERING, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_FAKTURERING, TYPE.INVOICE);
        typeMap.put(QUEUEID_DIGITALTRYCK, TYPE.DIGITAL_PRINT);
        typeMap.put(QUEUEID_FALSFOLDER, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_LAMINERING, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_PERFNING, TYPE.POSTPRESS);
        typeMap.put(QUEUEID_CLIENT, TYPE.CLIENT);

        EnumMap<TYPE, String> svGroupNames = new EnumMap<TYPE, String>(TYPE.class);

        svGroupNames.put(TYPE.PREPRESS, "Premedia");
        svGroupNames.put(TYPE.PLATE, "Plåtkörning");
        svGroupNames.put(TYPE.PRINT, "Tryckning");
        svGroupNames.put(TYPE.POSTPRESS, "Efterbehandling");
        svGroupNames.put(TYPE.DELIVERY, "Leverans");
        svGroupNames.put(TYPE.LEGO, "Tryckning/Efterbehandling");
        svGroupNames.put(TYPE.INVOICE, "Fakturering");
        svGroupNames.put(TYPE.DIGITAL_PRINT, "Digitaltryck");
        svGroupNames.put(TYPE.CLIENT, "Kundarbete");

        localizedGroupNames.put("sv", svGroupNames);

    }

    public static ProductionPlanUtility getInstance() {
        return ProductionPlanUtilityHolder.INSTANCE;
    }

    private static class ProductionPlanUtilityHolder {

        private static final ProductionPlanUtility INSTANCE = new ProductionPlanUtility();
    }

    public TYPE getType(Integer queueid) {

        return typeMap.get(queueid);

    }

    public String getLocalizedGroupName(Integer queueid, Locale locale) {

        String retval = "";

        try {
            retval = localizedGroupNames.get(locale.getLanguage()).get(getType(queueid));
        } catch (Exception ex) {
        }

        return retval;

    }

    public void estimateProductionTimes(ProductionPlan productionPlan) {

        ProductionStep[] productionSteps = productionPlan.toArray(new ProductionStep[0]);
        productionPlan.clear();

        for (int c = 0; c < productionSteps.length; c++) {

            if (c == 0) {

                productionPlan.add(productionSteps[c]);

            } else {

//                ProductionStep pseudoProductionStep = null;
//                
//                try {
//                    
//                    getPseudoStep(productionSteps[c - 1], productionSteps[c]);
//                    
//                    if (pseudoProductionStep != null) {
//                        
//                        alteredProductionPlan.add(pseudoProductionStep);
//                        
//                    }
//                    
//                } catch (Exception ex) {
//                    Logger.getLogger(ProductionPlanUtility.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                
                if (c < productionSteps.length - 1) {

                    ProductionStep nextProductionStep = null;

                    for (int i = c + 1; i < productionSteps.length; i++) {

                        if (getType(productionSteps[i].getQueueid()) != getType(productionSteps[c].getQueueid())) {

                            nextProductionStep = productionSteps[i];
                            break;

                        }

                    }

                    if (nextProductionStep != null) {

                        fillInProductionTimes(productionSteps[c - 1], productionSteps[c], nextProductionStep);

                    }

                }

                productionPlan.add(productionSteps[c]);

            }

        }

    }

    private void fillInProductionTimes(ProductionStep previousProductionStep, ProductionStep currentProductionStep, ProductionStep nextProductionStep) {

        if (!timeIsValid(currentProductionStep.getStarttime())) {

            if (getType(previousProductionStep.getQueueid()) == getType(currentProductionStep.getQueueid())
                    && timeIsValid(previousProductionStep.getStarttime())) {

                currentProductionStep.setStarttime(previousProductionStep.getStarttime());

            } else {

                currentProductionStep.setStarttime(previousProductionStep.getStoptime());

            }

            if (currentProductionStep.getStarttime().after(currentProductionStep.getStoptime())
                    && currentProductionStep.getStarttime().equals(currentProductionStep.getStoptime())) {

                currentProductionStep.setStarttime(new Timestamp(currentProductionStep.getStarttime().getTime() - HOUR));

            }

        }

        if (!timeIsValid(currentProductionStep.getStoptime())) {

            if (getType(nextProductionStep.getQueueid()) == getType(currentProductionStep.getQueueid())
                    && timeIsValid(nextProductionStep.getStoptime())) {

                currentProductionStep.setStoptime(nextProductionStep.getStoptime());

            } else if (currentProductionStep.getTimespan() > 0) {

                currentProductionStep.setStoptime(
                        new Timestamp(
                        currentProductionStep.getStarttime().getTime()
                        + (currentProductionStep.getTimespan() * 1000l)));

            } else {

                currentProductionStep.setStoptime(nextProductionStep.getStarttime());

            }

        }

    }

    public boolean timeIsValid(Timestamp timestamp) {

        return timestamp != null && timestamp.after(ORDERTIMELIMIT);

    }

    private ProductionStep getPseudoStep(ProductionStep previousProductionStep, ProductionStep currentProductionStep) throws Exception {

        ProductionStep retval = null;

        TYPE previousProductionStepType = getType(previousProductionStep.getQueueid());
        TYPE currentProductionStepType = getType(currentProductionStep.getQueueid());

        if (previousProductionStepType == TYPE.PRINT) {

            retval = new ProductionStep(-4, "Torktid", "Torkning", QUEUEID_DRYING);
            retval.setStarttime(previousProductionStep.getStoptime());

            int ordering = previousProductionStep.getOrdering() + 1;

            if (ordering > currentProductionStep.getOrdering()) {

                throw new Exception("Pseudo ProductionStep ordering factor ("
                        + ordering + ") must not be higher than current ProductionStep factor ("
                        + currentProductionStep.getOrdering() + ")");

            } else {

                retval.setOrdering(ordering);

            }

        }

        previousProductionStepType = null;
        currentProductionStepType = null;

        return retval;

    }
}
