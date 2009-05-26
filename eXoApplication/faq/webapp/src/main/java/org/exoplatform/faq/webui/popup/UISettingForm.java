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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.faq.service.Cate;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQServiceUtils;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UICategories;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.UIWatchContainer;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UISettingForm.gtmpl",
		events = {
				@EventConfig(listeners = UISettingForm.SaveActionListener.class),
				@EventConfig(listeners = UISettingForm.UserWatchManagerActionListener.class),
				@EventConfig(listeners = UISettingForm.ChildTabChangeActionListener.class),
				@EventConfig(listeners = UISettingForm.ResetMailContentActionListener.class),
				@EventConfig(listeners = UISettingForm.SelectCategoryForumActionListener.class),
				@EventConfig(listeners = UISettingForm.ChangeAvatarActionListener.class),
				@EventConfig(listeners = UISettingForm.SetDefaultAvatarActionListener.class),
				@EventConfig(listeners = UISettingForm.CancelActionListener.class)
		}
)
public class UISettingForm extends UIForm implements UIPopupComponent	{
	public final String DISPLAY_TAB = "DisplayTab";
	public final String SET_DEFAULT_EMAIL_TAB = "DefaultEmail";
	public final String SET_DEFAULT_ADDNEW_QUESTION_TAB = "AddNewQuestionTab";
	public final String SET_DEFAULT_EDIT_QUESTION_TAB = "EditQuestionTab";
	public final String CATEGORY_SCOPING = "CategoryScoping";
	public final String ITEM_VOTE = "vote";

	private final String DISPLAY_MODE = "display-mode".intern();
	public static final String ORDER_BY = "order-by".intern();
	public static final String ORDER_TYPE = "order-type".intern();
	private final String DISPLAY_APPROVED = "approved";
	private final String DISPLAY_BOTH = "both";
	private final String ENABLE_VOTE_COMMNET = "enableVotComment";
	public static final String ITEM_CREATE_DATE = "created".intern();
	public static final String ITEM_ALPHABET = "alphabet".intern();
	public static final String ASC = "asc".intern();
	public static final String DESC = "desc".intern();
	private final String ENABLE_RSS = "enableRSS";
	private final String ENABLE_VIEW_AVATAR = "enableViewAvatar";
	private static final String EMAIL_DEFAULT_ADD_QUESTION = "EmailAddNewQuestion";
	private static final String EMAIL_DEFAULT_EDIT_QUESTION = "EmailEditQuestion";
	private static final String DISCUSSION_TAB = "Discussion";
	private static final String FIELD_CATEGORY_PATH_INPUT = "CategoryPath";
	private static final String ENABLE_DISCUSSION = "EnableDiscuss";
	
	private FAQSetting faqSetting_ = new FAQSetting();
	private boolean isEditPortlet_ = false;
	private List<String> idForumName = new ArrayList<String>();
	private boolean isResetMail = false;
	private int indexOfTab = 0;
	private String avatarUrl ;
	private String tabSelected = DISPLAY_TAB;
	private List<Cate> listCate = new ArrayList<Cate>() ;
	
	public UISettingForm() throws Exception {
		isEditPortlet_ = false;
	}
	
	public void setIsEditPortlet(boolean isEditPortLet){
		this.isEditPortlet_ = isEditPortLet;
		if(isEditPortLet){
			FAQUtils.getPorletPreference(faqSetting_);
		}
	}
	
	public void setPathCatygory(List<String> idForumName) {
		this.idForumName =  idForumName;
		((UIFormInputWithActions)getChildById(DISCUSSION_TAB)).getUIStringInput(FIELD_CATEGORY_PATH_INPUT).setValue(idForumName.get(1));
	}
	
	private void setListCate() throws Exception {
		FAQService faqService = FAQUtils.getFAQService() ;
    List<Cate> listCate = new ArrayList<Cate>();
    Cate parentCate = null ;
    Cate childCate = null ;
    String userName = FAQUtils.getCurrentUser();
    List<String>userPrivates = null;
    if(userName != null){
    	userPrivates = FAQServiceUtils.getAllGroupAndMembershipOfUser(userName);
    }
    SessionProvider sessionProvider = FAQUtils.getSystemProvider();
    for(Category category : faqService.getSubCategories(null, sessionProvider, faqSetting_, true, userPrivates)) {
      if(category != null) {
        Cate cate = new Cate() ;
        cate.setCategory(category) ;
        cate.setDeft(0) ;
        listCate.add(cate) ;
      }
    }
    
    while (!listCate.isEmpty()) {
      parentCate = new Cate() ;
      parentCate = listCate.get(0);
      listCate.remove(0);
      this.listCate.add(parentCate) ;
      int i = 0;
      for(Category category : faqService.getSubCategories(parentCate.getCategory().getId(), sessionProvider, faqSetting_, true, userPrivates)){
        if(category != null) {
          childCate = new Cate() ;
          childCate.setCategory(category) ;
          childCate.setDeft(parentCate.getDeft() + 1) ;
          listCate.add(i ++, childCate) ;
        }
      }
    }
    sessionProvider.close();
  }
	
	private List<PageNavigation> getTreeNode(){
		List<PageNavigation> list = new ArrayList<PageNavigation>();
		list = Util.getUIPortal().getNavigations();
		List<PageNode> list2 ;
		for (PageNavigation pageNavigation : list) {
			list2 = pageNavigation.getNodes();
			for (PageNode pageNode : list2) {
	      System.out.println("\n\n Node Name: " + pageNode.getName());
      }
    }
		return list;
	}

	public void init() throws Exception {getTreeNode();
		if(isEditPortlet_){
			setListCate();
			
			UIFormInputWithActions DisplayTab = new UIFormInputWithActions(DISPLAY_TAB);
			UIFormInputWithActions EmailTab = new UIFormInputWithActions(SET_DEFAULT_EMAIL_TAB);
			UIFormInputWithActions EmailAddNewQuestion = new UIFormInputWithActions(SET_DEFAULT_ADDNEW_QUESTION_TAB);
			UIFormInputWithActions EmailEditQuestion = new UIFormInputWithActions(SET_DEFAULT_EDIT_QUESTION_TAB);
			UIFormInputWithActions Discussion = new UIFormInputWithActions(DISCUSSION_TAB);
			UIFormInputWithActions CategoryScoping = new UIFormInputWithActions(CATEGORY_SCOPING);
			
			List<SelectItemOption<String>> displayMode = new ArrayList<SelectItemOption<String>>();
			displayMode.add(new SelectItemOption<String>(DISPLAY_APPROVED, DISPLAY_APPROVED ));
			displayMode.add(new SelectItemOption<String>(DISPLAY_BOTH, DISPLAY_BOTH ));
			
			List<SelectItemOption<String>> orderBy = new ArrayList<SelectItemOption<String>>();
			orderBy.add(new SelectItemOption<String>(ITEM_CREATE_DATE, FAQSetting.DISPLAY_TYPE_POSTDATE ));
			orderBy.add(new SelectItemOption<String>(ITEM_ALPHABET + "/Index", FAQSetting.DISPLAY_TYPE_ALPHABET + "/Index" ));
			
			List<SelectItemOption<String>> orderType = new ArrayList<SelectItemOption<String>>();
			orderType.add(new SelectItemOption<String>(ASC, FAQSetting.ORDERBY_TYPE_ASC ));
			orderType.add(new SelectItemOption<String>(DESC, FAQSetting.ORDERBY_TYPE_DESC ));
			
			FAQUtils.getEmailSetting(faqSetting_, true, true);
			EmailAddNewQuestion.addUIFormInput((new UIFormWYSIWYGInput(EMAIL_DEFAULT_ADD_QUESTION, EMAIL_DEFAULT_ADD_QUESTION, ""))
																															.setValue(faqSetting_.getEmailSettingContent()));
			FAQUtils.getEmailSetting(faqSetting_, false, true);
			EmailEditQuestion.addUIFormInput((new UIFormWYSIWYGInput(EMAIL_DEFAULT_EDIT_QUESTION, EMAIL_DEFAULT_EDIT_QUESTION, ""))
																															.setValue(faqSetting_.getEmailSettingContent()));
			
			DisplayTab.addUIFormInput((new UIFormSelectBox(DISPLAY_MODE, DISPLAY_MODE, displayMode)).setValue(faqSetting_.getDisplayMode()));
			DisplayTab.addUIFormInput((new UIFormSelectBox(ORDER_BY, ORDER_BY, orderBy)).setValue(String.valueOf(faqSetting_.getOrderBy())));;
			DisplayTab.addUIFormInput((new UIFormSelectBox(ORDER_TYPE, ORDER_TYPE, orderType)).setValue(String.valueOf(faqSetting_.getOrderType())));
			DisplayTab.addUIFormInput((new UIFormCheckBoxInput<Boolean>(ENABLE_VOTE_COMMNET, ENABLE_VOTE_COMMNET, false)).
																																	setChecked(faqSetting_.isEnanbleVotesAndComments()));
			DisplayTab.addUIFormInput((new UIFormCheckBoxInput<Boolean>(ENABLE_RSS, ENABLE_RSS, false)).
																																	setChecked(faqSetting_.isEnableAutomaticRSS()));
			DisplayTab.addUIFormInput((new UIFormCheckBoxInput<Boolean>(ENABLE_VIEW_AVATAR, ENABLE_VIEW_AVATAR, false)).
																																	setChecked(faqSetting_.isEnableViewAvatar()));
			EmailTab.addChild(EmailAddNewQuestion);
			EmailTab.addChild(EmailEditQuestion);
			
			UIFormCheckBoxInput enableDiscus = new UIFormCheckBoxInput<Boolean>(ENABLE_DISCUSSION, ENABLE_DISCUSSION, false);
			enableDiscus.setChecked(faqSetting_.getIsDiscussForum());
			Discussion.addUIFormInput(enableDiscus);
			UIFormStringInput categoryPath = new UIFormStringInput(FIELD_CATEGORY_PATH_INPUT, FIELD_CATEGORY_PATH_INPUT, null) ;
			String pathCate = faqSetting_.getIdNameCategoryForum();
			idForumName.clear();
			if(pathCate.indexOf(";") > 0) {
				this.idForumName.add(pathCate.substring(0,pathCate.indexOf(";")));
				this.idForumName.add(pathCate.substring(pathCate.indexOf(";")+1));
			}else {
				this.idForumName.add("");
				this.idForumName.add("");
			}
			categoryPath.setValue(idForumName.get(1));
			categoryPath.setEditable(false);
			Discussion.addUIFormInput(categoryPath);
			List<ActionData> actionData = new ArrayList<ActionData>() ;
			ActionData ad ;
			ad = new ActionData() ;
			ad.setActionListener("SelectCategoryForum") ;
			ad.setActionName("SelectCategoryForum");
			ad.setActionType(ActionData.TYPE_ICON) ;
			ad.setCssIconClass("AddIcon16x16") ;
			actionData.add(ad) ;
			Discussion.setActionField(FIELD_CATEGORY_PATH_INPUT, actionData) ; 
			
			UIFormCheckBoxInput<Boolean> checkBoxInput = null;
			for(Cate cate : listCate){
				checkBoxInput = new UIFormCheckBoxInput<Boolean>(cate.getCategory().getId(), cate.getCategory().getId(), false);
				checkBoxInput.setChecked(cate.getCategory().isView());
				CategoryScoping.addChild(checkBoxInput);
			}
			
			this.addChild(DisplayTab);
			this.addChild(EmailTab);
			this.addChild(Discussion);
			this.addChild(CategoryScoping);
			
			DisplayTab.setRendered(true);
			EmailAddNewQuestion.setRendered(true);
			EmailEditQuestion.setRendered(true);
			EmailTab.setRendered(true);
		} else {
		
			List<SelectItemOption<String>> orderBy = new ArrayList<SelectItemOption<String>>();
			orderBy.add(new SelectItemOption<String>(ITEM_CREATE_DATE, FAQSetting.DISPLAY_TYPE_POSTDATE ));
			orderBy.add(new SelectItemOption<String>(ITEM_ALPHABET + "/Index", FAQSetting.DISPLAY_TYPE_ALPHABET + "/Index" ));
			addUIFormInput((new UIFormSelectBox(ORDER_BY, ORDER_BY, orderBy)).setValue(String.valueOf(faqSetting_.getOrderBy())));
			
			List<SelectItemOption<String>> orderType = new ArrayList<SelectItemOption<String>>();
			orderType.add(new SelectItemOption<String>(ASC, FAQSetting.ORDERBY_TYPE_ASC ));
			orderType.add(new SelectItemOption<String>(DESC, FAQSetting.ORDERBY_TYPE_DESC ));
			addUIFormInput((new UIFormSelectBox(ORDER_TYPE, ORDER_TYPE, orderType)).setValue(String.valueOf(faqSetting_.getOrderType())));
			
			addUIFormInput((new UIFormCheckBoxInput<Boolean>(ITEM_VOTE, ITEM_VOTE, false)).setChecked(faqSetting_.isSortQuestionByVote()));
			
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			avatarUrl = FAQUtils.getFileSource(((FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class))
																													.getUserAvatar(FAQUtils.getCurrentUser(), sessionProvider), 
																					getApplicationComponent(DownloadService.class)) ;
			if(avatarUrl == null || avatarUrl.trim().length() < 1)
				avatarUrl = Utils.DEFAULT_AVATAR_URL;
			sessionProvider.close();
		}
	}
	
	public void setAvatarUrl(String url){
		this.avatarUrl = url;
	}
	
	public FAQSetting getFaqSetting() {
  	return faqSetting_;
  }

	public void setFaqSetting(FAQSetting faqSetting) {
  	this.faqSetting_ = faqSetting;
  }
  
  public String[] getActions() { 
  	return new String[]{"Save", "Cancel"};
  }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  private String getSelectedTab(){
	  return tabSelected;
  }
	
	static public class SaveActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;			
			UIFAQPortlet uiPortlet = settingForm.getAncestorOfType(UIFAQPortlet.class);
			FAQSetting faqSetting = settingForm.faqSetting_ ;
			FAQService service = FAQUtils.getFAQService() ;
			SessionProvider sessionProvider = SessionProvider.createSystemProvider();
			if(settingForm.isEditPortlet_){
				
				UIFormInputWithActions inputWithActions = settingForm.getChildById(settingForm.CATEGORY_SCOPING);
				List<String> listCateIds = new ArrayList<String>();
				UIFormCheckBoxInput<Boolean> checkBoxInput = null;
				int position = 1;
				boolean isView = true;
				for(int i = 0; i < settingForm.listCate.size(); i ++){
					checkBoxInput = inputWithActions.getChildById(settingForm.listCate.get(i).getCategory().getId());
					if(settingForm.listCate.get(i).getDeft() <= position || (!checkBoxInput.isChecked() && isView)){
						isView = settingForm.listCate.get(i).getCategory().isView();
						position = settingForm.listCate.get(i).getDeft();
					}
					if((settingForm.listCate.get(i).getDeft() > position) && !isView)checkBoxInput.setChecked(false);
					if((checkBoxInput.isChecked() && !settingForm.listCate.get(i).getCategory().isView()) || 
							(!checkBoxInput.isChecked() && settingForm.listCate.get(i).getCategory().isView())){
						listCateIds.add(settingForm.listCate.get(i).getCategory().getId());
						settingForm.listCate.get(i).getCategory().setView(checkBoxInput.isChecked());
					}
				}
				if(listCateIds != null && listCateIds.size() > 0)service.changeStatusCategoryView(listCateIds, sessionProvider);
				
				inputWithActions = settingForm.getChildById(settingForm.DISPLAY_TAB);
				faqSetting.setDisplayMode(((UIFormSelectBox)inputWithActions.getChildById(settingForm.DISPLAY_MODE)).getValue());
				faqSetting.setOrderBy(String.valueOf(((UIFormSelectBox)inputWithActions.getChildById(ORDER_BY)).getValue())) ;
				faqSetting.setOrderType(String.valueOf(((UIFormSelectBox)inputWithActions.getChildById(ORDER_TYPE)).getValue())) ;
				faqSetting.setEnanbleVotesAndComments(((UIFormCheckBoxInput<Boolean>)inputWithActions.
																								getChildById(settingForm.ENABLE_VOTE_COMMNET)).isChecked());
				faqSetting.setEnableAutomaticRSS(((UIFormCheckBoxInput<Boolean>)inputWithActions.
																								getChildById(settingForm.ENABLE_RSS)).isChecked());
				faqSetting.setEnableViewAvatar(((UIFormCheckBoxInput<Boolean>)inputWithActions.
																								getChildById(settingForm.ENABLE_VIEW_AVATAR)).isChecked());
				
				UIFormInputWithActions emailTab = settingForm.getChildById(settingForm.SET_DEFAULT_EMAIL_TAB);
				String defaultAddnewQuestion = ((UIFormWYSIWYGInput)((UIFormInputWithActions)emailTab.getChildById(settingForm.SET_DEFAULT_ADDNEW_QUESTION_TAB))
																					.getChildById(EMAIL_DEFAULT_ADD_QUESTION)).getValue();
				String defaultEditQuestion = ((UIFormWYSIWYGInput)((UIFormInputWithActions)emailTab.getChildById(settingForm.SET_DEFAULT_EDIT_QUESTION_TAB))
																					.getChildById(EMAIL_DEFAULT_EDIT_QUESTION)).getValue();
				
				ValidatorDataInput validatorDataInput = new ValidatorDataInput();
				if(defaultAddnewQuestion == null || !validatorDataInput.fckContentIsNotEmpty(defaultAddnewQuestion)) defaultAddnewQuestion = " ";
				if(defaultEditQuestion == null || !validatorDataInput.fckContentIsNotEmpty(defaultEditQuestion)) defaultEditQuestion = " ";
				UIApplication uiApplication = settingForm.getAncestorOfType(UIApplication.class) ;
				UIFormInputWithActions Discussion = settingForm.getChildById(DISCUSSION_TAB);
				boolean isDiscus = (Boolean)Discussion.getUIFormCheckBoxInput(ENABLE_DISCUSSION).getValue();
				if(isDiscus) {
					if(!settingForm.idForumName.isEmpty()) {
						faqSetting.setIdNameCategoryForum(settingForm.idForumName.get(0)+";"+settingForm.idForumName.get(1));
					}else {
						 uiApplication.addMessage(new ApplicationMessage("UISettingForm.msg.pathCategory-empty", null, ApplicationMessage.WARNING)) ;
			       event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			       return ;
					}
				}else{
					faqSetting.setIdNameCategoryForum("");
				}
				faqSetting.setIsDiscussForum(isDiscus);
				settingForm.idForumName.clear();
				FAQUtils.savePortletPreference(faqSetting, defaultAddnewQuestion.replaceAll("&amp;", "&"), defaultEditQuestion.replaceAll("&amp;", "&"));
        uiApplication.addMessage(new ApplicationMessage("UISettingForm.msg.update-successful", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			} else {
				faqSetting.setOrderBy(String.valueOf(settingForm.getUIFormSelectBox(ORDER_BY).getValue())) ;
				faqSetting.setOrderType(String.valueOf(settingForm.getUIFormSelectBox(ORDER_TYPE).getValue())) ;
				faqSetting.setSortQuestionByVote(settingForm.getUIFormCheckBoxInput(settingForm.ITEM_VOTE).isChecked());
				service.saveFAQSetting(faqSetting,FAQUtils.getCurrentUser(), sessionProvider) ;
				UIPopupAction uiPopupAction = settingForm.getAncestorOfType(UIPopupAction.class) ;
				uiPopupAction.deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
				UIQuestions questions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
				UICategories categories = uiPortlet.findFirstComponentOfType(UICategories.class);
				categories.resetListCate(sessionProvider);
				questions.setFAQSetting(faqSetting);
				questions.setListObject() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
			}
			sessionProvider.close();
			return ;
		}
	}
	
	static public class UserWatchManagerActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;
			UIFAQPortlet uiPortlet = settingForm.getAncestorOfType(UIFAQPortlet.class);
			UIWatchContainer watchContainer = settingForm.getParent() ;
			UIPopupAction popupAction = watchContainer.getChild(UIPopupAction.class) ;
			UIUserWatchManager watchForm = popupAction.activate(UIUserWatchManager.class, 600) ;
			watchForm.setFAQSetting(settingForm.faqSetting_);
		  event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class ChangeAvatarActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;
			UIFAQPortlet uiPortlet = settingForm.getAncestorOfType(UIFAQPortlet.class);
			UIWatchContainer watchContainer = settingForm.getParent() ;
			UIPopupAction popupAction = watchContainer.getChild(UIPopupAction.class) ;
			UIAttachMentForm attachMentForm = popupAction.activate(UIAttachMentForm.class, 550) ;
			attachMentForm.setNumberUpload(1);
			attachMentForm.setIsChangeAvatar(true);
		  event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class SetDefaultAvatarActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;
			UIFAQPortlet uiPortlet = settingForm.getAncestorOfType(UIFAQPortlet.class);
			FAQService service = FAQUtils.getFAQService() ;
			SessionProvider sessionProvider = FAQUtils.getSystemProvider();
			service.setDefaultAvatar(FAQUtils.getCurrentUser(), sessionProvider);
			sessionProvider.close();
			settingForm.setAvatarUrl(Utils.DEFAULT_AVATAR_URL);
			event.getRequestContext().addUIComponentToUpdateByAjax(settingForm.getParent()) ;
		}
	}
	
	static public class ResetMailContentActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;
			String id = event.getRequestContext().getRequestParameter(OBJECTID);
			PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
			PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
			String emailContent = "";
			WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
			ResourceBundle res = context.getApplicationResourceBundle() ;
			UIFormInputWithActions formInputWithActions = settingForm.getChildById(settingForm.SET_DEFAULT_EMAIL_TAB);
			UIFormWYSIWYGInput input = null;
			if(id.equals("0")){
				emailContent =  res.getString("SendEmail.AddNewQuestion.Default");
				input = (UIFormWYSIWYGInput)((UIFormInputWithActions)
											formInputWithActions.getChildById(settingForm.SET_DEFAULT_ADDNEW_QUESTION_TAB))
											.getChildById(EMAIL_DEFAULT_ADD_QUESTION);
				input.setValue(emailContent);
			} else {
				emailContent =  res.getString("SendEmail.EditOrResponseQuestion.Default");
				input = (UIFormWYSIWYGInput)((UIFormInputWithActions)
											formInputWithActions.getChildById(settingForm.SET_DEFAULT_EDIT_QUESTION_TAB))
											.getChildById(EMAIL_DEFAULT_EDIT_QUESTION);
				input.setValue(emailContent);
			}
			
			settingForm.isResetMail = true;
			settingForm.indexOfTab = Integer.parseInt(id);
			
			event.getRequestContext().addUIComponentToUpdateByAjax(settingForm) ;
		}
	}
	
	
	static public class ChildTabChangeActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;		
			String[] tabId = event.getRequestContext().getRequestParameter(OBJECTID).split("/");
			String tab = tabId[0];
			int id = Integer.parseInt(tabId[1]);
			if(tab.equals("parent")){
				settingForm.isResetMail = false;
				if(id == 0) settingForm.tabSelected = settingForm.DISPLAY_TAB;
				else if(id == 2)  settingForm.tabSelected = DISCUSSION_TAB;
				else if(id == 3) settingForm.tabSelected = settingForm.CATEGORY_SCOPING;
				else settingForm.tabSelected = settingForm.SET_DEFAULT_EMAIL_TAB;
			} else {
				settingForm.indexOfTab = id;
				settingForm.isResetMail = true;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(settingForm.getParent()) ;
		}
	}
	
	static public class SelectCategoryForumActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;		
			UIFAQPortlet uiPortlet = settingForm.getAncestorOfType(UIFAQPortlet.class);
			try {
				UIWatchContainer pupupContainer = settingForm.getParent() ;
				UIPopupAction popupAction = pupupContainer.getChild(UIPopupAction.class) ;
				UISelectCategoryForumForm listCateForm = popupAction.activate(UISelectCategoryForumForm.class, 400) ;
				listCateForm.setListCategory();
				event.getRequestContext().addUIComponentToUpdateByAjax(pupupContainer) ;
      } catch (ClassCastException e) {
      	UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class) ; 
      	UISelectCategoryForumForm listCateForm = popupAction.createUIComponent(UISelectCategoryForumForm.class, null, null) ;
      	listCateForm.setListCategory();
      	popupAction.activate(listCateForm, 400, 400);
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch (Exception e) {
      	e.printStackTrace();
			}
		}
	}

	static public class CancelActionListener extends EventListener<UISettingForm> {
		public void execute(Event<UISettingForm> event) throws Exception {
			UISettingForm settingForm = event.getSource() ;		
			UIFAQPortlet uiPortlet = settingForm.getAncestorOfType(UIFAQPortlet.class);
			UIQuestions uiQuestions = uiPortlet.findFirstComponentOfType(UIQuestions.class) ;
			uiQuestions.setIsNotChangeLanguage();
			UIPopupAction uiPopupAction = settingForm.getAncestorOfType(UIPopupAction.class) ;
			uiPopupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiQuestions) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFAQContainer.class)) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
}