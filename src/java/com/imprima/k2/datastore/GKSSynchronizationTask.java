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
import java.sql.Timestamp;
import java.util.Collection;
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
    private UserSessionController userSessionController = UserSessionController.getInstance();
    private HashMap<Integer, String[]> userIdMap;
    private HashMap<Integer, String[]> clientIdMap;
    private ResourceBundle orderfields = ResourceBundle.getBundle("orderfields");
    private ProductionPlanUtility productionPlanUtility = ProductionPlanUtility.getInstance();
    private int lowestGKSOrderId = 0;
    private int highestGKSOrderId = 0;

    @Override
    public void run() {

        userIdMap = cacheDBUtility.fetchUserIdMap();
        clientIdMap = cacheDBUtility.fetchClientIdMap();

        putOrders(fetchActiveOrders());
        putProductionPlans(fetchProductionPlans());

    }

    private HashMap<Integer, Order> fetchActiveOrders() {

        HashMap<Integer, Order> retval = new HashMap<Integer, Order>();
        Connection connection = null;
        ResultSet resultSet = null;

        try {

            connection = DBConnectionUtility.getGksConnection();

            resultSet = connection.prepareStatement("SELECT DISTINCT "
                    + "oo.ID id, "
                    + "oo.iNummer ordernumber, oo.cBenämning name, oo.rFöretagID client_id, "
                    + "oo.rPersonalIDSäljare salesperson_id, oo.rPersonalIDHandläggare projectmanager_id, "
                    + "oo.dSenastUppdaterad updated, "
                    + "oo.dDatum orderdate "
                    + "FROM Gks.dbo.OffertOrder oo WHERE "
                    + "(oo.iNummer > 200000 OR oo.iNummer < 100000) "
                    + "AND oo.iTyp != 21 AND oo.dFaktureradDatum IS NULL AND "
                    + "oo.dLeveransdatumUtlovad > DATEADD(month, -1, GETDATE()) "
                    + "ORDER BY oo.ID").executeQuery();

            while (resultSet.next()) {

                Order order = new Order(resultSet.getInt("ordernumber"),
                        resultSet.getString("name"),
                        resultSet.getTimestamp("updated"),
                        resultSet.getInt("ordernumber") > 200000,
                        resultSet.getTimestamp("orderdate"));

                order.setGksReferenceId(resultSet.getInt("id"));

                if (lowestGKSOrderId == 0) {

                    lowestGKSOrderId = resultSet.getInt("id");

                }

                if (resultSet.getInt("id") > highestGKSOrderId) {

                    highestGKSOrderId = resultSet.getInt("id");

                }

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

                retval.put(resultSet.getInt("id"), order);

            }

        } catch (Exception ex) {

            Logger.getLogger(GKSSynchronizationTask.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            if (resultSet != null) {

                try {

                    if (!resultSet.isClosed()) {
                        resultSet.close();
                    }

                    resultSet = null;

                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (connection != null) {

                try {

                    if (!connection.isClosed()) {
                        connection.close();
                    }

                    connection = null;

                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return retval;

    }

    private Collection<ProductionPlan> fetchProductionPlans() {

        Connection connection = null;
        HashMap<Integer, ProductionPlan> retval = new HashMap<Integer, ProductionPlan>();
        ResultSet resultSet = null;
        HashMap<Integer, Timestamp[]> productionTimes = null;

        try {

            connection = DBConnectionUtility.getGksConnection();

            StringBuilder ordernumberList = new StringBuilder("(pej.ORDERNO = ");
            boolean firstEntry = true;

            for (Integer ordernumber : productionDatastore.getProductionOrdernumbers()) {

                if (!firstEntry) {
                    ordernumberList.append(" OR pej.ORDERNO = ");
                }

                ordernumberList.append(ordernumber);
                firstEntry = false;

            }

            ordernumberList.append(")");

            resultSet = connection.createStatement().executeQuery("SELECT DISTINCT "
                    + "pp.ID AS id, "
                    + "peq.DESCRIPTION AS queue, "
                    + "pp.PROCESSSTATEID AS state, "
                    + "ppt.DESCRIPTION AS step, "
                    + "pp.DESCRIPTION AS details, "
                    + "pp.LASTSTARTED AS laststarted, "
                    + "pp.cSubContractor AS subcontractor, "
                    + "pp.TIMESPAN AS timespan, "
                    + "pej.ORDERNO AS ordernumber, "
                    + "pej.MATERIALDAY AS materialday, "
                    + "pej.FIRSTPROOFDAY AS firstproofday, "
                    + "oo.dLeveransdatumUtlovad AS deliverydate, "
                    + "pp.QUEUEID AS queueid, "
                    + "pp.iOrder AS ordering, "
                    + "pp.cImposition AS imposition, "
                    + "pp.cPaperInfo AS paperinfo, "
                    + "pp.PrintPartName AS printpart "
                    + "FROM "
                    + "Gks.dbo.PE_QUEUE peq, "
                    + "Gks.dbo.PE_JOB pej,  "
                    + "Gks.dbo.PE_PROCESS pp, "
                    + "Gks.dbo.PE_PROCESSTYPE ppt, "
                    + "Gks.dbo.PE_PROCESSTATE pps, "
                    + "Gks.dbo.OffertOrder oo "
                    + "WHERE "
                    + "pp.QUEUEID != -999 AND "
                    + "pej.ORDERID = oo.ID AND "
                    + "peq.ID = pp.QUEUEID AND "
                    + "pp.DELETED = 0 AND "
                    + "ppt.ID = pp.PROCESSTYPEID AND "
                    + "pp.JOBID = pej.ID AND "
                    + ordernumberList.toString()
                    + " ORDER BY pp.id ASC");

            ordernumberList = null;

            productionTimes = fetchProductionTimes(connection);

            while (resultSet.next()) {

                if (productionDatastore.containsOrder(resultSet.getInt("ordernumber"))) {

                    if (productionPlanUtility.timeIsValid(resultSet.getTimestamp("deliverydate"))
                            && productionPlanUtility.timeIsValid(resultSet.getTimestamp("materialday"))) {

                        ProductionPlan productionPlan;

                        if (retval.containsKey(resultSet.getInt("ordernumber"))) {

                            productionPlan = retval.get(resultSet.getInt("ordernumber"));

                        } else {

                            productionPlan = new ProductionPlan(resultSet.getInt("ordernumber"));

                            Timestamp orderdate = productionDatastore.getOrder(resultSet.getInt("ordernumber")).getOrderdate();

                            ProductionStep initialProductionStep = new ProductionStep(-4, "Material", "Kund", ProductionPlanUtility.QUEUEID_CLIENT);
                            initialProductionStep.setStarttime(orderdate);
                            initialProductionStep.setLaststarted(orderdate);
                            initialProductionStep.setStoptime(resultSet.getTimestamp("materialday"));
                            initialProductionStep.setOrdering(1);

                            productionPlan.add(initialProductionStep);

                            retval.put(resultSet.getInt("ordernumber"), productionPlan);

                        }

                        ProductionStep productionStep = new ProductionStep(
                                resultSet.getInt("state"),
                                resultSet.getString("details"),
                                resultSet.getString("queue"),
                                resultSet.getInt("queueid"));

                        productionStep.setLaststarted(resultSet.getTimestamp("laststarted"));
                        productionStep.setSubcontractor(resultSet.getString("subcontractor"));
                        productionStep.setTimespan(resultSet.getInt("timespan"));
                        productionStep.setOrdering(resultSet.getInt("ordering"));
                        productionStep.setImposition(resultSet.getString("imposition"));
                        productionStep.setPaperinfo(resultSet.getString("paperinfo"));
                        productionStep.setPrintpart(resultSet.getString("printpart"));

                        if (productionTimes.containsKey(resultSet.getInt("id"))) {

                            productionStep.setStarttime(productionTimes.get(resultSet.getInt("id"))[0]);
                            productionStep.setStoptime(productionTimes.get(resultSet.getInt("id"))[1]);

                        } else if (productionPlanUtility.getType(resultSet.getInt("queueid")) == ProductionPlanUtility.TYPE.PREPRESS
                                && resultSet.getTimestamp("materialday") != null) {

                            productionStep.setStarttime(resultSet.getTimestamp("materialday"));
                            productionStep.setStoptime(resultSet.getTimestamp("firstproofday"));

                        } else if (productionPlanUtility.getType(resultSet.getInt("queueid")) == ProductionPlanUtility.TYPE.DELIVERY) {

                            productionStep.setStarttime(resultSet.getTimestamp("deliverydate"));
                            productionStep.setStoptime(new Timestamp(productionStep.getStarttime().getTime() + ProductionStep.WORKDDAYLENGTH));

                        }

                        productionPlan.add(productionStep);

                    }

                }

            }

        } catch (Exception ex) {

            Logger.getLogger(GKSSynchronizationTask.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            productionTimes = null;

            if (resultSet != null) {
                try {
                    if (!resultSet.isClosed()) {
                        resultSet.close();
                    }
                    resultSet = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (connection != null) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                    connection = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return retval.values();

    }

    private void putProductionPlans(Collection<ProductionPlan> productionPlanCollection) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        HashMap<Integer, ProductionPlan> cachedProductionPlanMap = cacheDBUtility.fetchProductionPlanMap();

        boolean cacheDirty = false;

        try {

            StringBuilder purgeProductionPlanStatement = new StringBuilder();

            boolean keepMultiple = false;

            connection = DBConnectionUtility.getCacheDBConnection();

            for (ProductionPlan productionPlan : productionPlanCollection) {

                productionPlanUtility.estimateProductionTimes(productionPlan);

                if (keepMultiple) {

                    purgeProductionPlanStatement.append(" AND");

                }

                purgeProductionPlanStatement.append(" ordernumber != ").append(productionPlan.getOrdernumber());

                keepMultiple = true;

                ProductionPlan cachedProductionPlan = cachedProductionPlanMap.get(productionPlan.getOrdernumber());

                if (cachedProductionPlan == null) {

                    addProductionPlan(productionPlan, connection);

                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, "Added production plan for order " + productionPlan.getOrdernumber());

                    cacheDirty = true;

                } else if (!productionPlan.equals(cachedProductionPlan)) {

                    removeProductionPlan(productionPlan, connection);
                    addProductionPlan(productionPlan, connection);

                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, "Updated production plan for order " + productionPlan.getOrdernumber());

                    cacheDirty = true;

                }

            }

            if (purgeProductionPlanStatement.length() > 0) {

                cacheDirty = cacheDirty || connection.createStatement().executeUpdate("DELETE FROM "
                        + "production_step_data "
                        + "WHERE "
                        + purgeProductionPlanStatement.toString()) > 0;

            }

            if (cacheDirty) {

                productionDatastore.updateProductionPlanCache();

            }

        } catch (SQLException ex) {

            Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

            cachedProductionPlanMap = null;

            if (resultSet != null) {
                try {
                    if (!resultSet.isClosed()) {
                        resultSet.close();
                    }
                    resultSet = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

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
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                    connection = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    private void removeProductionPlan(ProductionPlan productionPlan, Connection connection) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM "
                + "production_step_data WHERE "
                + "ordernumber = ?");

        preparedStatement.setInt(1, productionPlan.getOrdernumber());

        preparedStatement.executeUpdate();

    }

    private void addProductionPlan(ProductionPlan productionPlan, Connection connection) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "
                + "production_step_data("
                + "ordernumber, "
                + "state, "
                + "queue, "
                + "queueid, "
                + "details, "
                + "starttime, "
                + "stoptime, "
                + "laststarted, "
                + "subcontractor, "
                + "timespan, "
                + "ordering, "
                + "imposition, "
                + "paperinfo, "
                + "printpart) "
                + "VALUES "
                + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        for (ProductionStep productionStep : productionPlan) {

            preparedStatement.setInt(1, productionPlan.getOrdernumber());
            preparedStatement.setInt(2, productionStep.getState());
            preparedStatement.setString(3, productionStep.getQueuename());
            preparedStatement.setInt(4, productionStep.getQueueid());
            preparedStatement.setString(5, productionStep.getDetails());
            preparedStatement.setTimestamp(6, productionStep.getStarttime());
            preparedStatement.setTimestamp(7, productionStep.getStoptime());
            preparedStatement.setTimestamp(8, productionStep.getLaststarted());
            preparedStatement.setString(9, productionStep.getSubcontractor());
            preparedStatement.setInt(10, productionStep.getTimespan());
            preparedStatement.setInt(11, productionStep.getOrdering());
            preparedStatement.setString(12, productionStep.getImposition());
            preparedStatement.setString(13, productionStep.getPaperinfo());
            preparedStatement.setString(14, productionStep.getPrintpart());

            preparedStatement.executeUpdate();

        }

    }

    private void putOrders(HashMap<Integer, Order> orderMap) {

        Connection cacheDBConnection = null;
        Connection gksConnection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        HashMap<Integer, Order> cachedOrderMap = cacheDBUtility.fetchOrderMap();
        boolean cacheDirty = false;
        int lastKnownOrderNumber = -1;

        try {

            cacheDBConnection = DBConnectionUtility.getCacheDBConnection();
            gksConnection = DBConnectionUtility.getGksConnection();

            preparedStatement = gksConnection.prepareStatement("SELECT "
                    + "sr.rOffertOrderID id, "
                    + "sr.cKolumn1 column1, "
                    + "sr.cKolumn2 column2, "
                    + "sr.cKolumn3 column3, "
                    + "sr.iRadnummer rownumber "
                    + "FROM Gks.dbo.SpecifikationRad sr "
                    + "WHERE sr.rOffertOrderID >= ? "
                    + "AND sr.rOffertOrderID <= ? ORDER BY "
                    + "sr.iRadnummer ASC");

            preparedStatement.setInt(1, lowestGKSOrderId);
            preparedStatement.setInt(2, highestGKSOrderId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                if (orderMap.get(resultSet.getInt("id")) != null) {

                    orderMap.get(resultSet.getInt("id")).addOrderDataEntry(new OrderDataEntry(
                            resultSet.getString("column1").trim(),
                            resultSet.getString("column2").trim(),
                            resultSet.getString("column3").trim(),
                            resultSet.getInt("rownumber")));

                }

            }

            StringBuilder purgeOrdersStatement = new StringBuilder();

            boolean keepMultiple = false;

            for (Order order : orderMap.values()) {

                lastKnownOrderNumber = order.getOrdernumber();

                if (keepMultiple) {

                    purgeOrdersStatement.append(" AND");

                }

                purgeOrdersStatement.append(" ordernumber != ").append(order.getOrdernumber());

                keepMultiple = true;

                Order cachedOrder = cachedOrderMap.get(order.getOrdernumber());

                if (cachedOrder == null) {

                    addOrder(order, cacheDBConnection);

                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, "Added order {0}", order.getOrdernumber().toString());

                    cacheDirty = true;

                } else {

                    OrderComparison orderComparison = new OrderComparison(cachedOrder, order);

                    if (!orderComparison.getDifferences().isEmpty()) {

                        HashMap<String, Difference> differences = orderComparison.getDifferences();

                        StringBuilder orderUpdateMessage = new StringBuilder("Order ");
                        orderUpdateMessage.append(order.getOrdernumber()).append(": ");

                        for (String field : differences.keySet()) {

                            if (!field.endsWith("_username")) {

                                String humanReadableFieldName = orderfields.getString(field).substring(0, 1).toUpperCase() + orderfields.getString(field).substring(1);

                                orderUpdateMessage.append(humanReadableFieldName);

                                if (differences.get(field).getOldValue() != null) {

                                    orderUpdateMessage.append(" ändrades från ").append(differences.get(field).getOldValue());

                                } else {

                                    orderUpdateMessage.append(" sattes");

                                }

                                orderUpdateMessage.append(" till ").append(differences.get(field).getNewValue()).append(". ");

                            }

                        }

                        preparedStatement = cacheDBConnection.prepareStatement("UPDATE basic_order_data SET name = ?, "
                                + "updated = ? WHERE ordernumber = ? AND updated < ?");

                        preparedStatement.setString(1, order.getName());
                        preparedStatement.setTimestamp(2, order.getUpdated());
                        preparedStatement.setInt(3, order.getOrdernumber());
                        preparedStatement.setTimestamp(4, order.getUpdated());

                        preparedStatement.executeUpdate();

                        cacheDirty = true;

                        if (orderComparison.relationshipsChanged()) {

                            for (RelationshipDifference difference : orderComparison.getRelationshipDifferences()) {

                                if (difference.getOldValue() == null) {

                                    preparedStatement = cacheDBConnection.prepareStatement("INSERT INTO "
                                            + "order_user_relationship(username, fullname, ordernumber, relationship) VALUES(?,?,?,?)");

                                } else {

                                    preparedStatement = cacheDBConnection.prepareStatement("UPDATE order_user_relationship "
                                            + "SET username = ?, "
                                            + "fullname = ? "
                                            + "WHERE ordernumber = ? AND relationship = ?");

                                }

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

                                userSessionController.publishMessageToUser(new OrderUpdateMessage(order.getOrdernumber(), orderComparison), recipient);

                            }

                            userSessionController.publishMessageToUser(new UserMessage(orderUpdateMessage.toString()), recipient);

                        }

                        for (String recipient : cancelledRelationshipUsers) {

                            userSessionController.publishMessageToUser(new OrderRemovalMessage(order.getOrdernumber()), recipient);

                        }

                        for (String recipient : assignedRelationshipUsers) {

                            userSessionController.publishMessageToUser(new AddOrderMessage(order), recipient);

                        }

                    }

                }

            }

            if (purgeOrdersStatement.length() > 0) {

                resultSet = cacheDBConnection.createStatement().executeQuery("SELECT ordernumber "
                        + "FROM basic_order_data WHERE "
                        + purgeOrdersStatement.toString());

                while (resultSet.next()) {

                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.INFO, "Finished order {0}", resultSet.getString("ordernumber"));

                }

                cacheDBConnection.createStatement().executeUpdate("DELETE FROM "
                        + "order_user_relationship WHERE " + purgeOrdersStatement.toString());

                cacheDirty = cacheDirty || cacheDBConnection.createStatement().executeUpdate("DELETE FROM basic_order_data WHERE "
                        + purgeOrdersStatement.toString()) > 0;

            }

            if (cacheDirty) {

                productionDatastore.updateOrderCache();

            }

        } catch (Exception ex) {

            Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
            Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, "Last known order number: " + lastKnownOrderNumber);

        } finally {

            cachedOrderMap = null;

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                    preparedStatement = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (cacheDBConnection != null) {
                try {
                    if (!cacheDBConnection.isClosed()) {
                        cacheDBConnection.close();
                    }
                    cacheDBConnection = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (gksConnection != null) {
                try {
                    if (!gksConnection.isClosed()) {
                        gksConnection.close();
                    }
                    gksConnection = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ProductionDatastore.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    private void addOrder(Order order, Connection connection) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "
                + "basic_order_data(name, ordernumber, updated, productionorder, orderdate, gksreferenceid) "
                + "VALUES(?,?,?,?,?,?)",
                PreparedStatement.RETURN_GENERATED_KEYS);

        preparedStatement.setString(1, order.getName());
        preparedStatement.setInt(2, order.getOrdernumber());
        preparedStatement.setTimestamp(3, order.getUpdated());
        preparedStatement.setBoolean(4, order.isProductionOrder());
        preparedStatement.setTimestamp(5, order.getOrderdate());
        preparedStatement.setInt(6, order.getGksReferenceId());

        preparedStatement.executeUpdate();

        ResultSet resultSet = preparedStatement.getGeneratedKeys();

        if (resultSet.next()) {

            preparedStatement = connection.prepareStatement("INSERT INTO "
                    + "additional_order_data(basic_order_data_id, column1, column2, column3) "
                    + "VALUES(?,?,?,?)");

            for (OrderDataEntry orderDataEntry : order.getOrderDataEntrys()) {

                preparedStatement.setInt(1, resultSet.getInt(1));
                preparedStatement.setString(2, orderDataEntry.getColumn1());
                preparedStatement.setString(3, orderDataEntry.getColumn2());
                preparedStatement.setString(4, orderDataEntry.getColumn3());

                preparedStatement.executeUpdate();

            }

            for (OrderUserRelationship orderUserRelationship : order.getRelationships().values()) {

                preparedStatement = connection.prepareStatement("INSERT INTO "
                        + "order_user_relationship(ordernumber, username, fullname, relationship) "
                        + "VALUES(?,?,?,?)");

                preparedStatement.setInt(1, order.getOrdernumber());
                preparedStatement.setString(2, orderUserRelationship.getUsername());
                preparedStatement.setString(3, orderUserRelationship.getFullname());
                preparedStatement.setInt(4, orderUserRelationship.getRelationship());

                preparedStatement.executeUpdate();

                userSessionController.publishMessageToUser(new AddOrderMessage(order), orderUserRelationship.getUsername());
                userSessionController.publishMessageToUser(new UserMessage("Ny order: " + order.getOrdernumber() + " " + order.getName()), orderUserRelationship.getUsername());

            }

        }

    }

    private HashMap<Integer, Timestamp[]> fetchProductionTimes(Connection connection) throws SQLException {

        HashMap<Integer, Timestamp[]> retval = new HashMap<Integer, Timestamp[]>();

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT PT.ProcessID AS processid, "
                + "pt.StartTime AS starttime, "
                + "pt.StopTime AS stoptime "
                + "FROM Gks.dbo.PE_PROCESSTIME AS pt, "
                + "(SELECT MAX(ID) AS id, ProcessID FROM Gks.dbo.PE_PROCESSTIME WHERE DELETED = 0 GROUP BY ProcessID) AS maxidtable "
                + "WHERE "
                //+ "pt.Starttime > ? AND "
                + "pt.ID = maxidtable.id");

        //preparedStatement.setTimestamp(1, productionDatastore.getLowestProductionOrderTimestamp());

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {

            Timestamp[] entry = {resultSet.getTimestamp("starttime"), resultSet.getTimestamp("stoptime")};
            retval.put(resultSet.getInt("processid"), entry);

        }

        if (!resultSet.isClosed()) {
            resultSet.close();
        }
        resultSet = null;

        return retval;

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
