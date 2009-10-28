/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.common.bbcode.BBCode;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Jun 2, 2008 - 3:33:33 AM	
 */
public class Utils {
	
	public final static String TYPE_CATEGORY = "exo:forumCategory".intern();
	public final static String TYPE_FORUM = "exo:forum".intern();
	public final static String TYPE_TOPIC = "exo:topic".intern();
	public final static String USER_PROFILES_TYPE = "exo:forumUserProfile".intern() ;
	
	public final static String FORUM_SERVICE = "ForumService".intern() ;
	public final static String BANIP_HOME = "BanIPHome".intern() ;
	public final static String STATISTIC_HOME = "StatisticHome".intern() ;
	public final static String ADMINISTRATION_HOME = "AdministrationHome".intern() ;
	public final static String CATEGORY_HOME = "CategoryHome".intern() ;
	public final static String TAG_HOME = "TagHome".intern() ;
	
	public final static String FORUM_STATISTIC = "forumStatistic".intern() ;
	public final static String USER_PROFILE_HOME = "UserProfileHome".intern() ;
	public final static String USER_PROFILES = "UserProfile".intern() ;
	public final static String FORUM_BAN_IP = "forumBanIP".intern() ;
	public final static String FORUM_SUBSCRIOTION = "forumSubscription".intern() ;
	public final static String TOPIC_TYPE_HOME = "TopicTypeHome".intern() ;
	public final static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
	
	public final static String FORUMADMINISTRATION = "forumAdministration".intern() ;
	public final static String CATEGORY = "forumCategory".intern() ;
	public final static String FORUM = "forum".intern() ;
	public final static String TOPIC = "topic".intern() ;
	public final static String POST = "post".intern() ;
	public final static String POLL = "poll".intern() ;
	public final static String TAG = "tag".intern() ;
	public final static String TOPICTYPE = "topicType".intern() ;
	
	public final static String PRUNESETTING = "pruneSetting".intern() ;
	public final static String RECEIVE_MESSAGE = "receive".intern() ;
	public final static String SEND_MESSAGE = "send".intern() ;
	
	public static final String ADMIN = "Administrator".intern() ;
	public static final String MODERATOR = "Moderator".intern() ;
	public static final String USER = "User".intern() ;
	public static final String GUEST = "Guest".intern() ;
	
	public static final String ADMIN_ROLE = "ADMIN".intern() ;

	public static final String DEFAULT_EMAIL_CONTENT = "Hi ,</br> You have received this email because you registered for eXo Forum/Topic " +
			"Watching notification.<br/>We would like to inform that $OBJECT_WATCH_TYPE <b>$OBJECT_NAME</b> " +
			"has been added new $ADD_TYPE with content below: <div>_______________<br/>$POST_CONTENT<br/>_______________</div><div>At $TIME on $DATE, <b>$POSTER</b> posted</div> " +
			"For more detail, you can view at link : $LINK".intern();
	
	public static String getReplacementByBBcode(String s, List<BBCode> bbcodes) throws Exception {
		ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		s = getReplacementByBBcode(s, bbcodes, forumService);
		return s;
	}
	
	public static String getReplacementByBBcode(String s, List<BBCode> bbcodes, ForumService forumService) throws Exception {
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
								bbcode.setReplacement(forumService.getBBcode(bbcode.getId()).getReplacement());
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
								bbcode.setReplacement(forumService.getBBcode(bbcode.getId()).getReplacement());
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
	
	public static String removeCharterStrange(String s) {
		if (s == null || s.length() <= 0)
			return "";
		int i=0;
		StringBuilder builder = new StringBuilder();
		while(i < s.length()) {
			if(s.codePointAt(i) > 31){
				builder.append(s.charAt(i)) ;
			}
			++i;
		}
		return builder.toString();
	}
	
	static public class DatetimeComparatorDESC implements Comparator<Object> {
    public int compare(Object o1, Object o2) throws ClassCastException {
    	Date date1 = ((User) o1).getCreatedDate() ;
      Date date2  = ((User) o2).getCreatedDate() ;
      return date2.compareTo(date1) ;
    }
  }
	
	static public class DatetimeComparatorPostDESC implements Comparator<Post> {
		public int compare(Post o1, Post o2) throws ClassCastException {
			Date date1 = o2.getCreatedDate() ;
			Date date2  = o1.getCreatedDate() ;
			return date2.compareTo(date1) ;
		}
	}
	
	public static boolean isAddNewArray(String[] a, String[]b) {
		List<String> list = new ArrayList<String>();
		list = Arrays.asList(b);
		if(a.length == b.length) {
			for (int i = 0; i < a.length; i++) {
		    if(!list.contains(a[i])) {
		    	return true;
		    }
	    }
		} else {
			return true;
		}
	  return false;
  }

	public static boolean isAddNewList(List<String> a, List<String> b) {
		if(a.size() == b.size()) {
			for (String s : b) {
				if(!a.contains(s)) {
					return true;
				}
			}
		} else {
			return true;
		}
		return false;
	}
	
	
	public static String[] mapToArray(Map<String, String> map) {
		if (map.isEmpty()) return new String[]{" "};
		String[] strs = new String[map.size()];
		String str = map.toString().replace(" ", "").replace("{", "").replace("}", "");
		str = str.replace(",", ";").replace("=", ",");
		strs = str.split(";");
	  return strs;
  }
	
	public static Map<String, String> arrayToMap(String[] strs) {
		Map<String, String> map = new HashMap<String, String>();
		String[] arr;
		for (int i = 0; i < strs.length; i++) {
			arr = strs[i].split(",");
			if(arr.length == 2)
				map.put(arr[0], arr[1]);
    }
		return map;
  }
	
	public static String getQueryInList(List<String> list, String property) {
		StringBuilder builder = new StringBuilder();
		if(!list.isEmpty()) {
			int t = 0;
			for (String string : list) {
	      if(t == 0) builder.append("(").append(property).append("='").append(string).append("'");
	      else builder.append(" or ").append(property).append("='").append(string).append("'");
	      t = 1;
      }
			if(t == 1) builder.append(")");
		}
	  return builder.toString();
  }
	
	public static boolean isListContentItemList(List<String> list, List<String> list1) {
		if(list1.size() == 1 && list1.get(0).equals(" ")) return false;
		for (String string : list1) {
	    if(list.contains(string)) return true;
    }
	  return false;
  }
}
