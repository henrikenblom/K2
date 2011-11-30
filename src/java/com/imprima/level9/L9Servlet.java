package com.imprima.level9;

import com.google.gson.Gson;
import com.imprima.kesession.UserSessionController;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public void service(ServletRequest req, ServletResponse resp) {
        
        String sessionId = null;
        
        for (Cookie cookie : ((HttpServletRequest) req).getCookies()) {
            
            if (cookie.getName().equals("JSESSIONID")) {
            
                sessionId = cookie.getValue();
                break;
                
            }
            
        }
        
        if (userSessionController.userSessionExists(sessionId)) {
            
            try {
                super.service(req, resp);
            } catch (ServletException ex) {
                Logger.getLogger(L9Servlet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(L9Servlet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalStateException ex) {
                Logger.getLogger(L9Servlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
        
    }
    
}
