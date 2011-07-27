/***************************************************************************
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
 ***************************************************************************/
/**
 * 
 */

package org.exoplatform.poll;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;

/**
 * Created by The eXo Platform SARL 
 * Author : Vu Duy Tu 
 *          tu.duy@exoplatform.com 
 * June 24, 2010 5:35:54 PM
 */

public class Utils {
  public static final String POLL         = "Poll".intern();

  public static final String POLL_ID_SHOW = "pollIdShow".intern();

  public static final int    MAXSIGNATURE = 300;

  public static final int    MAXTITLE     = 100;

  public static final long   MAXMESSAGE   = 10000;

  public static String getFormatDate(String format, Date myDate) {
    /*
     * h,hh,H, m, mm, d, dd, DDD, DDDD, M, MM, MMM, MMMM, yy, yyyy
     */
    if (myDate == null)
      return "";
    if (!isEmpty(format)) {
      if (format.indexOf("DDDD") >= 0)
        format = format.replaceAll("DDDD", "EEEE");
      if (format.indexOf("DDD") >= 0)
        format = format.replaceAll("DDD", "EEE");
    }
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    Locale locale = new Locale(portalContext.getLocale().getLanguage(), portalContext.getLocale().getCountry());
    Format formatter = new SimpleDateFormat(format, locale);
    return formatter.format(myDate);
  }

  public static boolean isValidEmailAddresses(String addressList) throws Exception {
    if (isEmpty(addressList))
      return true;
    addressList = StringUtils.remove(addressList, " ");
    addressList = StringUtils.replace(addressList, ";", ",");
    try {
      InternetAddress[] iAdds = InternetAddress.parse(addressList, true);
      String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}";
      for (int i = 0; i < iAdds.length; i++) {
        if (!iAdds[i].getAddress().matches(emailRegex))
          return false;
      }
    } catch (AddressException e) {
      return false;
    }
    return true;
  }

  public static boolean isEmpty(String str) {
    if (str == null || str.trim().length() == 0)
      return true;
    else
      return false;
  }

  public static String removeZeroFirstNumber(String str) {
    if (!isEmpty(str)) {
      str = str.trim();
      StringBuilder s = new StringBuilder();
      int i = 0;
      while ((i + 1) < str.length() && (str.charAt(i) == '0' || str.charAt(i) == ' ')) {
        s.append(str.charAt(i));
        ++i;
      }
      str = str.replaceFirst(s.toString(), "");
    }
    return str;
  }

  public static String getSubString(String str, int max) {
    if (!isEmpty(str)) {
      int l = str.length();
      if (l > max) {
        str = str.substring(0, max);
        int space = str.lastIndexOf(" ");
        if (space > (max - 6))
          str = str.substring(0, space) + "...";
        else
          str = str + "...";
      }
    }
    return str;
  }

  public static String[] getColor() {
    return new String[] { "blue", "DarkGoldenRod", "green", "yellow", "BlueViolet", "orange", "darkBlue", "IndianRed", "DarkCyan", "lawnGreen" };
  }

  public static String getExpire(long timeOut, Date modifiDate, String[] dateUnit) {
    if (timeOut == 0)
      return dateUnit[0];
    else {
      Calendar calendar = CommonUtils.getGreenwichMeanTime();
      long timeEnd = (timeOut < 1000) ? (modifiDate.getTime() + timeOut * 86400000) : timeOut;
      long l = timeEnd - calendar.getTimeInMillis();
      if (l < 0)
        return dateUnit[1];
      long m = (long) l / 60000;
      if (m > 60) {
        long h = (long) m / 60;
        if (h > 24) {
          long d = (long) h / 24;
          return d + " " + dateUnit[2] + ", " + (h - d * 24) + " " + dateUnit[3] + ", " + (m - h * 60) + " " + dateUnit[4];
        } else {
          return h + " " + dateUnit[3] + ", " + (m - h * 60) + dateUnit[4];
        }
      }
      return m + " " + dateUnit[4];
    }
  }
}
