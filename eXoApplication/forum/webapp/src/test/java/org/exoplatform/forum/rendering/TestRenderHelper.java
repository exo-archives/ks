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
package org.exoplatform.forum.rendering;

import junit.framework.TestCase;

import org.exoplatform.forum.service.Post;
import org.exoplatform.ks.bbcode.core.BBCodeRenderer;
import org.exoplatform.ks.rendering.MarkupRenderingService;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestRenderHelper extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testRenderPost() {
    MarkupRenderingService service = new MarkupRenderingService();
    service.registerRenderer(new BBCodeRenderer());
    
    
    RenderHelper helper = new RenderHelper();
    helper.setMarkupRenderingService(service);
    
    String message = "this is [b]bold[/bold]";
    Post post = new Post();
    post.setMessage(message);
    
    String actual = helper.renderPost(post);
    String expected = service.getRenderer("bbcode").render(message);
    assertEquals(expected, actual);
    
  }
  
}
