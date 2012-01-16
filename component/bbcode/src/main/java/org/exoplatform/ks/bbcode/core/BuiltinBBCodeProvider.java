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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.bbcode.core;

import java.util.Collection;
import java.util.HashMap;

import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.spi.BBCodeProvider;

/**
 * Builtin BBCode provider. Porvides definitions for the following standard BBCodes : 
 * I,B,U,COLOR,SIZE,FONT,HIGHLIGHT,LEFT,RIGHT,CENTER,JUSTIFY,EMAIL,URL,GOTO,LIST,IMG,QUOTE,CSS
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class BuiltinBBCodeProvider extends HashMap<String, BBCode> implements BBCodeProvider {

  /**
   * 
   */
  private static final long serialVersionUID = -4330438782881224247L;

  public BuiltinBBCodeProvider() {
    addBBCode("I", "<i>{param}</i>", false);
    addBBCode("B", "<strong>{param}</strong>", false);
    addBBCode("U", "<u>{param}</u>", false);
    addBBCode("COLOR", "<font color='{option}'>{param}</font>", true);
    addBBCode("SIZE", "<font size='{option}'>{param}</font>", true);

    addBBCode("FONT", "<font face='{option}'>{param}</font>", true);
    addBBCode("HIGHLIGHT", "<span class='highlighted'>{param}</span>", false);
    addBBCode("LEFT", "<div align='left'>{param}</div>", false);
    addBBCode("RIGHT", "<div align='right'>{param}</div>", false);
    addBBCode("CENTER", "<div align='center'>{param}</div>", false);
    addBBCode("JUSTIFY", "<div align='justify'>{param}</div>", false);

    addBBCode("EMAIL", "<a href='mailto:{param}'>{param}</a>", false);
    addBBCode("EMAIL", "<a href='mailto:{option}'>{param}</a>", true);

    addBBCode("URL", "<a target='_blank' href='{param}'>{param}</a>", false);
    addBBCode("URL", "<a target='_blank' href='{option}'>{param}</a>", true);

    addBBCode("GOTO", "<a href='{option}'>{param}</a>", true);
    addBBCode("LIST", "", false);
    addBBCode("LIST", "", true);
    addBBCode("IMG", "<img border='0' alt='{param}' src='{param}' class='inlineimg'/>", false);
    addBBCode("QUOTE", "<blockquote>{param}</blockquote>", false);
    addBBCode("QUOTE", "<blockquote cite='{option}'>param</blockquote>", true);
    addBBCode("CSS", "<span class='{option}'>{param}</span>", true);
  }

  private void addBBCode(String tag, String replacement, boolean option) {
    replacement = replacement.replaceAll("'", "\"");
    BBCode bbCode = new BBCode();
    String id = option ? (tag + "=") : tag;
    bbCode.setTagName(tag);
    bbCode.setId(id);
    bbCode.setReplacement(replacement);
    bbCode.setOption(option);
    bbCode.setActive(true); // useless
    super.put(id, bbCode);
  }

  public BBCode getBBCode(String tagName) {
    return get(tagName);
  }

  public Collection<String> getSupportedBBCodes() {
    return keySet();
  }

}
