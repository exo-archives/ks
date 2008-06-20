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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    StringBuffer buffer ;
    int lastIndex=0;
    int tagIndex=0;
    //Lower Case bbc
    String start, end; 
    String []bbcs = new String[]{"B", "I", "IMG", "CSS", "URL", "LINK", "GOTO", "QUOTE", "LEFT", 
    		"RIGHT", "CENTER", "JUSTIFY", "SIZE", "COLOR", "RIGHT", "LEFT", "CENTER", "JUSTIFY", "CSS"};
    for (String bbc : bbcs) {
	    start = "["+bbc; end="[/"+bbc+"]";
	    lastIndex=0;tagIndex=0;
	    while ((tagIndex = b.indexOf(start, lastIndex))!=-1) {
	      lastIndex = tagIndex+1;
	      try {
	        int clsIndex = b.indexOf(end);
	        String content = b.substring(tagIndex + bbc.length()+1, clsIndex);
	        String bbc_= bbc.toLowerCase() ;
	        b = StringUtils.replace(b, "["+bbc + content + end, "["+bbc_ + content + "[/"+bbc_+"]");
	      }  catch (Exception e) {
	        System.out.println("Error in bbcode near char: " + tagIndex );
	        e.printStackTrace();
	        continue;
	      }
	    }
    }
    //Simple find and replaces
    b = StringUtils.replace(b, "[U]", "<u>");
    b = StringUtils.replace(b, "[/U]", "</u>");
    b = StringUtils.replace(b, "[u]", "<u>");
    b = StringUtils.replace(b, "[/u]", "</u>");
    b = StringUtils.replace(b, "[b]", "<b>" );
    b = StringUtils.replace(b, "[/b]", "</b>");
    b = StringUtils.replace(b, "[i]", "<i>");
    b = StringUtils.replace(b, "[/i]", "</i>");
    b = StringUtils.replace(b, "[code]", "[CODE]");
    b = StringUtils.replace(b, "[/code]", "[/CODE]");
    b = StringUtils.replace(b, "[CODE]", "<code>");
    b = StringUtils.replace(b, "[/CODE]", "</code>");
    b = StringUtils.replace(b, "&quot;", "\"");
    System.out.println("\n\n" + b + "\n\n");
    //Need to get the text inbetween img's
    lastIndex=-0;;
    tagIndex=0;
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
 
    //align right <div align="right"> 
    String []aligns = new String[]{"right", "left", "center", "justify"};
    for (String string : aligns) {
    	tagIndex = 0;
    	lastIndex = 0;
	    start = "["+string+"]" ; end = "[/"+string+"]" ;
	    while ((tagIndex = b.indexOf(start, lastIndex)) != -1) {
	    	lastIndex = tagIndex + 1;
	    	try {
	    		int clsIndex = b.indexOf(end);
	    		String content = b.substring(tagIndex+string.length()+2, clsIndex);
	    		b = StringUtils.replace(b, start + content + end,
	    				"<div align=\""+string+"\">" + content + "</div>");
	    	} catch (Exception e) {
	    		System.out.println("Error in bbcode near char: " + tagIndex);
	    		e.printStackTrace();
	    		continue;
	    	}
	    }
    }
    //size 
    tagIndex=0;
    lastIndex=0;
    while ((tagIndex = b.indexOf("[size=", lastIndex))!=-1) {
    	lastIndex = tagIndex+1;
    	try {
    		int clsIndex = b.indexOf("[/size]", tagIndex);
    		String urlStr = b.substring(tagIndex, clsIndex);
    		int fstb = urlStr.indexOf("=");
    		int clsUrl = urlStr.indexOf("]");
    		String size = urlStr.substring(fstb+1, clsUrl);
    		String size_ = size;
    		if(size.indexOf("\"") >= 0)size_ = size_.replaceAll("\"", "");
    		if(size.indexOf("+") >= 0)size_ = size_.replace("+", "");
    		String text = urlStr.substring(clsUrl + 1);
    		buffer = new StringBuffer();
    		buffer.append("<font size=\"").append(size_).append("\">").append(text).append("</font>") ;
    		b = StringUtils.replace(b, "[size=" + size + "]" + text + "[/size]", buffer.toString() );
    	} catch (Exception e) {
    		System.out.println("Error in bbcode near char: " + tagIndex );
    		e.printStackTrace();
    		continue;
    	}
    }
    //color
    tagIndex=0;
    lastIndex=0;
    while ((tagIndex = b.indexOf("[color=", lastIndex))!=-1) {
    	lastIndex = tagIndex+1;
    	try {
    		int clsIndex = b.indexOf("[/color]", tagIndex);
    		String urlStr = b.substring(tagIndex, clsIndex);
    		int fstb = urlStr.indexOf("=");
    		int clsUrl = urlStr.indexOf("]");
    		String color = urlStr.substring(fstb+1, clsUrl);
    		String color_ = color;
    		if(color.indexOf("\"") >= 0)color_ = color.replaceAll("\"", "");
    		String text = urlStr.substring(clsUrl + 1);
    		buffer = new StringBuffer();
    		buffer.append("<font color=\"").append(color_).append("\">").append(text).append("</font>") ;
    		b = StringUtils.replace(b, "[color=" + color + "]" + text + "[/color]", buffer.toString() );
    	} catch (Exception e) {
    		System.out.println("Error in bbcode near char: " + tagIndex );
    		e.printStackTrace();
    		continue;
    	}
    }
    //Need to get the text inbetween a as well as the href
    tagIndex=0;
    lastIndex=0;
    while ((tagIndex = b.indexOf("[url=", lastIndex))!=-1) {
    	lastIndex = tagIndex+1;
    	try {
    		int clsIndex = b.indexOf("[/url]", tagIndex);
    		String urlStr = b.substring(tagIndex, clsIndex);
    		int fstb = urlStr.indexOf("=");
    		int clsUrl = urlStr.indexOf("]");
    		String href = urlStr.substring(fstb+1, clsUrl);
    		String href_ = href;
    		if(href.indexOf("\"") >= 0)href_ = href.replaceAll("\"", "");
    		String text = urlStr.substring(clsUrl + 1);
    		buffer = new StringBuffer();
    		buffer.append("<a target='_blank' href=\"").append(href_).append("\">").append(text).append("</a>") ;
    		b = StringUtils.replace(b, "[url=" + href + "]" + text + "[/url]", buffer.toString() );
    	} catch (Exception e) {
    		System.out.println("Error in bbcode near char: " + tagIndex );
    		e.printStackTrace();
    		continue;
    	}
    }
    //url
    tagIndex = 0;
    lastIndex = 0;
    while ((tagIndex = b.indexOf("[url]", lastIndex)) != -1) {
    	lastIndex = tagIndex + 1;
    	try {
    		int clsIndex = b.indexOf("[/url]");
    		String src = b.substring(tagIndex + 5, clsIndex);
    		b = StringUtils.replace(b, "[url]" + src + "[/url]",
    				"<a target='_blank' href=\"" + src + "\">" + src + "</a>");
    	} catch (Exception e) {
    		System.out.println("Error in bbcode near char: " + tagIndex);
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
    //css
    tagIndex=0;
    lastIndex = 0;
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
    //quote
    while ((tagIndex = b.indexOf("[quote=", lastIndex )) != -1) {
    	lastIndex = tagIndex+1;
    	try {
    		int clsIndex = b.indexOf("[/quote]", tagIndex);
    		String urlStr = b.substring(tagIndex, clsIndex);
    		int fstb = urlStr.indexOf("=") + 1;
    		int clsUrl = urlStr.indexOf("]");
    		String userName = urlStr.substring(fstb,
    				urlStr.indexOf("]", fstb + 1));
    		String text = urlStr.substring(clsUrl + 1, urlStr.length());
    		buffer = new StringBuffer();
    		buffer.append("<div>Quote:</div><div class=\"Classquote\">") ;
    		buffer.append("<div>Originally Posted by <strong>").append(userName).append("</strong></div>") ;
    		buffer.append("<div>").append(text).append("</div></div>") ;
    		b = StringUtils.replace(b,
    				"[quote=" + userName + "]" + text + "[/quote]",
    				buffer.toString());
    	} catch (Exception e) {
    		System.out.println("Error in BBcodeSmall near char: " + tagIndex );
    		e.printStackTrace();
    		continue;
    	}
    }
    
    while ((tagIndex = b.indexOf("[quote]", lastIndex )) != -1) {
    	lastIndex = tagIndex+1;
    	try {
    		int clsIndex = b.indexOf("[/quote]", tagIndex);
    		String text = b.substring(tagIndex + 7, clsIndex);
    		buffer = new StringBuffer();
    		buffer.append("<div>Quote:</div><div class=\"Classquote\">") ;
    		buffer.append("<div>").append(text).append("</div></div>") ;
    		b = StringUtils.replace(b,
    				"[quote]" + text + "[/quote]",
    				buffer.toString());
    	} catch (Exception e) {
    		System.out.println("Error in BBcodeSmall near char: " + tagIndex );
    		e.printStackTrace();
    		continue;
    	}
    }

//    while ((tagIndex = b.indexOf("[CODE]", lastIndex )) != -1) {
//    	lastIndex = tagIndex+1;
//    	try {
//    		int clsIndex = b.indexOf("[/CODE]", tagIndex);
//    		String text = b.substring(tagIndex + 7, clsIndex);
//    		String text_ = text.replaceAll("&lt;","<").replaceAll("&gt;", ">").replaceAll("&nbsp;", "&#32");
//    		buffer = new StringBuffer();
//    		buffer.append("<div>Code:</div><div class=\"Classquote\">") ;
//    		buffer.append("<div><xmp>").append(text_).append("</xmp></div></div>") ;
//    		b = StringUtils.replace(b, "[CODE]" + text + "[/CODE]", buffer.toString());
//    	} catch (Exception e) {
//    		System.out.println("Error in BBcodeSmall near char: " + tagIndex );
//    		e.printStackTrace();
//    		continue;
//    	}
//    }
		
    //Goto
    tagIndex=0;
    lastIndex = 0;
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
	
	public static String cleanHtmlCode(String sms) {
		if(sms == null || sms.trim().length() <= 0) return "" ;
		List<String> bbcList = new ArrayList<String>();
		//clean bbcode
		String []bbcs = new String[]{"B", "I", "IMG", "CSS", "URL", "LINK", "GOTO", "QUOTE", "LEFT", 
				"RIGHT", "CENTER", "JUSTIFY", "SIZE", "COLOR", "RIGHT", "LEFT", "CENTER", "JUSTIFY", "CSS"};
		bbcList.addAll(Arrays.asList(bbcs)) ; 
		for (String bbc : bbcs) {
			bbcList.add(bbc.toLowerCase()) ;
		}
    int lastIndex=0;
    int tagIndex=0;
    String start, end; 
    for (String bbc : bbcList) {
	    start = "["+bbc; end="[/"+bbc+"]";
	    lastIndex=0;tagIndex=0;
	    while ((tagIndex = sms.indexOf(start, lastIndex))!=-1) {
	      lastIndex = tagIndex+1;
	      try {
	        int clsIndex = sms.indexOf(end);
	        String content = sms.substring(tagIndex, clsIndex);
	        String content_ = content.substring(content.indexOf("]")+1) ;
	        sms = StringUtils.replace(sms, content + end,  content_);
	      }  catch (Exception e) {
	        System.out.println("Error in bbcode near char: " + tagIndex );
	        e.printStackTrace();
	        continue;
	      }
	    }
    }
    sms = StringUtils.replace(sms, "[U]", "") ;
    sms = StringUtils.replace(sms, "[/U]", "") ;
    sms = StringUtils.replace(sms, "[u]", "") ;
    sms = StringUtils.replace(sms, "[/u]", "") ;
    //Clean html code
  	String scriptregex = "<(script|style)[^>]*>[^<]*</(script|style)>";
    Pattern p1 = Pattern.compile(scriptregex,Pattern.CASE_INSENSITIVE);
    Matcher m1 = p1.matcher(sms);
    sms = m1.replaceAll("");
    String tagregex = "<[^>]*>";
    Pattern p2 = Pattern.compile(tagregex);
    Matcher m2 = p2.matcher(sms);
    sms = m2.replaceAll("");
    String multiplenewlines = "(\\n{1,2})(\\s*\\n)+"; 
    sms = sms.replaceAll(multiplenewlines,"$1");
		return sms;
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
		 s = StringUtils.replace(s, "[/QUOTE]", "[/quote]");
	   s = StringUtils.replace(s, "[QUOTE", "[quote");
		while(true) {
			int t = s.indexOf('['+"quote");
			if(t < 0 ) break;
			String first = s.substring(0, t) ;
			int t2 = s.indexOf('['+"/quote");
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
