/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Nov 19, 2007 9:18:18 AM 
 */

@ComponentConfig(
   template = "app:/templates/faq/webui/UIFAQPageIterator.gtmpl",
   events = {
     @EventConfig(listeners = UIFAQPageIterator.GoPageActionListener.class)
   }
 )

public class UIFAQPageIterator extends UIContainer {
  private JCRPageList pageList ;
  private long page = 1 ;
  private int endTabPage = 0;
  private int beginTabPage = 0;
  public UIFAQPageIterator () throws Exception {
  }
  
  public void updatePageList(JCRPageList pageList ) {
    this.pageList = pageList ;
    page = 1;
    endTabPage = 0;
    beginTabPage = 0;
  }
  
  @SuppressWarnings("unused")
  private List<String> getTotalpage() throws  Exception {
    int max_Page = (int)pageList.getAvailablePage() ;
    if(this.page > max_Page) this.page = max_Page ;
    long page = this.page ;
    if(page <= 3) {
      beginTabPage = 1 ;
      if(max_Page <= 7)
        endTabPage = max_Page ;
      else endTabPage = 7 ;
    } else {
      if(max_Page > (page + 3)) {
        endTabPage = (int) (page + 3) ;
        beginTabPage = (int) (page - 3) ;
      } else {
        endTabPage = max_Page ;
        if(max_Page > 7) beginTabPage = max_Page - 6 ;
        else beginTabPage = 1 ;
      }
    }
    List<String> temp = new ArrayList<String>() ;
    for (int i = beginTabPage; i <= endTabPage; i++) {
      temp.add("" + i) ;
    }
    return temp ;
  }

  public List<Long> getInfoPage() throws Exception {
  	try{
	    List<Long> temp = new ArrayList<Long>() ;
	    temp.add(pageList.getPageSize()) ;//so item/trang
	    temp.add(pageList.getCurrentPage()) ;//so trang hien tai
	    temp.add(pageList.getAvailable()) ;//tong so item
	    temp.add(pageList.getAvailablePage()) ;// so trang toi da
	    return temp ;
  	} catch (NullPointerException npe){
  		return null;
  	}
  } 
  
  public void setSelectPage(long page) {
    this.page = page;
  }
  
  @SuppressWarnings("unused")
  public long getPageSelected() {
    return this.page ;
  }
    
  static public class GoPageActionListener extends EventListener<UIFAQPageIterator> {
    public void execute(Event<UIFAQPageIterator> event) throws Exception {
      UIFAQPageIterator faqPageIterator = event.getSource() ;
      String stateClick = event.getRequestContext().getRequestParameter(OBJECTID).trim() ;
      long maxPage = faqPageIterator.pageList.getAvailablePage() ;
      System.out.println("\n\nmaxPage: " + maxPage + "\nstateClick: " + stateClick);
      long presentPage  = faqPageIterator.page ;
      if(stateClick.equalsIgnoreCase("next")) {
        if(presentPage < maxPage){
          faqPageIterator.page = presentPage + 1 ;
        }
      } else if(stateClick.equalsIgnoreCase("previous")){
        if(presentPage > 1){
          faqPageIterator.page = presentPage - 1 ;
        }
      } else if(stateClick.equalsIgnoreCase("last")) {
        if(presentPage != maxPage) {
          faqPageIterator.page = maxPage ;
        }
      } else if(stateClick.equalsIgnoreCase("first")) {
        if(presentPage != 1) {
          faqPageIterator.page = 1 ;
        }
      } else {
        long temp = Long.parseLong(stateClick) ;
        if(temp > 0 && temp <= maxPage && temp != presentPage) {
          faqPageIterator.page = temp ;
        }
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(faqPageIterator.getParent()) ;
    }
  }
}
