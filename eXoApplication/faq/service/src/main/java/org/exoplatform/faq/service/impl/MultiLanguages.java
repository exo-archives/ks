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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.CategoryLanguage;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.JcrInputProperty;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.value.DateValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

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
  
  /**
   * Class constructor, instantiates a new multi languages.
   * 
   * @throws Exception the exception
   */
  public MultiLanguages()throws Exception {}  

  /**
   * Sets the property value for the node
   * 
   * @param propertyName the property name
   * @param node the node
   * @param requiredtype the requiredtype
   * @param value the value
   * @param isMultiple the is multiple
   * @throws Exception the exception
   */
  private void setPropertyValue(String propertyName, Node node, int requiredtype, Object value, boolean isMultiple) throws Exception {
    switch (requiredtype) {
    case PropertyType.STRING:
      if (value == null) {
        node.setProperty(propertyName, "");
      } else {
        if(isMultiple) {
          if (value instanceof String) node.setProperty(propertyName, new String[] { value.toString()});
          else if(value instanceof String[]) node.setProperty(propertyName, (String[]) value);
        } else {
          if(value instanceof StringValue) {
            StringValue strValue = (StringValue) value ;
            node.setProperty(propertyName, strValue.getString());
          } else {
            node.setProperty(propertyName, value.toString());
          }
        }
      }
      break;
    case PropertyType.BINARY:
      if (value == null) node.setProperty(propertyName, "");
      else if (value instanceof byte[]) node.setProperty(propertyName, new ByteArrayInputStream((byte[]) value));
      else if (value instanceof String) node.setProperty(propertyName, new ByteArrayInputStream((value.toString()).getBytes()));
      else if (value instanceof String[]) node.setProperty(propertyName, new ByteArrayInputStream((((String[]) value)).toString().getBytes()));      
      break;
    case PropertyType.BOOLEAN:
      if (value == null) node.setProperty(propertyName, false);
      else if (value instanceof String) node.setProperty(propertyName, new Boolean(value.toString()).booleanValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);         
      break;
    case PropertyType.LONG:
      if (value == null || "".equals(value)) node.setProperty(propertyName, 0);
      else if (value instanceof String) node.setProperty(propertyName, new Long(value.toString()).longValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);  
      break;
    case PropertyType.DOUBLE:
      if (value == null || "".equals(value)) node.setProperty(propertyName, 0);
      else if (value instanceof String) node.setProperty(propertyName, new Double(value.toString()).doubleValue());
      else if (value instanceof String[]) node.setProperty(propertyName, (String[]) value);        
      break;
    case PropertyType.DATE:      
      if (value == null) {        
        node.setProperty(propertyName, new GregorianCalendar());
      } else {
        if(isMultiple) {
          Session session = node.getSession() ;
          if (value instanceof String) {
            Value value2add = session.getValueFactory().createValue(ISO8601.parse((String) value));
            node.setProperty(propertyName, new Value[] {value2add});
          } else if (value instanceof String[]) {
            String[] values = (String[]) value;
            Value[] convertedCalendarValues = new Value[values.length];
            int i = 0;
            for (String stringValue : values) {
              Value value2add = session.getValueFactory().createValue(ISO8601.parse(stringValue));
              convertedCalendarValues[i] = value2add;
              i++;
            }
            node.setProperty(propertyName, convertedCalendarValues);
            session.logout();
          }
        } else {
          if(value instanceof String) {
            node.setProperty(propertyName, ISO8601.parse(value.toString()));
          } else if(value instanceof GregorianCalendar) {
            node.setProperty(propertyName, (GregorianCalendar) value);
          } else if(value instanceof DateValue) {
            DateValue dateValue = (DateValue) value ;
            node.setProperty(propertyName, dateValue.getDate());
          }
        }
      }
      break ;
    case PropertyType.REFERENCE :
      if (value == null) throw new RepositoryException("null value for a reference " + requiredtype);
      if(value instanceof Value[]) 
        node.setProperty(propertyName, (Value[]) value);
        else if (value instanceof String) {
          Session session = node.getSession();
          if(session.getRootNode().hasNode((String)value)) {
            Node catNode = session.getRootNode().getNode((String)value);
            Value value2add = session.getValueFactory().createValue(catNode);
            node.setProperty(propertyName, new Value[] {value2add});          
          } else {
            node.setProperty(propertyName, (String) value);
          }
        }       
      break ;
    }
  }
  
  /**
   * Adds the language node, when question have multi language, 
   * eache language is a child node of question node.
   * 
   * @param questionNode  the question node which have multi language
   * @param language the  language which is added in to questionNode
   * @throws Exception    throw an exception when save a new language node
   */
  @SuppressWarnings("static-access")
  public void addLanguage(Node questionNode, QuestionLanguage language) throws Exception{
  	if(!questionNode.isNodeType("mix:faqi18n")) {
  		questionNode.addMixin("mix:faqi18n") ;
  	}
  	Node languagesNode = null ;
    try{
    	languagesNode = questionNode.getNode(Utils.LANGUAGE_HOME) ;
    }catch(Exception e) {
    	languagesNode = questionNode.addNode(Utils.LANGUAGE_HOME, NTUNSTRUCTURED) ;
    }
    Node langNode = null ;
    try{
    	langNode = languagesNode.getNode(language.getLanguage()) ;
    }catch(Exception e) {
    	langNode = languagesNode.addNode(language.getLanguage(), NTUNSTRUCTURED) ;
    }
    langNode.setProperty("exo:name", language.getDetail()) ;
    langNode.setProperty("exo:title", language.getQuestion()) ;
    System.out.println("\n\n path LG: " + langNode.getPath());
    if(langNode.isNew())questionNode.getSession().save() ;
    else questionNode.save();
  }
  
  public void deleteAnswerQuestionLang(Node questionNode, String answerId, String language, SessionProvider sProvider) throws Exception{
  	Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME).getNode(language);
  	Node answerNode = languageNode.getNode(Utils.ANSWER_HOME).getNode(answerId);
  	answerNode.remove();
  	questionNode.save();
  }
  
  public void deleteCommentQuestionLang(Node questionNode, String commentId, String language, SessionProvider sProvider) throws Exception{
  	Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME).getNode(language);
  	Node commnetNode = languageNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
  	commnetNode.remove();
  	questionNode.save();
  }
  
  public QuestionLanguage getQuestionLanguageByLanguage(Node questionNode, String language) throws Exception{
  	QuestionLanguage questionLanguage = new QuestionLanguage();
  	questionLanguage.setLanguage(language);
  	Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME).getNode(language);
  	questionLanguage.setDetail(languageNode.getProperty("exo:name").getString());
  	questionLanguage.setQuestion(languageNode.getProperty("exo:title").getString());
  	
  	return questionLanguage;
  }
  
  public Comment getCommentById(Node questionNode, String commentId, String language) throws Exception{
  	Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME).getNode(language);
  	try{
  		Comment comment = new Comment();
  		Node commentNode = languageNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
			if(commentNode.hasProperty("exo:id")) comment.setId((commentNode.getProperty("exo:id").getValue().getString())) ;
			if(commentNode.hasProperty("exo:comments")) comment.setComments((commentNode.getProperty("exo:comments").getValue().getString())) ;
			if(commentNode.hasProperty("exo:commentBy")) comment.setCommentBy((commentNode.getProperty("exo:commentBy").getValue().getString())) ;  	
			if(commentNode.hasProperty("exo:dateComment")) comment.setDateComment((commentNode.getProperty("exo:dateComment").getValue().getDate().getTime())) ;
  		return comment;
  	} catch (Exception e){
  		e.printStackTrace();
  		return null;
  	}
  }
  
  public Answer getAnswerById(Node questionNode, String answerid, String language) throws Exception{
  	Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME).getNode(language);
  	Answer answer = new Answer();
  	try{
  		Node answerNode = languageNode.getNode(Utils.ANSWER_HOME).getNode(answerid);
  		if(answerNode.hasProperty("exo:id")) answer.setId((answerNode.getProperty("exo:id").getValue().getString())) ;
    	if(answerNode.hasProperty("exo:responses")) answer.setResponses((answerNode.getProperty("exo:responses").getValue().getString())) ;
      if(answerNode.hasProperty("exo:responseBy")) answer.setResponseBy((answerNode.getProperty("exo:responseBy").getValue().getString())) ;  	
      if(answerNode.hasProperty("exo:dateResponse")) answer.setDateResponse((answerNode.getProperty("exo:dateResponse").getValue().getDate().getTime())) ;
      if(answerNode.hasProperty("exo:usersVoteAnswer")) answer.setUsersVoteAnswer(ValuesToStrings(answerNode.getProperty("exo:usersVoteAnswer").getValues())) ;
      if(answerNode.hasProperty("exo:MarkVotes")) answer.setMarkVotes(ValuesToLong(answerNode.getProperty("exo:MarkVotes").getValues())) ;
      if(answerNode.hasProperty("exo:marksVoteAnswer")) answer.setMarksVoteAnswer((answerNode.getProperty("exo:marksVoteAnswer").getValue().getDouble())) ;
      if(answerNode.hasProperty("exo:approveResponses")) answer.setApprovedAnswers((answerNode.getProperty("exo:approveResponses").getValue().getBoolean())) ;
      if(answerNode.hasProperty("exo:activateResponses")) answer.setActivateAnswers((answerNode.getProperty("exo:activateResponses").getValue().getBoolean())) ;
      return answer;
  	} catch (Exception e){
  		return null;
  	}
  }
  
  private String [] ValuesToStrings(Value[] Val) throws Exception {
		if(Val.length < 1) return new String[]{} ;
		if(Val.length == 1) return new String[]{Val[0].getString()} ;
		String[] Str = new String[Val.length] ;
		for(int i = 0; i < Val.length; ++i) {
			Str[i] = Val[i].getString() ;
		}
		return Str;
	}
  
  private long [] ValuesToLong(Value[] Val) throws Exception {
  	if(Val.length < 1) return new long[]{0} ;
  	long[] d = new long[Val.length] ;
  	for(int i = 0; i < Val.length; ++i) {
  		d[i] = Val[i].getLong() ;
  	}
  	return d;
  }
  
  private Value[] longToValues(Node answerNode, long[] marks){
  	if(marks == null || marks.length < 1) return null;
  	Value[] values = new Value[marks.length];
  	try{
	  	for(int i = 0; i < marks.length; i ++){
	  		values[i] = answerNode.getSession().getValueFactory().createValue(marks[i]);
	  	}
  	} catch (Exception e){
  		return null;
  	}
  	return values;
  }
  
  public void saveAnswer(Node questionNode, Answer answer, String languge, SessionProvider sessionProvider) throws Exception{
  	Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME).getNode(languge);
  	if(!languageNode.isNodeType("mix:faqi18n")) {
  		languageNode.addMixin("mix:faqi18n") ;
  	}
  	Node answerHome = null;
  	try{
  		answerHome = languageNode.getNode(Utils.ANSWER_HOME);
  	} catch (Exception e){
  		answerHome = languageNode.addNode(Utils.ANSWER_HOME, NTUNSTRUCTURED);
  	}
  	Node answerNode = null;
  	try{
  		answerNode = answerHome.getNode(answer.getId());
  	} catch (Exception e) {
  		answerNode = answerHome.addNode(answer.getId(), "exo:answer");
  	}
  	answerNode.setProperty("exo:responses", answer.getResponses()) ;
  	answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
  	answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
  	answerNode.setProperty("exo:MarkVotes", longToValues(questionNode, answer.getMarkVotes())) ;
  	answerNode.setProperty("exo:marksVoteAnswer", answer.getMarksVoteAnswer()) ;
    if(answer.isNew()){
    	java.util.Calendar calendar = null ;
    	calendar = GregorianCalendar.getInstance() ;
    	calendar.setTime(new Date()) ;
    	answerNode.setProperty("exo:dateResponse", answerNode.getSession().getValueFactory().createValue(calendar)) ;
    	answerNode.setProperty("exo:id", answer.getId());
    }
    answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers());
    answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers());
    if(answer.isNew()) questionNode.getSession().save();
    else questionNode.save();
  }
  
  public void saveAnswer(Node quesNode, QuestionLanguage questionLanguage) throws Exception{
  	Node quesLangNode ;
  	try {
  		quesLangNode  = quesNode.getNode(Utils.LANGUAGE_HOME).getNode(questionLanguage.getLanguage());
    } catch (Exception e) {
    	quesLangNode  = quesNode.getNode(Utils.LANGUAGE_HOME).addNode(questionLanguage.getLanguage());
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
  		answerHome = quesLangNode.addNode(Utils.ANSWER_HOME, NTUNSTRUCTURED);
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
	  		answerNode.setProperty("exo:dateResponse", quesLangNode.getSession().getValueFactory().createValue(calendar));
	  	}
	  	answerNode.setProperty("exo:responses", answer.getResponses()) ;
	  	answerNode.setProperty("exo:responseBy", answer.getResponseBy()) ;
	  	answerNode.setProperty("exo:approveResponses", answer.getApprovedAnswers()) ;
	  	answerNode.setProperty("exo:activateResponses", answer.getActivateAnswers()) ;
	  	answerNode.setProperty("exo:usersVoteAnswer", answer.getUsersVoteAnswer()) ;
	  	answerNode.setProperty("exo:MarkVotes", longToValues(quesNode, answer.getMarkVotes())) ;
	  	answerNode.setProperty("exo:marksVoteAnswer", answer.getMarksVoteAnswer());
	  	if(answerNode.isNew()) quesNode.getSession().save();
	  	else quesNode.save();
  	}
  }
  
  public void saveComment(Node questionNode, Comment comment, String languge, SessionProvider sessionProvider) throws Exception{
  	Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME).getNode(languge);
  	if(!languageNode.isNodeType("mix:faqi18n")) {
  		languageNode.addMixin("mix:faqi18n") ;
  	}
  	Node commentHome = null;
  	Node commnetNode = null;
  	
  	try{
  		commentHome = languageNode.getNode(Utils.COMMENT_HOME);
  	} catch (Exception e){
  		commentHome = languageNode.addNode(Utils.COMMENT_HOME, NTUNSTRUCTURED);
  	}
  	try{
  		commnetNode = commentHome.getNode(comment.getId());
  	} catch(Exception e) {
  		commnetNode = commentHome.addNode(comment.getId(), "exo:comment");
  		commnetNode.setProperty("exo:id", comment.getId()) ;
  	}
  	commnetNode.setProperty("exo:comments", comment.getComments());
  	commnetNode.setProperty("exo:commentBy", comment.getCommentBy());
  	if(commnetNode.isNew()) {
  		java.util.Calendar calendar = null ;
  		calendar = GregorianCalendar.getInstance() ;
  		calendar.setTime(new Date()) ;
  		commnetNode.setProperty("exo:dateComment", commnetNode.getSession().getValueFactory().createValue(calendar)) ;
  		questionNode.getSession().save();
  	}else questionNode.save();
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
   * @param questionNode the question node which have multi language
   * @param listLanguage the list languages will be saved
   */
  public void removeLanguage(Node questionNode, List<String> listLanguage) {
    try {
      if(!questionNode.hasNode(Utils.LANGUAGE_HOME)) return ;
      Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME) ;
      NodeIterator nodeIterator = languageNode.getNodes();
      Node node = null ;
      while(nodeIterator.hasNext()) {
        node = nodeIterator.nextNode() ;
        if(!listLanguage.contains(node.getName())) {
          node.remove() ;
        }
      }
      questionNode.getSession().save() ;
    } catch (PathNotFoundException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Adds the language node, when category have multi language, 
   * each language is a child node of category node.
   * 
   * @param categoryNode the category node
   * @param language the language
   * @throws Exception the exception
   */
  public void addLanguage(Node categoryNode, CategoryLanguage language) throws Exception {
  	if(!categoryNode.isNodeType("mix:faqi18n")) {
  		categoryNode.addMixin("mix:faqi18n") ;
  	}
  	Node languagesNode = null ;
    try{
    	languagesNode = categoryNode.getNode(Utils.LANGUAGE_HOME) ;
    }catch(Exception e) {
    	languagesNode = categoryNode.addNode(Utils.LANGUAGE_HOME, NTUNSTRUCTURED) ;
    }
    Node langNode = null ;
    try{
    	langNode = languagesNode.getNode(language.getLanguage()) ;
    }catch(Exception e) {
    	langNode = languagesNode.addNode(language.getLanguage(), NTUNSTRUCTURED) ;
    }    
    langNode.setProperty("exo:name", language.getName()) ;
    categoryNode.save() ;
  }
  
  /**
   * Sets the mixin.
   * 
   * @param node the node
   * @param newLang the new lang
   * @throws Exception the exception
   */
  private void setMixin(Node node, Node newLang) throws Exception {
    NodeType[] mixins = node.getMixinNodeTypes() ;
    for(NodeType mixin:mixins) {
      if(!mixin.getName().equals("exo:actionable")) {
        if(newLang.canAddMixin(mixin.getName())) {
          newLang.addMixin(mixin.getName()) ;
          for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
            if(!def.isProtected()) {
              String propName = def.getName() ;
              if(def.isMandatory() && !def.isAutoCreated()) {
                if(def.isMultiple()) {
                  newLang.setProperty(propName,node.getProperty(propName).getValues()) ;
                } else {
                  newLang.setProperty(propName,node.getProperty(propName).getValue()) ; 
                }
              }        
            }
          }
        }
      }
    }
  }

  /**
   * Adds the language.
   * 
   * @param node the node
   * @param inputs the inputs
   * @param language the language
   * @param isDefault the is default
   * @throws Exception the exception
   */
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    String defaultLanguage = getDefault(node) ;
    if(!node.isNodeType("mix:faqi18n")) node.addMixin("mix:faqi18n") ;
    if(node.hasNode(Utils.LANGUAGE_HOME)) languagesNode = node.getNode(Utils.LANGUAGE_HOME) ;
    else languagesNode = node.addNode(Utils.LANGUAGE_HOME, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) {
          newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        } else {
          newLanguageNode = languagesNode.addNode(defaultLanguage) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin:mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;            
            }
          }
        }
      } else {
        if(languagesNode.hasNode(language)) {
          newLanguageNode = languagesNode.getNode(language) ;
        } else {
          newLanguageNode = languagesNode.addNode(language) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin : mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;
              for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
                if(!def.isProtected()) {
                  String propName = def.getName() ;
                  if(def.isMandatory() && !def.isAutoCreated()) {
                    if(def.isMultiple()) {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
                    } else {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
                    }
                  }        
                }
              }
            }
          }
          newLanguageNode.setProperty(EXO_LANGUAGE, language) ;
        }
      }
    }   
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)inputs.get(NODE + propertyName) ;
        System.out.println("propertyName ====>"+ propertyName + "===>"+ property);
        
        if(defaultLanguage.equals(language) && property != null) {
          setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              Object value = null ;
              int requiredType = node.getProperty(propertyName).getDefinition().getRequiredType() ;
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              if(isMultiple) value = node.getProperty(propertyName).getValues() ;
              else value = node.getProperty(propertyName).getValue() ;
              setPropertyValue(propertyName, newLanguageNode, requiredType, value, isMultiple) ;
            }
            if(property != null) {
              setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          } else {
            if(property != null) {
              setPropertyValue(propertyName, newLanguageNode, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          }
        }               
      }
    }
    if(!defaultLanguage.equals(language) && isDefault){
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    if(isDefault && languagesNode.hasNode(language)) languagesNode.getNode(language).remove() ;
    node.save() ;
    node.getSession().save() ;
    
    System.out.println("\n\n\n\n>>>>addLanguage() --> size of questionNodeChildren: " + node.getNodes().getSize());
  }
  
  /**
   * Adds the language.
   * 
   * @param node the node
   * @param inputs the inputs
   * @param language the language
   * @param isDefault the is default
   * @param nodeType the node type
   * @throws Exception the exception
   */
  public void addLanguage(Node node, Map inputs, String language, boolean isDefault, String nodeType) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    String defaultLanguage = getDefault(node) ;
    Workspace ws = node.getSession().getWorkspace() ;
    if(node.hasNode(Utils.LANGUAGE_HOME)) languagesNode = node.getNode(Utils.LANGUAGE_HOME) ;
    else languagesNode = node.addNode(Utils.LANGUAGE_HOME, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) {
          newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        } else {
          newLanguageNode = languagesNode.addNode(defaultLanguage) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin:mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;            
            }
          }
        }
      } else {
        if(languagesNode.hasNode(language)) {
          newLanguageNode = languagesNode.getNode(language) ;
        } else {
          newLanguageNode = languagesNode.addNode(language) ;
          NodeType[] mixins = node.getMixinNodeTypes() ;
          for(NodeType mixin : mixins) {
            if(!mixin.getName().equals("exo:actionable")) {
              if(newLanguageNode.canAddMixin(mixin.getName())) newLanguageNode.addMixin(mixin.getName()) ;
              for(PropertyDefinition def: mixin.getPropertyDefinitions()) {
                if(!def.isProtected()) {
                  String propName = def.getName() ;
                  if(def.isMandatory() && !def.isAutoCreated()) {
                    if(def.isMultiple()) {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValues()) ;
                    } else {
                      newLanguageNode.setProperty(propName,node.getProperty(propName).getValue()) ; 
                    }
                  }        
                }
              }
            }
          }
          newLanguageNode.setProperty(EXO_LANGUAGE, language) ;
        }
      }
      Node jcrContent = node.getNode(nodeType) ;
      node.save() ;
      if(!newLanguageNode.hasNode(nodeType)) {
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
      }
      Node newContentNode = newLanguageNode.getNode(nodeType) ;
      PropertyIterator props = newContentNode.getProperties() ;
      while(props.hasNext()) {
        Property prop = props.nextProperty() ;
        if(inputs.containsKey(NODE + nodeType + "/" + prop.getName())) {
          JcrInputProperty inputVariable = (JcrInputProperty) inputs.get(NODE + nodeType + "/" + prop.getName()) ;
          boolean isMultiple = prop.getDefinition().isMultiple() ;
          setPropertyValue(prop.getName(), newContentNode, prop.getType(), inputVariable.getValue(), isMultiple) ;
        }
      }
      if(isDefault) {
        Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
        node.getSession().move(node.getNode(nodeType).getPath(), tempNode.getPath() + "/" + nodeType) ;
        node.getSession().move(newLanguageNode.getNode(nodeType).getPath(), node.getPath() + "/" + nodeType) ;
        node.getSession().move(tempNode.getNode(nodeType).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + nodeType) ;
        tempNode.remove() ;
      }      
    } else {
      JcrInputProperty inputVariable = (JcrInputProperty) inputs.get(NODE + nodeType + "/" + JCRDATA) ;
      setPropertyValue(JCRDATA, node.getNode(nodeType), inputVariable.getType(), inputVariable.getValue(), false) ;
    }
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)inputs.get(NODE + propertyName) ;
        if(defaultLanguage.equals(language) && property != null) {
          setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              Object value = null ;
              int requiredType = node.getProperty(propertyName).getDefinition().getRequiredType() ;
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              if(isMultiple) value = node.getProperty(propertyName).getValues() ;
              else value = node.getProperty(propertyName).getValue() ;
              setPropertyValue(propertyName, newLanguageNode, requiredType, value, isMultiple) ;
            }
            if(property != null) {
              setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          } else {
            if(property != null) {
              setPropertyValue(propertyName, newLanguageNode, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          }
        }               
      }
    }
    if(!defaultLanguage.equals(language) && isDefault){
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    if(isDefault && languagesNode.hasNode(language)) languagesNode.getNode(language).remove() ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  /**
   * Adds the file language.
   * 
   * @param node        the node
   * @param value       the value
   * @param mimeType    the mime type
   * @param language    the language
   * @param isDefault   the is default
   * 
   * @throws Exception  the exception
   */
  public void addFileLanguage(Node node, Value value, String mimeType, String language, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    Workspace ws = node.getSession().getWorkspace() ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(Utils.LANGUAGE_HOME)) languagesNode = node.getNode(Utils.LANGUAGE_HOME) ;
    else languagesNode = node.addNode(Utils.LANGUAGE_HOME, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        else newLanguageNode = languagesNode.addNode(defaultLanguage) ;
        Node jcrContent = node.getNode(JCRCONTENT) ;
        node.save() ;
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
        jcrContent.setProperty(JCR_MIMETYPE, mimeType) ;
        jcrContent.setProperty(JCRDATA, value) ;
      } else {
        if(languagesNode.hasNode(language)) newLanguageNode = languagesNode.getNode(language) ;
        else newLanguageNode = languagesNode.addNode(language) ;
        Node jcrContent = node.getNode(JCRCONTENT) ;
        node.save() ;
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
        newLanguageNode.getNode(JCRCONTENT).setProperty(JCR_MIMETYPE, mimeType) ;
        newLanguageNode.getNode(JCRCONTENT).setProperty(JCRDATA, value) ;        
      }
      // add mixin type for node
      setMixin(node, newLanguageNode) ;
    } else {
      node.getNode(JCRCONTENT).setProperty(JCRDATA, value) ;   
    }
    if(!defaultLanguage.equals(language) && isDefault){
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  /**
   * Adds the file language.
   * 
   * @param node the node
   * @param language the language
   * @param mappings the mappings
   * @param isDefault the is default
   * 
   * @throws Exception the exception
   */
  public void addFileLanguage(Node node, String language, Map mappings, boolean isDefault) throws Exception {
    Node newLanguageNode = null ;
    Node languagesNode = null ;
    Workspace ws = node.getSession().getWorkspace() ;
    String defaultLanguage = getDefault(node) ;
    if(node.hasNode(Utils.LANGUAGE_HOME)) languagesNode = node.getNode(Utils.LANGUAGE_HOME) ;
    else languagesNode = node.addNode(Utils.LANGUAGE_HOME, NTUNSTRUCTURED) ;
    if(!defaultLanguage.equals(language)){
      if(isDefault) {
        if(languagesNode.hasNode(defaultLanguage)) newLanguageNode = languagesNode.getNode(defaultLanguage) ;
        else newLanguageNode = languagesNode.addNode(defaultLanguage) ;
      } else {
        if(languagesNode.hasNode(language)) newLanguageNode = languagesNode.getNode(language) ;
        else newLanguageNode = languagesNode.addNode(language) ;
      }
      Node jcrContent = node.getNode(JCRCONTENT) ;
      node.save() ;
      if(!newLanguageNode.hasNode(JCRCONTENT)) {
        ws.copy(jcrContent.getPath(), newLanguageNode.getPath() + "/" + jcrContent.getName()) ;
      }
      Node newContentNode = newLanguageNode.getNode(JCRCONTENT) ;
      PropertyIterator props = newContentNode.getProperties() ;
      while(props.hasNext()) {
        Property prop = props.nextProperty() ;
        if(mappings.containsKey(CONTENT_PATH + prop.getName())) {
          JcrInputProperty inputVariable = (JcrInputProperty) mappings.get(CONTENT_PATH + prop.getName()) ;
          boolean isMultiple = prop.getDefinition().isMultiple() ;
          setPropertyValue(prop.getName(), newContentNode, prop.getType(), inputVariable.getValue(), isMultiple) ;
        }
      }
      if(isDefault) {
        Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
        node.getSession().move(node.getNode(JCRCONTENT).getPath(), tempNode.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(newLanguageNode.getNode(JCRCONTENT).getPath(), node.getPath() + "/" + JCRCONTENT) ;
        node.getSession().move(tempNode.getNode(JCRCONTENT).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + JCRCONTENT) ;
        tempNode.remove() ;
      }
      // add mixin type for node
      setMixin(node, newLanguageNode) ;
    } else {
      JcrInputProperty inputVariable = (JcrInputProperty) mappings.get(CONTENT_PATH + JCRDATA) ;
      setPropertyValue(JCRDATA, node.getNode(JCRCONTENT), inputVariable.getType(), inputVariable.getValue(), false) ;
    }
    PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
    for(PropertyDefinition pro : properties){
      if(!pro.isProtected()) {
        String propertyName = pro.getName() ;
        JcrInputProperty property = (JcrInputProperty)mappings.get(NODE + propertyName) ;
        if(defaultLanguage.equals(language) && property != null) {
          setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
        } else {          
          if(isDefault) {            
            if(node.hasProperty(propertyName)) {
              Object value = null ;
              int requiredType = node.getProperty(propertyName).getDefinition().getRequiredType() ;
              boolean isMultiple = node.getProperty(propertyName).getDefinition().isMultiple() ;
              if(isMultiple) value = node.getProperty(propertyName).getValues() ;
              else value = node.getProperty(propertyName).getValue() ;
              setPropertyValue(propertyName, newLanguageNode, requiredType, value, isMultiple) ;
            }
            if(property != null) {
              setPropertyValue(propertyName, node, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          } else {
            if(property != null) {
              setPropertyValue(propertyName, newLanguageNode, pro.getRequiredType(), property.getValue(), pro.isMultiple()) ;
            }
          }
        }               
      }
    }    
    if(!defaultLanguage.equals(language) && isDefault) {
      Node selectedLangNode = null ;
      if(languagesNode.hasNode(language)) selectedLangNode = languagesNode.getNode(language) ;
      setVoteProperty(newLanguageNode, node, selectedLangNode) ;
      setCommentNode(node, newLanguageNode, selectedLangNode) ;
    }
    if(isDefault) node.setProperty(EXO_LANGUAGE, language) ;
    node.save() ;
    node.getSession().save() ;    
  }
  
  /**
   * Gets the default.
   * 
   * @param node the node
   * 
   * @return the default
   * 
   * @throws Exception the exception
   */
  public String getDefault(Node node) throws Exception {
    if(node.hasProperty(EXO_LANGUAGE)) return node.getProperty(EXO_LANGUAGE).getString() ;
    return null ;
  }

  /**
   * Gets the supported languages.
   * 
   * @param node the node
   * 
   * @return the supported languages
   * 
   * @throws Exception the exception
   */
  public List<String> getSupportedLanguages(Node node) throws Exception {
    List<String> languages = new ArrayList<String>();
    String defaultLang = getDefault(node) ;
    if(defaultLang != null) languages.add(defaultLang) ;
    if(node.hasNode(Utils.LANGUAGE_HOME)){
      Node languageNode = node.getNode(Utils.LANGUAGE_HOME) ;
      NodeIterator iter  = languageNode.getNodes() ;      
      while(iter.hasNext()) {
        languages.add(iter.nextNode().getName());
      }
    } 
    return languages;
  }

  /**
   * Sets the vote property.
   * 
   * @param newLang the new lang
   * @param node the node
   * @param selectedLangNode the selected lang node
   * 
   * @throws Exception the exception
   */
  private void setVoteProperty(Node newLang, Node node, Node selectedLangNode) throws Exception {
    if(hasMixin(newLang, "mix:votable")) {
      newLang.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
      newLang.setProperty(VOTE_TOTAL_LANG_PROP, node.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
      newLang.setProperty(VOTING_RATE_PROP, node.getProperty(VOTING_RATE_PROP).getLong()) ;
      if(node.hasProperty(VOTER_PROP)) {
        newLang.setProperty(VOTER_PROP, node.getProperty(VOTER_PROP).getValues()) ;
      }
      if(selectedLangNode != null) {
        node.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ; 
        if(selectedLangNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
          node.setProperty(VOTE_TOTAL_LANG_PROP, selectedLangNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong()) ;
        } else {
          node.setProperty(VOTE_TOTAL_LANG_PROP, 0) ;
        }
        if(selectedLangNode.hasProperty(VOTING_RATE_PROP)) {
          node.setProperty(VOTING_RATE_PROP, selectedLangNode.getProperty(VOTING_RATE_PROP).getLong()) ;
        } else {
          node.setProperty(VOTING_RATE_PROP, 0) ;
        }
        if(selectedLangNode.hasProperty(VOTER_PROP)) {
          node.setProperty(VOTER_PROP, selectedLangNode.getProperty(VOTER_PROP).getValues()) ;
        }
      } else {
        node.setProperty(VOTE_TOTAL_PROP, getVoteTotal(node)) ;
        node.setProperty(VOTE_TOTAL_LANG_PROP, 0) ;
        node.setProperty(VOTING_RATE_PROP, 0) ;
      }
    }
  }
  
  /**
   * Sets the comment node.
   * 
   * @param node the node
   * @param newLang the new lang
   * @param selectedLangNode the selected lang node
   * 
   * @throws Exception the exception
   */
  private void setCommentNode(Node node, Node newLang, Node selectedLangNode) throws Exception {
    if(node.hasNode(COMMENTS)) {
      node.getSession().move(node.getPath() + "/" + COMMENTS, newLang.getPath() + "/" + COMMENTS) ;
    }
    if(selectedLangNode != null && selectedLangNode.hasNode(COMMENTS)) {
      node.getSession().move(selectedLangNode.getPath() + "/" + COMMENTS, node.getPath() + "/" + COMMENTS) ;
    }
  }
  
  /**
   * Gets the vote total.
   * 
   * @param node the node
   * 
   * @return the vote total
   * 
   * @throws Exception the exception
   */
  public long getVoteTotal(Node node) throws Exception {
    long voteTotal = 0;
    if(!node.hasNode(Utils.LANGUAGE_HOME) && node.hasProperty(VOTE_TOTAL_PROP)) {
      return node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    }
    Node multiLanguages = node.getNode(Utils.LANGUAGE_HOME) ;
    voteTotal = node.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
    NodeIterator nodeIter = multiLanguages.getNodes() ;
    String defaultLang = getDefault(node) ;
    while(nodeIter.hasNext()) {
      Node languageNode = nodeIter.nextNode() ;
      if(!languageNode.getName().equals(defaultLang) && languageNode.hasProperty(VOTE_TOTAL_LANG_PROP)) {
        voteTotal = voteTotal + languageNode.getProperty(VOTE_TOTAL_LANG_PROP).getLong() ;
      }
    }
    return voteTotal ;
  }
  
  /**
   * Checks for mixin.
   * 
   * @param node the node
   * @param nodeTypeName the node type name
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  private boolean hasMixin(Node node, String nodeTypeName) throws Exception {
    NodeType[] mixinTypes = node.getMixinNodeTypes() ; 
    for(NodeType nodeType : mixinTypes) {
      if(nodeType.getName().equals(nodeTypeName)) return true ;
    }
    return false ;
  }
  
  /**
   * Sets the default language for the Node. If this language is not
   * default, get language node have name is <code>language</code>
   * and swap all properties of this language node with default language
   * 
   * @param node        the node is 
   * @param language    the name of language which will be default language 
   * 
   * @throws Exception  the exception when language node not found
   */
  public void setDefault(Node node, String language) throws Exception {
    String defaultLanguage = getDefault(node) ;
    if(!defaultLanguage.equals(language)){
      Node languagesNode = null ;
      if(node.hasNode(Utils.LANGUAGE_HOME)) languagesNode = node.getNode(Utils.LANGUAGE_HOME) ;
      else languagesNode = node.addNode(Utils.LANGUAGE_HOME, NTUNSTRUCTURED) ;
      Node selectedLangNode = languagesNode.getNode(language) ;
      Node newLang = languagesNode.addNode(defaultLanguage) ;
      PropertyDefinition[] properties = node.getPrimaryNodeType().getPropertyDefinitions() ;
      for(PropertyDefinition pro : properties){
        if(!pro.isProtected()){
          String propertyName = pro.getName() ;
          if(node.hasProperty(propertyName)) {
            if(node.getProperty(propertyName).getDefinition().isMultiple()) {
              Value[] values = node.getProperty(propertyName).getValues() ;
              newLang.setProperty(propertyName, values) ;
            } else {
              newLang.setProperty(propertyName, node.getProperty(propertyName).getValue()) ;
            }
          }
          if(selectedLangNode.hasProperty(propertyName)) {
            if(selectedLangNode.getProperty(propertyName).getDefinition().isMultiple()) {
              Value[] values = selectedLangNode.getProperty(propertyName).getValues() ;
              node.setProperty(propertyName, values) ;
            } else {
              node.setProperty(propertyName, selectedLangNode.getProperty(propertyName).getValue()) ;
            }
          }
        }
      }
      if(hasNodeTypeNTResource(node)) {
        processWithDataChildNode(node, selectedLangNode, languagesNode, defaultLanguage, getChildNodeType(node)) ;
      }
      setMixin(node, newLang) ;
      setVoteProperty(newLang, node, selectedLangNode) ;
      node.setProperty(EXO_LANGUAGE, language) ;
      setCommentNode(node, newLang, selectedLangNode) ;
      selectedLangNode.remove() ;
      node.save() ;
      node.getSession().save() ;
    }
  }
  
  /**
   * Process with data child node.
   * 
   * @param node              the node
   * @param selectedLangNode  the selected lang node
   * @param languagesNode     the languages node
   * @param defaultLanguage   the default language
   * @param nodeType          the node type
   * 
   * @throws Exception        the exception
   */
  private void processWithDataChildNode(Node node, Node selectedLangNode, Node languagesNode, 
      String defaultLanguage, String nodeType) throws Exception {
    Node tempNode = node.addNode(TEMP_NODE, "nt:unstructured") ;
    node.getSession().move(node.getNode(nodeType).getPath(), tempNode.getPath() + "/" + nodeType) ;
    node.getSession().move(selectedLangNode.getNode(nodeType).getPath(), node.getPath() + "/" + nodeType) ;
    node.getSession().move(tempNode.getNode(nodeType).getPath(), languagesNode.getPath() + "/" + defaultLanguage + "/" + nodeType) ;
    tempNode.remove() ;
  }
  
  /**
   * Checks for node type nt resource. Get all children node of this node,
   * if one of children node have type is <code>nt:resource</code> return
   * <code>true</code> opposite return <code>false</code>
   * 
   * @param node        the node
   * 
   * @return            <code>trie</code> if one of children node have type is
   *                    <code>nt:resource</code> and <code>false</code> if opposite
   * 
   * @throws Exception  the exception
   */
  private boolean hasNodeTypeNTResource(Node node) throws Exception {
    if(node.hasNodes()) {
      NodeIterator nodeIter = node.getNodes() ;
      while(nodeIter.hasNext()) {
        Node childNode = nodeIter.nextNode() ;
        if(childNode.getPrimaryNodeType().getName().equals("nt:resource")) return true ;
      }
    }
    return false ;
  }
  
  /**
   * Gets the child node type.
   * 
   * @param node the node
   * 
   * @return the child node type
   * 
   * @throws Exception the exception
   */
  private String getChildNodeType(Node node) throws Exception {
    if(node.hasNodes()) {
      NodeIterator nodeIter = node.getNodes() ;
      while(nodeIter.hasNext()) {
        Node childNode = nodeIter.nextNode() ;
        if(childNode.getPrimaryNodeType().getName().equals("nt:resource")) return childNode.getName() ;
      }
    }
    return null ;
  }

  /**
   * get language node which have name is <code>language</code> and all properties of
   * this language node.
   * 
   * @param node        the node
   * @param language    the name of language
   * 
   * @return            language node which have name is <code>language</code>
   * 
   * @throws Exception the exception
   */
  public Node getLanguage(Node node, String language) throws Exception {
  	if(node.hasNode(Utils.LANGUAGE_HOME + "/"+ language)) return node.getNode(Utils.LANGUAGE_HOME + "/"+ language) ;
    return null;
  }
  

}
