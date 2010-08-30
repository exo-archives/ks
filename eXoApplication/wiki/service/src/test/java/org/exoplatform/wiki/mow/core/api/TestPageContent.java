/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.mow.core.api;

import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;


public class TestPageContent extends AbstractMOWTestcase {

  public void testGetPageContent() {
    PageImpl wikipage = createWikiPage(WikiType.PORTAL, "classic", "AddPageContent-001");
    ContentImpl content = wikipage.getContent();
    assertNotNull(content);
    content.setSyntax("xwiki_2.0");
    content.setText("This is a content of page");
    assertEquals(content.getSyntax(), "xwiki_2.0");
    assertEquals(content.getText(), "This is a content of page");
  }

  public void testUpdatePageContent() {
    PageImpl wikipage = createWikiPage(WikiType.PORTAL, "classic", "UpdatePageContent-001");
    ContentImpl content = wikipage.getContent();
    assertNotNull(content);
    content.setSyntax("xwiki_2.0");
    content.setText("This is a content of page");
    assertEquals(content.getSyntax(), "xwiki_2.0");
    assertEquals(content.getText(), "This is a content of page");
    content.setText("This is a content of page - edited");
    content.setSyntax("xwiki_2.1");

    ContentImpl updatedContent = wikipage.getContent();
    assertEquals(updatedContent.getSyntax(), "xwiki_2.1");
    assertEquals(updatedContent.getText(), "This is a content of page - edited");
  }

}
