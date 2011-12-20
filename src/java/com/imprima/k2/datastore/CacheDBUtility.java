/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author henrik
 */
public class CacheDBUtility {

    public CacheDBUtility() {

        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CacheDBUtility.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public HashMap<Integer, ProductionPlan> fetchProductionPlanMap() {

        HashMap<Integer, ProductionPlan> productionPlanMap = new HashMap<Integer, ProductionPlan>();

        Connection connection = null;
        ResultSet resultSet = null;

        try {

            connection = DBConnectionUtility.getCacheDBConnection();

            resultSet = connection.createStatement().executeQuery("SELECT "
                    + "id, "
                    + "ordernumber, "
                    + "state, "
                    + "details, "
                    + "queue, "
                    + "queueid, "
                    + "starttime, "
                    + "stoptime, "
                    + "laststarted, "
                    + "subcontractor, "
                    + "timespan, "
                    + "ordering, "
                    + "imposition, "
                    + "paperinfo, "
                    + "printpart, "
                    + "progress "
                    + "FROM production_step_data "
                    + "ORDER BY ordernumber, starttime, stoptime");

            while (resultSet.next()) {

                ProductionPlan productionPlan;

                if (productionPlanMap.containsKey(resultSet.getInt("ordernumber"))) {

                    productionPlan = productionPlanMap.get(resultSet.getInt("ordernumber"));

                } else {

                    productionPlan = new ProductionPlan(resultSet.getInt("ordernumber"));
                    productionPlanMap.put(resultSet.getInt("ordernumber"), productionPlan);

                }

                if (productionPlan.getMinDate() == null) {
                    
                    productionPlan.setMinDate(resultSet.getTimestamp("starttime"));
                    
                }
                
                if (productionPlan.getMaxDate() == null
                        || productionPlan.getMaxDate().before(resultSet.getTimestamp("stoptime"))) {
                    
                    productionPlan.setMaxDate(resultSet.getTimestamp("stoptime"));
                    
                }
                
                ProductionStep productionStep = new ProductionStep(resultSet.getInt("state"),
                        resultSet.getString("details"),
                        resultSet.getString("queue"),
                        resultSet.getInt("queueid"));

                productionStep.setDbId(resultSet.getInt("id"));
                productionStep.setStarttime(resultSet.getTimestamp("starttime"));
                productionStep.setStoptime(resultSet.getTimestamp("stoptime"));
                productionStep.setLaststarted(resultSet.getTimestamp("laststarted"));
                productionStep.setSubcontractor(resultSet.getString("subcontractor"));
                productionStep.setTimespan(resultSet.getInt("timespan"));
                productionStep.setOrdering(resultSet.getInt("ordering"));
                productionStep.setImposition(resultSet.getString("imposition"));
                productionStep.setPaperinfo(resultSet.getString("paperinfo"));
                productionStep.setPrintpart(resultSet.getString("printpart"));
                productionStep.setProgress(resultSet.getInt("progress"));

                productionPlan.add(productionStep);

            }

        } catch (SQLException ex) {

            Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            if (resultSet != null) {
                try {
                    resultSet.close();
                    resultSet = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return productionPlanMap;

    }

    public HashMap<Integer, Order> fetchOrderMap() {

        HashMap<Integer, Order> orderMap = new HashMap<Integer, Order>();

        Connection connection = null;
        ResultSet resultSet = null;

        try {
            
            connection = DBConnectionUtility.getCacheDBConnection();

            resultSet = connection.createStatement().executeQuery("SELECT "
                    + "ordernumber, name, updated, productionorder, orderdate, id "
                    + "FROM basic_order_data");

            while (resultSet.next()) {

                Order order = new Order(resultSet.getInt("ordernumber"),
                        resultSet.getString("name"),
                        resultSet.getTimestamp("updated"),
                        resultSet.getBoolean("productionorder"),
                        resultSet.getTimestamp("orderdate"),
                        resultSet.getInt("id"));
                
                addParameters(order, connection);
                
                orderMap.put(resultSet.getInt("ordernumber"), order);
                
            }

            resultSet = connection.createStatement().executeQuery("SELECT "
                    + "ordernumber, username, fullname, relationship "
                    + "FROM order_user_relationship");

            while (resultSet.next()) {

                orderMap.get(resultSet.getInt("ordernumber")).putOrderUserRelationship(
                        new OrderUserRelationship(
                        resultSet.getString("username"),
                        resultSet.getString("fullname"),
                        resultSet.getInt("relationship")));

            }
            

        } catch (SQLException ex) {

            Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            if (resultSet != null) {
                try {
                    resultSet.close();
                    resultSet = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return orderMap;

    }
    
    private void addParameters(Order order, Connection connection) throws SQLException {
        
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT "
                + "key, value FROM additional_order_data WHERE basic_order_data_id = "
                + order.getCacheDBId());
        
        while (resultSet.next()) {
            
            order.put(resultSet.getString("key"), resultSet.getString("value"));
            
        }
        
    }

    public HashMap<Integer, String[]> fetchUserIdMap() {

        HashMap<Integer, String[]> userIdMap = new HashMap<Integer, String[]>();
        Connection connection = null;
        ResultSet resultSet = null;

        try {

            connection = DBConnectionUtility.getUserDBConnection();

            resultSet = connection.prepareStatement("SELECT id, username, fullname FROM system_users").executeQuery();

            while (resultSet.next()) {

                String fullname = (resultSet.getString("fullname") != null) ? resultSet.getString("fullname") : "";
                String username = (resultSet.getString("username") != null) ? resultSet.getString("username") : "";

                String[] entry = {username, fullname};
                userIdMap.put(resultSet.getInt("id"), entry);

            }

        } catch (SQLException ex) {

            Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            if (resultSet != null) {
                try {
                    resultSet.close();
                    resultSet = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return userIdMap;

    }

    public HashMap<Integer, String[]> fetchClientIdMap() {

        HashMap<Integer, String[]> clientIdMap = new HashMap<Integer, String[]>();
        Connection connection = null;
        ResultSet resultSet = null;

        try {

            connection = DBConnectionUtility.getUserDBConnection();

            resultSet = connection.prepareStatement("SELECT id, username, fullname FROM users").executeQuery();

            while (resultSet.next()) {

                String[] entry = {resultSet.getString("username"), resultSet.getString("fullname")};
                clientIdMap.put(resultSet.getInt("id"), entry);

            }

        } catch (SQLException ex) {

            Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            if (resultSet != null) {
                try {
                    resultSet.close();
                    resultSet = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return clientIdMap;

    }

    public void createTables() {

        Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, "Creating tables and indices.");

        Connection connection = null;

        try {

            connection = DBConnectionUtility.getCacheDBConnection();

            try {

                connection.createStatement().executeUpdate("CREATE TABLE "
                        + "basic_order_data("
                        + "id INTEGER IDENTITY PRIMARY KEY, "
                        + "gksreferenceid INTEGER, "
                        + "ordernumber INTEGER, "
                        + "productionorder BOOLEAN, "
                        + "name VARCHAR(200), "
                        + "updated DATETIME, "
                        + "orderdate DATETIME)");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }

            try {

                connection.createStatement().executeUpdate("CREATE INDEX "
                        + "order_index ON basic_order_data(ordernumber)");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }
            
            try {

                connection.createStatement().executeUpdate("CREATE TABLE "
                        + "additional_order_data("
                        + "id INTEGER IDENTITY PRIMARY KEY, "
                        + "basic_order_data_id INTEGER, "
                        + "key VARCHAR(100), "
                        + "value VARCHAR(200))");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }
            
            try {

                connection.createStatement().executeUpdate("CREATE INDEX "
                        + "additional_order_data_index ON additional_order_data(key)");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }
            
            try {

                connection.createStatement().executeUpdate("CREATE TABLE "
                        + "product(id INTEGER IDENTITY PRIMARY KEY, "
                        + "basic_order_data_id INTEGER, "
                        + "name VARCHAR(100))");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }

            try {

                connection.createStatement().executeUpdate("CREATE TABLE "
                        + "product_data(product_id INTEGER, "
                        + "key VARCHAR(100), "
                        + "value VARCHAR(200))");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }
            
            try {

                connection.createStatement().executeUpdate("CREATE TABLE "
                        + "order_user_relationship (ordernumber INT, "
                        + "username VARCHAR(20), "
                        + "fullname VARCHAR(60), "
                        + "relationship INT)");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }

            try {

                connection.createStatement().executeUpdate("CREATE INDEX order_user_relationship_index "
                        + "ON order_user_relationship(ordernumber, username)");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }

            try {

                connection.createStatement().executeUpdate("CREATE TABLE production_step_data "
                        + "(id INTEGER IDENTITY PRIMARY KEY, "
                        + "ordernumber INTEGER NOT NULL, "
                        + "state INTEGER, "
                        + "queue VARCHAR(100), "
                        + "queueid INTEGER, "
                        + "details VARCHAR(200), "
                        + "starttime DATETIME, "
                        + "stoptime DATETIME, "
                        + "laststarted DATETIME, "
                        + "subcontractor VARCHAR(100),"
                        + "timespan INTEGER, "
                        + "ordering INTEGER, "
                        + "imposition VARCHAR(100), "
                        + "paperinfo VARCHAR(100), "
                        + "printpart VARCHAR(100), "
                        + "progress INTEGER)");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }

            try {

                connection.createStatement().executeUpdate("CREATE INDEX "
                        + "production_step_data_index ON production_step_data(ordernumber)");

            } catch (SQLSyntaxErrorException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, ex.getMessage());

            }

        } catch (SQLException ex) {

            Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            if (connection != null) {

                try {
                    connection.close();
                } catch (SQLException ex) {
                }

            }

        }

    }
}
