<%-- 
    Document   : default
    Created on : 2011-jul-14, 14:04:19
    Author     : henrik
--%>

<!DOCTYPE html>
<%@include file="WEB-INF/jspf/sessionConstants.jspf" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<html>
    <head>
        <link rel="stylesheet" type="text/css" href="css/imprima-theme/jquery-ui-1.8.16.custom.css">
        <link rel="stylesheet" type="text/css" href="css/basics.css">
        <script type="text/javascript" src="js/org/cometd.js"></script>
        <script type="text/javascript" src="js/org/cometd/ReloadExtension.js"></script>
        <script type="text/javascript" src="js/json2.js"></script>
        <script type="text/javascript" src="js/jquery/jquery-1.6.2.min.js"></script>
        <script type="text/javascript" src="js/jquery/jquery-ui-1.8.16.custom.min.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.dateFormat-1.0.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.cometd.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.cookie.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.cometd-reload.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.tinysort.min.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.scrollTo-min.js"></script>
        <script type="text/javascript" src="js/k2.js"></script>
        <script type="text/javascript" src="js/messagebar.js"></script>

        <script type="text/javascript">
                        
            function handleDragEnter(event) {

                $('#' + event.srcElement.id).addClass("drag-enter");
                
                event.stopPropagation();
                event.preventDefault();
                
                return false;
            
            }
            
            function handleDragLeave(event) {
            
                $('#' + event.srcElement.id).removeClass("drag-enter");
                
                event.stopPropagation();
                event.preventDefault();
                
                return false;
            
            }
            
            function handleDrop(event) {
                
                var files = event.dataTransfer.files;
                
                $('#' + event.srcElement.id).removeClass("drag-enter");
                
                for (var i = 0, f; f = files[i]; i++) {
                    
                    alert(f.name);
                    
                }
                
                event.stopPropagation();
                event.preventDefault();
                
                return true;
                
            }
                        
            function updateTips( t ) {
                $('.validateTips')
                .text( t )
                .addClass( "ui-state-highlight" );
                setTimeout(function() {
                    $('.validateTips').removeClass( "ui-state-highlight", effectDurationDenominator * 8 );
                }, effectDurationDenominator * 2 );
            }

            function checkLength( o, n, min, max ) {
                if ( o.val().length > max || o.val().length < min ) {
                    o.addClass( "ui-state-error" );
                    updateTips( n + " måste bestå av minst " +
                        min + " och max " + max + " tecken." );
                    o.focus();
                    return false;
                } else {
                    return true;
                }
            }

            function checkRegexp( o, regexp, n ) {
                if ( !( regexp.test( o.val() ) ) ) {
                    o.addClass( "ui-state-error" );
                    updateTips( n );
                    return false;
                } else {
                    return true;
                }
            }
            
            function logoutAction() {
       
                clearMessageBar();
       
                document.location = 'logout.jsp';
        
            }
                                    
            $(window).unload(function() {
                
                cometd.reload();
                
            });
            
            $(document).ready(function() {
                                
                initMessageBar();
                
                $('li').mouseover(function() {
                    $(this).addClass('ui-state-hover');
                }).mouseout(function() {
                    $(this).removeClass('ui-state-hover');
                });
                                                                
                cometd.init({
                    url: '<%= path%>L9'
                });
                                
                cometd.subscribe("/" + $.cookie("CHANNELID"), function(bayeuxMessage) {
                    
                    handleLevel9Message(bayeuxMessage);
                    
                });
                                
                $('.buttonset').buttonset();
                $('#reporting').button();
                
                $.getJSON('OrderServlet', {action:'get_orders_by_username', username:'<%= userSession.getUsername()%>'}, function(data) {

                    $.each(data, function(i) {
            
                        addOrder(data[i]);
                     
                    });
                    
                    $('input.sorting-choice').change(function() {
                
                        doOrderListSort();
                
                    });
                   
                });
                
                adjustViewPort();
                
                $(document).bind('keydown keypress', function (event) {

                    handleKeyEvent(event);     
                    
                });
                
                if ($.cookie("name") == null || $.cookie("email") == null) {
                    
                    showUserSettingsDialog();
                    
                }
                                
            });
                      
        </script>
        <title></title>
    </head>
    <body onresize="adjustViewPort()">
        <div id="orderListControls" class="ui-widget">
            <div class="buttonset">
                <input class="sorting-choice" type="radio" id="ordernumber-sort-button" name="sort" value="ordernumber" checked="checked"/><label for="ordernumber-sort-button">Sortera efter ordernummer</label>
                <input class="sorting-choice" type="radio" id="ordername-sort-button" name="sort" value="ordername" /><label for="ordername-sort-button">Sortera efter ordernamn</label>
                <input class="sorting-choice" type="radio" id="timestamp-sort-button" name="sort" value="timestamp"/><label for="timestamp-sort-button">Sortera efter tidpunkt</label>
            </div>
            <div class="buttonset">
                <input class="sorting-choice" type="radio" id="descending-order-button" name="order" value="desc"/><label for="descending-order-button">Fallande</label>
                <input class="sorting-choice" type="radio" id="ascending-order-button" name="order" value="asc" checked="checked"/><label for="ascending-order-button">Stigande</label>
            </div>
        </div>
        <div id="menuLayer">
            <%@include file="WEB-INF/jspf/settingsMenus.jspf" %>
        </div>
        <div id="orderListLayer" class="ui-widget" ondragenter="return false" ondragover="return false" ondrop="return false">
            <div id="orderList" class="ui-widget">
            </div>
        </div>
        <div id="contentLayer" ondragenter="return false" ondragover="return false" ondrop="return false">
        </div>
        <div id="noticeHoverDetectionLayer" onmouseover="handleMouseOverNoticeHoverDetectionLayer()" ondragenter="return false" ondragover="return false" ondrop="return false">
        </div>
        <div id="noticeLayer" class="ui-widget" onmouseover="handleMouseOverNoticeHoverDetectionLayer()" onmouseout="handleMouseOutNoticeHoverDetectionLayer()" ondragenter="return false" ondragover="return false" ondrop="return false" style="display: none">
        </div>
        <div id="dialogLayer" ondragenter="return false" ondragover="return false" ondrop="return false">
        </div>
    </body>
</html>