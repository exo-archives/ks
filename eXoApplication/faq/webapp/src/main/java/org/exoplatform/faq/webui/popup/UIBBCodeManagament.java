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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.faq.service.BBCode;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIWatchContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Oct 7, 2009 - 10:02:51 AM  
 */

@ComponentConfig(
		template =	"app:/templates/faq/webui/popup/UIBBCodeManagament.gtmpl",
		events = {
			@EventConfig(listeners = UIBBCodeManagament.AddBBCodeActionListener.class),
			@EventConfig(listeners = UIBBCodeManagament.EditBBCodeActionListener.class),
			@EventConfig(listeners = UIBBCodeManagament.DeleteBBCodeActionListener.class)
		}
)
@SuppressWarnings("unused")
public class UIBBCodeManagament extends UIContainer {
	private List<BBCode> listBBCode = new ArrayList<BBCode>();
	private FAQService faqService;
	public UIBBCodeManagament() throws Exception {
		faqService = FAQUtils.getFAQService() ;
		setListBBcode();
  }
	
	public List<BBCode> getListBBcode() throws Exception{
		return listBBCode;
	}
	
	private BBCode getBBCode(String bbcId) {
		for (BBCode bbCode : listBBCode) {
	    if(bbCode.getId().equals(bbcId)) return bbCode;
    }
		return new BBCode();
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setListBBcode() throws Exception {
		listBBCode = new ArrayList<BBCode>();
		try {
			listBBCode.addAll(faqService.getAllBBCode());
    } catch (Exception e) {
	    e.printStackTrace();
    }
	}
	
	static public class AddBBCodeActionListener extends EventListener<UIBBCodeManagament> {
		public void execute(Event<UIBBCodeManagament> event) throws Exception {
			UIBBCodeManagament bbCodeManagament = event.getSource() ;			
			UIWatchContainer popupContainer = bbCodeManagament.getAncestorOfType(UIWatchContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIAddBBCodeForm bbcForm = popupAction.activate(UIAddBBCodeForm.class, 670) ;
			bbcForm.setId("AddBBCodeForm") ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class EditBBCodeActionListener extends EventListener<UIBBCodeManagament> {
		public void execute(Event<UIBBCodeManagament> event) throws Exception {
			String bbcId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIBBCodeManagament bbCodeManagament = event.getSource() ;
			BBCode bbCode = bbCodeManagament.getBBCode(bbcId);
			UIWatchContainer popupContainer = bbCodeManagament.getAncestorOfType(UIWatchContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIAddBBCodeForm bbcForm = popupAction.activate(UIAddBBCodeForm.class, 670) ;
			bbcForm.setEditBBcode(bbCode);
			bbcForm.setId("EditBBCodeForm") ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class DeleteBBCodeActionListener extends EventListener<UIBBCodeManagament> {
		public void execute(Event<UIBBCodeManagament> event) throws Exception {
			UIBBCodeManagament bbCodeManagament = event.getSource() ;			
			String bbcId = event.getRequestContext().getRequestParameter(OBJECTID);
			bbCodeManagament.faqService.removeBBCode(bbcId);
			bbCodeManagament.setListBBcode();
			UISettingForm settingForm = bbCodeManagament.getAncestorOfType(UISettingForm.class);
			settingForm.setCheckBoxBBCode(bbCodeManagament.listBBCode);
			event.getRequestContext().addUIComponentToUpdateByAjax(settingForm) ;
		}
	}
}
