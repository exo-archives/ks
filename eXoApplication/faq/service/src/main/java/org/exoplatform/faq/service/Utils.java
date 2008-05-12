/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.service;

import java.util.Comparator;
import java.util.Date;


/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Apr 10, 2008, 4:40:22 PM
 */
public class Utils {
	public static final String KEY_FAQ_SETTING = "FAQSetting".intern() ;
	
	public static final String EXO_FAQ_SETTING = "exo:faqSetting".intern();
	public static final String EXO_PROCESSING_MODE = "exo:processingMode".intern() ;
	public static final String EXO_DISPLAY_TYPE = "exo:displayType".intern() ;
	
	static public class DatetimeComparator implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
    	Date date1 = ((Category) o1).getCreatedDate() ;
      Date date2  = ((Category) o2).getCreatedDate() ;
      return date1.compareTo(date2) ;
    }
  }
	
	static public class NameComparator implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
    	String name1 = ((Category) o1).getName() ;
      String name2  = ((Category) o2).getName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }
}
