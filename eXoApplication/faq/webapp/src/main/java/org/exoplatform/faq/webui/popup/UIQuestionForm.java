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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class ,
    template =  "app:/templates/faq/webui/popup/UIQuestionForm.gtmpl",
    events = {
    	@EventConfig(listeners = UIQuestionForm.SelectLanguageActionListener.class),
    	@EventConfig(listeners = UIQuestionForm.DeleteLanguageActionListener.class),
      @EventConfig(listeners = UIQuestionForm.AttachmentActionListener.class),
      @EventConfig(listeners = UIQuestionForm.SaveActionListener.class),
      @EventConfig(listeners = UIQuestionForm.CancelActionListener.class),
      @EventConfig(listeners = UIQuestionForm.RemoveAttachmentActionListener.class)
    }
)
public class UIQuestionForm extends UIForm implements UIPopupComponent  {
  public static final String AUTHOR = "Author" ;
  public static final String EMAIL_ADDRESS = "EmailAddress" ;
  public static final String QUESTION_CONTENT = "QuestionTitle";
  public static final String ALL_LANGUAGES = "AllLanguages";
  public static final String QUESTION_DETAIL = "Question" ;
  public static final String ATTACHMENTS = "Attachment" ;
  public static final String FILE_ATTACHMENTS = "FileAttach" ;
  public static final String REMOVE_FILE_ATTACH = "RemoveFile" ;
  public static final String IS_APPROVED = "IsApproved" ;
  public static final String IS_ACTIVATED = "IsActivated" ;
  
  private UIFormStringInput inputAuthor = null ;
  private UIFormStringInput inputEmailAddress = null ;
  private UIFormStringInput inputQuestionContent = null ;
  private UIFormWYSIWYGInput inputQuestionDetail = null ;
  private UIFormSelectBox selectLanguage = null;
  private UIFormCheckBoxInput<Boolean> inputIsApproved = null ;
  private UIFormCheckBoxInput<Boolean> inputIsActivated = null ;
  private UIFormInputWithActions inputAttachcment = null ;
  
  private static FAQService fAQService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  private static Question question_ = null ;
  
  private Map<String, List<ActionData>> actionField_ ;
  
  private List<SelectItemOption<String>> listSystemLanguages = new ArrayList<SelectItemOption<String>>() ;
  private Map<String, String> listQuestionDetail = new HashMap<String, String>() ;
  private Map<String, String> listQuestionContent = new HashMap<String, String>() ;
  private List<String> listLanguages = new ArrayList<String>() ;
  private List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  private Map<String, QuestionLanguage> mapLanguageNode_ = new HashMap<String, QuestionLanguage>() ;
  private String categoryId_ = null ;
  private String questionId_ = null ;
  private String defaultLanguage_ = "" ;
  private String lastLanguage_ = "";
  private String link_ = "";
  private String author_ = "" ;
  private String email_ = "" ;
  private List<String> questionContents_ = new ArrayList<String>() ;
  private boolean isApproved_ = true ;
  private boolean isActivated_ = true ;
  
  private boolean isChildOfManager = false ;
  private FAQSetting faqSetting_ ;
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public String getLink() {return link_;}
	public void setLink(String link) { this.link_ = link;}
	public void setFAQSetting(FAQSetting faqSetting) {this.faqSetting_ = faqSetting;}
  @SuppressWarnings("static-access")
  public UIQuestionForm() throws Exception {
  	listQuestionDetail.clear();
  	listQuestionContent.clear();
    isChildOfManager = false ;
    listLanguages.clear() ;
    listFileAttach_.clear() ;
    mapLanguageNode_.clear() ;
    questionContents_.clear() ;
    
    listFileAttach_ = new ArrayList<FileAttachment>() ;
    actionField_ = new HashMap<String, List<ActionData>>() ;
    questionId_ = new String() ;
    question_ = null ;
    
    this.setActions(new String[]{"Save", "Cancel"});
  }
  
	public void refresh() throws Exception {    
		listFileAttach_.clear() ;
	}
	
	private void setListSystemLanguages() throws Exception{
		listSystemLanguages.clear();
		LocaleConfigService configService = getApplicationComponent(LocaleConfigService.class) ;
    for(Object object:configService.getLocalConfigs()) {      
      LocaleConfig localeConfig = (LocaleConfig)object ;
      Locale locale = localeConfig.getLocale() ;
      String displayName = locale.getDisplayLanguage() ;
      if(displayName.equals(defaultLanguage_))
      	listSystemLanguages.add(new SelectItemOption<String>(displayName + " ( default) ", displayName)) ;
      else
      	listSystemLanguages.add(new SelectItemOption<String>(displayName, displayName)) ;
    }
	}

  public void initPage(boolean isEdit) {
  	try {
	    setListSystemLanguages();
    } catch (Exception e1) {
	    e1.printStackTrace();
    }
    inputAuthor = new UIFormStringInput(AUTHOR, AUTHOR, author_) ;
    if(author_.trim().length() > 0) inputAuthor.setEditable(false);
    inputEmailAddress = new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, email_) ;
    inputQuestionContent = new UIFormStringInput(QUESTION_CONTENT, QUESTION_CONTENT, null);
    selectLanguage = new UIFormSelectBox(ALL_LANGUAGES, ALL_LANGUAGES, listSystemLanguages);
    if(defaultLanguage_ != null || defaultLanguage_.trim().length() > 0) selectLanguage.setSelectedValues(new String[]{defaultLanguage_});
    selectLanguage.setOnChange("SelectLanguage");
    inputIsApproved = (new UIFormCheckBoxInput<Boolean>(IS_APPROVED, IS_APPROVED, false)) ;
    inputIsActivated = (new UIFormCheckBoxInput<Boolean>(IS_ACTIVATED, IS_ACTIVATED, false)) ;
    inputAttachcment = new UIFormInputWithActions(ATTACHMENTS) ;
    inputAttachcment.addUIFormInput( new UIFormInputInfo(FILE_ATTACHMENTS, FILE_ATTACHMENTS, null) ) ;
    try{
      inputAttachcment.setActionField(FILE_ATTACHMENTS, getActionList()) ;
    } catch (Exception e) {
      e.printStackTrace() ;
    }
    
    inputQuestionDetail = new UIFormWYSIWYGInput(QUESTION_DETAIL, null, null, true) ;
    //inputQuestionDetail.setColumns(80) ;
    if(!questionContents_.isEmpty()){
      String input = questionContents_.get(0) ;
      if(input!= null && input.indexOf("<p>") >=0 && input.indexOf("</p>") >= 0) {
        input = input.replace("<p>", "") ;
        input = input.substring(0, input.lastIndexOf("</p>") - 1) ;
      }
      inputQuestionDetail.setValue(input) ;
    }
    addChild(inputQuestionContent);
    addChild(inputQuestionDetail);
    addChild(selectLanguage);
    addChild(inputAuthor) ;
    addChild(inputEmailAddress) ;
    if(questionId_ != null && questionId_.trim().length() > 0) {
      addChild(inputIsApproved.setChecked(isApproved_)) ;
      addChild(inputIsActivated.setChecked(isActivated_)) ;
    }
    addUIFormInput(inputAttachcment) ;
    if(question_ != null && !isEdit) {
      this.setListFileAttach(question_.getAttachMent()) ;
      try {
        refreshUploadFileList() ;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  public void setIsChildOfManager(boolean isChild) {
    isChildOfManager = isChild ;
    this.removeChildById(AUTHOR);
    this.removeChildById(EMAIL_ADDRESS);
    this.removeChildById(QUESTION_CONTENT);
    this.removeChildById(QUESTION_DETAIL);
    this.removeChildById(ATTACHMENTS);
    this.removeChildById(IS_APPROVED) ;
    this.removeChildById(IS_ACTIVATED) ;
    listFileAttach_.clear() ;
    mapLanguageNode_.clear() ;
  }
  
  @SuppressWarnings("static-access")
  public void setQuestionId(Question question){
  	List<QuestionLanguage> questionLanguages = new ArrayList<QuestionLanguage>();
    questionId_ = question.getId() ;
    categoryId_ = null ;
    SessionProvider sessionProvider = FAQUtils.getSystemProvider();
    try {
      question_ = question ;
      defaultLanguage_ = question_.getLanguage() ;
      lastLanguage_ = defaultLanguage_;
      listLanguages.clear() ;
      listLanguages.add(defaultLanguage_) ;
      listQuestionDetail.put(question_.getLanguage(), question_.getDetail());
      listQuestionContent.put(question_.getLanguage(), question_.getQuestion());
      questionLanguages = fAQService_.getQuestionLanguages(questionId_, sessionProvider) ;
      for(QuestionLanguage questionLanguage : questionLanguages) {
      	mapLanguageNode_.put(questionLanguage.getLanguage(), questionLanguage);
      	listQuestionDetail.put(questionLanguage.getLanguage(), questionLanguage.getDetail());
      	listQuestionContent.put(questionLanguage.getLanguage(), questionLanguage.getQuestion());
        listLanguages.add(questionLanguage.getLanguage());
      }
      isApproved_ = question_.isApproved() ;
      isActivated_ = question_.isActivated() ;
      initPage(false) ;
      UIFormStringInput authorQ = this.getChildById(AUTHOR) ;
      authorQ.setValue(question_.getAuthor()) ;
      UIFormStringInput emailQ = this.getChildById(EMAIL_ADDRESS);
      emailQ.setValue(question_.getEmail()) ;
      inputQuestionContent.setValue(question.getQuestion());
      inputQuestionDetail.setValue(question_.getDetail()) ;
      /*int i = 1 ;
      for(QuestionLanguage questionLanguage : questionLanguages) {
        ((UIFormTextAreaInput)listFormWYSIWYGInput.getChild(i++)).setValue(questionLanguage.getQuestion()) ;
      }*/
    } catch (Exception e) {
      e.printStackTrace();
      initPage(false) ;
    } finally {
    	sessionProvider.close();
    }
  }
  
  public String getQuestionId() {
    return questionId_ ;
  }
  
  public void setDefaultLanguage(String defaultLanguage) {
    this.defaultLanguage_ = defaultLanguage ;
  }
  
  public String getDefaultLanguage() {
    return this.defaultLanguage_;
  }
  
  @SuppressWarnings("unused")
  private String getAuthor() {return this.author_ ; }
  public void setAuthor(String author) {this.author_ = author ;}
  
  
  @SuppressWarnings("unused")
  private String getEmail() {return this.email_ ; }
  public void setEmail(String email) {this.email_ = email ;}
  
  private String getCategoryId(){
    return this.categoryId_ ; 
  }
  @SuppressWarnings("static-access")
  public void setCategoryId(String categoryId) {
    this.categoryId_ = categoryId ;
    questionId_ = null ;
    LocaleConfigService configService = getApplicationComponent(LocaleConfigService.class) ;
    for(Object object:configService.getLocalConfigs()) {      
      LocaleConfig localeConfig = (LocaleConfig)object ;
      Locale locale = localeConfig.getLocale() ;
      defaultLanguage_ = locale.getDefault().getDisplayLanguage() ;
    }
    if(!listLanguages.isEmpty())
      listLanguages.clear() ;
    listLanguages.add(defaultLanguage_) ;
    lastLanguage_ = defaultLanguage_;
    initPage(false) ;
  }
  
  protected UIForm getParentForm() {
    return (UIForm)this.getParent();
  }
  
  public List<ActionData> getActionList() { 
    List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
    for(FileAttachment attachdata : listFileAttach_) {
      ActionData uploadAction = new ActionData() ;
      uploadAction.setActionListener("Download") ;
      uploadAction.setActionParameter(attachdata.getPath());
      uploadAction.setActionType(ActionData.TYPE_ICON) ;
      uploadAction.setCssIconClass("AttachmentIcon") ; // "AttachmentIcon ZipFileIcon"
      uploadAction.setActionName(attachdata.getName() + " ("+attachdata.getSize()+" B)" ) ;
      uploadAction.setShowLabel(true) ;
      uploadedFiles.add(uploadAction) ;
      ActionData removeAction = new ActionData() ;
      removeAction.setActionListener("RemoveAttachment") ;
      removeAction.setActionName(REMOVE_FILE_ATTACH);
      removeAction.setActionParameter(attachdata.getPath());
      removeAction.setCssIconClass("LabelLink");
      removeAction.setActionType(ActionData.TYPE_LINK) ;
      uploadedFiles.add(removeAction) ;
    }
    return uploadedFiles ;
  }
  
  public void setListFileAttach(List<FileAttachment> listFileAttachment){
    listFileAttach_.addAll(listFileAttachment) ;
  }
  
  public void setListFileAttach(FileAttachment fileAttachment){
    listFileAttach_.add(fileAttachment) ;
  }
  
  @SuppressWarnings("unused")
  private List<FileAttachment> getListFile() {
    return listFileAttach_ ;
  }
  
  public void refreshUploadFileList() throws Exception {
    ((UIFormInputWithActions)this.getChildById(ATTACHMENTS)).setActionField(FILE_ATTACHMENTS, getActionList()) ;
  }
  
  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
  
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  
  public String[] getListLanguage(){return listLanguages.toArray(new String[]{}) ; }
  
  public void setListLanguage(List<String> listLanguage) {
    if(questionContents_.size() == 1) {
      listLanguages.clear() ;
      listLanguages.addAll(listLanguage) ;
    } else {
      int i = 0 ;
      while ( i < questionContents_.size()) {
        if(!listLanguage.contains(listLanguages.get(i))) {
          listLanguages.remove(i) ;
          questionContents_.remove(i) ;
        } else {
          i ++ ;
        }
      }
      for(String language : listLanguage) {
        if(!listLanguages.contains(language)) {
          listLanguages.add(language) ;
        }
      }
    }
  }
  
  static public class SelectLanguageActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource() ;
      String language = questionForm.selectLanguage.getValue() ;
      String detail = questionForm.inputQuestionDetail.getValue();
      String question = questionForm.inputQuestionContent.getValue();
      ValidatorDataInput validatorDataInput = new ValidatorDataInput();
      if(!validatorDataInput.fckContentIsNotEmpty(detail)) detail = " ";
      if(validatorDataInput.fckContentIsNotEmpty(question)){
      	if(questionForm.listQuestionDetail.containsKey(questionForm.lastLanguage_)) {
      		questionForm.listQuestionDetail.remove(questionForm.lastLanguage_);
      		questionForm.listQuestionContent.remove(questionForm.lastLanguage_);
      	}
      	questionForm.listQuestionDetail.put(questionForm.lastLanguage_, detail);
      	questionForm.listQuestionContent.put(questionForm.lastLanguage_, question);
      	if(!questionForm.listLanguages.contains(language)) questionForm.listLanguages.add(language);
      } else {
      	if(questionForm.listLanguages.contains(questionForm.lastLanguage_)){
      		questionForm.listLanguages.remove(questionForm.lastLanguage_);
      		questionForm.listQuestionDetail.remove(questionForm.lastLanguage_);
      		questionForm.listQuestionContent.remove(questionForm.lastLanguage_);
      	}
      }
      questionForm.lastLanguage_ = language;
      if(questionForm.listQuestionContent.containsKey(language)){
      	questionForm.inputQuestionDetail.setValue(questionForm.listQuestionDetail.get(language));
      	questionForm.inputQuestionContent.setValue(questionForm.listQuestionContent.get(language));
      } else {
      	questionForm.inputQuestionDetail.setValue("");
      	questionForm.inputQuestionContent.setValue("");
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
    }
  }
  
  static public class DeleteLanguageActionListener extends EventListener<UIQuestionForm> {
  	public void execute(Event<UIQuestionForm> event) throws Exception {
  		UIQuestionForm questionForm = event.getSource() ;
  		questionForm.inputQuestionDetail.setValue("");
  		questionForm.inputQuestionContent.setValue("");
  		event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
  	}
  }
  
  static public class SaveActionListener extends EventListener<UIQuestionForm> {
    @SuppressWarnings({ "static-access", "unchecked" })
    public void execute(Event<UIQuestionForm> event) throws Exception {
      Node questionNode = null ;
      boolean questionIsApproved = true ;
      UIQuestionForm questionForm = event.getSource() ;     
      DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy") ;
      java.util.Date date = new java.util.Date();
      String dateStr = dateFormat.format(date) ;
      date = dateFormat.parse(dateStr) ;
      String author = questionForm.inputAuthor.getValue() ;
      String emailAddress = questionForm.inputEmailAddress.getValue() ;
      String questionContent = questionForm.inputQuestionContent.getValue();
      if(author == null || author.trim().length() < 1) {
      	UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
      	uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.author-is-null", null, ApplicationMessage.WARNING)) ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      	return ;
      }
      if(emailAddress == null || emailAddress.trim().length() < 1 || !FAQUtils.isValidEmailAddresses(emailAddress)) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.email-address-invalid", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      }
      
      if((questionContent == null || questionContent.trim().length() < 1) && 
      		!questionForm.defaultLanguage_.equals(questionForm.lastLanguage_)) {
      	UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
      	uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      	return ;
      }
      
      MultiLanguages multiLanguages = new MultiLanguages() ;
      
      String questionDetail = questionForm.inputQuestionDetail.getValue();
      ValidatorDataInput validatorDataInput = new ValidatorDataInput();
      if(!validatorDataInput.fckContentIsNotEmpty(questionDetail)) questionDetail = " ";
      if(validatorDataInput.fckContentIsNotEmpty(questionContent)){
      	questionForm.listQuestionDetail.put(questionForm.lastLanguage_, questionDetail);
      	questionForm.listQuestionContent.put(questionForm.lastLanguage_, questionContent);
      	if(!questionForm.listLanguages.contains(questionForm.lastLanguage_)) questionForm.listLanguages.add(questionForm.lastLanguage_);
      } else {
      	if(questionForm.listLanguages.contains(questionForm.lastLanguage_)){
      		questionForm.listLanguages.remove(questionForm.lastLanguage_);
      		questionForm.listQuestionDetail.remove(questionForm.lastLanguage_);
      		questionForm.listQuestionContent.remove(questionForm.lastLanguage_);
      	}
      }
            
      if(questionForm.listQuestionContent.isEmpty()) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      } else {
        for(String input : questionForm.listQuestionContent.values()) {
          if(input == null || input.trim().length() < 1) {
            UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
            uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.question-null", null, ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
            return ;
          }
        }
      }
      SessionProvider sessionProvider = FAQUtils.getSystemProvider();
      if(questionForm.questionId_ == null || questionForm.questionId_.trim().length() < 1) {
        question_ = new Question() ;
        question_.setCategoryId(questionForm.getCategoryId()) ;
        question_.setRelations(new String[]{}) ;
        if(questionForm.categoryId_ != null){
	        try{
	          questionIsApproved = !fAQService_.getCategoryById(questionForm.categoryId_, sessionProvider).isModerateQuestions() ;
	        } catch(Exception exception){
	          UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
	          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-is-deleted", null, ApplicationMessage.WARNING)) ;
	          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	          
	          UIFAQPortlet portlet = questionForm.getAncestorOfType(UIFAQPortlet.class) ;
	          UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
	          popupAction.deActivate() ;
	          event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
	          return;
	        }
        } else {
        	questionIsApproved = true;
        }
        question_.setCreatedDate(date) ;
        question_.setApproved(questionIsApproved) ;
      } else {
        question_.setApproved(((UIFormCheckBoxInput<Boolean>)questionForm.getChildById(IS_APPROVED)).isChecked()) ;
        question_.setActivated(((UIFormCheckBoxInput<Boolean>)questionForm.getChildById(IS_ACTIVATED)).isChecked()) ;
      }
      question_.setLanguage(questionForm.getDefaultLanguage()) ;
      question_.setAuthor(author) ;
      question_.setEmail(emailAddress) ;
      question_.setQuestion(questionForm.inputQuestionContent.getValue());
      try{
      	question_.setDetail(questionForm.listQuestionDetail.get(questionForm.defaultLanguage_)) ;
      	question_.setQuestion(questionForm.listQuestionContent.get(questionForm.defaultLanguage_).replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
      } catch(Exception e){
      	UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.default-question-null", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        return ;
      }
      question_.setAttachMent(questionForm.listFileAttach_) ;
      
      UIFAQPortlet portlet = questionForm.getAncestorOfType(UIFAQPortlet.class) ;
      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
      //link
      String link = questionForm.getLink().replaceFirst("UIQuestionForm", "UIQuestions").replaceFirst("Attachment", "ViewQuestion").replaceAll("&amp;", "&");
      String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
      String portalName = "/" + Util.getUIPortal().getName() ;
      if(link.indexOf(portalName) > 0) {
		    if(link.indexOf(portalName + "/" + selectedNode) < 0){
		      link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
		    }									
			}	
			PortalRequestContext portalContext = Util.getPortalRequestContext();
			String url = portalContext.getRequest().getRequestURL().toString();
			url = url.replaceFirst("http://", "") ;
			url = url.substring(0, url.indexOf("/")) ;
			url = "http://" + url;
			String path = "" ;
			if(FAQUtils.isFieldEmpty(questionForm.questionId_)) path = questions.getPathService(questionForm.getCategoryId())+"/"+questionForm.getCategoryId() ;
			else path = questions.getPathService(question_.getCategoryId())+"/"+question_.getCategoryId() ;
			link = link.replaceFirst("OBJECTID", path);
			link = url + link;
      question_.setLink(link) ;
      
      try{
        FAQUtils utils = new FAQUtils();
        boolean isNew = true;
        if(questionForm.questionId_ != null && questionForm.questionId_.trim().length() > 0) isNew = false;
        utils.getEmailSetting(questionForm.faqSetting_, isNew, false);
        if(!isNew) {
        	FAQSetting faqSetting = new FAQSetting();
 					FAQUtils.getPorletPreference(faqSetting);
 					if(faqSetting.getIsDiscussForum()) {
 						String pathTopic = question_.getPathTopicDiscuss();
 						if(pathTopic != null && pathTopic.length() > 0) {
 							try {
 								ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
 								String []ids = pathTopic.split("/");
 								Topic topic = forumService.getTopic(sessionProvider, ids[0], ids[1], ids[2], "");
 								if(topic == null){
 									System.out.println("\n\n ==========> Topic is removed or deleted");
 								} else {
 									topic.setModifiedBy(FAQUtils.getCurrentUser());
 									topic.setTopicName(question_.getQuestion());
 									topic.setDescription(question_.getDetail());
 									forumService.saveTopic(sessionProvider, ids[0], ids[1], topic, false, false, "");
 								}
              } catch (Exception e) {
	              e.printStackTrace();
              }
 						}
 					}
        	
          questionNode = fAQService_.saveQuestion(question_, false, sessionProvider, questionForm.faqSetting_) ;
          multiLanguages.removeLanguage(questionNode, questionForm.listLanguages) ;
          if(questionForm.listLanguages.size() > 1) {
          	try{
          		QuestionLanguage questionLanguage = new QuestionLanguage() ;
          		for(int i = 1; i < questionForm.listLanguages.size() ; i ++) {
          			if(questionForm.listLanguages.get(i).equals(questionForm.defaultLanguage_)) continue;
          			questionLanguage = questionForm.mapLanguageNode_.get(questionForm.listLanguages.get(i));
          			if(questionLanguage == null){
          				questionLanguage = new QuestionLanguage();
          				questionLanguage.setLanguage(questionForm.listLanguages.get(i));
          			}
          			questionLanguage.setDetail(questionForm.listQuestionDetail.get(questionForm.listLanguages.get(i))) ;
          			questionLanguage.setQuestion(questionForm.listQuestionContent.get(questionForm.listLanguages.get(i)).replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
          			multiLanguages.addLanguage(questionNode, questionLanguage) ;
          		}
          	} catch(Exception e) {
          		e.printStackTrace() ;
          	}
          }
          
        } else {
          questionNode = fAQService_.saveQuestion(question_, true, sessionProvider, questionForm.faqSetting_) ;
          if(questionForm.listLanguages.size() > 1) {
          	try{
          		QuestionLanguage questionLanguage = new QuestionLanguage() ;
          		for(int i = 0; i < questionForm.listLanguages.size() ; i ++) {
          			if(questionForm.listLanguages.get(i).equals(questionForm.defaultLanguage_)) continue;
          			questionLanguage = new QuestionLanguage() ;
          			questionLanguage.setLanguage(questionForm.listLanguages.get(i)) ;
          			questionLanguage.setDetail(questionForm.listQuestionDetail.get(questionForm.listLanguages.get(i))) ;
          			questionLanguage.setQuestion(questionForm.listQuestionContent.get(questionForm.listLanguages.get(i)).replaceAll("<", "&lt;").replaceAll(">", "&gt;")) ;
          			multiLanguages.addLanguage(questionNode, questionLanguage) ;
          		}
          	} catch(Exception e) {
          		e.printStackTrace() ;
          	}
          }
        }
        
        if(questionForm.questionId_ == null || questionForm.questionId_.trim().length() < 1) {
          if(questionIsApproved) {
          	UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
          	uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.add-new-question-successful", null, ApplicationMessage.INFO)) ;
          	event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          } else {
          	UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
          	uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.question-not-is-approved", null, ApplicationMessage.INFO)) ;
          	event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          }
        }
      } catch (PathNotFoundException notFoundException) {
        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
      } catch (Exception e) {
        e.printStackTrace() ;
      }
      
      if(!questionForm.isChildOfManager) {
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        questions.setIsNotChangeLanguage() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
       // String pathParentCategoryId = questions.getPathService(question_.getCategoryId()) ;
       // String parentCategoryId = null ;
        //if(pathParentCategoryId.lastIndexOf("/") > 0) parentCategoryId = pathParentCategoryId.substring(pathParentCategoryId.lastIndexOf("/")+1, pathParentCategoryId.length()) ;
        /*if(questionNode!= null && questions.getCategoryId() != null && questions.getCategoryId().trim().length() > 0 &&
            !questions.getCategoryId().equals(question_.getCategoryId()) && !questions.getCategoryId().equals(parentCategoryId)) {
          UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
          Category category = fAQService_.getCategoryById(question_.getCategoryId(), sessionProvider) ;
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-moved", new Object[]{category.getName()}, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
        }*/
      } else {
        UIQuestionManagerForm questionManagerForm = questionForm.getParent() ;
        UIResponseForm responseForm = questionManagerForm.getChild(UIResponseForm.class) ;
        if(questionManagerForm.isResponseQuestion && questionForm.getQuestionId().equals(responseForm.getQuestionId())) {
          responseForm.setIsChildren(true) ;
          responseForm.setQuestionId(question_, "", !fAQService_.getCategoryById(question_.getCategoryId(), sessionProvider).isModerateAnswers()) ;
        }
        questionManagerForm.isEditQuestion = false ;
        UIPopupContainer popupContainer = questionManagerForm.getParent() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
      }
      
      sessionProvider.close();
    }
  }
  
  static public class AttachmentActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource() ;     
      UIPopupContainer popupContainer = questionForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      UIAttachMentForm attachMentForm = uiChildPopup.activate(UIAttachMentForm.class, 550) ;
      attachMentForm.setNumberUpload(5);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  
  static public class RemoveAttachmentActionListener extends EventListener<UIQuestionForm> {
    @SuppressWarnings("static-access")
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource() ;
      String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
      for (FileAttachment att : questionForm.listFileAttach_) {
        if ( att.getId()!= null){
          if(att.getId().equals(attFileId)) {
            questionForm.listFileAttach_.remove(att) ;
            break;
          }
        } else {
          if(att.getPath().equals(attFileId)) {
            questionForm.listFileAttach_.remove(att) ;
            break;
          }
        }
      }
      questionForm.refreshUploadFileList() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questionForm) ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource() ;
      if(!questionForm.isChildOfManager) {
        UIFAQPortlet portlet = questionForm.getAncestorOfType(UIFAQPortlet.class) ;
        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
        popupAction.deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } else {
        UIQuestionManagerForm questionManagerForm = questionForm.getParent() ;
        questionManagerForm.isEditQuestion = false ;
        UIPopupContainer popupContainer = questionManagerForm.getAncestorOfType(UIPopupContainer.class) ;
        UIAttachMentForm attachMentForm = popupContainer.findFirstComponentOfType(UIAttachMentForm.class) ;
        if(attachMentForm != null) {
          UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
          popupAction.deActivate() ;
        } else {
          UILanguageForm languageForm = popupContainer.findFirstComponentOfType(UILanguageForm.class) ;
          if(languageForm != null) {
            UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
            popupAction.deActivate() ;
          }
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
      }
    }
  }
}


