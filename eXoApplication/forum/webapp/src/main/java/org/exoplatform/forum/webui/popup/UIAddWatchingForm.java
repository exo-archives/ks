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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
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
public class UIAddWatchingForm	extends BaseUIForm	implements UIPopupComponent {
	final static public String EMAIL_ADDRESS = "emails" ;
	public static final String USER_NAME = "userName" ; 
	private String path = "";
	private String type = "";
	private boolean isCategory = false;
	private UIFormMultiValueInputSet uiFormMultiValue = new UIFormMultiValueInputSet(EMAIL_ADDRESS,EMAIL_ADDRESS) ;
	public UIAddWatchingForm() throws Exception {
		UIFormStringInput userName = new UIFormStringInput(USER_NAME, USER_NAME, null);
		addUIFormInput(userName);
	}
	
	public void initForm() throws Exception	{
		List<String> list = new ArrayList<String>() ;
		String userId = UserHelper.getCurrentUser() ;
		if(!ForumUtils.isEmpty(userId)) {
			UIFormStringInput userName = getUIStringInput(USER_NAME) ;
			userName.setEditable(false) ;
			userName.setValue(userId) ;
			CommonContact contact = this.getPersonalContact(userId) ;
			String email = contact.getEmailAddress() ;
			if(!ForumUtils.isEmpty(email))
				list.add(email);
		}
		list.add("");
		this.initMultiValuesField(list);
	}

	public void activate() throws Exception {}
	public boolean isCategory() {
  	return isCategory;
  }

	public void setIsCategory(boolean isCategory) {
  	this.isCategory = isCategory;
  }

	public void deActivate() throws Exception {}
	
	public void setPathNode(String path) {
		this.path = path ;
	}

	public void setType(String type) {this.type = type ;}
	public String getType() { return type;}
	
	private void initMultiValuesField(List<String> list) throws Exception {
		if( uiFormMultiValue != null ) removeChildById(EMAIL_ADDRESS);
		uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
		uiFormMultiValue.setId(EMAIL_ADDRESS) ;
		uiFormMultiValue.setName(EMAIL_ADDRESS) ;
		uiFormMultiValue.setType(UIFormStringInput.class) ;
		uiFormMultiValue.setValue(list) ;
		addUIFormInput(uiFormMultiValue) ;
	}
	
	private CommonContact getPersonalContact(String userId) throws Exception {
	  CommonContact contact = ForumSessionUtils.getPersonalContact(userId) ;
		if(contact == null) {
			contact = new CommonContact() ;
		}
		return contact ;
	}

	static	public class SaveActionListener extends BaseEventListener<UIAddWatchingForm> {
    public void onEvent(Event<UIAddWatchingForm> event, UIAddWatchingForm uiForm, final String objectId) throws Exception {
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			String path = uiForm.path;
			List<String> values = (List<String>) uiForm.uiFormMultiValue.getValue();
			boolean isEmail = true;
			List<String> values_ = new ArrayList<String>();
			if(values.size() > 0) {
				String value = values.get(0);
				values_.add(value) ;
				for (String string : values) {
					if(values_.contains(string)) continue ;
					values_.add(string) ;
					value = value + "," +string;
				}
				isEmail = ForumUtils.isValidEmailAddresses(value) ;
				if(isEmail) {
				} else {
					warning("UIAddMultiValueForm.msg.invalid-field") ;
				}
			} 
			if(values_.size() > 0 && !ForumUtils.isEmpty(path)) {
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				try {
					forumService.addWatch(1, path, values_, UserHelper.getCurrentUser()) ;
				}catch (Exception e) {}
			}
			uiForm.path = "";
			uiForm.initForm() ;
			forumPortlet.cancelAction() ;
			info("UIAddWatchingForm.msg.successfully") ;
			if(uiForm.isCategory()) {
				UICategory category = forumPortlet.findFirstComponentOfType(UICategory.class);
				category.setIsEditCategory(true);
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
		}
	}
	
	static	public class RefreshActionListener extends BaseEventListener<UIAddWatchingForm> {
    public void onEvent(Event<UIAddWatchingForm> event, UIAddWatchingForm uiForm, final String objectId) throws Exception {
			uiForm.initForm() ;
		}
	}
	
	static	public class CancelActionListener extends BaseEventListener<UIAddWatchingForm> {
    public void onEvent(Event<UIAddWatchingForm> event, UIAddWatchingForm uiForm, final String objectId) throws Exception {
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
