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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.commons;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.exoplatform.portal.webui.util.Util;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 5 Jan 2011  
 */
public class DateTimeFomatter {
  public static String getLongFormatted(Date date) {
    Locale currentLocale = Util.getPortalRequestContext().getLocale();
    SimpleDateFormat df = new SimpleDateFormat("MMMMM dd, yyyy hh:mm aaa", currentLocale);
    return df.format(date);
  }
}
