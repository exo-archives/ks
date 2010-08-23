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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.jcr.Value;

import org.exoplatform.ks.common.jcr.KSDataLocation;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Apr 10, 2008, 4:40:22 PM
 */
public class Utils {
	final public static String FAQ_APP = "faqApp".intern() ;
	final public static String DEFAULT_AVATAR_URL = "/faq/skin/DefaultSkin/webui/background/Avatar1.gif";
	final public static String QUESTION_HOME = "questions".intern() ;
	final public static String CATEGORY_HOME = KSDataLocation.Locations.FAQ_CATEGORIES_HOME;
	final public static String ANSWER_HOME = "faqAnswerHome".intern();
	final public static String COMMENT_HOME = "faqCommentHome".intern();
	final public static String LANGUAGE_HOME = "languages".intern();
	final public static String EXO_FAQQUESTIONHOME = "exo:faqQuestionHome".intern() ;
	final public static String EXO_FAQCATEGORYHOME = "exo:faqCategoryHome".intern() ;
	final public static String ALL = "All".intern() ;
	final public static String CATEGORY_PREFIX = "categorySpace".intern(); 
	final public static String UI_FAQ_VIEWER = "UIFAQViewer".intern(); 
	
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
	 * Transforms a jcr Value array into a string array . 
	 * Calls {@link Value#getString()} on each item.
	 * @see javax.jcr.Value
	 * @param values array of values to transform
	 * @return string array for the Value array
	 * @throws Exception
	 */
	
	public static String[] valuesToArray(Value[] Val) throws Exception {
    if (Val.length < 1) return new String[] {};
    List<String> list = new ArrayList<String>();
    String s;
    for (int i = 0; i < Val.length; ++i) {
    	 s = Val[i].getString();
    	 if(s != null && s.trim().length() > 0) list.add(s);
    }
    return list.toArray(new String[list.size()]);
  }

	/**
	 * Transforms a jcr Value array into a string list . 
	 * Calls {@link Value#getString()} on each item.
	 * @see javax.jcr.Value
	 * @param values array of values to transform
	 * @return string list for the Value array
	 * @throws Exception
	 */
	
	public static List<String> valuesToList(Value[] values) throws Exception {
    List<String> list = new ArrayList<String>();
    if (values.length < 1) return list;
    String s;
    for (int i = 0; i < values.length; ++i) {
			s = values[i].getString();
			if (s != null && s.trim().length() > 0) list.add(s);
    }
    return list;
  }
	
	static public boolean hasPermission(List<String> listPlugin, List<String> listOfUser){
	  List<String> tem = new ArrayList<String>();
		for(String str : listOfUser){
			if(listPlugin.contains(str)) return true;
			if(str.contains("*")){
			  str = str.substring(str.indexOf("/"), str.length());
			  tem.add(str);
			  if(listPlugin.contains(str)) return true;
			}
		}
		
		for(String s : listPlugin){
		  if(tem.contains(s)) return true;
		}
		
		return false;
	}
	
	
	static public class NameComparator implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
    	String name1 = ((Watch) o1).getUser() ;
      String name2  = ((Watch) o2).getUser();
      return name1.compareToIgnoreCase(name2) ;
    }
  }
	
	static public String getStandardId(String s) {
		int i=0;
		StringBuilder builder = new StringBuilder();
		while(i < s.length()) {
			int t = s.codePointAt(i);
			if(t > 48 && t < 122){
				builder.append(s.charAt(i)) ;
			} else {
				builder.append("id") ;
			}
			++i;
		}
		return builder.toString();
  }
	
	static public String[] compareStr(String arr1[], String arr2[]) throws Exception {
		List<String> list = new ArrayList<String>();
		list.addAll(Arrays.asList(arr1));
		if(list.isEmpty() || list.get(0).equals(" ")) return new String[]{" "};
		for (int i = 0; i < arr2.length; i++) {
			if(!list.contains(arr2[i])) {
				list.add(arr2[i]);
			}
    }
		return list.toArray(new String[]{});
	}
	
	public static long getTimeOfLastActivity(String info) {
    if (info == null || info.length() == 0) return -1;
    int dashIndex = info.lastIndexOf("-");
    if (dashIndex < 0) return -1;    
    try {
      return Long.parseLong(info.substring(dashIndex + 1));
    } catch (NumberFormatException nfe) {
      return -1;
    }
  }
  
  public static String getAuthorOfLastActivity(String info) {
    if (info == null || info.length() == 0) return null;
    int dashIndex = info.lastIndexOf("-");
    if (dashIndex < 0) return null;
    return info.substring(0, dashIndex);
  }

}
