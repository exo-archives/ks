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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Dec 12, 2007 11:34:56 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UITagForm.gtmpl",
    events = {
      @EventConfig(listeners = UITagForm.AddTagActionListener.class), 
      @EventConfig(listeners = UITagForm.EditTagActionListener.class),
      @EventConfig(listeners = UITagForm.DeleteActionListener.class),
      @EventConfig(listeners = UITagForm.SelectedActionListener.class),
      @EventConfig(listeners = UITagForm.SaveActionListener.class),
      @EventConfig(listeners = UITagForm.CancelActionListener.class,phase = Phase.DECODE)
    }
)
public class UITagForm extends UIForm implements UIPopupComponent {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	@SuppressWarnings("unused")
  private String IdSelected = "";
	private String topicPath = "";
	private String tagId[] = new String[] {} ;
	private boolean isUpdateList = true ;
	List<Tag> tags_ = new ArrayList<Tag>() ;
	public UITagForm() throws Exception {
  }

	public void activate() throws Exception {
	  // TODO Auto-generated method stub
  }

	public void deActivate() throws Exception {
	  // TODO Auto-generated method stub
  }
	
	public void setTopicPathAndTagId(String topicPath, String[] tagId) {
	  this.topicPath = topicPath ;
	  this.tagId = tagId ;
	  this.isUpdateList = true ;
  }
	
	@SuppressWarnings("unused")
  private boolean getSelected(String tagId) throws Exception {
		if(this.IdSelected.equals(tagId)) return true ;
		return false ;
	}
	
	@SuppressWarnings("unused")
  private List<Tag> getAllTag() throws Exception {
		if(this.isUpdateList) {
			List<Tag> tags = forumService.getTags(ForumSessionUtils.getSystemProvider());
			this.tags_.clear() ;
			boolean isAdd = true ;
			for (Tag tag : tags) {
		    String tagId = tag.getId() ;
		    for(String str : this.tagId) {
		    	if(tagId.equals(str)) {
		    		isAdd = false ;
		    		break ;
		    	}
		    }
		    if(isAdd) this.tags_.add(tag) ;
		    isAdd = true ;
	    }
			if(this.tags_.size() > 0) this.IdSelected = tags_.get(0).getId() ;
			else this.IdSelected = "" ;
			this.isUpdateList = false ;
		}
		return this.tags_ ;
	}
	
	public void setUpdateList( boolean isUpdateList) {
	  this.isUpdateList = isUpdateList ;
  }

	private Tag getTagEdit() {
		List<Tag> tags = this.tags_ ;
		for (Tag tag : tags) {
	    if(tag.getId().equals(this.IdSelected)) return tag ;
    }
		return null ;
  }
	
	static  public class AddTagActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      uiChildPopup.activate(UIAddTagForm.class, 410) ;
      uiForm.isUpdateList = true ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static  public class EditTagActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIAddTagForm addTagForm = uiChildPopup.createUIComponent(UIAddTagForm.class, null, null) ;
      addTagForm.setUpdateTag(uiForm.getTagEdit());
      addTagForm.setIsTopicTag(false) ;
			uiChildPopup.activate(addTagForm, 410, 263) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static  public class DeleteActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			uiForm.forumService.removeTag(ForumSessionUtils.getSystemProvider(), uiForm.IdSelected);
			uiForm.isUpdateList = true ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
		}
	}

	static  public class SelectedActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			uiForm.IdSelected = event.getRequestContext().getRequestParameter(OBJECTID);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
		}
	}

	static  public class SaveActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			if(uiForm.IdSelected != null && uiForm.IdSelected.length() > 0 && uiForm.topicPath != null && uiForm.topicPath.length() > 0) {
				uiForm.forumService.addTopicInTag(ForumSessionUtils.getSystemProvider(), uiForm.IdSelected, uiForm.topicPath);
			}
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.findFirstComponentOfType(UITopicDetail.class).setIsEditTopic(true) ;
      forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UITagForm> {
    public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}