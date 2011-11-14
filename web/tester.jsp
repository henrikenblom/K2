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
        <link rel="stylesheet" type="text/css" href="css/imprima-theme/jquery-ui-1.8.16.custom.css">
        <link rel="stylesheet" type="text/css" href="css/basics.css">
        <link rel="stylesheet" type="text/css" href="css/hegantt.css">

        <script type="text/javascript" src="js/jquery/jquery-1.6.4.min.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.hegantt.js"></script>
        <script type="text/javascript" src="js/k2.js"></script>
        <script type="text/javascript"> 

            $(document).ready(function() { 

                $('#chart').hegantt({
                    source: 'servlet/productionplan?action=get_productionplan_by_ordernumber&ordernumber=272951',
                    callback: adjustViewPort
                });
    
            }); 
        </script> 
    </head>
    <body>

        <div id="content-layer" style="left: 320px; width: 1185px;">
            <div class="order-details-entry ui-widget" id="order-details-entry">
                <div style="height: 576px">
                    <div id="chart"></div>
                </div>
            </div>
        </div>


    </body>
</html>
