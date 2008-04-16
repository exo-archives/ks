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

import java.io.InputStream;

import javax.jcr.PathNotFoundException;

import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPortlet;
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
 *					tu.duy@exoplatform.com
 * October 2, 2007	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIViewTopic.gtmpl",
		events = {
			@EventConfig(listeners = UIViewTopic.CloseActionListener.class, phase = Phase.DECODE)
		}
)
public class UIViewTopic extends UIForm implements UIPopupComponent {
	private Post post;
  private boolean isViewUserInfo = false ;
	
	public UIViewTopic() {
	}
	
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() {
		return this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
  
  @SuppressWarnings("unused")
  private String getFileSource(ForumAttachment attachment) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    try {
    	InputStream input = attachment.getInputStream() ;
    	String fileName = attachment.getName() ;
    	return ForumSessionUtils.getFileSource(input, fileName, dservice);
    } catch (PathNotFoundException e) {
	    return null;
    }
  }
	
	public void setPostView(Post post) throws Exception {
		this.post = post ;
	}
	
	@SuppressWarnings("unused")
	private Post getPostView() throws Exception {
		return post ;
	}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
  
  public void setViewUserInfo(boolean isView){
    this.isViewUserInfo = isView ;
  }
  
  @SuppressWarnings("unused")
  private boolean getIsViewUserInfo(){
    return this.isViewUserInfo ;
  }
	
	static	public class CloseActionListener extends EventListener<UIViewTopic> {
    public void execute(Event<UIViewTopic> event) throws Exception {
			UIViewTopic uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			popupContainer.getChild(UIPopupAction.class).deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
}
