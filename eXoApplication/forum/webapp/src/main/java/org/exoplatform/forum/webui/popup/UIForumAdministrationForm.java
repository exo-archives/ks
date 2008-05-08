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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 5, 2008 - 9:01:20 AM  
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIFormInputWithActions.gtmpl",
		events = {
			@EventConfig(listeners = UIForumAdministrationForm.SaveActionListener.class), 
			@EventConfig(listeners = UIForumAdministrationForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UIForumAdministrationForm extends UIForm implements UIPopupComponent {
	private ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private ForumAdministration administration ;
	public static final String FIELD_FORUMSORT_TAB = "forumSort" ;
	public static final String FIELD_CENSOREDKEYWORD_TAB = "forumCensor" ;
	
	public static final String FIELD_FORUMSORTBY_INPUT = "forumSortBy" ;
	public static final String FIELD_FORUMSORTBYTYPE_INPUT = "forumSortByType" ;
	public static final String FIELD_TOPICSORTBY_INPUT = "topicSortBy" ;
	public static final String FIELD_TOPICSORTBYTYPE_INPUT = "topicSortByType" ;
	public static final String FIELD_CENSOREDKEYWORD_TEXTAREA = "censorKeyword" ;
	
	public UIForumAdministrationForm() throws Exception {
		this.administration = forumService.getForumAdministration(ForumSessionUtils.getSystemProvider());
		UIFormInputWithActions forumSortTab = new UIFormInputWithActions(FIELD_FORUMSORT_TAB) ;
		UIFormInputWithActions forumCensor = new UIFormInputWithActions(FIELD_CENSOREDKEYWORD_TAB) ;
		
		List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>("Forum Name", "name")) ;
		ls.add(new SelectItemOption<String>("Forum Order", "forumOrder")) ;
		ls.add(new SelectItemOption<String>("Forum Status", "isLock")) ;
		ls.add(new SelectItemOption<String>("Created Date", "createdDate")) ;
		ls.add(new SelectItemOption<String>("Modified Date", "modifiedDate")) ;
		ls.add(new SelectItemOption<String>("Thread Count", "topicCount")) ;
		ls.add(new SelectItemOption<String>("Post Count", "postCount")) ;
		UIFormSelectBox forumSortBy = new UIFormSelectBox(FIELD_FORUMSORTBY_INPUT, FIELD_FORUMSORTBY_INPUT, ls);
		forumSortBy.setValue(administration.getForumSortBy()) ;
		
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>("Ascending", "ascending")) ;
		ls.add(new SelectItemOption<String>("Descending", "descending")) ;
		UIFormSelectBox forumSortByType = new UIFormSelectBox(FIELD_FORUMSORTBYTYPE_INPUT, FIELD_FORUMSORTBYTYPE_INPUT, ls);
		forumSortByType.setValue(administration.getForumSortByType()) ;
		
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>("Thread Name", "name")) ;
		ls.add(new SelectItemOption<String>("Thread Status", "isLock")) ;
		ls.add(new SelectItemOption<String>("Created Date", "createdDate")) ;
		ls.add(new SelectItemOption<String>("Modified Date", "modifiedDate")) ;
		ls.add(new SelectItemOption<String>("Last Post Date", "lastPostDate")) ;
		ls.add(new SelectItemOption<String>("Post Count", "postCount")) ;
		ls.add(new SelectItemOption<String>("View Count", "viewCount")) ;
		ls.add(new SelectItemOption<String>("Number Attachment", "numberAttachments")) ;
		UIFormSelectBox topicSortBy = new UIFormSelectBox(FIELD_TOPICSORTBY_INPUT, FIELD_TOPICSORTBY_INPUT, ls);
		topicSortBy.setValue(administration.getTopicSortBy()) ;
		
		ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>("Ascending", "ascending")) ;
		ls.add(new SelectItemOption<String>("Descending", "descending")) ;
		UIFormSelectBox topicSortByType = new UIFormSelectBox(FIELD_TOPICSORTBYTYPE_INPUT, FIELD_TOPICSORTBYTYPE_INPUT, ls);
		topicSortByType.setValue(administration.getTopicSortByType()) ;
		
		UIFormTextAreaInput censorKeyword = new UIFormTextAreaInput(FIELD_CENSOREDKEYWORD_TEXTAREA, FIELD_CENSOREDKEYWORD_TEXTAREA, null);
		censorKeyword.setValue(administration.getCensoredKeyword()) ;
		
		forumSortTab.addUIFormInput(forumSortBy) ;
		forumSortTab.addUIFormInput(forumSortByType) ;
		forumSortTab.addUIFormInput(topicSortBy) ;
		forumSortTab.addUIFormInput(topicSortByType) ;
		
		forumCensor.addUIFormInput(censorKeyword) ;
		
		addUIFormInput(forumSortTab) ;
		addUIFormInput(forumCensor) ;
		
  }

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class SaveActionListener extends EventListener<UIForumAdministrationForm> {
		public void execute(Event<UIForumAdministrationForm> event) throws Exception {
			UIForumAdministrationForm administrationForm = event.getSource() ;
			UIFormInputWithActions forumSortTab = administrationForm.getChildById(FIELD_FORUMSORT_TAB) ;
			UIFormInputWithActions forumCensor = administrationForm.getChildById(FIELD_CENSOREDKEYWORD_TAB) ;
			String forumSortBy = forumSortTab.getUIFormSelectBox(FIELD_FORUMSORTBY_INPUT).getValue() ;
			String forumSortByType = forumSortTab.getUIFormSelectBox(FIELD_FORUMSORTBYTYPE_INPUT).getValue() ;
			String topicSortBy = forumSortTab.getUIFormSelectBox(FIELD_TOPICSORTBY_INPUT).getValue() ;
			String topicSortByType = forumSortTab.getUIFormSelectBox(FIELD_TOPICSORTBYTYPE_INPUT).getValue() ;
			String censoredKeyword = forumCensor.getUIFormTextAreaInput(FIELD_CENSOREDKEYWORD_TEXTAREA).getValue() ;
			ForumAdministration forumAdministration = administrationForm.administration ;
			forumAdministration.setForumSortBy(forumSortBy) ;
			forumAdministration.setForumSortByType(forumSortByType) ;
			forumAdministration.setTopicSortBy(topicSortBy) ;
			forumAdministration.setTopicSortByType(topicSortByType) ;
			forumAdministration.setCensoredKeyword(censoredKeyword) ;
			administrationForm.forumService.saveForumAdministration(ForumSessionUtils.getSystemProvider(), forumAdministration) ;
			UIForumPortlet forumPortlet = administrationForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIForumAdministrationForm> {
		public void execute(Event<UIForumAdministrationForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
