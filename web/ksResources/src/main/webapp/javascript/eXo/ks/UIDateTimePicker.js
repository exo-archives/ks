UIDateTimePicker = function(calendarId) {
	this.calendarId = calendarId ;
	this.dateField = null ;
	this.currentDate = null ; 	// Datetime value base of selectedDate for displaying calendar below
															// if selectedDate is invalid, currentDate deals with system time;
	this.selectedDate = null ; //Datetime value of input date&time field
	this.months = ['January','February','March','April','May','June','July','August','September','October','November','December'] ;
	this.weekdays = ['S','M','T','W','T','F','S'] ;
	this.tooltip = ['Previous Year', 'Previous Month', 'Next Month', 'Next Year'];
	this.pathResource = "/ksResources/javascript/eXo/ks/lang/";
	this.lang = "";
	this.fistWeekDay = 0; // sunday: 0, monday: 1, tuesday: 2, wednesday: 3, thursday: 4, friday: 5, saturday: 6 
}

UIDateTimePicker.prototype.getLang = function() {
	try {
	  var day = this.dateField.getAttribute('fistweekday');
	  if (day) this.fistWeekDay = day * 1 - 1; // attribute 'fistweekday' includes: sunday: 1, monday: 2,..., saturday: 7 
		var lang = this.dateField.getAttribute('lang'); 
		if (this.lang == lang) 
			return;
		this.lang = lang;
		var languages = eval(ajaxAsyncGetRequest(this.pathResource + this.lang.toLowerCase() + ".js", false));
		if (!languages || (typeof(languages) != "object")) 
			return;
	this.months = languages[0];
	this.weekdays = languages[1];
	this.tooltip = languages[2];
	} 
	catch (e) {}
} ;

UIDateTimePicker.prototype.init = function(field, isDisplayTime) {
	this.isDisplayTime = isDisplayTime ;
	if (this.dateField) {
		this.dateField.parentNode.style.position = '' ;
	}
	this.dateField = field ;
	eXo.ks.UIDateTimePicker.getLang() ;
	if (!document.getElementById(this.calendarId)) {
		this.create() ;
	}
//	field.parentNode.style.position = 'relative' ;
	field.parentNode.insertBefore(document.getElementById(this.calendarId), field) ;
	this.show() ;
};


UIDateTimePicker.prototype.create = function() {
	var clndr = document.createElement("div") ;
	clndr.id = this.calendarId ;
	clndr.style.position = "absolute" ;
	if (document.all) {
		clndr.innerHTML = "<div class='UICalendarComponent'><iframe id='" + this.calendarId + "IFrame' frameBorder='0' scrolling='no'></iframe><div style='position: absolute'></div></div>" ;
	} else {
		clndr.innerHTML = "<div class='UICalendarComponent'><div style='position: absolute; width: 100%;'></div></div>" ;
	}
	document.body.appendChild(clndr) ;
};

UIDateTimePicker.prototype.show = function() {
	document.onmousedown = new Function('eXo.ks.UIDateTimePicker.hide()') ;
	var re = /^(\d{1,2}\/\d{1,2}\/\d{1,4})\s*(\s+\d{1,2}:\d{1,2}:\d{1,2})?$/i ;
	this.selectedDate = new Date() ;
	if (re.test(this.dateField.value)) {
		var dateParts = this.dateField.value.split(" ") ;
		var arr = dateParts[0].split("/") ;
		this.selectedDate.setMonth(parseInt(arr[0],10) - 1) ;
		this.selectedDate.setDate(parseInt(arr[1],10)) ;
		this.selectedDate.setFullYear(parseInt(arr[2],10)) ;
		if (dateParts.length > 1 && dateParts[dateParts.length - 1] != "") {
			arr = dateParts[dateParts.length - 1].split(":") ;
			this.selectedDate.setHours(arr[0], 10) ;
			this.selectedDate.setMinutes(arr[1], 10) ;
			this.selectedDate.setSeconds(arr[2], 10) ;
		}
	}
	var fieldDateTime = eXo.core.DOMUtil.findFirstDescendantByClass(this.dateField.parentNode, "input","DateTimeInput") ;
	this.currentDate = new Date(this.selectedDate.valueOf()) ;
	var clndr = document.getElementById(this.calendarId) ;
	clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
	var x = 0 ; 
	//var y = this.dateField.offsetHeight ;
	var y = fieldDateTime.offsetHeight ;
	if(eXo.core.Browser.getBrowserType() == "ie")	{
		x = -(eXo.core.Browser.findPosX(this.dateField) - eXo.core.Browser.findPosX(fieldDateTime)+this.dateField.offsetWidth);
	}
	with (clndr.firstChild.style) {
		display = 'block' ;
		left = x+"px";
		top = y +2+ "px" ;
	}

	var drag = document.getElementById("BlockCaledar");
	var component =	eXo.core.DOMUtil.findAncestorByClass(drag, "UICalendarComponent");
	var calendar = eXo.core.DOMUtil.findFirstChildByClass(drag, "div", "UICalendar");
	var innerWidth = drag.offsetWidth;
	drag.onmousedown = function(evt) {
		var event = evt || window.event;
		event.cancelBubble = true;
		drag.style.position = "absolute";
		if(eXo.core.Browser.isIE7()) drag.style.height = calendar.offsetHeight + "px";
		drag.style.width = innerWidth + "px";
		eXo.core.DragDrop.init(null, drag, component, event);
	}
	if(eXo.core.Browser.isIE6()) clndr.getElementsByTagName("iframe")[0].style.height=drag.parentNode.offsetHeight + "px";
};

UIDateTimePicker.prototype.hide = function() {
	if (this.dateField) {
		document.getElementById(this.calendarId).firstChild.style.display = 'none' ;
//		this.dateField.parentNode.style.position = '' ;
		this.dateField = null ;
	}
 	document.onmousedown = null ;
};

/* TODO: Move HTML code to a javascript template file (.jstmpl) */
UIDateTimePicker.prototype.renderCalendar = function() {
	var dayOfMonth = 1 ;
	var validDay = 0 ;
	var startDayOfWeek = this.getDayOfWeek(this.currentDate.getFullYear(), this.currentDate.getMonth() + 1, dayOfMonth) ;
	var daysInMonth = this.getDaysInMonth(this.currentDate.getFullYear(), this.currentDate.getMonth()) ;
	var clazz = null;
	var dayIdx = this.fistWeekDay;
	var table = '<div id="BlockCaledar" class="BlockCalendar">' ;
	table += 		'<div class="UICalendar" onmousedown="event.cancelBubble = true">' ;
	table += 		'	<table class="MonthYearBox">' ;
	table += 		'		<tr>' ;
	table += 		'			<td class="MonthButton"><a class="PreviousMonth" title="' + this.tooltip[1]+ '" href="javascript:eXo.ks.UIDateTimePicker.changeMonth(-1);"></a></td>' ;
	table += 		'			<td class="YearButton"><a class="PreviousYear" title="' + this.tooltip[0]+ '" href="javascript:eXo.ks.UIDateTimePicker.changeYear(-1);"></a></td>' ;
	table += 		'			<td><font color="#f89302">' + this.months[this.currentDate.getMonth()] + '</font> - ' + this.currentDate.getFullYear() + '</td>' ;
	table += 		'			<td class="YearButton"><a class="NextYear" title="' + this.tooltip[3]+ '" href="javascript:eXo.ks.UIDateTimePicker.changeYear(1);"></a></td>' ;
	table += 		'			<td class="MonthButton"><a class="NextMonth" title="' + this.tooltip[2]+ '" href="javascript:eXo.ks.UIDateTimePicker.changeMonth(1);"></a></td>' ;
	table += 		'		</tr>' ;
	table += 		'	</table>' ;
	table += 		'	<div style="margin-top: 6px;padding: 0px 5px;">' ;
	table += 		'		<table>' ;
	table += 		'			<tr>' ;
	for (var i = 0; i < 7; i++) {
	 if (dayIdx == 0) {
	   table += '       <td><font color="red">' + this.weekdays[dayIdx] + '</font></td>';
	 } else {
	   table += '       <td><font>' + this.weekdays[dayIdx] + '</font></td>';
	 }
	 dayIdx = ++dayIdx % 7;
	}
	table += 		'			</tr>' ;
	table += 		'		</table>' ;
	table += 		'	</div>' ;
	table += 		'	<div class="CalendarGrid">' ;
	table += 		'	<table>' ;
	for (var week=0; week < 6; week++) {
		table += "<tr>";
		for (var dayOfWeek=0; dayOfWeek < 7; dayOfWeek++) {
		  var currentWeekDay = (dayOfWeek + this.fistWeekDay) % 7;
			if (week == 0 && startDayOfWeek == currentWeekDay) {
				validDay = 1;
			} else if (validDay == 1 && dayOfMonth > daysInMonth) {
				validDay = 0;
			}
			if (validDay) {
				if (dayOfMonth == this.selectedDate.getDate() && this.currentDate.getFullYear() == this.selectedDate.getFullYear() && this.currentDate.getMonth() == this.selectedDate.getMonth()) {
					clazz = 'Current';
				} else if (dayOfWeek == 0 || dayOfWeek == 6) {
					clazz = 'Weekend';
				} else {
					clazz = 'Weekday';
				}

				table = table + "<td><a class='"+clazz+"' href=\"javascript:eXo.ks.UIDateTimePicker.setDate("+this.currentDate.getFullYear()+","+(this.currentDate.getMonth() + 1)+","+dayOfMonth+")\">"+dayOfMonth+"</a></td>" ;
				dayOfMonth++ ;
			} else {
				table = table + "<td class='empty'><div>&nbsp;</div></td>" ;
			}
		}
		table += "</tr>" ;
	}		
	table += 		'		</table>' ;
	table += 		'	</div>' ;
	if (this.isDisplayTime) {
		table += 		'	<div class="CalendarTimeBox">' ;
		table += 		'		<div class="CalendarTimeBoxR">' ;
		table += 		'			<div class="CalendarTimeBoxM"><span><input class="InputTime" size="2" maxlength="2" value="' +
								((this.currentDate.getHours())>9 ? this.currentDate.getHours() : "0"+this.currentDate.getHours()) + 
								'" onkeyup="eXo.ks.UIDateTimePicker.setHour(this)" >:<input size="2" class="InputTime" maxlength="2" value="' + 
								((this.currentDate.getMinutes())>9 ? this.currentDate.getMinutes() : "0"+this.currentDate.getMinutes()) + 
								'" onkeyup = "eXo.ks.UIDateTimePicker.setMinus(this)">:<input size="2" class="InputTime" maxlength="2" value="' + 
								((this.currentDate.getSeconds())>9 ? this.currentDate.getSeconds() : "0"+this.currentDate.getSeconds()) + 
								'" onkeyup = "eXo.ks.UIDateTimePicker.setSeconds(this)"></span></div>' ;
		table += 		'		</div>' ;
		table += 		'	</div>' ;
	}
	table += 		'</div>' ;
	table += 		'</div>' ;
	return table ;
};

UIDateTimePicker.prototype.changeMonth = function(change) {
	this.currentDate.setDate(1);
	this.currentDate.setMonth(this.currentDate.getMonth() + change) ;
	var clndr = document.getElementById(this.calendarId) ;
	clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
};

UIDateTimePicker.prototype.changeYear = function(change) {
	this.currentDate.setFullYear(this.currentDate.getFullYear() + change) ;
	this.currentDay = 0 ;
	var clndr = document.getElementById(this.calendarId) ;
	clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
};

UIDateTimePicker.prototype.setDate = function(year, month, day) {	
	if (this.dateField) {
		if (month < 10) month = "0" + month ;
		if (day < 10) day = "0" + day ;
		var dateString = month + "/" + day + "/" + year ;
		if (!this.currentHours) this.currentHours = new Date().getHours() ;
		if (!this.currentMinutes) this.currentMinutes = new Date().getMinutes() ;
		if (!this.currentSeconds) this.currentSeconds = new Date().getSeconds() ;
		if(this.isDisplayTime) dateString += " " + this.currentHours + ":" + this.currentMinutes + ":" + this.currentSeconds ;
		var objRoot = this.dateField.parentNode;
		var fielDateTime = eXo.core.DOMUtil.findFirstDescendantByClass(objRoot, "input","DateTimeInput") ; 
		fielDateTime.value = dateString ;
		this.hide() ;
	}
	return ;
};

UIDateTimePicker.prototype.setSeconds = function(object) {
		if(this.dateField) {
			var seconds = object.value;
			if (seconds >= 60) {
				object.value = seconds.substring(0,1);
				return;
			}
//			this.currentHours = this.currentDate.getHours() ;
//			this.currentMinutes = this.currentDate.getMinutes() ;
			if(seconds.length < 2) seconds = "0" + seconds;
			var timeString = this.currentDate.getHours() + ":" + this.currentDate.getMinutes() + ":" + seconds;
			this.currentDate.setSeconds(seconds);
			if(!this.currentDay) this.currentDay = this.currentDate.getDay();
			if(!this.currentMonth) this.currentMonth = this.currentDate.getMonth() + 1;
			if(!this.currentYear) this.currentYear = this.currentDate.getFullYear();
			if(this.isDisplayTime) timeString = this.currentDay + "/" + this.currentMonth + "/" + this.currentYear + " " + timeString;
			this.dateField.value = timeString;
	}
	return;
};

UIDateTimePicker.prototype.setMinus = function(object) {
		if(this.dateField) {
			var minus = object.value;
			if(minus >= 60){
				object.value = minus.substring(0,1);
				return;
			}
//			this.currentHours = this.currentDate.getHours() ;
// 			this.currentSeconds = this.currentDate.getSeconds() ;
			if(minus.length < 2) minus = "0" + minus;
			this.currentDate.setMinutes(minus);
			var timeString = this.currentDate.getHours() + ":" + minus + ":" + this.currentDate.getSeconds();
			if(!this.currentDay) this.currentDay = this.currentDate.getDay();
			if(!this.currentMonth) this.currentMonth = this.currentDate.getMonth() + 1;
			if(!this.currentYear) this.currentYear = this.currentDate.getFullYear();
			if(this.isDisplayTime) timeString = this.currentDay + "/" + this.currentMonth + "/" + this.currentYear + " " + timeString;
			this.dateField.value = timeString;
	}
	return;
};

UIDateTimePicker.prototype.setHour = function(object) {
		if(this.dateField) {
			var hour = object.value;
			if (hour >= 24){
				object.value = hour.substring(0,1);	
				return;
			}
//			this.currentMinutes = this.currentDate.getMinutes() ;
//			this.currentSeconds = this.currentDate.getSeconds() ;
			if(hour.length < 2) hour = "0" + hour;
			this.currentDate.setHours(hour);
			var timeString = hour + ":" + this.currentDate.getMinutes() + ":" + this.currentDate.getSeconds();
			if(!this.currentDay) this.currentDay = this.currentDate.getDay();
			if(!this.currentMonth) this.currentMonth = this.currentDate.getMonth() + 1;
			if(!this.currentYear) this.currentYear = this.currentDate.getFullYear();
			if(this.isDisplayTime) timeString = this.currentDay + "/" + this.currentMonth + "/" + this.currentYear + " " + timeString;
			this.dateField.value = timeString;
	}
	return;
};

UIDateTimePicker.prototype.clearDate = function() {
	this.dateField.value = '' ;
	this.hide() ;
};

UIDateTimePicker.prototype.getDayOfWeek = function(year, month, day) {
	var date = new Date(year, month - 1, day) ;
	return date.getDay() ;
};

UIDateTimePicker.prototype.getDaysInMonth = function(year, month) {
	return [31, ((!(year % 4 ) && ( (year % 100 ) || !( year % 400 ) ))? 29:28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
};

eXo.ks.UIDateTimePicker = new UIDateTimePicker('UICalendarControl') ;