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
                
                
                
                $('#userSettingsDialog').dialog({
                    closeOnEscape: false,
                    open: function(e, ui) {
                        
                        $(".ui-dialog-titlebar-close").hide();
                        
                        $(this).keypress(function(e) {
                            if (e.keyCode == 13) {
                                $('.ui-dialog-buttonpane button:last').trigger('click');
                            }
                        });
                        
                    },
                    close: function(e, ui) {
                        
                        $('#userSettingsDialogNoCookie').hide();
                        $('#userSettingsDialogChangeData').show();
                        
                    },
                    modal: true,
                    resizable: false,
                    show: "fade",
                    hide: "fade",
                    autoOpen: false,
                    width: 460,
                    buttons: {
                        "Ok": function() {
                            
                            var bValid = true;
                            $('#name').removeClass("ui-state-error");
                            $('#email').removeClass("ui-state-error");

                            bValid = bValid && checkLength( $('#name'), "Namnet", 2, 80 );
                            bValid = bValid && checkLength( $('#email'), "E-postadressen", 6, 80 );

                            // From jquery.validate.js (by joern), contributed by Scott Gonzalez: http://projects.scottsplayground.com/email_address_validation/
                            bValid = bValid && checkRegexp(  $('#email'), /^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i, "E-postadressen är ogiltig." );

                            if ( bValid ) {
                                
                                $.cookie("name", $('#name').val(), { path: '/', expires: 3650 });
                                $.cookie("email", $('#email').val(), { path: '/', expires: 3650 });

                                name = $('#name').val();
                                email = $('#email').val();

                                $( this ).dialog( "close" );
                                                    
                            }
                        }
                    }
                });
                                
                cometd.init({
                    url: '<%= path%>L9'
                });
                                
                cometd.subscribe("/" + $.cookie("CHANNELID"), function(bayeuxMessage) {
                    
                    handleLevel9Message(bayeuxMessage);
                    
                });
                
                if ($.cookie("name") == null || $.cookie("email") == null) {
                    
                    $('#userSettingsDialog').dialog('open');
                    $('#name').focus();
                    
                } else {
                    
                    $('#userSettingsDialogNoCookie').hide();
                    $('#userSettingsDialogChangeData').show();

                    name = $.cookie("name");
                    email = $.cookie("email");
                    
                    $('#name').val(name);
                    $('#email').val(email);

                }
                
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
        <%@include file="WEB-INF/jspf/messageBar.jspf"%>
        <div id="dialogLayer" ondragenter="return false" ondragover="return false" ondrop="return false">
            <div id="userSettingsDialog" title="Namn och e-postadress">
                <div id="userSettingsDialogNoCookie">
                    <h3>Hej du!</h3>
                    <p>
                        Vi behöver få veta ditt namn och din e-postadress för att kunna skicka bekräftelsemail och direktlänkar till olika funktioner här i K2. Informationen lagras i cookies i din webbläsare. Hoppas du inte har något emot det.
                    </p>
                </div>
                <div id="userSettingsDialogChangeData" style="display: none;">
                    <h3>Ändra uppgifter</h3>
                    <p>
                        Fyll i de nya uppgifterna nedan. Informationen lagras i cookies i din webläsare.
                    </p>
                </div>
                <p class="validateTips">&nbsp;</p>
                <form>
                    <fieldset>
                        <label for="name">Namn:</label>
                        <input type="text" name="name" id="name" class="text ui-widget-content ui-corner-all" />
                        &nbsp;&nbsp;&nbsp;
                        <label for="email">E-postadress:</label>
                        <input type="text" name="email" id="email" value="" class="text ui-widget-content ui-corner-all" />
                    </fieldset>
                </form>
            </div>
        </div>
    </body>
</html>