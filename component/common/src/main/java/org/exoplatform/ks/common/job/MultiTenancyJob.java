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
package org.exoplatform.ks.common.job;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by The eXo Platform SAS Author : Lai Trung Hieu
 * hieult@exoplatform.com Aug 5, 2011
 */
public abstract class MultiTenancyJob implements Job {

  private static Log         log_  = ExoLogger.getLogger("cs.ks.job.multitenancy");

  public static final String COLON = ":".intern();

  public abstract Class<? extends MultiTenancyTask> getTask();

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer()
                                                                           .getComponentInstanceOfType(RepositoryService.class);
    List<RepositoryEntry> entries = repoService.getConfig().getRepositoryConfigurations();
    ExecutorService executor = Executors.newFixedThreadPool(entries.size());
    for (RepositoryEntry repositoryEntry : entries) {
      try {
        Constructor constructor = getTask().getConstructor(this.getClass(), JobExecutionContext.class, String.class);
        executor.execute((Runnable) constructor.newInstance(this, context, repositoryEntry.getName()));
      } catch (Exception e) {
        log_.error("Exception when looking for multi-tenancy task", e);
      }
    }
    executor.shutdown();
  }

  public class MultiTenancyTask implements Runnable {

    protected JobExecutionContext context;

    protected PortalContainer     container;

    protected String              repoName;

    public MultiTenancyTask(JobExecutionContext context, String repoName) {
      this.context = context;
      this.repoName = repoName;
    }

    @Override
    public void run() {
      this.container = getPortalContainer(context);

      if (container == null) {
        throw new IllegalStateException("Container is empty");
      }
      ExoContainerContext.setCurrentContainer(container);
      RepositoryService repoService = (RepositoryService) ExoContainerContext.getCurrentContainer()
                                                                             .getComponentInstanceOfType(RepositoryService.class);
      try {
        repoService.setCurrentRepositoryName(repoName);
      } catch (RepositoryConfigurationException e) {
        log_.error("Repository is error", e);
      }
    }
  }

  public static PortalContainer getPortalContainer(JobExecutionContext context) {
    if (context == null)
      return null;
    String portalName = context.getJobDetail().getGroup();
    if (portalName == null)
      return null;
    if (portalName.indexOf(COLON) > 0)
      portalName = portalName.substring(0, portalName.indexOf(":"));
    return RootContainer.getInstance().getPortalContainer(portalName);
  }
}
