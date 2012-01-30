/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.internal.parser.confluence;

import org.apache.commons.lang.StringUtils;
import org.xwiki.rendering.listener.reference.AttachmentResourceReference;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Jan 18, 2012  
 */
public class ConfluenceLink {
  
  private String rawReference;

  private String destinationReference;

  private String anchor;

  private String shortcutName;

  private String shortcutValue;

  private String attachmentName;
  
  private String uriPrefix;

  public String getRawReference() {
    return rawReference;
  }

  public void setRawReference(String rawReference) {
    this.rawReference = rawReference;
  }

  public String getDestinationReference() {
    return destinationReference;
  }

  public void setDestinationReference(String destinationReference) {
    this.destinationReference = destinationReference;
  }

  public String getAnchor() {
    return anchor;
  }

  public void setAnchor(String anchor) {
    this.anchor = anchor;
  }

  public String getShortcutName() {
    return shortcutName;
  }

  public void setShortcutName(String shortcutName) {
    this.shortcutName = shortcutName;
  }

  public String getShortcutValue() {
    return shortcutValue;
  }

  public void setShortcutValue(String shortcutValue) {
    this.shortcutValue = shortcutValue;
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public void setAttachmentName(String attachmentName) {
    this.attachmentName = attachmentName;
  }

  public String getUriPrefix() {
    return uriPrefix;
  }

  public void setUriPrefix(String uriPrefix) {
    this.uriPrefix = uriPrefix;
  }

  public ResourceReference toResourceReference() {
    if (!StringUtils.isEmpty(this.attachmentName)) {
      return new AttachmentResourceReference(this.attachmentName);
    }
    if (!StringUtils.isEmpty(this.uriPrefix)) {
      if (this.uriPrefix.equals(ResourceType.MAILTO.toString())) {
        return new ResourceReference(this.rawReference, ResourceType.MAILTO);
      }
      return new ResourceReference(this.rawReference, ResourceType.URL);
    }
    if (!StringUtils.isEmpty(this.destinationReference)) {
      DocumentResourceReference documentReference = new DocumentResourceReference(this.destinationReference);
      if (!StringUtils.isEmpty(this.anchor)) {
        documentReference.setAnchor(this.anchor);
      }
      return documentReference;
    }
    return new ResourceReference(this.rawReference, ResourceType.UNKNOWN);
  }
  
}
