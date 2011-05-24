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
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;


public class TestPageContent extends AbstractMOWTestcase {

  public void testGetPageContent() throws Exception {
    PageImpl wikipage = createWikiPage(WikiType.PORTAL, "classic", "AddPageContent-001");
    AttachmentImpl content = wikipage.getContent();
    assertNotNull(content);
    wikipage.setSyntax("xwiki_2.0");
    content.setText("This is a content of page");
    assertEquals(wikipage.getSyntax(), "xwiki_2.0");
    assertEquals(content.getText(), "This is a content of page");
  }

  public void testUpdatePageContent() throws Exception {
    PageImpl wikipage = createWikiPage(WikiType.PORTAL, "classic", "UpdatePageContent-001");
    AttachmentImpl content = wikipage.getContent();
    assertNotNull(content);
    wikipage.setSyntax("xwiki_2.0");
    content.setText("This is a content of page");
    assertEquals(wikipage.getSyntax(), "xwiki_2.0");
    assertEquals(content.getText(), "This is a content of page");
    wikipage.checkin();
    wikipage.checkout();
    content.setText("This is a content of page - edited");
    wikipage.setSyntax("xwiki_2.1");

    AttachmentImpl updatedContent = wikipage.getContent();
    assertEquals(wikipage.getSyntax(), "xwiki_2.1");
    assertEquals(updatedContent.getText(), "This is a content of page - edited");
  }

}
