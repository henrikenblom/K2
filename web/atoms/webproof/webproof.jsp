<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<link rel="stylesheet" type="text/css" href="atoms/webproof/webproof.css">
<script type="text/javascript">
    
    $(document).ready(function() {

        if ($('#webproof-controls').length == 0) {

            var controlbar = $('<div id="webproof-controls" class="ui-widget"></div>');
            var filechooserbutton = $('<input type="checkbox"><label>Filnamn</label>');
            var printsimulationbutton = $('<input type="checkbox"><label>Simulera tryck</label>');

            filechooserbutton.button({
            
                icons: {
                    primary: "ui-icon-document",
                    secondary: "ui-icon-triangle-1-s"
                }
            
            });
            
            printsimulationbutton.button();
            
            printsimulationbutton.change(function() {
               
               alert("Shooo!");
               
            });
            
            controlbar
            .append(filechooserbutton)
            .append(printsimulationbutton);

            $('#plugin-controls').append(controlbar);

        }

    });
    
</script>
<h1><%= request.getParameter("ordernumber")%></h1>
