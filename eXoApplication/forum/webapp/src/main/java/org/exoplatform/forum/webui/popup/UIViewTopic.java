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

import java.io.InputStream;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
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
 * May 25, 2008 - 2:55:24 AM  
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIViewTopic.gtmpl",
		events = {
			@EventConfig(listeners = UIViewTopic.CloseActionListener.class, phase = Phase.DECODE)
		}
)
public class UIViewTopic extends UIForm implements UIPopupComponent {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private Topic topic ;
	private JCRPageList pageList ;
	private UserProfile userProfile ;
	private long pageSelect ;
	public UIViewTopic() throws Exception {
		addChild(UIForumPageIterator.class, null, "ViewTopicPageIterator") ;
	}
	public void activate() throws Exception {	}
	public void deActivate() throws Exception {}
	
	public Topic getTopic() { return topic;}
	public void setTopic(Topic topic) {this.topic = topic;}
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() throws Exception {
		return this.userProfile ;
	}
	@SuppressWarnings("unused")
  private void initPage() throws Exception {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
		String userLogin = this.userProfile.getUserId();
		Topic topic = this.topic ;
		String id[] = topic.getPath().split("/");
		int l = id.length ;
		pageList = forumService.getPosts(ForumSessionUtils.getSystemProvider(), id[l-3], id[l-2], topic.getId(), "", "", "", userLogin)	; 
		long maxPost = this.userProfile.getMaxPostInPage() ;
		if(maxPost < 0) maxPost = 1 ;
		pageList.setPageSize(maxPost) ;
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		forumPageIterator.updatePageList(pageList) ;
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
  private List<Post> getPostPageList() throws Exception {
		if(this.pageList == null) return null ;
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		this.pageSelect = forumPageIterator.getPageSelected() ;
		long availablePage = this.pageList.getAvailablePage() ;
		if(this.pageSelect > availablePage) {
			this.pageSelect = availablePage ;
			forumPageIterator.setSelectPage(availablePage);
		}
		if(this.pageSelect < 1) return null ;
		return this.pageList.getPage(this.pageSelect) ;
	}
	
	@SuppressWarnings("unused")
  private boolean getIsRenderIter() {
		long availablePage = this.pageList.getAvailablePage() ;
		if(availablePage > 1) return true;
		return false ;
	}
	
	@SuppressWarnings("unused")
  private UserProfile getUserInfo(String userName) throws Exception {
		return this.forumService.getUserInfo(ForumSessionUtils.getSystemProvider(), userName);
	}
	@SuppressWarnings("unused")
	private Contact getPersonalContact(String userId) throws Exception {
		Contact contact = ForumSessionUtils.getPersonalContact(userId) ;
		if(contact == null) {
			contact = new Contact() ;
			contact.setId(userId) ;
		}
		return contact ;
	}
	
	@SuppressWarnings("unused")
  private String getAvatarUrl(Contact contact) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		try {
			ContactAttachment attachment = contact.getAttachment() ; 
    	InputStream input = attachment.getInputStream() ;
    	String fileName = attachment.getFileName() ;
    	return ForumSessionUtils.getFileSource(input, fileName, dservice);
    } catch (NullPointerException e) {
	    return "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
    }
	}
	@SuppressWarnings("unused")
  private boolean isOnline(String userId) throws Exception {
		return this.forumService.isOnline(userId) ;
	}
	static	public class CloseActionListener extends EventListener<UIViewTopic> {
    public void execute(Event<UIViewTopic> event) throws Exception {
    	UIViewTopic uiForm = event.getSource() ;
    	UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			if(popupContainer != null) {
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
				popupAction.deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
			}
		}
	}



}
