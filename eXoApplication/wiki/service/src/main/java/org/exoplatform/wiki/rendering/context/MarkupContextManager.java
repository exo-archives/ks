/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.context;

import org.exoplatform.wiki.service.WikiContext;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.rendering.listener.reference.ResourceType;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Oct 27, 2011  
 */
@ComponentRole
public interface MarkupContextManager {
  /**
   * Build the markup context for object
   * @param onjectname the name of an object
   * @param type the type of resource
   * @return the wikicontext
   */
  public WikiContext getMarkupContext(String objectName, ResourceType type);
}
