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
					System.out.println("\n\n NODE_ADDED =======>" + ev.getPath()) ;
					faqService.generateRSS(ev.getPath(), true) ;
				}else if(ev.getType() == Event.PROPERTY_CHANGED) {
					System.out.println("\n\n  PROPERTY_CHANGED =======>" + ev.getPath()) ;
					String propertyPath = ev.getPath() ;
					String nodePath = propertyPath.substring(0, propertyPath.lastIndexOf("/")) ;
					faqService.generateRSS(nodePath, true) ;
				}else if(ev.getType() == Event.NODE_REMOVED) {
					System.out.println("\n\n NODE_REMOVED =======>" + ev.getPath()) ;
					 faqService.generateRSS(ev.getPath(), false) ;
				}
				break ;								
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}		
	}
  
}
