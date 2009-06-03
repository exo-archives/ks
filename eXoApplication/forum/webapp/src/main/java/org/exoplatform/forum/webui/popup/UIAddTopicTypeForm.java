/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 1, 2009 - 10:56:38 AM  
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
		events = {
			@EventConfig(listeners = UIAddTopicTypeForm.SaveActionListener.class),
			@EventConfig(listeners = UIAddTopicTypeForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)

public class UIAddTopicTypeForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_TOPICTYPENAME_INPUT = "topicTypeName" ;
	public static final String FIELD_TOPICTYPEICON_TAB = "topicTypeIcon" ;
	public UIAddTopicTypeForm() throws Exception {
		UIFormStringInput topicTypeName = new UIFormStringInput(FIELD_TOPICTYPENAME_INPUT, FIELD_TOPICTYPENAME_INPUT, null);
		topicTypeName.addValidator(MandatoryValidator.class);
		UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector(FIELD_TOPICTYPEICON_TAB, FIELD_TOPICTYPEICON_TAB) ;
		uiIconSelector.setSelectedIcon("IconsView");
		addUIFormInput(topicTypeName);
		addUIFormInput(uiIconSelector) ;
  }
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}





	static	public class CancelActionListener extends EventListener<UIAddTopicTypeForm> {
		public void execute(Event<UIAddTopicTypeForm> event) throws Exception {
			UIAddTopicTypeForm topicTypeForm = event.getSource();
			UIPopupContainer popupContainer = topicTypeForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static	public class SaveActionListener extends EventListener<UIAddTopicTypeForm> {
		public void execute(Event<UIAddTopicTypeForm> event) throws Exception {
			UIAddTopicTypeForm topicTypeForm = event.getSource();
			String typeName = topicTypeForm.getUIStringInput(FIELD_TOPICTYPENAME_INPUT).getValue();
			UIFormInputIconSelector uiIconSelector = topicTypeForm.getChild(UIFormInputIconSelector.class);
			String typeIcon = uiIconSelector.getSelectedIcon();
			TopicType topicType = new TopicType();
			topicType.setName(typeName);
			topicType.setIcon(typeIcon);
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			forumService.saveTopicType(topicType);
			UIPopupContainer popupContainer = topicTypeForm.getAncestorOfType(UIPopupContainer.class) ;
			try {
	      UITopicForm topicForm = popupContainer.getChild(UITopicForm.class);
	      topicForm.addNewTopicType();
	      event.getRequestContext().addUIComponentToUpdateByAjax(topicForm) ;
      } catch (Exception e) {
      }
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}


















}
