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

import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;

	/**
	 * Created by The eXo Platform SAS
	 * Author : Vu Duy Tu
	 *          tu.duy@exoplatform.com
	 * May 13, 2008 - 8:34:57 AM  
	 */
	@ComponentConfig(
			lifecycle = UIFormLifecycle.class,
			template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
			events = {
				@EventConfig(listeners = UIAddMultiValueForm.SaveActionListener.class), 
				@EventConfig(listeners = UIAddMultiValueForm.RefreshActionListener.class),
				@EventConfig(listeners = UIAddMultiValueForm.CancelActionListener.class,phase = Phase.DECODE)
			}
	)
	public class UIAddMultiValueForm  extends UIForm	implements UIPopupComponent, UISelectComponent {
		final static public String EMAIL_ADDRESS = "emails" ;
		private UIComponent uiComponent ;
	  private String returnFieldName = null ;

		private UIFormMultiValueInputSet uiFormMultiValue = new UIFormMultiValueInputSet(EMAIL_ADDRESS,EMAIL_ADDRESS) ;
		public UIAddMultiValueForm() throws Exception {
	  }
		
		public void setComponent(UIComponent uicomponent, String[] initParams) {
	    uiComponent = uicomponent ;
	    if(initParams == null || initParams.length <= 0) return ;
	    for(int i = 0; i < initParams.length; i ++) {
	      if(initParams[i].indexOf("returnField") > -1) {
	        String[] array = initParams[i].split("=") ;
	        returnFieldName = array[1] ;
	        break ;
	      }
	      returnFieldName = initParams[0] ;
	    }
	  }
		
		public void activate() throws Exception {
			List<String> list = new ArrayList<String>() ;
			list.add("");
			this.initMultiValuesField(list);
		}
		public void deActivate() throws Exception {}
		
		private void initMultiValuesField(List<String> list) throws Exception {
			if( uiFormMultiValue != null ) removeChildById(EMAIL_ADDRESS);
			uiFormMultiValue = createUIComponent(UIFormMultiValueInputSet.class, null, null) ;
			uiFormMultiValue.setId(EMAIL_ADDRESS) ;
			uiFormMultiValue.setName(EMAIL_ADDRESS) ;
			uiFormMultiValue.setType(UIFormStringInput.class) ;
			uiFormMultiValue.setValue(list) ;
			addUIFormInput(uiFormMultiValue) ;
		}
		
		static	public class SaveActionListener extends EventListener<UIAddMultiValueForm> {
			@SuppressWarnings("unchecked")
	    public void execute(Event<UIAddMultiValueForm> event) throws Exception {
				UIAddMultiValueForm uiForm = event.getSource() ;
				List<String> values = (List<String>) uiForm.uiFormMultiValue.getValue();
				List<String> values_ = new ArrayList<String>();
				boolean isEmail = true;
				if(values.size() > 0) {
					String value = values.get(0);
					values_.add(value) ;
					for (String string : values) {
						if(values_.contains(string)) continue ;
	          values_.add(string) ;
	          value = value + "," +string;
          }
					isEmail = ForumFormatUtils.isValidEmailAddresses(value) ;
					if(isEmail) {
						((UISelector)uiForm.uiComponent).updateSelect(uiForm.returnFieldName, value) ;
					} else {
						String[] args = new String[] { "" } ;
						throw new MessageException(new ApplicationMessage("UIAddMultiValueForm.msg.invalid-field", args)) ;
					}
				} else {
					((UISelector)uiForm.uiComponent).updateSelect(uiForm.returnFieldName, "") ;
				}
				UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction uiPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
				if(isEmail) {
		      uiPopup.deActivate() ;
		      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
		      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer.getAncestorOfType(UIPopupAction.class)) ;
				} else {
					event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
				}
			}
		}
		
		static	public class RefreshActionListener extends EventListener<UIAddMultiValueForm> {
	    public void execute(Event<UIAddMultiValueForm> event) throws Exception {
	    	UIAddMultiValueForm uiForm = event.getSource() ;
				List<String> list = new ArrayList<String>() ;
				list.add("");
				uiForm.initMultiValuesField(list);
			}
		}
		
		static	public class CancelActionListener extends EventListener<UIAddMultiValueForm> {
	    public void execute(Event<UIAddMultiValueForm> event) throws Exception {
	    	UIAddMultiValueForm uiForm = event.getSource() ;
	    	UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
	      UIPopupAction uiPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
	      uiPopup.deActivate() ;
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
			}
		}
		
	}
