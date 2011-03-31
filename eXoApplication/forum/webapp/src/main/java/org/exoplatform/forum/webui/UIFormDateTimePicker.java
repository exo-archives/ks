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
import java.util.Locale;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SARL
 * Author : Tran The Trong
 *          trongtt@gmail.com
 * Jul 14, 2006  
 * 
 * A date picker element
 */
public class UIFormDateTimePicker extends UIFormInputBase<String> {
  /**
   * The DateFormat
   */
  private String  titleShowCalendar = "Show Calendar";

  private String  dateStyle_        = "MM/dd/yyyy";

  private String  timeStyle_        = "HH:mm:ss";

  private Date    date_;

  private boolean isDisplayTime_;

  private Locale  locale_;

  public UIFormDateTimePicker(String name, String bindField, Date date, boolean isDisplayTime) {
    super(name, bindField, String.class);
    date_ = date;
    isDisplayTime_ = isDisplayTime;
    if (date != null)
      value_ = getFormater().format(date);
    if (date != null)
      value_ = getFormater().format(date);
  }

  public UIFormDateTimePicker(String name, String bindField, Date date, boolean isDisplayTime, Locale locale) {
    super(name, bindField, String.class);
    date_ = date;
    isDisplayTime_ = isDisplayTime;
    locale_ = locale;
    if (date != null)
      value_ = getFormater().format(date);
    if (date != null)
      value_ = getFormater().format(date);
  }

  public UIFormDateTimePicker(String name, String bindField, Date date, boolean isDisplayTime, String titleShowCalendar, Locale locale, String dateStyle) {
    super(name, bindField, String.class);
    dateStyle_ = dateStyle;
    isDisplayTime_ = isDisplayTime;
    locale_ = locale;
    date_ = date;
    this.titleShowCalendar = titleShowCalendar;
    if (date != null)
      value_ = getFormater().format(date);
  }

  public UIFormDateTimePicker(String name, String bindField, Date date, boolean isDisplayTime, String titleShowCalendar, Locale locale, String dateStyle, String timeStyle) {
    super(name, bindField, String.class);
    dateStyle_ = dateStyle;
    timeStyle_ = timeStyle;
    locale_ = locale;
    date_ = date;
    isDisplayTime_ = isDisplayTime;
    this.titleShowCalendar = titleShowCalendar;
    if (date != null)
      value_ = getFormater().format(date);
  }

  public UIFormDateTimePicker(String name, String bindField, Date date) {
    this(name, bindField, date, true);
  }

  public UIFormDateTimePicker(String name, String bindField, Date date, String dateStyle) {
    this(name, bindField, date, false, "Show Calendar", null, dateStyle);
  }

  public UIFormDateTimePicker(String name, String bindField, Date date, String dateStyle, String timeStyle) {
    this(name, bindField, date, true, "Show Calendar", null, dateStyle, timeStyle);
  }

  public void setDisplayTime(boolean isDisplayTime) {
    isDisplayTime_ = isDisplayTime;
  }

  public void setTitleShowCalendar(String titleShowCalendar) {
    this.titleShowCalendar = titleShowCalendar;
  }

  public void setCalendar(Calendar date) {
    date_ = date.getTime();
    value_ = getFormater().format(date.getTime());
  }

  public Calendar getCalendar() {
    try {
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(getFormater().parse(value_ + " 0:0:0"));
      return calendar;
    } catch (ParseException e) {
      return null;
    }
  }

  public Date getDateValue() {
    try {
      Calendar calendar = new GregorianCalendar();
      calendar.setTime(getFormater().parse(value_ + " 0:0:0"));
      return calendar.getTime();
    } catch (ParseException e) {
      return null;
    }
  }

  public void setDateFormatStyle(String dateStyle) {
    dateStyle_ = dateStyle;
    value_ = getFormater().format(date_);

  }

  public void setTimeFormatStyle(String timeStyle) {
    timeStyle_ = timeStyle;
    value_ = getFormater().format(date_);
  }

  @SuppressWarnings("unused")
  public void decode(Object input, WebuiRequestContext context) throws Exception {
    if (input != null)
      value_ = ((String) input).trim();
  }

  public String getFormatStyle() {
    if (isDisplayTime_)
      return dateStyle_ + " " + timeStyle_;
    return dateStyle_;
  }

  private int getFirstDayOfWeek() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    Locale locale = context.getParentAppRequestContext().getLocale();
    Calendar calendar = GregorianCalendar.getInstance(locale);
    return calendar.getFirstDayOfWeek();
  }
  
  private String getLang() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    Locale locale = context.getParentAppRequestContext().getLocale();
    return locale.getLanguage();
  }

  private DateFormat getFormater() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    Locale locale = context.getParentAppRequestContext().getLocale();
    if (locale_ == null)
      locale_ = locale;
    return new SimpleDateFormat(getFormatStyle(), locale_);
  }

  public void setInfo(String titleShowCalendar, Locale locale, String dateStyle, String timeStyle) {
    this.titleShowCalendar = titleShowCalendar;
    dateStyle_ = dateStyle;
    timeStyle_ = timeStyle;
    locale_ = locale;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.ks.UIDateTimePicker", "/ksResources/javascript/");
    Writer w = context.getWriter();
    w.write("<input type='text' class='DateTimeInput'");
    w.write("name='");
    w.write(getName());
    w.write('\'');
    if (value_ != null && value_.length() > 0) {
      w.write(" value='");
      w.write(value_.toString());
      w.write('\'');
    }
    w.write("/>");
    w.write("<div class='CalendarIcons' lang='" + getLang() + "' fistweekday='" + getFirstDayOfWeek() + "' format='" + getFormatStyle() + "' onclick='eXo.ks.UIDateTimePicker.init(this,");
    w.write(String.valueOf(isDisplayTime_) + ");' title='" + titleShowCalendar + "'><span></span></div>");
  }
}
