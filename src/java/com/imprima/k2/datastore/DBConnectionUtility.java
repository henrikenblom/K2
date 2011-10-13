/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author henrik
 */
public class DBConnectionUtility {

    public static Connection getGksConnection() throws NamingException, SQLException {
        
        Context c = new InitialContext();
        return ((DataSource) c.lookup("java:comp/env/gksDataSource")).getConnection();
    
    }

    public static Connection getUserDBConnection() throws SQLException {

        return DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/user", "sa", "");

    }
    
    public static Connection getCacheDBConnection() throws SQLException {

        return DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/cache", "sa", "");

    }
    
}
