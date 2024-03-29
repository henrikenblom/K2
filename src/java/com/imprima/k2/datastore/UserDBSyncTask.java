package com.imprima.k2.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import com.imprima.util.password.*;
import java.sql.DriverManager;

/**
 *
 * @author henrik
 * 
 * TimerTask to update and synchronize user database with GKS.
 * 
 */
public class UserDBSyncTask extends TimerTask {

    PasswordGenerator passwordGenerator = new PronouncablePasswordGenerator();
    
    @Override
    public void run() {

        synchronizeSystemUsers();
        synchronizeStandardUsers();

    }

    private void synchronizeSystemUsers() {

        ResultSet resultSet = null;
        Connection gksConnection = null;
        Connection userDBConnection = null;

        try {

            gksConnection = getGksConnection();
            userDBConnection = getUserDBConnection();

            resultSet = gksConnection.prepareStatement("SELECT ID AS id, cAnvändarnamn AS username, cLösenord AS password, cNamn AS fullname FROM Gks.dbo.Användare WHERE bRaderas = 0;").executeQuery();

            while (resultSet.next()) {

                if (userDBConnection.prepareStatement("UPDATE system_users SET "
                        + "username = '" + resultSet.getString("username") + "',"
                        + "password = '" + GKSPasswordCipher.decipher(resultSet.getString("password")) + "',"
                        + "fullname = '" + resultSet.getString("fullname") + "'"
                        + " WHERE id = " + resultSet.getString("id")).executeUpdate() == 0) {

                    userDBConnection.prepareStatement("INSERT INTO system_users (id, username, password, fullname) VALUES("
                            + resultSet.getString("id") + ", "
                            + "'" + resultSet.getString("username") + "',"
                            + "'" + GKSPasswordCipher.decipher(resultSet.getString("password")) + "',"
                            + "'" + resultSet.getString("fullname") + "')").executeUpdate();

                }

            }

        } catch (NamingException ex) {
            Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            if (resultSet != null) {

                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
                }

                resultSet = null;

            }

            if (gksConnection != null) {

                try {
                    gksConnection.close();
                } catch (SQLException ex) {
                }

                gksConnection = null;

            }

            if (userDBConnection != null) {

                try {
                    userDBConnection.close();
                } catch (SQLException ex) {
                }

                userDBConnection = null;

            }

        }

    }

    private void synchronizeStandardUsers() {

        ResultSet resultSet = null;
        Connection gksConnection = null;
        Connection userDBConnection = null;
        int userDBMaxId = 0;

        try {

            gksConnection = getGksConnection();
            userDBConnection = getUserDBConnection();

            resultSet = userDBConnection.prepareStatement("SELECT MAX(id) as max_id FROM users").executeQuery();

            if (resultSet.next()) {

                userDBMaxId = resultSet.getInt("max_id");

            }

            resultSet = gksConnection.prepareStatement("SELECT ID AS id, cAlias AS username, cFirma AS fullname FROM Gks.dbo.Företag WHERE bTypKund = 1 AND bRaderad = 0 AND ID > " + userDBMaxId).executeQuery();

            while (resultSet.next()) {
                
                PreparedStatement preparedStatement = userDBConnection.prepareStatement("INSERT INTO users (id, username, password, fullname) VALUES(?,?,?,?)");

                preparedStatement.setInt(1, resultSet.getInt("id"));
                preparedStatement.setString(2, resultSet.getString("username"));
                preparedStatement.setString(3, passwordGenerator.generate());
                preparedStatement.setString(4, resultSet.getString("fullname"));

                preparedStatement.executeUpdate();
                
                Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.INFO, "Created user ''{0}'' ({1}).", new String[]{resultSet.getString("username"), resultSet.getString("fullname")});
                
            }

        } catch (NamingException ex) {
            Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            if (resultSet != null) {

                try {
                    resultSet.close();
                } catch (SQLException ex) {
                    Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
                }

                resultSet = null;

            }

            if (gksConnection != null) {

                try {
                    gksConnection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
                }

                gksConnection = null;

            }

            if (userDBConnection != null) {

                try {
                    userDBConnection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
                }

                userDBConnection = null;

            }

        }

    }

    private Connection getGksConnection() throws NamingException, SQLException {

        return getGksDataSource().getConnection();

    }

    private Connection getUserDBConnection() throws NamingException, SQLException {

        return DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/user", "sa", "");

    }

    private DataSource getGksDataSource() throws NamingException {
        Context c = new InitialContext();
        return (DataSource) c.lookup("java:comp/env/gksDataSource");
    }

}
