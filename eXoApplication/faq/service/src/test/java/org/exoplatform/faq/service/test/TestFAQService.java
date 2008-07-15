/*
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
 */
package org.exoplatform.faq.service.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */


public class TestFAQService extends FAQServiceTestCase{
	private FAQService faqService_ ;
	private SessionProvider sProvider_ ;
	private List<FileAttachment> listAttachments = new ArrayList<FileAttachment>() ;
	public void setUp() throws Exception {
    super.setUp() ;
    faqService_ = (FAQService) container.getComponentInstanceOfType(FAQService.class) ;
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
  }
	
	public void testFAQService() throws Exception {
		assertNotNull(faqService_) ;
  	assertNotNull(sProvider_) ;
	}
  
	public void testCategory() throws Exception {
		Category cate1 = createCategory() ;
//	add category
	faqService_.saveCategory(null, cate1, true, sProvider_) ;
	
//get category Id	
	assertNotNull(faqService_.getCategoryById(cate1.getId(), sProvider_)) ;
	
//	edit category 
	cate1.setName("Nguyen van truong test category111111") ;
	cate1.setCreatedDate(new Date()) ;
	faqService_.saveCategory(null, cate1, false, sProvider_);
	
//	add category 2
	Category cate2 = createCategory() ;
	cate2.setName("Nguyen van truong test category222222") ;
	cate2.setModerators(new String[]{"Demo"}) ;
	faqService_.saveCategory(null, cate2, true, sProvider_) ;
	
//	add sub categoy 1
	Category subCate1 = createCategory() ;
	subCate1.setName("Nguyen van truong test Sub category 1") ;
	subCate1.setModerators(new String[]{"Truong","Demo"}) ;
	faqService_.saveCategory(cate1.getId(), subCate1, true, sProvider_) ;
// get all category
	List<Category> list = faqService_.getAllCategories(sProvider_) ;
	System.out.println("\n\n Get all Categroy :");
	for(Category cate: list) {
			String[] moderator = cate.getModerators() ;
			List<String> listModerator = new ArrayList<String>() ;
			for(String string: moderator) {
				listModerator.add(string) ;
			}
		System.out.println(cate.getName()+"=="+cate.getDescription()+"=="+listModerator+"=="+cate.isModerateQuestions()+"=="+cate.getCreatedDate());
	}
	
//move category 
	faqService_.moveCategory(cate2.getId(), cate1.getId(), sProvider_) ;
	
//	get sub category
	List<Category> listSubCate = faqService_.getSubCategories(cate1.getId(), sProvider_) ;
	System.out.println("\n\n get all category in category 1:");
	for(Category cate : listSubCate) {
//		String[] moderator = cate.getModerators() ;
//		List<String> listModerator = new ArrayList<String>() ;
//		for(String string: moderator) {
//			listModerator.add(string) ;
//		}
		System.out.println(cate.getName()+"=="+cate.getDescription()+"=="+cate.getModerators()+"=="+cate.isModerateQuestions()+"=="+cate.getCreatedDate());
	}

//	Delete category
	faqService_.removeCategory(cate1.getId(), sProvider_) ;

	}
	
	 public Category createCategory() {
		Date date = new Date() ;
		Category category = new Category() ;
		category.setName("Nguyen Van Truong") ;
		category.setDescription("Description") ;
		category.setModerateQuestions(true) ;
		category.setModerators(new String[]{"root"}) ;
		category.setCreatedDate(date) ;
		return category ;
	}
	
	public void testGetCategoryById() throws Exception {
		Category cate = createCategory() ;
		faqService_.saveCategory(null, cate, true, sProvider_) ;
		assertNotNull(faqService_.getCategoryById(cate.getId(), sProvider_)) ;
//		assertNull(faqService_.getCategoryById(cate.getId(), sProvider_)) ;
	}
	 
	public Question createQuestion() throws Exception {
		Category cate = createCategory() ;
		faqService_.saveCategory(null, cate, true, sProvider_) ;
		Question question = new Question() ;
		question.setCategoryId(cate.getId()) ;
		question.setRelations(new String[]{}) ;
		question.setResponses(" ") ;
    question.setApproved(true) ;
    question.setDateResponse(new Date()) ;
    question.setActivated(true) ;
    question.setLanguage("English") ;
    question.setAuthor("Nguyen van truong") ;
    question.setEmail("truongtb1984@gmail.com") ;
    question.setQuestion("Add new question 1") ;
    question.setCreatedDate(new Date()) ;
    question.setAttachMent(listAttachments) ;
		return question ;
	}
	public void testQuestion() throws Exception {
		Question question1 = createQuestion() ;
//		save question 1
		faqService_.saveQuestion(question1, true, sProvider_) ;
		System.out.println("==>categoryId1:" + question1.getCategoryId());
	
//		Edit question 1
		question1.setQuestion("Nguyen van truong test question 11111111") ;
		question1.setDateResponse(new Date()) ;
		question1.setResponses(" ") ; //Nguyen van truong test response 11111111
		faqService_.saveQuestion(question1, false, sProvider_) ;
		
//		Add question 2
		Question question2 = createQuestion() ;
		question2.setRelations(new String[]{}) ;
		question2.setResponses(" ") ;
    question2.setApproved(true) ;
    question2.setDateResponse(new Date()) ;
    question2.setActivated(true) ;
    question2.setLanguage("English") ;
    question2.setAuthor("Mai Van Ha") ;
    question2.setEmail("truong_tb1984@yahoo.com") ;
    question2.setQuestion("Nguyen van truong test question 2222222") ;
    question2.setCreatedDate(new Date()) ;
    faqService_.saveQuestion(question2, true, sProvider_) ;
    System.out.println("==>categoryId2:" + question2.getCategoryId());
    
//	Add question 3
		Question question3 = createQuestion() ;
		question3.setRelations(new String[]{}) ;
		question3.setResponses(" ") ;
		question3.setApproved(true) ;
		question3.setDateResponse(new Date()) ;
		question3.setActivated(true) ;
		question3.setLanguage("English") ;
		question3.setAuthor("Phung Hai Nam") ;
		question3.setEmail("phunghainam@yahoo.com") ;
		question3.setQuestion("Nguyen van truong test question 33333333") ;
		question3.setCreatedDate(new Date()) ;
    faqService_.saveQuestion(question3, true, sProvider_) ;
		
//	Add question 4
		Question question4 = createQuestion() ;
		question4.setRelations(new String[]{}) ;
		question4.setResponses(" ") ;
		question4.setApproved(false) ;
		question4.setDateResponse(new Date()) ;
		question4.setActivated(true) ;
		question4.setLanguage("English") ;
		question4.setAuthor("Pham Dinh Tan") ;
		question4.setEmail("phamdinhtan@yahoo.com") ;
		question4.setQuestion("Nguyen van truong test question 44444444") ;
		question4.setCreatedDate(new Date()) ;
    faqService_.saveQuestion(question4, true, sProvider_) ;
    
//	Add question 5
		Question question5 = createQuestion() ;
		question5.setRelations(new String[]{}) ;
		question5.setResponses(" ") ;
		question5.setApproved(false) ;
		question5.setDateResponse(new Date()) ;
		question5.setActivated(true) ;
		question5.setLanguage("English") ;
		question5.setAuthor("Ly Dinh Quang") ;
		question5.setEmail("lydinhquang@yahoo.com") ;
		question5.setQuestion("Nguyen van truong test question 5555555") ;
		question5.setCreatedDate(new Date()) ;
    faqService_.saveQuestion(question5, true, sProvider_) ;
    
//    response question 2
    question2.setDateResponse(new Date()) ;
    question2.setResponses("Nguyen van truong test response 22222222 ") ;//Nguyen van truong test response 22222222
    question2.setRelations(new String[]{"Nguyen van truong test question 33333333"}) ;
    faqService_.saveQuestion(question2, false, sProvider_) ;
		
//    move question 3 to category(question1.getCategoryId())
    question3.setCategoryId(question1.getCategoryId()) ;
    faqService_.saveQuestion(question3, false, sProvider_) ;
    
//    get question by category
    List<Question> listQuestionByCategory = faqService_.getQuestionsByCatetory(question1.getCategoryId(), sProvider_).getAll();
    Category categoryQuestion1 = faqService_.getCategoryById(question1.getCategoryId(), sProvider_) ;
    System.out.println("\n\n List question in category " + categoryQuestion1.getName());
    for(Question ques: listQuestionByCategory) {
    	System.out.println("==" + ques.getQuestion()+"=="+ques.getResponses()+"=="+ques.getRelations().length+"=="+ ques.getCreatedDate());
    }
    
//    get list question not yet answer
    List<Question> listQuestionNotYes = faqService_.getQuestionsNotYetAnswer(sProvider_).getAll();
    System.out.println("\n\n List question not yet answer: ");
    for(Question ques: listQuestionNotYes) {
    	List<String> listRelations = new ArrayList<String>() ;
    	String[] relations = ques.getRelations();
    	if(relations.length > 0){ 
		  	for(String string: relations) {
		  		listRelations.add(string);
		  	}
    	}
    	System.out.println("==" + ques.getQuestion()+"=="+ques.getResponses()+"=="+listRelations+"=="+ ques.getCreatedDate());
    }
    
//  move category
    faqService_.moveCategory(question4.getCategoryId(), question1.getCategoryId(), sProvider_) ;
    faqService_.moveCategory(question5.getCategoryId(), question1.getCategoryId(), sProvider_) ;
    List<Category> listSubCate = faqService_.getSubCategories( question1.getCategoryId(), sProvider_) ;
    System.out.println("\n\n List Category when move in funtion testQuestion:");
  	for(Category cate : listSubCate) {
  		System.out.println(cate.getName()+"=="+cate.getDescription()+"=="+cate.getModerators()+"=="+cate.isModerateQuestions()+"=="+cate.getCreatedDate());
  	}
    
//		get all question
//    List<Question> listAllQuestion = faqService_.getAllQuestions(sProvider_).getAll() ;
//    for(Question ques: listAllQuestion) {
//    	System.out.println("==" + ques.getQuestion()+"=="+ques.getResponses()+"=="+ques.getRelations());
//    	faqService_.removeQuestion(question1.getId(), sProvider_);
//    }
    
	}
	
}