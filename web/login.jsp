<%-- 
    Document   : login
    Created on : 2011-jun-15, 15:43:43
    Author     : henrik
--%>

<%@page import="java.util.Enumeration"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login</title>
        <script type="text/javascript">
            
            $(document).ready(function() {
                
                $('#username').focus();
                
            });
            
        </script>
    </head>
    <body>
        <form method="POST" action="j_security_check">
            <input type="text" name="j_username" id="username">
            <input type="password" name="j_password" id="password">
            <input type="submit" value="Logga in">
        </form>
        <div id="cookiejar">
            <% if (request.getParameter("failed") != null) {
                
              %>
              
                <%= "Fel användarnamn/lösenord" %>
              
              <%
                
            } %>
        </div>
    </body>
</html>
