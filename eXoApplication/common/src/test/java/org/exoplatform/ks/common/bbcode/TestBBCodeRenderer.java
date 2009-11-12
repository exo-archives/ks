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

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestBBCodeRenderer extends TestCase {

  public void testProcessTag() {
    BBCodeData bbcode = new BBCodeData();
    bbcode.setTagName("I");
    bbcode.setReplacement("<i>{param}</i>");
    BBCodeRenderer renderer = new BBCodeRenderer();
    String actual = renderer.processTag("[I]italic[/I]", bbcode);
    assertEquals("<i>italic</i>", actual);
  }
  
  public void testRender() {
    BBCodeData bbcode = new BBCodeData();
    bbcode.setTagName("URL");
    bbcode.setReplacement("<a href='{option}'>{param}</a>");
    bbcode.setIsOption("true");
    BBCodeRenderer renderer = new BBCodeRenderer();
    renderer.setBbcodes(Arrays.asList(new BBCodeData[]{bbcode}));
    String actual = renderer.render("[link=http://www.example.org]example[/link]");
    assertEquals("<a href='http://www.example.org'>example</a>", actual);
  }

  public void testProcessOptionedTag() {
    BBCodeData bbcode = new BBCodeData();
    bbcode.setTagName("email");
    bbcode.setReplacement("<a href='mailto:{option}'>{param}</a>");
    BBCodeRenderer renderer = new BBCodeRenderer();
    String actual=renderer.processOptionedTag("[email=demo@example.com]Click Here to Email me[/email]", bbcode);
    assertEquals("<a href='mailto:demo@example.com'>Click Here to Email me</a>", actual);
  }

  public void testProcessList() {
    BBCodeData bbcode = new BBCodeData();
    bbcode.setTagName("list");
    bbcode.setIsOption("false");
    BBCodeRenderer renderer = new BBCodeRenderer();
    String actual=renderer.processList("[list][*]list item 1[*]list item 2[/list]");
    assertEquals("<ul><li>list item 1</li><li>list item 2</li></ul>", actual); 
  }
  

}
