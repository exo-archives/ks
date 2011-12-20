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

public class ForumRSSEventListener implements EventListener{
  private String path_ ;
  private String workspace_ ;
  private String repository_ ; 
  protected KSDataLocation locator;
  public ForumRSSEventListener(KSDataLocation dataLocator) throws Exception {
    this.locator = dataLocator;
    workspace_ = dataLocator.getWorkspace();
    repository_ = KSDataLocation.DEFAULT_REPOSITORY_NAME;
  }
  
  public String getSrcWorkspace(){  return workspace_ ; }
  public String getRepository(){ return repository_ ; }
  public String getPath(){ return path_ ; }
  public void setPath(String path){  path_  = path ; }
  
  public void onEvent(EventIterator evIter){    
    /*try{
      ForumFeedGenerator process = new ForumFeedGenerator(locator);
      String path = null, path_= "";;
      while(evIter.hasNext()) {
        Event ev = evIter.nextEvent() ;
        path = ev.getPath();
        if(path.indexOf("pruneSetting") > 0) continue;
        if(ev.getType() == Event.NODE_ADDED){
          process.itemAdded(ev.getPath());
          break;
        }else if(ev.getType() == Event.PROPERTY_CHANGED) {
          if(hasProperties(path)) {
            process.itemUpdated(path.substring(0, path.lastIndexOf("/")));
            break;
          }          
        }else if(ev.getType() == Event.NODE_REMOVED) {  
          if(path_.contains(path) || path_.length() == 0) {
            path_ = path;
          }
        }
      }
      if(path_.length() > 0) {
        process.itemRemoved(path_);
      }
    }catch(Exception e) {
      e.printStackTrace() ;
    }  */  
  }
  
  protected boolean hasProperties(String path) {
    String property = path.substring(path.lastIndexOf("/")+1);
    List<String> list = Arrays.asList((new String[]{"exo:message", "exo:name", "exo:isApproved", "exo:isActiveByTopic",
        "exo:isHidden", "exo:isClosed", "exo:isLock", "exo:isWaiting", "exo:isActive", "exo:isActiveByForum"}));
    return (list.contains(property))?true:false;
  }
}

