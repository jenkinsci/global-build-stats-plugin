  YAHOO.namespace("global.build.search.calendar");
  
  YAHOO.global.build.search.calendar.selectEvent = function(type, selectedDates, target) {
    updateDateWithTime(target.getSelectedDates()[0].getTime(), target.targetInputId, target.targetDisplayId); 
  	target.enclosingDialog.hide();
  }
  
  YAHOO.global.build.search.calendar.renderEvent = function(type, nullValue, target) {
  	target.enclosingDialog.fireEvent("changeContent");
  }
  
  function verifyDates(){
    $("datesError").innerHTML = (($("timeStart").value > $("timeEnd").value)?generateErrorMessage($('swapDatesErrorMessage').innerHTML):"");
  }
  
  function updateDateWithTime(time, targetInputId, targetDisplayId){
    $(targetInputId).value = time;
    $(targetDisplayId).innerHTML = displayTime(time);
    verifyDates();
  }
  
  function displayTime(time){
  	var d = new Date();
  	d.setTime(time);
  	return YAHOO.util.Date.format(d, { format: "%Y-%m-%d"} );
  }
  
  function initCalendar(yuiCalendar, yuiDialog, defaultTimeValue, targetInputId, targetDisplayId){
  	  var d = new Date();
  	  d.setTime(defaultTimeValue);
  	  
  	  yuiCalendar.cfg.setProperty("iframe", false, false);
  	  
  	  yuiCalendar.cfg.setProperty("pagedate", (d.getMonth()+1) + "/" + d.getFullYear(), false);
  	  yuiCalendar.cfg.setProperty("selected", (d.getMonth()+1) + "/" + d.getDate() + "/" + d.getFullYear(), false);
  	  yuiCalendar.cfg.setProperty("close", false, false);
  	  yuiCalendar.cfg.setProperty("navigator", true, false);
  	  // Declaring new attributes on yuiCalendar
  	  yuiCalendar.targetInputId = targetInputId;
  	  yuiCalendar.targetDisplayId = targetDisplayId;
  	  yuiCalendar.enclosingDialog = yuiDialog;
  	  
  	  yuiCalendar.selectEvent.subscribe(YAHOO.global.build.search.calendar.selectEvent, yuiCalendar, true);
  	  yuiCalendar.renderEvent.subscribe(YAHOO.global.build.search.calendar.renderEvent, yuiCalendar, true);
  }
  
  function initDialog(yuiDialog, showButtonId){
	yuiDialog.cfg.setProperty("context", [showButtonId, "tl", "bl"], false);
	yuiDialog.cfg.setProperty("width", "16em", false);
	yuiDialog.cfg.setProperty("draggable", false, false);
	yuiDialog.cfg.setProperty("close", false, false);
  }
  
  YAHOO.global.build.search.calendar.init = function() {
  	  YAHOO.global.build.search.calendar.startDialog = new YAHOO.widget.Dialog("startCalendarContainer");
  	  YAHOO.global.build.search.calendar.startCalendar = new YAHOO.widget.Calendar("startCalendar");
  	  YAHOO.global.build.search.calendar.endDialog = new YAHOO.widget.Dialog("endCalendarContainer");
	  YAHOO.global.build.search.calendar.endCalendar = new YAHOO.widget.Calendar("endCalendar"); 
      
      initCalendar(YAHOO.global.build.search.calendar.startCalendar, YAHOO.global.build.search.calendar.startDialog, startDate, "timeStart", "startTimeDisplay");
      initCalendar(YAHOO.global.build.search.calendar.endCalendar, YAHOO.global.build.search.calendar.endDialog, endDate, "timeEnd", "endTimeDisplay");
      initDialog(YAHOO.global.build.search.calendar.startDialog, "showStartCalendar");
      initDialog(YAHOO.global.build.search.calendar.endDialog, "showEndCalendar");
  	  
  	  YAHOO.global.build.search.calendar.startCalendar.render();
  	  YAHOO.global.build.search.calendar.endCalendar.render();
  	  YAHOO.global.build.search.calendar.startDialog.render();
  	  YAHOO.global.build.search.calendar.endDialog.render();
  	  
  	  YAHOO.global.build.search.calendar.startDialog.hide();
  	  YAHOO.global.build.search.calendar.endDialog.hide();
  	  
	  updateDateWithTime(startDate, "timeStart", "startTimeDisplay");
	  updateDateWithTime(endDate, "timeEnd", "endTimeDisplay");
	  
	  YAHOO.util.Event.on("showStartCalendar", "click", YAHOO.global.build.search.calendar.startDialog.show, YAHOO.global.build.search.calendar.startDialog, true);
	  YAHOO.util.Event.on("showEndCalendar", "click", YAHOO.global.build.search.calendar.endDialog.show, YAHOO.global.build.search.calendar.endDialog, true);
  }
  
  YAHOO.util.Event.onDOMReady(YAHOO.global.build.search.calendar.init);
