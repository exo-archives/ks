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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.ks.common.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendMailJob implements Job {
	private static Log log_ = ExoLogger.getLogger("job.forum.SendMailJob");
  public SendMailJob() throws Exception {}
		
	
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
    	ExoContainer exoContainer = Utils.getExoContainer(context);
      MailService mailService = (MailService)exoContainer.getComponentInstanceOfType(MailService.class) ;
      ForumService forumService =(ForumService)exoContainer.getComponentInstanceOfType(ForumService.class) ;
      int countEmail = 0;
      Iterator<SendMessageInfo> iter = forumService.getPendingMessages() ;
      while (iter.hasNext()) {
        try{
          SendMessageInfo messageInfo = iter.next() ;
          List<String> emailAddresses = messageInfo.getEmailAddresses() ;
          Message message = messageInfo.getMessage() ;
          message.setFrom(makeNotificationSender(message.getFrom()));
          
          if(message != null && emailAddresses != null && emailAddresses.size() > 0) {
            List<String> sentMessages = new ArrayList<String>() ;             
            for(String address : emailAddresses) {
              if(!sentMessages.contains(address)) {
                message.setTo(address) ;
                mailService.sendMessage(message) ;
                sentMessages.add(address) ;
                countEmail ++;
              }
            }	              
          }
        } catch(Exception e) {
          log_.error("Could not send email notification", e);  	
        }	          	          
      }
      if (log_.isInfoEnabled() && countEmail > 0) {
        log_.info("\n\nEmail notifications has been sent to " + countEmail + " addresses");
      }
    }catch(Exception e) {
      log_.warn("\n\n Unable send email notification ") ;
    }
  }
  
  
  /**
   * This function will change email address in 'from' field by address of mail service which is configured as system property : <code>gatein.email.smtp.from</code> or <code>mail.from</code>. <br>
   * That ensures that 'emailAddress' part of 'from' field in a message object is always the same identity with authentication of smtp configuration.<br>
   * It's because of 2 reasons:
   *    <li> we don't want notification message to show email address of user as sender. Instead, we use mail service of kernel. </li>
   *    <li> Almost authenticated smtp systems do not allow to separate email address in <code>from</code> field of message from smtp authentication</b> 
   *    (for now, GMX, MS exchange deny, Gmail efforts to modify the such value)
   *    </li>
   * @param from
   * @return null if can not find suitable sender.
   */
  public String makeNotificationSender(String from) {
    InternetAddress addr = null;
    if (from == null) return null;
    try {
      addr = new InternetAddress(from);
    } catch (AddressException e) {
      if (log_.isDebugEnabled()) { log_.debug("value of 'from' field in message made by forum notification feature is not in format of mail address", e); }
      return null;
    }
    Properties props = new Properties(System.getProperties());
    String mailAddr = props.getProperty("gatein.email.smtp.from");
    if (mailAddr == null || mailAddr.length() == 0) mailAddr = props.getProperty("mail.from");
    if (mailAddr != null) {
      try {
        InternetAddress serMailAddr = new InternetAddress(mailAddr);
        addr.setAddress(serMailAddr.getAddress());
        return addr.toUnicodeString();
      } catch (AddressException e) {
        if (log_.isDebugEnabled()) { log_.debug("value of 'gatein.email.smtp.from' or 'mail.from' in configuration file is not in format of mail address", e); }
        return null;
      }
    } else {
      return null;
    }
    
    
  }
}
