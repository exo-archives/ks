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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIAddWatchingForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIMergeTopicForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIMoveTopicForm;
import org.exoplatform.forum.webui.popup.UIPageListTopicUnApprove;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UITopicForm;
import org.exoplatform.forum.webui.popup.UIWatchToolsForm;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
	lifecycle = UIFormLifecycle.class,
	template =	"app:/templates/forum/webui/UITopicContainer.gtmpl", 
	events = {
		@EventConfig(listeners = UITopicContainer.SearchFormActionListener.class ),	
		@EventConfig(listeners = UITopicContainer.GoNumberPageActionListener.class ),	
		@EventConfig(listeners = UITopicContainer.AddTopicActionListener.class ),	
		@EventConfig(listeners = UITopicContainer.OpenTopicActionListener.class ),
		@EventConfig(listeners = UITopicContainer.OpenTopicsTagActionListener.class ),
		@EventConfig(listeners = UITopicContainer.ApproveTopicsActionListener.class ),//Menu Forum
		@EventConfig(listeners = UITopicContainer.EditForumActionListener.class ),	
		@EventConfig(listeners = UITopicContainer.SetLockedForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetUnLockForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetOpenForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetCloseForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.MoveForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.RemoveForumActionListener.class,confirm="UITopicContainer.confirm.RemoveForum"),//Menu Topic
		@EventConfig(listeners = UITopicContainer.WatchOptionActionListener.class),
		
		@EventConfig(listeners = UITopicContainer.EditTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetOpenTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetCloseTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetLockedTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetUnLockTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetStickTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetUnStickTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetMoveTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.MergeTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetDeleteTopicActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetUnWaitingActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetOrderByActionListener.class),
		@EventConfig(listeners = UITopicContainer.AddWatchingActionListener.class),
		@EventConfig(listeners = UITopicContainer.AddBookMarkActionListener.class),
		@EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
	}
)
public class UITopicContainer extends UIForumKeepStickPageIterator {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String forumId = "";
	private String categoryId = "";
	private Forum forum;
	private JCRPageList pageList ;
	private List <Topic> topicList ;
	private boolean isUpdate = false;
	private boolean isModerator = false ;
	private long maxTopic = 10 ;
	private long maxPost = 10 ;
	private boolean canAddNewThread = true ;
	private UserProfile userProfile = null;
	private String strOrderBy = "" ;
	private boolean isLogin = false;

	public boolean isLogin() {return isLogin;}
	public void setLogin(boolean isLogin) {this.isLogin = isLogin;}

	public UITopicContainer() throws Exception {
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_T, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_B, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.SEARCHFORM_ID, null)) ;
		if(ForumSessionUtils.getCurrentUser() != null) isLogin = true;
	}
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() { return userProfile ;}

	public void setUpdateForum(String categoryId, Forum forum) throws Exception {
		this.forum = forum ;
		this.forumId = forum.getId() ;
		this.categoryId = categoryId ;
		this.pageSelect = 1 ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
		this.userProfile = forumPortlet.getUserProfile() ;
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId)) ;
	}
	
	public void setIdUpdate(boolean isUpdate) { this.isUpdate = isUpdate;}
	
	public void updateByBreadcumbs(String categoryId, String forumId, boolean isBreadcumbs) throws Exception {
		this.forumId = forumId ;
		this.categoryId = categoryId ;
		this.isUpdate = true ;
		this.pageSelect = 1 ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
		this.userProfile = forumPortlet.getUserProfile() ;
		if(!isBreadcumbs) {
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId)) ;
		}
	}

	public boolean getCanAddNewThread(){return this.canAddNewThread ; }
	
	private Forum getForum() throws Exception {
		if(this.isUpdate) {
			this.forum = forumService.getForum(ForumSessionUtils.getSystemProvider(), categoryId, forumId);
			this.isUpdate = false ;
		}
		UIForumContainer forumContainer = this.getParent() ;
		if(this.forum != null)
			forumContainer.findFirstComponentOfType(UIForumInfos.class).setForum(this.forum);
		return this.forum ;
	}
	
	@SuppressWarnings("unused")
	private void initPage() throws Exception {
		this.canAddNewThread = true ;
		if(this.userProfile == null) this.userProfile = new UserProfile();
		StringBuffer strQuery = new StringBuffer() ;
		long role = this.userProfile.getUserRole() ;
		String userId = this.userProfile.getUserId() ;
		String[] strings = this.forum.getCreateTopicRole() ;
		if( strings != null && strings.length > 0){
			this.canAddNewThread = ForumServiceUtils.hasPermission(strings, userId) ;
		}
		isModerator = false ;
		if(role == 0 || ForumServiceUtils.hasPermission(forum.getModerators(), userId)) isModerator = true;
		else {
			strQuery.append("@exo:isClosed='false' and @exo:isWaiting='false'");
			boolean isView = ForumServiceUtils.hasPermission(forum.getPoster(), userId) ;
			if(!isView) isView = ForumServiceUtils.hasPermission(forum.getViewer(), userId) ;
			if(!isView) {
				strQuery.append(" and (@exo:owner='").append(userId).append("' or @exo:canView=' ' or @exo:canPost=' '") ;
				for (String string : ForumSessionUtils.getAllGroupAndMembershipOfUser(userId)) {
					strQuery.append(" or @exo:canView='"+string+"' or @exo:canPost='"+string+"'") ;
				}
				strQuery.append(")");
			}
		}
		if(!isModerator && this.forum.getIsModerateTopic()) {
			if(!ForumUtils.isEmpty(strQuery.toString())) strQuery.append(" and ") ;
			strQuery.append("@exo:isApproved='true'") ;
		}
		this.pageList = forumService.getPageTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, strQuery.toString(), strOrderBy);
		long maxTopic = userProfile.getMaxTopicInPage() ;
		if(maxTopic > 0) this.maxTopic = maxTopic ;
		try{
			this.pageList.setPageSize(this.maxTopic);
		}catch (NullPointerException e) {
			e.printStackTrace();
		}
		this.updatePageList(this.pageList);
	}
	
	@SuppressWarnings("unused")
	private JCRPageList getPageTopics() throws Exception {
		return pageList ;
	}
	
	@SuppressWarnings("unused")
	private String[] getActionMenuForum() throws Exception {
		String []actions = {"EditForum", "SetUnLockForum", "SetLockedForum", "SetOpenForum", "SetCloseForum", "MoveForum", "RemoveForum", "WatchOption"};
		return actions;
	}

	@SuppressWarnings("unused")
	private String[] getActionMenuTopic() throws Exception {
		String []actions = {"EditTopic", "SetOpenTopic", "SetCloseTopic", "SetLockedTopic", "SetUnLockTopic", "SetStickTopic",
				"SetUnStickTopic", "SetMoveTopic", "SetDeleteTopic", "MergeTopic", "SetUnWaiting", "ApproveTopics"}; 
		return actions;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
  private List<Topic> getTopicPageLits() throws Exception {
		maxPage = this.pageList.getAvailablePage() ;
		if(this.pageSelect > maxPage)this.pageSelect = maxPage ;
		List<Topic> topics = null;
		while(topics == null && pageSelect >= 1){
			try {
				topics = pageList.getPage(pageSelect) ;
      } catch (Exception e) {
      	topics = null; 
      	--pageSelect;
      }
		}
		if(topics == null) topics = new ArrayList<Topic>(); 
		this.topicList = topics;
		for(Topic topic : this.topicList) {
			if(getUIFormCheckBoxInput(topic.getId()) != null) {
				getUIFormCheckBoxInput(topic.getId()).setChecked(false) ;
			}else {
				UIFormCheckBoxInput<Boolean> checkItem = new UIFormCheckBoxInput<Boolean>(topic.getId(), topic.getId(), false);
				addChild(checkItem);
			}
		}
		return this.topicList ;
	}
	
	@SuppressWarnings("unchecked")
  private Topic getTopic(String topicId) throws Exception {
		List<Topic> listTopic = this.pageList.getPage(0) ;
		for (Topic topic : listTopic) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return null ;
	}
	
	private JCRPageList getPageListPost(String topicId) throws Exception {
		String isApprove = "" ;
		String isHidden = "" ;
		String userLogin = this.userProfile.getUserId();
		Topic topic = getTopic(topicId) ;
		long role = this.userProfile.getUserRole() ;
		if(role >=2){ isHidden = "false" ;}
		if(role == 1) {
			if(!ForumServiceUtils.hasPermission(forum.getModerators(), userLogin)){
				isHidden = "false" ;
			}
		}
		if(this.forum.getIsModeratePost() || topic.getIsModeratePost()) {
			if(isHidden.equals("false") && !(topic.getOwner().equals(userLogin))) isApprove = "true" ;
		}
		JCRPageList pageListPost = this.forumService.getPosts(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, topicId, isApprove, isHidden, "", userLogin)	; 
		long maxPost = getUserProfile().getMaxPostInPage() ;
		if(maxPost > 0) this.maxPost = maxPost ;
		pageListPost.setPageSize(this.maxPost) ;
		return pageListPost;
	}
	
	
	@SuppressWarnings("unused")
	private String[] getStarNumber(Topic topic) throws Exception {
		double voteRating = topic.getVoteRating() ;
		return ForumUtils.getStarNumber(voteRating) ;
	}

	@SuppressWarnings("unused")
	private List<Tag> getTagsByTopic(String[] tagIds) throws Exception {
		return this.forumService.getTagsByTopic(ForumSessionUtils.getSystemProvider(), tagIds);	
	}
	
	@SuppressWarnings("unchecked")
  private List<String> getIdSelected() throws Exception{
		List<UIComponent> children = this.getChildren() ;
		List<String> ids = new ArrayList<String>() ;
		for (int i = 0; i <= this.maxPage; i++) {
			if(this.getListChecked(i) != null)ids.addAll(this.getListChecked(i));
		}
		for(UIComponent child : children) {
			if(child instanceof UIFormCheckBoxInput) {
				if(((UIFormCheckBoxInput)child).isChecked()) {
					if(!ids.contains(child.getName()))ids.add(child.getName());
				}
			}
		}
		this.cleanCheckedList();
		return ids;
	}
	
	static public class SearchFormActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource() ;
			String path = uiTopicContainer.forum.getPath() ;
			UIFormStringInput formStringInput = uiTopicContainer.getUIStringInput(ForumUtils.SEARCHFORM_ID) ;
			String text = formStringInput.getValue() ;
			if(!ForumUtils.isEmpty(text) && !ForumUtils.isEmpty(path)) {
				String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=";
				for (int i = 0; i < special.length(); i++) {
					char c = special.charAt(i);
					if(text.indexOf(c) >= 0) {
						UIApplication uiApp = uiTopicContainer.getAncestorOfType(UIApplication.class) ;
						uiApp.addMessage(new ApplicationMessage("UIQuickSearchForm.msg.failure", null, ApplicationMessage.WARNING)) ;
						return ;
					}
				}
				StringBuffer type = new StringBuffer();
				if(uiTopicContainer.isModerator){ 
					type.append("true,").append(Utils.TOPIC).append("/").append(Utils.POST);
				} else {
					type.append("false,").append(Utils.TOPIC).append("/").append(Utils.POST);
				}
				UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
				UICategories categories = forumPortlet.findFirstComponentOfType(UICategories.class);
				categories.setIsRenderChild(true) ;
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				List<ForumSearch> list = forumService.getQuickSearch(ForumSessionUtils.getSystemProvider(), text, type.toString(), path, ForumSessionUtils.getAllGroupAndMembershipOfUser(uiTopicContainer.getUserProfile().getUserId()));
				UIForumListSearch listSearchEvent = categories.getChild(UIForumListSearch.class) ;
				listSearchEvent.setListSearchEvent(list) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
				formStringInput.setValue("") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UIQuickSearchForm.msg.checkEmpty", args, ApplicationMessage.WARNING)) ;
			}
			
		}
	}
	
	static public class GoNumberPageActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer topicContainer = event.getSource() ;
			int idbt = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
			UIFormStringInput stringInput1 = topicContainer.getUIStringInput(ForumUtils.GOPAGE_ID_T) ;
			UIFormStringInput stringInput2 = topicContainer.getUIStringInput(ForumUtils.GOPAGE_ID_B) ;
			String numberPage = "" ;
			if(idbt == 1) {
				numberPage = stringInput1.getValue() ;
			} else {
				numberPage = stringInput2.getValue() ;
			}
			stringInput1.setValue("") ; stringInput2.setValue("") ;
			numberPage = ForumUtils.removeZeroFirstNumber(numberPage) ;
			if(!ForumUtils.isEmpty(numberPage)) {
				try {
					long page = Long.parseLong(numberPage.trim()) ;
					if(page < 0) {
						Object[] args = { "go page" };
						throw new MessageException(new ApplicationMessage("NameValidator.msg.Invalid-number", args, ApplicationMessage.WARNING)) ;
					} else {
						if(page == 0) {
							page = (long)1;
						} else if(page > topicContainer.pageList.getAvailablePage()){
							page = topicContainer.pageList.getAvailablePage() ;
						}
						topicContainer.pageSelect = page ;
						event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer) ;
					}
				} catch (NumberFormatException e) {
					Object[] args = { "go page" };
					throw new MessageException(new ApplicationMessage("NameValidator.msg.Invalid-number", args, ApplicationMessage.WARNING)) ;
				}
			}
		}
	}
	
	static public class AddTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource() ;
			UIForumPortlet forumPortlet =uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UITopicForm topicForm = popupContainer.addChild(UITopicForm.class, null, null) ;
			topicForm.setTopicIds(uiTopicContainer.categoryId, uiTopicContainer.forumId, uiTopicContainer.forum) ;
			topicForm.setMod(uiTopicContainer.isModerator) ;
			popupContainer.setId("UIAddTopicContainer") ;
			popupAction.activate(popupContainer, 670, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class OpenTopicsTagActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource() ;
			String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.TAG) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption(Utils.FORUM_SERVICE) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(tagId) ;
			forumPortlet.getChild(UITopicsTag.class).setIdTag(tagId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class OpenTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			String idAndNumber = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String []temp = idAndNumber.split(",") ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			try {
				Topic topic = uiTopicContainer.getTopic(temp[0]) ;
				boolean isReadTopic = ForumUtils.isStringInStrings(uiTopicContainer.userProfile.getReadTopic(), topic.getId());
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
				uiForumContainer.setIsRenderChild(false) ;
				UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
				uiTopicDetail.setUpdateForum(uiTopicContainer.forum) ;
				uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic ) ;
				forumPortlet.getChild(UIForumLinks.class).setValueOption((uiTopicContainer.categoryId+"/"+ uiTopicContainer.forumId + " "));
				if(isReadTopic){
					uiTopicDetail.setGetTopic(true) ;
				}
				if(temp[2].equals("true")) {
					uiTopicDetail.setIdPostView("lastpost") ;
				} else {
					uiTopicDetail.setIdPostView("top") ;
				}
				JCRPageList pageList = uiTopicContainer.getPageListPost(temp[0]) ;
				long page = Long.parseLong(temp[1]) ;
				forumPortlet.setUserProfile() ;
				UserProfile userProfile = forumPortlet.getUserProfile() ;
				if(pageList != null) {
					pageList.setPageSize(userProfile.getMaxPostInPage()) ;
					if(page > pageList.getAvailablePage()) {
						page = pageList.getAvailablePage();
						Object[] args = { };
						UIApplication uiApp = uiTopicContainer.getAncestorOfType(UIApplication.class) ;
						uiApp.addMessage(new ApplicationMessage("UITopicDetail.msg.erro-change-posts-per-page", args, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					}
				}
				uiTopicDetail.setUpdateContainer(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, page) ;
				uiTopicDetail.setUpdatePageList(pageList) ;
				WebuiRequestContext context = event.getRequestContext() ;
				context.addUIComponentToUpdateByAjax(uiForumContainer) ;
				context.addUIComponentToUpdateByAjax(forumPortlet.getChild(UIBreadcumbs.class)) ;
			} catch (NullPointerException e) {
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
				UIApplication uiApp = uiTopicContainer.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UITopicContainer.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
				return;
			}
		}
	}

	static public class EditForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIForumForm forumForm = popupContainer.addChild(UIForumForm.class, null, null) ;
			boolean isMode = false ;
			if(uiTopicContainer.userProfile.getUserRole() == 1) isMode = true;
			forumForm.setMode(isMode);
			forumForm.initForm();
			forumForm.setCategoryValue(uiTopicContainer.categoryId, false) ;
			forumForm.setForumValue(forum, true);
			forumForm.setForumUpdate(true) ;
			popupContainer.setId("EditForumForm") ;
			popupAction.activate(popupContainer, 650, 480) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			uiTopicContainer.isUpdate = true ;
		}
	}	
	
	static public class SetLockedForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			forum.setIsLock(true);
			uiTopicContainer.isUpdate = true ;
			uiTopicContainer.forumService.modifyForum(ForumSessionUtils.getSystemProvider(), forum, 2) ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetUnLockForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			forum.setIsLock(false);
			uiTopicContainer.isUpdate = true ;
			uiTopicContainer.forumService.modifyForum(ForumSessionUtils.getSystemProvider(), forum, 2) ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetOpenForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			forum.setIsClosed(false);
			uiTopicContainer.isUpdate = true ;
			uiTopicContainer.forumService.modifyForum(ForumSessionUtils.getSystemProvider(), forum, 1) ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetCloseForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			forum.setIsClosed(true);
			uiTopicContainer.isUpdate = true ;
			uiTopicContainer.forumService.modifyForum(ForumSessionUtils.getSystemProvider(), forum, 1) ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	} 
	
	static public class MoveForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			List <Forum> forums = new ArrayList<Forum>();
			forums.add(forum);
			uiTopicContainer.isUpdate = true ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIMoveForumForm moveForumForm = popupAction.createUIComponent(UIMoveForumForm.class, null, null) ;
			moveForumForm.setListForum(forums, uiTopicContainer.categoryId);
			moveForumForm.setForumUpdate(true) ;
			popupAction.activate(moveForumForm, 315, 365) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class RemoveForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			uiTopicContainer.forumService.removeForum(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, forum.getId()) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
			categoryContainer.updateIsRender(false) ;
			categoryContainer.getChild(UICategory.class).updateByBreadcumbs(uiTopicContainer.categoryId) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(uiTopicContainer.categoryId) ;
			forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class WatchOptionActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIWatchToolsForm watchToolsForm = popupAction.createUIComponent(UIWatchToolsForm.class, null, null) ;
			watchToolsForm.setPath(forum.getPath());
			watchToolsForm.setEmails(forum.getEmailNotification()) ;
			watchToolsForm.setIsTopic(false);
			popupAction.activate(watchToolsForm, 500, 365) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	//----------------------------------MenuThread---------------------------------
	static public class ApproveTopicsActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			List <Topic> topics = new ArrayList<Topic>();
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					if(topic.getIsApproved()) continue ;
					topic.setIsApproved(true);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				uiTopicContainer.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 3) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPageListTopicUnApprove pageListTopicUnApprove	= popupAction.createUIComponent(UIPageListTopicUnApprove.class, null, null) ;
				pageListTopicUnApprove.setUpdateContainer(uiTopicContainer.categoryId, uiTopicContainer.forumId) ;
				popupAction.activate(pageListTopicUnApprove, 500, 365) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}
	}	

	static public class EditTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic = null ;
			boolean checked = false ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					checked = true ;
					break;
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(checked) {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UITopicForm topicForm = popupContainer.addChild(UITopicForm.class, null, null) ;
				topicForm.setTopicIds(uiTopicContainer.categoryId, uiTopicContainer.forumId, uiTopicContainer.forum) ;
				topicForm.setUpdateTopic(topic, true) ;
				topicForm.setMod(uiTopicContainer.isModerator) ;
				popupContainer.setId("UIEditTopicContainer") ;
				popupAction.activate(popupContainer, 670, 460) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}
		}
	}	
	
	static public class SetOpenTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			List <Topic> topics = new ArrayList<Topic>();
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					if(!topic.getIsClosed()) continue ;
					topic.setIsClosed(false);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				uiTopicContainer.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 1) ;
			} 
			if(topics.size() == 0){
				Object[] args = {"Open" };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetCloseTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List <Topic> topics = new ArrayList<Topic>();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					if(topic.getIsClosed()) continue ;
					topic.setIsClosed(true);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				uiTopicContainer.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 1) ;
			} 
			if(topics.size() == 0){
				Object[] args = { "Close" };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetLockedTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<Topic> topics = new ArrayList<Topic>();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					if(topic.getIsLock()) continue ;
					topic.setIsLock(true);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				uiTopicContainer.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 2) ;
			} 
			if(topics.size() == 0){
				Object[] args = { "Locked" };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class SetUnLockTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			try{
				if(uiTopicContainer.getForum().getIsLock()){
					UIApplication uiApp = uiTopicContainer.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UITopicContainer.sms.ForumIsLocked", new Object[]{}, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return;
				}
			} catch (Exception e){
				e.printStackTrace();
				return;
			}
			List<Topic> topics = new ArrayList<Topic>();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					if(!topic.getIsLock()) continue ;
					topic.setIsLock(false);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				uiTopicContainer.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 2) ;
			} 
			if(topics.size() == 0){
				Object[] args = { "UnLock" };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetUnStickTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<Topic> topics = new ArrayList<Topic>();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					if(topic.getIsSticky()){ topic.setIsSticky(false); topics.add(topic); }
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				uiTopicContainer.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 4) ;
			} else {
				Object[] args = { "UnStick" };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetStickTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<Topic> topics = new ArrayList<Topic>();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					if(!topic.getIsSticky()){ topic.setIsSticky(true); topics.add(topic); }
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				uiTopicContainer.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 4) ;
			}else {
				Object[] args = { "Stick" };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetMoveTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<Topic> topics = new ArrayList<Topic>();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIMoveTopicForm moveTopicForm = popupAction.createUIComponent(UIMoveTopicForm.class, null, null) ;
				moveTopicForm.setUserProfile(uiTopicContainer.userProfile) ;
				moveTopicForm.updateTopic(uiTopicContainer.forumId, topics, false);
				popupAction.activate(moveTopicForm, 400, 420) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} 
			if(topics.size() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheckMove", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class MergeTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<Topic> topics = new ArrayList<Topic>();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 1) {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIMergeTopicForm mergeTopicForm = popupAction.createUIComponent(UIMergeTopicForm.class, null, null) ;
				mergeTopicForm.updateTopics(topics) ;
				popupAction.activate(mergeTopicForm, 560, 260) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} 
			if(topics.size() <= 1){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheckThreads", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetDeleteTopicActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<Topic> topics = new ArrayList<Topic>();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				for(Topic topic_ : topics) {
					try{
						uiTopicContainer.forumService.removeTopic(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, uiTopicContainer.forumId, topic_.getId()) ;
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			} else if (topics.size() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheckMove", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetUnWaitingActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<Topic> topics = new ArrayList<Topic>();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopic(topicId);
				if(topic != null) {
					if(topic.getIsWaiting()){ topic.setIsWaiting(false) ;topics.add(topic); } 
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				uiTopicContainer.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 5) ;
			} 
			if(topics.size() == 0){
				Object[] args = {};
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheckUnWait", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class SetOrderByActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(uiContainer.strOrderBy)) {
				if(uiContainer.strOrderBy.indexOf(path) >= 0) {
					if(uiContainer.strOrderBy.indexOf("descending") > 0) {
						uiContainer.strOrderBy = path + " ascending";
					} else {
						uiContainer.strOrderBy = path + " descending";
					}
				} else {
					uiContainer.strOrderBy = path + " ascending";
				}
			} else {
				uiContainer.strOrderBy = path + " ascending";
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
		}
	}
	
	static public class AddBookMarkActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer topicContainer = event.getSource();
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(topicId)) {
				try{
				Topic topic = topicContainer.getTopic(topicId);
				StringBuffer buffer = new StringBuffer();
				buffer.append("ThreadNoNewPost//").append(topic.getTopicName()).append("//")
				.append(topicContainer.categoryId).append("/").append(topicContainer.forumId).append("/").append(topicId) ;
				String userName = topicContainer.userProfile.getUserId() ;
				topicContainer.forumService.saveUserBookmark(ForumSessionUtils.getSystemProvider(), userName, buffer.toString(), true) ;
				UIForumPortlet forumPortlet = topicContainer.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.setUserProfile() ;
				} catch (Exception e) {
				}
			}
		}
	}
	
	static public class AddWatchingActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer topicContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			path = topicContainer.categoryId+"/"+topicContainer.forumId+"/"+path ;
			UIForumPortlet forumPortlet = topicContainer.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIAddWatchingForm addWatchingForm = popupAction.createUIComponent(UIAddWatchingForm.class, null, null) ;
			addWatchingForm.initForm() ;
			addWatchingForm.setPathNode(path);
			popupAction.activate(addWatchingForm, 425, 250) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
}
