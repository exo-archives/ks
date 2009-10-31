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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.portlet.ActionResponse;
import javax.xml.namespace.QName;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIPostForm;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.portal.account.UIAccountSetting;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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
				@EventConfig(listeners = UIBreadcumbs.AccountSettingsActionListener.class),
				@EventConfig(listeners = UIBreadcumbs.RssActionListener.class)
		}
)
@SuppressWarnings("unused")
public class UIBreadcumbs extends UIContainer {
  
  private static Log log = ExoLogger.getExoLogger(UIBreadcumbs.class);
  
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
				} catch (Exception e) {}
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
	
  private String getToolTip() {
		return tooltipLink ;
	}
	private boolean isLink() {return this.isLink;}
	
	private String getPath(int index) {
		return this.path_.get(index) ;
	}
	
	private int getMaxPath() {
		return breadcumbs_.size() ;
	}
	
	private List<String> getBreadcumbs() throws Exception {
		return breadcumbs_ ;
	}
	
	private boolean isArrayNotNull(String []strs){
		if(strs != null && strs.length > 0 && !strs[0].equals(" ")) return true;//private
		else  return false;
	}
	
  private boolean checkLinkPrivate(String id) throws Exception {
		boolean isPrivate = false;
		if(id.indexOf(Utils.TOPIC) >= 0) {
			try {
				Topic topic = (Topic)this.forumService.getObjectNameById(id, Utils.TOPIC);
				if(topic != null) {
					if(topic.getIsClosed() || !topic.getIsActiveByForum() || !topic.getIsActive() || topic.getIsWaiting() || (isArrayNotNull(topic.getCanView()))){
						isPrivate = true;
					}
					if(!isPrivate) {
						String path = topic.getPath();
						id = path.substring(path.lastIndexOf(Utils.FORUM), path.indexOf(Utils.TOPIC)-1);
						Forum forum = (Forum)this.forumService.getObjectNameById(id, Utils.FORUM);
						if(forum.getIsClosed()) isPrivate = true;
						if(!isPrivate) {
							id = path.substring(path.indexOf(Utils.CATEGORY), path.lastIndexOf(Utils.FORUM)-1);
							Category cate = (Category)this.forumService.getObjectNameById(id, Utils.CATEGORY);
							if(isArrayNotNull(cate.getUserPrivate())){
								isPrivate = true;
							}
						}
					}
				}
      } catch (Exception e) {
      	e.printStackTrace();
      }
		}else	if(id.indexOf(Utils.CATEGORY) == 0) {
			try {
				Category cate = (Category)this.forumService.getObjectNameById(id, Utils.CATEGORY);
				if(isArrayNotNull(cate.getUserPrivate())){
					isPrivate = true;
				}
      } catch (Exception e) {
      	e.printStackTrace();
      }
		}else if(id.indexOf(Utils.FORUM) == 0){
      try {
				Forum forum = (Forum)this.forumService.getObjectNameById(id, Utils.FORUM);
				if(forum.getIsClosed()) isPrivate = true;
				if(!isPrivate) {
					String path = forum.getPath();
					path = path.substring(path.indexOf(Utils.CATEGORY), path.lastIndexOf(Utils.FORUM)-1);
					Category cate = (Category)this.forumService.getObjectNameById(path, Utils.CATEGORY);
					if(isArrayNotNull(cate.getUserPrivate())){
						isPrivate = true;
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return isPrivate;
	}
	
	private boolean isInArray(String[] arr, String str) {
		if(Arrays.asList(arr).contains(str)){
    	return true;
    }
		return false;
	}
	
	private boolean checkCanView(Category cate, Forum forum, Topic topic) throws Exception {
		String[] viewer = cate.getUserPrivate();
		if(userProfile == null) setIsUseAjax();
		String userId = userProfile.getUserId();
		if(userProfile.getUserRole() == 0) return true;
		if(isArrayNotNull(viewer)) {
			if(!isInArray(viewer, userId)) {
				return false;
			}
		}
		if(forum != null){
			if(isArrayNotNull(forum.getModerators()) && isInArray(forum.getModerators(), userId)) {
				return true;
			} else if(forum.getIsClosed()) return false;
		}
		if(topic != null) {
			if(topic.getIsClosed() || !topic.getIsActive() || !topic.getIsActiveByForum() || !topic.getIsApproved() || 
				 topic.getIsWaiting() || (isArrayNotNull(topic.getCanView()) && !isInArray(topic.getCanView(), userId))) return false;
		}
	  return true;
  }
	
	static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
		public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs breadcums = event.getSource() ;
			if(breadcums.isOpen()) {
				String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
				UIForumPortlet forumPortlet = breadcums.getAncestorOfType(UIForumPortlet.class) ;
				UIApplication uiApp = breadcums.getAncestorOfType(UIApplication.class) ;
				if(path.indexOf(ForumUtils.FIELD_EXOFORUM_LABEL) >= 0 || path.equals(FORUM_SERVICE)) {
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.updateIsRender(true) ;
					categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				}else	if(path.lastIndexOf(Utils.TOPIC) >= 0) {
					boolean isReply = false, isQuote = false;
					if(path.indexOf("/true") > 0){
						isQuote = true;
						path = path.replaceFirst("/true", "");
					} else	if(path.indexOf("/false") > 0){
						isReply = true;
						path = path.replaceFirst("/false", "");
					}
					String []id = path.split("/") ;
					String postId = "top";
					int page = 0;
					if(path.indexOf(Utils.POST) > 0) {
						postId = id[id.length-1];
						path = path.substring(0, path.lastIndexOf("/")) ;
						id = new String[]{path};
					} else if(id.length > 1) {
						try {
							page = Integer.parseInt(id[id.length-1]);
            } catch (Exception e) {}
						if(page > 0){
							path = path.replace("/"+id[id.length-1], "");
							id = new String[]{path};
						} else page = 0;
					}
					try{
						Topic topic ;
						if(id.length > 1) {
							topic = breadcums.forumService.getTopicByPath(path, false) ;
						} else {
							topic = (Topic)breadcums.forumService.getObjectNameById(path, Utils.TOPIC);
							path = topic.getPath();
							path = path.substring(path.indexOf(Utils.CATEGORY));
							id = path.split("/") ;
						}
						if(topic != null) {
							Category category = breadcums.forumService.getCategory(id[0]);
							Forum forum = breadcums.forumService.getForum(id[0], id[1]) ;
							if(breadcums.checkCanView(category, forum, topic)){
								forumPortlet.updateIsRendered(ForumUtils.FORUM);
								UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
								UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
								uiForumContainer.setIsRenderChild(false) ;
								uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
								UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
								uiTopicDetail.setUpdateForum(forum) ;
								uiTopicDetail.setTopicFromCate(id[0], id[1] , topic, page) ;
								uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1] , topic.getId()) ;
								forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0] + "/" + id[1] + " "));
								uiTopicDetail.setIdPostView(postId) ;
								if(isReply || isQuote){
									if(uiTopicDetail.getCanPost()) {
										uiTopicDetail.setIdPostView("top") ;
										try {
											UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
											UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
											UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
											boolean isMod = ForumServiceUtils.hasPermission(forum.getModerators(), breadcums.userProfile.getUserId());
											postForm.setPostIds(id[0], id[1], topic.getId(), topic) ;
											postForm.setMod(isMod);
											if(isQuote){
												uiTopicDetail.setLastPostId(postId) ;
												Post post = breadcums.forumService.getPost(id[0], id[1], topic.getId(), postId);
												if(post != null) {
													postForm.updatePost(postId, true, false, post) ;
													popupContainer.setId("UIQuoteContainer") ;
												} else {
													uiApp.addMessage(new ApplicationMessage("UIBreadcumbs.msg.post-no-longer-exist", null, ApplicationMessage.WARNING)) ;
													event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
													uiTopicDetail.setIdPostView("normal") ;
												}
											} else {
												postForm.updatePost("", false, false, null) ;
												popupContainer.setId("UIAddPostContainer") ;
											}
											popupAction.activate(popupContainer, 900, 500) ;
											event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
										} catch (Exception e) {
										  log.error(e);
										}
									} else {
										uiApp.addMessage(new ApplicationMessage("UIPostForm.msg.no-permission", new String[]{}, ApplicationMessage.WARNING)) ;
									}
								}
								if (!UserHelper.isAnonim()) {
								//if(!forumPortlet.getUserProfile().getUserId().equals(UserProfile.USER_GUEST)) {
									breadcums.forumService.updateTopicAccess(UserHelper.getCurrentUser(),  topic.getId()) ;
									forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
								}
							} else {
								uiApp.addMessage(new ApplicationMessage("UIBreadcumbs.msg.do-not-permission", new String[]{ForumUtils.THREAD}, ApplicationMessage.WARNING)) ;
								forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
								UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
								categoryContainer.updateIsRender(true) ;
								categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
								path = FORUM_SERVICE;
							}
						}				
					}catch(Exception e) {
						uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
						categoryContainer.updateIsRender(true) ;
						categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
						path = FORUM_SERVICE;
					}
				}else	if((path.lastIndexOf(Utils.FORUM) == 0 && path.lastIndexOf(Utils.CATEGORY) < 0) || (path.lastIndexOf(Utils.FORUM) > 0)) {
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					try {
						Forum forum;
						String cateId = null;
						int page = 0;
						if(path.indexOf("/") > 0){
							String []arr = path.split("/");
							try {
								page = Integer.parseInt(arr[arr.length-1]);
							} catch (Exception e) {}
							if(arr[0].indexOf(Utils.CATEGORY) == 0){
								cateId = arr[0];
								forum = breadcums.forumService.getForum(cateId, arr[1]);
							} else {
								forum = (Forum)breadcums.forumService.getObjectNameById(arr[0], Utils.FORUM);
							}
						} else {
							forum = (Forum)breadcums.forumService.getObjectNameById(path, Utils.FORUM);
						}
						path = forum.getPath();
						if(cateId == null){
							cateId = path.substring(path.indexOf(Utils.CATEGORY), path.lastIndexOf(Utils.FORUM)-1);
						}
						path = path.substring(path.indexOf(Utils.CATEGORY));
						Category category = breadcums.forumService.getCategory(cateId);
						if(breadcums.checkCanView(category, forum, null)){
							forumPortlet.updateIsRendered(ForumUtils.FORUM);
							UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
							forumContainer.setIsRenderChild(true) ;
							forumContainer.getChild(UIForumDescription.class).setForum(forum) ;
							forumContainer.getChild(UITopicContainer.class).setUpdateForum(cateId, forum, page) ;
						} else {
							uiApp.addMessage(new ApplicationMessage("UIBreadcumbs.msg.do-not-permission", null, ApplicationMessage.WARNING)) ;
							forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
							categoryContainer.updateIsRender(true) ;
							categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
							path = FORUM_SERVICE;
						}
					}catch(Exception e) {
						e.printStackTrace();
						uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", new String[]{ForumUtils.FORUM}, ApplicationMessage.WARNING)) ;
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						categoryContainer.updateIsRender(true) ;
						categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
						path = FORUM_SERVICE;
					}
				}else	if(path.indexOf(Utils.CATEGORY) >= 0 && path.indexOf("/") < 0) {
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					try {
						Category category = breadcums.forumService.getCategory(path);
						if(breadcums.checkCanView(category, null, null)){
							categoryContainer.getChild(UICategory.class).updateByLink(category) ;
							categoryContainer.updateIsRender(false) ;
							forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						} else {
							uiApp.addMessage(new ApplicationMessage("UIBreadcumbs.msg.do-not-permission", new String[]{ForumUtils.CATEGORIES}, ApplicationMessage.WARNING)) ;
							forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
							categoryContainer.updateIsRender(true) ;
							categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
							path = FORUM_SERVICE;
						}
					} catch (Exception e) {
						e.printStackTrace();
						uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						categoryContainer.updateIsRender(true) ;
						categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
						path = FORUM_SERVICE;
					}
				}else {
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.updateIsRender(true) ;
					categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
					path = FORUM_SERVICE;
				}
				breadcums.setUpdataPath(path);
				forumPortlet.getChild(UIForumLinks.class).setValueOption(path);
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				breadcums.isOpen = true;
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
	
	static public class AccountSettingsActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIPortal uiPortal = Util.getUIPortal() ;
      UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class) ;
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID) ;
     
      UIAccountSetting uiAccountForm = uiMaskWS.createUIComponent(UIAccountSetting.class, null, null) ;
      uiMaskWS.setUIComponent(uiAccountForm) ;
      uiMaskWS.setShow(true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS) ;
    }
  }
}