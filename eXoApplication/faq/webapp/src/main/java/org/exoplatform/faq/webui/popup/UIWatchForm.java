/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIFormMultiValueInputSet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;

@ComponentConfig(
		lifecycle = UIFormLifecycle.class, 
		template = "app:/templates/faq/webui/popup/UIWatchForm.gtmpl", 
		events = {
				@EventConfig(listeners = UIWatchForm.SaveActionListener.class), 
				@EventConfig(listeners = UIWatchForm.CancelActionListener.class) 
		}
)
public class UIWatchForm extends BaseUIForm implements UIPopupComponent {
	public static final String USER_NAME = "userName";
	public static final String EMAIL_ADDRESS = "emailAddress";
	private String categoryId_ = "";
	private UIFormMultiValueInputSet emailAddress;
	private UIFormStringInput userName;

	public UIWatchForm() throws Exception {
		userName = new UIFormStringInput(USER_NAME, USER_NAME, null);
		emailAddress = createUIComponent(UIFormMultiValueInputSet.class, null, null);
		emailAddress.setId(EMAIL_ADDRESS);
		emailAddress.setName(EMAIL_ADDRESS);
		emailAddress.setType(UIFormStringInput.class);
		addUIFormInput(userName);
		addUIFormInput(emailAddress);
	}

	public String[] getActions() {
		return new String[] { "Save", "Cancel" };
	}

	public void activate() throws Exception {
	}

	public void deActivate() throws Exception {
	}

	public String getCategoryID() {
		return categoryId_;
	}

	public void setCategoryID(String s) {
		categoryId_ = s;
	}

	protected void setWatch(Watch watch) throws Exception {
		String[] values = FAQUtils.splitForFAQ(watch.getEmails());
		emailAddress.setValue(new ArrayList<String>(Arrays.asList(values)));
		UIFormStringInput user = getChildById(USER_NAME);
		user.setValue(watch.getUser());
	}

	static public class SaveActionListener extends EventListener<UIWatchForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UIWatchForm> event) throws Exception {
			UIWatchForm uiWatchForm = event.getSource();
			String name = uiWatchForm.getUIStringInput(USER_NAME).getValue();
			String listEmail = "";
			List<String> values = (List<String>) uiWatchForm.emailAddress.getValue();
			for (String str : values) {
				listEmail += str.trim() + ",";
			}
			if (FAQUtils.isFieldEmpty(name)) {
				uiWatchForm.warning("UIWatchForm.msg.name-field-empty");
				return;
			}
			if (FAQUtils.isFieldEmpty(listEmail)) {
				uiWatchForm.warning("UIWatchForm.msg.to-field-empty");
				return;
			} else if (!FAQUtils.isValidEmailAddresses(listEmail)) {
				uiWatchForm.warning("UIWatchForm.msg.invalid-to-field");
				return;
			}
			String categoryId = uiWatchForm.getCategoryID();

			Watch watch = new Watch();
			watch.setUser(name);
			watch.setEmails(listEmail);
			FAQUtils.getFAQService().addWatchCategory(categoryId, watch);
			UIAnswersPortlet watchContainer = uiWatchForm.getAncestorOfType(UIAnswersPortlet.class);
			UIWatchManager watchManager = watchContainer.findFirstComponentOfType(UIWatchManager.class);
			watchManager.setCategoryID(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(watchManager);
			uiWatchForm.cancelChildPopupAction();
		}
	}

	static public class CancelActionListener extends EventListener<UIWatchForm> {
		public void execute(Event<UIWatchForm> event) throws Exception {
			event.getSource().cancelChildPopupAction();
		}
	}
}