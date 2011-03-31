/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 *         
 * Dec 5, 2007 11:00:12 AM
 */

public class Tag {
  private String   id;

  private String   name;

  private String[] userTag;

  private long     useCount = 0;

  public Tag() {
    id = Utils.TAG + IdGenerator.generate();
  }

  public String getId() {
    return id;
  }

  public void setId(String s) {
    id = s;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String[] getUserTag() {
    return userTag;
  }

  public void setUserTag(String[] userTag) {
    this.userTag = userTag;
  }

  public long getUseCount() {
    return useCount;
  }

  public void setUseCount(long useCount) {
    this.useCount = useCount;
  }
}
