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
		String[] bbcs = new String[] { "B", "I", "HIGHLIGHT", "IMG", "CSS", "URL", "LINK", "GOTO", "QUOTE", 
				"LEFT", "RIGHT", "CENTER", "JUSTIFY", "SIZE", "COLOR", "CSS", "EMAIL", "CODE", "LIST" };
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
		lastIndex = 0;
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
				String href_ = href.trim();
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
				String href_ = href.trim();
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
				b = StringUtils.replace(b, "[email]" + src + "[/email]", "<a href=\"mailto:" + src.trim()	+ "\">" + src + "</a>");
			} catch (Exception e) {
				continue;
			}
		}

		// Custom replaces
		if (b.indexOf("[!bbcode]") >= 0 && b.indexOf("[!v]") < 20) {
			b = StringUtils.replace(b, "[!bbcode]", "");
			b = StringUtils.replace(b, "\r\n", "<br/>\r\n");
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
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[quote]", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/quote]", tagIndex);
				String text = b.substring(tagIndex + 7, clsIndex);
				if(text == null || text.trim().length() == 0) {
					b = StringUtils.replace(b, "[quote]" + text + "[/quote]", "");
					continue;
				}
				buffer = new StringBuffer();
				buffer.append("<div class=\"Classquote\">");
				buffer.append("<div>").append(text).append("</div></div>");
				b = StringUtils.replace(b, "[quote]" + text + "[/quote]", buffer.toString());
			} catch (Exception e) {
				continue;
			}
		}
		tagIndex = 0;
		lastIndex = 0;
		while ((tagIndex = b.indexOf("[quote=", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/quote]", tagIndex);
				String urlStr = b.substring(tagIndex + 7, clsIndex);
				int clsUrl = urlStr.indexOf("]");
				String userName = urlStr.substring(0, clsUrl);
				String text = urlStr.substring(clsUrl + 1);
				if(text == null || text.trim().length() == 0) {
					b = StringUtils.replace(b, "[quote=" + userName + "]" + text + "[/quote]", "");
					continue;
				}
				buffer = new StringBuffer();
				buffer.append("<div class=\"Classquote\">");
				buffer.append("<div>Originally Posted by <strong>").append(StringUtils.remove(userName, '"')).append(
				    "</strong></div>");
				buffer.append("<div>").append(text).append("</div></div>");
				b = StringUtils.replace(b, "[quote=" + userName + "]" + text + "[/quote]", buffer.toString());
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
				String text_ = text.replaceAll("&nbsp;", "&#32");
				buffer = new StringBuffer();
				buffer.append("<div class=\"ClassCode\">");
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
				    + href.trim() + "\">" + text + "</a>");
			} catch (Exception e) {
				continue;
			}
		}
		//List 
		lastIndex = 0;
		tagIndex = 0;
		while ((tagIndex = b.indexOf("[list]", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			try {
				int clsIndex = b.indexOf("[/list]", tagIndex);
				String str = b.substring(tagIndex + 6, clsIndex);
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
				b = StringUtils.replace(b, "[list]" + str + "[/list]", "<ul>" + str_ + "</ul>");
			} catch (Exception e) {
				continue;
			}
		}
		
		lastIndex = 0;
		tagIndex = 0;
		while ((tagIndex = b.indexOf("[list=", lastIndex)) != -1) {
			lastIndex = tagIndex + 1;
			
			try {
				int clsIndex = b.indexOf("[/list]", tagIndex);
				String content = b.substring(tagIndex + 6, clsIndex);
				int clsType = content.indexOf("]");
				String type = content.substring(0, clsType);
				type.replaceAll("\"", "").replaceAll("'", "");
				String str = content.substring(clsType + 1);
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
				b = StringUtils.replace(b, "[list=" + content + "[/list]", "<ol type=\""+type+"\">" + str_ + "</ol>");
			} catch (Exception e) {
				continue;
			}
		}
		return b;
	}

	public static String cleanHtmlCode(String sms) {
		if (sms == null || sms.trim().length() <= 0)
			return "";
		sms = StringUtils.replace(sms, "\n", "");
		List<String> bbcList = new ArrayList<String>();
		// clean bbcode
		String[] bbcs = new String[] { "B", "I", "IMG", "CSS", "URL", "LINK", "GOTO", "QUOTE", "LEFT", "CODE",
		    "RIGHT", "CENTER", "JUSTIFY", "SIZE", "COLOR", "RIGHT", "LEFT", "CENTER", "JUSTIFY", "CSS", "EMAIL", "LIST" };
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
		if(s.length() > 500) s = s.substring(0, 500);
		s = removeCharterStrange(s);
		s = cleanHtmlCode(s);
		s = s.replaceAll("&nbsp;&nbsp;", "&nbsp;").replaceAll("&nbsp; ", "&nbsp;").replaceAll("<br/>", " ");
		s = StringUtils.replace(s, "  ", " ");
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
	
	public static String convertCodeHTML(String s) {
		if (s == null || s.length() <= 0)
			return "";
		s = StringUtils.replace(s, "\n", "");
		s = s.replaceAll("(<p>((\\&nbsp;)*)(\\s*)?</p>)|(<p>((\\&nbsp;)*)?(\\s*)</p>)", "<br/>").trim();
		s = s.replaceFirst("(<br/>)*", "");
	//	s = s.replaceAll("(\\w|\\$)(>?,?\\.?\\*?\\!?\\&?\\%?\\]?\\)?\\}?)(<br/><br/>)*", "$1$2");
		try {
			s = transform(s);
			s = s.replaceAll("(https?|ftp)://", " $0").replaceAll("(=\"|=\'|\'>|\">)(\\s*)(https?|ftp)", "$1$3")
					 .replaceAll("[^=\"|^=\'|^\'>|^\">](https?://|ftp://)([-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])", "<a target=\"_blank\" href=\"$1$2\">$1$2</a>");
		} catch (Exception e) {
			return "";
		}
		return s ;
//		s = s.replaceAll(
//				"[^mailto:\"?|\'?][_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}",
//				"<a target=\"_blank\" href=\"mailto:$0\"> $0 </a>");
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
		s = StringUtils.replace(s, "\n", "");
		s = s.replaceAll("(<p>((\\&nbsp;)*)(\\s*)?</p>)|(<p>((\\&nbsp;)*)?(\\s*)</p>)", "<br/>").trim();
		s = s.replaceFirst("(<br/>)*", "");
		s = s.replaceAll("(\\w|\\$)(>?,?\\.?\\*?\\!?\\&?\\%?\\]?\\)?\\}?)(<br/><br/>)*", "$1$2");
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
	
	public static String enCodeHTML(String s) {
		StringBuffer buffer = new StringBuffer();
		if(s != null) {
			s = StringUtils.replace(s, "\n", "");
			s = s.replaceAll("(<p>((\\&nbsp;)*)(\\s*)?</p>)|(<p>((\\&nbsp;)*)?(\\s*)</p>)", "<br/>").trim();
			s = s.replaceFirst("(<br/>)*", "");
			s = s.replaceAll("(\\w|\\$)(>?,?\\.?\\*?\\!?\\&?\\%?\\]?\\)?\\}?)(<br/><br/>)*", "$1$2");
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
	
	public static String fixAddBBcodeAction(String b) {
		int tagIndex = 0;
		int lastIndex = 0;
		String start;
		String end;
		String text_ = "";
		StringBuilder builder = new StringBuilder();
		String[] tagBBcode = new String[] { "quote", "code", "QUOTE", "CODE" };
		for (int i = 0; i < tagBBcode.length; i++) {
			start = "[" + tagBBcode[i];
			end = "[/" + tagBBcode[i] + "]";
			while ((tagIndex = b.indexOf(start, lastIndex)) != -1) {
				lastIndex = tagIndex + 1;
				try {
					int clsIndex = b.indexOf(end, tagIndex);
					String text = b.substring(tagIndex, clsIndex);
					if (text == null || text.trim().length() == 0)
						continue;
					text_ = text;
					builder = new StringBuilder();
					if (text.indexOf('<' + "p") > text.indexOf('<' + "/p")) {
						text = StringUtils.replaceOnce(text, "</p>", "");
						int t = text.lastIndexOf('<' + "p>");
						builder.append(text.substring(0, t));
						if (text.length() > (t + 3)) {
							builder.append(text.substring(t + 3));
						}
					}
					text = builder.toString();
					if(text != null && text.length() > 0) {
						b = StringUtils.replace(b, text_, text);
						text_ = text;
					} else text = text_;
					
					builder = new StringBuilder();
					if (text.indexOf('<' + "span") > text.indexOf('<' + "/span")) {
						text = StringUtils.replaceOnce(text, "</span>", "");
						int t = text.lastIndexOf('<' + "span");
						builder.append(text.substring(0, t));
						if (text.length() > (t + 6)) {
							builder.append(text.substring((t + 6)));
						}
					}
					text = builder.toString();
					if(text != null && text.length() > 0) {
						b = StringUtils.replace(b, text_, text);
					}
				} catch (Exception e) {
					continue;
				}
			}
		}
		return b;
	}
}
