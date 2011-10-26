    
function initMessageBar() {
        
    if (localStorage.getItem("messagebar")) {
            
        $('#notice-layer').html(localStorage.getItem("messagebar"));
            
    }
        
}
    
function clearMessageBar() {
        
    localStorage.removeItem("messagebar");
    
}
    
function showTextMessage(message, timestring) {

    if (message.length > 0) {

        var completeText = '<b>' + timestring + '</b>&nbsp;-&nbsp;' + message + '&nbsp;&nbsp;&nbsp;' + $('#notice-layer').html().substr(0,3000);

        $('#notice-layer').html(completeText);
        
        localStorage.setItem("messagebar", completeText);

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
    