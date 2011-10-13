package com.imprima.kesession;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Web application lifecycle listener.
 * @author henrik
 * 
 * Taps client-server communication and removes
 * com.imprima.kesession.UserSession objects from sessionstore as sessions get
 * destroyed (at timeout or when user logs out).
 * 
 */
public class SessionListener implements HttpSessionListener {

    private UserSessionController sessionStore = UserSessionController.getInstance();

    public SessionListener() {
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        
        sessionStore.removeSession(se.getSession().getId());
        
    }
    
}
