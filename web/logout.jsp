<%-- 
    Document   : logout
    Created on : 2011-jun-15, 14:32:18
    Author     : henrik
--%>

<%

    session.invalidate();

    final Cookie sessionCookie = new Cookie("CHANNELID", "-1");
    sessionCookie.setMaxAge(0);
    sessionCookie.setSecure(false);
    sessionCookie.setPath(request.getContextPath());
    response.addCookie(sessionCookie);
    
    response.sendRedirect("default.jsp");

%>
