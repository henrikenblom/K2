<%-- 
    Document   : tester
    Created on : 2011-jun-28, 13:49:23
    Author     : henrik
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Tester</title>
        <script type="text/javascript" src="js/jquery/jquery-1.6.2.min.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.form.js"></script>
        <script type="text/javascript"> 

        $(document).ready(function() { 

            $('#messageForm').ajaxForm(function() { 
            });
            
            $('#userMessageForm').ajaxForm(function() { 
            });
            
        }); 
    </script> 
    </head>
    <body>
        <form id="messageForm" action="CometdTester" method="POST">
            <input type="text" name="message">
            <input type="submit" value="skicka" />
        </form>
        
        <form id="userMessageForm" action="CometdTester" method="POST">
            <input type="text" name="userMessage">
            <input type="submit" value="skicka" />
        </form>
        
    </body>
</html>
