/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.conf.ForumInitialData;
import org.exoplatform.ks.common.conf.ManagedPlugin;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 23-10-2008 - 07:57:47  
 */
@Managed
@NameTemplate( { @Property(key = "service", value = "forum"), @Property(key = "view", value = "plugins"), @Property(key = "name", value = "{Name}") })
@ManagedDescription("Plugin that allows to initialize default data for the forum")
public class InitializeForumPlugin extends ManagedPlugin {
  private ForumInitialData initialData = new ForumInitialData();

  public InitializeForumPlugin(InitParams params) throws Exception {
    setInitialData((ForumInitialData) params.getObjectParam("livedemo.default.configuration").getObject());
  }

  private void setInitialData(ForumInitialData object) {
    this.initialData = object;
  }

  public ForumInitialData getForumInitialData() {
    return initialData;
  }

}
