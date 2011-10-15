<%-- 
    Document   : newjsp
    Created on : 2011-okt-15, 00:32:47
    Author     : henrik
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="UploadServlet" enctype="multipart/form-data" method="post">
            <input type="file" name="datafile">
            <input type="submit">
        </form>
    </body>
</html>
