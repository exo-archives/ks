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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionResponse;
import javax.xml.namespace.QName;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		template =	"app:/templates/forum/webui/UIBreadcumbs.gtmpl" ,
		events = {
				@EventConfig(listeners = UIBreadcumbs.ChangePathActionListener.class),
				@EventConfig(listeners = UIBreadcumbs.RssActionListener.class)
		}
)
public class UIBreadcumbs extends UIContainer {
	@SuppressWarnings("unused")
  private boolean useAjax = true;
	private ForumService forumService ;
	private List<String> breadcumbs_ = new ArrayList<String>();
	private List<String> path_ = new ArrayList<String>();
	private String QUICK_SEARCH = "QuickSearchForm" ;
	public static final String FORUM_SERVICE = Utils.FORUM_SERVICE ;
	private boolean isLink = false ;
	private boolean isOpen = true;
	private String tooltipLink = "forumHome";
	private UserProfile userProfile ;
	public UIBreadcumbs()throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
		path_.add(FORUM_SERVICE) ;
		addChild(UIQuickSearchForm.class, null, QUICK_SEARCH) ;
	}

	@SuppressWarnings("unused")
  private void setIsUseAjax() throws Exception{
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class); 
		userProfile = forumPortlet.getUserProfile();
		useAjax = forumPortlet.isUseAjax();
	}

	public void setUpdataPath(String path) throws Exception {
		isLink = false ;
		setRenderForumLink(path);
		if(!ForumUtils.isEmpty(path) && !path.equals(FORUM_SERVICE)) {
			String temp[] = path.split("/") ;
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add(FORUM_SERVICE) ;
			tooltipLink = "forumHome";
			breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			if(path.equals(ForumUtils.FIELD_EXOFORUM_LABEL)) {
				breadcumbs_.add(ForumUtils.FIELD_SEARCHFORUM_LABEL) ;
				path_.add("/"+ForumUtils.FIELD_EXOFORUM_LABEL) ;
			} else {
				String tempPath = ""; int i = 0;
				SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
				try {
					for (String string : temp) {
						
						if (!ForumUtils.isEmpty(tempPath))
							tempPath = tempPath + "/" + string;
						else
							tempPath = string;
						Object obj = forumService.getObjectNameByPath(tempPath);
						if (obj == null) {
							if (i == 0) {
								isLink = true;
							}
							break;
						}
						if (obj instanceof Category) {
							Category category = (Category) obj;
							tempPath = string;
							breadcumbs_.add(category.getCategoryName());
							tooltipLink = "category";
						} else if (obj instanceof Forum) {
							Forum forum = (Forum) obj;
							breadcumbs_.add(forum.getForumName());
							tooltipLink = "forum";
						} else if (obj instanceof Topic) {
							Topic topic = (Topic) obj;
							breadcumbs_.add(topic.getTopicName());
							tooltipLink = "topic";
						} else if (obj instanceof Tag) {
							Tag tag = (Tag) obj;
							breadcumbs_.add(tag.getName());
							tooltipLink = "tag";
						}
						path_.add(tempPath);
						++i;
					}
				} catch (Exception e) {
				} finally {
					sProvider.close();
				}
			}
		} else {
			path_.clear() ;
			breadcumbs_.clear() ;
			path_.add(FORUM_SERVICE) ;
			breadcumbs_.add(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			tooltipLink = "forumHome";
		}
	}
	
	private void setRenderForumLink(String path) throws Exception {
		try {
			PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
			ActionResponse actionRes = (ActionResponse) pcontext.getResponse();
			ForumParameter param = new ForumParameter();
			if (userProfile.getIsShowForumJump() && !path.equals(FORUM_SERVICE)) {
				if(path.indexOf(Utils.TOPIC) > 0) {
					path = path.substring(0, path.lastIndexOf("/"));
				}
				param.setRenderForumLink(true);
				param.setPath(path);
			} else {
				param.setRenderForumLink(false);
			}
			actionRes.setEvent(new QName("ForumLinkEvent"), param) ;
    } catch (Exception e) {
    }
	}
	
	public boolean isOpen() {
		return isOpen;
	}

	public void setOpen(boolean isOpen) {
		this.isOpen = isOpen;
	}
	
	@SuppressWarnings("unused")
  private String getToolTip() {
		return tooltipLink ;
	}
	@SuppressWarnings("unused")
	private boolean isLink() {return this.isLink;}
	@SuppressWarnings("unused")
	private String getPath(int index) {
		return this.path_.get(index) ;
	}
	
	@SuppressWarnings("unused")
	private int getMaxPath() {
		return breadcumbs_.size() ;
	}
	
	@SuppressWarnings("unused")
	private List<String> getBreadcumbs() throws Exception {
		return breadcumbs_ ;
	}
	
	static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs uiBreadcums = event.getSource() ;
			if(uiBreadcums.isOpen()) {
				String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
				UIForumPortlet forumPortlet = uiBreadcums.getAncestorOfType(UIForumPortlet.class) ;
				UIApplication uiApp = uiBreadcums.getAncestorOfType(UIApplication.class) ;
				if(path.indexOf(ForumUtils.FIELD_EXOFORUM_LABEL) >= 0) {
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.updateIsRender(true) ;
					categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				}else if(path.equals(FORUM_SERVICE)){
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.updateIsRender(true) ;
					categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				}else	if(path.lastIndexOf(Utils.TOPIC) >= 0) {
					String []id = path.split("/") ;
					SessionProvider sProvider = SessionProviderFactory.createSystemProvider() ;
					try{
						Topic topic ;
						if(id.length > 1) {
							topic = uiBreadcums.forumService.getTopicByPath(sProvider, path, false) ;
						} else {
							topic = (Topic)uiBreadcums.forumService.getObjectNameById(sProvider, path, Utils.TOPIC);
							path = topic.getPath();
							path = path.substring(path.indexOf(Utils.CATEGORY));
							id = path.split("/") ;
						}
						if(topic != null) {
							forumPortlet.updateIsRendered(ForumUtils.FORUM);
							Forum forum = uiBreadcums.forumService.getForum(sProvider, id[0], id[1]) ;
							UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
							UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
							uiForumContainer.setIsRenderChild(false) ;
							uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
							UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
							uiTopicDetail.setUpdateForum(forum) ;
							uiTopicDetail.setTopicFromCate(id[0], id[1] , topic) ;
							uiTopicDetail.setIdPostView("top") ;
							uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1] , topic.getId()) ;
							forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0] + "/" + id[1] + " "));
							if(!forumPortlet.getUserProfile().getUserId().equals(UserProfile.USER_GUEST)) {
								uiBreadcums.forumService.updateTopicAccess(forumPortlet.getUserProfile().getUserId(),  topic.getId()) ;
								forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
							}
						}						
					}catch(Exception e) {
						uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
						categoryContainer.updateIsRender(true) ;
						categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
						path = FORUM_SERVICE;
					}finally {
						sProvider.close() ;
					}
				}else	if((path.lastIndexOf(Utils.FORUM) == 0 && path.lastIndexOf(Utils.CATEGORY) < 0) || (path.lastIndexOf(Utils.FORUM) > 0)) {
					String id[] = path.split("/");
					forumPortlet.updateIsRendered(ForumUtils.FORUM);
					UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
					forumContainer.setIsRenderChild(true) ;
					if(id.length > 1) {
						forumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
						forumContainer.getChild(UITopicContainer.class).updateByBreadcumbs(id[0], id[1], true) ;
					} else {
						SessionProvider sProvider = SessionProviderFactory.createSystemProvider() ;
						try {
							Forum forum = (Forum)uiBreadcums.forumService.getObjectNameById(sProvider, path, Utils.FORUM);
							path = forum.getPath();
							path = path.substring(path.indexOf(Utils.CATEGORY));
							id = path.split("/");
							forumContainer.getChild(UIForumDescription.class).setForum(forum) ;
							forumContainer.getChild(UITopicContainer.class).setUpdateForum(id[0], forum) ;
						}catch(Exception e) {
							uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
							forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
							UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
							categoryContainer.updateIsRender(true) ;
							categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
							path = FORUM_SERVICE;
						}finally {
							sProvider.close() ;
						}
					}
				}else {
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.getChild(UICategory.class).updateByBreadcumbs(path) ;
					categoryContainer.updateIsRender(false) ;
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				}
				uiBreadcums.setUpdataPath(path);
				forumPortlet.getChild(UIForumLinks.class).setValueOption(path);
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				uiBreadcums.isOpen = true;
			}
		}
	}	
	
	static public class RssActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
			categoryContainer.updateIsRender(true) ;
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
			event.getSource().setUpdataPath(FORUM_SERVICE);
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
}