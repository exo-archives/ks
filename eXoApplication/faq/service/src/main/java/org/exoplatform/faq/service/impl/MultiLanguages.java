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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.faq.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQNodeTypes;
import org.exoplatform.faq.service.QuestionLanguage;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.ks.common.jcr.PropertyReader;
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

public class MultiLanguages implements FAQNodeTypes {
  static private Log         log                  = ExoLogger.getLogger(MultiLanguages.class);

  /**
   * Class constructor, instantiates a new multi languages.
   * 
   */
  public MultiLanguages() {
  }

  private static Node getLanguageNodeByLanguage(Node questionNode, String language) throws Exception {
    if (language.equals(questionNode.getProperty(EXO_LANGUAGE).getString())) {
      return questionNode;
    }
    NodeIterator nodeIterator = questionNode.getNode(Utils.LANGUAGE_HOME).getNodes();
    Node languageNode = null;
    while (nodeIterator.hasNext()) {
      languageNode = nodeIterator.nextNode();
      if (languageNode.getProperty(EXO_LANGUAGE).getString().equals(language)) {
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
  public static void addLanguage(Node questionNode, QuestionLanguage language) throws Exception {
    if (!questionNode.isNodeType(MIX_FAQI_1_8N)) {
      questionNode.addMixin(MIX_FAQI_1_8N);
    }
    Node languageHome = null;
    try {
      languageHome = questionNode.getNode(Utils.LANGUAGE_HOME);
    } catch (Exception e) {
      languageHome = questionNode.addNode(Utils.LANGUAGE_HOME, EXO_QUESTION_LANGUAGE_HOME);
    }
    Node langNode = null;
    try {
      langNode = languageHome.getNode(language.getId());
    } catch (Exception e) {
      langNode = languageHome.addNode(language.getId(), EXO_FAQ_LANGUAGE);
    }
    langNode.setProperty(EXO_LANGUAGE, language.getLanguage());
    langNode.setProperty(EXO_NAME, language.getDetail());
    langNode.setProperty(EXO_TITLE, language.getQuestion());
    langNode.setProperty(EXO_QUESTION_ID, questionNode.getName());
    langNode.setProperty(EXO_CATEGORY_ID, questionNode.getProperty(EXO_CATEGORY_ID).getString());
    if (langNode.isNew())
      questionNode.getSession().save();
    else
      questionNode.save();
  }

  public static void deleteAnswerQuestionLang(Node questionNode, String answerId, String language) throws Exception {
    Node answerNode;
    if (language != null && language.length() > 0) {
      Node languageNode = getLanguageNodeByLanguage(questionNode, language);
      answerNode = languageNode.getNode(Utils.ANSWER_HOME).getNode(answerId);
    } else {
      answerNode = questionNode.getNode(Utils.ANSWER_HOME).getNode(answerId);
    }
    answerNode.remove();
    questionNode.save();
  }

  public static void deleteCommentQuestionLang(Node questionNode, String commentId, String language) throws Exception {
    Node languageNode = getLanguageNodeByLanguage(questionNode, language);
    Node commnetNode = languageNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
    commnetNode.remove();
    questionNode.save();
  }

  public static QuestionLanguage getQuestionLanguageByLanguage(Node questionNode, String language) throws Exception {
    // QuestionLanguage questionLanguage = new QuestionLanguage();
    // questionLanguage.setLanguage(language);
    Node languageNode = getLanguageNodeByLanguage(questionNode, language);
    /*
     * questionLanguage.setId(getName()); questionLanguage.setLanguage(reader.string(EXO_LANGUAGE, "")); questionLanguage.setDetail(getProperty(EXO_NAME).getString()); questionLanguage.setQuestion(getProperty(EXO_TITLE).getString());
     */
    return getQuestionLanguage(languageNode);
  }

  private static QuestionLanguage getQuestionLanguage(Node questionNode) throws Exception {
    QuestionLanguage questionLanguage = new QuestionLanguage();
    PropertyReader reader = new PropertyReader(questionNode);
    questionLanguage.setState(QuestionLanguage.VIEW);
    questionLanguage.setId(questionNode.getName());
    questionLanguage.setLanguage(reader.string(EXO_LANGUAGE, ""));
    questionLanguage.setQuestion(reader.string(EXO_TITLE, ""));
    questionLanguage.setDetail(reader.string(EXO_NAME, ""));
    Comment[] comments = getComment(questionNode);
    Answer[] answers = getAnswers(questionNode);
    questionLanguage.setComments(comments);
    questionLanguage.setAnswers(answers);
    return questionLanguage;
  }

  private static Comment[] getComment(Node questionNode) throws Exception {
    try {
      if (!questionNode.hasNode(Utils.COMMENT_HOME))
        return new Comment[] {};
      NodeIterator nodeIterator = questionNode.getNode(Utils.COMMENT_HOME).getNodes();
      Comment[] comments = new Comment[(int) nodeIterator.getSize()];
      Node commentNode = null;
      int i = 0;
      while (nodeIterator.hasNext()) {
        commentNode = nodeIterator.nextNode();
        comments[i] = getCommentByNode(commentNode);
        i++;
      }
      return comments;
    } catch (Exception e) {

      return new Comment[] {};
    }
  }

  private static Comment getCommentByNode(Node commentNode) throws Exception {
    Comment comment = new Comment();
    comment.setId(commentNode.getName());
    PropertyReader reader = new PropertyReader(commentNode);
    comment.setComments((reader.string(EXO_COMMENTS, "")));
    comment.setCommentBy((reader.string(EXO_COMMENT_BY, "")));
    comment.setDateComment((commentNode.getProperty(EXO_DATE_COMMENT).getDate().getTime()));
    comment.setFullName((reader.string(EXO_FULL_NAME, "")));
    comment.setPostId(reader.string(EXO_POST_ID, ""));
    return comment;
  }

  private static Answer[] getAnswers(Node questionNode) throws Exception {
    try {
      if (!questionNode.hasNode(Utils.ANSWER_HOME))
        return new Answer[] {};
      NodeIterator nodeIterator = questionNode.getNode(Utils.ANSWER_HOME).getNodes();
      List<Answer> answers = new ArrayList<Answer>();
      Answer ans;
      String language = questionNode.getProperty(EXO_LANGUAGE).getString();
      while (nodeIterator.hasNext()) {
        try {
          ans = getAnswerByNode(nodeIterator.nextNode());
          ans.setLanguage(language);
          answers.add(ans);
        } catch (Exception e) {
          if (log.isDebugEnabled()) {
            log.debug("Failed to achieve an answer", e);
          }
        }
      }
      return answers.toArray(new Answer[] {});
    } catch (Exception e) {
      log.error("Fail to get answers: ", e);
    }
    return new Answer[] {};
  }

  private static Answer getAnswerByNode(Node answerNode) throws Exception {
    PropertyReader reader = new PropertyReader(answerNode);
    Answer answer = new Answer();
    answer.setId(answerNode.getName());
    answer.setResponses((reader.string(EXO_RESPONSES, "")));
    answer.setResponseBy((reader.string(EXO_RESPONSE_BY, "")));
    answer.setFullName((reader.string(EXO_FULL_NAME, "")));
    answer.setDateResponse((answerNode.getProperty(EXO_DATE_RESPONSE).getDate().getTime()));
    answer.setUsersVoteAnswer(reader.strings(EXO_USERS_VOTE_ANSWER, new String[] {}));
    answer.setMarkVotes(reader.l(EXO_MARK_VOTES));
    answer.setApprovedAnswers(reader.bool(EXO_APPROVE_RESPONSES, true));
    answer.setActivateAnswers(reader.bool(EXO_ACTIVATE_RESPONSES, true));
    answer.setPostId(reader.string(EXO_POST_ID, ""));
    String path = answerNode.getPath();
    answer.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
    return answer;
  }

  public static Comment getCommentById(Node questionNode, String commentId, String language) throws Exception {
    try {
      Node commentNode;
      if (language != null && language.length() > 0) {
        Node languageNode = getLanguageNodeByLanguage(questionNode, language);
        commentNode = languageNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
      } else {
        commentNode = questionNode.getNode(Utils.COMMENT_HOME).getNode(commentId);
      }
      Comment comment = new Comment();
      PropertyReader reader = new PropertyReader(commentNode);
      comment.setId((reader.string(EXO_ID, "")));
      comment.setComments((reader.string(EXO_COMMENTS, "")));
      comment.setCommentBy((reader.string(EXO_COMMENT_BY, "")));
      comment.setFullName((reader.string(EXO_FULL_NAME, "")));
      comment.setDateComment(reader.date(EXO_DATE_COMMENT));
      comment.setPostId(reader.string(EXO_POST_ID, ""));
      return comment;
    } catch (Exception e) {
      return null;
    }
  }

  public static Answer getAnswerById(Node questionNode, String answerid, String language) throws Exception {
    Answer answer = new Answer();
    try {
      Node answerNode;
      if (language != null && language.length() > 0) {
        Node languageNode = getLanguageNodeByLanguage(questionNode, language);
        answerNode = languageNode.getNode(Utils.ANSWER_HOME).getNode(answerid);
      } else {
        answerNode = questionNode.getNode(Utils.ANSWER_HOME).getNode(answerid);
      }
      PropertyReader reader = new PropertyReader(answerNode);
      answer.setId((reader.string(EXO_ID, "")));
      String path = answerNode.getPath();
      answer.setPath(path.substring(path.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1));
      answer.setResponses((reader.string(EXO_RESPONSES, "")));
      answer.setResponseBy((reader.string(EXO_RESPONSE_BY, "")));
      answer.setFullName((reader.string(EXO_FULL_NAME, "")));
      answer.setDateResponse(reader.date(EXO_DATE_RESPONSE));
      answer.setUsersVoteAnswer(reader.strings(EXO_USERS_VOTE_ANSWER, new String[] {}));
      answer.setMarkVotes(reader.l(EXO_MARK_VOTES));
      answer.setApprovedAnswers(reader.bool(EXO_APPROVE_RESPONSES, true));
      answer.setActivateAnswers(reader.bool(EXO_ACTIVATE_RESPONSES, true));
      answer.setPostId(reader.string(EXO_POST_ID, ""));
      answer.setLanguage(reader.string(EXO_RESPONSE_LANGUAGE, ""));
      return answer;
    } catch (Exception e) {
      return null;
    }
  }

  public static void saveAnswer(Node questionNode, Answer answer, String language) throws Exception {
    Node answerHome;
    Node answerNode;

    String defaultLang = questionNode.getProperty(EXO_LANGUAGE).getString();
    if (language != null && language.length() > 0 && !language.equals(defaultLang)) {
      Node languageNode = getLanguageNodeByLanguage(questionNode, language);
      if (!languageNode.isNodeType(MIX_FAQI_1_8N)) {
        languageNode.addMixin(MIX_FAQI_1_8N);
      }
      try {
        answerHome = languageNode.getNode(Utils.ANSWER_HOME);
      } catch (Exception e) {
        answerHome = languageNode.addNode(Utils.ANSWER_HOME, EXO_ANSWER_HOME);
      }
      answer.setLanguage(language);
    } else {
      try {
        answerHome = questionNode.getNode(Utils.ANSWER_HOME);
      } catch (Exception e) {
        answerHome = questionNode.addNode(Utils.ANSWER_HOME, EXO_ANSWER_HOME);
      }
      answer.setLanguage(questionNode.getProperty(EXO_LANGUAGE).getString());
    }
    try {
      answerNode = answerHome.getNode(answer.getId());
    } catch (Exception e) {
      answerNode = answerHome.addNode(answer.getId(), EXO_ANSWER);
    }
    answerNode.setProperty(EXO_RESPONSES, answer.getResponses());
    answerNode.setProperty(EXO_RESPONSE_BY, answer.getResponseBy());
    answerNode.setProperty(EXO_FULL_NAME, answer.getFullName());
    answerNode.setProperty(EXO_USERS_VOTE_ANSWER, answer.getUsersVoteAnswer());
    answerNode.setProperty(EXO_MARK_VOTES, answer.getMarkVotes());

    Date answerDate = null;
    if (answer.isNew()) {
      java.util.Calendar calendar = null;
      calendar = GregorianCalendar.getInstance();
      answerDate = new Date();
      calendar.setTime(answerDate);
      answerNode.setProperty(EXO_DATE_RESPONSE, calendar);
      answerNode.setProperty(EXO_ID, answer.getId());
      answerNode.setProperty(EXO_QUESTION_ID, questionNode.getName());
      answerNode.setProperty(EXO_RESPONSE_LANGUAGE, language);
      answerNode.setProperty(EXO_CATEGORY_ID, questionNode.getProperty(EXO_CATEGORY_ID).getString());
      answerNode.setProperty(EXO_APPROVE_RESPONSES, answer.getApprovedAnswers());
      answerNode.setProperty(EXO_ACTIVATE_RESPONSES, answer.getActivateAnswers());
    } else {
      if (answerNode.hasProperty(EXO_DATE_RESPONSE)) {
        answerDate = answerNode.getProperty(EXO_DATE_RESPONSE).getDate().getTime();
      }
      if (new PropertyReader(answerNode).bool(EXO_APPROVE_RESPONSES, false) != answer.getApprovedAnswers())
        answerNode.setProperty(EXO_APPROVE_RESPONSES, answer.getApprovedAnswers());
      if (new PropertyReader(answerNode).bool(EXO_ACTIVATE_RESPONSES, false) != answer.getActivateAnswers())
        answerNode.setProperty(EXO_ACTIVATE_RESPONSES, answer.getActivateAnswers());
    }
    if (answer.getPostId() != null && answer.getPostId().length() > 0) {
      answerNode.setProperty(EXO_POST_ID, answer.getPostId());
    }

    if (questionNode.isNew()) {
      questionNode.getSession().save();
    } else {
      questionNode.save();
    }
  }

  public static void saveAnswer(Node quesNode, QuestionLanguage questionLanguage) throws Exception {
    Node quesLangNode;

    try {
      quesLangNode = quesNode.getNode(Utils.LANGUAGE_HOME).getNode(questionLanguage.getId());
    } catch (Exception e) {
      quesLangNode = quesNode.getNode(Utils.LANGUAGE_HOME).addNode(questionLanguage.getId(), EXO_FAQ_LANGUAGE);
      quesNode.getSession().save();
    }
    if (!quesLangNode.isNodeType(MIX_FAQI_1_8N)) {
      quesLangNode.addMixin(MIX_FAQI_1_8N);
    }
    Node answerHome = null;
    Node answerNode;
    Answer[] answers = questionLanguage.getAnswers();
    try {
      answerHome = quesLangNode.getNode(Utils.ANSWER_HOME);
    } catch (Exception e) {
      answerHome = quesLangNode.addNode(Utils.ANSWER_HOME, EXO_ANSWER_HOME);
    }
    if (!answerHome.isNew()) {
      List<String> listNewAnswersId = new ArrayList<String>();
      for (int i = 0; i < answers.length; i++) {
        listNewAnswersId.add(answers[i].getId());
      }
      NodeIterator nodeIterator = answerHome.getNodes();
      while (nodeIterator.hasNext()) {
        answerNode = nodeIterator.nextNode();
        if (!listNewAnswersId.contains(answerNode.getName()))
          answerNode.remove();
      }
    }
    for (Answer answer : answers) {
      answerNode = null;
      try {
        answerNode = answerHome.getNode(answer.getId());
      } catch (Exception e) {
        answerNode = answerHome.addNode(answer.getId(), EXO_ANSWER);
        answerNode.setProperty(EXO_ID, answer.getId());
      }
      Date answerDate = null;
      if (answerNode.isNew()) {
        java.util.Calendar calendar = null;
        calendar = null;
        calendar = GregorianCalendar.getInstance();
        answerDate = new Date();
        calendar.setTime(answerDate);
        answerNode.setProperty(EXO_DATE_RESPONSE, calendar);
        answerNode.setProperty(EXO_QUESTION_ID, quesNode.getName());
        answerNode.setProperty(EXO_CATEGORY_ID, quesNode.getProperty(EXO_CATEGORY_ID).getString());
      } else {
        if (answerNode.hasProperty(EXO_DATE_RESPONSE)) {
          answerDate = answerNode.getProperty(EXO_DATE_RESPONSE).getDate().getTime();
        }
      }
      String language = answer.getLanguage();
      if (language == null || language.length() == 0)
        language = questionLanguage.getLanguage();
      answerNode.setProperty(EXO_RESPONSES, answer.getResponses());
      answerNode.setProperty(EXO_RESPONSE_BY, answer.getResponseBy());
      answerNode.setProperty(EXO_FULL_NAME, answer.getFullName());
      answerNode.setProperty(EXO_APPROVE_RESPONSES, answer.getApprovedAnswers());
      answerNode.setProperty(EXO_ACTIVATE_RESPONSES, answer.getActivateAnswers());
      answerNode.setProperty(EXO_USERS_VOTE_ANSWER, answer.getUsersVoteAnswer());
      answerNode.setProperty(EXO_MARK_VOTES, answer.getMarkVotes());
      answerNode.setProperty(EXO_RESPONSE_LANGUAGE, language);
      if (answer.getPostId() != null && answer.getPostId().length() > 0) {
        answerNode.setProperty(EXO_POST_ID, answer.getPostId());
      }

      if (answerNode.isNew())
        quesNode.getSession().save();
      else
        quesNode.save();
    }
  }

  public static void saveComment(Node questionNode, Comment comment, String language) throws Exception {
    String lang;
    Node commentHome;
    Node commentNode;

    if (language != null && language.length() > 0) {
      Node languageNode = getLanguageNodeByLanguage(questionNode, language);
      lang = language;
      if (!languageNode.isNodeType(MIX_FAQI_1_8N)) {
        languageNode.addMixin(MIX_FAQI_1_8N);
      }
      try {
        commentHome = languageNode.getNode(Utils.COMMENT_HOME);
      } catch (Exception e) {
        commentHome = languageNode.addNode(Utils.COMMENT_HOME, EXO_COMMENT_HOME);
      }
    } else {
      try {
        commentHome = questionNode.getNode(Utils.COMMENT_HOME);
      } catch (Exception e) {
        commentHome = questionNode.addNode(Utils.COMMENT_HOME, EXO_COMMENT_HOME);
      }
      lang = questionNode.getProperty(EXO_LANGUAGE).getString();
    }
    try {
      commentNode = commentHome.getNode(comment.getId());
    } catch (Exception e) {
      commentNode = commentHome.addNode(comment.getId(), EXO_COMMENT);
      commentNode.setProperty(EXO_ID, comment.getId());
    }
    commentNode.setProperty(EXO_COMMENTS, comment.getComments());
    commentNode.setProperty(EXO_COMMENT_BY, comment.getCommentBy());
    commentNode.setProperty(EXO_FULL_NAME, comment.getFullName());
    commentNode.setProperty(EXO_CATEGORY_ID, questionNode.getProperty(EXO_CATEGORY_ID).getString());
    commentNode.setProperty(EXO_QUESTION_ID, questionNode.getName());
    commentNode.setProperty(EXO_COMMENT_LANGUAGE, lang);
    if (comment.getPostId() != null && comment.getPostId().length() > 0) {
      commentNode.setProperty(EXO_POST_ID, comment.getPostId());
    }
    Date commentTime = null;
    if (commentNode.isNew()) {
      java.util.Calendar calendar = null;
      calendar = GregorianCalendar.getInstance();
      commentTime = new Date();
      calendar.setTime(commentTime);
      commentNode.setProperty(EXO_DATE_COMMENT, calendar);
      // questionNode.getSession().save();
    }

    questionNode.save();
  }

  protected Value[] booleanToValues(Node node, Boolean[] bools) throws Exception {
    if (bools == null)
      return new Value[] { node.getSession().getValueFactory().createValue(true) };
    Value[] values = new Value[bools.length];
    for (int i = 0; i < values.length; i++) {
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
      if (!questionNode.hasNode(Utils.LANGUAGE_HOME))
        return;
      Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME);
      NodeIterator nodeIterator = languageNode.getNodes();
      Node node = null;
      while (nodeIterator.hasNext()) {
        node = nodeIterator.nextNode();
        if (!listLanguage.contains(node.getProperty(EXO_LANGUAGE).getString())) {
          node.remove();
        }
      }
      questionNode.getSession().save();
    } catch (Exception e) {
      log.error("Fail to remove language from a list of language: ", e);
    }
  }

  public static void removeLanguage(Node questionNode, QuestionLanguage lang) {
    try {
      if (!questionNode.hasNode(Utils.LANGUAGE_HOME))
        return;
      Node languageNode = questionNode.getNode(Utils.LANGUAGE_HOME);
      languageNode.getNode(lang.getId()).remove();
      questionNode.save();
    } catch (Exception e) {
      log.error("Fail to remove language from a question language: ", e);
    }
  }

  public static void voteAnswer(Node answerNode, String userName, boolean isUp) throws Exception {
    boolean isVoted = false;
    long mark = 0;
    List<String> users = new ArrayList<String>();
    if (answerNode.hasProperty(EXO_USERS_VOTE_ANSWER) && answerNode.hasProperty(EXO_MARK_VOTES)) {
      PropertyReader reader = new PropertyReader(answerNode);
      users = reader.list(EXO_USERS_VOTE_ANSWER, new ArrayList<String>());
      mark = reader.l(EXO_MARK_VOTES);
      int i = 0;
      for (String user : users) {
        if (user.indexOf(userName) > -1) {
          String[] values = user.split("/");
          if (values[1].equals("1")) { // up
            if (!isUp) {
              mark = mark - 2;
              users.add(i, userName + "/-1");
            }
          } else { // -1: down
            if (isUp) {
              mark = mark + 2;
              users.add(i, userName + "/1");
            }
          }
          isVoted = true;
          break;
        }
        i++;
      }
    }
    if (isVoted) {
      answerNode.setProperty(EXO_USERS_VOTE_ANSWER, users.toArray(new String[] {}));
      answerNode.setProperty(EXO_MARK_VOTES, mark);
    } else {
      List<String> newUsers = users;
      // if(users.length > 0) newUsers = Arrays.asList(users) ;
      if (isUp) {
        mark = mark + 1;
        newUsers.add(userName + "/1");
      } else {
        mark = mark - 1;
        newUsers.add(userName + "/-1");
      }
      answerNode.setProperty(EXO_USERS_VOTE_ANSWER, newUsers.toArray(new String[] {}));
      answerNode.setProperty(EXO_MARK_VOTES, mark);
    }
    // System.out.println("mark ==>" + mark);
    answerNode.save();
  }

  public static void voteQuestion(Node questionNode, String userName, int number) throws Exception {
    if (questionNode.hasProperty(EXO_MARK_VOTE) && questionNode.hasProperty(EXO_USERS_VOTE)) {
      double mark = questionNode.getProperty(EXO_MARK_VOTE).getDouble();
      List<String> currentUsers = new ArrayList<String>();
      currentUsers.addAll(new PropertyReader(questionNode).list(EXO_USERS_VOTE, new ArrayList<String>()));
      double currentMark = (mark * currentUsers.size() + number) / (currentUsers.size() + 1);
      currentUsers.add(userName + "/" + number);
      questionNode.setProperty(EXO_MARK_VOTE, currentMark);
      questionNode.setProperty(EXO_USERS_VOTE, currentUsers.toArray(new String[] {}));
    } else {
      double mark = number;
      questionNode.setProperty(EXO_MARK_VOTE, mark);
      questionNode.setProperty(EXO_USERS_VOTE, new String[] { userName + "/" + number });
    }
    questionNode.save();
  }

  public static void unVoteQuestion(Node questionNode, String userName) throws Exception {
    PropertyReader reader = new PropertyReader(questionNode);
    List<String> userList = reader.list(EXO_USERS_VOTE, new ArrayList<String>());
    List<String> newList = new ArrayList<String>();
    double mark = 0;
    for (String user : userList) {
      // System.out.println("User ==>" + user);
      if (user.indexOf(userName + "/") == 0) {
        int number = Integer.parseInt(user.substring(user.indexOf("/") + 1));
        mark = reader.d(EXO_MARK_VOTE);
        if (userList.size() > 1) {
          mark = ((mark * userList.size()) - number) / (userList.size() - 1);
        } else {
          mark = 0;
        }
      } else {
        newList.add(user);
      }
    }
    // System.out.println("size ==>" + newList.size());
    questionNode.setProperty(EXO_MARK_VOTE, mark);
    questionNode.setProperty(EXO_USERS_VOTE, newList.toArray(new String[newList.size()]));
    questionNode.save();
  }
}
