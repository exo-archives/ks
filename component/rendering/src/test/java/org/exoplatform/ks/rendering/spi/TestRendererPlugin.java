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
package org.exoplatform.ks.rendering.spi;

import junit.framework.TestCase;

import org.exoplatform.commons.testing.AssertUtils;
import org.exoplatform.commons.testing.Closure;
import org.exoplatform.commons.testing.KernelUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ks.rendering.api.Renderer;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestRendererPlugin extends TestCase {

  public void testConstructor() throws Exception {
    final InitParams params = new InitParams();

    // null param not accepted
    AssertUtils.assertException(new Closure() {
      public void dothis() {
        new RendererPlugin(null);
      }
    });

    // value-param "class" is required
    AssertUtils.assertException(new Closure() {
      public void dothis() {
        new RendererPlugin(params);
      }
    });

    KernelUtils.addValueParam(params, "class", "FOO");
    // class should be an accessible type
    AssertUtils.assertException(new Closure() {
      public void dothis() {
        new RendererPlugin(params);
      }
    });

    RendererPlugin plugin = createSampleRendererPlugin();
    assertEquals(SampleRenderer.class, plugin.getRenderer().getClass());

  }

  public void testCreateRenderer() throws Exception {
    RendererPlugin plugin = createSampleRendererPlugin();
    Renderer actual = plugin.getRenderer();
    assertTrue("renderer should be an instance of Renderer", (actual instanceof Renderer));
  }

  private RendererPlugin createSampleRendererPlugin() {
    final InitParams params = new InitParams();
    KernelUtils.addObjectParam(params, "renderer", new SampleRenderer());
    RendererPlugin plugin = new RendererPlugin(params);
    return plugin;
  }

}
