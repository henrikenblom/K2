<!DOCTYPE html>

<%@page import="com.imprima.kesession.UserSession"%>
<%@page import="com.imprima.kesession.UserSessionController"%>

<%

    UserSession userSession = UserSessionController.getInstance().getUserSession(request.getSession().getId());

    String path = request.getScheme() + "://" + request.getServerName() + ":" + Integer.toString(request.getServerPort()) + request.getContextPath() + "/";

%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<html>
    <head>
        <meta charset="utf-8">
        <script type="text/javascript">            
            var fullname = '<%= userSession.get("fullname")%>';
            var username = '<%= userSession.getUsername()%>';
        </script>

        <link rel="stylesheet" type="text/css" href="css/imprima-theme/jquery-ui-1.8.16.custom.css">
        <link rel="stylesheet" type="text/css" href="css/basics.css">
        <script type="text/javascript" src="js/org/cometd.js"></script>
        <script type="text/javascript" src="js/org/cometd/ReloadExtension.js"></script>
        <script type="text/javascript" src="js/json2.js"></script>
        <script type="text/javascript" src="js/jquery/jquery-1.6.4.min.js"></script>
        <script type="text/javascript" src="js/jquery/jquery-ui-1.8.16.custom.min.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.dateFormat-1.0.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.cometd.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.cookie.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.cometd-reload.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.tinysort.min.js"></script>
        <script type="text/javascript" src="js/jquery/jquery.scrollTo-min.js"></script>
        <script type="text/javascript" src="js/jquery/fileupload/jquery.fileupload.js"></script>
        <script type="text/javascript" src="js/k2.js"></script>
        <script type="text/javascript" src="js/messagebar.js"></script>
        <script type="text/javascript" src="js/settingsmenus.js"></script>

        <script type="text/javascript">
            
            var fullname = '<%= userSession.get("fullname")%>';
            var username = '<%= userSession.getUsername()%>';
                                                                        
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
                
                $('#sort-order-button').button({
                    icons: {
                        secondary: "ui-icon-triangle-1-s"
                    }
                });
                
                $('#sort-order-button').change(function() {
                    
                    if ($('#sort-order-button').is(':checked')) {
                        
                        showSortingMenu();
                                            
                    } else {

                        hideSortingMenu();
            
                    }
                    
                });
                
                $.getJSON('OrderServlet', {action:'get_orders_by_username', username:'<%= userSession.getUsername()%>'}, function(data) {

                    $.each(data, function(i) {
            
                        addOrder(data[i]);
                     
                    });
                    
                    doOrderListSort();
                    
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
                    
                } else {
                    
                    name = $.cookie("name");
                    email = $.cookie("email");
                    
                }
                
                $("#orderview-info-button").button( { icons: {primary:'ui-icon-document'} } );
                $("#orderview-planning-button").button( { icons: {primary:'ui-icon-calendar'} } );
                $("#orderview-webproof-button").button( { icons: {primary:'ui-icon-image'} } );
                                                
            });
                      
        </script>
        <title>K2 - <%= userSession.get("fullname")%></title>
    </head>
    <body onresize="adjustViewPort()">
        <div id="order-controls" class="ui-widget">
            <input type="checkbox" id="sort-order-button"><label for="sort-order-button"></label>
            <ul id="sorting-menu" class="ui-menu ui-widget ui-widget-content ui-corner-all" role="menu" style="display:none">
                <li class="ui-menu-item ui-corner-all" role="menuitem">
                    <input class="sorting-choice ui-helper-hidden-accessible" type="radio" id="ordernumber-sort-button" name="sort" value="ordernumber" checked="checked"/>
                    <label for="ordernumber-sort-button" class="ui-button ui-widget ui-button-text-icon-primary" aria-disabled="false">
                        <span id="ordernumber-sort-button-icon" class="ui-button-icon-primary ui-icon ui-icon-check"></span>
                        <span class="ui-button-text">Ordernummer</span>
                    </label>
                </li>
                <li class="ui-menu-item ui-corner-all" role="menuitem">
                    <input class="sorting-choice ui-helper-hidden-accessible" type="radio" id="ordername-sort-button" name="sort" value="ordername"/>
                    <label for="ordername-sort-button" class="ui-button ui-widget ui-button-text-icon-primary" aria-disabled="false">
                        <span id="ordername-sort-button-icon" class="ui-button-icon-primary ui-icon-check"></span>
                        <span class="ui-button-text">Ordernamn</span>
                    </label>
                </li>
                <li class="ui-menu-item ui-corner-all" role="menuitem">
                    <input class="sorting-choice ui-helper-hidden-accessible" type="radio" id="timestamp-sort-button" name="sort" value="timestamp"/>
                    <label for="timestamp-sort-button" class="ui-button ui-widget ui-button-text-icon-primary" aria-disabled="false">
                        <span id="timestamp-sort-button-icon" class="ui-button-icon-primary ui-icon-check"></span>
                        <span class="ui-button-text">Senast uppdaterad</span>
                    </label>
                </li>
                <li class="ui-menu-item menu-divider" role="menuitem">
                </li>
                <li class="ui-menu-item ui-corner-all" role="menuitem">
                    <input class="sorting-choice ui-helper-hidden-accessible" type="radio" id="descending-order-button" name="order" value="desc" checked="checked"/>
                    <label for="descending-order-button" class="ui-button ui-widget ui-button-text-icon-primary" aria-disabled="false">
                        <span id="desc-order-button-icon" class="ui-button-icon-primary ui-icon ui-icon-check"></span>
                        <span class="ui-button-text">Fallande</span>
                    </label>
                </li>
                <li class="ui-menu-item ui-corner-all" role="menuitem">
                    <input class="sorting-choice ui-helper-hidden-accessible" type="radio" id="ascending-order-button" name="order" value="asc"/>
                    <label for="ascending-order-button" class="ui-button ui-widget ui-button-text-icon-primary" aria-disabled="false">
                        <span id="asc-order-button-icon" class="ui-button-icon-primary ui-icon-check"></span>
                        <span class="ui-button-text">Stigande</span>
                    </label>
                </li>
            </ul>
            <div class="buttonset" id="order-view-menu" style="display: none">
                <input type="radio" name="orderview" id="orderview-info-button" checked="checked"/><label for="orderview-info-button">Information</label>
                <input type="radio" name="orderview" id="orderview-planning-button"/><label for="orderview-planning-button">Planering</label>
                <input type="radio" name="orderview" id="orderview-webproof-button"/><label for="orderview-webproof-button">Webbkorrektur</label>
            </div>
        </div>
        <div id="menu-layer">
            <div id="settings-menus">
                <input type="checkbox" id="user-menu-button"><label for="user-menu-button"><%= userSession.get("fullname")%></label>
            </div>
            <ul id="user-menu" class="ui-menu ui-widget ui-widget-content ui-corner-all" role="menu" style="display: none;">
                <li class="ui-menu-item ui-corner-all" role="menuitem"><a href="#" onclick="showUserSettingsAction()" class="ui-corner-all" tabindex="-1">Namn och e-postadress</a></li>
                <li class="ui-menu-item ui-corner-all" role="menuitem"><a href="#" onclick="logoutAction()" class="ui-corner-all" tabindex="-1">Logga ut</a></li>
            </ul>
        </div>
        <div id="order-list-layer" class="ui-widget" ondragenter="return false" ondragover="return false" ondrop="return false">
            <div id="order-list" class="ui-widget">
            </div>
        </div>
        <div id="dropzone-info" style="width: 500px;" ></div>
        <div id="content-layer" ondragenter="return false" ondragover="return false" ondrop="return false">
        </div>
        <div id="notice-hover-detection-layer" onmouseover="handleMouseOverNoticeHoverDetectionLayer()" ondragenter="return false" ondragover="return false" ondrop="return false">
        </div>
        <div id="notice-layer" class="ui-widget" onmouseover="handleMouseOverNoticeHoverDetectionLayer()" onmouseout="handleMouseOutNoticeHoverDetectionLayer()" ondragenter="return false" ondragover="return false" ondrop="return false" style="display: none">
        </div>
        <div id="dialog-layer" ondragenter="return false" ondragover="return false" ondrop="return false">
        </div>
    </body>
</html>