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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.poll.service;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ks.common.conf.ManagedPlugin;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          tu.duy@exoplatform.com
 * Dec 21, 2010  
 */
@Managed
@NameTemplate( { @Property(key = "service", value = "poll"), @Property(key = "view", value = "plugins"), @Property(key = "name", value = "{Name}") })
@ManagedDescription("Plugin that allows to initialize default data for the poll")
public class InitialDefaultDataPlugin extends ManagedPlugin {
  private PollInitialData initialData = new PollInitialData();

  public InitialDefaultDataPlugin(InitParams params) throws Exception {
    setPollInitialData((PollInitialData) params.getObjectParam("livedemo.default.configuration").getObject());
  }

  private void setPollInitialData(PollInitialData object) {
    this.initialData = object;
  }

  public PollInitialData getPollInitialData() {
    return initialData;
  }

}
