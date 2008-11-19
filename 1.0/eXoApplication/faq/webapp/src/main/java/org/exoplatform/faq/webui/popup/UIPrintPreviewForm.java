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
		template =	"app:/templates/faq/webui/popup/UIPrintPreviewForm.gtmpl",
		events = {
				@EventConfig(listeners = UIPrintPreviewForm.SaveActionListener.class),
				@EventConfig(listeners = UIPrintPreviewForm.CancelActionListener.class)
		}
)
public class UIPrintPreviewForm extends UIForm	{
	 
	public UIPrintPreviewForm() throws Exception {
	}
	
	static public class SaveActionListener extends EventListener<UIPrintPreviewForm> {
    public void execute(Event<UIPrintPreviewForm> event) throws Exception {
			UIPrintPreviewForm uiCategory = event.getSource() ;			
			System.out.println("========> Save") ;
		}
	}

	static public class CancelActionListener extends EventListener<UIPrintPreviewForm> {
    public void execute(Event<UIPrintPreviewForm> event) throws Exception {
			UIPrintPreviewForm uiCategory = event.getSource() ;			
			System.out.println("==========> Cancel") ;
		}
	}
	
	
	
}