
$.fn.hegantt = function(options) {
    
    var PIXELSPERMILLISECOND = 0.000005;
    
    var CELLWIDTH = 18;
    var CELLHEIGHT = 24;
        
    var data = null;
    
    var defaults = {
        source: null,
        months: ["Januari", "Februari", "Mars", "April", "Maj", "Juni", "Juli", "Augusti", "September", "Oktober", "November", "December"],
        dow: ["Söndag", "Måndag", "Tisdag", "Onsdag", "Torsdag", "Fredag", "Lördag"],
        callback: null
    }
    
    var options = $.extend(defaults, options);
    var rows = 0;
    var dateStart = null;
    var dateEnd = null;
    var range = null;
    var chartwidth = 0;
    var barstyleindex = 0;
        
    var create = function(container) {
        
        if (!options.source) {
            return;
        }
    
        $.getJSON(options.source, function(rData) {
            
            data = rData;
            
            dateStart = tools.getMinDate();
            dateEnd = tools.getMaxDate();
            range = tools.parseDateRange(dateStart, dateEnd);
        
            var gantt = $('<div class="hegantt ui-widget"/>');
            
            var content = $('<div class="fn-content"/>');
            
            var rightpanel = $('<div class="rightPanel"/>');
                            
            createLeftPanel(content);
            
            createGridHeader(rightpanel);
            
            rightpanel.height(CELLHEIGHT * (rows + 4) + 1);
                                                                    
            content.append(rightpanel);
            
            createGrid(rightpanel);
            
            gantt.append(content);
                
            container.html(gantt);
            
            if (options.callback && typeof(options.callback) === "function") {
                options.callback();
            }
                
        });
                
    }

    var createBar = function(from, length, index) {

        var width = length * PIXELSPERMILLISECOND;
        
        if (width < CELLWIDTH) {
            
            width = CELLWIDTH / 2;
            
        }

        var bar = $('<div class="bar">')
        .css('top', index * CELLHEIGHT)
        .css('margin-left', (from - dateStart.getTime()) * PIXELSPERMILLISECOND)
        .width(width)
        .html('<div class="bar-content ui-corner-all barstyle-' + barstyleindex + '"></div>');
            
        if (barstyleindex++ > 4) {
            barstyleindex = 0;
        }
            
        return bar;
        
    }
    
    var createLeftPanel = function(target) {
        
        var leftPanel = $('<div class="leftPanel"/>')
        .append($('<div class="row spacer"/>')
            .height(CELLHEIGHT * 4)
            .css("width", "100%"));
            
        $.each(data, function(i, entry) {

            if (entry.values) {
                
                leftPanel
                .append($('<div class="row name"/>').append($('<span class="label"/>').html(entry.name)));
                
                rows++;
                
            }
            
        });
        
        target.append(leftPanel);
        
    }
    
    var createGrid = function(target) {
        
        var grid = $('<div class="grid"/>')
        .width(CELLWIDTH * range.length);
        
        var skipmarkers = $('<div class="skipmarkers">');
        
        target.append(grid);
        target.append(skipmarkers);
        
        var skippedMilliseconds = 0;
        var markerCount = 0;
        var skipLength = 0;
        var skipMarkerPosition = 0;
        
        $.each(data, function(i, entry) {

            skipMarkerPosition = 0;
            skipLength = 0;
                        
            try {  
                
                skipMarkerPosition = (entry.values[0].skip_from - dateStart.getTime() - skippedMilliseconds) * PIXELSPERMILLISECOND;
                
                skipLength = (entry.values[0].skip_to - entry.values[0].skip_from);
                
            } catch (e) {
            
                try {
                    
                    skipMarkerPosition = (entry.skipspan.skip_from - dateStart.getTime() - skippedMilliseconds) * PIXELSPERMILLISECOND;
                
                    skipLength = (entry.skipspan.skip_to - entry.skipspan.skip_from);
                
                } catch (e) {
                    
                }
                                                                                   
            }
            
            if (skipMarkerPosition > 0) {
                                
                var skipmarker = $('<div class="skipmarker">')
                .css('left', (skipMarkerPosition - 33) - (33 * markerCount++))
                .height(CELLHEIGHT * (rows + 5) + 1);
                                            
                skipmarkers.append(skipmarker);
                                            
            }

            if (!skipLength) {
                
                skipLength = 0;
                
            }

            if (entry.values) {
                
                $.each(entry.values, function(j, barData) {
                                    
                    grid.append(createBar(barData.from - skippedMilliseconds, barData.to - barData.from - skipLength, i));
                                    
                });
            
            }
            
            skippedMilliseconds += skipLength;
            
        });
        
        grid.height(CELLHEIGHT * rows);
        
    }
    
    var createGridHeader = function(target) {
        
        var years = $("<div class='row'/>");
        var year = range[0].getFullYear();
        var daysInYear = 0;

        var month = range[0].getMonth();
        var daysInMonth = 0;
			
        var days = $('<div class="row"/>');
        var day = range[0].getDate();
        var hoursInDay = 0;
        
        var dows = $('<div class="row"/>');
        var dow = range[0].getDay();
        
        var hours = $('<div class="row"/>');
                
        $.each(range, function(i, hour) {
                
            if (hour.getFullYear() != year) {
                                    
                years.append($('<div class="row header year"/>')
                    .width(CELLWIDTH * daysInYear)
                    .append($("<div class='label'/>").html(year)));
                                
                year = hour.getFullYear();
                
                daysInYear = 0;
                
            }
            
            daysInYear++;

            if (hour.getDate() != day) {
                                                    
                days.append($('<div class="row header day"/>')
                    .width(CELLWIDTH * hoursInDay)
                    .append($("<div class='label'/>").html(day + ' ' + options.months[month])));
                                    
                dows.append($('<div class="row header dow"/>')
                    .width(CELLWIDTH * hoursInDay)
                    .append($("<div class='label'/>").html(options.dow[dow])));
                                
                day = hour.getDate();
                dow = hour.getDay();
                
                hoursInDay = 0;
                
            }            
            
            hoursInDay++;
            
            if (hour.getMonth() != month) {
                
                month = hour.getMonth();
                
                daysInMonth = 0;
                
            }
            
            daysInMonth++;
                                        
            hours.append($('<div class="row header hour"/>')
                .width(CELLWIDTH)
                .html(hour.getHours()));
                    
            chartwidth += CELLWIDTH;
                            
        });
        
        years.append($('<div class="row header year"/>')
            .width(CELLWIDTH * daysInYear)
            .append($("<div class='label'/>").html(year)));
            
        days.append($('<div class="row header day"/>')
            .width(CELLWIDTH * hoursInDay)
            .append($("<div class='label'/>").html(day + ' ' + options.months[month])));
            
        dows.append($('<div class="row header dow"/>')
            .width(CELLWIDTH * hoursInDay)
            .append($("<div class='label'/>").html(options.dow[dow])));

        var gridHeader = $("<div class='gridheader'/>")
        .height((CELLHEIGHT * 4) + 1)
        .width(chartwidth)
        .append(years)
        .append(dows)
        .append(days)
        
        .append(hours);
        
        target.append(gridHeader);

    }
    
    var tools = new function() {
        
        this.findNextDateNotInSkiprange = function(date) {
            
            var retval = date;
            var inSkipRange = false;
            
            retval.addHours(1);
            
            $.each(data, function(i, entry) {
               
                try {  
                    
                    if ((date.getTime() >= entry.values[0].skip_from)
                        && (date.getTime() <= entry.values[0].skip_to)) {
                        
                        retval = new Date(entry.values[0].skip_to);
                        inSkipRange = true;
                        
                    }             
                                        
                } catch (e) {
                   
                    try {
                                
                        if (date.getTime() >= entry.skipspan.skip_from
                            && date.getTime() <= entry.skipspan.skip_to) {
                            
                            retval = new Date(entry.skipspan.skip_to);
                            inSkipRange = true;
                            
                        }
                                
                    } catch (e) {
                    }
                   
                }
                
                if (inSkipRange) {
                    
                    return retval;
                    
                }
               
            });
            
            return retval;
            
        }

        this.parseDateRange = function(from, to) {
            
            var current = new Date(from.getTime());
            
            var ret = new Array();
            var i = 0;
            
            while (current <= to) {
                
                var date = new Date(current.getTime());

                ret[i++] = date;

                current = tools.findNextDateNotInSkiprange(current);
                                    
            }
            
            return ret;
            
        }

        this.getMaxDate = function() {
            
            var maxDate = null;
            
            $.each(data, function(i, entry) {
                
                if (entry.values) {
                    $.each(entry.values, function(i, date) {
                        maxDate = maxDate < new Date(date.to) ? new Date(date.to) : maxDate;
                    });
                }
            });
            
            maxDate.addHours(1);
            
            return maxDate;
            
        }
        
        this.getMinDate = function() {
            
            var minDate = null;
            
            $.each(data, function(i, entry) {
                
                if (entry.values) {
                    $.each(entry.values, function(i, date) {
                        minDate = minDate > new Date(date.from) || minDate == null ? new Date(date.from) : minDate;
                    });
                }
            });
            
            minDate.addHours(-1);
            
            return minDate;
            
        }
        
    }
    
    Date.prototype.addHours= function(h){
        this.setHours(this.getHours()+h);
        return this;
    }
    
    $(this).empty();
    create($(this));
 
}