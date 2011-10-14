
var noticeLayerDisplayTimer = null;
var projectTrayArray = new Array();
var trayWidth = 483;
var effectDurationDenominator = 200;
var noticeDuration = 5000;
var noticeLayerMouseOver = false;
var name,email;
var cometd = $.cometd;
var sortingcaption = {"ordernumber":"ordernummer","ordername":"ordernamn", "timestamp":"'senast uppdaterad'"};

function handleKeyEvent(event) {

    switch (event.keyCode) {
                        
        case 38:
            showOrderDetails($('.order-list-entry.ui-state-active').prev().children('.ordernumber').html());
            break;
                        
        case 40:
            showOrderDetails($('.order-list-entry.ui-state-active').next().children('.ordernumber').html());
            break;
                        
    }
                    
}

function showSortingMenu() {
    
    $('#sorting-menu').fadeIn(effectDurationDenominator, function() {

        $(document).bind('click', function(event) {
            if ($(event.target) != $('#sorting-menu')) {
                hideSortingMenu();
            }
        });
                          
    });
    
}

function hideSortingMenu() {
    
    $('#sorting-menu').fadeOut(effectDurationDenominator, function() {
        
        $(document).unbind('click');

        $('#sort-order-button').attr('checked', false);
        $('#sort-order-button').button('refresh');
        
    });
    
}

function doOrderListSort() {

    $('div#orderList>.order-list-entry').tsort("div." + $('input[name$="sort"]:checked').val(),
    {
        order:$('input[name$="order"]:checked').val()
    }
    );
                
    $('#order-controls ul li label span').removeClass('ui-icon'); 
               
    $('#' + $('input[name$="sort"]:checked').val() + '-sort-button-icon').addClass('ui-icon');
    $('#' + $('input[name$="order"]:checked').val() + '-order-button-icon').addClass('ui-icon');

    hideSortingMenu();
    
    $('#sort-order-button').button('option', 'label', 'Sortera efter ' + sortingcaption[$('input[name$="sort"]:checked').val()]);
    //$('label[for$="sort-order-button"]').html('Sortera efter ' + sortingcaption[$('input[name$="sort"]:checked').val()]);
                    
}

function adjustViewPort() {

    $('#orderListLayer').height($(window).height() - $('#menyLayer').height() - $('#orderListControls').height() - 18); 

    $('#contentLayer').css('left', $('#orderListLayer').width());
    $('#contentLayer').width($(window).width() - $('#orderListLayer').width());

    $('.order-details-entry').height($('#contentLayer').height() - 136);

}

function showOrderDetails(ordernumber) {
    
    if (ordernumber != null && ordernumber.length > 0) {
    
        $('.order-details-entry').fadeOut(effectDurationDenominator);
    
        $('.order-list-entry').removeClass('ui-state-active');
    
        $('#order-list-entry-' + ordernumber).addClass('ui-state-active');
        
        if (!isScrolledIntoView($('#order-list-entry-' + ordernumber))) {
                        
            $('#orderListLayer').scrollTo($('#order-list-entry-' + ordernumber), effectDurationDenominator);
        
        }
        
        $.getJSON('OrderServlet', {
        
            action:'get_order_by_ordernumber', 
            ordernumber: ordernumber
        
        }, function(orderData) {
        
            var orderdetails = $('<div>');
            orderdetails.attr("style", "display: none");
            orderdetails.addClass('order-details-entry');
            orderdetails.addClass('ui-widget');
            orderdetails.attr('id', 'order-details-entry-' + orderData.ordernumber);
        
            var orderdetailsHeader = $('<div>').addClass('header');
        
            var ordernumber = $('<h1>');
            ordernumber.html(orderData.ordernumber);
            ordernumber.addClass('ordernumber');
        
            var ordername = $('<h1>');
            ordername.html(orderData.name);
            ordername.addClass('ordername');
        
            orderdetailsHeader.append(ordernumber)
            .append(ordername);
        
            var updated = $('<div>');
            var labelUpdated = $('<label>').html('Senast uppdaterad');
        
            updated.append(labelUpdated).append(orderData.updated);
        
            orderdetails.append(orderdetailsHeader)
            .append(updated);
        
            $('#contentLayer').html(orderdetails);
            $('#contentLayer').click(function() {
            
                orderdetails.fadeOut(effectDurationDenominator, function() {
                
                    orderdetails.remove();
                    $('.order-list-entry').removeClass('ui-state-active');
                    
                    $('#order-view-menu').fadeOut(effectDurationDenominator);
                
                });
            
            });
        
            orderdetails.click(function() {
            
                return false;
            
            });
            
            $('#order-view-menu').fadeIn(effectDurationDenominator);
        
            adjustViewPort();
        
            orderdetails.fadeIn(effectDurationDenominator);
        
        });
    
    }
        
}

function generateOrderListEntry(orderData) {

    var frameDiv = $('<div>');
    frameDiv.attr("style", "display: none");
    frameDiv.addClass('order-list-entry');
    frameDiv.attr('data-id', 'id-' + orderData.ordernumber);
    frameDiv.attr('id', 'order-list-entry-' + orderData.ordernumber);
    
    var timestamp = $('<div>');
    timestamp.html(orderData.timestamp);
    timestamp.addClass('timestamp');
    
    var ordernumber = $('<div>');
    ordernumber.html(orderData.ordernumber);
    ordernumber.addClass('ordernumber');
                
    var ordername = $('<div>');
    ordername.html(orderData.name);
    ordername.addClass('ordername');
    
    var updated = $('<div>');
    updated.html(orderData.updated);
    updated.addClass('updated');
    
    var sales_fullname_label = $('<label>');
    sales_fullname_label.html('Säljare:');
    
    var sales_fullname = $('<div>');
    sales_fullname.html(orderData.sales_fullname);
    ordername.addClass('sales_fullname');
    
    var projectmanager_fullname_label = $('<label>');
    projectmanager_fullname_label.html('Projektledare:');
    
    var projectmanager_fullname = $('<div>');
    projectmanager_fullname.html(orderData.projectmanager_fullname);
    projectmanager_fullname.addClass('projectmanager_fullname');
    
    var client_fullname_label = $('<label>');
    client_fullname_label.html('Kund:');
    
    var client_fullname = $('<div>');
    client_fullname.html(orderData.client_fullname);
    client_fullname.addClass('client_fullname');
    
    frameDiv.append(timestamp)
    .append(ordernumber)
    .append(ordername)
    .append(updated)
    .append('<br>')
    .append('<br>')
    .append(client_fullname_label)
    .append(client_fullname)
    .append('<br>')
    .append(sales_fullname_label)
    .append(sales_fullname)
    .append('<br>')
    .append(projectmanager_fullname_label)
    .append(projectmanager_fullname);

    frameDiv.click(function() {
        
        showOrderDetails(orderData.ordernumber);
        
    });
    
    return frameDiv;
                
}

function handleLevel9Message(bayeuxMessage) {
    
    var message = $.parseJSON(bayeuxMessage.data);

    if (message.type == 'usermessage') {
        
        showTextMessage(message.body, message.time);
        
    } else if (message.type == 'orderupdatemessage') {
        
        updateOrderdata(message.body);
            
    } else if (message.type == 'orderremovalmessage') {
        
        removeOrder(message.body);
            
    } else if (message.type == 'addordermessage') {
        
        addOrder(message.body);
        doOrderListSort();
        
    }
    
}

function addOrder(orderData) {

    $('#orderList').append(generateOrderListEntry(orderData));
        
    $('#order-list-entry-' + orderData.ordernumber).fadeIn(effectDurationDenominator * 2, function() {
        
        adjustViewPort();
        
    });

}

function removeOrder(ordernumber) {
        
    $('#order-list-entry-' + ordernumber).fadeOut(effectDurationDenominator, function() {
        
        $('#order-list-entry-' + ordernumber).remove();
        adjustViewPort();
        
    });
    
}

function updateOrderdata(orderData) {
        
    $.each(orderData, function(key, value) {
                
        $('#order-list-entry-' + orderData.ordernumber + ' .order' + key).fadeOut(effectDurationDenominator, function() {
            
            $('#order-list-entry-' + orderData.ordernumber + ' .order' + key).html(value);
            $('#order-list-entry-' + orderData.ordernumber + ' .order' + key).addClass("ui-state-highlight");
            $('#order-list-entry-' + orderData.ordernumber + ' .order' + key).fadeIn(effectDurationDenominator, function() {
                
                adjustViewPort();
                
                setTimeout(function() {
                    $('#order-list-entry-' + orderData.ordernumber + ' .order' + key).removeClass( "ui-state-highlight", effectDurationDenominator * 6 );
                }, effectDurationDenominator * 4);
                
            });
            
        });
        
      
    });
    
}

function isScrolledIntoView(elem) {
    
    var docViewTop = $(window).scrollTop();
    var docViewBottom = docViewTop + $(window).height();

    var elemTop = $(elem).offset().top;

    var elemBottom = elemTop + 2;
    
    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
    
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

function addAtom(elementname, filename) {
    
    $.ajax({
        async: false,
        url: 'atoms/' + filename,
        context: document.body,
        success: function(data){
            $('#' + elementname).append(data);
        }
    });
    
}

function showUserSettingsDialog() {
    
    if (!$('#userSettingsDialog').length) {
        
        addAtom('dialogLayer', 'usersettingsdialog.html');
        
    }
                
    if ($.cookie("name") != null || $.cookie("email") != null) {
                    
        $('#userSettingsDialogNoCookie').hide();
        $('#userSettingsDialogChangeData').show();

        name = $.cookie("name");
        email = $.cookie("email");
                    
        $('#name').val(name);
        $('#email').val(email);

    }
                
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
                                
                    $.cookie("name", $('#name').val(), {
                        path: '/', 
                        expires: 3650
                    });
                    $.cookie("email", $('#email').val(), {
                        path: '/', 
                        expires: 3650
                    });

                    name = $('#name').val();
                    email = $('#email').val();

                    $( this ).dialog( "close" );
                                                    
                }
            }
        }
    });
    
    $('#userSettingsDialog').dialog('open');
    $('#name').focus();
    
}