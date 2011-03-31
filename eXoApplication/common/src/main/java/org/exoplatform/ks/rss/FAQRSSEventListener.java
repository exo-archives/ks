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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.rss;

import java.util.Arrays;
import java.util.List;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class FAQRSSEventListener implements EventListener{

  private String path_ ;
  private String workspace_ ;
  private String repository_ ; 
  private List<String> listPropertyNotGetEvent = Arrays.asList((new String[]{"exo:rssWatching", "ks.rss", "exo:emailWatching",
                                                                             "exo:userWatching"}));
  private KSDataLocation locator;
  
  private Log log = ExoLogger.getLogger(FAQRSSEventListener.class);
  
  public FAQRSSEventListener(KSDataLocation dataLocator) throws Exception {
    workspace_ = dataLocator.getWorkspace() ;
    repository_ = dataLocator.DEFAULT_REPOSITORY_NAME; ;
    this.locator = dataLocator;
  }
  
  public String getSrcWorkspace(){  return workspace_ ; }
  public String getRepository(){ return repository_ ; }
  public String getPath(){ return path_ ; }
  public void setPath(String path){  path_  = path ; }
  
  public void onEvent(EventIterator evIter){    
  /*  try{
      AnswersFeedGenerator process = new AnswersFeedGenerator(locator);
      String path = null;
      String path_= "";
      while(evIter.hasNext()) {
        Event ev = evIter.nextEvent() ;
        path = ev.getPath();
        if(ev.getType() == Event.NODE_ADDED){
          int length = ev.getPath().indexOf("/Question") + 41 ;
          if(length == ev.getPath().length()) {
            process.itemAdded(ev.getPath());
          } else if (ev.getPath().indexOf("/faqCommentHome") > 0 || ev.getPath().indexOf("/faqAnswerHome") > 0) {
            process.itemUpdated(ev.getPath());
          }
        }else if(ev.getType() == Event.PROPERTY_CHANGED) {
          process.itemUpdated(path.substring(0, path.lastIndexOf("/")));
        }else if(ev.getType() == Event.NODE_REMOVED) {
          if(path_.contains(path) || path_.length() == 0) {
            path_ = path;
          }
        }
        //break ;                
      }
      if(path_.length() > 0){
        int length = path.indexOf("/Question") + 41 ;
        if(length == path.length()) {
          process.itemRemoved(path);
        } else if (path.indexOf("/faqCommentHome") > 0 || path.indexOf("/faqAnswerHome") > 0) {
          process.itemUpdated(path.substring(0, path.indexOf("/Question") + 41));
        }
      }
    }catch(Exception e) {
      log.error("Init FAQ RSS fail: ", e);
    }  */  
  }  
}

