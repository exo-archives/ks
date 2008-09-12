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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UIFormSelectBoxForum;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicsTag;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Dec 12, 2007 4:26:06 PM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
		events = {
			@EventConfig(listeners = UIAddTagForm.SaveActionListener.class),
			@EventConfig(listeners = UIAddTagForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UIAddTagForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_TAGNAME_INPUT = "TagName" ;
	public static final String FIELD_TAGDESCRIPTION_TEXTAREA = "TagDescription" ;
	public static final String FIELD_TAGCOLOR_SELECTBOX = "TagColor" ;
	private String colors[] = new String[] {"Blue", "DarkGoldenRod", "Green", "Yellow", "BlueViolet", "Orange","DarkBlue", "IndianRed", "DarkCyan", "LawnGreen", "Violet", "Red"} ;
	private boolean isUpdate = false ;
	private String tagId = "" ;
	private boolean isTopicTag	= false ;
	public UIAddTagForm() throws Exception {
		UIFormStringInput tagName = new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null);
		tagName.addValidator(MandatoryValidator.class);
		UIFormStringInput description = new UIFormTextAreaInput(FIELD_TAGDESCRIPTION_TEXTAREA, FIELD_TAGDESCRIPTION_TEXTAREA, null);
		description.addValidator(MandatoryValidator.class);
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		Map <String, String> newMap = getColorName() ;
		for(String string : this.colors) {
			list.add(new SelectItemOption<String>(newMap.get(string)+"/" + "Color " + string, string)) ;
		}
		UIFormSelectBoxForum tagColor = new UIFormSelectBoxForum(FIELD_TAGCOLOR_SELECTBOX, FIELD_TAGCOLOR_SELECTBOX, list) ;
		tagColor.setDefaultValue("blue");
		
		addUIFormInput(tagName);
		addUIFormInput(tagColor);
		addUIFormInput(description);
	}
	
	public void setUpdateTag(Tag tag) {
		this.isUpdate = true ;
		this.tagId = tag.getId() ;
		getUIStringInput(FIELD_TAGNAME_INPUT).setValue(tag.getName()) ;
		getUIFormTextAreaInput(FIELD_TAGDESCRIPTION_TEXTAREA).setValue(tag.getDescription()) ;
		getUIFormSelectBoxForum(FIELD_TAGCOLOR_SELECTBOX).setValue(tag.getColor()) ;
	}
	
	
	private Map<String, String> getColorName() throws Exception {
		String colorsName[] = new String[] {"Blue", "Dark Golden Rod", "Green", "Yellow", "Blue Violet", "Orange","Dark Blue", "Indian Red","Dark Cyan" ,"Lawn Green", "Violet", "Red"} ;
		TreeMap<String, String> map = new TreeMap<String, String>();
		int i = 0;
		for (String string : this.colors) {
			map.put(string, colorsName[i]);
			++i;
		}
		return map ;	
	}
	
	public void setIsTopicTag(boolean b) {
		this.isTopicTag = b ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public UIFormSelectBoxForum getUIFormSelectBoxForum(String name) {
		return	findComponentById(name) ;
	}
	
	static	public class SaveActionListener extends EventListener<UIAddTagForm> {
		public void execute(Event<UIAddTagForm> event) throws Exception {
			UIAddTagForm uiForm = event.getSource() ;
			UIFormStringInput tagNameInput = uiForm.getUIStringInput(FIELD_TAGNAME_INPUT) ;
			String tagName = tagNameInput.getValue() ;
			int maxText = 50 ;
			if(ForumUtils.isEmpty(tagName)) {
				throw new MessageException(new ApplicationMessage("UIAddTagForm.ms.tagnameisnull", null, ApplicationMessage.WARNING)) ;
			}else if(tagName.length() > maxText){
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				Object[] args = { uiForm.getLabel(FIELD_TAGNAME_INPUT), String.valueOf(maxText) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			
			String color = uiForm.getUIFormSelectBoxForum(FIELD_TAGCOLOR_SELECTBOX).getValue() ;
			String descriptiom = uiForm.getUIFormTextAreaInput(FIELD_TAGDESCRIPTION_TEXTAREA).getValue() ;
			Tag newTag = new Tag() ;
			newTag.setName(tagName) ;
			newTag.setColor(color);
			newTag.setDescription(descriptiom) ;
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			if(uiForm.isUpdate) {
				newTag.setId(uiForm.tagId); 
				forumService.saveTag(ForumSessionUtils.getSystemProvider(), newTag, false);
			} else {
				String userName = ForumSessionUtils.getCurrentUser() ;
				newTag.setOwner(userName) ;
				forumService.saveTag(ForumSessionUtils.getSystemProvider(), newTag, true);
			}
			if(uiForm.isTopicTag) {
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
				uiForm.isTopicTag = false ;
				UITopicsTag topicsTag = forumPortlet.getChild(UITopicsTag.class) ;
				topicsTag.setIdTag(uiForm.tagId) ;
				forumPortlet.cancelAction() ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(uiForm.tagId) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}else {
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				popupContainer.getChild(UITagForm.class).setUpdateList(true) ;
				popupContainer.getChild(UIPopupAction.class).deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIAddTagForm> {
		public void execute(Event<UIAddTagForm> event) throws Exception {
			UIAddTagForm uiForm = event.getSource() ;
			if(uiForm.isTopicTag) {
				uiForm.getAncestorOfType(UIForumPortlet.class).cancelAction() ;
				uiForm.isTopicTag = false ;
			}else {
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				popupContainer.getChild(UIPopupAction.class).deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}
		}
	}
}