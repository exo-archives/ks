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
package org.exoplatform.faq.service.notify;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.faq.service.FAQService;

public class RSSEventListener implements EventListener{
	private String workspace_ ;
	private String repository_ ; 
	private final int EVENT_ADDNEW = 0;
	private final int EVENT_EDIT = 1;
	private final int EVENT_REMOVE = 2;
	
	public RSSEventListener(String ws, String repo) throws Exception {
		workspace_ = ws ;
		repository_ = repo ;		
	}
	
  public String getSrcWorkspace(){  return workspace_ ; }
  public String getRepository(){ return repository_ ; }
  
	public void onEvent(EventIterator evIter){		
		try{
			ExoContainer container = ExoContainerContext.getCurrentContainer();
			FAQService faqService = (FAQService)container.getComponentInstanceOfType(FAQService.class) ; 
			while(evIter.hasNext()) {
				Event ev = evIter.nextEvent() ;
				if(ev.getType() == Event.NODE_ADDED){
					faqService.generateRSS(ev.getPath(), EVENT_ADDNEW) ;
				}else if(ev.getType() == Event.PROPERTY_CHANGED) {
					String propertyPath = ev.getPath() ;
					String nodePath = propertyPath.substring(0, propertyPath.lastIndexOf("/")) ;
					faqService.generateRSS(nodePath, EVENT_EDIT) ;
				}else if(ev.getType() == Event.NODE_REMOVED) {
					 faqService.generateRSS(ev.getPath(), EVENT_REMOVE) ;
				}
				break ;								
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}		
	}
  
}
