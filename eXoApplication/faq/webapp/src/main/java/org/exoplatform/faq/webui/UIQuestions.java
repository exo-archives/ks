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
package org.exoplatform.faq.webui;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.webui.popup.UICategoryForm;
import org.exoplatform.faq.webui.popup.UIDeleteQuestion;
import org.exoplatform.faq.webui.popup.UIMoveCategoryForm;
import org.exoplatform.faq.webui.popup.UIMoveQuestionForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.faq.webui.popup.UIQuestionForm;
import org.exoplatform.faq.webui.popup.UIResponseForm;
import org.exoplatform.faq.webui.popup.UISendMailForm;
import org.exoplatform.faq.webui.popup.UISettingForm;
import org.exoplatform.faq.webui.popup.UIWatchForm;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@SuppressWarnings("unused")
@ComponentConfig(
		template =	"app:/templates/faq/webui/UIQuestions.gtmpl" ,
		events = {
			@EventConfig(listeners = UIQuestions.AddCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.AddNewQuestionActionListener.class),
			@EventConfig(listeners = UIQuestions.SubCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.OpenCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.ShowQuestionNotYetAnswerActionListener.class),
	    @EventConfig(listeners = UIQuestions.EditSubCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.EditCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.DeleteCategoryActionListener.class, confirm= "UIQuestions.msg.confirm-delete-category"),
	    @EventConfig(listeners = UIQuestions.MoveCategoryActionListener.class),
	    @EventConfig(listeners = UIQuestions.MoveDownActionListener.class),
	    @EventConfig(listeners = UIQuestions.MoveUpActionListener.class),
	    @EventConfig(listeners = UIQuestions.SettingActionListener.class),
	    @EventConfig(listeners = UIQuestions.WatchActionListener.class),
      
      // action of question:
	    @EventConfig(listeners = UIQuestions.ViewQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.ResponseQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.EditQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.DeleteQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.MoveQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.PrintQuestionActionListener.class),
	    @EventConfig(listeners = UIQuestions.SendQuestionActionListener.class)
      
		}
)
public class UIQuestions extends UIContainer {
	private List<Category> categories_ = null ;
  private List<Question> listQuestion_ =  null ;
  private String categoryId = null ;
  private String parentId_ = null ;
  private String questionView_ = "" ;
  public static String newPath_ = "" ;
  private String[] firstTollbar = new String[]{"AddCategory","ShowQuestionNotYetAnswer", "Setting"} ;
  private String[] secondTollbar = new String[]{"SubCategory", "AddNewQuestion", "Setting"} ; 
	private static	FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
	public UIQuestions()throws Exception {
	  this.categoryId = new String() ;
		setCategories() ;
	}
  
  public String[] getActionTollbar() {
    return this.secondTollbar ;
  }
  
  @SuppressWarnings("unused")
  private String[] getFirstTollbar() {
    return this.firstTollbar ;
  }
  
  @SuppressWarnings("unused")
  private String[] getActionCategory(){
    String[] action = new String[]{"SubCategory", "AddNewQuestion", "EditCategory", "DeleteCategory", "MoveCategory", "MoveDown", "MoveUp", "Watch"} ;
    return action ;
  }
  private String[] getSecondActionCategory() {
    String[] action = new String[]{"EditSubCategory", "DeleteCategory", "MoveCategory"} ;
    return action ;
  }
  @SuppressWarnings("unused")
  private String[] getActionQuestion(){
    String[] action = new String[]{"ResponseQuestion","EditQuestion","DeleteQuestion","MoveQuestion","PrintQuestion","SendQuestion"} ;
    return action ;
  }
  @SuppressWarnings("unused")
  private String getParentId(){
    return this.parentId_ ;
  }
  
  public void setParentId(String parentId_) {
    this.parentId_ = parentId_ ;
  }
  
  public void setCategories() throws Exception  {
    if(this.categoryId == null || this.categoryId.trim().length() < 1) {
      categories_ = faqService.getSubCategories(null, FAQUtils.getSystemProvider()) ;
    } else {
      categories_ = faqService.getSubCategories(this.categoryId, FAQUtils.getSystemProvider()) ;
    }
    System.out.println("number of category : " + categories_.size());
  }
	
  public void setCategories(String categoryId) throws Exception  {
  	this.categoryId = categoryId ;
    categories_ = faqService.getSubCategories(categoryId, FAQUtils.getSystemProvider()) ;
  }
  
	@SuppressWarnings("unused")
  private List<Category> getCategories() throws Exception {
		return categories_ ;
	}
  
  public void update(Category category) throws Exception {
  	categoryId = category.getId() ;
//		this.getAncestorOfType(UIFAQContainer.class).getChild(UIBreadcumbs.class).setUpdataPath((categoryId)) ;
	}
	
  public void setListQuestion() throws Exception {
    SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
    this.categories_.clear() ;
    this.categories_ = faqService.getSubCategories(categoryId, sessionProvider) ;
    this.listQuestion_ = faqService.getQuestionsByCatetory(categoryId, sessionProvider).getAll() ;
  }
  
  public void setList(String category) throws Exception {
    SessionProvider sessionProvider = FAQUtils.getSystemProvider() ;
    listQuestion_ = faqService.getQuestionsByCatetory(category, sessionProvider).getAll() ;
  }
  
  @SuppressWarnings("unused")
  private List<Question> getListQuestion() {
    return this.listQuestion_ ;
  }
  
  @SuppressWarnings("unused")
  private String getQuestionView(){
    return this.questionView_ ;
  }
  
  public void setQuestionView(String questionid){
    this.questionView_ = questionid ;
  }
  
  @SuppressWarnings("unused")
  private String getCategoryId(){
    return this.categoryId ;
  }
  
  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId ;
  }
  
  public String getQuestionRelationById(String questionId) {
    Question question = new Question();
    try {
      question = faqService.getQuestionById(questionId, FAQUtils.getSystemProvider());
      return question.getCategoryId() + "/" + question.getId() + "/" + question.getQuestion();
    } catch (Exception e) {
      e.printStackTrace();
      return "" ;
    }
  }
	
	public void moveDownUp(Event<UIQuestions> event, int i) {
  	String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
  	int index = 0 ;
  	for (Category cate : categories_) {
  		if (cate.getId().equals(categoryId)) {
  			break ;
  		} else {
  			index ++ ;
  		}
  	}

  	if (index < 0) return ;
  	if ( index ==0 && i == -1) return ;
  	if (index == categories_.size()-1 && i==1) return ;
  	Category category = categories_.remove(index) ;
  	for (Category cate : categories_) {
  	}
  	categories_.add(index+i, category) ;
  	
  }	
	
	static  public class AddCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
    	System.out.println("\n\n AddCategoryActionListener");
    	UIQuestions question = event.getSource() ; 
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,520) ;  
		  uiPopupContainer.setId("AddCategoryForm") ;
		  UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
		  category.init() ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      UIFAQContainer fAQContainer = question.getAncestorOfType(UIFAQContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
		}
	}
  
	static  public class ShowQuestionNotYetAnswerActionListener extends EventListener<UIQuestions> {
	  public void execute(Event<UIQuestions> event) throws Exception {
	    System.out.println("\n\n ShowQuestionNotYetAnswerActionListener");
	    UIQuestions question = event.getSource() ; 
	  }
	}
  
  static public class AddNewQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
      questionForm.setCategoryId(categoryId) ;
      popupContainer.setId("AddQuestion") ;
      popupAction.activate(popupContainer, 650, 1000) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static public class OpenCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ;
      UIFAQPortlet faqPortlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      questions.setCategoryId(categoryId) ;
      questions.setListQuestion() ;
      UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
      String oldPath = breadcumbs.getPaths() ;
      if(oldPath != null && oldPath.trim().length() > 0) {
      	if(!oldPath.contains(categoryId)) {
      		newPath_ = oldPath + "/" +categoryId ;
      		breadcumbs.setUpdataPath(oldPath + "/" +categoryId);
      	}
      } else breadcumbs.setUpdataPath(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
      UIFAQContainer fAQContainer = questions.getAncestorOfType(UIFAQContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
    }
  }
	
	static	public class EditCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			System.out.println("\n\n EditCategoryActionListener");
			UIQuestions question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer uiPopupContainer = popupAction.activate(UIPopupContainer.class,520) ;
			uiPopupContainer.setId("EditCategoryForm") ;
      UICategoryForm uiCategoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
			uiCategoryForm.init();
			uiCategoryForm.setCategoryValue(categoryId, true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
		}
	
	static	public class DeleteCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
			System.out.println("\n\n DeleteCategoryActionListener");
			UIQuestions question = event.getSource() ; 			
			String CategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			if(CategoryId != null) {
				faqService.removeCategory(CategoryId, FAQUtils.getSystemProvider()) ;
				question.setCategories() ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
		}
	}
	
	static	public class MoveCategoryActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = faqPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIMoveCategoryForm uiMoveCategoryForm = popupAction.activate(UIMoveCategoryForm.class, 400) ;
			popupContainer.setId("MoveCategoryForm") ;
			uiMoveCategoryForm.setCategoryID(categoryId) ;
			uiMoveCategoryForm.setListCate() ;
			uiMoveCategoryForm.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}
	}
  
	static	public class MoveUpActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			question.moveDownUp(event, -1);
			event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
	}
  
  }
	static	public class MoveDownActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
			UIQuestions question = event.getSource() ; 
			question.moveDownUp(event, 1);
			event.getRequestContext().addUIComponentToUpdateByAjax(question) ;
	}
  
		}
	static	public class SettingActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
    	UIQuestions question = event.getSource() ; 
    	String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;	
			UISettingForm uiSetting = popupAction.activate(UISettingForm.class, 400) ;
			popupContainer.setId("CategorySettingForm") ;
      uiSetting.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	static	public class WatchActionListener extends EventListener<UIQuestions> {
		public void execute(Event<UIQuestions> event) throws Exception {
    	UIQuestions question = event.getSource() ; 
    	String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIWatchForm uiWatchForm = popupAction.activate(UIWatchForm.class, 420) ;
			popupContainer.setId("CategoryWatchForm") ;
			uiWatchForm.setCategoryID(cateId) ;
			uiWatchForm.init() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	static  public class SubCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
    	System.out.println("\n\n SubCategoryActionListener");
    	UIQuestions question = event.getSource() ; 
    	String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet uiPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class) ; 
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,520) ;  
		  uiPopupContainer.setId("SubCategoryForm") ;
		  UICategoryForm category = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
		  category.setParentPath(categoryId) ;
		  System.out.println("=====>>>>>>CategoryId: " + categoryId) ;
		  category.init() ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
	
	static  public class EditSubCategoryActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
    	System.out.println("\n\n EditSubCategoryActionListener");
    	UIQuestions question = event.getSource() ; 
    	String categoryId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet faqPortlet = question.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction uiPopupAction = faqPortlet.getChild(UIPopupAction.class) ; 
      UIPopupContainer uiPopupContainer = uiPopupAction.activate(UIPopupContainer.class,520) ;  
      uiPopupContainer.setId("EditSubCategoryForm") ;
		  UICategoryForm categoryForm = uiPopupContainer.addChild(UICategoryForm.class, null, null) ;
		  categoryForm.init() ;
		  System.out.println("=====>>>>>>newPath_: " + newPath_) ;
		  String parentCategoryId = newPath_.substring(newPath_.lastIndexOf("/")+1, newPath_.length()) ;
		  System.out.println("=====>>>>>>parentCategoryId: " + parentCategoryId) ;
		  System.out.println("=====>>>>>>CategoryId: " + categoryId) ;
		  categoryForm.setParentPath(parentCategoryId) ;
		  categoryForm.setCategoryValue(categoryId, true) ;
		  event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
  
  // action for question :
	static  public class ViewQuestionActionListener extends EventListener<UIQuestions> {
	  public void execute(Event<UIQuestions> event) throws Exception {
	    UIQuestions uiQuestions = event.getSource() ; 
      String strId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      String questionId = new String() ;
      if(strId.indexOf("/") < 0) {
        questionId = strId ;
      } else {
        String categoryId = strId.split("/")[0] ;
        questionId = strId.split("/")[1] ;
        
        UIFAQPortlet faqPortlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
        uiQuestions.setCategoryId(categoryId) ;
        uiQuestions.setListQuestion() ;
        UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
        String oldPath = breadcumbs.getPaths() ;
        if(oldPath != null && oldPath.trim().length() > 0) {
          if(!oldPath.contains(categoryId)) {
            newPath_ = oldPath + "/" +categoryId ;
            breadcumbs.setUpdataPath(oldPath + "/" +categoryId);
          }
        } else breadcumbs.setUpdataPath(categoryId);
        event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
        UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
      }
      
      String questionViewed = uiQuestions.questionView_ ;
      if( questionViewed == null || questionViewed.trim().length() < 1 || !questionViewed.equals(questionId)){
        uiQuestions.questionView_ = questionId ; 
      } else {
        uiQuestions.questionView_ = "" ;
      }
	  }
	}
  
  static  public class ResponseQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions question = event.getSource() ; 
      UIFAQPortlet portlet = question.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIResponseForm responseForm = popupContainer.addChild(UIResponseForm.class, null, null) ;
      responseForm.setQuestionId(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      popupContainer.setId("FAQResponseQuestion") ;
      popupAction.activate(popupContainer, 700, 1000) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class EditQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ;
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIQuestionForm questionForm = popupContainer.addChild(UIQuestionForm.class, null, null) ;
      questionForm.setQuestionId(questionId) ;
      popupContainer.setId("AddQuestion") ;
      popupAction.activate(popupContainer, 650, 1000) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class DeleteQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ; 
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIDeleteQuestion deleteQuestion = popupContainer.addChild(UIDeleteQuestion.class, null, null) ;
      deleteQuestion.setQuestionId(questionId) ;
      popupContainer.setId("FAQDeleteQuestion") ;
      popupAction.activate(popupContainer, 450, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class MoveQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions questions = event.getSource() ; 
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = questions.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UIMoveQuestionForm moveQuestionForm = popupContainer.addChild(UIMoveQuestionForm.class, null, null) ;
      moveQuestionForm.setQuestionId(questionId) ;
      popupContainer.setId("FAQMoveQuestion") ;
      popupAction.activate(popupContainer, 400, 200) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class PrintQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions question = event.getSource() ; 
    }
  }
  
  static  public class SendQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
      UIQuestions uiQuestions = event.getSource() ; 
      String questionId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFAQPortlet portlet = uiQuestions.getAncestorOfType(UIFAQPortlet.class) ;
      UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
      UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
      UISendMailForm sendMailForm = popupContainer.addChild(UISendMailForm.class, null, null) ;
      popupContainer.setId("FAQSendMailForm") ;
      popupAction.activate(popupContainer, 700, 1000) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }

}