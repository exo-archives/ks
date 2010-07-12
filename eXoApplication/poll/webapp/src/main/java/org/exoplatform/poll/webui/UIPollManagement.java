/***************************************************************************
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.poll.webui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.webui.popup.UIPollForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 25, 2010, 3:32:09 PM
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/poll/webui/UIPollManagement.gtmpl", 
		events = {
			@EventConfig(listeners = UIPollManagement.EditPollActionListener.class) ,
			@EventConfig(listeners = UIPollManagement.DeletePollActionListener.class) ,
			@EventConfig(listeners = UIPollManagement.AddPollActionListener.class) ,
			@EventConfig(listeners = UIPollManagement.SaveActionListener.class)	
		}
)
public class UIPollManagement extends BasePollForm {
  public static String[] BEAN_FIELD = {"question", "votes", "lastVote", "expire"};
  private static String[] ACTION = {"EditPoll", "DeletePoll"} ;
  Map<String, Poll> mapPoll = new HashMap<String, Poll>();
  
	public UIPollManagement() throws Exception {
		 UIGrid categoryList = addChild(UIGrid.class, null , "PollManagementList") ;
	   categoryList.configure("id", BEAN_FIELD, ACTION) ;
	   categoryList.getUIPageIterator().setId("PollManagementIterator");
	   setActions(new String[]{"AddPoll", "Save"});
	}
	
	public long getCurrentPage() {
    return getChild(UIGrid.class).getUIPageIterator().getCurrentPage() ;
  }
  public long getAvailablePage() {
    return getChild(UIGrid.class).getUIPageIterator().getAvailablePage() ;
  }
  public void setCurrentPage(int page) throws Exception {
    getChild(UIGrid.class).getUIPageIterator().setCurrentPage(page) ;
  }
  
 /* public void processRender(WebuiRequestContext context) throws Exception {
    Writer w =  context.getWriter() ;
    updateGrid();
    w.write("<div id=\"UIPollManagement\" class=\"UIPollManagement\">");
    renderChildren();
    w.write("</div>");
  }*/
  
  public void updateGrid() throws Exception {
		List<Poll> polls = getPollService().getPagePoll();
		for (Poll poll : polls) {
			mapPoll.put(poll.getId(), poll);
		}
		UIGrid uiGrid = getChild(UIGrid.class) ; 
    ObjectPageList objPageList = new ObjectPageList(polls, 10) ;
    uiGrid.getUIPageIterator().setPageList(objPageList) ;  
	}

  static public class EditPollActionListener extends EventListener<UIPollManagement> {
		public void execute(Event<UIPollManagement> event) throws Exception {
			String pollId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIPollManagement pollManagement = event.getSource();
			Poll poll = pollManagement.mapPoll.get(pollId); 
			UIPollPortlet pollPortlet = pollManagement.getParent();
			UIPopupAction popupAction = pollPortlet.getChild(UIPopupAction.class);
			UIPollForm pollForm = popupAction.createUIComponent(UIPollForm.class, null, null);
			pollForm.setUpdatePoll(poll, true);
			popupAction.activate(pollForm, 655, 455, true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

  static public class DeletePollActionListener extends EventListener<UIPollManagement> {
  	public void execute(Event<UIPollManagement> event) throws Exception {
  		String pollId = event.getRequestContext().getRequestParameter(OBJECTID) ;
  		UIPollManagement pollManagement = event.getSource();
  		Poll poll = pollManagement.mapPoll.get(pollId); 
  		UIPollPortlet pollPortlet = pollManagement.getParent();
  		UIPopupAction popupAction = pollPortlet.getChild(UIPopupAction.class);
  		UIPollForm pollForm = popupAction.createUIComponent(UIPollForm.class, null, null);
  		pollForm.setUpdatePoll(poll, true);
  		popupAction.activate(pollForm, 655, 455, true) ;
  		event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  	}
  }

  static public class AddPollActionListener extends EventListener<UIPollManagement> {
  	public void execute(Event<UIPollManagement> event) throws Exception {
  		UIPollManagement pollManagement = event.getSource();
  		UIPollPortlet pollPortlet = pollManagement.getParent();
  		UIPopupAction popupAction = pollPortlet.getChild(UIPopupAction.class);
  		UIPollForm pollForm = popupAction.createUIComponent(UIPollForm.class, null, null);
  		popupAction.activate(pollForm, 655, 455, true) ;
  		event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  	}
  }

  static public class SaveActionListener extends EventListener<UIPollManagement> {
  	public void execute(Event<UIPollManagement> event) throws Exception {
  		UIPollManagement pollManagement = event.getSource();
  		UIPollPortlet pollPortlet = pollManagement.getParent();
  		UIPopupAction popupAction = pollPortlet.getChild(UIPopupAction.class);
  		UIPollForm pollForm = popupAction.createUIComponent(UIPollForm.class, null, null);
  		popupAction.activate(pollForm, 655, 455, true) ;
  		event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  	}
  }
	
}

