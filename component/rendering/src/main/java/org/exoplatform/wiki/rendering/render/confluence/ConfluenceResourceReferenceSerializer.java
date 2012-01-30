/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.render.confluence;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.renderer.reference.ResourceReferenceSerializer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 9 Mar 2011  
 */
@Component("confluence/1.0/link")
public class ConfluenceResourceReferenceSerializer implements ResourceReferenceSerializer {
  /**
   * {@inheritDoc}
   *
   * @see org.xwiki.rendering.renderer.reference.ResourceReferenceTypeSerializer
   */
  @Override
  public String serialize(ResourceReference reference) {
    if (reference == null || StringUtils.isEmpty(reference.getReference()))
      return StringUtils.EMPTY;

    String ref = reference.getReference();
    ResourceType type = reference.getType();
    if (ResourceType.ATTACHMENT.equals(type)) {
      return new StringBuilder("^").append(ref).toString();
    } else if (ResourceType.MAILTO.equals(type)) {
      return new StringBuilder(ResourceType.MAILTO.getScheme()).append(":").append(ref).toString();
    } else {
      return ref;
    }
  }

}
