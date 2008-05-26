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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/

package org.exoplatform.forum;

import org.apache.commons.lang.StringUtils;
/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 22, 2008 - 2:58:53 AM  
 */

public class ForumTransformHTML {

	public static String transform(String bbcode) {
    String b = bbcode.substring(0, bbcode.length());
    //Simple find and replaces
    b = StringUtils.replace(b, "[B]", "[b]" );
    b = StringUtils.replace(b, "[/B]", "[/b]");
    b = StringUtils.replace(b, "[I]", "[i]");
    b = StringUtils.replace(b, "[/I]", "[/i]");
    b = StringUtils.replace(b, "[U]", "[u]");
    b = StringUtils.replace(b, "[/U]", "[/u]");
    b = StringUtils.replace(b, "[/IMG]", "[img]");
    b = StringUtils.replace(b, "[/IMG]", "[/img]");
    b = StringUtils.replace(b, "[CSS:", "[css:");
    b = StringUtils.replace(b, "[/CSS]", "[/css]");
    b = StringUtils.replace(b, "[URL=\"", "[url=\"");
    b = StringUtils.replace(b, "[/URL]", "[/url]");
    b = StringUtils.replace(b, "[LINK=\"", "[url=\"");
    b = StringUtils.replace(b, "[/LINK]", "[/url]");
    b = StringUtils.replace(b, "[link=", "[url=");
    b = StringUtils.replace(b, "[/link]", "[/url]");
    b = StringUtils.replace(b, "[GOTO=", "[goto=");
    b = StringUtils.replace(b, "[/GOTO]", "[/goto]");
    b = StringUtils.replace(b, "[/quote]", "[/QUOTE]");
    b = StringUtils.replace(b, "[quote", "[QUOTE");
    
    b = StringUtils.replace(b, "[b]", "<b>" );
    b = StringUtils.replace(b, "[/b]", "</b>");
    b = StringUtils.replace(b, "[i]", "<i>");
    b = StringUtils.replace(b, "[/i]", "</i>");
    b = StringUtils.replace(b, "[u]", "<u>");
    b = StringUtils.replace(b, "[/u]", "</u>");
    b = StringUtils.replace(b, "[code]", "<code>");
    b = StringUtils.replace(b, "[/code]", "</code>");
    b = StringUtils.replace(b, "[CODE]", "<code>");
    b = StringUtils.replace(b, "[/CODE]", "</code>");
    StringBuffer buffer ;
    //Need to get the text inbetween img's
    int lastIndex=-0;;
    int tagIndex=0;
    while ((tagIndex = b.indexOf("[img]", lastIndex))!=-1) {
      lastIndex = tagIndex+1;
      try {
        int clsIndex = b.indexOf("[/img]");
        String src = b.substring(tagIndex + 5, clsIndex);
        buffer = new StringBuffer();
        buffer.append("<img src=\"").append(src).append("\" />") ;
        b = StringUtils.replace(b, "[img]" + src + "[/img]", buffer.toString());
      }  catch (Exception e) {
        System.out.println("Error in bbcode near char: " + tagIndex );
        e.printStackTrace();
        continue;
      }
    }
 
    //Need to get the text inbetween a as well as the href
    tagIndex=0;
    lastIndex=-0;
    while ((tagIndex = b.indexOf("[url=\"", lastIndex))!=-1) {
      lastIndex = tagIndex+1;
      try {
        int clsIndex = b.indexOf("[/url]", tagIndex);
        String urlStr = b.substring(tagIndex, clsIndex);
        int fstb = urlStr.indexOf("=\"") + 1;
        int clsUrl = urlStr.indexOf("]");
        String href = urlStr.substring(fstb + 1,
                         urlStr.indexOf("\"", fstb + 1));
        String text = urlStr.substring(clsUrl + 1, urlStr.length());
        buffer = new StringBuffer();
        buffer.append("<a target='_blank' href=\"").append(href).append("\">").append(text).append("</a>") ;
        b = StringUtils.replace(b, "[url=\"" + href + "\"]" + text + "[/url]", buffer.toString() );
      } catch (Exception e) {
        System.out.println("Error in bbcode near char: " + tagIndex );
        e.printStackTrace();
        continue;
      }
    }
    //Custom replaces
    if (b.indexOf("[!bbcode]")>=0 && b.indexOf("[!v]")<20) {
      b = StringUtils.replace(b, "[!bbcode]", "");
      b = StringUtils.replace(b, "\r\n", "<br>\r\n");
    }
    //Dir to images directory, should be replaced with a System propert
    b = StringUtils.replace(b, "[imgdir]", "/www/public/images/");
    b = StringUtils.replace(b, "[public]", "/www/public/");
 
    tagIndex=0;
    lastIndex = -1;
    while ((tagIndex = b.indexOf("[css:", lastIndex )) != -1) {
      lastIndex = tagIndex+1;
      try {
        int clsIndex = b.indexOf("[/css]", tagIndex);
        String urlStr = b.substring(tagIndex, clsIndex);
        int fstb = urlStr.indexOf(":") + 1;
        int clsUrl = urlStr.indexOf("]");
        String css = urlStr.substring(fstb,
                        urlStr.indexOf("]", fstb + 1));
        String text = urlStr.substring(clsUrl + 1, urlStr.length());
        buffer = new StringBuffer();
        buffer.append("<div class='").append(css).append("'>").append(text).append("</div>") ;
        b = StringUtils.replace(b, "[css:" + css + "]" + text + "[/css]",buffer.toString());
      } catch (Exception e) {
        System.out.println("Error in BBcode near char: " + tagIndex );
        e.printStackTrace();
        continue;
      }
    }

    while ((tagIndex = b.indexOf("[QUOTE=", lastIndex )) != -1) {
    	lastIndex = tagIndex+1;
    	try {
    		int clsIndex = b.indexOf("[/QUOTE]", tagIndex);
    		String urlStr = b.substring(tagIndex, clsIndex);
    		int fstb = urlStr.indexOf("=") + 1;
    		int clsUrl = urlStr.indexOf("]");
    		String userName = urlStr.substring(fstb,
    				urlStr.indexOf("]", fstb + 1));
    		String text = urlStr.substring(clsUrl + 1, urlStr.length());
    		buffer = new StringBuffer();
    		buffer.append("<div>Quote:</div><div class=\"ClassQuote\">") ;
    		buffer.append("<div>Originally Posted by <strong>").append(userName).append("</strong></div>") ;
    		buffer.append("<div>").append(text).append("</div></div>") ;
    		b = StringUtils.replace(b,
    				"[QUOTE=" + userName + "]" + text + "[/QUOTE]",
    				buffer.toString());
    	} catch (Exception e) {
    		System.out.println("Error in BBcodeSmall near char: " + tagIndex );
    		e.printStackTrace();
    		continue;
    	}
    }
    
    while ((tagIndex = b.indexOf("[QUOTE]", lastIndex )) != -1) {
    	lastIndex = tagIndex+1;
    	try {
    		int clsIndex = b.indexOf("[/QUOTE]", tagIndex);
    		String text = b.substring(tagIndex + 7, clsIndex);
    		buffer = new StringBuffer();
    		buffer.append("<div>Quote:</div><div class=\"ClassQuote\">") ;
    		buffer.append("<div>").append(text).append("</div></div>") ;
    		b = StringUtils.replace(b,
    				"[QUOTE]" + text + "[/QUOTE]",
    				buffer.toString());
    	} catch (Exception e) {
    		System.out.println("Error in BBcodeSmall near char: " + tagIndex );
    		e.printStackTrace();
    		continue;
    	}
    }
		
    //spot
    tagIndex = 0;
    lastIndex = -1;
    while ((tagIndex = b.indexOf("[spot]", lastIndex)) != -1) {
      lastIndex = tagIndex + 1;
      try {
        int clsIndex = b.indexOf("[/spot]");
        String src = b.substring(tagIndex + 6, clsIndex);
        b = StringUtils.replace(b, "[spot]" + src + "[/spot]",
                    "<a name=\"" + src + "\" />");
      } catch (Exception e) {
        System.out.println("Error in bbcode near char: " + tagIndex);
        e.printStackTrace();
        continue;
      }
    }
 
    //Goto
    tagIndex=0;
    lastIndex = -1;
    while ((tagIndex = b.indexOf("[goto=\"", lastIndex))!=-1) {
      lastIndex = tagIndex+1;
      try {
        int clsIndex = b.indexOf("[/goto]", tagIndex);
        String urlStr = b.substring(tagIndex, clsIndex);
        int fstb = urlStr.indexOf("=\"") + 1;
        int clsUrl = urlStr.indexOf("]");
        String href = urlStr.substring(fstb + 1,
                         urlStr.indexOf("\"", fstb + 1));
        String text = urlStr.substring(clsUrl + 1, urlStr.length());
        b = StringUtils.replace(b,
                    "[goto=\"" + href + "\"]" + text + "[/goto]",
                    "<a href=\"" + href +
                    "\">" + text + "</a>");
      } catch (Exception e) {
        System.out.println("Error in bbcode near char: " + tagIndex );
        e.printStackTrace();
        continue;
      }
    }
    return b;
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
		s = transform(s);
		StringBuffer buffer = new StringBuffer();
		while (true) {
			i = s.indexOf("http://");
			if(i < 0)break ;
			if(i > 6 && s.substring(i-2,i).equalsIgnoreCase("=\"")) {
				i = s.indexOf("/>");//<img />
				if(i < 0) {
					i = s.indexOf("</");//</a>
					buffer.append(s.substring(0, i+4));
					s = s.substring(i+4);
				} else {
					buffer.append(s.substring(0, i+2));
					s = s.substring(i+2);
				}
			}else {
				j = 0;
				String temp = s.substring(i) ;
				int []Int = {9,10,32,33,34,39,40,41,42,44,60,91,93,94,123,124,125,8221,8220};
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
		return buffer.toString() ;
  }
	
	
	public static String clearQuote(String s) {
		if(s == null || s.length() <= 0) return "" ;
		StringBuffer buffer = new StringBuffer();
		 s = StringUtils.replace(s, "[/quote]", "[/QUOTE]");
	   s = StringUtils.replace(s, "[quote", "[QUOTE");
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
	
	public static String convetToCode(String s) {
//	String []commands = {"for","do","while","continue","break","if","else","new","public","import","final","private","void","static","class","extends","implements",
//			"throws","try","catch","return","this","int","long","double","char","null","true","false"} ;
//	for (String string : commands) {
//		if(s.indexOf(string) > -1) {
//			 s = s.replaceAll(string, "<span style=\"color:#7f0055;\">"+string+"</span>") ;
//		}
//  }
  return s;
	}
	
}
