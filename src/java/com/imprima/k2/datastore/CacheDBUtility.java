/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

    public HashMap<Integer, Order> fetchOrderMap() {

        HashMap<Integer, Order> orderMap = new HashMap<Integer, Order>();

        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;

        try {

            connection = DBConnectionUtility.getCacheDBConnection();

            resultSet = connection.createStatement().executeQuery("SELECT "
                    + "ordernumber, name, updated "
                    + "FROM basic_order_data");

            while (resultSet.next()) {

                orderMap.put(resultSet.getInt("ordernumber"),
                        new Order(resultSet.getInt("ordernumber"),
                                resultSet.getString("name"),
                                resultSet.getTimestamp("updated")
                                )
                        );

            }

            resultSet = connection.createStatement().executeQuery("SELECT "
                    + "ordernumber, username, fullname, relationship "
                    + "FROM order_user_relationship");
            
            while (resultSet.next()) {
                
                orderMap.get(resultSet.getInt("ordernumber"))
                        .putOrderUserRelationship(
                        new OrderUserRelationship(
                                resultSet.getString("username"),
                                resultSet.getString("fullname"),
                                resultSet.getInt("relationship")
                                )
                        );
                
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
                        + "basic_order_data(id INTEGER IDENTITY PRIMARY KEY, "
                        + "ordernumber INTEGER, "
                        + "name VARCHAR(200), "
                        + "updated DATETIME)");

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
