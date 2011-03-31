/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.forum.service.impl;

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 18, 2009  
 */
@Managed
@NameTemplate( { @Property(key = "service", value = "forum"), @Property(key = "view", value = "jobs"), @Property(key = "name", value = "{Name}") })
@ManagedDescription("Plugin that defines rules for administrator role")
public class JobManager {
  JobDetail jobDetail;

  public JobManager(JobDetail jobDetail) {
    this.jobDetail = jobDetail;
  }

  @Managed
  @ManagedName("Name")
  public String getName() {
    return jobDetail.getName();
  }

  @Managed
  @ManagedName("DataMap")
  public JobDataMap getDataMap() {
    return jobDetail.getJobDataMap();
  }

}
