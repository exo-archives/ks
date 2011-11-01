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
package org.exoplatform.wiki.rendering.reference;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Oct 17, 2011  
 */
@Component("confluence/1.0")
public class ConfluenceObjectReferenceConverter implements ObjectReferenceConverter {

  public static final String wikiPageSeparator = ":";

  public static final String spaceSeparator = ".";

  @Override
  public String convert(String objectReference) {
    int wikiIndex = objectReference.indexOf(wikiPageSeparator);
    if (wikiIndex > 0) {
      String space = objectReference.substring(0, wikiIndex);
      String pageName = objectReference.substring(wikiIndex + 1);
      int spaceIndex = objectReference.indexOf(spaceSeparator);
      if (spaceIndex > 0) {
        String spaceType = space.substring(0, spaceIndex);
        String spaceName = space.substring(spaceIndex + 1);
        space = new StringBuilder(spaceType).append(wikiPageSeparator).append(spaceName).toString();
      } 
      pageName = StringUtils.replace(pageName, ".", "\\.");
      return new StringBuilder(space).append(spaceSeparator).append(pageName).toString();
    } else {
      return StringUtils.replace(objectReference, ".", "\\.");
    }
   
  }
}
