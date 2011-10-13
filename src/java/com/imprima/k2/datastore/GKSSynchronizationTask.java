package com.imprima.k2.datastore;

import com.imprima.kesession.UserSessionController;
import com.imprima.level9.AddOrderMessage;
import com.imprima.level9.OrderRemovalMessage;
import com.imprima.level9.OrderUpdateMessage;
import com.imprima.level9.UserMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author henrik
 * 
 * TimerTask for prefetching order data.
 * 
 */
public class GKSSynchronizationTask extends TimerTask {
    
    private CacheDBUtility cacheDBUtility = new CacheDBUtility();
    private ProductionDatastore productionDatastore = ProductionDatastore.getInstance();
    private UserSessionController sessionStore = UserSessionController.getInstance();
    private HashMap<Integer, String[]> userIdMap;
    private HashMap<Integer, String[]> clientIdMap;
    private ResourceBundle orderfields = ResourceBundle.getBundle("orderfields");    
    
    public GKSSynchronizationTask() throws SQLException, ClassNotFoundException {
        
        cacheDBUtility.createTables();
        
    }
    
    @Override
    public void run() {
        
        userIdMap = cacheDBUtility.fetchUserIdMap();
        clientIdMap = cacheDBUtility.fetchClientIdMap();
        
        putOrders(fetchActiveOrders());
        
    }
    
    private ArrayList<Order> fetchActiveOrders() {
        
        ArrayList<Order> retval = new ArrayList<Order>();
        Connection connection = null;
        ResultSet resultSet = null;
        
        try {
            
            connection = DBConnectionUtility.getGksConnection();
            
            resultSet = connection.prepareStatement("SELECT DISTINCT "
                    + "oo.iNummer ordernumber, oo.cBenämning name, oo.rFöretagID client_id, "
                    + "oo.rPersonalIDSäljare salesperson_id, oo.rPersonalIDHandläggare projectmanager_id, "
                    + "oo.dSenastUppdaterad updated, oo.dLeveransdatumUtlovad, "
                    + "oo.dLeveransdatumFaktisk "
                    + "FROM Gks.dbo.OffertOrder oo WHERE "
                    + "(oo.iNummer > 200000 OR oo.iNummer < 100000) "
                    + "AND oo.iTyp != 21 AND oo.dFaktureradDatum IS NULL AND "
                    + "oo.dLeveransdatumUtlovad > DATEADD(month, -1, GETDATE())").executeQuery();
            
            while (resultSet.next()) {
                
                Order order = new Order(resultSet.getInt("ordernumber"), resultSet.getString("name"), resultSet.getTimestamp("updated"));
                                
                String[] salesData = getUserDataById(resultSet.getInt("salesperson_id"));
                String[] projectmanagerData = getUserDataById(resultSet.getInt("projectmanager_id"));
                String[] clientData = getClientDataById(resultSet.getInt("client_id"));
                
                if (salesData != null) {
                    order.putOrderUserRelationship(
                            new OrderUserRelationship(salesData[0],
                            salesData[1],
                            OrderUserRelationship.SALES));
                }
                
                if (projectmanagerData != null) {
                    order.putOrderUserRelationship(
                            new OrderUserRelationship(projectmanagerData[0],
                            projectmanagerData[1],
                            OrderUserRelationship.PROJECTMANAGER));
                }
                
                if (clientData != null) {
                    order.putOrderUserRelationship(
                            new OrderUserRelationship(clientData[0],
                            clientData[1],
                            OrderUserRelationship.CLIENT));
                }
                
                retval.add(order);
                
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(GKSSynchronizationTask.class.getName()).log(Level.SEVERE, null, ex);
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
    
    private void putOrders(ArrayList<Order> orderList) {
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        HashMap<Integer, Order> cachedOrderMap = cacheDBUtility.fetchOrderMap();
        
        try {
            
            connection = DBConnectionUtility.getCacheDBConnection();
            
            StringBuilder purgeOrdersStatement = new StringBuilder();
            HashSet<Integer> activeOrderList = new HashSet<Integer>();
            
            boolean keepMultiple = false;
            
            for (Order order : orderList) {
                
                if (keepMultiple) {
                    
                    purgeOrdersStatement.append(" AND");
                    
                }
                
                purgeOrdersStatement.append(" ordernumber != ").append(order.getOrdernumber());
                activeOrderList.add(order.getOrdernumber());
                
                keepMultiple = true;
                
                Order cachedOrder = cachedOrderMap.get(order.getOrdernumber());
                
                if (cachedOrder == null) {
                    
                    preparedStatement = connection.prepareStatement("INSERT INTO "
                            + "basic_order_data(name, ordernumber, updated) VALUES(?,?,?)");
                    
                    preparedStatement.setString(1, order.getName());
                    preparedStatement.setInt(2, order.getOrdernumber());
                    preparedStatement.setTimestamp(3, order.getUpdated());
                    
                    preparedStatement.executeUpdate();
                    
                    for (OrderUserRelationship orderUserRelationship : order.getRelationships().values()) {
                        
                        preparedStatement = connection.prepareStatement("INSERT INTO "
                                + "order_user_relationship(ordernumber, username, fullname, relationship) VALUES(?,?,?,?)");
                        
                        preparedStatement.setInt(1, order.getOrdernumber());
                        preparedStatement.setString(2, orderUserRelationship.getUsername());
                        preparedStatement.setString(3, orderUserRelationship.getFullname());
                        preparedStatement.setInt(4, orderUserRelationship.getRelationship());
                        
                        preparedStatement.executeUpdate();
                        
                        sessionStore.publishMessageToUser(new AddOrderMessage(order), orderUserRelationship.getUsername());
                        sessionStore.publishMessageToUser(new UserMessage("Ny order: " + order.getOrdernumber() + " " + order.getName()), orderUserRelationship.getUsername());
                        
                    }
                    
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, "Added order {0}", order.getOrdernumber().toString());
                    
                } else {
                    
                    OrderComparison orderComparison = new OrderComparison(cachedOrder, order);
                    
                    if (!orderComparison.getDifferences().isEmpty()) {
                        
                        HashMap<String, Difference> differences = orderComparison.getDifferences();
                        
                        StringBuilder orderUpdateMessage = new StringBuilder("Order ");
                        orderUpdateMessage.append(order.getOrdernumber()).append(": ");
                        
                        for (String field : differences.keySet()) {
                            
                            if (!field.endsWith("_username")) {
                                
                                String humanReadableFieldName = orderfields.getString(field).substring(0, 1).toUpperCase() + orderfields.getString(field).substring(1);
                                
                                orderUpdateMessage.append(humanReadableFieldName).append(" ändrades från ").append(differences.get(field).getOldValue()).append(" till ").append(differences.get(field).getNewValue()).append(". ");
                                
                            }
                            
                        }
                        
                        preparedStatement = connection.prepareStatement("UPDATE basic_order_data SET name = ?, "
                                + "updated = ? WHERE ordernumber = ? AND updated < ?");
                        
                        preparedStatement.setString(1, order.getName());
                        preparedStatement.setTimestamp(2, order.getUpdated());
                        preparedStatement.setInt(3, order.getOrdernumber());
                        preparedStatement.setTimestamp(4, order.getUpdated());
                        
                        preparedStatement.executeUpdate();
                        
                        if (orderComparison.relationshipsChanged()) {
                            
                            for (RelationshipDifference difference : orderComparison.getRelationshipDifferences()) {
                                
                                preparedStatement = connection.prepareStatement("UPDATE order_user_relationship "
                                        + "SET username = ?, "
                                        + "fullname = ? "
                                        + "WHERE ordernumber = ? AND relationship = ?");
                                
                                preparedStatement.setString(1, order.getRelationShip(difference.getType()).getUsername());
                                preparedStatement.setString(2, order.getRelationShip(difference.getType()).getFullname());
                                preparedStatement.setInt(3, order.getOrdernumber());
                                preparedStatement.setInt(4, difference.getType());
                                
                                preparedStatement.executeUpdate();
                                
                            }
                            
                        }
                        
                        HashSet<String> recipientOrderUpdatedList = new HashSet<String>();
                        HashSet<String> cancelledRelationshipUsers = new HashSet<String>();                        
                        HashSet<String> assignedRelationshipUsers = new HashSet<String>();
                        
                        cancelledRelationshipUsers.addAll(cachedOrder.getRelationshipUsernames());
                        assignedRelationshipUsers.addAll(order.getRelationshipUsernames());
                        
                        for (String recipient : order.getRelationshipUsernames()) {
                            
                            cancelledRelationshipUsers.remove(recipient);
                            
                            if (!recipientOrderUpdatedList.contains(recipient)) {
                                
                                recipientOrderUpdatedList.add(recipient);
                                
                            }
                            
                        }
                        
                        for (String recipient : cachedOrder.getRelationshipUsernames()) {
                            
                            assignedRelationshipUsers.remove(recipient);
                            
                            if (!recipientOrderUpdatedList.contains(recipient)) {
                                
                                recipientOrderUpdatedList.add(recipient);
                                
                            }
                            
                        }
                        
                        Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, "Updated order {0}", order.getOrdernumber().toString());
                        System.err.println(orderUpdateMessage.toString());
                        for (String recipient : recipientOrderUpdatedList) {
                            
                            if (!cancelledRelationshipUsers.contains(recipient)
                                    && !assignedRelationshipUsers.contains(recipient)) {
                                
                                sessionStore.publishMessageToUser(new OrderUpdateMessage(order.getOrdernumber(), orderComparison), recipient);
                                
                            }
                            
                            sessionStore.publishMessageToUser(new UserMessage(orderUpdateMessage.toString()), recipient);
                            
                        }
                        
                        for (String recipient : cancelledRelationshipUsers) {
                            
                            sessionStore.publishMessageToUser(new OrderRemovalMessage(order.getOrdernumber()), recipient);
                            
                        }
                        
                        for (String recipient : assignedRelationshipUsers) {
                            
                            sessionStore.publishMessageToUser(new AddOrderMessage(order), recipient);
                            
                        }
                        
                    }
                    
                }
                
            }
            
            resultSet = connection.createStatement().executeQuery("SELECT ordernumber "
                    + "FROM basic_order_data WHERE "
                    + purgeOrdersStatement.toString());
            
            while (resultSet.next()) {
                
                Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, "Finished order {0}", resultSet.getString("ordernumber"));
                
            }
            
            connection.createStatement().executeUpdate("DELETE FROM "
                    + "order_user_relationship WHERE " + purgeOrdersStatement.toString());
            
            if (connection.createStatement().executeUpdate("DELETE FROM basic_order_data WHERE "
                    + purgeOrdersStatement.toString()) > 0) {
                
                productionDatastore.updateOrderCache();
                
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
            
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
        
    }
    
    private String[] getUserDataById(int id) {
        
        String[] retval = userIdMap.get(id);
        
        if (retval == null) {
            
            userIdMap = cacheDBUtility.fetchUserIdMap();
            
            retval = userIdMap.get(id);
            
        }
        
        return retval;
        
    }
    
    private String[] getClientDataById(int id) {
        
        String[] retval = clientIdMap.get(id);
        
        if (retval == null) {
            
            clientIdMap = cacheDBUtility.fetchClientIdMap();
            
            retval = clientIdMap.get(id);
            
        }
        
        return retval;
        
    }
}
