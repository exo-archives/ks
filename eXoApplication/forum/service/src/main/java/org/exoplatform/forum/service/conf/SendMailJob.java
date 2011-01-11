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
	private static Log	log_	= ExoLogger.getLogger("job.forum.SendMailJob");

	public SendMailJob() throws Exception {
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			ExoContainer exoContainer = Utils.getExoContainer(context);
			MailService mailService = (MailService) exoContainer.getComponentInstanceOfType(MailService.class);
			ForumService forumService = (ForumService) exoContainer.getComponentInstanceOfType(ForumService.class);
			Iterator<SendMessageInfo> iter = forumService.getPendingMessages();
			int countEmail = 0;
			while (iter.hasNext()) {
				try {
					SendMessageInfo messageInfo = iter.next();
					List<String> emailAddresses = messageInfo.getEmailAddresses();
					Message message = messageInfo.getMessage();
					message.setFrom(Utils.makeNotificationSender(message.getFrom()));
					if (message != null && emailAddresses != null && emailAddresses.size() > 0) {
						List<String> sentMessages = new ArrayList<String>();
						for (String address : emailAddresses) {
							if (!sentMessages.contains(address)) {
								message.setTo(address);
								mailService.sendMessage(message);
								sentMessages.add(address);
								countEmail++;
							}
						}
					}
				} catch (Exception e) {
					log_.error("Could not send email notification", e);
				}
			}
			if (log_.isInfoEnabled() && countEmail > 0) {
				log_.info("\n\nEmail notifications has been sent to " + countEmail + " addresses");
			}
		} catch (Exception e) {
			log_.warn("\n\n Unable send email notification ");
		}
	}
}
