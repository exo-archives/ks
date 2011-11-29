/**
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
 **/
package org.exoplatform.faq.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Data of question node is stored in question object which is used in processings:
 * add new question, edit question and reponse question.
 * 
 * @author : Hung Nguyen Quang
 */

public class Question {
  public static final String   QUESTION_ID             = "Question".intern();
  /** The id. */
  private String               id;

  /** The language. */
  private String               language;

  /** The question. */
  private String               question;

  /** The question. */
  private String               detail;

  /** The author. */
  private String               author;

  /** The email. */
  private String               email;

  /** The is activated. */
  private boolean              isActivated             = true;

  /** The is approved. */
  private boolean              isApproved              = true;

  /** The created date. */
  private Date                 createdDate;

  /** The category id. */
  private String               categoryId;

  /** The category id. */
  private String               categoryPath;

  /** The relations. */
  private String[]             relations;

  /** link to question. */
  private String               link                    = "";

  /** The list attachments. */
  private List<FileAttachment> listAttachments         = new ArrayList<FileAttachment>();

  /** language of question which is not yet answer. */
  private String               languagesNotYetAnswered = "";

  /** The name attachs. */
  private String[]             nameAttachs;

  /** The answers. */
  private Answer[]             answers;

  /** The comments. */
  private Comment[]            comments;

  /** The users vote. */
  private String[]             usersVote;

  /** The mark vote. */
  private double               markVote                = 0;

  /** The users watch. */
  private String[]             usersWatch              = null;

  /** The emails watch. */
  private String[]             emailsWatch             = null;

  private QuestionLanguage[]   multiLanguages;

  /** The path topic discuss. */
  private String               topicIdDiscuss;

  private String               path;

  /** author who make last activity of question */
  private String               authorOfLastActivity;

  /** the time when last activity appears */
  private long                 timeOfLastActivity      = -1;

  /** number of answers that are activated and approved */
  private long                 numberOfPublicAnswers   = 0;

  /**
   * Class constructor specifying id of object is created.
   */
  public Question() {
    id = QUESTION_ID + IdGenerator.generate();
    relations = new String[] {};
    multiLanguages = new QuestionLanguage[] {};
  }

  /**
   * Get id of Question object.
   * 
   * @return  question's id
   */
  public String getId() {
    return id;
  }

  /**
   * Set an id for Question object.
   * 
   * @param id  the id of question object
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get content of Question which is wanted answer.
   * 
   * @return  the content of question
   */
  public String getDetail() {
    return detail;
  }

  /**
   * Set content for Question object.
   * 
   * @param name  the content of question which is wanted answer
   */
  public void setDetail(String name) {
    this.detail = name;
  }

  /**
   * Set language for question, a language may be have multi languages but
   * all of languages is used must be supported in portal. And the language
   * which is useing in portal will be auto setted for this quetsion.
   * 
   * @param language the language is default language is used in system
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * Get question's language, this is default language of system
   * when created question.
   * 
   * @return language the language is default of system and question
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Set name for property author of this question, author is person who
   * write question and wait an answer.
   * 
   * @param author ther author of question
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * Get question's author who write question and wait an answer.
   * 
   * @return author  the name of question's author
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Get list questions in system which are like, related or support for this question.
   * 
   * @return  relations return list question's content is like this question
   */
  public String[] getRelations() {
    return relations;
  }

  /**
   * Registers list questions is related or supported for this question.
   * 
   * @param relations list questions have related with this question
   */
  public void setRelations(String[] relations) {
    this.relations = relations;
  }

  /**
   * Registers email address of question's author for this Question object.
   * 
   * @param email the email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Get email address of question's author.
   * 
   * @return email  the email address of person who write question
   */
  public String getEmail() {
    return email;
  }

  /**
   * Registers  date time for Question object.
   * 
   * @param createdDate the date time when question is created
   */
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  /**
   * Return date time when question is created or updated.
   * 
   * @return  the date time of question
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * Registers for this Question object is activated or not. This setting with
   * set approve for question will allow question is viewed or not
   * 
   * @param isActivated is <code>true</code> if this question is activated and
   * is <code>false</code> if this questiosn is not activated
   */
  public void setActivated(boolean isActivated) {
    this.isActivated = isActivated;
  }

  /**
   * Return status of this question, return <code>true</code> if
   * this question is activated and <code>false</code> if opposite.
   * 
   * @return    status of this question object, is activated or not
   */
  public boolean isActivated() {
    return isActivated;
  }

  /**
   * Registers for this question is approved or not. This setting with
   * set activate for question will allow this question is viewed or not
   * 
   * @param isApproved  is <code>true</code> if this question is approved and
   * is <code>false</code> if this question is not approved
   */
  public void setApproved(boolean isApproved) {
    this.isApproved = isApproved;
  }

  /**
   * Return status of thi question, return <code>true</code> if
   * this question is approved and <code>false</code> if opposite.
   * 
   * @return    status of question is approved or not
   */
  public boolean isApproved() {
    return isApproved;
  }

  /**
   * Registers id for property categoryId of Question object,
   * each question is contained in a category and this property is used to
   * point this cateogry.
   * 
   * @param catId id of category which contain this question
   */
  public void setCategoryId(String catId) {
    this.categoryId = catId;
  }

  /**
   * Get id of category which contain this question.
   * 
   * @return    an id of category which thi question
   */
  public String getCategoryId() {
    return categoryId;
  }

  /**
   * Relative path of parent category of Question object,
   * each question is contained in a category
   * 
   * @param categoryPath is relative path of category which contain this question
   */
  public void setCategoryPath(String categoryPath) {
    if(categoryPath != null && categoryPath.indexOf(Utils.FAQ_APP) > 0) {
      categoryPath = categoryPath.substring(categoryPath.indexOf(Utils.FAQ_APP) + Utils.FAQ_APP.length() + 1);
    }
    this.categoryPath = categoryPath;
  }

  /**
   * Get relative path of category which contain this question.
   * 
   * @return a relative path of category which thi question
   */
  public String getCategoryPath() {
    return categoryPath;
  }

  /**
   * Registers list files will be attach to this question to description for this question.
   * Each file have size is less than 10MB and larger 0B
   * 
   * @param listFile  list files are attached to question.
   */
  public void setAttachMent(List<FileAttachment> listFile) {
    this.listAttachments = listFile;
  }

  /**
   * Get list files are attached to this quetsion.
   * 
   * @return    list files are attached to this quetsion
   * 
   * @see       FileAttachment
   */
  public List<FileAttachment> getAttachMent() {
    return this.listAttachments;
  }

  /**
   * Get list languages of question which are not yet answered.
   * 
   * @return list languages
   */
  public String getLanguagesNotYetAnswered() {
    return languagesNotYetAnswered;
  }

  /**
   * Registers language is not yet answered.
   * 
   * @param languagesNotYetAnswered the languages not yet answered
   * 
   * @return the question
   */
  public Question setLanguagesNotYetAnswered(String languagesNotYetAnswered) {
    this.languagesNotYetAnswered = languagesNotYetAnswered;
    return this;
  }

  /**
   * Gets the name attachs.
   * 
   * @return the name attachs
   */
  public String[] getNameAttachs() {
    return nameAttachs;
  }

  /**
   * Sets the name attachs.
   * 
   * @param nameAttachs the new name attachs
   */
  public void setNameAttachs(String[] nameAttachs) {
    this.nameAttachs = nameAttachs;
  }

  /**
   * Get link to question, this link is used to send mail notify,
   * when user click in to this link, will be jump to FAQ and view this question.
   * 
   * @return link to question
   */
  public String getLink() {
    return link;
  }

  /**
   * Register link to question, this link is used to send mail notify,
   * when user click in to this link, will be jump to FAQ and view this question.
   * 
   * @param link the link
   */
  public void setLink(String link) {
    this.link = link;
  }

  /**
   * Gets the users vote.
   * 
   * @return the users vote
   */
  public String[] getUsersVote() {
    return usersVote;
  }

  /**
   * Sets the users vote.
   * 
   * @param usersVote the new users vote
   */
  public void setUsersVote(String[] usersVote) {
    this.usersVote = usersVote;
  }

  /**
   * Gets the mark vote.
   * 
   * @return the mark vote
   */
  public double getMarkVote() {
    return markVote;
  }

  /**
   * Sets the mark vote.
   * 
   * @param markVote the new mark vote
   */
  public void setMarkVote(double markVote) {
    this.markVote = markVote;
  }

  /**
   * Gets the users watch.
   * 
   * @return the users watch
   */
  public String[] getUsersWatch() {
    return usersWatch;
  }

  /**
   * Sets the users watch.
   * 
   * @param usersWatch the new users watch
   */
  public void setUsersWatch(String[] usersWatch) {
    this.usersWatch = usersWatch;
  }

  /**
   * Gets the emails watch.
   * 
   * @return the emails watch
   */
  public String[] getEmailsWatch() {
    return emailsWatch;
  }

  /**
   * Sets the emails watch.
   * 
   * @param emailsWatch the new emails watch
   */
  public void setEmailsWatch(String[] emailsWatch) {
    this.emailsWatch = emailsWatch;
  }

  /**
   * Gets the question.
   * 
   * @return the question
   */
  public String getQuestion() {
    return question;
  }

  /**
   * Sets the question.
   * 
   * @param title the new question
   */
  public void setQuestion(String title) {
    this.question = title;
  }

  /**
   * Gets the path topic discuss.
   * 
   * @return the path topic discuss
   */
  public String getTopicIdDiscuss() {
    return topicIdDiscuss;
  }

  /**
   * Sets the path topic discuss.
   * 
   * @param pathTopicDiscus the new path topic discuss
   */
  public void setTopicIdDiscuss(String topicIdDiscuss) {
    this.topicIdDiscuss = topicIdDiscuss;
  }

  /**
   * Gets the answers.
   * 
   * @return the answers
   */
  public Answer[] getAnswers() {
    return answers;
  }

  /**
   * Sets the answers.
   * 
   * @param answers the new answers
   */
  public void setAnswers(Answer[] answers) {
    this.answers = answers;
  }

  /**
   * Gets the comments.
   * 
   * @return the comments
   */
  public Comment[] getComments() {
    return comments;
  }

  /**
   * Sets the comments.
   * 
   * @param comments the new comments
   */
  public void setComments(Comment[] comments) {
    this.comments = comments;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  public void setMultiLanguages(QuestionLanguage[] multiLanguages) {
    this.multiLanguages = multiLanguages;
  }

  public QuestionLanguage[] getMultiLanguages() {
    return multiLanguages;
  }

  public String getAuthorOfLastActivity() {
    if (authorOfLastActivity == null || authorOfLastActivity.length() == 0) {
      return author;
    }
    return authorOfLastActivity;
  }

  public long getTimeOfLastActivity() {
    if (timeOfLastActivity < 0 && createdDate != null) {
      return createdDate.getTime();
    }
    return timeOfLastActivity;
  }

  /**
   * set information of last activity of question.
   * @param info includes two parts separated by a dash. the first part is user name has last activity.
   *  The second part is long value of time that appears last activity.
   */
  public void setLastActivity(String info) {
    authorOfLastActivity = Utils.getAuthorOfLastActivity(info);
    timeOfLastActivity = Utils.getTimeOfLastActivity(info);
  }

  /**
   * @return the numberOfPublicAnswers
   */
  public long getNumberOfPublicAnswers() {
    return numberOfPublicAnswers;
  }

  /**
   * @param numberOfPublicAnswers the numberOfPublicAnswers to set
   */
  public void setNumberOfPublicAnswers(long numberOfPublicAnswers) {
    this.numberOfPublicAnswers = numberOfPublicAnswers;
  }

}
