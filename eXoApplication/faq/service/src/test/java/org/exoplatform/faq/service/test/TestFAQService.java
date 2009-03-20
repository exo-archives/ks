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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQEventQuery;
import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.FileAttachment;
import org.exoplatform.faq.service.JCRPageList;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Watch;
import org.exoplatform.faq.service.impl.JCRDataStorage;
import org.exoplatform.faq.service.impl.MultiLanguages;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.mail.Message;

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

	private static String  USER_ROOT = "root";
	private static String  USER_JOHN = "john";
	private static String  USER_DEMO = "demo";
	private JCRDataStorage datastorage;

	public TestFAQService() throws Exception {
		super();
	}

	public void setUp() throws Exception {
		super.setUp();
		faqService_ = (FAQService) container.getComponentInstanceOfType(FAQService.class);
		datastorage = (JCRDataStorage) container.getComponentInstanceOfType(JCRDataStorage.class);
		SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
		sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
		faqSetting_.setDisplayMode("both");
		faqSetting_.setOrderBy("created");
		faqSetting_.setOrderType("asc") ;
		faqSetting_.setSortQuestionByVote(true);
	}

	public void testFAQService() throws Exception {
		assertNotNull(faqService_) ;
		assertNotNull(sProvider_) ;
	}

	public Category createCategory(String categoryName) {
		Date date = new Date() ;
		Category category = new Category() ;
		category.setName(categoryName) ;
		category.setDescription("Description") ;
		category.setModerateQuestions(true) ;
		category.setModerateAnswers(true);
		category.setViewAuthorInfor(true);
		category.setModerators(new String[]{"root"}) ;
		category.setCreatedDate(date) ;
		return category ;
	}

	public Question createQuestion(Category cate) throws Exception {
		Question question = new Question() ;
		question.setLanguage("English") ;
		question.setQuestion("What is FAQ?");
		question.setDetail("Add new question 1") ;
		question.setAuthor("root") ;
		question.setEmail("maivanha1610@gmail.com") ;
		question.setActivated(true) ;
		question.setApproved(true) ;
		question.setCreatedDate(new Date()) ;
		question.setCategoryId(cate.getId()) ;
		question.setRelations(new String[]{}) ;
		question.setAttachMent(listAttachments) ;
		question.setAnswers(new Answer[]{});
		question.setComments(new Comment[]{});
		question.setUsersVote(new String[]{});
		question.setMarkVote(0.0);
		question.setUsersWatch(new String[]{});
		question.setEmail(new String());
		question.setEmailsWatch(new String[]{});
		question.setTopicIdDiscuss(null);
		return question ;
	}

	private QuestionLanguage createQuestionLanguage(String language){
		QuestionLanguage questionLanguage = new QuestionLanguage();
		questionLanguage.setAnswers(null);
		questionLanguage.setComments(null);
		questionLanguage.setDetail("detail for language " + language);
		questionLanguage.setLanguage(language);
		questionLanguage.setQuestion("question for language " + language);
		return questionLanguage;
	}

	private Answer createAnswer(String user, String content){
		Answer answer = new Answer();
		answer.setActivateAnswers(true);
		answer.setApprovedAnswers(true);
		answer.setDateResponse(new Date());
		answer.setMarksVoteAnswer(0);
		answer.setMarkVotes(0);
		answer.setNew(true);
		answer.setPostId(null);
		answer.setResponseBy(user);
		answer.setResponses(content);
		answer.setUsersVoteAnswer(null);
		return answer;
	}

	private Comment createComment(String user, String content){
		Comment comment = new Comment();
		comment.setCommentBy(user);
		comment.setComments(content);
		comment.setDateComment(new Date());
		comment.setNew(true);
		comment.setPostId(null);
		return comment;
	}

	private InputStream createQuestionToImport(String questionId){
		InputStream is = null;
		String data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<sv:node xmlns:nt=\"http://www.jcp.org/jcr/nt/1.0\" xmlns:rep=\"internal\" xmlns:sv=\"http://www.jcp.org/jcr/sv/1.0\" " +
		"xmlns:mix=\"http://www.jcp.org/jcr/mix/1.0\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" " +
		"xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:fn_old=\"http://www.w3.org/2004/10/xpath-functions\" " +
		"xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" " +
		"xmlns:webdav=\"http://www.exoplatform.org/jcr/webdav\" xmlns:exo=\"http://www.exoplatform.com/jcr/exo/1.0\" " +
		"sv:name=\""+questionId+"\"><sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\">" +
		"<sv:value>exo:faqQuestion</sv:value></sv:property><sv:property sv:name=\"jcr:mixinTypes\" sv:type=\"Name\">" +
		"<sv:value>mix:faqi18n</sv:value></sv:property><sv:property sv:name=\"exo:author\" sv:type=\"String\"><sv:value>root</sv:value>" +
		"</sv:property><sv:property sv:name=\"exo:categoryId\" sv:type=\"String\"><sv:value>null</sv:value></sv:property>" +
		"<sv:property sv:name=\"exo:createdDate\" sv:type=\"Date\"><sv:value>2009-03-16T14:06:25.843+07:00</sv:value>" +
		"</sv:property><sv:property sv:name=\"exo:email\" sv:type=\"String\"><sv:value>root@localhost.com</sv:value></sv:property>" +
		"<sv:property sv:name=\"exo:isActivated\" sv:type=\"Boolean\"><sv:value>true</sv:value></sv:property><sv:property " +
		"sv:name=\"exo:isApproved\" sv:type=\"Boolean\"><sv:value>true</sv:value></sv:property><sv:property sv:name=\"exo:language\" " +
		"sv:type=\"String\"><sv:value>English</sv:value></sv:property><sv:property sv:name=\"exo:markVote\" sv:type=\"Double\">" +
		"<sv:value>0.0</sv:value></sv:property><sv:property sv:name=\"exo:name\" sv:type=\"String\"><sv:value>&lt;p&gt;detail " +
		"for this question&lt;/p&gt;</sv:value></sv:property><sv:property sv:name=\"exo:relatives\" sv:type=\"String\"></sv:property>" +
		"<sv:property sv:name=\"exo:title\" sv:type=\"String\"><sv:value>new question 1</sv:value></sv:property><sv:node " +
		"sv:name=\"faqAnswerHome\"><sv:property sv:name=\"jcr:primaryType\" sv:type=\"Name\"><sv:value>nt:unstructured</sv:value>" +
		"</sv:property><sv:node sv:name=\"Answer0e1a8845c0a8013100c543662af2545d\"><sv:property sv:name=\"jcr:primaryType\" " +
		"sv:type=\"Name\"><sv:value>exo:answer</sv:value></sv:property><sv:property sv:name=\"exo:MarkVotes\" sv:type=\"Long\">" +
		"<sv:value>0</sv:value></sv:property><sv:property sv:name=\"exo:activateResponses\" sv:type=\"Boolean\"><sv:value>true" +
		"</sv:value></sv:property><sv:property sv:name=\"exo:approveResponses\" sv:type=\"Boolean\"><sv:value>true</sv:value>" +
		"</sv:property><sv:property sv:name=\"exo:dateResponse\" sv:type=\"Date\"><sv:value>2009-03-16T14:06:41.109+07:00</sv:value>" +
		"</sv:property><sv:property sv:name=\"exo:id\" sv:type=\"String\"><sv:value>Answer0e1a8845c0a8013100c543662af2545d</sv:value>" +
		"</sv:property><sv:property sv:name=\"exo:responseBy\" sv:type=\"String\"><sv:value>root</sv:value></sv:property>" +
		"<sv:property sv:name=\"exo:responses\" sv:type=\"String\"><sv:value>&lt;p&gt;new answer 1&lt;/p&gt;</sv:value>" +
		"</sv:property></sv:node></sv:node></sv:node>";
		try {
			is = new ByteArrayInputStream(data.getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return is;
	}

	private Watch createNewWatch(String user, String mail){
		Watch watch = new Watch();
		watch.setUser(user);
		watch.setEmails(mail);
		return watch;
	}
	
	private FileAttachment createUserAvatar(String fileName) throws Exception{
		FileAttachment attachment = new FileAttachment();
		try {
			File file =  new File("../service/src/test/java/conf/portal/defaultAvatar.jpg");
			Writer writer = new BufferedWriter(new FileWriter(file));
			writer.write("noi dung cua file");
			writer.close();
		
			attachment.setName(fileName);
			InputStream is = new FileInputStream(file);
			attachment.setInputStream(is);
			attachment.setMimeType("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attachment;
	}

	public void testCategory() throws Exception {
//		add category Id	
		Category cate1 = createCategory("Cate 1") ;
		faqService_.saveCategory(null, cate1, true, sProvider_) ;

		Category cate2 = createCategory("Cate 2") ;
		cate2.setName("Nguyen van truong test category222222") ;
		cate2.setModerators(new String[]{"Demo"}) ;
		faqService_.saveCategory(null, cate2, true, sProvider_) ;

		assertNotNull(faqService_.getCategoryById(cate1.getId(), sProvider_)) ;
//		get infor of root category:
		assertEquals(faqService_.getCategoryInfo(null, sProvider_)[0], 2);

//		Check category is already exist
		assertEquals(faqService_.categoryAlreadyExist(cate1.getId(), sProvider_), true);

//		Get path of category
		assertNotNull(faqService_.getCategoryPath(sProvider_, cate1.getId()));

//		Swap 2 category
		assertEquals(faqService_.getCategoryById(cate1.getId(), sessionProvider).getIndex(), 2);
		assertEquals(faqService_.getCategoryById(cate2.getId(), sessionProvider).getIndex(), 1);
		faqService_.swapCategories(null, cate1.getId(), cate2.getId(), sessionProvider);
		assertEquals(faqService_.getCategoryById(cate1.getId(), sessionProvider).getIndex(), 1);
		assertEquals(faqService_.getCategoryById(cate2.getId(), sessionProvider).getIndex(), 2);

//		add sub category 1
		Category subCate1 = createCategory("Sub Cate 1") ;
		subCate1.setName("Nguyen van truong test Sub category 1") ;
		subCate1.setModerators(new String[]{"marry","Demo"}) ;
		faqService_.saveCategory(cate1.getId(), subCate1, true, sProvider_) ;

//		update category 
		cate1.setName("Nguyen van truong test category111111") ;
		cate1.setCreatedDate(new Date()) ;
		faqService_.saveCategory(null, cate1, false, sProvider_);
		assertNotNull(cate1) ;
		assertEquals("Nguyen van truong test category111111", cate1.getName());

//		get Categories
		List<Category> listCate = faqService_.getSubCategories(null, sProvider_, faqSetting_) ;
		assertEquals(listCate.size(), 2) ;

		assertEquals(faqService_.getMaxindexCategory(null, sessionProvider), 2);

//		get sub category
		List<Category> listSubCate = faqService_.getSubCategories(cate1.getId(), sProvider_, faqSetting_) ;
		assertEquals(listSubCate.size(), 1) ;

//		update sub category 
		subCate1.setName("Sub category 1") ;
		subCate1.setCreatedDate(new Date()) ;
		faqService_.saveCategory(cate1.getId(), subCate1, false, sProvider_);
		assertNotNull(subCate1) ;
		assertEquals("Sub category 1", subCate1.getName());

//		get all Category 
		List<Category> listAll = faqService_.getAllCategories(sProvider_) ;
		assertEquals(listAll.size(), 3) ;

//		move category 
		faqService_.moveCategory(cate2.getId(), cate1.getId(), sProvider_) ;
		assertNotNull(faqService_.getCategoryById(cate2.getId(), sProvider_)) ;

//		Delete category 2
		faqService_.removeCategory(cate2.getId(), sProvider_) ;

//		get all Category 
		List<Category> listAllAfterRemove = faqService_.getAllCategories(sProvider_) ;
		assertEquals(listAllAfterRemove.size(), 2) ;

//		get list category by moderator
		List<String> listCateByModerator = faqService_.getListCateIdByModerator(USER_ROOT, sProvider_);
		assertEquals(listCateByModerator.size(), 1);

	}

	public void testQuestion() throws Exception {
		Category cate = createCategory("Category to test question") ;
		try{
			faqService_.saveCategory(null, cate, true, sProvider_) ;
		} catch (Exception e){
			e.printStackTrace();
		}

		Question question1 = createQuestion(cate) ;
//		save question 1
		faqService_.saveQuestion(question1, true, sProvider_,faqSetting_) ;

//		get question 1
		assertNotNull(faqService_.getQuestionById(question1.getId(), sProvider_)) ;
		List<Question> listQuestion = faqService_.getQuestionsNotYetAnswer(sProvider_, "All").getAll() ;
		assertEquals(listQuestion.size(), 0) ; 

//		update question 1
		question1.setDetail("Nguyen van truong test question 11111111 ?") ;
		faqService_.saveQuestion(question1, false, sProvider_,faqSetting_) ;
		assertNotNull(question1) ;
		assertEquals("Nguyen van truong test question 11111111 ?", question1.getDetail());

//		Add question 2
		Question question2 = createQuestion(cate) ;
		question2.setRelations(new String[]{}) ;
		//question2.setResponses(new String[]{" "}) ;
		question2.setApproved(true) ;
		//question2.setDateResponse(new Date[]{new Date()}) ;
		question2.setActivated(true) ;
		question2.setLanguage("English") ;
		question2.setAuthor("Mai Van Ha") ;
		question2.setEmail("truong_tb1984@yahoo.com") ;
		question2.setDetail("Nguyen van truong test question 2222222 ?") ;
		question2.setCreatedDate(new Date()) ;
		faqService_.saveQuestion(question2, true, sProvider_,faqSetting_) ;

//		move question 2 to category of question 1
		List<String> listQues = new ArrayList<String>() ;
		listQues.add(question2.getId());
		faqService_.moveQuestions(listQues, question1.getCategoryId(), sProvider_);
		assertNotNull(faqService_.getQuestionById(question2.getId(), sProvider_)) ;

//		Add question 3
		Question question3 = createQuestion(cate) ;
		question3.setRelations(new String[]{}) ;
		//question3.setResponses(new String[]{" "}) ;
		question3.setApproved(true) ;
		//question3.setDateResponse(new Date[]{new Date()}) ;
		question3.setActivated(true) ;
		question3.setLanguage("English") ;
		question3.setAuthor("Phung Hai Nam") ;
		question3.setEmail("phunghainam@yahoo.com") ;
		question3.setDetail("Nguyen van truong test question 33333333 nguyenvantruong ?") ;
		question3.setCreatedDate(new Date()) ;
		faqService_.saveQuestion(question3, true, sProvider_,faqSetting_) ;

//		Add question 4
		Question question4 = createQuestion(cate) ;
		question4.setRelations(new String[]{}) ;
		//question4.setResponses(new String[]{" "}) ;
		question4.setApproved(false) ;
		//question4.setDateResponse(new Date[]{new Date()}) ;
		question4.setActivated(true) ;
		question4.setLanguage("English") ;
		question4.setAuthor("Pham Dinh Tan") ;
		question4.setEmail("phamdinhtan@yahoo.com") ;
		question4.setDetail("Nguyen van truong test question nguyenvantruong ?") ;
		question4.setCreatedDate(new Date()) ;
		faqService_.saveQuestion(question4, true, sProvider_,faqSetting_) ;

//		Add question 5
		Question question5 = createQuestion(cate) ;
		question5.setRelations(new String[]{}) ;
		//question5.setResponses(new String[]{" "}) ;
		question5.setApproved(false) ;
		//question5.setDateResponse(new Date[]{new Date()}) ;
		question5.setActivated(true) ;
		question5.setLanguage("English") ;
		question5.setAuthor("Ly Dinh Quang") ;
		question5.setEmail("lydinhquang@yahoo.com") ;
		question5.setDetail("Nguyen van truong test question 55555555555 ?") ;
		question5.setCreatedDate(new Date()) ;
		faqService_.saveQuestion(question5, true, sProvider_,faqSetting_) ;

//	Get question by list category
		JCRPageList pageList = faqService_.getQuestionsByListCatetory(Arrays.asList(new String[]{cate.getId()}), false, sProvider_);
		pageList.setPageSize(10);
		assertEquals(pageList.getPage(1, null).size(), 5);

//		get list all question
		List<Question> listAllQuestion = faqService_.getAllQuestions(sProvider_).getAll();
		assertEquals(listAllQuestion.size(), 5) ;

//		get list question by category of question 1
		List<Question> listQuestionByCategory = faqService_.getQuestionsByCatetory(question1.getCategoryId(), sProvider_, faqSetting_).getAll() ;
		assertEquals(listQuestionByCategory.size(), 5) ;

//		Get list paths of all question in category
		List<String> listPaths = faqService_.getListPathQuestionByCategory(cate.getId(), sessionProvider);
		assertEquals(listPaths.size(), 5);

//		Get question node by id
		assertNotNull(faqService_.getQuestionNodeById(question1.getId(), sessionProvider));

//		remove question
		faqService_.removeQuestion(question5.getId(), sProvider_);
		List<Question> listAllQuestionAfterRemove = faqService_.getAllQuestions(sProvider_).getAll();
		assertEquals(listAllQuestionAfterRemove.size(), 4) ;
	}

	public void testSearch() throws Exception {

//		quick search with text = "test"
		List<FAQFormSearch> listQuickSearch = faqService_.getAdvancedEmpty(sProvider_, "test", null, null) ;
		assertEquals(listQuickSearch.size(), 6) ;

//		search all category and question in database
		List<FAQFormSearch> listSearchAll = faqService_.getAdvancedEmpty(sProvider_, "", null, null) ;
		assertEquals(listSearchAll.size(), 7) ;

//		advance search all category in database
		FAQEventQuery eventQueryCategory = new FAQEventQuery() ;
		eventQueryCategory.setType("faqCategory");
		List<Category> listAllCategroy = faqService_.getAdvancedSearchCategory(sProvider_, eventQueryCategory) ;
		assertEquals(listAllCategroy.size(), 3) ;

//		advance search with category name = "Sub"
		FAQEventQuery eventQuerySub = new FAQEventQuery() ;
		eventQuerySub.setType("faqCategory");
		eventQuerySub.setName("Sub") ;
		List<Category> listAllSub = faqService_.getAdvancedSearchCategory(sProvider_, eventQuerySub) ;
		assertEquals(listAllSub.size(), 1) ;

//		advance search all question in database
		FAQEventQuery eventQueryQuestion = new FAQEventQuery() ;
		eventQueryQuestion.setType("faqQuestion");
		List<Question> listAllQuestion = faqService_.getAdvancedSearchQuestion(sProvider_, eventQueryQuestion) ;
		assertEquals(listAllQuestion.size(), 0) ;


//		advance search with category name = "Sub"
		FAQEventQuery eventQueryAdvanceQuestion = new FAQEventQuery() ;
		eventQueryAdvanceQuestion.setType("faqQuestion");
		eventQueryAdvanceQuestion.setQuestion("nguyenvantruong") ;
		List<Question> listSearchAdvanceQuestion = faqService_.getAdvancedSearchQuestion(sProvider_, eventQueryAdvanceQuestion) ;
		assertEquals(listSearchAdvanceQuestion.size(), 2) ;
	}

	public void testAnswerComment() throws Exception{
		Category cate = createCategory("category to test answer");
		faqService_.saveCategory(null, cate, true, sProvider_);
		Question question = createQuestion(cate);
		faqService_.saveQuestion(question, true, sProvider_, faqSetting_);
		Answer answer1 = createAnswer(USER_ROOT, "Root answer 1 for question");
		Answer answer2 = createAnswer(USER_DEMO, "Demo answer 2 for question");

		Comment comment1 = createComment(USER_ROOT, "Root comment 1 for question");
		Comment comment2 = createComment(USER_DEMO, "Demo comment 2 for question");

//		Save answer:
		faqService_.saveAnswer(question.getId(), new Answer[]{answer1, answer2}, sProvider_);

//		Get answer by id:
		assertNotNull(faqService_.getAnswerById(question.getId(), answer2.getId(), sProvider_));

//		Update answers:
		assertEquals(answer1.getResponses(), "Root answer 1 for question");
		answer1.setResponses("Root answer 1 for question edit");
		faqService_.saveAnswer(question.getId(), answer1, false, sProvider_);
		assertEquals(faqService_.getAnswerById(question.getId(), answer1.getId(), sProvider_).getResponses(), 
		"Root answer 1 for question edit");

//		Get all answers of question:
		JCRPageList pageList = faqService_.getPageListAnswer(sProvider_, question.getId(), null);
		pageList.setPageSize(10);
		assertEquals(pageList.getPageItem(0).size(), 2);

//		Delete answer
		faqService_.deleteAnswer(question.getId(), answer1.getId(), sProvider_);
		pageList = faqService_.getPageListAnswer(sProvider_, question.getId(), null);
		pageList.setPageSize(10);
		assertEquals(pageList.getPageItem(0).size(), 1);

//		Save comment
		faqService_.saveComment(question.getId(), comment1, true, sProvider_);
		faqService_.saveComment(question.getId(), comment2, true, sProvider_);

//		Get comment by Id:
		assertNotNull(faqService_.getCommentById(sProvider_, question.getId(), comment1.getId()));
		assertNotNull(faqService_.getCommentById(sProvider_, question.getId(), comment2.getId()));

//		Get all comment of question
		pageList = faqService_.getPageListComment(sProvider_, question.getId());
		pageList.setPageSize(10);
		assertEquals(pageList.getPageItem(0).size(), 2);

//		Delete comment by id
		faqService_.deleteComment(question.getId(), comment1.getId(), sProvider_);
		pageList = faqService_.getPageListComment(sProvider_, question.getId());
		pageList.setPageSize(10);
		assertEquals(pageList.getPageItem(0).size(), 1);

//		Generate RSS
		faqService_.generateRSS(faqService_.getQuestionNodeById(question.getId(), sProvider_).getPath(), 1);

//		Get RSS node
		assertNotNull(faqService_.getRSSNode(sProvider_, cate.getId()));
	}

	public void testImportExport() throws Exception{
		Question question = new Question();
		faqService_.importData(null, faqService_.getCategoryNodeById(null, sProvider_).getSession(), createQuestionToImport(question.getId()), false, sProvider_);
		assertNotNull(faqService_.getQuestionById(question.getId(), sProvider_));
	}

	public void testWatch() throws Exception {
		Category cateWatch = createCategory("Cate to test watch") ;
		cateWatch.setName("test cate add watch") ;
		faqService_.saveCategory(null, cateWatch, true, sProvider_) ;

		List<Watch> listWatchs = new ArrayList<Watch>();
//		add  watch
		faqService_.addWatch(cateWatch.getId(), createNewWatch(USER_ROOT, "maivanha1610@gmail.com"), sProvider_) ;
		faqService_.addWatch(cateWatch.getId(), createNewWatch(USER_DEMO, "maivanha1610@yahoo.com"), sProvider_) ;
		faqService_.addWatch(cateWatch.getId(), createNewWatch(USER_JOHN, "john@localhost.com"), sProvider_) ;

//		get email watch		
		JCRPageList pageList = faqService_.getListMailInWatch(cateWatch.getId(), sProvider_) ;
		pageList.setPageSize(5);
		listWatchs.addAll(pageList.getPageListWatch(1, USER_ROOT));
		assertEquals(listWatchs.size(), 3) ;

//		Delete email watch
		faqService_.deleteMailInWatch(cateWatch.getId(), sProvider_, "john@localhost.com");
		pageList = faqService_.getListMailInWatch(cateWatch.getId(), sProvider_) ;
		pageList.setPageSize(5);
		assertEquals(pageList.getPageListWatch(1, USER_ROOT).size(), 2) ;

//		Check category is watched by user
		assertEquals(faqService_.getWatchByUser(USER_ROOT, cateWatch.getId(), sProvider_), true);

//		get all categories are watched by user
		assertNotNull(faqService_.getListCategoriesWatch(USER_ROOT, sProvider_));

//		UnWatch category
		faqService_.UnWatch(cateWatch.getId(), sProvider_, USER_DEMO);
		pageList = faqService_.getListMailInWatch(cateWatch.getId(), sProvider_) ;
		pageList.setPageSize(5);
		assertEquals(pageList.getPageListWatch(1, USER_ROOT).size(), 1) ;
	}

	public void testMultilanguage() throws Exception{
		Category category = createCategory("Cateogry to test multilanguage");
		faqService_.saveCategory(null, category, true, sProvider_);
		Question question = createQuestion(category);
		faqService_.saveQuestion(question, true, sProvider_, faqSetting_);

//		Add question language for question
		MultiLanguages multiLanguages = new MultiLanguages();
		multiLanguages.addLanguage(faqService_.getQuestionNodeById(question.getId(), sProvider_), createQuestionLanguage("Viet Nam"));
		multiLanguages.addLanguage(faqService_.getQuestionNodeById(question.getId(), sProvider_), createQuestionLanguage("French"));

//		Get all question language :
		assertEquals(faqService_.getQuestionLanguages(question.getId(), sProvider_).size(), 2);
		
//		Search question language 
		assertEquals(faqService_.searchQuestionByLangageOfText(faqService_.getQuestionsByCatetory(category.getId(), 
																																							sProvider_, faqSetting_).getAll(),
																													"Viet Nam", "Viet Nam", sProvider_).size(),1);
		assertEquals(faqService_.searchQuestionByLangage(faqService_.getQuestionsByCatetory(category.getId(), 
				sProvider_, faqSetting_).getAll(),
				"Viet Nam", "Viet Nam", null, sProvider_).size(),1);
	}

	public void testUserSetting() throws Exception {
//		set userSetting information into user node
		faqSetting_.setDisplayMode("both");
		faqSetting_.setOrderBy("created");
		faqSetting_.setOrderType("asc") ;
		assertEquals(faqSetting_.getOrderBy(), "created");
		assertEquals(faqSetting_.getOrderType(), "asc");
		faqService_.getUserSetting(sessionProvider, USER_ROOT, faqSetting_);

//		get all userSetting information from user node and set for FAQSetting object
		FAQSetting setting = new FAQSetting();
		setting.setOrderBy(null);
		setting.setOrderType(null);
		assertNull(setting.getOrderBy());
		assertNull(setting.getOrderType());
		faqService_.getUserSetting(sessionProvider, USER_ROOT, setting);
		assertEquals(setting.getOrderBy(), "created");
		assertEquals(setting.getOrderType(), "asc");

//		update userSetting information in to user node
		setting.setSortQuestionByVote(false);
		setting.setOrderBy("alpha");
		setting.setOrderType("des");
		faqService_.saveFAQSetting(setting, USER_ROOT, sessionProvider);
		assertEquals(faqSetting_.getOrderBy(), "created");
		assertEquals(faqSetting_.getOrderType(), "asc");
		faqService_.getUserSetting(sessionProvider, USER_ROOT, faqSetting_);
		assertEquals(faqSetting_.getOrderBy(), "alpha");
		assertEquals(faqSetting_.getOrderType(), "des");
		
//	Get all admins of FAQ
		List<String> list = faqService_.getAllFAQAdmin();
		assertNotNull(list);
		assertEquals(faqService_.isAdminRole(USER_DEMO, sessionProvider), false);
/*
//	Test send mail for user:
		Message  message = new Message(); 
    message.setMimeType("text/htm") ;
    message.setFrom("maivanha1610@yahoo.com") ;
    message.setTo("maivanha1610@gmail.com") ;
    message.setSubject("Test send mail") ;
    message.setBody("run JUnit test") ;
    try {
    	faqService_.sendMessage(message) ;
    } catch(Exception e) {
    	e.printStackTrace();
    }*/
	}
	
	public void testUserAvatar()throws Exception{
		//	Add new avatar for user:
		faqService_.saveUserAvatar(USER_ROOT, createUserAvatar("rootAvatar"), sProvider_);
		
		//	Get user avatar 
		assertNotNull(faqService_.getUserAvatar(USER_ROOT, sProvider_));
		
		//	Set default avartar for user
		faqService_.setDefaultAvatar(USER_ROOT, sProvider_);
		assertNull(faqService_.getUserAvatar(USER_ROOT, sProvider_));
	}
}