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

import org.exoplatform.contact.service.Contact;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 12, 2008 - 10:15:49 AM  
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
		events = {
			@EventConfig(listeners = UIAddWatchingForm.SaveActionListener.class), 
			@EventConfig(listeners = UIAddWatchingForm.RefreshActionListener.class),
			@EventConfig(listeners = UIAddWatchingForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UIAddWatchingForm  extends UIForm	implements UIPopupComponent {
	final static public String EMAIL_ADDRESS = "emails" ;
	public static final String USER_NAME = "userName" ; 
	private String path = "";
	private UIFormMultiValueInputSet uiFormMultiValue = new UIFormMultiValueInputSet(EMAIL_ADDRESS,EMAIL_ADDRESS) ;
	public UIAddWatchingForm() throws Exception {
  	UIFormStringInput userName = new UIFormStringInput(USER_NAME, USER_NAME, null);
  	addUIFormInput(userName);
  }
	public void activate() throws Exception {
		List<String> list = new ArrayList<String>() ;
		list.add("");
		this.initMultiValuesField(list);
	}
	public void deActivate() throws Exception {}
	
	public void setPathNode(String path) {
	  this.path = path ;
  }
	private void initMultiValuesField(List<String> list) throws Exception {
		if( uiFormMultiValue != null ) removeChildById(EMAIL_ADDRESS);
		uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
		uiFormMultiValue.setId(EMAIL_ADDRESS) ;
		uiFormMultiValue.setName(EMAIL_ADDRESS) ;
		uiFormMultiValue.setType(UIFormStringInput.class) ;
		uiFormMultiValue.setValue(list) ;
//		uiFormMultiValue.addValidator(MandatoryValidator.class);
		addUIFormInput(uiFormMultiValue) ;
	}
	
	private Contact getPersonalContact(String userId) throws Exception {
		Contact contact = ForumSessionUtils.getPersonalContact(userId) ;
		if(contact == null) {
			contact = new Contact() ;
			contact.setId(userId) ;
		}
		return contact ;
	}
	
	static	public class SaveActionListener extends EventListener<UIAddWatchingForm> {
		@SuppressWarnings("unchecked")
    public void execute(Event<UIAddWatchingForm> event) throws Exception {
			UIAddWatchingForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			String path = uiForm.path;
			UserProfile userProfile = forumPortlet.getUserProfile();
			String userId = userProfile.getUserId() ;
			Contact contact = uiForm.getPersonalContact(userId) ;
			String email = contact.getEmailAddress() ;
			List<String> values = (List<String>) uiForm.uiFormMultiValue.getValue();
			if(email != null && email.length() > 0){
				values.add(email) ;
			}
			if(values.size() > 0 && path != null && path.length() > 0) {
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				forumService.addWatch(ForumSessionUtils.getSystemProvider(), 1, path, values) ;
			}
			uiForm.path = "";
			forumPortlet.cancelAction() ;
		}
	}
	
	static	public class RefreshActionListener extends EventListener<UIAddWatchingForm> {
    public void execute(Event<UIAddWatchingForm> event) throws Exception {
    	UIAddWatchingForm uiForm = event.getSource() ;
			List<String> list = new ArrayList<String>() ;
			list.add("");
			uiForm.initMultiValuesField(list);
			uiForm.getUIStringInput(USER_NAME).setValue("") ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIAddWatchingForm> {
    public void execute(Event<UIAddWatchingForm> event) throws Exception {
    	UIAddWatchingForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
	
}
