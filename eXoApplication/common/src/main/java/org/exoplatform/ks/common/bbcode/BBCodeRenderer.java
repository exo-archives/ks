/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.ks.common.bbcode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.ks.common.bbcode.core.BuiltinBBCodeProvider;
import org.exoplatform.ks.common.bbcode.spi.BBCodeProvider;
import org.exoplatform.ks.rendering.api.Renderer;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class BBCodeRenderer implements Renderer {
  public static final String BBCODE_SYNTAX_ID = "bbcode";
  protected BBCodeProvider bbCodeProvider;
  
  public BBCodeRenderer() {
    bbCodeProvider = new BuiltinBBCodeProvider();
  }
  
  
  
  public String getSyntax() {
    return BBCODE_SYNTAX_ID;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ks.rendering.api.Renderer#render(java.lang.String)
   */
  public String render(String s) {
    for (BBCode bbcode : getBbcodes()) {
      String bbc = bbcode.getTagName();
      if(bbc.equals("URL")){
        s = StringUtils.replace(s, "[link", "[URL");
        s = StringUtils.replace(s, "[/link]", "[/URL]");
        s = StringUtils.replace(s, "[LINK", "[URL");
        s = StringUtils.replace(s, "[/LINK]", "[/URL]");
      }
      bbc = bbc.toLowerCase();
      if(!bbc.equals("list")){
        if(Boolean.valueOf(bbcode.isOption())){
          s = processOptionedTag(s, bbcode);
        } else {
          s = processTag(s, bbcode);
        }
      } else {
        s = processList(s);
      }
    }
    return s;
  
  }

  String processTag(String s, BBCode bbcode) {
    String bbc = bbcode.getTagName();
    int clsIndex;
    String start;
    String end;
    String str;
    String param;
    int lastIndex = 0, tagIndex = 0;
    start = "[" + bbc + "]";
    end = "[/" + bbc + "]";
    s = StringUtils.replace(s, start.toUpperCase(), start);
    s = StringUtils.replace(s, end.toUpperCase(), end);
    while ((tagIndex = s.indexOf(start, lastIndex)) != -1) {
      lastIndex = tagIndex + 1;
      try {
        clsIndex = s.indexOf(end, tagIndex);
        str = bbcode.getReplacement();
        /*
        if(str == null || str.trim().length() == 0 || str.equals("null")) {
          bbcode.setReplacement(forumService.getBBcode(bbcode.getId()).getReplacement());
        }*/
        str = s.substring(tagIndex + start.length(), clsIndex);
        param = StringUtils.replace(bbcode.getReplacement(), "{param}", str);
        s = StringUtils.replace(s, start + str + end, param);
      } catch (Exception e) {
        continue;
      }
    }
    return s;
  }

  String processOptionedTag(String markup, BBCode bbcode) {
    String bbc = bbcode.getTagName();
    int clsIndex;
    String start;
    String end;
    String str;
    String param;
    String option;
    //System.out.println(bbcode + " > " + bbcode.isOption());
    int lastIndex = 0, tagIndex = 0;
    start = "[" + bbc + "=";
    end = "[/" + bbc + "]";
    markup = StringUtils.replace(markup, start.toUpperCase(), start);
    markup = StringUtils.replace(markup, end.toUpperCase(), end);
    while ((tagIndex = markup.indexOf(start, lastIndex)) != -1) {
      lastIndex = tagIndex + 1;
      try {
        clsIndex = markup.indexOf(end, tagIndex);
        str = bbcode.getReplacement();
        /*
        if(str == null || str.trim().length() == 0 || str.equals("null")) {
          bbcode.setReplacement(forumService.getBBcode(bbcode.getId()).getReplacement());
        }
        */
        str = markup.substring(tagIndex + start.length(), clsIndex);
        option = str.substring(0, str.indexOf("]"));
        if(option.indexOf("+")==0)option = option.replaceFirst("+", "");
        if(option.indexOf("\"")==0)option = option.replaceAll("\"", "");
        if(option.indexOf("&quot;")==0)option = option.replaceAll("&quot;", "");
        param = str.substring(str.indexOf("]")+1);
        param = StringUtils.replace(bbcode.getReplacement(), "{param}", param);
        param = StringUtils.replace(param, "{option}", option.trim());
        markup = StringUtils.replace(markup, start + str + end, param);
      } catch (Exception e) {
        continue;
      }
    }
    return markup;
  }

  String processList(String s) {
    int lastIndex;
    int tagIndex;
    int clsIndex;
    String str;
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
    return s;
  }

  public List<BBCode> getBbcodes() {
    List<BBCode> result = new ArrayList<BBCode>();
    Collection<String> supported = getBbCodeProvider().getSupportedBBCodes();
    for (String tag : supported) {
      result.add(getBbCodeProvider().getBBCode(tag));
    }
    return result;
  }


  private List<BBCode> convert(List<BBCodeData> bbc) {
    List<BBCode> bbcodes = new ArrayList<BBCode>();
    for (BBCodeData bbCodeData : bbc) {
      BBCode bbCode = new BBCode();
      bbCode.setActive(Boolean.valueOf(bbCodeData.getIsActive()));
      bbCode.setDescription(bbCodeData.getDescription());
      bbCode.setExample(bbCodeData.getExample());
      bbCode.setOption(Boolean.valueOf(bbCodeData.getIsOption()));
      bbCode.setReplacement(bbCodeData.getReplacement());
      bbCode.setTagName(bbCodeData.getTagName());
      bbcodes.add(bbCode);
    }
    return bbcodes;
  }


  public BBCodeProvider getBbCodeProvider() {
    return bbCodeProvider;
  }



  public void setBbCodeProvider(BBCodeProvider bbCodeProvider) {
    this.bbCodeProvider = bbCodeProvider;
  }



}
