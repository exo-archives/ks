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

package org.exoplatform.faq.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * MultiLanguages class allow question and category have multi language.
 * Question content and category's name is can written by one or 
 * more languages. But only default language (only one language is default
 * in system) is set as property of question/ category, 
 * other languages is children of question/category. 
 * 
 * @author  Hung Nguyen Quang
 * @since   Jul 10, 2007
 */

public class MultiLanguages {
  /** The Constant EXO_LANGUAGE. */
  final static public String EXO_LANGUAGE = "exo:language" ;
  
  /** The Constant COMMENTS. */
  final static public String COMMENTS = "comments".intern() ;
  
  /** The Constant JCRCONTENT. */
  final static public String  JCRCONTENT = "jcr:content";
  
  /** The Constant JCRDATA. */
  final static public String  JCRDATA = "jcr:data";
  
  /** The Constant JCR_MIMETYPE. */
  final static public String  JCR_MIMETYPE = "jcr:mimeType";
  
  /** The Constant NTUNSTRUCTURED. */
  final static public String  NTUNSTRUCTURED = "nt:unstructured";
  
  /** The Constant VOTER_PROP. */
  final static String VOTER_PROP = "exo:voter".intern() ;  
  
  /** The Constant VOTING_RATE_PROP. */
  final static String VOTING_RATE_PROP = "exo:votingRate".intern() ;
  
  /** The Constant VOTE_TOTAL_PROP. */
  final static String VOTE_TOTAL_PROP = "exo:voteTotal".intern() ; 
  
  /** The Constant VOTE_TOTAL_LANG_PROP. */
  final static String VOTE_TOTAL_LANG_PROP = "exo:voteTotalOfLang".intern() ;
  
  /** The Constant NODE. */
  final static String NODE = "/node/" ;
  
  /** The Constant NODE_LANGUAGE. */
  final static String NODE_LANGUAGE = "/node/languages/" ;
  
  /** The Constant CONTENT_PATH. */
  final static String CONTENT_PATH = "/node/jcr:content/" ;
  
  /** The Constant TEMP_NODE. */
  final static String TEMP_NODE = "temp" ;
  
  static private Log log = ExoLogger.getLogger(MultiLanguages.class);
  /**
   * Class constructor, instantiates a new multi languages.
   * 
   * @throws Exception the exception
   */
  public MultiLanguages()throws Exception {}  

  
  private static String [] ValuesToStrings(Value[] Val) throws Exception {
		if(Val.length < 1) return new String[]{} ;
		if(Val.length == 1) return new String[]{Val[0].getString()} ;
		String[] Str = new String[Val.length] ;
		for(int i = 0; i < Val.length; ++i) {
			Str[i] = Val[i].getString() ;
		}
		return Str;
	}

  private static List<String> ValuesToList(Value[] Val) throws Exception {
  	List<String> list = new ArrayList<String>();
  	if(Val.length < 1) return list;
  	for(int i = 0; i < Val.length; ++i) {
  		list.add(Val[i].getString() );
  	}
  	return list;
  }
 
  private static Node getLanguageNodeByLanguage(Node questionNode, String language) throws Exception{
  	if(language.equals(questionNode.getProperty("exo:language").getString())) {
  		return questionNode ;
  	}
  	NodeIterator nodeIterator = questionNode.getNode(Utils.LANGUAGE_HOME).getNodes();
  	Node languageNode = null;
  	while(nodeIterator.hasNext()){
  		languageNode = nodeIterator.nextNode();
  		if(languageNode.getProperty("exo:language").getString().equals(language)){
  			return languageNode;
  		}
  	}
  	return null;
  }
  
  /**
   * Adds the language node, when question have multi language, 
   * each language is a child node of question node.
   * 
   * @param questionNode  the question node which have multi language
   * @param language the  language which is added in to questionNode
   * @throws Exception    throw an exception when save a new language node
   */
  public static void addLanguage(Node questionNode, QuestionLanguage language) throws Exception{
  	if(!questionNode.isNodeType("mix:faqi18n")) {
  		questionNode.addMixin("mix:faqi18n") ;
  	}
  	Node languageHome = null ;
    try{
    	languageHome = questionNode.getNode(Utils.LANGUAGE_HOME) ;
    }catch(Exception e) {
    	languageHome = questionNode.addNode(Utils.LANGUAGE_HOME, "exo:questionLanguageHome") ;
    }
    Node langNode = null ;
    try{
    	langNode = languageHome.getNode(language.getId()) ;
    }catch(Exception e) {
    	langNode = languageHome.addNode(language.getId(), "exo:faqLanguage") ;
    }
    langNode.setProperty("exo:language", language.getLanguage()) ;
    langNode.setProperty("exo:name", language.getDetail()) ;
    langNode.setProperty("exo:title", language.getQuestion()) ;
    langNode.setProperty("exo:questionId", questionNode.getName()) ;
    langNode.setProperty("exo:categoryId", questionNode.getProperty("exo:categoryId").getString()) ;
    if(langNode.isNew())questionNode.getSession().save() ;
    else questionNode.save();
  }
  
  public static void deleteAnswerQuestionLang(Node questionNode, String answerId, String language) throws Exception{
  	Node answerNode ;
  	if(language != null && language.length() > 0) {
  		Node languageNode = getLanguageNodeByLanguage(questionNode, language);
    	answerNode = languageNode.getNode(Utils.ANSWER_HOME).getNode(answerId);
  	}else {
  		answerNode = questionNode.getNode(Utils.ANSWER_HOME).getNode(answerId);
  	}  	
  	answerNode.remove();
  	questionNode.save();
  }
  
  public static void deleteCommentQuestionLang(Node questionNode, String commentId, String language) throws Exception{
  	Node languageNode = getLanguageNodeByLanguage(questionNode, language);
  	Node commnetNode = languageNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
  	commnetNode.remove();
  	questionNode.save();
  }
  
  public static QuestionLanguage getQuestionLanguageByLanguage(Node questionNode, String language) throws Exception{
  	//QuestionLanguage questionLanguage = new QuestionLanguage();
  	//questionLanguage.setLanguage(language);
  	Node languageNode = getLanguageNodeByLanguage(questionNode, language);
  	/*questionLanguage.setId(languageNode.getName());
  	questionLanguage.setLanguage(languageNode.getProperty("exo:language").getString());
  	questionLanguage.setDetail(languageNode.getProperty("exo:name").getString());
  	questionLanguage.setQuestion(languageNode.getProperty("exo:title").getString());*/  	
  	return getQuestionLanguage(languageNode);
  }
  
  private static QuestionLanguage getQuestionLanguage(Node questionNode) throws Exception{
  	QuestionLanguage questionLanguage = new QuestionLanguage() ;
  	questionLanguage.setState(QuestionLanguage.VIEW) ;
    questionLanguage.setId(questionNode.getName()) ;
    questionLanguage.setLanguage(questionNode.getProperty("exo:language").getValue().getString());
    questionLanguage.setQuestion(questionNode.getProperty("exo:title").getValue().getString());
    if(questionNode.hasProperty("exo:name")) questionLanguage.setDetail(questionNode.getProperty("exo:name").getValue().getString());
    Comment[] comments = getComment(questionNode);
    Answer[] answers = getAnswers(questionNode);
    questionLanguage.setComments(comments);
    questionLanguage.setAnswers(answers);
    return questionLanguage ;
  }
  
  private static Comment[] getComment(Node questionNode) throws Exception{
		try{
			if(!questionNode.hasNode(Utils.COMMENT_HOME)) return new Comment[]{};
			NodeIterator nodeIterator = questionNode.getNode(Utils.COMMENT_HOME).getNodes();
			Comment[] comments = new Comment[(int) nodeIterator.getSize()];
			Node commentNode = null;
			int i = 0;
			while(nodeIterator.hasNext()){
				commentNode = nodeIterator.nextNode();
				comments[i] = getCommentByNode(commentNode);
				i ++;
			}
			return comments;
		} catch (Exception e){
		  
		  return new Comment[]{};
		}
	}
  
  private static Comment getCommentByNode(Node commentNode) throws Exception {
		Comment comment = new Comment();
		comment.setId(commentNode.getName()) ;
		if(commentNode.hasProperty("exo:comments")) comment.setComments((commentNode.getProperty("exo:comments").getValue().getString())) ;
		if(commentNode.hasProperty("exo:commentBy")) comment.setCommentBy((commentNode.getProperty("exo:commentBy").getValue().getString())) ;		
		if(commentNode.hasProperty("exo:dateComment")) comment.setDateComment((commentNode.getProperty("exo:dateComment").getValue().getDate().getTime())) ;
		if(commentNode.hasProperty("exo:fullName")) comment.setFullName((commentNode.getProperty("exo:fullName").getValue().getString())) ;
		if(commentNode.hasProperty("exo:postId")) comment.setPostId(commentNode.getProperty("exo:postId").getString()) ;
		return comment;
	}
  
  private static Answer[] getAnswers(Node questionNode) throws Exception{
		try{
			if(!questionNode.hasNode(Utils.ANSWER_HOME)) return new Answer[]{};
			NodeIterator nodeIterator = questionNode.getNode(Utils.ANSWER_HOME).getNodes();
			List<Answer> answers = new ArrayList<Answer>();
			Answer ans;
			String language = questionNode.getProperty("exo:language").getString() ;
			while(nodeIterator.hasNext()){
				try{
					ans = getAnswerByNode(nodeIterator.nextNode());
					ans.setLanguage(language) ;
					answers.add(ans);
				}catch(Exception e){}				
			}
			return answers.toArray(new Answer[]{});
		} catch (Exception e){
		  log.error("Fail to get answers: ", e);
		}
		return new Answer[]{};
	}
  
  private static Answer getAnswerByNode(Node answerNode) throws Exception {
		Answer answer = new Answer();
		answer.setId(answerNode.getName()) ;
		if(answerNode.hasProperty("exo:responses")) answer.setResponses((answerNode.getProperty("exo:responses").getValue().getString())) ;
		if(answerNode.hasProperty("exo:responseBy")) answer.setResponseBy((answerNode.getProperty("exo:responseBy").getValue().getString())) ;		
		if(answerNode.hasProperty("exo:fullName")) answer.setFullName((answerNode.getProperty("exo:fullName").getValue().getString())) ;		
		if(answerNode.hasProperty("exo:dateResponse")) answer.setDateResponse((answerNode.getProperty("exo:dateResponse").getValue().getDate().getTime())) ;
		if(answerNode.hasProperty("exo:usersVoteAnswer")) answer.setUsersVoteAnswer(ValuesToStrings(answerNode.getProperty("exo:usersVoteAnswer").getValues())) ;
		if(answerNode.hasProperty("exo:MarkVotes")) answer.setMarkVotes(answerNode.getProperty("exo:MarkVotes").getValue().getLong()) ;
		if(answerNode.hasProperty("exo:approveResponses")) answer.setApprovedAnswers(answerNode.getProperty("exo:approveResponses").getValue().getBoolean()) ;
		if(answerNode.hasProperty("exo:activateResponses")) answer.setActivateAnswers(answerNode.getProperty("exo:activateResponses").getValue().getBoolean()) ;
		if(answerNode.hasProperty("exo:postId")) answer.setPostId(answerNode.getProperty("exo:postId").getString()) ;
		String path = answerNode.getPath() ;
		answer.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;
		return answer;
	}
  
  public static Comment getCommentById(Node questionNode, String commentId, String language) throws Exception{
  	try{
  		Node commentNode ;
  		if(language != null && language.length() > 0) {
  			Node languageNode = getLanguageNodeByLanguage(questionNode, language);
  			commentNode = languageNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
    	}else {
    		commentNode = questionNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
    	}    	
  		Comment comment = new Comment();  		
			if(commentNode.hasProperty("exo:id")) comment.setId((commentNode.getProperty("exo:id").getValue().getString())) ;
			if(commentNode.hasProperty("exo:comments")) comment.setComments((commentNode.getProperty("exo:comments").getValue().getString())) ;
			if(commentNode.hasProperty("exo:commentBy")) comment.setCommentBy((commentNode.getProperty("exo:commentBy").getValue().getString())) ;
			if(commentNode.hasProperty("exo:fullName")) comment.setFullName((commentNode.getProperty("exo:fullName").getValue().getString())) ;
			if(commentNode.hasProperty("exo:dateComment")) comment.setDateComment((commentNode.getProperty("exo:dateComment").getValue().getDate().getTime())) ;
			if(commentNode.hasProperty("exo:postId")) comment.setPostId(commentNode.getProperty("exo:postId").getString()) ;
  		return comment;
  	} catch (Exception e){
  		return null;
  	}
  }
  
  public static Answer getAnswerById(Node questionNode, String answerid, String language) throws Exception{
  	Answer answer = new Answer();
  	try{
  		Node answerNode ;
  		if(language != null && language.length() > 0) {
  			Node languageNode = getLanguageNodeByLanguage(questionNode, language);
    		answerNode = languageNode.getNode(Utils.ANSWER_HOME).getNode(answerid);
  		}else {
    		answerNode = questionNode.getNode(Utils.ANSWER_HOME).getNode(answerid);
  		}  		
  		answer.setId((answerNode.getProperty("exo:id").getValue().getString())) ;
  		String path = answerNode.getPath() ;
  		answer.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1)) ;  		
    	if(answerNode.hasProperty("exo:responses")) answer.setResponses((answerNode.getProperty("exo:responses").getValue().getString())) ;
      if(answerNode.hasProperty("exo:responseBy")) answer.setResponseBy((answerNode.getProperty("exo:responseBy").getValue().getString())) ;
      if(answerNode.hasProperty("exo:fullName")) answer.setFullName((answerNode.getProperty("exo:fullName").getValue().getString())) ;
      if(answerNode.hasProperty("exo:dateResponse")) answer.setDateResponse((answerNode.getProperty("exo:dateResponse").getValue().getDate().getTime())) ;
      if(answerNode.hasProperty("exo:usersVoteAnswer")) answer.setUsersVoteAnswer(ValuesToStrings(answerNode.getProperty("exo:usersVoteAnswer").getValues())) ;
      if(answerNode.hasProperty("exo:MarkVotes")) answer.setMarkVotes(answerNode.getProperty("exo:MarkVotes").getValue().getLong()) ;
      if(answerNode.hasProperty("exo:approveResponses")) answer.setApprovedAnswers((answerNode.getProperty("exo:approveResponses").getValue().getBoolean())) ;
      if(answerNode.hasProperty("exo:activateResponses")) answer.setActivateAnswers((answerNode.getProperty("exo:activateResponses").getValue().getBoolean())) ;
      if(answerNode.hasProperty("exo:postId")) answer.setPostId(answerNode.getProperty("exo:postId").getString()) ;
      answer.setLanguage(answerNode.getProperty("exo:responseLanguage").getValue().getString()) ;
      return answer;
  	} catch (Exception e){
  	}
  	return null;
  }
  
  
  
  public static void saveAnswer(Node questionNode, Answer answer, String language) throws Exception{
  	Node answerHome ;
  	Node answerNode ;
  	String defaultLang = questionNode.getProperty("exo:language").getString() ;
  	if(language != null && language.length() > 0 && !language.equals(defaultLang)) {
  		Node languageNode = getLanguageNodeByLanguage(questionNode, language);
    	if(!languageNode.isNodeType("mix:faqi18n")) {
    		languageNode.addMixin("mix:faqi18n") ;
    	}
    	try{
    		answerHome = languageNode.getNode(Utils.ANSWER_HOME);
    	} catch (Exception e){
    		answerHome = languageNode.addNode(Utils.ANSWER_HOME, "exo:answerHome");
    	}
    	answer.setLanguage(language) ;
  	}else {
  		try{
    		answerHome = questionNode.getNode(Utils.ANSWER_HOME);
    	} catch (Exception e){
    		answerHome = questionNode.addNode(Utils.ANSWER_HOME, "exo:answerHome");
    	}
    	answer.setLanguage(questionNode.getProperty("exo:language").getString()) ;
  	}
  	try{
  		answerNode = answerHome.getNode(answer.getId());
  	} catch (Exception e) {
  		answerNode = answerHome.addNode(answer.getId(), "exo:answer");
  	}
  	answerNode.setProperty("exo:responses", answer.getResponses()) ;
  	answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
  	answerNode.setProperty("exo:fullName", answer.getFullName());
  	answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
  	answerNode.setProperty("exo:MarkVotes", answer.getMarkVotes()) ;
    if(answer.isNew()){
    	java.util.Calendar calendar = null ;
    	calendar = GregorianCalendar.getInstance() ;
    	calendar.setTime(new Date()) ;
    	answerNode.setProperty("exo:dateResponse", calendar) ;
    	answerNode.setProperty("exo:id", answer.getId());
    	answerNode.setProperty("exo:questionId", questionNode.getName()) ;
    	answerNode.setProperty("exo:responseLanguage", language) ;
    	answerNode.setProperty("exo:categoryId", questionNode.getProperty("exo:categoryId").getString() ) ;    	
    }
    answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers());
    answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers());
    if(answer.getPostId() != null && answer.getPostId().length() > 0) {
  		answerNode.setProperty("exo:postId", answer.getPostId());
  	}
    if(questionNode.isNew()){
    	questionNode.getSession().save();
    } else {
    	questionNode.save();
    }
  }
  
  public static void saveAnswer(Node quesNode, QuestionLanguage questionLanguage) throws Exception{
  	Node quesLangNode ;
  	try {
  		quesLangNode  = quesNode.getNode(Utils.LANGUAGE_HOME).getNode(questionLanguage.getId());
    } catch (Exception e) {
    	quesLangNode  = quesNode.getNode(Utils.LANGUAGE_HOME).addNode(questionLanguage.getId(), "exo:faqLanguage");
    	quesNode.getSession().save();
    }
  	if(!quesLangNode.isNodeType("mix:faqi18n")) {
  		quesLangNode.addMixin("mix:faqi18n") ;
  	}
  	Node answerHome = null;
  	Node answerNode;
  	Answer[] answers = questionLanguage.getAnswers();
  	try{
  		answerHome = quesLangNode.getNode(Utils.ANSWER_HOME);
  	} catch (Exception e){
  		answerHome = quesLangNode.addNode(Utils.ANSWER_HOME, "exo:answerHome");
  	}
  	if(!answerHome.isNew()){
  		List<String> listNewAnswersId = new ArrayList<String>();
    	for(int i = 0; i < answers.length; i ++){
    		listNewAnswersId.add(answers[i].getId());
    	}
    	NodeIterator nodeIterator = answerHome.getNodes();
    	while(nodeIterator.hasNext()){
    		answerNode = nodeIterator.nextNode();
    		if(!listNewAnswersId.contains(answerNode.getName()))
    			answerNode.remove();
    	}
  	}
  	for(Answer answer : answers){
  		answerNode = null;
  		try{
  			answerNode = answerHome.getNode(answer.getId());
  		} catch(Exception e) {
  			answerNode = answerHome.addNode(answer.getId(), "exo:answer");
  			answerNode.setProperty("exo:id", answer.getId());
  		}
	  	if(answerNode.isNew()){
	  		java.util.Calendar calendar = null ;
	  		calendar = null ;
	  		calendar = GregorianCalendar.getInstance();
	  		calendar.setTime(new Date());
	  		answerNode.setProperty("exo:dateResponse", calendar);
	  		//String path = answerNode.getPath() ;
	  		//answerNode.setProperty("exo:answerPath", path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
	    	answerNode.setProperty("exo:questionId", quesNode.getName() ) ;
	    	answerNode.setProperty("exo:categoryId", quesNode.getProperty("exo:categoryId").getString() ) ;
	  	}
	  	String language = answer.getLanguage();
	  	if(language == null || language.length() == 0) language = questionLanguage.getLanguage();
	  	answerNode.setProperty("exo:responses", answer.getResponses()) ;
	  	answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
	  	answerNode.setProperty("exo:fullName", answer.getFullName());
	  	answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
	  	answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
	  	answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
	  	answerNode.setProperty("exo:MarkVotes", answer.getMarkVotes()) ;
	  	answerNode.setProperty("exo:responseLanguage", language) ;
	  	if(answer.getPostId() != null && answer.getPostId().length() > 0) {
	  		answerNode.setProperty("exo:postId", answer.getPostId());
	  	}
	  	if(answerNode.isNew()) quesNode.getSession().save();
	  	else quesNode.save();
  	}
  }
  
  public static void saveComment(Node questionNode, Comment comment, String language) throws Exception{
  	String lang ;
  	Node commentHome ;
  	Node commentNode ;
  	if(language != null && language.length() > 0) {
  		Node languageNode = getLanguageNodeByLanguage(questionNode, language);
  		lang = language ;
    	if(!languageNode.isNodeType("mix:faqi18n")) {
    		languageNode.addMixin("mix:faqi18n") ;
    	}
    	try{
    		commentHome = languageNode.getNode(Utils.COMMENT_HOME);
    	} catch (Exception e){
    		commentHome = languageNode.addNode(Utils.COMMENT_HOME, "exo:commentHome");
    	}
  	}else {
  		try{
    		commentHome = questionNode.getNode(Utils.COMMENT_HOME);    		
    	} catch (Exception e){
    		commentHome = questionNode.addNode(Utils.COMMENT_HOME, "exo:commentHome");
    	}
    	lang = questionNode.getProperty("exo:language").getString() ;
  	}  	
  	try{
  		commentNode = commentHome.getNode(comment.getId());
  	} catch(Exception e) {
  		commentNode = commentHome.addNode(comment.getId(), "exo:comment");
  		commentNode.setProperty("exo:id", comment.getId()) ;
  	}
  	commentNode.setProperty("exo:comments", comment.getComments());
  	commentNode.setProperty("exo:commentBy", comment.getCommentBy());
  	commentNode.setProperty("exo:fullName", comment.getFullName());
  	commentNode.setProperty("exo:categoryId", questionNode.getProperty("exo:categoryId").getString());
  	commentNode.setProperty("exo:questionId", questionNode.getName());
  	commentNode.setProperty("exo:commentLanguage", lang);
  	if(comment.getPostId() != null && comment.getPostId().length() > 0) {
  		commentNode.setProperty("exo:postId", comment.getPostId());
  	}
  	if(commentNode.isNew()) {
  		java.util.Calendar calendar = null ;
  		calendar = GregorianCalendar.getInstance() ;
  		calendar.setTime(new Date()) ;
  		commentNode.setProperty("exo:dateComment", calendar) ;
  		//questionNode.getSession().save();
  	}//else questionNode.save();
  	questionNode.save();
  }
  
  protected Value[] booleanToValues(Node node, Boolean[] bools) throws Exception{
  	if(bools == null) return new Value[]{node.getSession().getValueFactory().createValue(true)};
  	Value[] values = new Value[bools.length]; 
  	for(int i = 0; i < values.length; i ++){
  		values[i] = node.getSession().getValueFactory().createValue(bools[i]);
  	}
  	return values;
  }
  
  /**
   * Removes the language, when question have multi language, and now one of them
   * is not helpful, and admin or moderator want to delete it, this function will
   * be called. And this function will do:
   * <p>
   * Get all children nodes of question node, and compare them with list language
   * is inputted in this function. Each language node, if it's name is not contained
   * in list language, it will be deleted.
   * <p>
   * After that, the remains of language nodes will be saved as children node of
   * question node.
   * 
   * @param questionNode the question node which have multilanguage
   * @param listLanguage the list languages will be saved
   */
  public static void removeLanguage(Node questionNode, List<String> listLanguage) {
    try {
      if(!questionNode.hasNode(Utils.LANGUAGE_HOME)) return ;
      Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME) ;
      NodeIterator nodeIterator = languageNode.getNodes();
      Node node = null ;
      while(nodeIterator.hasNext()) {
        node = nodeIterator.nextNode() ;
        if(!listLanguage.contains(node.getProperty("exo:language").getString())) {
          node.remove() ;
        }
      }
      questionNode.getSession().save() ;
    } catch (Exception e) {
      log.error("Fail to remove language from a list of language: ", e);
    }
  }
  
  public static void removeLanguage(Node questionNode, QuestionLanguage lang) {
    try {
      if(!questionNode.hasNode(Utils.LANGUAGE_HOME)) return ;
      Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME) ;
      languageNode.getNode(lang.getId()).remove() ;
      questionNode.save() ;
    } catch (Exception e) {
      log.error("Fail to remove language from a question language: ", e);
    }
  }
  public static void voteAnswer(Node answerNode, String userName, boolean isUp) throws Exception {
  	boolean isVoted = false ;
  	long mark = 0 ;
  	List<String> users = new ArrayList<String>();
  	if(answerNode.hasProperty("exo:usersVoteAnswer") && answerNode.hasProperty("exo:MarkVotes")) {
  		users = ValuesToList(answerNode.getProperty("exo:usersVoteAnswer").getValues()) ;
    	mark = answerNode.getProperty("exo:MarkVotes").getLong() ;
    	int i = 0 ;
    	for(String user : users) {
    		if (user.indexOf(userName) > -1) {
    			String[] values = user.split("/") ;
    			if(values[1].equals("1")){ //up
    				if(!isUp){
    					mark = mark - 2 ;
    					users.add(i, userName + "/-1")  ;
    				} 
    			}else { // -1: down 
    				if(isUp){
    					mark = mark + 2 ;
    					users.add(i, userName + "/1") ;
    				}  				
    			}
    			isVoted = true ;	
    			break ;
    		}
    		i++ ;
    	}
  	}  	 
  	if(isVoted) {
  		answerNode.setProperty("exo:usersVoteAnswer", users.toArray(new String[]{})) ;
  		answerNode.setProperty("exo:MarkVotes", mark) ;  		  		
  	}else {
  		List<String> newUsers = users ;
  		//if(users.length > 0) newUsers = Arrays.asList(users) ;
  		if(isUp){
  			mark = mark + 1 ;
  			newUsers.add(userName + "/1") ;
  		} else {  			
  			mark = mark - 1 ;
  			newUsers.add(userName + "/-1") ;
  		}
  		answerNode.setProperty("exo:usersVoteAnswer", newUsers.toArray(new String[]{})) ;
  		answerNode.setProperty("exo:MarkVotes", mark) ;
  	}
  	//System.out.println("mark ==>" + mark);  	
  	answerNode.save() ;
  }
  
  public static void voteQuestion(Node questionNode, String userName, int number) throws Exception {
  	if(questionNode.hasProperty("exo:markVote") && questionNode.hasProperty("exo:usersVote")) {
  		double mark = questionNode.getProperty("exo:markVote").getDouble() ;
  		List<String> currentUsers = new ArrayList<String>() ;
  		currentUsers.addAll(ValuesToList(questionNode.getProperty("exo:usersVote").getValues())) ;
  		double currentMark = (mark * currentUsers.size() + number) / (currentUsers.size() + 1) ;
  		currentUsers.add(userName + "/" + number) ;
  		questionNode.setProperty("exo:markVote", currentMark) ;
  		questionNode.setProperty("exo:usersVote", currentUsers.toArray(new String[]{})) ;
  	}else {
  		double mark = number ;
  		questionNode.setProperty("exo:markVote", mark) ;
  		questionNode.setProperty("exo:usersVote", new String[]{userName + "/" + number}) ;
  	}
  	questionNode.save() ;
  }
  
  public static void unVoteQuestion(Node questionNode, String userName) throws Exception {
  	String[] users = ValuesToStrings(questionNode.getProperty("exo:usersVote").getValues()) ;
  	List<String> userList = Arrays.asList(users) ;
  	List<String> newList = new ArrayList<String>() ;
  	double mark = 0 ;
  	for(String user : userList) {
  		//System.out.println("User ==>" + user);
  		if(user.indexOf(userName + "/") == 0){
  			int number = Integer.parseInt(user.substring(user.indexOf("/") + 1)) ;
  			mark = questionNode.getProperty("exo:markVote").getDouble() ;
  			mark = (mark * userList.size()) - number ;  			
  		}else{
  			newList.add(user) ;
  		}  		
  	}
  	//System.out.println("size ==>" + newList.size());
  	questionNode.setProperty("exo:markVote", mark) ;
		questionNode.setProperty("exo:usersVote", newList.toArray(new String[]{})) ;
		questionNode.save() ;		
  }
}
