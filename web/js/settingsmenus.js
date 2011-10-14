function showUserSettingsAction() {
        
    showUserSettingsDialog();
    hideUserMenu();
        
}

function logoutAction() {
       
    clearMessageBar();
       
    document.location = 'logout.jsp';
        
}

function showUserMenu() {
                
    $('#user-menu').fadeIn(effectDurationDenominator, function() {

        $(document).bind('click', function(event) {
            if ($(event.target) != $('#user-menu')) {
                hideUserMenu();
            }
        });
                          
    });
        
}
    
function hideUserMenu() {
    
    $('#user-menu').fadeOut(effectDurationDenominator, function() {
            
        $(document).unbind('click');

        $('#user-menu-button').attr('checked', false);
        $('#user-menu-button').button('refresh');
                                                
    });
    
}
    
$(document).ready(function() {
    
    $('#userFunctionsButton').button({
        icons: {
            primary: "ui-icon-gear"
        }
    });
                
    $('#user-menu-button').button({
        icons: {
            primary: "ui-icon-gear",
            secondary: "ui-icon-triangle-1-s"
        }
    });

    $('#user-menu-button').change(function() {
                    
        if ($('#user-menu-button').is(':checked')) {
                        
            showUserMenu();
                                            
        } else {
                        
            hideUserMenu();
                        
        }
                    
    });
        
});