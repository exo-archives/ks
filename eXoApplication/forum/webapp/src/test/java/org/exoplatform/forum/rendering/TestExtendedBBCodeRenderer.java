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
package org.exoplatform.forum.rendering;

import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.forum.service.FakeForumService;
import org.exoplatform.ks.common.bbcode.BBCode;
import org.exoplatform.ks.rendering.MarkupRenderingService;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestExtendedBBCodeRenderer extends TestCase {

  private MarkupRenderingService markupRenderingService;
  private ExtendedBBCodeRenderer bbcodeRenderer;
  private FakeForumService service;
  
  protected void setUp() throws Exception {
    super.setUp();
    service = new FakeForumService();  
    this.markupRenderingService = new MarkupRenderingService();
    bbcodeRenderer = new ExtendedBBCodeRenderer();
    markupRenderingService.registerRenderer(bbcodeRenderer);
    bbcodeRenderer.setForumService(service);
  }


    public void testSyncBBCodeCache() throws Exception {
      
      // active BBCodes are cached
      registerBBCode("FOO", "");

      bbcodeRenderer.syncBBCodeCache();
      List<BBCode> actual = bbcodeRenderer.getBbcodes();
      assertEquals("FOO", actual.get(0).getId());
      
      // = prefix for options
      registerBBCode("=BAR", "");
      bbcodeRenderer.syncBBCodeCache();
      BBCode alt = bbcodeRenderer.getBbcodes().get(1);
      assertTrue(alt.isOption());
      assertEquals("BAR_option", alt.getId());
      assertEquals("BAR", alt.getTagName());

    }
    
    private void registerBBCode(String tagName, String replacement) {
      service.addActiveBBCodes(tagName);
      BBCode foo = new BBCode();
      foo.setReplacement(replacement);
      foo.setId(tagName);
      foo.setTagName(tagName);  
      service.setBBCode(tagName, foo);

      // register the BBCode in the extended bbcode renderer
      ExtendedBBCodeRenderer renderer = (ExtendedBBCodeRenderer) markupRenderingService.getRenderer("bbcode");
      renderer.addBBCode(foo);
    }
    

}
