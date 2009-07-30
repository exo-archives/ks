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
package org.exoplatform.forum.service.user;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.services.log.ExoLogger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AutoPruneJob implements Job{
  private static Log log_ = ExoLogger.getLogger("job.RecordsJob");
  
  public AutoPruneJob() throws Exception {}
  @SuppressWarnings("deprecation")
  public void execute(JobExecutionContext context) throws JobExecutionException {
	  try {
	  	ExoContainer container = ExoContainerContext.getCurrentContainer();
	  	String desc = context.getJobDetail().getDescription();
	  	ForumService forumService = (ForumService)container.getComponentInstanceOfType(ForumService.class) ;
	  	//System.out.println("\n\n >>>>>> AutoPrune Job");
	  	forumService.runPrune(desc) ;
	  	if (log_.isDebugEnabled()) {
	  		log_.debug("\n\nAuto prune has worked on " + desc + " forum");
	  	}
	  } catch (Exception e) {
		  e.printStackTrace();			
	  }
  }
}
