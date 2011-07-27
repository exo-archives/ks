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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.ks.common.notify;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.ks.common.Common;
import org.exoplatform.ks.common.NotifyInfo;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NotifyJob implements Job {
  @SuppressWarnings("unused")
  private NotifyInfo notify_ ;
  public NotifyJob() throws Exception {
    
  }

  public void setMessageInfo(NotifyInfo notifyInfo) {
    this.notify_ = notifyInfo ;
  }
  
  private static Log log_ = ExoLogger.getLogger(NotifyJob.class);
  
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      ExoContainer exoContainer = CommonUtils.getExoContainer(context);
      MailService mailService = (MailService)exoContainer.getComponentInstanceOfType(MailService.class) ;
      String name = context.getJobDetail().getName();
      Common common = new Common() ;
      NotifyInfo messageInfo = common.getMessageInfo(name) ;
      List<String> emailAddresses = messageInfo.getEmailAddresses() ;
      Message message = messageInfo.getMessage() ;
      JobSchedulerService schedulerService = (JobSchedulerService)exoContainer.getComponentInstanceOfType(JobSchedulerService.class) ;
      JobInfo info = new JobInfo(name, "KnowledgeSuite", context.getJobDetail().getJobClass());
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
      }
      schedulerService.removeJob(info) ;      

    } catch (Exception e) { 
      log_.error("Failed to execute email notification job", e)  ;  
    }
  }
}
