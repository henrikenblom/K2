package com.imprima.kesession;

import com.imprima.level9.Message;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.LocalSession;

/**
 *
 * @author henrik
 * 
 * Class for storing user sessions and to manage each sessions's Bayeux channel
 * for server to client communication.
 * 
 */
public class UserSession extends HashMap<String, String> {

    public static final String DEFAULT_LANG = "sv";
    
    private String lang = DEFAULT_LANG;
    private LocalSession localSession;
    private ClientSessionChannel sessionChannel;
    private HttpSession httpSession;
    private String username;
    private String sessionId;
    private int channelId;
    private boolean sysUser = false;
    private boolean hasEstablishedL9Connection = false;

    public UserSession(int channelId, String username, HttpSession httpSession, ServletContext servletContext) {

        this.channelId = channelId;
        this.username = username;
        this.httpSession = httpSession;
        this.sessionId = httpSession.getId();

        localSession = ((BayeuxServer) servletContext.getAttribute(BayeuxServer.ATTRIBUTE)).newLocalSession(sessionId);
        localSession.handshake();
        
        sessionChannel = localSession.getChannel("/" + channelId);

    }

    public HttpSession getHttpSession() {
        return httpSession;
    }

    public boolean isSysUser() {
        return sysUser;
    }

    public void setSysUser(boolean sysUser) {
        this.sysUser = sysUser;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getChannelId() {
        return channelId;
    }
    
    public boolean hasEstablishedL9Connection() {
        return hasEstablishedL9Connection;
    }

    public void setHasEstablishedL9Connection(boolean hasEstablishedL9Connection) {
        
        this.hasEstablishedL9Connection = hasEstablishedL9Connection;
        
    }
    
    public ClientSessionChannel getSessionChannel() {
        return sessionChannel;
    }
    
    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public void publishMessage(Message message) {

        getSessionChannel().publish(message.toJson());

    }
    
    public void prepareForRemoval() {

        sessionChannel.unsubscribe();
        localSession.disconnect();
        
        sessionChannel = null;
        localSession = null;

    }
    
}
