function showUserSettingsAction() {
        
    showUserSettingsDialog();
    hideUserMenu();
        
}

function logoutAction() {
       
    clearMessageBar();
       
    document.location = 'logout.jsp';
        
}

function showUserMenu() {
                
    $('#userMenu').fadeIn(effectDurationDenominator, function() {

        $(document).bind('click', function(event) {
            if ($(event.target) != $('#userMenu')) {
                hideUserMenu();
            }
        });
                          
    });
        
}
    
function hideUserMenu() {
    
    $('#userMenu').fadeOut(effectDurationDenominator, function() {
            
        $(document).unbind('click');

        $('#userMenuButton').attr('checked', false);
        $('#userMenuButton').button('refresh');
                                    
        $('#userMenu').fadeOut(effectDurationDenominator);
            
    });
    
}
    
$(document).ready(function() {
    
    $('#userFunctionsButton').button({
        icons: {
            primary: "ui-icon-gear"
        }
    });
                
    $('#userMenuButton').button({
        icons: {
            primary: "ui-icon-gear",
            secondary: "ui-icon-triangle-1-s"
        }
    });

    $('#userMenuButton').change(function() {
                    
        if ($('#userMenuButton').is(':checked')) {
                        
            showUserMenu();
                                            
        } else {
                        
            hideUserMenu();
                        
        }
                    
    });
        
});