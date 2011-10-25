package com.imprima.level9;

import com.google.gson.Gson;
import com.imprima.kesession.UserSessionController;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.cometd.server.CometdServlet;

/**
 *
 * @author henrik
 * 
 * Servlet extending org.cometd.server.CometdServlet, adding the possibility
 * to specify SubscriptionListener.
 * 
 */
public class L9Servlet extends CometdServlet {
    
    private Gson gson = new Gson();
    private UserSessionController userSessionController = UserSessionController.getInstance();
    
    @Override
    public void init() throws ServletException {

        super.init();

        super.getBayeux().addListener(new L9SubscriptionListener());

    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        
        String sessionId = null;
        
        for (Cookie cookie : ((HttpServletRequest) req).getCookies()) {
            
            if (cookie.getName().equals("JSESSIONID")) {
            
                sessionId = cookie.getValue();
                break;
                
            }
            
        }
        
        if (userSessionController.userSessionExists(sessionId)) {
        
            super.service(req, resp);
        
        }
        
    }
    
}
