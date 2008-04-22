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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UILanguageForm.gtmpl",
		events = {
		  @EventConfig(listeners = UILanguageForm.SelectedLanguageActionListener.class),
			@EventConfig(listeners = UILanguageForm.SaveActionListener.class),
			@EventConfig(listeners = UILanguageForm.CancelActionListener.class)
		}
)
public class UILanguageForm extends UIForm implements UIPopupComponent	{
  private List<String> LIST_LANGUAGE = new ArrayList<String>() ;
  private List<String> LANGAUGE_SELECT = new ArrayList<String>() ;
  
  private boolean isReponse = false ;
  
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public void setResponse(boolean isResponse) {
    this.isReponse = isResponse ;
  }
	 
	public UILanguageForm() throws Exception {
    LIST_LANGUAGE.addAll(Arrays.asList(new String[]{"English", "France", "Vietnamese", "Ukrainnian"})) ;
	}
  
  public void setListSelected(List<String> language) {
    this.LANGAUGE_SELECT.clear();
    this.LANGAUGE_SELECT.addAll(language);
  }
  
  private List<String> getListLanguage(){
    return this.LIST_LANGUAGE ;
  }
  
  private List<String> getListSelected(){
    return this.LANGAUGE_SELECT ;
  }
	
	static public class SelectedLanguageActionListener extends EventListener<UILanguageForm> {
	  public void execute(Event<UILanguageForm> event) throws Exception {
      UILanguageForm languageForm = event.getSource() ;
      String languageIsSelect = event.getRequestContext().getRequestParameter(OBJECTID) ;
      if(languageForm.LANGAUGE_SELECT.contains(languageIsSelect) && languageForm.LANGAUGE_SELECT.size() > 1){
        languageForm.LANGAUGE_SELECT.remove(languageIsSelect) ;
      } else {
        languageForm.LANGAUGE_SELECT.add(languageIsSelect) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(languageForm) ;
	  }
	}
  
	static public class SaveActionListener extends EventListener<UILanguageForm> {
    public void execute(Event<UILanguageForm> event) throws Exception {
			UILanguageForm languageForm = event.getSource() ;
			UIPopupContainer popupContainer = languageForm.getAncestorOfType(UIPopupContainer.class) ;
      if(!languageForm.isReponse) {
  			UIQuestionForm questionForm = popupContainer.getChild(UIQuestionForm.class) ;
  			questionForm.setListLanguage(languageForm.LANGAUGE_SELECT) ;
        questionForm.initPage(true) ;
      } else {
        UIResponseForm responseForm = popupContainer.getChild(UIResponseForm.class) ;
        responseForm.setListLanguageToReponse(languageForm.LANGAUGE_SELECT) ;
        responseForm.initPage(true) ;
      }
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
      //event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
		}
	}

	static public class CancelActionListener extends EventListener<UILanguageForm> {
    public void execute(Event<UILanguageForm> event) throws Exception {
			UILanguageForm uiCategory = event.getSource() ;			
			UIPopupContainer popupContainer = uiCategory.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}