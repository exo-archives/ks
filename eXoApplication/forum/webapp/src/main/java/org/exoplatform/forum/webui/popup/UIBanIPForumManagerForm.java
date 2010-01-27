/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 23-12-2008 - 04:17:18  
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIBanIPForumManagerForm.gtmpl",
		events = {
			@EventConfig(listeners = UIBanIPForumManagerForm.AddIpActionListener.class), 
			@EventConfig(listeners = UIBanIPForumManagerForm.OpenPostsActionListener.class), 
			@EventConfig(listeners = UIBanIPForumManagerForm.UnBanActionListener.class, confirm="UIBanIPForumManagerForm.confirm.UnBanIP"), 
			@EventConfig(listeners = UIBanIPForumManagerForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)

public class UIBanIPForumManagerForm extends BaseForumForm implements UIPopupComponent{
	public static final String SEARCH_IP_BAN = "searchIpBan";
	public static final String NEW_IP_BAN_INPUT1 = "newIpBan1";
	public static final String NEW_IP_BAN_INPUT2 = "newIpBan2";
	public static final String NEW_IP_BAN_INPUT3 = "newIpBan3";
	public static final String NEW_IP_BAN_INPUT4 = "newIpBan4";
	public static final String BAN_IP_PAGE_ITERATOR = "IpBanPageIterator";
	private String forumId ;

	private JCRPageList pageList ;
	private UIForumPageIterator pageIterator ;
	public UIBanIPForumManagerForm() throws Exception {
		
		addUIFormInput(new UIFormStringInput(SEARCH_IP_BAN, null));
		addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT1, null)).setMaxLength(3));
		addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT2, null)).setMaxLength(3));
		addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT3, null)).setMaxLength(3));
		addUIFormInput((new UIFormStringInput(NEW_IP_BAN_INPUT4, null)).setMaxLength(3));
		setActions(new String[]{"Cancel"});
		pageIterator = addChild(UIForumPageIterator.class, null, BAN_IP_PAGE_ITERATOR);
  }
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}

	public void setForumId(String forumId) {
	  this.forumId = forumId;
  }
	
	@SuppressWarnings({ "unused", "unchecked" })
  private List<String> getListIpBan() throws Exception {
		List<String> listIpBan = getForumService().getForumBanList(forumId);
		pageList = new ForumPageList(8, listIpBan.size());
		pageList.setPageSize(8);
		pageIterator = this.getChild(UIForumPageIterator.class);
		pageIterator.updatePageList(pageList);
		List<String>list = new ArrayList<String>();
		list.addAll(this.pageList.getPageList(pageIterator.getPageSelected(), listIpBan)) ;
		pageIterator.setSelectPage(pageList.getCurrentPage());
		try {
			if(pageList.getAvailablePage() <= 1) pageIterator.setRendered(false);
			else  pageIterator.setRendered(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * Validates an IP Address. IP elements must be 4 integers between 0 and 255.
	 * 255.255.255.255 is not a valid IP address;
	 * @param ipAdd elements of the address
	 * @return null if the address is not valid
	 */
	protected String checkIpAddress(String[] ipAdd){
		StringBuffer sbip = new StringBuffer();
		try{
		  if (ipAdd.length != 4) {
		    return null;
		  }
			int[] ips = new int[4];
			for(int t = 0; t < ipAdd.length; t ++){
				if(t>0) sbip.append(".");
				sbip.append(ipAdd[t]);
				ips[t] = Integer.parseInt(ipAdd[t]);
			}
			for(int i = 0; i < 4; i ++){
				if(ips[i] < 0 || ips[i] > 255) return null;
			}
			if(ips[0] == 255 && ips[1] == 255 && ips[2] == 255 && ips[3] == 255) return null;
			return sbip.toString();
		} catch (Exception e){
			return null;
		}
	}
	
	static	public class AddIpActionListener extends EventListener<UIBanIPForumManagerForm> {
		public void execute(Event<UIBanIPForumManagerForm> event) throws Exception {
			UIBanIPForumManagerForm ipManagerForm = event.getSource();
			String[] ip = new String[]{((UIFormStringInput)ipManagerForm.getChildById(NEW_IP_BAN_INPUT1)).getValue(),
																	((UIFormStringInput)ipManagerForm.getChildById(NEW_IP_BAN_INPUT2)).getValue(),
																	((UIFormStringInput)ipManagerForm.getChildById(NEW_IP_BAN_INPUT3)).getValue(),
																	((UIFormStringInput)ipManagerForm.getChildById(NEW_IP_BAN_INPUT4)).getValue(),
																	};
			for(int i = 1; i <= 4; i ++){
				((UIFormStringInput)ipManagerForm.getChildById("newIpBan" + i)).setValue("");
			}
			UIApplication uiApp = ipManagerForm.getAncestorOfType(UIApplication.class) ;
			String ipAdd = ipManagerForm.checkIpAddress(ip);
			if(ipAdd == null){
				uiApp.addMessage(new ApplicationMessage("UIForumAdministrationForm.sms.ipInvalid", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			} 
			if(!ipManagerForm.getForumService().addBanIPForum(ipAdd, ipManagerForm.forumId)){
				uiApp.addMessage(new ApplicationMessage("UIBanIPForumManagerForm.sms.ipBanFalse", new Object[]{ipAdd}, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return;
			}
			UIForumPortlet forumPortlet = ipManagerForm.getAncestorOfType(UIForumPortlet.class);
			UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
			topicContainer.setIdUpdate(true);
			event.getRequestContext().addUIComponentToUpdateByAjax(ipManagerForm) ;
		}
	}
	
	static	public class OpenPostsActionListener extends EventListener<UIBanIPForumManagerForm> {
		public void execute(Event<UIBanIPForumManagerForm> event) throws Exception {
			UIBanIPForumManagerForm ipManagerForm = event.getSource();
			String ip = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIPopupContainer popupContainer = ipManagerForm.getAncestorOfType(UIPopupContainer.class);
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIPageListPostByIP viewPostedByUser = popupAction.activate(UIPageListPostByIP.class, 650) ;
			viewPostedByUser.setIp(ip) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class UnBanActionListener extends EventListener<UIBanIPForumManagerForm> {
		public void execute(Event<UIBanIPForumManagerForm> event) throws Exception {
			UIBanIPForumManagerForm ipManagerForm = event.getSource();
			String ip = event.getRequestContext().getRequestParameter(OBJECTID)	;
			ipManagerForm.getForumService().removeBanIPForum(ip, ipManagerForm.forumId);
			UIForumPortlet forumPortlet = ipManagerForm.getAncestorOfType(UIForumPortlet.class);
			UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
			topicContainer.setIdUpdate(true);
			event.getRequestContext().addUIComponentToUpdateByAjax(ipManagerForm) ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIBanIPForumManagerForm> {
		public void execute(Event<UIBanIPForumManagerForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
			topicContainer.setIdUpdate(true);
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer) ;
		}
	}
	
	
	
}
