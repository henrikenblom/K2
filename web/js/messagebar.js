    
function initMessageBar() {
        
    if ($.cookie("messagebar") != null) {
            
        $('#notice-layer').html($.cookie("messagebar"));
            
    }
        
}
    
function clearMessageBar() {
        
    //$('#notice-layer').html("");
        
    $.cookie("messagebar", null, {
        path: '/', 
        expires: -1
    });
        
}
    
function showTextMessage(message, timestring) {

    if (message.length > 0) {

        var completeText = '<b>' + timestring + '</b>&nbsp;-&nbsp;' + message + '&nbsp;&nbsp;&nbsp;' + $('#notice-layer').html().substr(0,3000);

        $('#notice-layer').html(completeText);
        
        $.cookie("messagebar", completeText, {
            path: '/', 
            expires: 1
        });
        
        animateShowNoticeLayer();
        
    }

}
            
function handleMouseOverNoticeHoverDetectionLayer() {
                
    noticeLayerMouseOver = true;
    animateShowNoticeLayer();
                
}
            
function handleMouseOutNoticeHoverDetectionLayer() {
                
    noticeLayerDisplayTimer = setInterval("animateHideNoticeLayer(); noticeLayerMouseOver = false", effectDurationDenominator);
                
}
            
function animateShowNoticeLayer() {
                
    clearInterval(noticeLayerDisplayTimer);
                                                    
    $('#notice-layer').slideDown(effectDurationDenominator * 2, function() {
                    
        if (!noticeLayerMouseOver) {
                        
            noticeLayerDisplayTimer = setInterval("animateHideNoticeLayer()", noticeDuration);

        }
            
    });
                
}
            
function animateHideNoticeLayer() {
                
    $('#notice-layer').slideUp(effectDurationDenominator * 2);
                
    clearInterval(noticeLayerDisplayTimer);
                
}
    