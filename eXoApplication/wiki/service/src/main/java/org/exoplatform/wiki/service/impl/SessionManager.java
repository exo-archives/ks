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
package org.exoplatform.wiki.service.impl;

import java.util.Hashtable;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 24, 2010  
 */
public class SessionManager extends Hashtable<String, String> {

  private Hashtable<String, Object> context = new Hashtable<String, Object>();
  
  final public String getSessionContainer(String id)
  {
     return get(id);
  }
  
  final public void removeSessionContainer(String id)
  {
     remove(id);
     removeSessionContext(id);
  }
  
  final public void addSessionContainer(String id, String scontainer)
  {
     put(id, scontainer);
  }
  
  final public Object getSessionContext(String id) {
    return this.context.get(id);
  }
  
  final public void addSessionContext(String id, Object context) {
    this.context.put(id, context);
  }

  final public void removeSessionContext(String id) {
    this.context.remove(id);
  }
  
}
