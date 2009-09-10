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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionResponse;
import javax.xml.namespace.QName;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIBanIPForumManagerForm;
import org.exoplatform.forum.webui.popup.UIExportForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIMergeTopicForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIMoveTopicForm;
import org.exoplatform.forum.webui.popup.UIPageListTopicUnApprove;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UITopicForm;
import org.exoplatform.forum.webui.popup.UIWatchToolsForm;
import org.exoplatform.ks.rss.RSS;
import org.exoplatform.services.portletcontainer.plugins.pc.portletAPIImp.PortletRequestImp;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen
 * hung.nguyen@exoplatform.com Aus 01, 2007 2:48:18 PM
 */

@ComponentConfig(
	lifecycle = UIFormLifecycle.class,
	template =	"app:/templates/forum/webui/UITopicContainer.gtmpl", 
	events = {
		@EventConfig(listeners = UITopicContainer.SearchFormActionListener.class ),	
		@EventConfig(listeners = UITopicContainer.GoNumberPageActionListener.class ),	
		@EventConfig(listeners = UITopicContainer.AddTopicActionListener.class ),	
		@EventConfig(listeners = UITopicContainer.OpenTopicActionListener.class ),
		@EventConfig(listeners = UITopicContainer.OpenTopicsTagActionListener.class ),// Menu
																																									// Forum
		@EventConfig(listeners = UITopicContainer.EditForumActionListener.class ),	
		@EventConfig(listeners = UITopicContainer.SetLockedForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetUnLockForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetOpenForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.SetCloseForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.MoveForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.RemoveForumActionListener.class,confirm="UITopicContainer.confirm.RemoveForum"),// Menu
																																																															// Topic
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
		@EventConfig(listeners = UITopicContainer.ApproveTopicsActionListener.class ),
		@EventConfig(listeners = UITopicContainer.ActivateTopicsActionListener.class ),
		
		@EventConfig(listeners = UITopicContainer.SetOrderByActionListener.class),
		@EventConfig(listeners = UITopicContainer.AddWatchingActionListener.class),
		@EventConfig(listeners = UITopicContainer.AddBookMarkActionListener.class),
		@EventConfig(listeners = UITopicContainer.ExportForumActionListener.class),
		@EventConfig(listeners = UITopicContainer.AdvancedSearchActionListener.class),
		@EventConfig(listeners = UITopicContainer.BanIpForumToolsActionListener.class),
		@EventConfig(listeners = UITopicContainer.RSSActionListener.class),
		@EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
	}
)
@SuppressWarnings({ "unchecked", "unused" })
public class UITopicContainer extends UIForumKeepStickPageIterator {
	private ForumService forumService ;
	private String forumId = "";
	private String categoryId = "";
	private Forum forum;
	private List <Topic> topicList ;
	private List<String> moderators;
	private boolean isUpdate = false;
	private boolean isModerator = false ;
	private boolean canAddNewThread = true ;
	private UserProfile userProfile = null;
	private String strOrderBy = "" ;
	private boolean isLogin = false;
	private boolean isNull = false; 
	private boolean enableIPLogging = true;
	private boolean isReload = true;
  private String DEFAULT_ID = TopicType.DEFAULT_ID;
	public boolean isNull() { return isNull; }
	public void setNull(boolean isNull) { this.isNull = isNull;}
	public boolean isLogin() {return isLogin;}
	public void setLogin(boolean isLogin) {this.isLogin = isLogin;}
	private Map<String, TopicType> topicTypeM = new HashMap<String, TopicType>();
	public UITopicContainer() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_T, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_B, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.SEARCHFORM_ID, null)) ;
		if(!ForumSessionUtils.isAnonim()) isLogin = true;
		isLink = true;
	}
	
	private UserProfile getUserProfile() { return userProfile ;}
	public void setUserProfile(UserProfile userProfile) throws Exception {
		this.userProfile	= userProfile ;
  }

	public String getRSSLink(String cateId){
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return RSS.getRSSLink("forum", pcontainer.getPortalContainerInfo().getContainerName(), cateId);
	}
	
  private String getLastPostIdReadOfTopic(String topicId) throws Exception {
		return userProfile.getLastPostIdReadOfTopic(topicId);
	}
	
	public void setUpdateForum(String categoryId, Forum forum, int page) throws Exception {
		this.forum = forum ;
		this.forumId = forum.getId() ;
		this.categoryId = categoryId ;
		this.pageSelect = page ;
		this.isUpdate = false ;
		this.isReload = false;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
		this.isUseAjax = forumPortlet.isUseAjax();
		enableIPLogging = forumPortlet.isEnableIPLogging() ;
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId)) ;
		forumPortlet.updateAccessForum(forumId);
		this.userProfile = forumPortlet.getUserProfile() ;
		cleanCheckedList();
		setForum(true);
	}
	
  private boolean getIsAutoPrune() throws Exception {
		PruneSetting pruneSetting = new PruneSetting();
		try {
			pruneSetting = forumService.getPruneSetting(forum.getPath());
    } catch (Exception e) {
    }
		return pruneSetting.isActive();
	}
	
  public void setTopicType(String typeId) throws Exception {
  	try {
  		TopicType topicType = forumService.getTopicType(typeId);
  		if(topicType.getId().equals(TopicType.DEFAULT_ID)) {
  			if(topicTypeM.containsKey(typeId)) topicTypeM.remove(typeId) ;
  		} else topicTypeM.put(typeId, topicType);
    } catch (Exception e) {
    	if(topicTypeM.containsKey(typeId)) topicTypeM.remove(typeId) ;
    }
  }
  
  private String[] getIconTopicType(String typeId) throws Exception {
		try {
			TopicType topicType = topicTypeM.get(typeId);
			if(topicType != null) {
				return new String[]{topicType.getIcon(), topicType.getName()};
			} else {
				topicType = forumService.getTopicType(typeId);
				if(!topicType.getId().equals(TopicType.DEFAULT_ID)) {
					topicTypeM.put(typeId, topicType);
					return new String[]{topicType.getIcon(), topicType.getName()};
				} else {
					return new String[]{" "};
				}
			}
    } catch (Exception e) {
    	e.printStackTrace();
	    return new String[]{" "};
    }
	}
	
  private String getScreenName(String userName) throws Exception {
		return forumService.getScreenName(userName);
	}
	
	public void setIdUpdate(boolean isUpdate) { this.isUpdate = isUpdate;}
	
	public void updateByBreadcumbs(String categoryId, String forumId, boolean isBreadcumbs, int page) throws Exception {
		this.forumId = forumId ;
		this.categoryId = categoryId ;
		this.isUpdate = true ;
		this.pageSelect = page ;
		this.isReload = false;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
		this.isUseAjax = forumPortlet.isUseAjax();
		enableIPLogging = forumPortlet.isEnableIPLogging() ;
		forumPortlet.updateAccessForum(forumId);
		this.userProfile = forumPortlet.getUserProfile() ;
		if(!isBreadcumbs) {
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId)) ;
		}
		cleanCheckedList();
		setForum(true);
	}

	public boolean getCanAddNewThread(){return this.canAddNewThread ; }
	
	private void setForumModeratorPortlet() throws Exception {
		try {
			PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
			ActionResponse actionRes = (ActionResponse)pcontext.getResponse();
			ForumParameter param = new ForumParameter() ;
			param.setRenderModerator(true);
			param.setModerators(moderators);
			param.setRenderRule(true);
			List<String> list = param.getInfoRules();
			boolean isLock = forum.getIsClosed() ;
			if(!isLock) isLock = forum.getIsLock() ;
			if(!isLock) isLock = !canAddNewThread ;
			list.set(0, String.valueOf(isLock));
			param.setInfoRules(list);
			actionRes.setEvent(new QName("ForumModerateEvent"), param) ;
			actionRes.setEvent(new QName("ForumRuleEvent"), param) ;
    } catch (Exception e) {
    }
	}
	
	public void setForum(boolean isSetModerator) throws Exception {
		if(this.isUpdate || forum == null) {
			this.forum = forumService.getForum(categoryId, forumId);
			this.isUpdate = false ;
		}
		this.canAddNewThread = true ;
		moderators = ForumServiceUtils.getUserPermission(forum.getModerators()) ;
		String userId = userProfile.getUserId() ;
		List<String> ipBaneds = forum.getBanIP();
		isModerator = false ;
		if(userProfile.getUserRole() == 0 || (!moderators.isEmpty() && moderators.contains(userId))) isModerator = true;
		if(ipBaneds != null && ipBaneds.size() > 0) {
			if(!ipBaneds.contains(getIPRemoter())) {
				String[] strings = this.forum.getCreateTopicRole() ;
				if(strings != null && strings.length > 0 && !strings[0].equals(" ")){
					canAddNewThread = ForumServiceUtils.hasPermission(strings, userId) ;
				}
				if(!canAddNewThread || strings == null || strings.length == 0 || strings[0].equals(" ")){
					strings = forumService.getPermissionTopicByCategory(categoryId, "createTopic");
					if(strings != null && strings.length > 0 && !strings[0].equals(" ")){
						canAddNewThread = ForumServiceUtils.hasPermission(strings, userId) ;
					}
				}
			} else canAddNewThread = false;
		} else {
			if(!isModerator) {
				String[] strings = this.forum.getCreateTopicRole() ;
				if(strings != null && strings.length > 0){
					canAddNewThread = ForumServiceUtils.hasPermission(strings, userId) ;
				}
				if(!canAddNewThread || strings == null || strings.length == 0 || strings[0].equals(" ")){
					strings = forumService.getPermissionTopicByCategory(categoryId, "createTopic");
					if(strings != null && strings.length > 0 && !strings[0].equals(" ")){
						canAddNewThread = ForumServiceUtils.hasPermission(strings, userId) ;
					}
				}
			}
		}
		if(isSetModerator) setForumModeratorPortlet();
		UIForumContainer forumContainer = this.getParent() ;
		if(this.forum != null){
			forumContainer.findFirstComponentOfType(UIForumInfos.class).setForum(this.forum);
		}
	}
	
	private Forum getForum() throws Exception {
		return this.forum ;
	}
	
	private void initPage() throws Exception {
		objectId = forumId;
		if(userProfile == null) userProfile = new UserProfile();
		StringBuffer strQuery = new StringBuffer() ;
		String userId = userProfile.getUserId() ;
		if(isReload)setForum(false);
		else isReload = true;
		if(!isModerator) {
			strQuery.append("@exo:isClosed='false' and @exo:isWaiting='false' and @exo:isActive='true'");
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
		//this.pageList = forumService.getPageTopic(categoryId, forumId, strQuery.toString(), strOrderBy);
		
    int maxTopic = userProfile.getMaxTopicInPage().intValue() ;
    if(maxTopic <= 0) maxTopic = 10 ;
		
		this.pageList = forumService.getTopicList(categoryId, forumId, strQuery.toString(), strOrderBy, maxTopic);


	}
	
	private String getIPRemoter() throws Exception {
		if(enableIPLogging) {
			WebuiRequestContext	context =	RequestContext.getCurrentInstance() ;
			PortletRequestImp request = context.getRequest() ;
			return request.getRemoteAddr();
		}
		return "";
	}
	
	private String[] getActionMenuForum() throws Exception {
		String []actions ;
		if(userProfile.getUserRole() == 0) actions = new String[]{"EditForum", "SetUnLockForum", "SetLockedForum", "SetOpenForum", "SetCloseForum", 
				"MoveForum", "RemoveForum", "ExportForum", "WatchOption", "BanIpForumTools"};
		else actions = new String[]{"EditForum", "SetUnLockForum", "SetLockedForum", "SetOpenForum", "SetCloseForum", 
				"ExportForum", "WatchOption", "BanIpForumTools"};
		return actions;
	}

	private String[] getActionMenuTopic() throws Exception {
		String []actions = {"EditTopic", "SetOpenTopic", "SetCloseTopic", "SetLockedTopic", "SetUnLockTopic", "SetStickTopic",
				"SetUnStickTopic", "SetMoveTopic", "SetDeleteTopic", "MergeTopic", "SetUnWaiting", "ApproveTopics", "ActivateTopics"}; 
		return actions;
	}
	
	
  private List<Topic> getTopicPageList() throws Exception {
		maxPage = this.pageList.getAvailablePage() ;
		if(this.pageSelect > maxPage)this.pageSelect = maxPage ;
		topicList = pageList.getPage(pageSelect);
		pageSelect = pageList.getCurrentPage();
		if(topicList == null) topicList = new ArrayList<Topic>();
		try {
			for(Topic topic : topicList) {
				if(getUIFormCheckBoxInput(topic.getId()) != null) {
					getUIFormCheckBoxInput(topic.getId()).setChecked(false) ;
				}else {
					UIFormCheckBoxInput<Boolean> checkItem = new UIFormCheckBoxInput<Boolean>(topic.getId(), topic.getId(), false);
					addChild(checkItem);
				}
			}  
    } catch (Exception e) {}
		return topicList ;
	}
	
  private Topic getTopicByAll(String topicId) throws Exception {
		List<Topic> listTopic = this.pageList.getAll() ;
		for (Topic topic : listTopic) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return null ;
	}
	
  private Topic getTopic(String topicId) throws Exception {
  	try {
  		for (Topic topic : topicList) {
  			if(topic.getId().equals(topicId)) return topic ;
  		}
    } catch (Exception e) {}
		return null ;
	}
	
  private long getSizePost(Topic topic) throws Exception {
		long maxPost = userProfile.getMaxPostInPage() ;
		if(maxPost <= 0) maxPost = 10;
		if(topic.getPostCount() >= maxPost) {
			long availablePost = 0;
			if(isModerator){
				availablePost = topic.getPostCount()+1;
			} else {
				String isApprove = "" ;
				String userLogin = userProfile.getUserId();
				if(this.forum.getIsModeratePost() || topic.getIsModeratePost()) {
					if(!(topic.getOwner().equals(userLogin))) isApprove = "true" ;
				}
				availablePost = this.forumService.getAvailablePost(this.categoryId, this.forumId, topic.getId(), isApprove, "false", userLogin)	;
			}
			long value = (availablePost)/maxPost;
			if((value*maxPost) < availablePost) value = value + 1;
			return value;
		} else return 1;
	}
	
	private String[] getStarNumber(Topic topic) throws Exception {
		double voteRating = topic.getVoteRating() ;
		return ForumUtils.getStarNumber(voteRating) ;
	}

	/*@SuppressWarnings("unused")
	private List<Tag> getTagsByTopic(String[] tagIds) throws Exception {
		List<Tag> list = new ArrayList<Tag>();
		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
		try {
			list = this.forumService.getTagsByTopic(sProvider, tagIds);
    } catch (Exception e) {
    }finally{
    	sProvider.close();
    }
		return list;	
	}*/
	
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
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class);
				categoryContainer.updateIsRender(true);
				UICategories categories = categoryContainer.getChild(UICategories.class);
				categories.setIsRenderChild(true) ;
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				List<ForumSearch> list = forumService.getQuickSearch(text, type.toString(), path, uiTopicContainer.getUserProfile().getUserId(),
																							forumPortlet.getInvisibleCategories(), forumPortlet.getInvisibleForums(), null);
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
					int page = Integer.parseInt(numberPage.trim()) ;
					if(page < 0) {
						Object[] args = { "go page" };
						throw new MessageException(new ApplicationMessage("NameValidator.msg.Invalid-number", args, ApplicationMessage.WARNING)) ;
					} else {
						if(page == 0) {
							page = 1;
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
			topicForm.setTopicIds(uiTopicContainer.categoryId, uiTopicContainer.forumId, uiTopicContainer.forum, uiTopicContainer.userProfile.getUserRole()) ;
			topicForm.setMod(uiTopicContainer.isModerator) ;
			popupContainer.setId("UIAddTopicContainer") ;
			popupAction.activate(popupContainer, 850, 500) ;
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
				if(topic != null) {
					uiTopicContainer.forum = uiTopicContainer.forumService.getForum(uiTopicContainer.categoryId, uiTopicContainer.forumId);
					if(uiTopicContainer.forum != null){
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
						uiForumContainer.setIsRenderChild(false) ;
						UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
						uiTopicDetail.setUpdateForum(uiTopicContainer.forum) ;
						uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic.getId() ) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((uiTopicContainer.categoryId+"/"+ uiTopicContainer.forumId + " "));
						if(temp[2].equals("true")) {
							uiTopicDetail.setIdPostView("lastpost") ;
						} else if(temp[2].equals("false")){
							uiTopicDetail.setIdPostView("top") ;
						} else {
							uiTopicDetail.setIdPostView(temp[2]) ;
							uiTopicDetail.setLastPostId(temp[2]);
						}
						if(!forumPortlet.getUserProfile().getUserId().equals(UserProfile.USER_GUEST)) {
							uiTopicContainer.forumService.updateTopicAccess(forumPortlet.getUserProfile().getUserId(),  topic.getId()) ;
						}
						uiTopicDetail.setUpdateContainer(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, Integer.parseInt(temp[1])) ;
						WebuiRequestContext context = event.getRequestContext() ;
						context.addUIComponentToUpdateByAjax(uiForumContainer) ;
						context.addUIComponentToUpdateByAjax(forumPortlet.getChild(UIBreadcumbs.class)) ;
					} else {
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
						categoryContainer.updateIsRender(true) ;
						categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
						forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
						UIApplication uiApp = uiTopicContainer.getAncestorOfType(UIApplication.class) ;
						uiApp.addMessage(new ApplicationMessage("UITopicContainer.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
					}
				} else {
					UIApplication uiApp = uiTopicContainer.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiTopicContainer) ;
				}
			} catch (Exception e) {
				e.printStackTrace();
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
			uiTopicContainer.isUpdate = true ;
			uiTopicContainer.isReload = false;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class SetLockedForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			forum.setIsLock(true);
			try {
				uiTopicContainer.forumService.modifyForum(forum, 2) ;
				uiTopicContainer.isUpdate = true ;
				uiTopicContainer.isReload = false;
				uiTopicContainer.setForum(true);
			} catch(Exception e) {
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetUnLockForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			forum.setIsLock(false);
			try {
				uiTopicContainer.forumService.modifyForum(forum, 2) ;
				uiTopicContainer.isUpdate = true ;
				uiTopicContainer.isReload = false;
				uiTopicContainer.setForum(true);
			} catch(Exception e) {
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetOpenForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			forum.setIsClosed(false);
			try {
				uiTopicContainer.forumService.modifyForum(forum, 1) ;
				uiTopicContainer.isUpdate = true ;
				uiTopicContainer.isReload = false;
				uiTopicContainer.setForum(true);
			} catch(Exception e) {
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetCloseForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			forum.setIsClosed(true);
			try {
				uiTopicContainer.forumService.modifyForum(forum, 1) ;
				uiTopicContainer.isUpdate = true ;
				uiTopicContainer.isReload = false;
				uiTopicContainer.setForum(true);
			} catch(Exception e) {
			}
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
			try {
				uiTopicContainer.forumService.removeForum(uiTopicContainer.categoryId, forum.getId()) ;
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
				categoryContainer.updateIsRender(false) ;
				categoryContainer.getChild(UICategory.class).updateByBreadcumbs(uiTopicContainer.categoryId) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(uiTopicContainer.categoryId) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
			} catch(Exception e) {
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class ExportForumActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(forum == null){
				UIApplication uiApp = uiTopicContainer.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UITopicContainer.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
				return;
			}
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIExportForm exportForm = popupAction.createUIComponent(UIExportForm.class, null, null) ;
			exportForm.setObjectId(forum);
			popupAction.activate(exportForm, 380, 160) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static public class WatchOptionActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			try {
				uiTopicContainer.forum = uiTopicContainer.forumService.getForum(uiTopicContainer.categoryId, uiTopicContainer.forumId);;
				UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIWatchToolsForm watchToolsForm = popupAction.createUIComponent(UIWatchToolsForm.class, null, null) ;
				watchToolsForm.setPath(uiTopicContainer.forum.getPath());
				watchToolsForm.setEmails(uiTopicContainer.forum.getEmailNotification()) ;
				popupAction.activate(watchToolsForm, 500, 365) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch (Exception e) {
      	e.printStackTrace();
      	UIApplication uiApp = uiTopicContainer.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UITopicContainer.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTopicContainer);
		}
	}	
	
	// ----------------------------------MenuThread---------------------------------
	static public class ApproveTopicsActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			List <Topic> topics = new ArrayList<Topic>();
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					if(topic.getIsApproved()) continue ;
					topic.setIsApproved(true);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					uiTopicContainer.forumService.modifyTopic(topics, 3) ;
				} catch(Exception e) {
				}
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

	static public class ActivateTopicsActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<String> topicIds = uiTopicContainer.getIdSelected() ;
			List <Topic> topics = new ArrayList<Topic>();
			Topic topic ;
			for(String topicId : topicIds) {
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					if(topic.getIsActive()) continue ;
					topic.setIsActive(true);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					uiTopicContainer.forumService.modifyTopic(topics, 6) ;
				} catch(Exception e) {
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
//				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
//				UIPageListTopicUnApprove pageListTopicUnApprove	= popupAction.createUIComponent(UIPageListTopicUnApprove.class, null, null) ;
//				pageListTopicUnApprove.setUpdateContainer(uiTopicContainer.categoryId, uiTopicContainer.forumId) ;
//				popupAction.activate(pageListTopicUnApprove, 500, 365) ;
//				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
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
				topic = uiTopicContainer.getTopicByAll(topicId);
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
				topicForm.setTopicIds(uiTopicContainer.categoryId, uiTopicContainer.forumId, uiTopicContainer.forum, uiTopicContainer.userProfile.getUserRole()) ;
				topicForm.setUpdateTopic(topic, true) ;
				topicForm.setMod(uiTopicContainer.isModerator) ;
				popupContainer.setId("UIEditTopicContainer") ;
				popupAction.activate(popupContainer, 850, 460) ;
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
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					if(!topic.getIsClosed()) continue ;
					topic.setIsClosed(false);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					uiTopicContainer.forumService.modifyTopic(topics, 1) ;
				} catch(Exception e) {
				}
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
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					if(topic.getIsClosed()) continue ;
					topic.setIsClosed(true);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					uiTopicContainer.forumService.modifyTopic(topics, 1) ;
				} catch(Exception e) {
				}
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
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					if(topic.getIsLock()) continue ;
					topic.setIsLock(true);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					uiTopicContainer.forumService.modifyTopic(topics, 2) ;
				} catch(Exception e) {
				}
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
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					if(!topic.getIsLock()) continue ;
					topic.setIsLock(false);
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					uiTopicContainer.forumService.modifyTopic(topics, 2) ;
				} catch(Exception e) {
				}
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
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					if(topic.getIsSticky()){ topic.setIsSticky(false); topics.add(topic); }
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					uiTopicContainer.forumService.modifyTopic(topics, 4) ;
				} catch(Exception e) {
				}
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
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					if(!topic.getIsSticky()){ topic.setIsSticky(true); topics.add(topic); }
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					uiTopicContainer.forumService.modifyTopic(topics, 4) ;
				} catch(Exception e) {
				}
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
				topic = uiTopicContainer.getTopicByAll(topicId);
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
				topic = uiTopicContainer.getTopicByAll(topicId);
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
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					topics.add(topic);
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					for(Topic topic_ : topics) {
						try{
							uiTopicContainer.forumService.removeTopic(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic_.getId()) ;
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				} catch(Exception e) {
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
				topic = uiTopicContainer.getTopicByAll(topicId);
				if(topic != null) {
					if(topic.getIsWaiting()){ topic.setIsWaiting(false) ;topics.add(topic); } 
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				try {
					uiTopicContainer.forumService.modifyTopic(topics, 5) ;
				} catch(Exception e) {
				}
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
					StringBuffer buffer = new StringBuffer();
					if(topicId.equals("forum")) {
						buffer.append("ForumNormalIcon//").append(topicContainer.forum.getForumName()).append("//").append(topicContainer.forumId);
					}else {
						Topic topic = topicContainer.getTopicByAll(topicId);
						buffer.append("ThreadNoNewPost//").append(topic.getTopicName()).append("//").append(topicId) ;
					}
					String userName = topicContainer.userProfile.getUserId() ;
					topicContainer.forumService.saveUserBookmark(userName, buffer.toString(), true) ;
					UIForumPortlet forumPortlet = topicContainer.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.updateUserProfileInfo() ;
				} catch (Exception e) {
				}
			}
		}
	}
	
	static public class AddWatchingActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer topicContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(path.equals("forum")){
				path = topicContainer.categoryId+"/"+topicContainer.forumId ;
				topicContainer.isUpdate = true;
			} else {
				path = topicContainer.categoryId+"/"+topicContainer.forumId+"/"+path ;
			}
			List<String> values = new ArrayList<String>();
			String userName = topicContainer.userProfile.getUserId();
			try {
				values.add(topicContainer.userProfile.getEmail());
				topicContainer.forumService.addWatch(1, path, values, userName) ;
				Object[] args = { };
				UIApplication uiApp = topicContainer.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.successfully", args, ApplicationMessage.INFO)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			} catch (Exception e) {
				Object[] args = { };
				UIApplication uiApp = topicContainer.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.fall", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			}
		}
	}
	
	static	public class AdvancedSearchActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FIELD_SEARCHFORUM_LABEL) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			UISearchForm searchForm = forumPortlet.getChild(UISearchForm.class) ;
			searchForm.setUserProfile(forumPortlet.getUserProfile()) ;
			searchForm.setSelectType(Utils.TOPIC) ;
			searchForm.setPath(uiForm.forum.getPath());
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

	static	public class BanIpForumToolsActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIBanIPForumManagerForm ipForumManager = popupContainer.addChild(UIBanIPForumManagerForm.class, null, null) ;
			popupContainer.setId("BanIPForumManagerForm") ;
			ipForumManager.setForumId(uiForm.categoryId + "/" + uiForm.forumId);
			popupAction.activate(popupContainer, 450, 500) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	

	static public class RSSActionListener extends EventListener<UITopicContainer> {
		public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiForm = event.getSource();
			String forumId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!uiForm.userProfile.getUserId().equals(UserProfile.USER_GUEST)){
				uiForm.forumService.addWatch(-1, forumId, null, uiForm.userProfile.getUserId());
			}
			
			/*String rssLink = uiForm.getRSSLink(forumId);
			UIForumPortlet portlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("ForumRSSForm") ;
			UIRSSForm exportForm = popupContainer.addChild(UIRSSForm.class, null, null) ;
			popupAction.activate(popupContainer, 560, 170) ;
			exportForm.setRSSLink(rssLink);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;*/
		}
	}
}
