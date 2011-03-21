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
package org.exoplatform.wiki.chromattic.ext.ntdef;

import java.util.Date;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Value;

import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Properties;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.MOWService;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 6, 2010  
 */
@PrimaryType(name = "nt:frozenNode")
public abstract class NTFrozenNode {

  private MOWService mowService;

  @Property(name = "jcr:frozenUuid")
  public abstract String getFrozenUuid();

  public abstract void setFrozenUuid(String frozenUuid);

  @OneToMany
  public abstract Map<String, Object> getChildren();

  @Properties
  public abstract Map<String, Object> getProperties();

  // TODO: remove these API when Chromattic support versioning
  public String getAuthor() throws Exception {
    Value value = getPropertyValue(WikiNodeType.Definition.AUTHOR);
    if (value != null) {
      return value.getString();
    } else {
      return null;
    }
  }

  public Date getUpdatedDate() throws Exception {
    Value value = getPropertyValue(WikiNodeType.Definition.UPDATED_DATE);
    if (value != null) {
      return value.getDate().getTime();
    } else {
      return null;
    }
  }
  
  public String getComment() throws Exception {
    Value value = getPropertyValue(WikiNodeType.Definition.COMMENT);
    if (value != null) {
      return value.getString();
    } else {
      return null;
    }
  }

  public void setMOWService(MOWService mowService) {
    this.mowService = mowService;
  }

  @Path
  protected abstract String getPath();

  private Value getPropertyValue(String propertyName) throws Exception {
    Node pageNode = getJCRPageNode();
    if (pageNode.hasProperty(propertyName)) {
      javax.jcr.Property property = pageNode.getProperty(propertyName);
      Value value = property.getValue();
      return value;
    } else {
      return null;
    }
  }

  private Node getJCRPageNode() throws Exception {
    return (Node) mowService.getSession().getJCRSession().getItem(getPath());
  }

}
