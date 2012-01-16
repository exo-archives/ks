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

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.spi.BBCodeData;
import org.exoplatform.ks.bbcode.spi.BBCodeProvider;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestBBCodeRenderer extends TestCase {

  public void testProcessTag() {
    BBCodeRenderer renderer = new BBCodeRenderer();

    // simple case
    BBCode bbcode = createBBCode("I", "<i>{param}</i>");
    String actual = renderer.processTag("[I]italic[/I]", bbcode);
    assertEquals("<i>italic</i>", actual);

    // several occurrences
    actual = renderer.processTag("[I]foo[/I] ... [I]bar[/I]", bbcode);
    assertEquals("<i>foo</i> ... <i>bar</i>", actual);
  }

  public void testRenderLinkAlias() {

    BBCodeRenderer renderer = new BBCodeRenderer();

    renderer.setBbCodeProvider(new BBCodeProvider() {

      public Collection<String> getSupportedBBCodes() {

        return Arrays.asList("URL");
      }

      public BBCode getBBCode(String tagName) {
        BBCode bbcode = new BBCode();
        bbcode.setTagName("URL");
        bbcode.setReplacement("<a href='{option}'>{param}</a>");
        bbcode.setOption(true);
        return bbcode;
      }
    });

    String actual = renderer.render("[link=http://www.example.org]example[/link]");
    assertEquals("<a href='http://www.example.org'>example</a>", actual);
  }

  public void testProcessOptionedTag() {

    BBCodeRenderer renderer = new BBCodeRenderer();

    // simple case
    BBCode bbc = createOptionedBBCode("email", "<a href='mailto:{option}'>{param}</a>");
    String markup = "[email=demo@example.com]Click Here to Email me[/email]";
    assertEquals("<a href='mailto:demo@example.com'>Click Here to Email me</a>", renderer.processOptionedTag(markup, bbc));

    // test we can replace several occurences
    bbc = createOptionedBBCode("foo", "foo:{option}>{param}");
    markup = "[foo=bar]blah[/foo] ... [foo=joe]blih[/foo]";
    assertEquals("foo:bar>blah ... foo:joe>blih", renderer.processOptionedTag(markup, bbc));

    // '+' (plus) symbol is removed from option
    bbc = createOptionedBBCode("size", "<font size='{option}'>{param}</font>");
    markup = "[size=+2]foo[/size]";
    assertEquals("<font size='2'>foo</font>", renderer.processOptionedTag(markup, bbc));

    // '"' (quote) symbols are ignored
    bbc = createOptionedBBCode("url", "<a href='{option}'>{param}</a>");
    markup = "[url=\"http://www.example.org\"]foo[/url]";
    assertEquals("<a href='http://www.example.org'>foo</a>", renderer.processOptionedTag(markup, bbc));

    // '&quot;' (html quote entity) are ignored
    bbc = createOptionedBBCode("url", "<a href='{option}'>{param}</a>");
    markup = "[url=&quot;http://www.example.org&quot;]foo[/url]";
    assertEquals("<a href='http://www.example.org'>foo</a>", renderer.processOptionedTag(markup, bbc));

  }

  private BBCode createOptionedBBCode(String tag, String replacement) {
    BBCode bbc = new BBCode();
    bbc.setTagName(tag);
    bbc.setOption(true);
    bbc.setReplacement(replacement);
    return bbc;
  }

  private BBCode createBBCode(String tag, String replacement) {
    BBCode bbc = new BBCode();
    bbc.setTagName(tag);
    bbc.setOption(false);
    bbc.setReplacement(replacement);
    return bbc;
  }

  public void testProcessList() {
    BBCodeData bbcode = new BBCodeData();
    bbcode.setTagName("list");
    bbcode.setIsOption("false");
    BBCodeRenderer renderer = new BBCodeRenderer();
    String actual = renderer.processList("[list][*]list item 1[*]list item 2[/list]");
    assertEquals("<ul><li>list item 1</li><li>list item 2</li></ul>", actual);
  }

  public void testBuiltinBBCodes() {
    BBCodeRenderer renderer = new BBCodeRenderer();
    renderer.setBbCodeProvider(new BuiltinBBCodeProvider());
    assertEquals("<strong>param</strong>", renderer.render("[B]param[/B]"));
    assertEquals("<i>param</i>", renderer.render("[I]param[/I]"));
    assertEquals("<u>param</u>", renderer.render("[U]param[/U]"));
    assertEquals("<font color=\"blue\">param</font>", renderer.render("[COLOR=blue]param[/COLOR]"));
    assertEquals("<font face=\"courier\">param</font>", renderer.render("[FONT=courier]param[/FONT]"));
    assertEquals("<span class=\"highlighted\">param</span>", renderer.render("[HIGHLIGHT]param[/HIGHLIGHT]"));
    assertEquals("<div align=\"left\">param</div>", renderer.render("[LEFT]param[/LEFT]"));
    assertEquals("<div align=\"right\">param</div>", renderer.render("[RIGHT]param[/RIGHT]"));
    assertEquals("<div align=\"center\">param</div>", renderer.render("[CENTER]param[/CENTER]"));
    assertEquals("<div align=\"justify\">param</div>", renderer.render("[JUSTIFY]param[/JUSTIFY]"));
    assertEquals("<a href=\"mailto:param\">param</a>", renderer.render("[EMAIL]param[/EMAIL]"));
    assertEquals("<a href=\"mailto:option\">param</a>", renderer.render("[EMAIL=option]param[/EMAIL]"));
    assertEquals("<a target=\"_blank\" href=\"http://www.exoplatform.org\">http://www.exoplatform.org</a>", renderer.render("[URL]http://www.exoplatform.org[/URL]"));
    assertEquals("<a target=\"_blank\" href=\"http://www.exoplatform.org\">eXo</a>", renderer.render("[URL=http://www.exoplatform.org]eXo[/URL]"));
    assertEquals("<a href=\"http://www.exoplatform.org\">eXo</a>", renderer.render("[GOTO=http://www.exoplatform.org]eXo[/GOTO]"));
    assertEquals("<img border=\"0\" alt=\"param\" src=\"param\" class=\"inlineimg\"/>", renderer.render("[IMG]param[/IMG]"));
    assertEquals("<blockquote>param</blockquote>", renderer.render("[QUOTE]param[/QUOTE]"));
    assertEquals("<blockquote cite=\"author\">param</blockquote>", renderer.render("[QUOTE=author]param[/QUOTE]"));
    assertEquals("<span class=\"option\">param</span>", renderer.render("[CSS=option]param[/CSS]"));
    assertEquals("<font size=\"2\">param</font>", renderer.render("[SIZE=2]param[/SIZE]"));
    assertEquals("<font size=\"2\">param</font>", renderer.render("[SIZE=+2]param[/SIZE]"));
    assertEquals("<font size=\"-2\">param</font>", renderer.render("[SIZE=-2]param[/SIZE]"));
  }

}
