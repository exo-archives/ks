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
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQContainer;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.faq.webui.ValidatorDataInput;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
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
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;
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
  //private Map<String, String> listQuestionDetail = new HashMap<String, String>() ;
  //private Map<String, String> listQuestionContent = new HashMap<String, String>() ;
  //private List<String> listLanguages = new ArrayList<String>() ;
  private List<FileAttachment> listFileAttach_ = new ArrayList<FileAttachment>() ;
  private Map<String, QuestionLanguage> mapLanguage = new HashMap<String, QuestionLanguage>() ;
  private String categoryId_ = "" ;
  private String questionId_ = null ;
  private String defaultLanguage_ = "" ;
  private String lastLanguage_ = "";
  private String link_ = "";
  private String author_ = "" ;
  private String email_ = "" ;
  private List<String> questionContents_ = new ArrayList<String>() ;
  private boolean isApproved_ = true ;
  private boolean isActivated_ = true ;
  private boolean isMode = false;
  private boolean isChildOfManager = false ;
  private FAQSetting faqSetting_ ;
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public String getLink() {return link_;}
	public void setLink(String link) { this.link_ = link;}
	public void setFAQSetting(FAQSetting faqSetting) {this.faqSetting_ = faqSetting;}
  public UIQuestionForm() throws Exception {
  	//listQuestionDetail.clear();
  	//listQuestionContent.clear();
    isChildOfManager = false ;
    //listLanguages.clear() ;
    listFileAttach_.clear() ;
    mapLanguage.clear() ;
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
		List<String> languages = FAQUtils.getAllLanguages(this) ;
    for(String lang : languages) {      
      if(lang.equals(defaultLanguage_))
      	listSystemLanguages.add(new SelectItemOption<String>(lang + " (default) ", lang)) ;
      else
      	listSystemLanguages.add(new SelectItemOption<String>(lang, lang)) ;
    }
	}

  public void initPage(boolean isEdit) throws Exception {
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
    
    inputQuestionDetail = new UIFormWYSIWYGInput(QUESTION_DETAIL, QUESTION_DETAIL, "") ;
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
    	String cateId = question_.getPath();
    	if(!FAQUtils.isFieldEmpty(cateId))cateId = cateId.substring(0, cateId.indexOf("/"+org.exoplatform.faq.service.Utils.QUESTION_HOME));
    	if(getIsModerator(cateId)){
	      addChild(inputIsApproved.setChecked(isApproved_)) ;
	      addChild(inputIsActivated.setChecked(isActivated_)) ;
    	}
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
  
  private boolean getIsModerator(String cateId) throws Exception{
  	try {
  		if(isMode || faqSetting_.isAdmin() || fAQService_.isCategoryModerator(cateId, FAQUtils.getCurrentUser())) {
  			isMode = true;
  			return isMode ;
  		}
    } catch (Exception e) {e.printStackTrace();}
    return false;
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
    this.removeChildById(ALL_LANGUAGES);
    listFileAttach_.clear() ;
    mapLanguage.clear() ;
  }
  
  public void setQuestion(Question question) throws Exception{
  	List<QuestionLanguage> questionLanguages = new ArrayList<QuestionLanguage>();
    questionId_ = question.getPath() ;
    categoryId_ = "" ;
    try {
      question_ = question ;
      defaultLanguage_ = question_.getLanguage() ;
      lastLanguage_ = defaultLanguage_;
      questionLanguages = fAQService_.getQuestionLanguages(questionId_) ;
      for(QuestionLanguage questionLanguage : questionLanguages) {
      	mapLanguage.put(questionLanguage.getLanguage(), questionLanguage);
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
    } catch (Exception e) {
      e.printStackTrace();
      initPage(false) ;
    } 
  }
  
  public boolean isMode() {return isMode;}
  public void setIsMode(boolean isMode) {this.isMode = isMode;}
  
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
  public void setCategoryId(String categoryId) throws Exception {
    this.categoryId_ = categoryId ;
    questionId_ = null ;
    defaultLanguage_ = FAQUtils.getDefaultLanguage() ;
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
  
  static public class SelectLanguageActionListener extends EventListener<UIQuestionForm> {
    public void execute(Event<UIQuestionForm> event) throws Exception {
      UIQuestionForm questionForm = event.getSource() ;
      String language = questionForm.selectLanguage.getValue() ;
      String detail = questionForm.inputQuestionDetail.getValue();
      String question = questionForm.inputQuestionContent.getValue();
      if(!ValidatorDataInput.fckContentIsNotEmpty(detail)) detail = " ";
      if(!ValidatorDataInput.fckContentIsNotEmpty(question)){
      	//System.out.println("=======> lang empty") ;
      	if( questionForm.mapLanguage.containsKey(questionForm.lastLanguage_)){
      		questionForm.mapLanguage.get(questionForm.lastLanguage_).setState(QuestionLanguage.DELETE) ;      		
      	}
      }else {
      	//System.out.println("=======> lang not empty") ;
      	QuestionLanguage langObj = new QuestionLanguage();
      	if(questionForm.mapLanguage.containsKey(questionForm.lastLanguage_)) {
      		langObj = questionForm.mapLanguage.get(questionForm.lastLanguage_) ;
      		langObj.setState(QuestionLanguage.EDIT) ;
      	}
      	langObj.setQuestion(question) ;
      	langObj.setDetail(detail) ;
      	langObj.setLanguage(questionForm.lastLanguage_) ;
      	questionForm.mapLanguage.put(langObj.getLanguage(), langObj) ;
      }      
      questionForm.lastLanguage_ = language;
      if(questionForm.mapLanguage.containsKey(language)){
      	questionForm.inputQuestionDetail.setValue(questionForm.mapLanguage.get(language).getDetail());
      	questionForm.inputQuestionContent.setValue(questionForm.mapLanguage.get(language).getQuestion());
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
    public void execute(Event<UIQuestionForm> event) throws Exception {
    	UIQuestionForm questionForm = event.getSource() ;
    	try {
    		boolean isNew = true;
	      boolean questionIsApproved = true ;
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
	      } else if(FAQUtils.getCurrentUser() == null && FAQUtils.getUserByUserId(author) != null){
	      	UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
	      	uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.author-is-duplicate", null, ApplicationMessage.WARNING)) ;
	      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	      	return ;
	      }
	      if(emailAddress == null || emailAddress.trim().length() < 1 || !FAQUtils.isValidEmailAddresses(emailAddress)) {
	        UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
	        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.email-address-invalid", null, ApplicationMessage.WARNING)) ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	        return ;
	      }
	      String language = questionForm.selectLanguage.getValue() ;
	      //Duy Tu: Check require question content not empty.
	      if(FAQUtils.isFieldEmpty(questionContent)) {
	      	UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
	      	if(language.equals(questionForm.defaultLanguage_)){
	      		uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.default-question-null", null, ApplicationMessage.WARNING)) ;
	      	}else {
		      	String[]sms = new String[]{language};
		        uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.mutil-language-question-null", sms, ApplicationMessage.WARNING)) ;
	      	}
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	        return ;
	      }
	
	      if(language.equals(questionForm.defaultLanguage_)) {
	      	if(questionContent == null) {
	          UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
	          uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.default-question-null", null, ApplicationMessage.WARNING)) ;
	          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	          return ;
	        }
	      }else {
	      	if(questionForm.mapLanguage.isEmpty() || questionForm.mapLanguage.get(questionForm.getDefaultLanguage()) == null) {
	          UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
	          uiApplication.addMessage(new ApplicationMessage("UIQuestionForm.msg.default-question-null", null, ApplicationMessage.WARNING)) ;
	          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
	          return ;
	        }
	      }
	      
	      String questionDetail = questionForm.inputQuestionDetail.getValue();
	      if(!ValidatorDataInput.fckContentIsNotEmpty(questionDetail)) questionDetail = " ";
	      if(!ValidatorDataInput.fckContentIsNotEmpty(questionContent)){
	      	if( questionForm.mapLanguage.containsKey(language)){
	      		questionForm.mapLanguage.get(language).setState(QuestionLanguage.DELETE) ;
	      		//System.out.println(questionForm.mapLanguage.get(language).getLanguage() + " " + questionForm.mapLanguage.get(questionForm.lastLanguage_).getState()) ;
	      	}
	      }
	       
	      if(questionForm.questionId_ == null || questionForm.questionId_.trim().length() < 1) { //Add new question
	        question_ = new Question() ;
	        question_.setCategoryId(questionForm.getCategoryId()) ;
	        question_.setRelations(new String[]{}) ;
	        try{
	          questionIsApproved = !fAQService_.isModerateAnswer(questionForm.getCategoryId()) ;
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
	        question_.setCreatedDate(date) ;
	        question_.setApproved(questionIsApproved) ;
	      } else { // Edit question
	      	isNew = false ;
	      	if(questionForm.isMode){
		        question_.setApproved(questionForm.getUIFormCheckBoxInput(IS_APPROVED).isChecked()) ;
		        question_.setActivated(questionForm.getUIFormCheckBoxInput(IS_ACTIVATED).isChecked()) ;
	      	}
	      }
	      question_.setLanguage(questionForm.getDefaultLanguage()) ;
	      question_.setAuthor(author) ;
	      question_.setEmail(emailAddress) ;
	      if(language.equals(questionForm.defaultLanguage_)) {
	      	question_.setQuestion(questionContent);
	        question_.setDetail(questionDetail);
	      }else {
	      	question_.setQuestion(questionForm.mapLanguage.get(questionForm.getDefaultLanguage()).getQuestion());
	        question_.setDetail(questionForm.mapLanguage.get(questionForm.getDefaultLanguage()).getDetail());
	        QuestionLanguage otherLang = new QuestionLanguage() ;
	        if(questionForm.mapLanguage.containsKey(language)) {
	        	otherLang = questionForm.mapLanguage.get(language) ;
	        	otherLang.setState(QuestionLanguage.EDIT) ;
	        }
	        //System.out.println("questionContent =======> " + questionContent) ;
	        otherLang.setQuestion(questionContent) ;
	        otherLang.setDetail(questionDetail) ;
	        otherLang.setLanguage(language) ;
	        questionForm.mapLanguage.put(language, otherLang) ;
	      }
	      //System.out.println("lang =======> " + questionForm.mapLanguage.keySet().size()) ;
	      questionForm.mapLanguage.remove(question_.getLanguage()) ;
	      question_.setMultiLanguages(questionForm.mapLanguage.values().toArray(new QuestionLanguage[]{})) ;
	      question_.setAttachMent(questionForm.listFileAttach_) ;      
	      UIFAQPortlet portlet = questionForm.getAncestorOfType(UIFAQPortlet.class) ;
	      UIQuestions questions = portlet.getChild(UIFAQContainer.class).getChild(UIQuestions.class) ;
	      //Create link by Vu Duy Tu.
	      if(isNew){
	      	StringBuilder qsId = new StringBuilder().append(question_.getCategoryId()).append("/").append(org.exoplatform.faq.service.Utils.QUESTION_HOME)
	      		.append("/").append(question_.getId());
		      String link = FAQUtils.getLink(questionForm.getLink(), questionForm.getId(), "UIQuestions", "Attachment", "ViewQuestion", qsId.toString().replace("private", "public"));
		      question_.setLink(link) ;
	      }
	      
	      //For discuss in forum
	      try{        
	        FAQUtils.getEmailSetting(questionForm.faqSetting_, isNew, false);
	      	FAQSetting faqSetting = new FAQSetting();
					FAQUtils.getPorletPreference(faqSetting);
					if(faqSetting.getIsDiscussForum()) {
						String topicId = question_.getTopicIdDiscuss();
						if(topicId != null && topicId.length() > 0) {
							try {
								ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
								Topic topic = (Topic)forumService.getObjectNameById(topicId, Utils.TOPIC);
								if(topic != null) {
									String[] ids = topic.getPath().split("/");
									int t = ids.length;
									topic.setModifiedBy(FAQUtils.getCurrentUser());
									topic.setTopicName(question_.getQuestion());
									topic.setDescription(question_.getDetail());
									topic.setIsWaiting(true);
									forumService.saveTopic(ids[t - 3], ids[t - 2], topic, false, false, "");
									//System.out.println("\n\n ==========>" + ids[t - 3] + " / " + ids[t - 2]);
								}
	            } catch (Exception e) {
	              e.printStackTrace();
	            }
						}
					}
	      	// end discuss
					if(fAQService_.saveQuestion(question_, isNew, questionForm.faqSetting_) == null) {
						UIApplication uiApplication = questionForm.getAncestorOfType(UIApplication.class) ;
		        uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.question-id-deleted", null, ApplicationMessage.WARNING)) ;
		        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
		        isNew = false;
					}        
	        if(isNew) {
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
	      } catch (Exception e) {
	        e.printStackTrace() ;
	      }
	      
	      if(!questionForm.isChildOfManager) {
	        UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
	        questions.setDefaultLanguage();
	        questions.updateCurrentQuestionList() ;
	      	if(!isNew && question_.getPath().equals(questions.viewingQuestionId_)){
	      		questions.updateLanguageMap() ;
	      	}
	        
	        event.getRequestContext().addUIComponentToUpdateByAjax(questions.getAncestorOfType(UIFAQContainer.class)) ;
	        popupAction.deActivate() ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;       
	      } else {
	        UIQuestionManagerForm questionManagerForm = questionForm.getParent() ;
	        UIResponseForm responseForm = questionManagerForm.getChild(UIResponseForm.class) ;
	        if(questionManagerForm.isResponseQuestion && questionForm.getQuestionId().equals(responseForm.questionId_)) {
	          responseForm.updateChildOfQuestionManager(true) ;
	          responseForm.setQuestionId(question_, "", !fAQService_.isModerateAnswer(question_.getPath())) ;
	        }
	        questionManagerForm.isEditQuestion = false ;
	        UIPopupContainer popupContainer = questionManagerForm.getParent() ;
	        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
	      }
    	} catch (Exception e) {
    		e.printStackTrace();
      }
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
        } 
        event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
      }
    }
  }
}


