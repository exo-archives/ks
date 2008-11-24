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
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * May 5, 2008 - 9:01:20 AM	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIForumAdministrationForm.gtmpl",
		events = {
			@EventConfig(listeners = UIForumAdministrationForm.SaveActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.CancelActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UIForumAdministrationForm.SelectTabActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UIForumAdministrationForm.RunActionListener.class)
		}
)
public class UIForumAdministrationForm extends UIForm implements UIPopupComponent {
	private ForumService forumService ;
	private ForumAdministration administration ;
	private int id = 0 ;
	private boolean isRenderListTopic = false ;
	public static final String FIELD_FORUMSORT_TAB = "forumSortTab" ;
	public static final String FIELD_CENSOREDKEYWORD_TAB = "forumCensorTab" ;
	public static final String FIELD_ACTIVETOPIC_TAB = "activeTopicTab" ;
	public static final String FIELD_NOTIFYEMAIL_TAB = "notifyEmailTab" ;
	
	public static final String FIELD_FORUMSORTBY_INPUT = "forumSortBy" ;
	public static final String FIELD_FORUMSORTBYTYPE_INPUT = "forumSortByType" ;
	public static final String FIELD_TOPICSORTBY_INPUT = "topicSortBy" ;
	public static final String FIELD_TOPICSORTBYTYPE_INPUT = "topicSortByType" ;
	
	public static final String FIELD_CENSOREDKEYWORD_TEXTAREA = "censorKeyword" ;
	
	public static final String FIELD_NOTIFYEMAIL_TEXTAREA = "notifyEmail" ;
	
	public static final String FIELD_ACTIVEABOUT_INPUT = "activeAbout" ;
	public static final String FIELD_SETACTIVE_INPUT = "setActive" ;
	
	public UIForumAdministrationForm() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addChild(UIListTopicOld.class, null, null) ;
		this.setActions(new String[]{"Save", "Cancel"}) ;
	}
	
	public void setInit() throws Exception{
		this.administration = forumService.getForumAdministration(ForumSessionUtils.getSystemProvider());
		UIFormInputWithActions forumSortTab = new UIFormInputWithActions(FIELD_FORUMSORT_TAB) ;
		UIFormInputWithActions forumCensorTab = new UIFormInputWithActions(FIELD_CENSOREDKEYWORD_TAB) ;
//		UIFormInputWithActions activeTopicTab = new UIFormInputWithActions(FIELD_ACTIVETOPIC_TAB);
		UIFormInputWithActions notifyEmailTab = new UIFormInputWithActions(FIELD_NOTIFYEMAIL_TAB);
		String []idLables = new String[]{"forumOrder", "isLock", "createdDate",
																"modifiedDate",	"topicCount", "postCount"}; 
		List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(this.getLabel("forumName"), "name")) ;
		for (String string : idLables) {
			ls.add(new SelectItemOption<String>(this.getLabel(string), string)) ;
		}
		UIFormSelectBox forumSortBy = new UIFormSelectBox(FIELD_FORUMSORTBY_INPUT, FIELD_FORUMSORTBY_INPUT, ls);
		forumSortBy.setValue(administration.getForumSortBy()) ;
		
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(this.getLabel("ascending"), "ascending")) ;
		ls.add(new SelectItemOption<String>(this.getLabel("descending"), "descending")) ;
		UIFormSelectBox forumSortByType = new UIFormSelectBox(FIELD_FORUMSORTBYTYPE_INPUT, FIELD_FORUMSORTBYTYPE_INPUT, ls);
		forumSortByType.setValue(administration.getForumSortByType()) ;
		
		idLables = new String[]{"isLock", "createdDate", "modifiedDate", 
				"lastPostDate", "postCount", "viewCount", "numberAttachments"}; 
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(this.getLabel("threadName"), "name")) ;
		for (String string : idLables) {
			ls.add(new SelectItemOption<String>(this.getLabel(string), string)) ;
		}
		
		UIFormSelectBox topicSortBy = new UIFormSelectBox(FIELD_TOPICSORTBY_INPUT, FIELD_TOPICSORTBY_INPUT, ls);
		topicSortBy.setValue(administration.getTopicSortBy()) ;
		
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(this.getLabel("ascending"), "ascending")) ;
		ls.add(new SelectItemOption<String>(this.getLabel("descending"), "descending")) ;
		UIFormSelectBox topicSortByType = new UIFormSelectBox(FIELD_TOPICSORTBYTYPE_INPUT, FIELD_TOPICSORTBYTYPE_INPUT, ls);
		topicSortByType.setValue(administration.getTopicSortByType()) ;
		
		UIFormTextAreaInput censorKeyword = new UIFormTextAreaInput(FIELD_CENSOREDKEYWORD_TEXTAREA, FIELD_CENSOREDKEYWORD_TEXTAREA, null);
		censorKeyword.setValue(administration.getCensoredKeyword()) ;
		
		UIFormStringInput activeAbout = new UIFormStringInput(FIELD_ACTIVEABOUT_INPUT, FIELD_ACTIVEABOUT_INPUT, null);
		activeAbout.setValue("0");
		activeAbout.addValidator(PositiveNumberFormatValidator.class);
		List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
		options.add( new SelectItemOption<String>("true", "true") ) ;
		options.add( new SelectItemOption<String>("false", "false") ) ;
		UIFormRadioBoxInput setActive = new UIFormRadioBoxInput(FIELD_SETACTIVE_INPUT, FIELD_SETACTIVE_INPUT, options);
		setActive.setValue("false") ;
		
		UIFormTextAreaInput notifyEmail = new UIFormTextAreaInput(FIELD_NOTIFYEMAIL_TEXTAREA, FIELD_NOTIFYEMAIL_TEXTAREA, null);
		String value = administration.getNotifyEmailContent();
		if(ForumUtils.isEmpty(value)) value = this.getLabel("notifyEmailContentDefault");
		notifyEmail.setValue(value) ;
		
		forumSortTab.addUIFormInput(forumSortBy) ;
		forumSortTab.addUIFormInput(forumSortByType) ;
		forumSortTab.addUIFormInput(topicSortBy) ;
		forumSortTab.addUIFormInput(topicSortByType) ;
		
		notifyEmailTab.addUIFormInput(notifyEmail) ;
		forumCensorTab.addUIFormInput(censorKeyword) ;
		
		addUIFormInput(activeAbout);
		addUIFormInput(setActive);
		
		addUIFormInput(forumSortTab) ;
		addUIFormInput(forumCensorTab) ;
		addUIFormInput(notifyEmailTab) ;
		//addUIFormInput(activeTopicTab) ;
	}
	
	public boolean isRenderListTopic() {
		return isRenderListTopic;
	}

	public void setRenderListTopic(boolean isRenderListTopic) {
		this.isRenderListTopic = isRenderListTopic;
	}
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	@SuppressWarnings("unused")
	private boolean getIsSelected(int id) {
		if(this.id == id) return true ;
		return false ;
	}
	
	static	public class SaveActionListener extends EventListener<UIForumAdministrationForm> {
		public void execute(Event<UIForumAdministrationForm> event) throws Exception {
			UIForumAdministrationForm administrationForm = event.getSource() ;
			UIFormInputWithActions forumSortTab = administrationForm.getChildById(FIELD_FORUMSORT_TAB) ;
			UIFormInputWithActions forumCensor = administrationForm.getChildById(FIELD_CENSOREDKEYWORD_TAB) ;
			UIFormInputWithActions notifyEmailTab = administrationForm.getChildById(FIELD_NOTIFYEMAIL_TAB) ;
			String forumSortBy = forumSortTab.getUIFormSelectBox(FIELD_FORUMSORTBY_INPUT).getValue() ;
			String forumSortByType = forumSortTab.getUIFormSelectBox(FIELD_FORUMSORTBYTYPE_INPUT).getValue() ;
			String topicSortBy = forumSortTab.getUIFormSelectBox(FIELD_TOPICSORTBY_INPUT).getValue() ;
			String topicSortByType = forumSortTab.getUIFormSelectBox(FIELD_TOPICSORTBYTYPE_INPUT).getValue() ;
			String censoredKeyword = forumCensor.getUIFormTextAreaInput(FIELD_CENSOREDKEYWORD_TEXTAREA).getValue() ;
			censoredKeyword = ForumUtils.removeSpaceInString(censoredKeyword);
			if(!ForumUtils.isEmpty(censoredKeyword)) {
				censoredKeyword = censoredKeyword.toLowerCase();
			}
			String notifyEmail = notifyEmailTab.getUIFormTextAreaInput(FIELD_NOTIFYEMAIL_TEXTAREA).getValue() ;
			ForumAdministration forumAdministration = administrationForm.administration ;
			forumAdministration.setForumSortBy(forumSortBy) ;
			forumAdministration.setForumSortByType(forumSortByType) ;
			forumAdministration.setTopicSortBy(topicSortBy) ;
			forumAdministration.setTopicSortByType(topicSortByType) ;
			forumAdministration.setCensoredKeyword(censoredKeyword) ;
			forumAdministration.setNotifyEmailContent(notifyEmail) ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				administrationForm.forumService.saveForumAdministration(sProvider, forumAdministration) ;
			} finally {
				sProvider.close();
			}
			UIForumPortlet forumPortlet = administrationForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static	public class RunActionListener extends EventListener<UIForumAdministrationForm> {
		public void execute(Event<UIForumAdministrationForm> event) throws Exception {
			UIForumAdministrationForm administrationForm = event.getSource() ;
			String activeAbout = administrationForm.getUIStringInput(FIELD_ACTIVEABOUT_INPUT).getValue() ;
			if(!ForumUtils.isEmpty(activeAbout)) {
				try {
					long date = Long.parseLong(activeAbout) ;
					if(date > 0) {
						administrationForm.setRenderListTopic(true) ;
						UIListTopicOld listTopicOld = administrationForm.getChild(UIListTopicOld.class);
						listTopicOld.setDate(date) ;
						listTopicOld.setIsUpdate(true);
						event.getRequestContext().addUIComponentToUpdateByAjax(administrationForm) ;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	static	public class SelectTabActionListener extends EventListener<UIForumAdministrationForm> {
		public void execute(Event<UIForumAdministrationForm> event) throws Exception {
			String id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIForumAdministrationForm uiForm = event.getSource();
			uiForm.id = Integer.parseInt(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIForumAdministrationForm> {
		public void execute(Event<UIForumAdministrationForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
