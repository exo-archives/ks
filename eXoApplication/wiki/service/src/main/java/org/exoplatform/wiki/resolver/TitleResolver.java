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

  public static String getPageId(String pageTitle, boolean isEncoded) throws UnsupportedEncodingException{
    //TODO: replace space by "+" when update to Gatein 3.0.1-GA to like Confluence 
    //following implement like Creole and Xwiki 2.0
    if(pageTitle == null){
      return null;
    }
    String title = pageTitle;
    if(isEncoded){
      title = URLDecoder.decode(title, "UTF-8");
    }
    return removeSpaces(title);
  }
  
  private static String removeSpaces(String s) {
    StringTokenizer st = new StringTokenizer(s," ",false);
    StringBuilder sb = new StringBuilder();
    while (st.hasMoreElements()) sb.append(st.nextElement());
    return sb.toString();
  }

  
}
