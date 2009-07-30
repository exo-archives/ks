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
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 22, 2009 - 2:11:20 AM  
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIAutoPruneSettingForm.gtmpl",
		events = {
			@EventConfig(listeners = UIAutoPruneSettingForm.SaveActionListener.class),
			@EventConfig(listeners = UIAutoPruneSettingForm.RunActionListener.class),
			@EventConfig(listeners = UIAutoPruneSettingForm.CloseActionListener.class, phase=Phase.DECODE)
		}
)

public class UIAutoPruneSettingForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_INACTIVEDAY_INPUT = "inActiveDay" ;
	public static final String FIELD_INACTIVEDAY_SELECTBOX = "inActiveDayType" ;
	public static final String FIELD_JOBDAY_INPUT = "jobDay" ;
	public static final String FIELD_JOBDAY_SELECTBOX = "jobDayType" ;
	
	public static final String FIELD_VALUEDAY = "Day" ;
	public static final String FIELD_VALUEWEEKS = "Weeks" ;
	public static final String FIELD_VALUEMONTHS = "Months" ;
	
	private PruneSetting pruneSetting;
	private long topicOld = 0;
	private boolean isTest = false;
	private boolean isActivate = false;
	public UIAutoPruneSettingForm() throws Exception {
		UIFormStringInput inActiveDay = new UIFormStringInput(FIELD_INACTIVEDAY_INPUT, FIELD_INACTIVEDAY_INPUT, null);
		UIFormStringInput jobDay = new UIFormStringInput(FIELD_JOBDAY_INPUT, FIELD_JOBDAY_INPUT, null);
		
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>(getLabel(FIELD_VALUEDAY), FIELD_VALUEDAY)) ;
		list.add(new SelectItemOption<String>(getLabel(FIELD_VALUEWEEKS), FIELD_VALUEWEEKS)) ;
		list.add(new SelectItemOption<String>(getLabel(FIELD_VALUEMONTHS), FIELD_VALUEMONTHS)) ;
		
		UIFormSelectBox inActiveDayType = new UIFormSelectBox(FIELD_INACTIVEDAY_SELECTBOX, FIELD_INACTIVEDAY_SELECTBOX, list) ;
		inActiveDayType.setDefaultValue(FIELD_VALUEDAY);
		
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>(getLabel(FIELD_VALUEDAY), FIELD_VALUEDAY+"_Id")) ;
		list.add(new SelectItemOption<String>(getLabel(FIELD_VALUEWEEKS), FIELD_VALUEWEEKS+"_Id")) ;
		list.add(new SelectItemOption<String>(getLabel(FIELD_VALUEMONTHS), FIELD_VALUEMONTHS+"_Id")) ;
		UIFormSelectBox jobDayType = new UIFormSelectBox(FIELD_JOBDAY_SELECTBOX, FIELD_JOBDAY_SELECTBOX, list) ;
		jobDayType.setDefaultValue(FIELD_VALUEDAY+"_Id");
		
		addUIFormInput(inActiveDay);
		addUIFormInput(inActiveDayType);
		addUIFormInput(jobDay);
		addUIFormInput(jobDayType);
		setActions(new String[]{"Save", "Close"});
  }
	
  public boolean isActivate() {
  	return isActivate;
  }
  public void setActivate(boolean isActivate) {
  	this.isActivate = isActivate;
  }

	@SuppressWarnings("unused")
  private void setInitForm() throws Exception{
		if(!isTest) {
			long i = pruneSetting.getInActiveDay();
			String type = FIELD_VALUEDAY;
			if(i != 0){
				if(i%7 == 0) {i = i/7; type = FIELD_VALUEWEEKS;}
				else if(i%30 == 0) {i = i/30; type = FIELD_VALUEMONTHS;}
			}
		  getUIStringInput(FIELD_INACTIVEDAY_INPUT).setValue(String.valueOf(i));
		  getUIFormSelectBox(FIELD_INACTIVEDAY_SELECTBOX).setValue(type) ;
		  i = pruneSetting.getPeriodTime();
		  type = FIELD_VALUEDAY;
		  if(i != 0){
		  	if(i%7 == 0) {i = i/7; type = FIELD_VALUEWEEKS;}
		  	else if(i%30 == 0) {i = i/30; type = FIELD_VALUEMONTHS;}
		  }
		  type = type+"_Id";
		  getUIStringInput(FIELD_JOBDAY_INPUT).setValue(String.valueOf(i));
		  getUIFormSelectBox(FIELD_JOBDAY_SELECTBOX).setValue(type) ;
		  isTest = false;
		}
  }

	public void setPruneSetting(PruneSetting pruneSetting) {
		this.pruneSetting = pruneSetting ;
	}
	
	public long getTopicOld() {
  	return topicOld;
  }

	public void setTopicOld(long topicOld) {
  	this.topicOld = topicOld;
  }

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	private long convertDay(String type, long date) throws Exception {
		 if(type.equals(FIELD_VALUEMONTHS) || type.equals(FIELD_VALUEMONTHS+"_Id")) date = date*30;
		 else if(type.equals(FIELD_VALUEWEEKS) || type.equals(FIELD_VALUEWEEKS+"_Id")) date = date*7;
		return date;
	}
	
	static	public class SaveActionListener extends EventListener<UIAutoPruneSettingForm> {
		public void execute(Event<UIAutoPruneSettingForm> event) throws Exception {
			UIAutoPruneSettingForm uiform = event.getSource();
			boolean isInactiveDay = false;
			try {
				String date_ = uiform.getUIStringInput(FIELD_INACTIVEDAY_INPUT).getValue();
				String type = uiform.getUIFormSelectBox(FIELD_INACTIVEDAY_SELECTBOX).getValue();
	      uiform.pruneSetting.setInActiveDay(uiform.convertDay(type, Long.parseLong(date_)));
	      isInactiveDay = true;
	      date_ = uiform.getUIStringInput(FIELD_JOBDAY_INPUT).getValue();
	      type = uiform.getUIFormSelectBox(FIELD_JOBDAY_SELECTBOX).getValue();
	      uiform.pruneSetting.setPeriodTime(uiform.convertDay(type, Long.parseLong(date_)));
	      if(uiform.isActivate) {uiform.pruneSetting.setActive(true); uiform.isActivate = false;}
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				forumService.savePruneSetting(uiform.pruneSetting);
				UIPopupContainer popupContainer = uiform.getAncestorOfType(UIPopupContainer.class) ;
				popupContainer.getChild(UIPopupAction.class).deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			} catch (NumberFormatException e) {
				UIApplication uiApp = uiform.getAncestorOfType(UIApplication.class) ;
				String[] args = new String[]{uiform.getLabel(FIELD_INACTIVEDAY_INPUT)};
				if(isInactiveDay)args = new String[]{uiform.getLabel(FIELD_JOBDAY_INPUT)};
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.Invalid-number", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
		}
	}
	
	static	public class RunActionListener extends EventListener<UIAutoPruneSettingForm> {
		public void execute(Event<UIAutoPruneSettingForm> event) throws Exception {
			UIAutoPruneSettingForm uiform = event.getSource();
			
			String date_ = uiform.getUIStringInput(FIELD_INACTIVEDAY_INPUT).getValue();
			String type = uiform.getUIFormSelectBox(FIELD_INACTIVEDAY_SELECTBOX).getValue();
			long date =  uiform.convertDay(type, Long.parseLong(date_));
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			//uiform.topicOld = forumService.getTotalTopicOld(date, uiform.pruneSetting.getForumPath());
			PruneSetting setting = uiform.pruneSetting ;
			setting.setInActiveDay(date) ;
			uiform.topicOld = forumService.checkPrune(setting) ;
			uiform.pruneSetting.setInActiveDay(date);
			uiform.isTest = true;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiform);
		}
	}
	
	static	public class CloseActionListener extends EventListener<UIAutoPruneSettingForm> {
		public void execute(Event<UIAutoPruneSettingForm> event) throws Exception {
			UIAutoPruneSettingForm uiform = event.getSource();
			UIPopupContainer popupContainer = uiform.getAncestorOfType(UIPopupContainer.class) ;
			popupContainer.getChild(UIPopupAction.class).deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
}
