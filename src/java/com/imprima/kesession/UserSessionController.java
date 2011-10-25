package com.imprima.kesession;

import com.imprima.k2.datastore.DBConnectionUtility;
import com.imprima.level9.Message;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author henrik
 * 
 * Singleton class to manage com.imprima.kesession.UserSession objects.
 * 
 */
public class UserSessionController {

    public static final int SYSTEM_USERS = 0;
    public static final int USERS = 1;
    public static final int ALL_USERS = 2;
    private static UserSessionController instance = null;
    private int channelId = 0;
    private HashMap<Integer, UserSession> sessionList = new HashMap<Integer, UserSession>();
    private HashMap<String, Integer> channelIdCache = new HashMap<String, Integer>();

    protected UserSessionController() {
    }

    public static UserSessionController getInstance() {
        if (instance == null) {
            instance = new UserSessionController();
        }
        return instance;
    }

    public UserSession createSession(HttpSession httpSession, String username, ServletContext servletContext) {

        UserSession userSession = new UserSession(channelId, username, httpSession, servletContext);

        sessionList.put(channelId, userSession);
        channelIdCache.put(httpSession.getId(), channelId);

        channelId++;

        populateSession(userSession);

        return userSession;

    }

    public HashMap<Integer, UserSession> getSessions() {

        return sessionList;

    }

    public Integer[] getIdList() {

        return (Integer[]) sessionList.keySet().toArray();

    }

    public boolean sessionExists(String sessionId) {

        return channelIdCache.containsKey(sessionId);

    }

    public boolean userSessionExists(String sessionId) {
        
        return sessionId != null && channelIdCache.containsKey(sessionId);
        
    }
    
    public boolean userSessionExists(Integer id) {
        
        return id != null && sessionList.containsKey(id);
        
    }
    
    public UserSession getUserSession(String sessionId) {

        return sessionList.get(channelIdCache.get(sessionId));

    }

    public UserSession getUserSession(Integer id) {

        return sessionList.get(id);

    }

    public void removeSession(String sessionId) {

        try {

            getUserSession(sessionId).prepareForRemoval();

            sessionList.remove(channelIdCache.get(sessionId));
            channelIdCache.remove(sessionId);

        } catch (NullPointerException ex) {
            //no-op (Redan avslutad)
        }

    }

    public void destroy() {

        for (UserSession entry : sessionList.values()) {

            entry.prepareForRemoval();

        }

        sessionList.clear();
        channelIdCache.clear();

    }

    private void populateSession(UserSession userSession) {

        Connection connection = null;
        ResultSet resultSet = null;

        try {

            connection = DBConnectionUtility.getUserDBConnection();

            resultSet = connection.prepareStatement("SELECT id, "
                    + "fullname "
                    + "FROM system_users "
                    + "WHERE username = '" + userSession.getUsername().toUpperCase() + "'").executeQuery();

            if (resultSet.next()) {

                userSession.put("fullname", resultSet.getString("fullname"));
                userSession.put("id", resultSet.getString("id"));
                userSession.setSysUser(true);

            } else {

                resultSet = connection.prepareStatement("SELECT id, "
                        + "fullname "
                        + "FROM users "
                        + "WHERE username = '" + userSession.getUsername().toUpperCase() + "'").executeQuery();

                if (resultSet.next()) {

                    userSession.put("fullname", resultSet.getString("fullname"));
                    userSession.put("id", resultSet.getString("id"));
                    userSession.setSysUser(true);

                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void publishMessageToUsergroup(Message message, int usergroup) {

        for (UserSession entry : getSessions().values()) {

            if (usergroup == SYSTEM_USERS && entry.isSysUser()) {

                entry.publishMessage(message);

            } else if (usergroup == USERS && !entry.isSysUser()) {

                entry.publishMessage(message);

            } else {

                entry.publishMessage(message);

            }

        }

    }
    
    public void publishMessageToUser(Message message, String username) {

        for (UserSession userSession : getSessions().values()) {
            
            if (userSession.getUsername().equalsIgnoreCase(username)) {
                
                userSession.publishMessage(message);

            }

        }
        
    }
    
    public void publishMessageToUsers(Message message, Set<String> usernames) {
        
        for (String username : usernames) {
            
            publishMessageToUser(message, username);
            
        }
        
    }
    
}
