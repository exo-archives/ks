/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.forum.service.conf;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class UpdateDataJob implements Job {
	private static Log log_ = ExoLogger.getLogger("job.RecordsJob");
  public UpdateDataJob() throws Exception {}
		
	
	@SuppressWarnings("deprecation")
  public void execute(JobExecutionContext context) throws JobExecutionException {
	  try {
	  	ExoContainer container = ExoContainerContext.getCurrentContainer();	    
	    ForumService forumService = (ForumService)container.getComponentInstanceOfType(ForumService.class) ;
	    String name = context.getJobDetail().getName();
	    JobDataMap jdatamap = context.getJobDetail().getJobDataMap() ;
	    String path = jdatamap.getString("path") ;
	    forumService.updateForum(path) ;
	    JobSchedulerService schedulerService = (JobSchedulerService)container.getComponentInstanceOfType(JobSchedulerService.class) ;
		  JobInfo info = new JobInfo(name, "KnowledgeSuite-forum", context.getJobDetail().getJobClass());
		  if (log_.isDebugEnabled()) {
	  		log_.debug("\n\nForum statistic updated");
	  	}
		  schedulerService.removeJob(info) ;
	  } catch (Exception e) {
		  e.printStackTrace();			
	  }
  }
}
