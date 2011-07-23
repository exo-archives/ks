/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.forum;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jul 5, 2011  
 */
public class TimeConvertUtils {
  public static String[] strs = new String[] { "SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "YEAR", "DECADE" };
  private static String[] timeLength = new String[] { "60000", "60", "24", "7", "4.35", "12", "10", "10" };
  private static String JUSTNOW = "JUSTNOW";
  private static String SPACE = " ";

  public static Calendar getInstanceTempCalendar() {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setLenient(false);
    int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset);
    return calendar;
  }

  public static String convertDateTime(Date dateInput) throws Exception {
    float delta = (getInstanceTempCalendar().getTimeInMillis() - dateInput.getTime());
    int i = 0;
    for (i = 0; (delta >= Float.parseFloat(timeLength[i])) && i < timeLength.length - 1; i++) {
      delta = delta / Float.parseFloat(timeLength[i]);
    }
    long l = (long) delta;
    if (l < 0 || i < 1) {
      return JUSTNOW;
    }
    return new StringBuilder().append(l).append(SPACE).append(strs[i])
                              .append((l > 1) ? "S" : "").toString();
  }
  
  public static String convertDateTimeResourceBundle(Date dateInput, String format) {
    try {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      ResourceBundle res = context.getApplicationResourceBundle();
      String []strs = convertDateTime(dateInput).split(SPACE);
      if(strs[0].equals(JUSTNOW)) return res.getString("UIForumPortlet.timeFormat.JUSTNOW");
      else {
        return new StringBuilder(strs[0]).append(SPACE)
                                  .append(res.getString("UIForumPortlet.timeFormat."+strs[1])).toString();
      }
    } catch (Exception e) {
      return getFormatDate(format, dateInput);
    }
  }
  
  public static String getFormatDate(String format, Date myDate) {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    Locale locale = new Locale(portalContext.getLocale().getLanguage(), portalContext.getLocale().getCountry());
    return getFormatDateLocale(format, myDate, locale);
  }

  public static String getFormatDateLocale(String format, Date myDate, Locale locale) {
    /*
     * h,hh,H, m, mm, d, dd, DDD, DDDD, M, MM, MMM, MMMM, yy, yyyy
     */
    if (myDate == null)
      return ForumUtils.EMPTY_STR;
    if (!ForumUtils.isEmpty(format)) {
      if (format.indexOf("DDDD") >= 0)
        format = format.replaceAll("DDDD", "EEEE");
      if (format.indexOf("DDD") >= 0)
        format = format.replaceAll("DDD", "EEE");
    }
    Format formatter = new SimpleDateFormat(format, locale);
    return formatter.format(myDate);
  }
}
