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
package org.exoplatform.faq.service.notify;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NotifyJob implements Job{
  private static Log log_ = ExoLogger.getLogger("job.RecordsJob");
  
  public NotifyJob() throws Exception {}
  @SuppressWarnings("deprecation")
  public void execute(JobExecutionContext context) throws JobExecutionException {
	  try {
	  	//RootContainer rootContainer = RootContainer.getInstance() ;
	  	ExoContainer container = ExoContainerContext.getCurrentContainer();
	  	
	    MailService mailService = (MailService)container.getComponentInstanceOfType(MailService.class) ;
	    FAQService faqService = (FAQService)container.getComponentInstanceOfType(FAQService.class) ;
	    String name = context.getJobDetail().getName();
	    
	    NotifyInfo messageInfo = faqService.getMessageInfo(name) ;
	    List<String> emailAddresses = messageInfo.getEmailAddresses() ;
	    Message message = messageInfo.getMessage() ;
	    
		  JobSchedulerService schedulerService = (JobSchedulerService)container.getComponentInstanceOfType(JobSchedulerService.class) ;
		  		  
		  JobInfo info = new JobInfo(name, "KnowledgeSuite-faq", context.getJobDetail().getJobClass());
		  if(message != null && emailAddresses != null && emailAddresses.size() > 0) {
		  	List<String> sentMessages = new ArrayList<String>() ;
		  	int countEmail = 0;
		  	for(String address : emailAddresses) {
		  		if(!sentMessages.contains(address)) {
		  			message.setTo(address) ;
			  		mailService.sendMessage(message) ;
			  		sentMessages.add(address) ;
			  		countEmail ++;
		  		}
		  	}
		  	if (log_.isDebugEnabled()) {
		  		log_.debug("\n\nEmail notifications for Thread Save Question have been sent to " + countEmail + " addresses");
		  	}
		  }
		  schedulerService.removeJob(info) ;		  

	  } catch (Exception e) {
		  e.printStackTrace();			
	  }
  }
}
