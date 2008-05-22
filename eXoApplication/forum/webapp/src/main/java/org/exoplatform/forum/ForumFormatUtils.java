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
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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
		if(myDate == null) return null;
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
  
  public static boolean isValidEmailAddresses(String addressList) throws Exception {
    if (addressList == null || addressList.length() <= 0)  return true ;
    try {
      InternetAddress[] iAdds = InternetAddress.parse(addressList, true);
      String emailRegex = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}" ;
      for (int i = 0 ; i < iAdds.length; i ++) {
        if(!iAdds[i].getAddress().toString().matches(emailRegex)) return false;
      }
    } catch(AddressException e) {
      return false ;
    }
    return true ;
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
			if(str.contains(",")){ 
				str.replaceAll(";", ",") ;
				return str.trim().split(",") ;
			} else { 
				str.replaceAll(",", ";") ;
				return str.trim().split(";") ;
			}
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
	
	public static boolean isStringInStrings(String []strings, String string) {
	  for (String string1 : strings) {
	    if(string.equals(string1)) return true ;
    }
	  return false;
  }

	public static boolean isStringInList(List<String>list, String string) {
		if(list.contains(string)) return true;
		return false;
	}

	public static String getLabel(String label, String key) {
	  return label.replaceFirst("<keyWord>", key) ;
  }
	
	
	
}
