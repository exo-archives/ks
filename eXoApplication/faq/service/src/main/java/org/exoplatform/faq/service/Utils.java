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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;


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
	final public static String SETTING_HOME = "settingHome".intern() ;
	final public static String USER_SETTING_HOME = "userSettingHome".intern() ;
	final public static String CATEGORY_HOME = "categories".intern() ;
	final public static String TEMPLATE_HOME = "templateHome".intern() ;
	final public static String ANSWER_HOME = "faqAnswerHome".intern();
	final public static String COMMENT_HOME = "faqCommentHome".intern();
	final public static String LANGUAGE_HOME = "languages".intern();
	final public static String BBCODE = "faqBbcode".intern() ;
	public final static String FAQ_BBCODE = "faqBBCode".intern() ;
	final public static String EXO_FAQQUESTIONHOME = "exo:faqQuestionHome".intern() ;
	final public static String EXO_FAQCATEGORYHOME = "exo:faqCategoryHome".intern() ;
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
	
	public static String getReplacementByBBcode(String s, List<BBCode> bbcodes) throws Exception {
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		s = getReplacementByBBcode(s, bbcodes, faqService);
		return s;
	}
	
	public static String getReplacementByBBcode(String s, List<BBCode> bbcodes, FAQService faqService) throws Exception {
		int lastIndex = 0, tagIndex = 0, clsIndex = 0;
		String start, end, bbc, str="", param, option;
		for (BBCode bbcode : bbcodes) {
			bbc = bbcode.getTagName();
			if(bbc.equals("URL")){
				s = StringUtils.replace(s, "[link", "[URL");
				s = StringUtils.replace(s, "[/link]", "[/URL]");
				s = StringUtils.replace(s, "[LINK", "[URL");
				s = StringUtils.replace(s, "[/LINK]", "[/URL]");
			}
			bbc = bbc.toLowerCase();
			if(!bbc.equals("list")){
				lastIndex = 0; tagIndex = 0;
				if(bbcode.isOption()){
					start = "[" + bbc + "=";
					end = "[/" + bbc + "]";
					s = StringUtils.replace(s, start.toUpperCase(), start);
					s = StringUtils.replace(s, end.toUpperCase(), end);
					while ((tagIndex = s.indexOf(start, lastIndex)) != -1) {
						lastIndex = tagIndex + 1;
						try {
							clsIndex = s.indexOf(end, tagIndex);
							str = bbcode.getReplacement();
							if(str == null || str.trim().length() == 0 || str.equals("null")) {
								bbcode.setReplacement(faqService.getBBcode(bbcode.getId()).getReplacement());
							}
							str = s.substring(tagIndex + start.length(), clsIndex);
							option = str.substring(0, str.indexOf("]"));
							if(option.indexOf("+")==0)option = option.replaceFirst("+", "");
							if(option.indexOf("\"")==0)option = option.replaceAll("\"", "");
							if(option.indexOf("&quot;")==0)option = option.replaceAll("&quot;", "");
							param = str.substring(str.indexOf("]")+1);
							param = StringUtils.replace(bbcode.getReplacement(), "{param}", param);
							param = StringUtils.replace(param, "{option}", option.trim());
							s = StringUtils.replace(s, start + str + end, param);
						} catch (Exception e) {
							continue;
						}
					}
				} else {
					start = "[" + bbc + "]";
					end = "[/" + bbc + "]";
					s = StringUtils.replace(s, start.toUpperCase(), start);
					s = StringUtils.replace(s, end.toUpperCase(), end);
					while ((tagIndex = s.indexOf(start, lastIndex)) != -1) {
						lastIndex = tagIndex + 1;
						try {
							clsIndex = s.indexOf(end, tagIndex);
							str = bbcode.getReplacement();
							if(str == null || str.trim().length() == 0 || str.equals("null")) {
								bbcode.setReplacement(faqService.getBBcode(bbcode.getId()).getReplacement());
							}
							str = s.substring(tagIndex + start.length(), clsIndex);
							param = StringUtils.replace(bbcode.getReplacement(), "{param}", str);
							s = StringUtils.replace(s, start + str + end, param);
						} catch (Exception e) {
							continue;
						}
					}
				}
	    } else {
	    	lastIndex = 0;
	  		tagIndex = 0;
	  		s = StringUtils.replace(s, "[LIST", "[list");
	  		s = StringUtils.replace(s, "[/LIST]", "[/list]");
	  		while ((tagIndex = s.indexOf("[list]", lastIndex)) != -1) {
	  			lastIndex = tagIndex + 1;
	  			try {
	  				clsIndex = s.indexOf("[/list]", tagIndex);
	  				str = s.substring(tagIndex + 6, clsIndex);
	  				String str_ =  "";
	  				str_ = StringUtils.replaceOnce(str, "[*]", "<li>");
	  				str_ = StringUtils.replace(str_, "[*]", "</li><li>");
	  				if(str_.lastIndexOf("</li><li>") > 0) {
	  					str_ = str_ + "</li>";
	  				}
	  				if(str_.indexOf("<br/>") >= 0) {
	  					str_ = StringUtils.replace(str_, "<br/>", "");
	  				}
	  				if(str_.indexOf("<p>") >= 0) {
	  					str_ = StringUtils.replace(str_, "<p>", "");
	  					str_ = StringUtils.replace(str_, "</p>", "");
	  				}
	  				s = StringUtils.replace(s, "[list]" + str + "[/list]", "<ul>" + str_ + "</ul>");
	  			} catch (Exception e) {
	  				continue;
	  			}
	  		}
	  		
	  		lastIndex = 0;
	  		tagIndex = 0;
	  		while ((tagIndex = s.indexOf("[list=", lastIndex)) != -1) {
	  			lastIndex = tagIndex + 1;
	  			
	  			try {
	  				clsIndex = s.indexOf("[/list]", tagIndex);
	  				String content = s.substring(tagIndex + 6, clsIndex);
	  				int clsType = content.indexOf("]");
	  				String type = content.substring(0, clsType);
	  				type.replaceAll("\"", "").replaceAll("'", "");
	  				str = content.substring(clsType + 1);
	  				String str_ =  "";
	  				str_ = StringUtils.replaceOnce(str, "[*]", "<li>");
	  				str_ = StringUtils.replace(str_, "[*]", "</li><li>");
	  				if(str_.lastIndexOf("</li><li>") > 0) {
	  					str_ = str_ + "</li>";
	  				}
	  				if(str_.indexOf("<br/>") >= 0) {
	  					str_ = StringUtils.replace(str_, "<br/>", "");
	  				}
	  				if(str_.indexOf("<p>") >= 0) {
	  					str_ = StringUtils.replace(str_, "<p>", "");
	  					str_ = StringUtils.replace(str_, "</p>", "");
	  				}
	  				s = StringUtils.replace(s, "[list=" + content + "[/list]", "<ol type=\""+type+"\">" + str_ + "</ol>");
	  			} catch (Exception e) {
	  				continue;
	  			}
	  		}
	    }
		}
		return s;
	}

	public static String convertCodeHTML(String s, List<String> bbcs) {
		if (s == null || s.length() <= 0)
			return "";
		s = s.replaceAll("(<p>((\\&nbsp;)*)(\\s*)?</p>)|(<p>((\\&nbsp;)*)?(\\s*)</p>)", "<br/>").trim();
		s = s.replaceFirst("(<br/>)*", "");
		s = s.replaceAll("(\\w|\\$)(>?,?\\.?\\*?\\!?\\&?\\%?\\]?\\)?\\}?)(<br/><br/>)*", "$1$2");
		try {
			List<BBCode> bbcodes = new ArrayList<BBCode>();
			BBCode bbcode;
			for (String string : bbcs) {
	      bbcode = new BBCode();
	      if(string.indexOf("=") >= 0){
	      	bbcode.setOption(true);
    			string = string.replaceFirst("=", "");
    			bbcode.setId(string+"_option");
    		}else {
    			bbcode.setId(string);
    		}
	      bbcode.setTagName(string);
	      bbcodes.add(bbcode);
      }
			s = getReplacementByBBcode(s, bbcodes);
			s = s.replaceAll("(https?|ftp)://", " $0").replaceAll("(=\"|=\'|\'>|\">)( )(https?|ftp)", "$1$3")
					 .replaceAll("[^=\"|^=\'|^\'>|^\">](https?://|ftp://)([-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", "<a target=\"_blank\" href=\"$1$2\">$1$2</a>");
			s = s.replaceAll("&apos;", "'");
    } catch (Exception e) {
    	return "";
    }
		return s;
	}
}
