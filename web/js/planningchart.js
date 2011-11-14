
function showPlanningChart(ordernumber, target) {

    var ganttchart = $('<div>');
    
    ganttchart.hegantt({
        source: 'servlet/productionplan?action=get_productionplan_by_ordernumber&ordernumber=' + ordernumber,
        callback: adjustViewPort
    });
    
    target.html(ganttchart);

}
