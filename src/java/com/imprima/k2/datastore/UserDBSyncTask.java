package com.imprima.k2.datastore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import com.imprima.util.password.*;

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

            gksConnection = DBConnectionUtility.getGksConnection();
            userDBConnection = DBConnectionUtility.getUserDBConnection();

            resultSet = gksConnection.prepareStatement("SELECT ID AS id, "
                    + "cAnvändarnamn AS username, "
                    + "cLösenord AS password, "
                    + "cNamn AS fullname, "
                    + "cEpost AS email "
                    + "FROM Gks.dbo.Användare WHERE bRaderas = 0").executeQuery();

            while (resultSet.next()) {

                if (userDBConnection.prepareStatement("UPDATE system_users SET "
                        + "username = '" + resultSet.getString("username") + "',"
                        + "password = '" + GKSPasswordCipher.decipher(resultSet.getString("password")) + "',"
                        + "fullname = '" + resultSet.getString("fullname") + "',"
                        + "email = '" + resultSet.getString("email") + "'"
                        + " WHERE id = " + resultSet.getString("id")).executeUpdate() == 0) {

                    userDBConnection.prepareStatement("INSERT INTO system_users (id, username, password, fullname, email) VALUES("
                            + resultSet.getString("id").trim() + ", "
                            + "'" + resultSet.getString("username").trim() + "',"
                            + "'" + GKSPasswordCipher.decipher(resultSet.getString("password")) + "',"
                            + "'" + resultSet.getString("fullname").trim() + "',"
                            + "'" + (resultSet.getString("email") == null ? "" : resultSet.getString("email").trim()) + "')").executeUpdate();
                    
                }

            }

        } catch (Exception ex) {
            Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            if (resultSet != null) {

                try {
                    if (!resultSet.isClosed()) {
                        resultSet.close();
                    }
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

            gksConnection = DBConnectionUtility.getGksConnection();
            userDBConnection = DBConnectionUtility.getUserDBConnection();

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
                    if (!resultSet.isClosed()) {
                        resultSet.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
                }

                resultSet = null;

            }

            if (gksConnection != null) {

                try {

                    if (!gksConnection.isClosed()) {
                        gksConnection.close();
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
                }

                gksConnection = null;

            }

            if (userDBConnection != null) {

                try {

                    if (!userDBConnection.isClosed()) {
                        userDBConnection.close();
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(UserDBSyncTask.class.getName()).log(Level.SEVERE, null, ex);
                }

                userDBConnection = null;

            }

        }

    }

}
