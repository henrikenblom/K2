package com.imprima.level9;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import org.cometd.server.CometdServlet;

/**
 *
 * @author henrik
 * 
 * Servlet extending org.cometd.server.CometdServlet, adding the possibility
 * to specify SubscriptionListener.
 * 
 */
@WebServlet(name = "L9Servlet", urlPatterns = {"/L9"})
public class L9Servlet extends CometdServlet {

    @Override
    public void init() throws ServletException {

        super.init();

        super.getBayeux().addListener(new L9SubscriptionListener());

    }

}
