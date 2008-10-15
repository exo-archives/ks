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
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
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
	private FAQSetting faqSetting_ = new FAQSetting();
	private SessionProvider sProvider_ ;
	private List<FileAttachment> listAttachments = new ArrayList<FileAttachment>() ;
	public void setUp() throws Exception {
    super.setUp() ;
    faqService_ = (FAQService) container.getComponentInstanceOfType(FAQService.class) ;
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
    faqSetting_.setDisplayMode("both");
    faqSetting_.setOrderBy("created");
    faqSetting_.setOrderType("asc") ;
  }
	
	public void testFAQService() throws Exception {
		assertNotNull(faqService_) ;
  	assertNotNull(sProvider_) ;
	}
  
	public void testCategory() throws Exception {
		Category cate1 = createCategory() ;
	faqService_.saveCategory(null, cate1, true, sProvider_) ;
//add category Id	
	assertNotNull(faqService_.getCategoryById(cate1.getId(), sProvider_)) ;
	
//	get Categories
	List<Category> listCate = faqService_.getSubCategories(null, sProvider_, faqSetting_) ;
	assertEquals(listCate.size(), 1) ;
	
//update category 
	cate1.setName("Nguyen van truong test category111111") ;
	cate1.setCreatedDate(new Date()) ;
	faqService_.saveCategory(null, cate1, false, sProvider_);
	assertNotNull(cate1) ;
	assertEquals("Nguyen van truong test category111111", cate1.getName());
	
//	add category 2
	Category cate2 = createCategory() ;
	cate2.setName("Nguyen van truong test category222222") ;
	cate2.setModerators(new String[]{"Demo"}) ;
	faqService_.saveCategory(null, cate2, true, sProvider_) ;
	
//	add sub category 1
	Category subCate1 = createCategory() ;
	subCate1.setName("Nguyen van truong test Sub category 1") ;
	subCate1.setModerators(new String[]{"marry","Demo"}) ;
	faqService_.saveCategory(cate1.getId(), subCate1, true, sProvider_) ;
	
//	get sub category
	List<Category> listSubCate = faqService_.getSubCategories(cate1.getId(), sProvider_, faqSetting_) ;
	assertEquals(listSubCate.size(), 1) ;

//update sub category 
	subCate1.setName("Sub category 1") ;
	subCate1.setCreatedDate(new Date()) ;
	faqService_.saveCategory(cate1.getId(), subCate1, false, sProvider_);
	assertNotNull(subCate1) ;
	assertEquals("Sub category 1", subCate1.getName());
	
//	get all Category 
	List<Category> listAll = faqService_.getAllCategories(sProvider_) ;
	assertEquals(listAll.size(), 3) ;
	
//move category 
	faqService_.moveCategory(cate2.getId(), cate1.getId(), sProvider_) ;
	assertNotNull(faqService_.getCategoryById(cate2.getId(), sProvider_)) ;
	
//	Delete category 2
	faqService_.removeCategory(cate2.getId(), sProvider_) ;
	
//	get all Category 
	List<Category> listAllAfterRemove = faqService_.getAllCategories(sProvider_) ;
	assertEquals(listAllAfterRemove.size(), 2) ;
	
//	get list category by moderator
	List<String> listCateByModerator = faqService_.getListCateIdByModerator("root", sProvider_);
	assertEquals(listCateByModerator.size(), 1);
	
	
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
	}
	 
	public Question createQuestion() throws Exception {
		Category cate = createCategory() ;
		faqService_.saveCategory(null, cate, true, sProvider_) ;
		Question question = new Question() ;
		question.setCategoryId(cate.getId()) ;
		question.setRelations(new String[]{}) ;
		question.setResponses(new String[]{" "}) ;
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
		faqService_.saveQuestion(question1, true, sProvider_,faqSetting_) ;

//		get question 1
		assertNotNull(faqService_.getQuestionById(question1.getId(), sProvider_)) ;
		List<Question> listQuestion = faqService_.getQuestionsNotYetAnswer(sProvider_).getAll() ;
		assertEquals(listQuestion.size(), 1) ; 
		
//		update question 1
		question1.setQuestion("Nguyen van truong test question 11111111 ?") ;
		faqService_.saveQuestion(question1, false, sProvider_,faqSetting_) ;
		assertNotNull(question1) ;
		assertEquals("Nguyen van truong test question 11111111 ?", question1.getQuestion());
		
//		Add question 2
		Question question2 = createQuestion() ;
		question2.setRelations(new String[]{}) ;
		question2.setResponses(new String[]{" "}) ;
    question2.setApproved(true) ;
    question2.setDateResponse(new Date()) ;
    question2.setActivated(true) ;
    question2.setLanguage("English") ;
    question2.setAuthor("Mai Van Ha") ;
    question2.setEmail("truong_tb1984@yahoo.com") ;
    question2.setQuestion("Nguyen van truong test question 2222222 ?") ;
    question2.setCreatedDate(new Date()) ;
    faqService_.saveQuestion(question2, true, sProvider_,faqSetting_) ;
    
//    move question 2 to category of question 1
    List<String> listQues = new ArrayList<String>() ;
    listQues.add(question2.getId());
    faqService_.moveQuestions(listQues, question1.getCategoryId(), sProvider_);
    assertNotNull(faqService_.getQuestionById(question2.getId(), sProvider_)) ;
    
//	Add question 3
		Question question3 = createQuestion() ;
		question3.setRelations(new String[]{}) ;
		question3.setResponses(new String[]{" "}) ;
		question3.setApproved(true) ;
		question3.setDateResponse(new Date()) ;
		question3.setActivated(true) ;
		question3.setLanguage("English") ;
		question3.setAuthor("Phung Hai Nam") ;
		question3.setEmail("phunghainam@yahoo.com") ;
		question3.setQuestion("Nguyen van truong test question 33333333 nguyenvantruong ?") ;
		question3.setCreatedDate(new Date()) ;
    faqService_.saveQuestion(question3, true, sProvider_,faqSetting_) ;
		
//	Add question 4
		Question question4 = createQuestion() ;
		question4.setRelations(new String[]{}) ;
		question4.setResponses(new String[]{" "}) ;
		question4.setApproved(false) ;
		question4.setDateResponse(new Date()) ;
		question4.setActivated(true) ;
		question4.setLanguage("English") ;
		question4.setAuthor("Pham Dinh Tan") ;
		question4.setEmail("phamdinhtan@yahoo.com") ;
		question4.setQuestion("Nguyen van truong test question nguyenvantruong ?") ;
		question4.setCreatedDate(new Date()) ;
    faqService_.saveQuestion(question4, true, sProvider_,faqSetting_) ;
    
//	Add question 5
		Question question5 = createQuestion() ;
		question5.setRelations(new String[]{}) ;
		question5.setResponses(new String[]{" "}) ;
		question5.setApproved(false) ;
		question5.setDateResponse(new Date()) ;
		question5.setActivated(true) ;
		question5.setLanguage("English") ;
		question5.setAuthor("Ly Dinh Quang") ;
		question5.setEmail("lydinhquang@yahoo.com") ;
		question5.setQuestion("Nguyen van truong test question 55555555555 ?") ;
		question5.setCreatedDate(new Date()) ;
    faqService_.saveQuestion(question5, true, sProvider_,faqSetting_) ;
    
//    get list all question
    List<Question> listAllQuestion = faqService_.getAllQuestions(sProvider_).getAll();
    assertEquals(listAllQuestion.size(), 5) ;

//  get list question by category of question 1
    FAQSetting setting = new FAQSetting();
    setting.setDisplayMode("both");
  	List<Question> listQuestionByCategory = faqService_.getQuestionsByCatetory(question1.getCategoryId(), sProvider_, setting).getAll() ;
  	assertEquals(listQuestionByCategory.size(), 2) ;

//  	remove question
  	faqService_.removeQuestion(question5.getId(), sProvider_);
  	List<Question> listAllQuestionAfterRemove = faqService_.getAllQuestions(sProvider_).getAll();
    assertEquals(listAllQuestionAfterRemove.size(), 4) ;
	}
	
	public void testWatch() throws Exception {
		Category cateWatch = createCategory() ;
		cateWatch.setName("test cate add watch") ;
		faqService_.saveCategory(null, cateWatch, true, sProvider_) ;
		
//		add  watch
//		faqService_.addWatch(cateWatch.getId(),"root", "truongtb19@gmail.com", sProvider_) ;
//		List<String> emailList = faqService_.getListMailInWatch(cateWatch.getId(), sProvider_) ;

//		get email watch		
//		assertEquals(emailList.size(), 1) ;
//		for(String email : emailList) {
//			assertEquals(email, "truongtb19@gmail.com");
//		}
	}
	
	public void testSearch() throws Exception {
		
//		quick search with text = "test"
		List<FAQFormSearch> listQuickSearch = faqService_.getAdvancedEmpty(sProvider_, "test", null, null) ;
		assertEquals(listQuickSearch.size(), 6) ;
		
//		search all category and question in database
		List<FAQFormSearch> listSearchAll = faqService_.getAdvancedEmpty(sProvider_, "", null, null) ;
		assertEquals(listSearchAll.size(), 13) ;
		
//		advance search all category in database
		FAQEventQuery eventQueryCategory = new FAQEventQuery() ;
		eventQueryCategory.setType("faqCategory");
		List<Category> listAllCategroy = faqService_.getAdvancedSearchCategory(sProvider_, eventQueryCategory) ;
		assertEquals(listAllCategroy.size(), 9) ;
		
//	advance search all question in database
		FAQEventQuery eventQueryQuestion = new FAQEventQuery() ;
		eventQueryQuestion.setType("faqQuestion");
		List<Question> listAllQuestion = faqService_.getAdvancedSearchQuestion(sProvider_, eventQueryQuestion) ;
		assertEquals(listAllQuestion.size(), 4) ;
		
//	advance search with category name = "Sub"
		FAQEventQuery eventQuerySub = new FAQEventQuery() ;
		eventQuerySub.setType("faqCategory");
		eventQuerySub.setName("Sub") ;
		List<Category> listAllSub = faqService_.getAdvancedSearchCategory(sProvider_, eventQuerySub) ;
		assertEquals(listAllSub.size(), 1) ;
		
//	advance search with category name = "Sub"
		FAQEventQuery eventQueryAdvanceQuestion = new FAQEventQuery() ;
		eventQueryAdvanceQuestion.setType("faqQuestion");
		eventQueryAdvanceQuestion.setQuestion("nguyenvantruong") ;
		List<Question> listSearchAdvanceQuestion = faqService_.getAdvancedSearchQuestion(sProvider_, eventQueryAdvanceQuestion) ;
		assertEquals(listSearchAdvanceQuestion.size(), 2) ;
	}
}