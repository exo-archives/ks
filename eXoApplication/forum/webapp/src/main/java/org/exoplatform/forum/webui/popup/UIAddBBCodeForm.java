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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Apr 28, 2009 - 9:55:17 AM  
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIAddBBCodeForm.gtmpl",
		events = {
			@EventConfig(listeners = UIAddBBCodeForm.SaveActionListener.class), 
			@EventConfig(listeners = UIAddBBCodeForm.PreviewActionListener.class),
			@EventConfig(listeners = UIAddBBCodeForm.ApplyActionListener.class),
			@EventConfig(listeners = UIAddBBCodeForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UIAddBBCodeForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_TAGNAME_INPUT = "TagName" ;
	public static final String FIELD_REPLACEMENT_TEXTARE = "Replacement" ;
	public static final String FIELD_DESCRIPTION_TEXTARE = "Description" ;
	public static final String FIELD_EXAMPLE_TEXTARE = "Example" ;
	public static final String FIELD_USEOPTION_CHECKBOX = "UseOption" ;
	public static final String PREVIEW = "priview" ;
	private boolean isPriview = false;
	private boolean isEdit = false;
	private ForumService forumService;
	private String example = "";
	private List<BBCode>listBBCode = new ArrayList<BBCode>();
	private BBCode bbcode = new BBCode();
	public UIAddBBCodeForm() throws Exception{
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		UIFormStringInput tagNameInput = new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null);
		tagNameInput.addValidator(MandatoryValidator.class);
		UIFormTextAreaInput replacementInput = new UIFormTextAreaInput(FIELD_REPLACEMENT_TEXTARE, FIELD_REPLACEMENT_TEXTARE, null);
		replacementInput.addValidator(MandatoryValidator.class);
		UIFormTextAreaInput description = new UIFormTextAreaInput(FIELD_DESCRIPTION_TEXTARE, FIELD_DESCRIPTION_TEXTARE, null);
		UIFormTextAreaInput example = new UIFormTextAreaInput(FIELD_EXAMPLE_TEXTARE, FIELD_EXAMPLE_TEXTARE, null);
		example.addValidator(MandatoryValidator.class);
		UIFormCheckBoxInput<Boolean> isOption = new UIFormCheckBoxInput<Boolean>(FIELD_USEOPTION_CHECKBOX, FIELD_USEOPTION_CHECKBOX, false);
		addUIFormInput(tagNameInput);
		addUIFormInput(replacementInput);
		addUIFormInput(description);
		addUIFormInput(example);
		addUIFormInput(isOption);
		this.setActions(new String[]{"Save", "Cancel"});
  }
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	@SuppressWarnings("unused")
  private boolean getPriview() {
		return isPriview;
	}
	
	public void setEditBBcode(BBCode bbcode) throws Exception {
		this.bbcode = bbcode;
		this.isEdit = true;
		this.getUIStringInput(FIELD_TAGNAME_INPUT).setValue(bbcode.getTagName());
		UIFormTextAreaInput replacement = this.getUIFormTextAreaInput(FIELD_REPLACEMENT_TEXTARE);
		replacement.setValue(bbcode.getReplacement());
		if(bbcode.getTagName().equalsIgnoreCase("list")){
			replacement.setEditable(false);
		}
		this.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTARE).setValue(bbcode.getDescription());
		this.getUIFormTextAreaInput(FIELD_EXAMPLE_TEXTARE).setValue(bbcode.getExample());
		this.getUIFormCheckBoxInput(FIELD_USEOPTION_CHECKBOX).setChecked(bbcode.isOption());
  }
	
	@SuppressWarnings("unused")
  private String getReplaceByBBCode() throws Exception {
    return Utils.getReplacementByBBcode(example, listBBCode, null);
	}
	
	static	public class SaveActionListener extends EventListener<UIAddBBCodeForm> {
		public void execute(Event<UIAddBBCodeForm> event) throws Exception {
			UIAddBBCodeForm uiForm = event.getSource();
			String tagName = uiForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue();
			String replacement = uiForm.getUIFormTextAreaInput(FIELD_REPLACEMENT_TEXTARE).getValue();
			String description = uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTARE).getValue();
			String example = uiForm.getUIFormTextAreaInput(FIELD_EXAMPLE_TEXTARE).getValue();
			boolean isOption = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_USEOPTION_CHECKBOX).getValue();
			if(ForumUtils.isEmpty(description)) description = " ";
			uiForm.bbcode.setTagName(tagName.toUpperCase());
			uiForm.bbcode.setReplacement(replacement);
			uiForm.bbcode.setDescription(description);
			uiForm.bbcode.setExample(example);
			uiForm.bbcode.setOption(isOption);
			uiForm.listBBCode = new ArrayList<BBCode>();
			try {
				uiForm.listBBCode.addAll(uiForm.forumService.getAllBBCode());
	    } catch (Exception e) {
	    }
    	for (BBCode code : uiForm.listBBCode) {
	      if(uiForm.bbcode.getTagName().equals(code.getTagName()) && (uiForm.bbcode.isOption() == code.isOption()) &&
	      		!uiForm.bbcode.getId().equals(code.getId())) {
	      	UIApplication uiApplication = uiForm.getAncestorOfType(UIApplication.class) ;
					uiApplication.addMessage(new ApplicationMessage("UIAddBBCodeForm.msg.addDuplicateBBCode", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	      	return;
	      }
      }
			try {
				List<BBCode> bbcodes = new ArrayList<BBCode>();
				bbcodes.add(uiForm.bbcode);
				uiForm.forumService.saveBBCode(bbcodes);
      } catch (Exception e) {
	      e.printStackTrace();
      }
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
			popupAction.deActivate() ;
			UIForumAdministrationForm forumAdministration = popupContainer.getChild(UIForumAdministrationForm.class) ;
			forumAdministration.setListBBcode();
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static	public class PreviewActionListener extends EventListener<UIAddBBCodeForm> {
		public void execute(Event<UIAddBBCodeForm> event) throws Exception {
			UIAddBBCodeForm uiForm = event.getSource();
			String priview = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(priview.equals(PREVIEW)) {
				uiForm.isPriview = true;
				String tagName = uiForm.getUIStringInput(FIELD_TAGNAME_INPUT).getValue();
				String replacement = uiForm.getUIFormTextAreaInput(FIELD_REPLACEMENT_TEXTARE).getValue();
				String description = uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTARE).getValue();
				String example = uiForm.getUIFormTextAreaInput(FIELD_EXAMPLE_TEXTARE).getValue();
				boolean isOption = (Boolean)uiForm.getUIFormCheckBoxInput(FIELD_USEOPTION_CHECKBOX).getValue();
				if(ForumUtils.isEmpty(description)) description = " ";
				uiForm.bbcode.setTagName(tagName);
				uiForm.bbcode.setReplacement(replacement);
				uiForm.bbcode.setDescription(description);
				uiForm.bbcode.setExample(example);
				uiForm.bbcode.setOption(isOption);
				uiForm.listBBCode = new ArrayList<BBCode>();
				try {
					uiForm.listBBCode.addAll(uiForm.forumService.getAllBBCode());
		    } catch (Exception e) {
		    }
		    if(uiForm.isEdit){
		    	int i = 0;
		    	for (BBCode bbc : uiForm.listBBCode) {
	          if(bbc.getId().equals(uiForm.bbcode.getId())) uiForm.listBBCode.set(i, uiForm.bbcode);
	          ++i;
          }
		    } else {
		    	uiForm.listBBCode.add(uiForm.bbcode);
		    }
				uiForm.example = example;
			} else {
				uiForm.isPriview = false;
				uiForm.listBBCode = new ArrayList<BBCode>();
				uiForm.getUIStringInput(FIELD_TAGNAME_INPUT).setValue(uiForm.bbcode.getTagName());
				uiForm.getUIFormTextAreaInput(FIELD_REPLACEMENT_TEXTARE).setValue(uiForm.bbcode.getReplacement());
				uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTARE).setValue(uiForm.bbcode.getDescription());
				uiForm.getUIFormCheckBoxInput(FIELD_USEOPTION_CHECKBOX).setChecked(uiForm.bbcode.isOption());
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	
	static	public class ApplyActionListener extends EventListener<UIAddBBCodeForm> {
		public void execute(Event<UIAddBBCodeForm> event) throws Exception {
			UIAddBBCodeForm uiForm = event.getSource();
			String example = uiForm.getUIFormTextAreaInput(FIELD_EXAMPLE_TEXTARE).getValue();
			uiForm.example = example;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UIAddBBCodeForm> {
		public void execute(Event<UIAddBBCodeForm> event) throws Exception {
			UIAddBBCodeForm addBBCodeForm = event.getSource();
			UIPopupContainer popupContainer = addBBCodeForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
