    
function initMessageBar() {
        
    if ($.cookie("messagebar") != null) {
            
        $('#noticeLayer').html($.cookie("messagebar"));
            
    }
        
}
    
function clearMessageBar() {
        
    //$('#noticeLayer').html("");
        
    $.cookie("messagebar", null, {
        path: '/', 
        expires: -1
    });
        
}
    
function showTextMessage(message, timestring) {

    if (message.length > 0) {

        var completeText = '<b>' + timestring + '</b>&nbsp;-&nbsp;' + message + '&nbsp;&nbsp;&nbsp;' + $('#noticeLayer').html().substr(0,3000);

        $('#noticeLayer').html(completeText);
        
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
                                                    
    $('#noticeLayer').slideDown(effectDurationDenominator * 2, function() {
                    
        if (!noticeLayerMouseOver) {
                        
            noticeLayerDisplayTimer = setInterval("animateHideNoticeLayer()", noticeDuration);

        }
            
    });
                
}
            
function animateHideNoticeLayer() {
                
    $('#noticeLayer').slideUp(effectDurationDenominator * 2);
                
    clearInterval(noticeLayerDisplayTimer);
                
}
    