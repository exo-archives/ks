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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
/**
 * 
 */

package org.exoplatform.forum;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Dec 21, 2007 5:35:54 PM 
 */

public class ForumFormatUtils {
	@SuppressWarnings("deprecation")
  public static String getFormatDate(String format, Date myDate) {
		/*h,hh,H, m, mm, D, DD, DDD, DDDD, M, MM, MMM, MMMM, yy, yyyy
		 * */
		String strCase = "" ;
		int day = myDate.getDay() ;
		switch (day) {
    case 0:
    	strCase = "Sunday" ;
	    break;
    case 1:
    	strCase = "Monday" ;
    	break;
    case 2:
    	strCase = "Tuesday" ;
    	break;
    case 3:
    	strCase = "Webnesday" ;
    	break;
    case 4:
    	strCase = "Thursday" ;
    	break;
    case 5:
    	strCase = "Friday" ;
    	break;
    case 6:
    	strCase = "Saturday" ;
    	break;
    default:
	    break;
    }
		String form = "temp" + format ;
		if(form.indexOf("DDDD") > 0) {
			Format formatter = new SimpleDateFormat(form.substring(form.indexOf("DDDD") + 5));
			return strCase + ", "  + formatter.format(myDate).replaceAll(",", ", ");
		} else if(form.indexOf("DDD") > 0) {
			Format formatter = new SimpleDateFormat(form.substring(form.indexOf("DDD") + 4));
			return strCase.replaceFirst("day", "") + ", " + formatter.format(myDate).replaceAll(",", ", ");
		} else {
			Format formatter = new SimpleDateFormat(format);
			return formatter.format(myDate);
		}
  }
	
  public static Calendar getInstanceTempCalendar() { 
    Calendar  calendar = GregorianCalendar.getInstance() ;
    calendar.setLenient(false) ;
    int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset) ; 
    return  calendar;
  }
  
  public static String getSizeFile(double size) {
  	String unit = " Byte" ;
		if(size >= 1000) {
			size = size/1024 ;
			unit = " Kb" ;
		}
		if(size >= 1000) {
			size = size/1024 ;
			unit = " Mb" ;
		}
		String str = String.valueOf(size) ;
		int t = str.indexOf("."); 
		if(t > 0) {
			if(str.length() > (t+4))
				str = str.substring(0,(t+4)) ;
		}
	  return (str + unit);
  }
  
	public static String getTimeZoneNumberInString(String string) {
		if(string != null && string.length() > 0) {
			StringBuffer stringBuffer = new StringBuffer();
			for(int i = 0; i <	string.length(); ++i) {
				char c = string.charAt(i) ; 
				if(c == ')') break ;
				if (Character.isDigit(c) || c == '-' || c == '+' || c == ':'){
					if(c == ':') c = '.';
					if(c == '3' && string.charAt(i-1) == ':') c = '5';
					stringBuffer.append(c);
				}
			}
			return stringBuffer.toString() ;
		}
		return null ;
	}
	
	public static String[] getStarNumber(double voteRating) throws Exception {
		int star = (int)voteRating ;
		String[] className = new String[6] ;
		float k = 0;
		for (int i = 0; i < 5; i++) {
			if(i < star) className[i] = "star" ;
			else if(i == star) {
				k = (float) (voteRating - i) ; 
				if(k < 0.25) className[i] = "notStar" ;
				if(k >= 0.25 && k < 0.75) className[i] = "halfStar" ;
				if(k >= 0.75) className[i] = "star" ;
			} else {
				className[i] = "notStar" ;
			}
			className[5] = ("" + voteRating) ;
			if(className[5].length() >= 3) className[5] = className[5].substring(0, 3) ;
			if(k == 0) className[5] = "" + star ; 
		}
		return className ;
	}
	
	public static String[] splitForForum (String str) throws Exception {
		if(str != null && str.length() > 0) {
			if(str.contains(",")) return str.trim().split(",") ;
			else return str.trim().split(";") ;
		} else return new String[] {} ;
	}
	
	public static String unSplitForForum (String[] str) throws Exception {
		StringBuilder rtn = new StringBuilder();
		if(str.length > 0) {
			for (String temp : str) {
				rtn.append(temp).append(",") ; 
			}
		}
		return rtn.toString() ;
	}
	
	public static String getStringCleanHtmlCode(String sms) {
		if(sms == null || sms.trim().length() <= 0) return "" ;
		StringBuffer string = new StringBuffer();
		char c; boolean get = true ;
		for (int i = 0; i < sms.length(); i++) {
			c = sms.charAt(i);
			if(c == '<') get = false ;
			if(get) string.append(c);
			if(c == '>') get = true ;
		}
		return string.toString();
	}
	
	public static String convertCodeHTML(String s) {
		String link = "";
		if(s == null || s.length() <= 0) return link ;
		int i = 0, j = 0 ;
		StringBuffer buffer = new StringBuffer();
		while (true) {
			i = s.indexOf("http://");
			if(i < 0)break ;
			if(i > 6 && s.substring(i-6,i-2).equalsIgnoreCase("href")) {
				i = s.indexOf("</a>");
				buffer.append(s.substring(0, i+4));
				s = s.substring(i+4);
			}else {
				j = 0;
				String temp = s.substring(i) ;
				int []Int = {32,33,34,39,40,41,42,44,60,91,93,94,123,124,125,8221,8220};
				boolean isEnd = false ;
				while(true) {
					char c = temp.charAt(j);
					for (int k : Int) {
	          if(k == (int)c){isEnd=true; break ;}
          }
					if(isEnd) break ;
					j++ ;
					if(j == temp.length()) break;
				}
				j = j + i;
				buffer.append(s.substring(0, i) + "<a target=\"_blank\" href=\"");
				if(j <= i){
					link = s.substring(i); 
					buffer.append(link + "\">" + link + "</a>") ;
					break;
				} else {
					link = s.substring(i, j);
					buffer.append(link + "\">" + link + "</a>") ;
				}
				s = s.substring(j);
			}
		}
		buffer.append(s);
		s = buffer.toString() ;
		
		buffer = new StringBuffer();
		while (true) {
			int t = s.indexOf('['+"QUOTE");
			if(t < 0 ) break;
			String first = s.substring(0, t) ;
			int t1 = s.indexOf(']');
			if(t1 < 0) break ;
			String usernam = "";
			if(t+7 < t1)usernam = s.substring((t+7), t1) ;
			int t2 = s.indexOf('['+"/QUOTE");
			if(t2 < 0) break ;
			String content = s.substring(t1+1, t2);
			s = s.substring(t2+8) ;
			if(first != null && first.length() > 0) buffer.append(first) ;
			if(content != null && content.length() > 0) {
				buffer.append("<div>Quote:</div><div class=\"ClassQuote\">") ;
				if(usernam.length() > 0)buffer.append("<div>Originally Posted by <strong>").append(usernam).append("</strong></div>") ;
				buffer.append("<div>").append(content).append("</div></div>") ;
			}
		}
		buffer.append(s);
	  return buffer.toString() ;
  }
	
	public static String clearQuote(String s) {
		if(s == null || s.length() <= 0) return "" ;
		StringBuffer buffer = new StringBuffer();
		while(true) {
			int t = s.indexOf('['+"QUOTE");
			if(t < 0 ) break;
			String first = s.substring(0, t) ;
			int t2 = s.indexOf('['+"/QUOTE");
			if(t2 < 0) break ;
			buffer.append(first+"</br>");
			s = s.substring(t2+8);
		}
		buffer.append(s);
		return buffer.toString() ;
  }
	//isReadTopic
	public static boolean isStringInStrings(String []strings, String string) {
	  for (String string1 : strings) {
	    if(string.equals(string1)) return true ;
    }
	  return false;
  }

	public static String getLabel(String label, String key) {
	  return label.replaceFirst("<keyWord>", key) ;
  }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static String convetToCode(String s) {
//		String []commands = {"for","do","while","continue","break","if","else","new","public","import","final","private","void","static","class","extends","implements",
//  			"throws","try","catch","return","this","int","long","double","char","null","true","false"} ;
//  	for (String string : commands) {
//  		if(s.indexOf(string) > -1) {
//  			 s = s.replaceAll(string, "<span style=\"color:#7f0055;\">"+string+"</span>") ;
//  		}
//    }
	  return s;
  }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
