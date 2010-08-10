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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/

package org.exoplatform.forum;

import java.util.ArrayList;
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


	public static String cleanHtmlCode(String sms, List<String> bbcs) {
		if (sms == null || sms.trim().length() <= 0)
			return "";
		sms = StringUtils.replace(sms, "\n", " ");
		// clean bbcode
		List<String> bbcList = new ArrayList<String>();
		bbcList.addAll(bbcs);
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

	public static String getTitleInHTMLCode(String s, List<String> bbcs) {
		if(s == null || s.trim().length() == 0) return "";
		if(s.length() > 500) s = s.substring(0, 500);
		s = s.replaceAll("&nbsp;&nbsp;", "&nbsp;").replaceAll("&nbsp; ", "&nbsp;")
										 .replaceAll(" &nbsp;", "&nbsp;").replaceAll("<br/>", " ");
		s = StringUtils.replace(s, "	", " ");
		s = cleanHtmlCode(s, bbcs);
		s = removeCharterStrange(s);
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
	
	public static String enCodeHTML(String s) {
		StringBuffer buffer = new StringBuffer();
		if(s != null) {
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
		} else s = "";
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
