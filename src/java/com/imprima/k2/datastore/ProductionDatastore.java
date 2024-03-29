package com.imprima.k2.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author henrik
 * 
 * Communication glue and datastore logic for production data.
 * 
 */
public final class ProductionDatastore {

    private static ProductionDatastore instance;
    private HashMap<Integer, Order> ordercacheMap;
    private HashMap<Integer, Timestamp> ordercacheTimestampMap = new HashMap<Integer, Timestamp>();
    private CacheDBUtility cacheDBUtility = new CacheDBUtility();

    protected ProductionDatastore() {

        updateOrderCache();

    }

    public synchronized static ProductionDatastore getInstance() {

        if (instance == null) {
            instance = new ProductionDatastore();
        }

        return instance;

    }

    public void updateOrderCache() {

        ordercacheMap = cacheDBUtility.fetchOrderMap();

        ordercacheTimestampMap.clear();

        for (Integer ordernumber : ordercacheMap.keySet()) {

            ordercacheTimestampMap.put(ordernumber, ordercacheMap.get(ordernumber).getUpdated());

        }

    }
    
    public Order getOrder(Integer ordernumber) {
        
        return ordercacheMap.get(ordernumber);
        
    } 

    public synchronized ArrayList<Order> getOrderListByUsername(String username) {

        ArrayList<Order> retval = new ArrayList<Order>();

        Connection connection = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;

        try {

            connection = DBConnectionUtility.getCacheDBConnection();

            preparedStatement = connection.prepareStatement("SELECT DISTINCT "
                    + "o.ordernumber ordernumber, o.updated updated "
                    + "FROM order_user_relationship r, basic_order_data o "
                    + "WHERE o.ordernumber = r.ordernumber AND r.username = ?");

            preparedStatement.setString(1, username.toUpperCase());

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                retval.add(fetchOrder(resultSet.getInt("ordernumber"),
                        resultSet.getTimestamp("updated"),
                        connection));

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

        return retval;

    }

    private Order fetchOrder(int ordernumber, Timestamp updated, Connection cacheConnection) {

        Order order = null;

        if (ordercacheMap.containsKey(ordernumber)
                && !updated.after(ordercacheTimestampMap.get(ordernumber))) {

            order = ordercacheMap.get(ordernumber);

        } else {

            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            Connection userConnection = null;

            try {

                userConnection = DBConnectionUtility.getUserDBConnection();

                preparedStatement = cacheConnection.prepareStatement("SELECT name,"
                        + " updated FROM basic_order_data WHERE ordernumber = ?");

                preparedStatement.setInt(1, ordernumber);

                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {

                    order = new Order(ordernumber, resultSet.getString("name"),
                            resultSet.getTimestamp("updated"));

                    preparedStatement = cacheConnection.prepareStatement("SELECT username,"
                            + " fullname, relationship FROM order_user_relationship WHERE ordernumber = ?");

                    preparedStatement.setInt(1, ordernumber);

                    resultSet = preparedStatement.executeQuery();

                    while (resultSet.next()) {

                        order.putOrderUserRelationship(new OrderUserRelationship(
                                resultSet.getString("username"),
                                resultSet.getString("fullname"),
                                resultSet.getInt("relationship")));

                    }

                }


            } catch (SQLException ex) {

                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);

            } finally {

                if (preparedStatement != null) {
                    try {
                        preparedStatement.close();
                        preparedStatement = null;
                    } catch (SQLException ex) {
                        Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (userConnection != null) {
                    try {
                        userConnection.close();
                        userConnection = null;
                    } catch (SQLException ex) {
                        Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

            ordercacheMap.put(ordernumber, order);

        }

        return order;

    }
}
