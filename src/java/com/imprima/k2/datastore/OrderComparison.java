package com.imprima.k2.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author henrik
 */
public class OrderComparison {

    private HashMap<String, Difference> differences = new HashMap<String, Difference>();
    private boolean relationshipsChanged = false;

    public OrderComparison() {
    }

    public OrderComparison(Order oldOrder, Order newOrder) {

        compareStaticFields(oldOrder, newOrder);
        compareRelationShips(oldOrder, newOrder);

    }

    public void putChange(String field, String oldValue, String newValue) {

        differences.put(field, new Difference(oldValue, newValue));

    }

    public HashMap<String, Difference> getDifferences() {

        return differences;

    }

    public ArrayList<RelationshipDifference> getRelationshipDifferences() {

        ArrayList<RelationshipDifference> retval = new ArrayList<RelationshipDifference>();

        for (Difference difference : getDifferences().values()) {

            if (difference.getClass().getName().equals("com.imprima.k2.datastore.RelationshipDifference")) {
                
                retval.add((RelationshipDifference) difference);
                
            }
        }
        
        return retval;

    }

    public Set<String> getFieldSet() {

        return differences.keySet();

    }

    private void compareStaticFields(Order oldOrder, Order newOrder) {


        if (!oldOrder.getName().equals(newOrder.getName())) {

            differences.put("name", new Difference(oldOrder.getName(), newOrder.getName()));

        }

    }

    public boolean relationshipsChanged() {
        return relationshipsChanged;
    }

    private void compareRelationShips(Order oldOrder, Order newOrder) {

        for (Integer type : OrderUserRelationship.getTypeList()) {

            String oldUsername = null;
            String newUsername = null;
            String oldFullname = null;
            String newFullname = null;

            try {
                oldUsername = oldOrder.getRelationShip(type).getUsername();
                oldFullname = oldOrder.getRelationShip(type).getFullname();
            } catch (NullPointerException ex) {
                // no-op
            }

            try {
                newUsername = newOrder.getRelationShip(type).getUsername();
                newFullname = newOrder.getRelationShip(type).getFullname();
            } catch (NullPointerException ex) {
                // no-op
            }

            if (oldUsername == null ? newUsername != null : !oldUsername.equals(newUsername)) {

                differences.put(OrderUserRelationship.TYPENAME[type] + "_username",
                        new RelationshipDifference(oldUsername, newUsername, type));

                differences.put(OrderUserRelationship.TYPENAME[type] + "_fullname",
                        new RelationshipDifference(oldFullname, newFullname, type));
                
                relationshipsChanged = true;

            }

        }

    }
}
