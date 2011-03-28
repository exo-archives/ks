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
package org.exoplatform.faq.service.impl;

import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@Managed
@NameTemplate(@Property(key = "service", value = "faq"))
@ManagedDescription("FAQ management")
public class FAQServiceManaged implements ManagementAware {

  private static final Log  log = ExoLogger.getLogger(FAQServiceManaged.class);

  private FAQServiceImpl    faqService;

  private ManagementContext context;

  public FAQServiceManaged(FAQServiceImpl forumService) {
    this.faqService = forumService;
    this.faqService.managed = this;
  }

  public void setContext(ManagementContext context) {
    this.context = context;
  }

}
