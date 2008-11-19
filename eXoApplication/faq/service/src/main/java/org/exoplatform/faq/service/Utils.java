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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.jcr.Value;


/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Apr 10, 2008, 4:40:22 PM
 */
public class Utils {
	/**
	 *  This method convert string to string[] split with comma
	 * @param str
	 * @return string[] 
	 * @throws Exception
	 */
	public static String[] splitForFAQ (String str) throws Exception {
		if(str != null && str.length() > 0) {
			if(str.contains(",")) return str.trim().split(",") ;
			else return str.trim().split(";") ;
		} else return new String[] {} ;
	}
	
	/**
	 * This method sort list category is date ascending
	 * @author Administrator
	 *
	 */
	static public class DatetimeComparatorASC implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
    	Date date1 = ((Category) o1).getCreatedDate() ;
      Date date2  = ((Category) o2).getCreatedDate() ;
      return date1.compareTo(date2) ;
    }
  }
	/**
	 * This method sort list category is name ascending
	 * @author Administrator
	 *
	 */
	static public class NameComparatorASC implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
    	String name1 = ((Category) o1).getName() ;
      String name2  = ((Category) o2).getName() ;
      return name1.compareToIgnoreCase(name2) ;
    }
  }
	
	/**
	 * This method sort list category is date descending
	 * @author Administrator
	 *
	 */
	static public class DatetimeComparatorDESC implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
    	Date date1 = ((Category) o1).getCreatedDate() ;
      Date date2  = ((Category) o2).getCreatedDate() ;
      return date2.compareTo(date1) ;
    }
  }
	/**
	 * This method sort list category is name descending
	 * @author Administrator
	 *
	 */
	static public class NameComparatorDESC implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
    	String name1 = ((Category) o1).getName() ;
      String name2  = ((Category) o2).getName() ;
      return name2.compareToIgnoreCase(name1) ;
    }
  }
	
	/**
	 * This method return value[] to list String
	 * @param values
	 * @return list string
	 * @throws Exception
	 */
	static public List<String> ValuesToList(Value[] values) throws Exception {
		List<String> list = new ArrayList<String>() ;
		if(values.length < 1) return list ;
		if(values.length == 1) {
			list.add(values[0].getString()) ;
			return list ;
		}
		for(int i = 0; i < values.length; ++i) {
			list.add(values[i].getString() );
		}
		return list;
	}
	
	static public class NameComparator implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
    	String name1 = ((Watch) o1).getUser() ;
      String name2  = ((Watch) o2).getUser();
      return name1.compareToIgnoreCase(name2) ;
    }
  }
}
