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

import org.apache.commons.lang.StringUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Jun 2, 2008 - 3:33:33 AM	
 */
public class Utils {

	public final static String FORUM_SERVICE = "ForumService".intern() ;
	public final static String FORUM_STATISTIC = "forumStatisticId".intern() ;
	public final static String USER_ADMINISTRATION = "UserAdministration".intern() ;
	public final static String USER_PROFILE = "UserProfile".intern() ;
	public final static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
	
	public final static String FORUMADMINISTRATION = "forumAdministration".intern() ;
	public final static String CATEGORY = "forumCategory".intern() ;
	public final static String FORUM = "forum".intern() ;
	public final static String TOPIC = "topic".intern() ;
	public final static String POST = "post".intern() ;
	public final static String POLL = "poll".intern() ;
	public final static String TAG = "tag".intern() ;
	public final static String AGREEMESSAGE = "agree".intern() ;
	public final static String SENDMESSAGE = "send".intern() ;
	
	public static final String ADMIN = "Administrator".intern() ;
	public static final String MODERATOR = "Moderator".intern() ;
	public static final String USER = "User".intern() ;
	public static final String GUEST = "Guest".intern() ;
	
	public static final String DEFAULT_USER_ADMIN_ID = "root".intern() ;
	public static final String ADMIN_ROLE = "ADMIN".intern() ;

	public static final String DEFAULT_EMAIL_CONTENT = "Hi ,</br> You have received this email because you registered for eXo Forum/Topic " +
	"Watching notification.<br/>We would like to inform that &objectWatch <b>&objectName</b> " +
	"has been added new Post with content below: <div> &content </div> For more detail, you can " +
	"view at link : &link".intern();
	
	
	
	public static String transform(String bbcode) {
		String b = bbcode.substring(0, bbcode.length());

		StringBuffer buffer;
		int lastIndex = 0;
		int tagIndex = 0;
		// Lower Case bbc
		String start, end;
		String[] bbcs = new String[] { "B", "I", "IMG", "CSS", "URL", "LINK", "GOTO", "QUOTE", "LEFT",
				"RIGHT", "CENTER", "SIZE", "COLOR", "RIGHT", "LEFT", "CENTER", "JUSTIFY", "CSS" };
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
		b = StringUtils.replace(b, "[CODE]", "<code>");
		b = StringUtils.replace(b, "[/CODE]", "</code>");
		b = StringUtils.replace(b, "[link]", "[url]");
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
				buffer = new StringBuffer();
				buffer.append("<img src=\"").append(src).append("\" />");
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
		// size
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
				if (size.indexOf("+") >= 0)
					size_ = size_.replace("+", "");
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
				b = StringUtils.replace(b, "[url]" + src + "[/url]", "<a target='_blank' href=\"" + src
						+ "\">" + src + "</a>");
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
				buffer = new StringBuffer();
				buffer.append("<div>Quote:</div><div class=\"Classquote\">");
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
				buffer = new StringBuffer();
				buffer.append("<div>Quote:</div><div class=\"Classquote\">");
				buffer.append("<div>").append(text).append("</div></div>");
				b = StringUtils.replace(b, "[quote]" + text + "[/quote]", buffer.toString());
			} catch (Exception e) {
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
	
	public static String convertCodeHTML(String s) {
		String link = "";
		if (s == null || s.length() <= 0)
			return link;
		try {
			s = transform(s);
			s = s.replaceAll(
					"[^=\"?|\'?](https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
			"<a target=\"blank_\" href=\"$0\">$0</a>");
			s = s.replaceAll(
					"[^mailto:\"?|\'?][_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+\\.[A-Za-z]{2,5}",
			"<a target=\"_blank\" href=\"mailto:$0\"> $0 </a>");
			s = s.replaceAll("href=\" http://", "href=\"http://").replaceAll("href=\">http://", "href=\"http://")
			 .replaceAll(">>http://", ">http://").replaceAll("> http://", ">http://");
    } catch (Exception e) {
    }
		return s;
	}
}
