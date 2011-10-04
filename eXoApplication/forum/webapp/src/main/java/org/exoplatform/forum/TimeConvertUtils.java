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

import java.util.Date;


/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jul 5, 2011  
 */
public class TimeConvertUtils extends org.exoplatform.webui.utils.TimeConvertUtils {

  public static String convertXTimeAgo(Date myDate, String format, long zoneTime) {
    if (!ForumUtils.isEmpty(format))
      format = format.replaceAll("D", "E");
    long day = 24 * 60 * 60 * 1000;
    if ((getGreenwichMeanTime().getTimeInMillis() - myDate.getTime()) < (31l * day)) {
      return convertXTimeAgo(myDate, format, MONTH);
    } else {
      Date date = new Date();
      date.setTime(myDate.getTime() - zoneTime);
      return getFormatDate(date, format);
    }
  }

  public static String getFormatDate(String format, Date myDate) {
    if (!ForumUtils.isEmpty(format)) format = format.replaceAll("D", "E");
    return getFormatDate(myDate, format);
  }
}
