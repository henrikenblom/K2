<%-- 
    Document   : login
    Created on : 2011-jun-15, 15:43:43
    Author     : henrik
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    boolean loginFailed = false;

    if (request.getParameter("failed") != null) {

        loginFailed = true;

    }
%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login</title>
        <link rel="stylesheet" type="text/css" href="css/imprima-theme/jquery-ui-1.8.16.custom.css">
        <link rel="stylesheet" type="text/css" href="css/basics.css">
        <script type="text/javascript" src="js/jquery/jquery-1.7.1.min.js"></script>
        <script type="text/javascript" src="js/jquery/jquery-ui-1.8.16.custom.min.js"></script>
        <script type="text/javascript">
            
            var adjustViewPort = function() {
                
                $('#login-dialog').position({
                    of: $('#content-layer'),
                    my: 'center center',
                    at: 'center center',
                    offset: '0 -100'
                });
                
            }
           
            $(document).ready(function() {
                
                localStorage.removeItem('messagebar');
                
                var reloadTimeout = <%= (request.getSession().getMaxInactiveInterval() - 60) * 1000%>;
                
                setTimeout("location.reload(true);", reloadTimeout);
                
                $(this).keypress(function(e) {
                    
                    if (e.keyCode == 13) {
                        
                        if ($('#username').val().length > 0
                            && $('#password').val().length > 0) {
                            
                            $('#login-form').trigger('submit');
                            
                        }
                        
                    }
                    
                });                
                
                $(window).resize(adjustViewPort);
                
                adjustViewPort();
                
                $('#username').focus();
                
            });
                        
        </script>
    </head>
    <body>
        <div id="content-layer">
            <div id="login-dialog" class="ui-widget ui-widget-content ui-corner-all">
                <h1>Logga in</h1>
                <form method="POST" action="j_security_check" id="login-form">
                    <fieldset>
                        <label for="username">Användarnamn:</label>
                        <input type="text" name="j_username" id="username">
                        &nbsp;&nbsp;&nbsp;
                        <label for="password">Lösenord:</label>
                        <input type="password" name="j_password" id="password">
                    </fieldset>
                </form>
                <% if (loginFailed) {%>

                <span id="login-error">Fel användarnamn/lösenord</span>

                <%} else {%>

                &nbsp;

                <%}%>
            </div>
        </div>
    </body>
</html>
