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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIMergeTopicForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIMoveTopicForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupComponent;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UITopicForm;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

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
			@EventConfig(listeners = UITopicContainer.GoNumberPageActionListener.class ),	
			@EventConfig(listeners = UITopicContainer.AddTopicActionListener.class ),	
			@EventConfig(listeners = UITopicContainer.OpenTopicActionListener.class ),
			@EventConfig(listeners = UITopicContainer.OpenTopicsTagActionListener.class ),
//			@EventConfig(listeners = UITopicContainer.DisplayOptionActionListener.class ),//Menu Forum
			@EventConfig(listeners = UITopicContainer.EditForumActionListener.class ),	
			@EventConfig(listeners = UITopicContainer.SetLockedForumActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetUnLockForumActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetOpenForumActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetCloseForumActionListener.class),
			@EventConfig(listeners = UITopicContainer.MoveForumActionListener.class),
			@EventConfig(listeners = UITopicContainer.RemoveForumActionListener.class),//Menu Topic
			@EventConfig(listeners = UITopicContainer.EditTopicActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetOpenTopicActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetCloseTopicActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetLockedTopicActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetUnLockTopicActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetStickTopicActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetUnStickTopicActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetMoveTopicActionListener.class),
			@EventConfig(listeners = UITopicContainer.MergeTopicActionListener.class),
			@EventConfig(listeners = UITopicContainer.SetDeleteTopicActionListener.class)
		}
)
public class UITopicContainer extends UIForm implements UIPopupComponent {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String forumId = "";
	private String categoryId = "";
	private Forum forum;
	private JCRPageList pageList ;
	private List <Topic> topicList ;
	private long page = 1 ;
	private boolean isGoPage = false;
	private boolean isUpdate = false;
	private long maxTopic = 10 ;
	private long maxPost = 10 ;
	private UserProfile userProfile = null;
	public UITopicContainer() throws Exception {
		addUIFormInput( new UIFormStringInput("gopage1", null)) ;
		addUIFormInput( new UIFormStringInput("gopage2", null)) ;
		addChild(UIForumPageIterator.class, null, "ForumPageIterator") ;
	}
	
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() {
		return userProfile ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setUpdateForum(String categoryId, Forum forum) throws Exception {
		this.forum = forum ;
		this.forumId = forum.getId() ;
		this.categoryId = categoryId ;
		this.page = 1;
		this.isGoPage = true ;
		getChild(UIForumPageIterator.class).setSelectPage(this.page) ;
		this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId)) ;
	}
	
	public void updateByBreadcumbs(String categoryId, String forumId, boolean isBreadcumbs) throws Exception {
		this.forumId = forumId ;
		this.categoryId = categoryId ;
		this.isUpdate = true ;
		this.page = 1;
		this.isGoPage = true ;
		getChild(UIForumPageIterator.class).setSelectPage(this.page) ;
		if(!isBreadcumbs) {
			this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId)) ;
		}
	}
	
	private Forum getForum() throws Exception {
		if(this.isUpdate) {
			this.forum = forumService.getForum(ForumSessionUtils.getSystemProvider(), categoryId, forumId);
			this.isUpdate = false ;
		}
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		forumPortlet.findFirstComponentOfType(UIForumInfos.class).setModeratorsForum(this.forum.getModerators());
		return this.forum ;
	}
	
	@SuppressWarnings("unused")
	private void initPage() throws Exception {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
		this.pageList = forumService.getPageTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId);
		long maxTopic = userProfile.getMaxTopicInPage() ;
		if(maxTopic > 0) this.maxTopic = maxTopic ;
		this.pageList.setPageSize(this.maxTopic);
		this.getChild(UIForumPageIterator.class).updatePageList(this.pageList) ;
	}
	
	@SuppressWarnings("unused")
	private JCRPageList getPageTopics() throws Exception {
		return pageList ;
	}
	
	@SuppressWarnings("unused")
	private String[] getActionMenuForum() throws Exception {
		String []actions = {"EditForum", "SetLockedForum", "SetUnLockForum", "SetOpenForum", "SetCloseForum", "MoveForum", "RemoveForum"};
		return actions;
	}

	@SuppressWarnings("unused")
	private String[] getActionMenuTopic() throws Exception {
		String []actions = {"EditTopic", "SetOpenTopic", "SetCloseTopic", "SetLockedTopic", "SetUnLockTopic", "SetStickTopic", "SetUnStickTopic", "SetMoveTopic", "SetDeleteTopic", "MergeTopic"}; 
		return actions;
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private List<Topic> getTopicPageLits() throws Exception {
		if(!this.isGoPage) {
			this.page = this.getChild(UIForumPageIterator.class).getPageSelected() ;
		}
		this.topicList = this.pageList.getPage(this.page);
		for(Topic topic : this.topicList) {
			if(getUIFormCheckBoxInput(topic.getId()) != null) {
				getUIFormCheckBoxInput(topic.getId()).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(topic.getId(), topic.getId(), false) );
			}
		}
		this.isGoPage = false ;
		return this.topicList ;
	}
	
	private Topic getTopic(String topicId) throws Exception {
		List<Topic> listTopic = this.topicList ;
		for (Topic topic : listTopic) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return null ;
	}
	
	private JCRPageList getPageListPost(String topicId) throws Exception {
		JCRPageList pageListPost = this.forumService.getPosts(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, topicId)	; 
		long maxPost = getUserProfile().getMaxTopicInPage() ;
		if(maxPost > 0) this.maxPost = maxPost ;
		pageListPost.setPageSize(this.maxPost) ;
		return pageListPost;
	}
	
	
	@SuppressWarnings("unused")
	private String[] getStarNumber(Topic topic) throws Exception {
		double voteRating = topic.getVoteRating() ;
		return ForumFormatUtils.getStarNumber(voteRating) ;
	}
	
	@SuppressWarnings("unused")
	private String getStringCleanHtmlCode(String sms) {
		return ForumFormatUtils.getStringCleanHtmlCode(sms);
	}

	@SuppressWarnings("unused")
	private List<Tag> getTagsByTopic(String[] tagIds) throws Exception {
		return this.forumService.getTagsByTopic(ForumSessionUtils.getSystemProvider(), tagIds);	
	}
	
	static public class GoNumberPageActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer topicContainer = event.getSource() ;
			UIFormStringInput stringInput1 = topicContainer.getUIStringInput("gopage1") ;
			UIFormStringInput stringInput2 = topicContainer.getUIStringInput("gopage2") ;
			stringInput1.addValidator(PositiveNumberFormatValidator.class) ;
			stringInput2.addValidator(PositiveNumberFormatValidator.class) ;
			String numberPage1 = stringInput1.getValue() ;
			String numberPage2 = stringInput2.getValue() ;
			String numberPage = "" ;
			if(numberPage1 != null && numberPage1.length() > 0) {
				numberPage = numberPage1 ;
			} else numberPage = numberPage2 ;
			if(numberPage != null && numberPage.length() > 0) {
				boolean isNumber = true ;
				for (int i = 0; i < numberPage.length(); i++) {
	        char c = numberPage.charAt(i) ;
	        if(!Character.isDigit(c)) {
	        	isNumber = false ;
	        	break ;
	        }
        }
				if(isNumber) {
					Long page = Long.parseLong(numberPage);
					if(page == 0) {
						page = (long)1;
					} else if(page > topicContainer.pageList.getAvailablePage()){
						page = topicContainer.pageList.getAvailablePage() ;
					}
					topicContainer.page = page ;
					topicContainer.isGoPage = true ;
					topicContainer.getChild(UIForumPageIterator.class).setSelectPage(page) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer) ;
				}
			}
			stringInput1.setValue("") ;
			stringInput2.setValue("") ;
		}
	}
	
	static public class AddTopicActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource() ;
			UIForumPortlet forumPortlet =uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UITopicForm topicForm = popupContainer.addChild(UITopicForm.class, null, null) ;
			topicForm.setTopicIds(uiTopicContainer.categoryId, uiTopicContainer.forumId) ;
			popupContainer.setId("UIAddTopicContainer") ;
			popupAction.activate(popupContainer, 670, 440) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class OpenTopicsTagActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource() ;
			String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(3) ;
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
			Topic topic = uiTopicContainer.getTopic(temp[0]) ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			uiTopicDetail.setUpdateContainer(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, Long.parseLong(temp[1])) ;
			uiTopicDetail.setUpdatePageList(uiTopicContainer.getPageListPost(temp[0])) ;
			uiTopicDetail.setUpdateForum(uiTopicContainer.forum) ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(uiTopicContainer.categoryId, uiTopicContainer.forumId, topic ) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((uiTopicContainer.categoryId+"/"+ uiTopicContainer.forumId + " "));
			if(temp[2].equals("true")) {
				uiTopicDetail.setIdPostView("true") ;
			} else {
				uiTopicDetail.setIdPostView("false") ;
			}
			WebuiRequestContext context = event.getRequestContext() ;
			context.addUIComponentToUpdateByAjax(uiForumContainer) ;
			context.addUIComponentToUpdateByAjax(forumPortlet.getChild(UIBreadcumbs.class)) ;
		}
	}

	static public class EditForumActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			Forum forum = uiTopicContainer.getForum() ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIForumForm forumForm = popupAction.createUIComponent(UIForumForm.class, null, null) ;
			forumForm.setCategoryValue(uiTopicContainer.categoryId, false) ;
			forumForm.setForumValue(forum, true);
			forumForm.setForumUpdate(true);
			popupAction.activate(forumForm, 662, 466) ;
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
			uiTopicContainer.forumService.saveForum(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, forum, false) ;
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
			uiTopicContainer.forumService.saveForum(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, forum, false) ;
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
			uiTopicContainer.forumService.saveForum(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, forum, false) ;
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
			uiTopicContainer.forumService.saveForum(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, forum, false) ;
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	} 
	
	static public class DisplayOptionActionListener extends EventListener<UITopicContainer> {
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			event.getRequestContext().addUIComponentToUpdateByAjax(uiTopicContainer) ;
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
			forumPortlet.updateIsRendered(1) ;
			categoryContainer.updateIsRender(false) ;
			categoryContainer.getChild(UICategory.class).updateByBreadcumbs(uiTopicContainer.categoryId) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(uiTopicContainer.categoryId) ;
			forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	//----------------------------------MenuThread---------------------------------
	

	static public class EditTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			Topic topic = null ;
			boolean checked = false ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topic = uiTopicContainer.getTopic(child.getName());
						checked = true ;
						break ;
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(checked) {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UITopicForm topicForm = popupContainer.addChild(UITopicForm.class, null, null) ;
				topicForm.setTopicIds(uiTopicContainer.categoryId, uiTopicContainer.forumId) ;
				topicForm.setUpdateTopic(topic, true) ;
				popupContainer.setId("UIEditTopicContainer") ;
				popupAction.activate(popupContainer, 670, 440) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetOpenTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			List <Topic> topics = new ArrayList<Topic>();
			String sms = "";
			int i = 0 ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topics.add(uiTopicContainer.getTopic(child.getName()));
						if(!topics.get(i).getIsClosed()){ sms = topics.get(i).getTopicName() ; break ;} 
						++i ;
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0 && sms.length() == 0) {
				for(Topic topic : topics) {
					topic.setIsClosed(false) ;
					uiTopicContainer.forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
				}
			} 
			if(topics.size() == 0 && sms.length() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			if(sms.length() > 0){
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Open", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetCloseTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			List <Topic> topics = new ArrayList<Topic>();
			String sms = "";
			int i = 0 ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topics.add(uiTopicContainer.getTopic(child.getName()));
						if(topics.get(i).getIsClosed()){ sms = topics.get(i).getTopicName() ; break ;} 
						++i ;
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0 && sms.length() == 0) {
				for(Topic topic : topics) {
					topic.setIsClosed(true) ;
					uiTopicContainer.forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
				}
			} 
			if(topics.size() == 0 && sms.length() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			if(sms.length() > 0){
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Close", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetLockedTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			List <Topic> topics = new ArrayList<Topic>();
			String sms = "";
			int i = 0 ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topics.add(uiTopicContainer.getTopic(child.getName()));
						if(topics.get(i).getIsLock()){ sms = topics.get(i).getTopicName() ; break ;} 
						++i ;
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0 && sms.length() == 0) {
				for(Topic topic : topics) {
					topic.setIsLock(true) ;
					uiTopicContainer.forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
				}
			} 
			if(topics.size() == 0 && sms.length() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			if(sms.length() > 0){
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Locked", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class SetUnLockTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			List <Topic> topics = new ArrayList<Topic>();
			String sms = "";
			int i = 0 ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topics.add(uiTopicContainer.getTopic(child.getName()));
						if(!topics.get(i).getIsLock()){ sms = topics.get(i).getTopicName() ; break ;} 
						++i ;
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0 && sms.length() == 0) {
				for(Topic topic : topics) {
					topic.setIsLock(false) ;
					uiTopicContainer.forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
				}
			} 
			if(topics.size() == 0 && sms.length() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			if(sms.length() > 0){
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnLock", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class SetUnStickTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			List <Topic> topics = new ArrayList<Topic>();
			String sms = "";
			int i = 0 ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topics.add(uiTopicContainer.getTopic(child.getName()));
						if(!topics.get(i).getIsSticky()){ sms = topics.get(i).getTopicName() ; break ;} 
						++i ;
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0 && sms.length() == 0) {
				for(Topic topic : topics) {
					topic.setIsSticky(false) ;
					uiTopicContainer.forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
				}
			} 
			if(topics.size() == 0 && sms.length() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			if(sms.length() > 0){
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnStick", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetStickTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			List <Topic> topics = new ArrayList<Topic>();
			String sms = "";
			int i = 0 ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topics.add(uiTopicContainer.getTopic(child.getName()));
						if(topics.get(i).getIsSticky()){ sms = topics.get(i).getTopicName() ; break ;} 
						++i ;
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0 && sms.length() == 0) {
				for(Topic topic : topics) {
					topic.setIsSticky(true) ;
					uiTopicContainer.forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, uiTopicContainer.forumId, topic, false) ;
				}
			} 
			if(topics.size() == 0 && sms.length() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			if(sms.length() > 0){
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Stick", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetMoveTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			List <Topic> topics = new ArrayList<Topic>();
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topics.add(uiTopicContainer.getTopic(child.getName()));
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIMoveTopicForm moveTopicForm = popupAction.createUIComponent(UIMoveTopicForm.class, null, null) ;
				moveTopicForm.updateTopic(uiTopicContainer.forumId, topics, false);
				popupAction.activate(moveTopicForm, 400, 420) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} 
			if(topics.size() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	

	static public class MergeTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			List <Topic> topics = new ArrayList<Topic>();
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topics.add(uiTopicContainer.getTopic(child.getName()));
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIMergeTopicForm mergeTopicForm = popupAction.createUIComponent(UIMergeTopicForm.class, null, null) ;
				mergeTopicForm.updateTopics(topics) ;
				popupAction.activate(mergeTopicForm, 560, 260) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} 
			if(topics.size() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
	static public class SetDeleteTopicActionListener extends EventListener<UITopicContainer> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UITopicContainer> event) throws Exception {
			UITopicContainer uiTopicContainer = event.getSource();
			List<UIComponent> children = uiTopicContainer.getChildren() ;
			List <Topic> topics = new ArrayList<Topic>();
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topics.add(uiTopicContainer.getTopic(child.getName()));
					}
				}
			}
			UIForumPortlet forumPortlet = uiTopicContainer.getAncestorOfType(UIForumPortlet.class) ;
			if(topics.size() > 0) {
				for(Topic topic : topics) {
					uiTopicContainer.forumService.removeTopic(ForumSessionUtils.getSystemProvider(), uiTopicContainer.categoryId, uiTopicContainer.forumId, topic.getId()) ;
				}
			} 
			if(topics.size() == 0){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", args, ApplicationMessage.WARNING)) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}	
	
}
