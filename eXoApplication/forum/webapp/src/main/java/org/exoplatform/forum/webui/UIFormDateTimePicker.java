/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.webui;

import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *					trongtt@gmail.com
 * Jul 14, 2006	
 * 
 * A date picker element
 */
public class UIFormDateTimePicker extends UIFormInputBase<String> {
	/**
	 * The DateFormat
	 */
	private DateFormat dateFormat ;
	/**
	 * Whether to display the full time (with hours, minutes and seconds), not only the date
	 */
	private boolean isDisplayTime_ ;
	private String titleShowCalendar = "Show Calendar";
	public UIFormDateTimePicker(String name, String bindField, Date date, boolean isDisplayTime, String titleShowCalendar) {
		super(name, bindField, String.class) ;
		setDisplayTime(isDisplayTime) ;
		if(date != null) value_ = dateFormat.format(date) ;
		setTitleShowCalendar(titleShowCalendar);
	}
	
	public void setTitleShowCalendar(String titleShowCalendar) {
		this.titleShowCalendar = titleShowCalendar;
	}
	public UIFormDateTimePicker(String name, String bindField, Date date) {
		this(name, bindField, date, true, "") ;
	}
	/**
	 * By default, creates a date of format Month/Day/Year
	 * If isDisplayTime is true, adds the time of format Hours:Minutes:Seconds
	 * TODO : Display time depending on the locale of the client.
	 * @param isDisplayTime
	 */
	public void setDisplayTime(boolean isDisplayTime) {
		isDisplayTime_ = isDisplayTime;
		if(isDisplayTime_) dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		else dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	}
	
	public void setCalendar(Calendar date) { value_ = dateFormat.format(date.getTime()) ; }
	public Calendar getCalendar() {
		try {
			Calendar calendar = new GregorianCalendar() ;
			calendar.setTime(dateFormat.parse(value_ + " 0:0:0")) ;
			return calendar ;
		} catch (ParseException e) {
			return null;
		}
	}
	
	public void decode(Object input, WebuiRequestContext context) throws Exception {
		if(input != null) value_ = ((String)input).trim();
	}

	public void processRender(WebuiRequestContext context) throws Exception {
		context.getJavascriptManager().importJavascript("eXo.ks.UIDateTimePicker","/ksResources/javascript/") ;
		Writer w = context.getWriter();
		w.write("<input type='text' class='DateTimeInput'") ;
		w.write("name='") ;
		w.write(getName()) ; w.write('\'') ;
		if(value_ != null && value_.length() > 0) {			
			w.write(" value='"); w.write(value_); w.write('\'');
		}
		w.write("/>");
		w.write("<div class='CalendarIcons' onclick='eXo.ks.UIDateTimePicker.init(this,");
		w.write(String.valueOf(isDisplayTime_)+");' title='"+titleShowCalendar+"'><span></span></div>");
	}
}
