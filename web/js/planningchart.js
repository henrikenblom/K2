
function showPlanningChart(ordernumber, target) {

    var ganttchart = $('<div>');
    
    ganttchart.gantt({
        source: 'js/data.js'
    });
    
    target.html(ganttchart);

}
