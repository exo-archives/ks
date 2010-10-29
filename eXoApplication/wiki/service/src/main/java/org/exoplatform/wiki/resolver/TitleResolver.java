/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.resolver;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.StringTokenizer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 7, 2010  
 */
public class TitleResolver {

  public static String getObjectId(String objectTitle, boolean isEncoded) throws UnsupportedEncodingException {
    if (objectTitle == null) {
      return null;
    }
    String title = objectTitle;
    if (isEncoded) {
      title = URLDecoder.decode(title, "UTF-8");
    }
    return encodeDecodeSpace(title, false);
  }

  public static String encodePlusSign(String encodedString) {
    return encodeDecodeSpace(encodedString, true);
  }

  private static String encodeDecodeSpace(String s, boolean isDecode) {
    String delim = " ";
    String udelim = "+";
    if (isDecode) {
      delim = "+";
      udelim = " ";
    }
    StringTokenizer st = new StringTokenizer(s, delim, false);
    StringBuilder sb = new StringBuilder();
    if (st.hasMoreElements()) {
      sb.append(st.nextElement());
    }
    while (st.hasMoreElements()) {
      sb.append(udelim).append(st.nextElement());
    }
    return sb.toString();
  }

}
