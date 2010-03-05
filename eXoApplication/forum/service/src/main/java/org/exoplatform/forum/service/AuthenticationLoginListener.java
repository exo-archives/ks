/*
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
 */
package org.exoplatform.forum.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.conf.SendMessageInfo;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;


/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Nov 23, 2007 3:09:21 PM
 */
public class AuthenticationLoginListener extends Listener<ConversationRegistry, ConversationState> {

	public AuthenticationLoginListener() throws Exception {
	
	}

	@Override
	public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
  	ForumService fservice = (ForumService)container.getComponentInstanceOfType(ForumService.class) ;
  	String userId = event.getData().getIdentity().getUserId() ;
  	fservice.userLogin(userId) ;
  	
	}	
	
	private void sendEmailNotification(List<String> addresses, Message message) throws Exception {
		try {
			Calendar cal = new GregorianCalendar();
			PeriodInfo periodInfo = new PeriodInfo(cal.getTime(), null, 1, 86400000);
			String name = String.valueOf(cal.getTime().getTime());
			Class clazz = Class.forName("org.exoplatform.forum.service.conf.SendMailJob");
			JobInfo info = new JobInfo(name, "KnowledgeSuite-forum", clazz);
			ExoContainer container = ExoContainerContext.getCurrentContainer();
			JobSchedulerService schedulerService = (JobSchedulerService) container.getComponentInstanceOfType(JobSchedulerService.class);
			//infoMap_.put(name, new SendMessageInfo(addresses, message));
			schedulerService.addPeriodJob(info, periodInfo);
		} catch (Exception e) {
		}
	}
	
}