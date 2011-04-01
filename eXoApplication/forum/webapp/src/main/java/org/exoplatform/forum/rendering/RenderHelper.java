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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.Post;
import org.exoplatform.ks.rendering.MarkupRenderingService;
import org.exoplatform.ks.rendering.core.SupportedSyntaxes;
import org.exoplatform.ks.rendering.spi.MarkupRenderDelegate;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class RenderHelper {

  private MarkupRenderingService markupRenderingService;

  public RenderHelper() {
  }

  /**
   * Render markup for a forum Post
   * 
   * @param post
   * @return
   */
  public String renderPost(Post post) {
    try {
      return getMarkupRenderingService().delegateRendering(new PostDelegate(), post);
    } catch (Exception e) {
      throw new RenderingException(e);
    }
  }

  static class PostDelegate implements MarkupRenderDelegate<Post> {

    public String getMarkup(Post post) {
      return post.getMessage();
    }

    /**
     * Note: when Forum will support more syntaxes, we should have the resolving logic here
     */
    public String getSyntax(Post target) {
      return SupportedSyntaxes.bbcode.name();
    }

  }

  public MarkupRenderingService getMarkupRenderingService() {
    if (this.markupRenderingService == null) {
      this.markupRenderingService = (MarkupRenderingService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MarkupRenderingService.class);
    }
    return this.markupRenderingService;
  }

  public void setMarkupRenderingService(MarkupRenderingService markupRenderingService) {
    this.markupRenderingService = markupRenderingService;
  }

}
