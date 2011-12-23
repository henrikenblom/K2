var noticeLayerDisplayTimer = null;
var projectTrayArray = new Array();
var trayWidth = 483;
var effectDurationDenominator = 200;
var noticeDuration = 5000;
var noticeLayerMouseOver = false;
var cometd = $.cometd;
var sortingcaption = {
    "ordernumber":"ordernummer",
    "name":"ordernamn", 
    "timestamp":"aktualitet"
};

var setName = function(name) {

    localStorage.setItem(username + "_name", name);
    
}

var setEmail = function(email) {
    
    localStorage.setItem(username + "_email", email);
    
}

var getName = function() {
    
    return localStorage.getItem(username + "_name");
    
}

var getEmail = function() {
    
    return localStorage.getItem(username + "_email");
    
}

var handleKeyEvent = function(event) {

    switch (event.keyCode) {
                        
        case 38:
            showOrderDetails($('.order-list-entry.ui-state-active').prev().children('._ordernumber').html());
            break;
                        
        case 40:
            showOrderDetails($('.order-list-entry.ui-state-active').next().children('._ordernumber').html());
            break;
                        
    }
                    
}

var showSortingMenu = function() {
    
    $('#sorting-menu').fadeIn(effectDurationDenominator, function() {

        $(document).bind('click', function(event) {
            if ($(event.target) != $('#sorting-menu')) {
                hideSortingMenu();
            }
        });
                          
    });
    
}

var hideSortingMenu = function() {
    
    $('#sorting-menu').fadeOut(effectDurationDenominator, function() {
        
        $(document).unbind('click');

        $('#sort-order-button').attr('checked', false);
        $('#sort-order-button').button('refresh');
        
    });
    
}

var doOrderListSort = function() {

    if ($('#search').val().length > 0) {
        
        $('div#order-list>.order-list-entry').hide();
        $('div#order-list>.order-list-entry:containsi(' + $('#search').val() + ')').show();
    
    } else {
        
        $('div#order-list>.order-list-entry').show();
        
    }
    
    $('div#order-list>.order-list-entry').tsort("div." + $('input[name$="sort"]:checked').val(),
    {
        order:$('input[name$="order"]:checked').val()
    }
    );
                
    $('#order-controls ul li label span').removeClass('ui-icon');
    
    $('#' + $('input[name$="sort"]:checked').val() + '-sort-button-icon').addClass('ui-icon');
    $('#' + $('input[name$="order"]:checked').val() + '-order-button-icon').addClass('ui-icon');

    hideSortingMenu(); 
    
    $('#sort-order-button').button('option', 'label', 'Sortera efter ' + sortingcaption[$('input[name$="sort"]:checked').val()]);
    
    localStorage.setItem(username + '_sort', $('input[name$="sort"]:checked').val());
    localStorage.setItem(username + '_order', $('input[name$="order"]:checked').val());
                    
}

var adjustViewPort = function() {

    $('#order-list-layer').height($(window).height() - $('#menyLayer').height() - $('#order-listControls').height() - 54); 

    $('#content-layer, #order-view-menu').css('left', $('#order-list-layer').width());
    
    $('#content-layer').width($(window).width() - $('#order-list-layer').width());
    
    $('div.hegantt > div.fn-content > div.rightPanel').width($('#content-layer').width() - 310);

}

var showOrderInfo = function(ordernumber, target) {
        
    $.getJSON('servlet/order', {
        
        action:'get_order_by_ordernumber', 
        ordernumber: ordernumber
        
    }, function(orderData) {
            
        var orderdetailsHeader = $('<div>').addClass('header');
        
        var ordername = $('<h1>');
        ordername.append(orderData._ordernumber + ' ' + orderData._name);
        ordername.addClass('ordername');
        
        orderdetailsHeader.append(ordername);
        
        var updated = $('<div>');
        var labelUpdated = $('<label>').html('Senast uppdaterad:');
        
        var orderrows = $('<div id="orderrows">');

        var keys = [];

        $.each(orderData, function(key, value) {
            
            if (key.substring(0, 15) == "orderdataentry_") {
        
                keys.push(key);
                
            }

        });
        
        keys.sort();
        
        keys.forEach(function(key) {
            
            var value = orderData[key];

            var orderrow = $('<div class="orderrow">');
                
            $.each(value.split('\t'), function(index, value) {
                    
                var column = $('<div class="orderrowcell">');
                
                if (index == 0) {
                    
                    column.addClass('orderrowheader');
                    
                }
                
                column.append(value).append('&nbsp;\n\n');
                    
                orderrow.append(column);
                    
            });
                
            orderrows.append(orderrow);
                
            
        });
        
        updated.append(labelUpdated).append(orderData._updated);
                
        target.html(orderdetailsHeader);
        target.append(updated);
        target.append(orderrows);
                          
        if (hasProductionPlan(orderData._ordernumber)) {
            
            var ganttchartheader = $('<h2>');
            ganttchartheader.html('Produktionsplan');
        
            var gantt = $('<div id="gantt-' + orderData._ordernumber + '">');
            gantt.addClass('ganttchart');
        
            gantt.hegantt({
                source: 'servlet/productionplan?action=get_productionplan_by_ordernumber&ordernumber=' + orderData._ordernumber,
                callback: adjustViewPort
            });
            
            target
            .append(ganttchartheader)
            .append(gantt); 
            
        }
                  
        makeFileuploadDropZone(target, orderData._ordernumber);        
                
    });
            
}

var generateOrderListEntry = function(orderData) {

    var orderListEntry = $('<div>');
    orderListEntry.attr("style", "display: none");
    orderListEntry.addClass('order-list-entry');
    orderListEntry.attr('data-id', 'id-' + orderData._ordernumber);
    orderListEntry.attr('id', 'order-list-entry-' + orderData._ordernumber);
    
    var timestamp = $('<div>');
    timestamp.html(orderData._timestamp);
    timestamp.addClass('_timestamp');
    
    var ordernumber = $('<div>');
    ordernumber.html(orderData._ordernumber);
    ordernumber.addClass('_ordernumber');
                
    var name = $('<div>');
        
    name.html(truncateString(orderData._name, 33, 35));
    name.addClass('_name');
    
    orderListEntry.attr('title', orderData._name);
    
    var updated = $('<div>');
    updated.html(orderData._updated);
    updated.addClass('_updated');
    
    var sales_fullname_label = $('<label>');
    sales_fullname_label.html('Säljare:');
    
    var sales_fullname = $('<span>');
    sales_fullname.html(orderData._sales_fullname);
    sales_fullname.addClass('sales_fullname');
    
    var projectmanager_fullname_label = $('<label>');
    projectmanager_fullname_label.html('Projektledare:');
    
    var projectmanager_fullname = $('<span>');
    projectmanager_fullname.html(orderData._projectmanager_fullname);
    projectmanager_fullname.addClass('projectmanager_fullname');
    
    var client_fullname_label = $('<label>');
    client_fullname_label.html('Kund:');
    
    var client_fullname = $('<span>');
    client_fullname.html(orderData._client_fullname);
    client_fullname.addClass('client_fullname');
    
    var uploaded_filename = $('<span>');
    uploaded_filename.attr('id', 'uploaded-filename' + orderData._ordernumber);
    
    orderListEntry.append(timestamp)
    .append(ordernumber)
    .append(name)
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

    makeFileuploadDropZone(orderListEntry, orderData._ordernumber);
    
    orderListEntry.click(function() {
        
        showOrderDetails(orderData._ordernumber);
        
    });
    
    return orderListEntry;
                
}

var closeOrderdetails = function(ordernumber) {
    
    
    $('#order-details-entry').fadeOut(effectDurationDenominator, function() {
                
        $('#order-details-entry').remove();
        $('.order-list-entry').removeClass('ui-state-active');
                    
        $('#order-view-menu').fadeOut(effectDurationDenominator);
                
    });
    
    selectedOrdernumber = null;
    
}

var showOrderDetails = function(ordernumber) {
         
    selectedOrdernumber = ordernumber;
    
    $('#order-details-entry').fadeOut(effectDurationDenominator);
    
    $('.order-list-entry').removeClass('ui-state-active');
    
    $('#order-list-entry-' + ordernumber).addClass('ui-state-active');
        
    if (!isScrolledIntoView($('#order-list-entry-' + ordernumber))) {
                        
        $('#order-list-layer').scrollTo($('#order-list-entry-' + ordernumber), effectDurationDenominator);
        
    }
        
    var orderdetails = $('<div>');
    orderdetails.attr('style', 'display: none');
    orderdetails.addClass('order-details-entry');
    orderdetails.addClass('ui-widget');
    orderdetails.attr('id', 'order-details-entry');
        
    $('#content-layer').html(orderdetails);            
    $('#content-layer').click(function() {
            
        $('#content-layer').unbind('click');
                
        closeOrderdetails(ordernumber);
            
    });
        
    orderdetails.click(function() {
            
        return false;
            
    });
    
    $('#order-view-menu').fadeIn(effectDurationDenominator);
    
    setOrderDetailsView(ordernumber, orderdetails);
    
    orderdetails.fadeIn(effectDurationDenominator);
         
}

var setOrderDetailsView = function(ordernumber, target) {
    
    var orderview = $('input[name$="orderview"]:checked').val();
    
    if (orderview == 'information') {
            
        showOrderInfo(ordernumber, target);

    }

    adjustViewPort();
    
}

var makeFileuploadDropZone = function(dropZone, ordernumber) {
    
    var identifier = getUniqueIdFromServer();
    var uploadingString = 'Laddar upp ';

    $('body').append('<div id=\'fileupload-' + identifier + '\' class=\'fileupload\'></div>');
    
    $('#fileupload-' + identifier).fileupload({
        type: 'POST',
        url: 'servlet/upload',
        singleFileUploads: false,
        dropZone: dropZone,
        namespace: identifier,
        formData: [
        {
            name: 'ordernumber',
            value: ordernumber
        },
        {
            name: 'email',
            value: getEmail
        },
        {
            name: 'fullname',
            value: getName
        },
        {
            name: 'identifier',
            value: identifier
        }
        ]
    });
    
    $('#fileupload-' + identifier).bind('fileuploaddragover', function (e) {
        
        dropZone.addClass('ui-state-hover');
        
    })
    .bind('fileuploaddrop', function (e, data) {
        
        dropZone.removeClass('ui-state-hover');
               
        $.each(data.files, function(i, v) {
       
            uploadingString += v.name;
            
            if (i != (data.files.length - 1)) {
                uploadingString += ', ';
            }
       
        });
        
    }).bind('fileuploadstart', function (e) {
         
        var progressbar = $('<div>').addClass('progressbar');
        progressbar.attr('id', 'progessbar-' + identifier);
        progressbar.progressbar({
            value: 0
        });
        
        var currentFileUpload = $('<div>').addClass('current-file-upload');
        currentFileUpload.attr('id', 'current-file-upload-' + identifier);
        currentFileUpload.attr('title', uploadingString);
        currentFileUpload.html(truncateString(uploadingString, 51, 49));
        
        $('#order-list-entry-' + ordernumber)
        .append(progressbar)
        .append(currentFileUpload);
        
        makeFileuploadDropZone(dropZone, ordernumber);
                
    })
    .bind('fileuploadstop', function (e, data) {
        
        $('#fileupload-' + identifier).remove();
        $('#progessbar-' + identifier).fadeOut(effectDurationDenominator, function() {
            $('#progessbar-' + identifier).remove();
        });
        
        $('#current-file-upload-' + identifier).fadeOut(effectDurationDenominator, function() {
            $('#current-file-upload-' + identifier).remove();
        });
        
    })
    .bind('fileuploadprogressall', function (e, data) {
        
        $('#progessbar-' + identifier).progressbar('value' , parseInt(data.loaded / data.total * 100, 10));
        
    });
    
    dropZone.bind('dragleave', function(e) {
        
        dropZone.removeClass('ui-state-hover');
        
    });
    
}

var getUniqueIdFromServer = function() {
    
    var id;
    
    $.ajax({
        async: false,
        url: 'servlet/unique_id?action=get_timestamp',
        dataType: 'json',
        context: document.body,
        success: function(data){
            id = data;
        }
    });
    
    return id;
    
}

var hasProductionPlan = function(ordernumber) {
    
    var retval = false;
    
    $.ajax({
        async: false,
        url: 'servlet/productionplan?action=order_has_productionplan&ordernumber=' + ordernumber,
        dataType: 'json',
        context: document.body,
        success: function(data){
            retval = data;
        }
    });
    
    return retval;
    
}

var handleLevel9Message = function(bayeuxMessage) {
    
    var message = $.parseJSON(bayeuxMessage.data);

    if (message.type == 'usermessage') {
        
        showTextMessage(message.body, message.time);
        
    } else if (message.type == 'orderupdatemessage') {
        
        updateOrderdata(message.body);
            
    } else if (message.type == 'orderremovalmessage') {
        
        removeOrder(message.body);
            
    }else if (message.type == 'addordermessage') {
        
        addOrder(message.body);
        doOrderListSort();
        
    }
    
}

var addOrder = function(orderData) {

    $('#order-list').append(generateOrderListEntry(orderData));
        
    $('#order-list-entry-' + orderData.ordernumber).fadeIn(effectDurationDenominator * 2, function() {
        
        adjustViewPort();
        
    });

}

var removeOrder = function(ordernumber) {
    
    closeOrderdetails(ordernumber);
    
    $('#order-list-entry-' + ordernumber).fadeOut(effectDurationDenominator, function() {
        
        $('#order-list-entry-' + ordernumber).remove();
        adjustViewPort();
        
    });
    
}

var updateOrderdata = function(orderData) {
        
    $.each(orderData, function(key, value) {

        if (key != 'ordernumber') {
            
            $('#order-list-entry-' + orderData.ordernumber + ' .' + key).fadeOut(effectDurationDenominator, function() {
            
                $('#order-list-entry-' + orderData.ordernumber + ' .' + key).html(truncateString(value, 33, 35));
                $('#order-list-entry-' + orderData.ordernumber + ' .' + key).addClass("ui-state-highlight");
                $('#order-list-entry-' + orderData.ordernumber + ' .' + key).fadeIn(effectDurationDenominator, function() {
                
                    adjustViewPort();
                
                    setTimeout(function() {
                        $('#order-list-entry-' + orderData.ordernumber + ' .' + key).removeClass( "ui-state-highlight", effectDurationDenominator * 6 );
                    }, effectDurationDenominator * 4);
                
                });
            
            });
        
        }
      
    });
    
}

var isScrolledIntoView = function(elem) {
    
    var docViewTop = $(window).scrollTop();
    var docViewBottom = docViewTop + $(window).height();

    var elemTop = $(elem).offset().top;

    var elemBottom = elemTop + 2;
    
    return ((elemBottom >= docViewTop) && (elemTop <= docViewBottom));
    
}

var updateTips = function(t) {
    $('.validateTips')
    .text( t )
    .addClass( "ui-state-highlight" );
    setTimeout(function() {
        $('.validateTips').removeClass( "ui-state-highlight", effectDurationDenominator * 8 );
    }, effectDurationDenominator * 2 );
}

var checkLength = function(o, n, min, max) {
    if ( o.val().length > max || o.val().length < min ) {
        o.addClass( "ui-state-error" );
        updateTips( n + " måste bestå av minst " +
            min + " och max " + max + " tecken." );
        o.focus();
        return false;
    }else {
        return true;
    }
}

var checkRegexp = function(o, regexp, n) {
    if ( !( regexp.test( o.val() ) ) ) {
        o.addClass( "ui-state-error" );
        updateTips( n );
        return false;
    } else {
        return true;
    }
}

var addAtom = function(elementname, filename) {
    
    $.ajax({
        async: false,
        url: 'atoms/' + filename,
        context: document.body,
        success: function(data){
            $('#' + elementname).append(data);
        }
    });
    
}

var showUserSettingsDialog = function() {
    
    if (!$('#userSettingsDialog').length) {
        
        addAtom('dialog-layer', 'usersettingsdialog.html');
        
    }
                
    if (getName != null || getEmail != null) {
                    
        $('#userSettingsDialogNoData').hide();
        $('#userSettingsDialogChangeData').show();
                    
        $('#name').val(getName);
        $('#email').val(getEmail);

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
                        
            $('#userSettingsDialogNoData').hide();
            $('#userSettingsDialogChangeData').show();
                        
        },
        modal: true,
        resizable: false,
        show: "fade",
        hide: "fade",
        autoOpen: false,
        width: 441,
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
                                
                    setName($('#name').val());
                    setEmail($('#email').val());

                    $(this).dialog( "close" );
                                                    
                }
            }
        }
    });
    
    $('#userSettingsDialog').dialog('open');
    $('#name').focus();
    
}

var truncateString = function(string, length, limit) {
    
    var retval = string;
    
    if (!limit) {
        
        limit = length;
        
    }
    
    if (string.length > limit) {
    
        retval = string.substring(0,length) + "\u2026";

    }
    
    return retval;
    
}