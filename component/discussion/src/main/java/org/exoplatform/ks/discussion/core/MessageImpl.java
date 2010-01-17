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
package org.exoplatform.ks.discussion.core;

import java.util.Date;
import java.util.List;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.ks.discussion.api.Message;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
@PrimaryType(name="discussion=message")
public abstract class MessageImpl implements Message {

  @Property(name="discussion:author")
  public abstract String getAuthor();

  @Property(name="discussion:body")
  public abstract String getBody();

  @Property(name="discussion:title")
  public abstract String getTitle();
  
  @Property(name="discussion:createdAt")
  public abstract Date getCreatedAt();

  @OneToMany(type=RelationshipType.HIERARCHIC)
  public abstract List<Message> getReplies();

  @ManyToOne
  public abstract Message getParent();
  
}
