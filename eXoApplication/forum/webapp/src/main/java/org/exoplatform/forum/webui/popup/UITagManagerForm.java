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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicsTag;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 17-09-2008 - 07:48:18  
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UITagManagerForm.gtmpl",
		events = {
//			@EventConfig(listeners = UITagManagerForm.OpenTagActionListener.class), 
//			@EventConfig(listeners = UITagManagerForm.EditTagActionListener.class), 
//			@EventConfig(listeners = UITagManagerForm.DeleteTagActionListener.class), 
//			@EventConfig(listeners = UITagManagerForm.CancelActionListener.class,phase = Phase.DECODE) 
		}
)
public class UITagManagerForm extends UIForm implements UIPopupComponent {
	private ForumService forumService ;
	private List<Tag> tags = new ArrayList<Tag>();
	private boolean isGetTag = true;
	public UITagManagerForm() {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  }
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	public void setUpdateTag(boolean isGetTag) {
		this.isGetTag = isGetTag;
  }
	/*
	@SuppressWarnings("unused")
  private List<Tag> getListTags() throws Exception {
		if(isGetTag) {
			tags = forumService.getTags(ForumSessionUtils.getSystemProvider());
			isGetTag =false;
		}
		return tags;
	}
	
	private Tag getTagById(String id) throws Exception{
		for (Tag tag : this.tags) {
			if(tag.getId().equals(id)) return tag;
		}
		return new Tag();
	}
	
	static	public class OpenTagActionListener extends EventListener<UITagManagerForm> {
		public void execute(Event<UITagManagerForm> event) throws Exception {
			UITagManagerForm uiForm = event.getSource() ;
			String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.TAG) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption(Utils.FORUM_SERVICE) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(tagId) ;
			forumPortlet.getChild(UITopicsTag.class).setTag(uiForm.getTagById(tagId)) ;
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static	public class EditTagActionListener extends EventListener<UITagManagerForm> {
		public void execute(Event<UITagManagerForm> event) throws Exception {
			UITagManagerForm uiForm = event.getSource() ;
			String tagId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIAddTagForm addTagForm = uiChildPopup.createUIComponent(UIAddTagForm.class, null, null) ;
			addTagForm.setUpdateTag(uiForm.getTagById(tagId));
			addTagForm.setIsTopicTag(false) ;
			uiChildPopup.activate(addTagForm, 410, 263) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static	public class DeleteTagActionListener extends EventListener<UITagManagerForm> {
		public void execute(Event<UITagManagerForm> event) throws Exception {
			UITagManagerForm uiForm = event.getSource() ;
			String tagId = event.getRequestContext().getRequestParameter(OBJECTID);
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				uiForm.forumService.removeTag(sProvider, tagId);
				uiForm.setUpdateTag(true) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
      } catch (Exception e) {
	      e.printStackTrace();
      } finally {
      	sProvider.close();
      }
		}
	}
	
	static	public class CancelActionListener extends EventListener<UITagManagerForm> {
		public void execute(Event<UITagManagerForm> event) throws Exception {
			UITagManagerForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}*/
}
