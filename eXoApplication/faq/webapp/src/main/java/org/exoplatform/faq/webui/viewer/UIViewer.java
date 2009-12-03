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
package org.exoplatform.faq.webui.viewer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.rendering.RenderHelper;
import org.exoplatform.faq.rendering.RenderingException;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 24, 2009 - 4:32:48 AM  
 */

@ComponentConfig(
//		template =	"app:/templates/faq/webui/UIViewer.gtmpl",
		events = {
		  	 @EventConfig(listeners = UIViewer.ChangePathActionListener.class)
		}
)
@SuppressWarnings("unused")
public class UIViewer extends UIContainer {
	private FAQService fAqService;
	private String path = Utils.CATEGORY_HOME;
	private boolean useAjax = false;
	private RenderHelper renderHelper = new RenderHelper();
	public UIViewer() {
		 fAqService = (FAQService)PortalContainer.getComponent(FAQService.class) ;
  }
	
  private List<String> arrangeList(List<String> list) {
		List<String> newList = new ArrayList<String>();
		if(list.isEmpty() || list.size() == 0){
			newList.add("<img src=\"/faq/skin/DefaultSkin/webui/background/HomeIcon.gif\" alt=\""+Utils.CATEGORY_HOME+"\"/>");
		} else {
			for (int i = (list.size()-1); i >= 0; i--) {
				if(i == (list.size()-1)) {
					newList.add("<img src=\"/faq/skin/DefaultSkin/webui/background/HomeIcon.gif\" alt=\""+list.get(i)+"\"/>");
				} else {
					newList.add(list.get(i));
				}
			}
		}
		return newList;
	} 
	
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
  	return new FAQResourceResolver() ;
  }
  
  public String getTemplate() {
  	return "FAQViewerTemplate" ;
  }
  
  private CategoryInfo getCategoryInfo() throws Exception {
		CategoryInfo categoryInfo = new CategoryInfo();
		List<String> list = new ArrayList<String>();
		list = FAQUtils.getCategoriesIdFAQPortlet();
		useAjax = FAQUtils.getUseAjaxFAQPortlet();
		try {
			categoryInfo = this.fAqService.getCategoryInfo(this.path, list);
    } catch (Exception e) {
    	e.printStackTrace();
    }
		return categoryInfo;
	}

	private String render(String s) {
		Question question = new Question();
		question.setDetail(s);
		return renderHelper.renderQuestion(question) ;
	}
	
	static public class ChangePathActionListener extends EventListener<UIViewer> {
		public void execute(Event<UIViewer> event) throws Exception {
			String path = event.getRequestContext().getRequestParameter(OBJECTID);
			UIViewer viewer = event.getSource();
			viewer.path = path;
			event.getRequestContext().addUIComponentToUpdateByAjax(viewer);
		}
	}
}
