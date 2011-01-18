Index: web/ksResources/src/main/webapp/javascript/eXo/ks/UIDateTimePicker.js
===================================================================
--- web/ksResources/src/main/webapp/javascript/eXo/ks/UIDateTimePicker.js	(revision 65245)
+++ web/ksResources/src/main/webapp/javascript/eXo/ks/UIDateTimePicker.js	(working copy)
@@ -5,15 +5,38 @@
   														// if selectedDate is invalid, currentDate deals with system time;
   this.selectedDate = null ; //Datetime value of input date&time field
   this.months = ['January','February','March','April','May','June','July','August','September','October','November','December'] ;
+  this.weekdays = ['S','M','T','W','T','F','S'] ;
+  this.tooltip = ['Previous Year', 'Previous Month', 'Next Month', 'Next Year'];
+  this.pathResource = "/ksResources/javascript/eXo/ks/lang/";
+  this.lang = "";
 }
 
+UIDateTimePicker.prototype.getLang = function() {
+       try {
+               var lang = this.dateField.getAttribute('lang'); 
+               if (this.lang == lang) 
+                       return;
+               this.lang = lang;
+               var languages = eval(ajaxAsyncGetRequest(this.pathResource + this.lang.toLowerCase() + ".js", false));
+               if (!languages || (typeof(languages) != "object")) 
+                       return;
+       this.months = languages[0];
+       this.weekdays = languages[1];
+       this.tooltip = languages[2];
+       } 
+       catch (e) {}
+} ;
+
 UIDateTimePicker.prototype.init = function(field, isDisplayTime) {
 	this.isDisplayTime = isDisplayTime ;
 	if (this.dateField) {
 		this.dateField.parentNode.style.position = '' ;
 	}
 	this.dateField = field ;
-	if (!document.getElementById(this.calendarId)) this.create() ;
+        eXo.ks.UIDateTimePicker.getLang() ;
+        if (!document.getElementById(this.calendarId)) {
+               this.create() ;
+        }
 //	field.parentNode.style.position = 'relative' ;
   field.parentNode.insertBefore(document.getElementById(this.calendarId), field) ;
   this.show() ;
@@ -34,7 +57,7 @@
 UIDateTimePicker.prototype.show = function() {
 	document.onmousedown = new Function('eXo.ks.UIDateTimePicker.hide()') ;
 	var re = /^(\d{1,2}\/\d{1,2}\/\d{1,4})\s*(\s+\d{1,2}:\d{1,2}:\d{1,2})?$/i ;
-  this.selectedDate = new Date() ;
+        this.selectedDate = new Date() ;
 	if (re.test(this.dateField.value)) {
 	  var dateParts = this.dateField.value.split(" ") ;
 	  var arr = dateParts[0].split("/") ;
@@ -99,17 +122,17 @@
 	table += 		'<div class="UICalendar" onmousedown="event.cancelBubble = true">' ;
 	table += 		'	<table class="MonthYearBox">' ;
 	table += 		'	  <tr>' ;
-	table += 		'			<td class="MonthButton"><a class="PreviousMonth" href="javascript:eXo.ks.UIDateTimePicker.changeMonth(-1);"></a></td>' ;
-	table += 		'			<td class="YearButton"><a class="PreviousYear" href="javascript:eXo.ks.UIDateTimePicker.changeYear(-1);"></a></td>' ;
+	table += 		'			<td class="MonthButton"><a class="PreviousMonth" title="' + this.tooltip[1]+ '" href="javascript:eXo.ks.UIDateTimePicker.changeMonth(-1);"></a></td>' ;
+	table += 		'			<td class="YearButton"><a class="PreviousYear" title="' + this.tooltip[0]+ '" href="javascript:eXo.ks.UIDateTimePicker.changeYear(-1);"></a></td>' ;
 	table += 		'			<td><font color="#f89302">' + this.months[this.currentDate.getMonth()] + '</font> - ' + this.currentDate.getFullYear() + '</td>' ;
-	table += 		'			<td class="YearButton"><a class="NextYear" href="javascript:eXo.ks.UIDateTimePicker.changeYear(1);"></a></td>' ;
-	table += 		'			<td class="MonthButton"><a class="NextMonth" href="javascript:eXo.ks.UIDateTimePicker.changeMonth(1);"></a></td>' ;
+	table += 		'			<td class="YearButton"><a class="NextYear" title="' + this.tooltip[3]+ '" href="javascript:eXo.ks.UIDateTimePicker.changeYear(1);"></a></td>' ;
+	table += 		'			<td class="MonthButton"><a class="NextMonth" title="' + this.tooltip[2]+ '" href="javascript:eXo.ks.UIDateTimePicker.changeMonth(1);"></a></td>' ;
 	table += 		'		</tr>' ;
 	table += 		'	</table>' ;
 	table += 		'	<div style="margin-top: 6px;padding: 0px 5px;">' ;
 	table += 		'		<table>' ;
 	table += 		'			<tr>' ;
-	table += 		'				<td><font color="red">S</font></td><td>M</td><td>T</td><td>W</td><td>T</td><td>F</td><td>S</td>' ;
+	table += 		'				<td><font color="red">' + this.weekdays[0] + '</font></td><td>' + this.weekdays[1] + '</td><td>' + this.weekdays[2] + '</td><td>' + this.weekdays[3] + '</td><td>' + this.weekdays[4] + '</td><td>' + this.weekdays[5] + '</td><td>' + this.weekdays[6] + '</td>' ;
 	table += 		'			</tr>' ;
 	table += 		'		</table>' ;
 	table += 		'	</div>' ;
@@ -163,8 +186,8 @@
 UIDateTimePicker.prototype.changeMonth = function(change) {
 	this.currentDate.setDate(1);
 	this.currentDate.setMonth(this.currentDate.getMonth() + change) ;
-  var clndr = document.getElementById(this.calendarId) ;
-  clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
+	var clndr = document.getElementById(this.calendarId) ;
+	clndr.firstChild.lastChild.innerHTML = this.renderCalendar() ;
 };
 
 UIDateTimePicker.prototype.changeYear = function(change) {
@@ -268,14 +291,4 @@
 	return [31, ((!(year % 4 ) && ( (year % 100 ) || !( year % 400 ) ))? 29:28), 31, 30, 31, 30, 31, 31, 30, 31, 30, 31][month];
 };
 
-//UIDateTimePicker.prototype.getChangedTime = function(input, type) {
-//	var time = input.value ;
-//	if (isNaN(time)) {
-//		return ; 
-//	}
-//	if (type == 'h') this.currentHours = time ;
-//	else if (type == 'm') this.currentMinutes = time ;
-//	else if (type == 's') this.currentSeconds = time ;
-//}
-
-eXo.ks.UIDateTimePicker = new UIDateTimePicker('UICalendarControl') ;
\ No newline at end of file
+eXo.ks.UIDateTimePicker = new UIDateTimePicker('UICalendarControl') ;
