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
 * 					tu.duy@exoplatform.com 
 * May 22, 2008 - 2:58:53 AM
 */

public class ForumTransformHTML {

	public static String transform(String bbcode) {
		String b = bbcode.substring(0, bbcode.length());

		StringBuffer buffer;
		int lastIndex = 0;
		int tagIndex = 0;
		// Lower Case bbc
		String start, end;
		String[] bbcs = new String[] { "B", "I", "HIGHLIGHT", "IMG", "CSS", "URL", "LINK", "GOTO", "QUOTE", "LEFT",
		    "RIGHT", "CENTER", "SIZE", "COLOR", "RIGHT", "LEFT", "CENTER", "JUSTIFY", "CSS", "EMAIL", "CODE" };
		for (String bbc : bbcs) {
			start = "[" + bbc;
			end = "[/" + bbc + "]";
			lastIndex = 0;
			tagIndex = 0;
			while ((tagIndex = b.indexOf(start, lastIndex)) != -1) {
				lastIndex = tagIndex + 1;
				try {
					int clsIndex = b.indexOf(end);
					String content = b.substring(tagIndex + bbc.length() + 1, clsIndex);
					String bbc_ = bbc.toLowerCase();
					b = StringUtils.replace(b, "[" + bbc + content + end, "[" + bbc_ + content + "[/" + bbc_
					    + "]");
				} catch (Exception e) {
					continue;
				}
			}
		}
		// Simple find and replaces
		b = StringUtils.replace(b, "[U]", "<u>");
		b = StringUtils.replace(b, "[/U]", "</u>");
		b = StringUtils.replace(b, "[u]", "<u>");
		b = StringUtils.replace(b, "[/u]", "</u>");
		b = StringUtils.replace(b, "[b]", "<b>");
		b = StringUtils.replace(b, "[/b]", "</b>");
		b = StringUtils.replace(b, "[i]", "<i>");
		b = StringUtils.replace(b, "[/i]", "</i>");
		b = StringUtils.replace(b, "[link", "[url");
		b = StringUtils.replace(b, "[/link]", "[/url]");
		b = StringUtils.replace(b, "&quot;", "\"");
		// Need to get the text inbetween img's
		lastIndex = -0;
		;
		tagIndex = 0;
		while ((tagIndex = b.indexOf("[img]", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/img]", tagIndex);
				String src = b.substring(tagIndex + 5, clsIndex);
				String src_ = cleanHtmlCode(src).replaceAll("&nbsp;", "").replaceAll(" ", "").trim() ;
				buffer = new StringBuffer();
				buffer.append("<img src=\"").append(src_).append("\" />");
				b = StringUtils.replace(b, "[img]" + src + "[/img]", buffer.toString());
			} catch (Exception e) {
				continue;
			}
		}

		// align right <div align="right">
		String[] aligns = new String[] { "right", "left", "center", "justify" };
		for (String string : aligns) {
			tagIndex = 0;
			lastIndex = 0;
			start = "[" + string + "]";
			end = "[/" + string + "]";
			while ((tagIndex = b.indexOf(start, lastIndex)) != -1) {
				lastIndex = tagIndex + 1;
				try {
					int clsIndex = b.indexOf(end, tagIndex);
					String content = b.substring(tagIndex + string.length() + 2, clsIndex);
					b = StringUtils.replace(b, start + content + end, "<div align=\"" + string + "\">"
					    + content + "</div>");
				} catch (Exception e) {
					continue;
				}
			}
		}
		// size [size=-1]jlfjsdfjds[/size] font-size: 12px;
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[size=", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/size]", tagIndex);
				String urlStr = b.substring(tagIndex, clsIndex);
				int fstb = urlStr.indexOf("=");
				int clsUrl = urlStr.indexOf("]");
				String size = urlStr.substring(fstb + 1, clsUrl);
				String size_ = size;
				
				if (size.indexOf("\"") >= 0)
					size_ = size_.replaceAll("\"", "");
				if (size.indexOf("+") >= 0){
					size_ = size_.replace("+", "");
				}
				String text = urlStr.substring(clsUrl + 1);
				buffer = new StringBuffer();
				buffer.append("<font size=\"").append(size_).append("\">").append(text).append("</font>");
				b = StringUtils.replace(b, "[size=" + size + "]" + text + "[/size]", buffer.toString());
			} catch (Exception e) {
				continue;
			}
		}
		// color
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[color=", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/color]", tagIndex);
				String urlStr = b.substring(tagIndex, clsIndex);
				int fstb = urlStr.indexOf("=");
				int clsUrl = urlStr.indexOf("]");
				String color = urlStr.substring(fstb + 1, clsUrl);
				String color_ = color;
				if (color.indexOf("\"") >= 0)
					color_ = color.replaceAll("\"", "");
				String text = urlStr.substring(clsUrl + 1);
				buffer = new StringBuffer();
				buffer.append("<font color=\"").append(color_).append("\">").append(text).append("</font>");
				b = StringUtils.replace(b, "[color=" + color + "]" + text + "[/color]", buffer.toString());
			} catch (Exception e) {
				continue;
			}
		}
		// Need to get the text inbetween a as well as the href
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[url=", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/url]", tagIndex);
				String urlStr = b.substring(tagIndex, clsIndex);
				int fstb = urlStr.indexOf("=");
				int clsUrl = urlStr.indexOf("]");
				String href = urlStr.substring(fstb + 1, clsUrl);
				String href_ = href;
				if (href.indexOf("\"") >= 0)
					href_ = href.replaceAll("\"", "");
				String text = urlStr.substring(clsUrl + 1);
				buffer = new StringBuffer();
				buffer.append("<a target='_blank' href=\"").append(href_).append("\">").append(text)
				    .append("</a>");
				b = StringUtils.replace(b, "[url=" + href + "]" + text + "[/url]", buffer.toString());
			} catch (Exception e) {
				continue;
			}
		}
		// url
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[url]", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/url]", tagIndex);
				String src = b.substring(tagIndex + 5, clsIndex);
				b = StringUtils.replace(b, "[url]" + src + "[/url]", "<a target='_blank' href=\"" + src.trim()
				    + "\">" + src + "</a>");
			} catch (Exception e) {
				continue;
			}
		}
		// email=
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[email=", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/email]", tagIndex);
				String urlStr = b.substring(tagIndex, clsIndex);
				int fstb = urlStr.indexOf("=");
				int clsUrl = urlStr.indexOf("]");
				String href = urlStr.substring(fstb + 1, clsUrl);
				String href_ = href;
				if (href.indexOf("\"") >= 0)
					href_ = href.replaceAll("\"", "");
				String text = urlStr.substring(clsUrl + 1);
				buffer = new StringBuffer();
				buffer.append("<a href=\"mailto:").append(href_).append("\">").append(text).append("</a>");
				b = StringUtils.replace(b, "[email=" + href + "]" + text + "[/email]", buffer.toString());
			} catch (Exception e) {
				continue;
			}
		}
		// url
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[email]", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/email]", tagIndex);
				String src = b.substring(tagIndex + 7, clsIndex);
				b = StringUtils.replace(b, "[email]" + src + "[/email]", "<a href=\"mailto:" + src.trim()	+ "\">" + src.trim() + "</a>");
			} catch (Exception e) {
				continue;
			}
		}

		// Custom replaces
		if (b.indexOf("[!bbcode]") >= 0 && b.indexOf("[!v]") < 20) {
			b = StringUtils.replace(b, "[!bbcode]", "");
			b = StringUtils.replace(b, "\r\n", "<br>\r\n");
		}
		// Dir to images directory, should be replaced with a System propert
		b = StringUtils.replace(b, "[imgdir]", "/www/public/images/");
		b = StringUtils.replace(b, "[public]", "/www/public/");
		// css
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[css:", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/css]", tagIndex);
				String urlStr = b.substring(tagIndex, clsIndex);
				int fstb = urlStr.indexOf(":") + 1;
				int clsUrl = urlStr.indexOf("]");
				String css = urlStr.substring(fstb, urlStr.indexOf("]", fstb + 1));
				String text = urlStr.substring(clsUrl + 1, urlStr.length());
				if(text == null || text.trim().length() == 0) continue;
				buffer = new StringBuffer();
				buffer.append("<div class='").append(css).append("'>").append(text).append("</div>");
				b = StringUtils.replace(b, "[css:" + css + "]" + text + "[/css]", buffer.toString());
			} catch (Exception e) {
				continue;
			}
		}
		//highlight
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[highlight]", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/highlight]", tagIndex);
				String urlStr = b.substring(tagIndex+11, clsIndex);
				buffer = new StringBuffer();
				buffer.append("<span style=\"font-weight:bold; color: blue;\">").append(urlStr).append("</span>");
				b = StringUtils.replace(b, "[highlight]" + urlStr + "[/highlight]", buffer.toString());
			} catch (Exception e) {
				continue;
			}
		}
		// quote
		while ((tagIndex = b.indexOf("[quote=", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/quote]", tagIndex);
				String urlStr = b.substring(tagIndex, clsIndex);
				int fstb = urlStr.indexOf("=") + 1;
				int clsUrl = urlStr.indexOf("]");
				String userName = urlStr.substring(fstb, urlStr.indexOf("]", fstb + 1));
				String text = urlStr.substring(clsUrl + 1, urlStr.length());
				if(text == null || text.trim().length() == 0) continue;
				buffer = new StringBuffer();
				buffer.append("<div class=\"Classquote\">");
				buffer.append("<div>Originally Posted by <strong>").append(userName).append(
				    "</strong></div>");
				buffer.append("<div>").append(text).append("</div></div>");
				b = StringUtils.replace(b, "[quote=" + userName + "]" + text + "[/quote]", buffer
				    .toString());
			} catch (Exception e) {
				continue;
			}
		}

		while ((tagIndex = b.indexOf("[quote]", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/quote]", tagIndex);
				String text = b.substring(tagIndex + 7, clsIndex);
				if(text == null || text.trim().length() == 0) continue;
				buffer = new StringBuffer();
				buffer.append("<div class=\"Classquote\">");
				buffer.append("<div>").append(text).append("</div></div>");
				b = StringUtils.replace(b, "[quote]" + text + "[/quote]", buffer.toString());
			} catch (Exception e) {
				continue;
			}
		}
		// Code
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[code]", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/code]", tagIndex);
				String text = b.substring(tagIndex + 6, clsIndex);
				if(text == null || text.trim().length() == 0) continue;
				String text_ = text.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&nbsp;", "&#32");
				buffer = new StringBuffer();
				buffer.append("<div>Code:</div><div class=\"ClassCode\">");
				buffer.append("<pre>").append(text_).append("</pre></div>");
				b = StringUtils.replace(b, "[code]" + text + "[/code]", buffer.toString());
			} catch (Exception e) {
				System.out.println("Error in BBcodeSmall near char: " + tagIndex);
				e.printStackTrace();
				continue;
			}
		}

		// Goto
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[goto=\"", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/goto]", tagIndex);
				String urlStr = b.substring(tagIndex, clsIndex);
				int fstb = urlStr.indexOf("=\"") + 1;
				int clsUrl = urlStr.indexOf("]");
				String href = urlStr.substring(fstb + 1, urlStr.indexOf("\"", fstb + 1));
				String text = urlStr.substring(clsUrl + 1, urlStr.length());
				b = StringUtils.replace(b, "[goto=\"" + href + "\"]" + text + "[/goto]", "<a href=\""
				    + href + "\">" + text + "</a>");
			} catch (Exception e) {
				continue;
			}
		}
		return b;
	}

	public static String cleanHtmlCode(String sms) {
		if (sms == null || sms.trim().length() <= 0)
			return "";
		List<String> bbcList = new ArrayList<String>();
		// clean bbcode
		String[] bbcs = new String[] { "B", "I", "IMG", "CSS", "URL", "LINK", "GOTO", "QUOTE", "LEFT",
		    "RIGHT", "CENTER", "JUSTIFY", "SIZE", "COLOR", "RIGHT", "LEFT", "CENTER", "JUSTIFY", "CSS" };
		bbcList.addAll(Arrays.asList(bbcs));
		for (String bbc : bbcs) {
			bbcList.add(bbc.toLowerCase());
		}
		int lastIndex = 0;
		int tagIndex = 0;
		String start, end;
		for (String bbc : bbcList) {
			start = "[" + bbc;
			end = "[/" + bbc + "]";
			lastIndex = 0;
			tagIndex = 0;
			while ((tagIndex = sms.indexOf(start, lastIndex)) != -1) {
				lastIndex = tagIndex + 1;
				try {
					int clsIndex = sms.indexOf(end, tagIndex);
					String content = sms.substring(tagIndex, clsIndex);
					String content_ = content.substring(content.indexOf("]") + 1);
					sms = StringUtils.replace(sms, content + end, content_);
				} catch (Exception e) {
					continue;
				}
			}
		}
		sms = StringUtils.replace(sms, "[U]", "");
		sms = StringUtils.replace(sms, "[/U]", "");
		sms = StringUtils.replace(sms, "[u]", "");
		sms = StringUtils.replace(sms, "[/u]", "");
		// Clean html code
		String scriptregex = "<(script|style)[^>]*>[^<]*</(script|style)>";
		Pattern p1 = Pattern.compile(scriptregex, Pattern.CASE_INSENSITIVE);
		Matcher m1 = p1.matcher(sms);
		sms = m1.replaceAll("");
		String tagregex = "<[^>]*>";
		Pattern p2 = Pattern.compile(tagregex);
		Matcher m2 = p2.matcher(sms);
		sms = m2.replaceAll("");
		String multiplenewlines = "(\\n{1,2})(\\s*\\n)+";
		sms = sms.replaceAll(multiplenewlines, "$1");
		return sms;
	}

	public static String getTitleInHTMLCode(String s) {
		s = cleanHtmlCode(s);
		s = s.replaceAll("&nbsp;&nbsp;", "&nbsp;").replaceAll("&nbsp; ", " ").replaceAll("\n", "");
		return s;
	}

	public static String convertCodeHTML(String s) {
		String link = "";
		if (s == null || s.length() <= 0)
			return link;
		s = transform(s);
		s = s.replaceAll(
		    "[^=\"?|\'?](https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
		    "<a target=\"blank_\" href=\"$0\">$0</a>");
//		s = s.replaceAll(
//		    "[^mailto:\"?|\'?][_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}",
//		    "<a target=\"_blank\" href=\"mailto:$0\"> $0 </a>");
		s = s.replaceAll("href=\" http://", "href=\"http://").replaceAll("href=\">http://", "href=\"http://")
				 .replaceAll(">>http://", ">http://").replaceAll("> http://", ">http://");
		return s;
	}
	
	public static String clearQuote(String s) {
		if (s == null || s.length() <= 0)
			return "";
		s = StringUtils.replace(s, "[/QUOTE]", "[/quote]");
		s = StringUtils.replace(s, "[QUOTE", "[quote");
		StringBuffer buffer = new StringBuffer();
		while (true) {
			int t = s.indexOf('[' + "quote");
			if (t < 0)
				break;
			String first = s.substring(0, t);
			int t2 = s.indexOf('[' + "/quote");
			if (t2 < 0)
				break;
			if(!ForumUtils.isEmpty(first))
				buffer.append(first + "</br>");
			s = s.substring(t2 + 8);
		}
		buffer.append(s);
		s = buffer.toString();
		s = s.trim();
		s = StringUtils.replace(s, "<p>[&nbsp;\\s?]</p>", "");
		s = StringUtils.replace(s, "\n", "");
		while(s.indexOf("<br/>") == 0){
			s = s.replaceFirst("<br/>", "");
		}
		if (s.indexOf("<p>") == 0) {
			s = s.replaceFirst("<p>", "");
			s = s.replaceFirst("</p>", "");
		}
		return s;
	}

	public static String convetToCode(String s) {
		// String []commands =
		// {"for","do","while","continue","break","if","else","new","public","import","final","private","void","static","class","extends","implements",
		// "throws","try","catch","return","this","int","long","double","char","null","true","false"}
		// ;
		// for (String string : commands) {
		// if(s.indexOf(string) > -1) {
		// s = s.replaceAll(string, "<span
		// style=\"color:#7f0055;\">"+string+"</span>") ;
		// }
		// }
		return s;
	}
	public static String autoAddUser(String s) {
		String tmp = s;
	  for (int i = 0; i < 400; i++) {
	  	String t = tmp.replaceFirst("testUser", "testUser" + i);
	    s = s + "<br/>" + t;
    }
		return s;
  }
	public static String enCodeHTML(String s) {
		StringBuffer buffer = new StringBuffer();
		if(s != null) {
			for (int j = 0; j < s.trim().length(); j++) {
				char c = s.charAt(j); 
				if((int)c == 60){
					buffer.append("&lt;") ;
				} else if((int)c == 62){
					buffer.append("&gt;") ;
				} else if(c == '\''){
					buffer.append("&#39") ;
				} else {
					buffer.append(c) ;
				}
			}
		}
		return buffer.toString();
	}
	
	public static String unCodeHTML(String s) {
		if(s != null && s.trim().length() > 0) {
			s = s.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&#39", "'");
		}
		return s;
	}
}
