package com.imprima.kesession;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

/**
 *
 * @author henrik
 * 
 * Taps client-server communication to create and store
 * com.imprima.kesession.UserSession objects as well as setting an unprotected
 * cookie with channel id for Bayeux communication.
 *
 */
public class SessionIdFilter implements Filter {

    private static final boolean debug = false;
    private FilterConfig filterConfig = null;
    private UserSessionController sessionStore = UserSessionController.getInstance();

    public SessionIdFilter() {
    }

    private void doBeforeProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException {
        if (debug) {
            log("SessionIdFilter:DoBeforeProcessing");
        }

    }

    private void doAfterProcessing(RequestWrapper request, ResponseWrapper response)
            throws IOException, ServletException {
        if (debug) {
            log("SessionIdFilter:DoAfterProcessing");
        }

    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        /* 
         * Tomcat tracks the httpSession using the JSESSIONID. When the httpSession is 
         * created as a consequence of a request of a secure page, however, the 
         * "secure" attribute of the cookie is set to true. That prevents the 
         * httpSession to be consecutively tracked on non-secure pages. We would 
         * like a unified approach, though. 
         */

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        final HttpSession httpSession = httpRequest.getSession(false);
        UserSession userSession = null;
                
        if (httpRequest.getRemoteUser() != null && !sessionStore.sessionExists(httpSession.getId())) {

            userSession = sessionStore.createSession(httpSession, httpRequest.getRemoteUser(), request.getServletContext());
            
        } else if (sessionStore.sessionExists(httpSession.getId())) {
            
            userSession = sessionStore.getUserSession(httpSession.getId());
            
        }

        if (userSession != null) {
            
            final Cookie sessionCookie = new Cookie("CHANNELID", String.valueOf(userSession.getChannelId()));
            sessionCookie.setMaxAge(httpSession.getMaxInactiveInterval());
            sessionCookie.setSecure(false);
            sessionCookie.setPath(httpRequest.getContextPath());
            httpResponse.addCookie(sessionCookie);
            
            httpResponse.addHeader("Content-Language", userSession.getLang());
            
        } else {
            
            httpResponse.addHeader("Content-Language", UserSession.DEFAULT_LANG);
            
        }

        chain.doFilter(request, response);

    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter 
     */
    @Override
    public void destroy() {
        
        sessionStore.destroy();
        
    }

    /**
     * Init method for this filter 
     */
    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
                log("SessionIdFilter: Initializing filter");
            }
        }
    }

    private void sendProcessingError(Throwable t, ServletResponse response) {
        String stackTrace = getStackTrace(t);

        if (stackTrace != null && !stackTrace.equals("")) {
            try {
                response.setContentType("text/html");
                PrintStream ps = new PrintStream(response.getOutputStream());
                PrintWriter pw = new PrintWriter(ps);
                pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

                // PENDING! Localize this for next official release
                pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");
                pw.print(stackTrace);
                pw.print("</pre></body>\n</html>"); //NOI18N
                pw.close();
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        } else {
            try {
                PrintStream ps = new PrintStream(response.getOutputStream());
                t.printStackTrace(ps);
                ps.close();
                response.getOutputStream().close();
            } catch (Exception ex) {
            }
        }
    }

    public static String getStackTrace(Throwable t) {
        String stackTrace = null;
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sw.close();
            stackTrace = sw.getBuffer().toString();
        } catch (Exception ex) {
        }
        return stackTrace;
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }

    /**
     * This request wrapper class extends the support class HttpServletRequestWrapper,
     * which implements all the methods in the HttpServletRequest interface, as
     * delegations to the wrapped request. 
     * You only need to override the methods that you need to change.
     * You can get access to the wrapped request using the method getRequest()
     */
    class RequestWrapper extends HttpServletRequestWrapper {

        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }
        // You might, for example, wish to add a setParameter() method. To do this
        // you must also override the getParameter, getParameterValues, getParameterMap,
        // and getParameterNames methods.
//        protected Hashtable localParams = null;
//        public void setParameter(String name, String[] values) {
//            if (debug) {
//                System.out.println("SessionIdFilter::setParameter(" + name + "=" + values + ")" + " localParams = " + localParams);
//            }
//
//            if (localParams == null) {
//                localParams = new Hashtable();
//                // Copy the parameters from the underlying request.
//                Map wrappedParams = getRequest().getParameterMap();
//                Set keySet = wrappedParams.keySet();
//                for (Iterator it = keySet.iterator(); it.hasNext();) {
//                    Object key = it.next();
//                    Object value = wrappedParams.get(key);
//                    localParams.put(key, value);
//                }
//            }
//            localParams.put(name, values);
//        }
//
//        @Override
//        public String getParameter(String name) {
//            if (debug) {
//                System.out.println("SessionIdFilter::getParameter(" + name + ") localParams = " + localParams);
//            }
//            if (localParams == null) {
//                return getRequest().getParameter(name);
//            }
//            Object val = localParams.get(name);
//            if (val instanceof String) {
//                return (String) val;
//            }
//            if (val instanceof String[]) {
//                String[] values = (String[]) val;
//                return values[0];
//            }
//            return (val == null ? null : val.toString());
//        }
//
//        @Override
//        public String[] getParameterValues(String name) {
//            if (debug) {
//                System.out.println("SessionIdFilter::getParameterValues(" + name + ") localParams = " + localParams);
//            }
//            if (localParams == null) {
//                return getRequest().getParameterValues(name);
//            }
//            return (String[]) localParams.get(name);
//        }
//
//        @Override
//        public Enumeration getParameterNames() {
//            if (debug) {
//                System.out.println("SessionIdFilter::getParameterNames() localParams = " + localParams);
//            }
//            if (localParams == null) {
//                return getRequest().getParameterNames();
//            }
//            return localParams.keys();
//        }
//
//        @Override
//        public Map getParameterMap() {
//            if (debug) {
//                System.out.println("SessionIdFilter::getParameterMap() localParams = " + localParams);
//            }
//            if (localParams == null) {
//                return getRequest().getParameterMap();
//            }
//            return localParams;
//        }
    }

    /**
     * This response wrapper class extends the support class HttpServletResponseWrapper,
     * which implements all the methods in the HttpServletResponse interface, as
     * delegations to the wrapped response. 
     * You only need to override the methods that you need to change.
     * You can get access to the wrapped response using the method getResponse()
     */
    class ResponseWrapper extends HttpServletResponseWrapper {

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }
        // You might, for example, wish to know what cookies were set on the response
        // as it went throught the filter chain. Since HttpServletRequest doesn't
        // have a get cookies method, we will need to store them locally as they
        // are being set.
	/*
        protected Vector cookies = null;
        
        // Create a new method that doesn't exist in HttpServletResponse
        public Enumeration getCookies() {
        if (cookies == null)
        cookies = new Vector();
        return cookies.elements();
        }
        
        // Override this method from HttpServletResponse to keep track
        // of cookies locally as well as in the wrapped response.
        public void addCookie (Cookie cookie) {
        if (cookies == null)
        cookies = new Vector();
        cookies.add(cookie);
        ((HttpServletResponse)getResponse()).addCookie(cookie);
        }
         */
    }
}
