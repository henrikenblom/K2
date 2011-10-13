package com.imprima.level9;

import com.imprima.kesession.UserSessionController;
import com.imprima.kesession.UserSession;
import org.cometd.bayeux.server.BayeuxServer.SubscriptionListener;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerSession;

/**
 *
 * @author henrik
 * 
 * Implements a SubscriptionListener to keep track of open Bayeux channels.
 * 
 */
public class L9SubscriptionListener implements SubscriptionListener {

    UserSessionController sessionStore = UserSessionController.getInstance();

    @Override
    public void subscribed(ServerSession ss, ServerChannel sc) {

        try {

            Integer id = Integer.parseInt(sc.getId().replace("/", ""));

            UserSession userSession = sessionStore.getUserSession(id);

            if (!userSession.hasEstablishedL9Connection()) {
                
                userSession.setHasEstablishedL9Connection(true);

                sessionStore.publishMessageToUsergroup(new UserMessage(userSession.get("fullname") + " loggade in."), UserSessionController.SYSTEM_USERS);

            }

        } catch (NumberFormatException ex) {
            //no-op
        }
        
    }

    @Override
    public void unsubscribed(ServerSession ss, ServerChannel sc) {

    }
}
