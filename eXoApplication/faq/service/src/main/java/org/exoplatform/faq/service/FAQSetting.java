/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.service;

/**
 * Created by The eXo Platform SARL
 * 
 * This Object is used to set some properties of FAQ.
 * 
 * Author : Truong Nguyen
 * truong.nguyen@exoplatform.com
 * Apr 10, 2008, 2:07:25 PM
 */
public class FAQSetting {
  private boolean      enableViewAvatar              = false;

  private boolean      enableAutomaticRSS            = true;

  private boolean      enableVotesAndComments        = true;

  private boolean      enableAnonymousSubmitQuestion = true;

  /** display mode of faq. */
  private String       displayMode;

  /** content of email will be sent to user who add watch or owner of question or morderator. */
  private String       emailSettingContent           = null;

  /** subject of email. */
  private String       emailSettingSubject           = null;

  /** The order by. */
  private String       orderBy;

  /** The order type. */
  private String       orderType                     = ORDERBY_TYPE_ASC;

  /** The sort question by vote. */
  private boolean      sortQuestionByVote            = false;

  /** The can edit. */
  private boolean      canEdit                       = false;

  /** The is admin. */
  private String       isAdmin                       = null;

  private String       idNameCategoryForum;

  private boolean      isDiscussForum                = false;

  /** The DISPLA type alphabet. */
  public static String DISPLAY_TYPE_ALPHABET         = "alphabet";

  /** The DISPLA type postdate. */
  public static String DISPLAY_TYPE_POSTDATE         = "created";

  /** The ORDERB type asc. */
  public static String ORDERBY_TYPE_ASC              = "asc";

  /** The ORDERB type desc. */
  public static String ORDERBY_TYPE_DESC             = "desc";

  /** The ORDERB asc. */
  public static String ORDERBY_ASC                   = " ascending";

  /** The ORDERB desc. */
  public static String ORDERBY_DESC                  = " descending";

  public static String DISPLAY_APPROVED              = "approved";

  public static String DISPLAY_BOTH                  = "both";

  private String       emailMoveQuestion             = null;

  private boolean      isPostQuestionInRootCategory  = true;

  /** The Current User Login */
  private String       currentUser                   = "";

  /**
   * This method get one value is Alphabet or Post Date.
   * 
   * @return displayType
   */
  public String getDisplayMode() {
    return displayMode;
  }

  /**
   * All categories/questions can be displayed in some types depending on users.
   * This is a combobox with two values: Alphabet or Post Date
   * 
   * @param displayMode the display mode
   */
  public void setDisplayMode(String displayMode) {
    this.displayMode = displayMode;
  }

  /**
   * Get field is ordered of datas are get from FAQ system, value is returned : alphabet or created date.
   * 
   * @return order of categories and questions
   */
  public String getOrderBy() {
    return orderBy;
  }

  /**
   * Registers field which is ordered when get them from database.
   * 
   * @param orderBy only one of two case: alphabet or created
   */
  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  /**
   * Get how to order when get data, have two values: ascending and descending.
   * 
   * @return ascending or descending
   */
  public String getOrderType() {
    return orderType;
  }

  /**
   * Registers order of the field which is chosen when get data,
   * input one of tow values: <code>ascending</code> and <code>descending</code>.
   * 
   * @param orderType ascending or descending
   */
  public void setOrderType(String orderType) {
    this.orderType = orderType;
  }

  /**
   * Checks if is can edit. Return <code>true</code> if user is admin or moderator or
   * this category and return <code>false</code> if opposite
   * 
   * @return true, if is can edit
   */
  public boolean isCanEdit() {
    return canEdit;
  }

  /**
   * Sets the can edit.
   * input a boolean value, <code>true</code> if user is addmin of FAQ or moderator 
   * of this category and <code>false</code> if opposite 
   * @param canEdit the new can edit
   */
  public void setCanEdit(boolean canEdit) {
    this.canEdit = canEdit;
  }

  /**
   * Get the content of email will be sent for user.
   * This email have two cases:
   * <p>The first: when add new question
   * <p>The second: when edit or answer for question
   * 
   * @return the email setting content
   */
  public String getEmailSettingContent() {
    return emailSettingContent;
  }

  /**
   * Registers content of email is sent to user,
   * when set value for email, note: have two case
   * <p>The first: when add new question
   * <p>The second: when edit or answer for question.
   * 
   * @param emailSettingContent content of email
   */
  public void setEmailSettingContent(String emailSettingContent) {
    this.emailSettingContent = emailSettingContent;
  }

  /**
   * Gets the subject of email which is sent to user.
   * 
   * @return the email setting subject
   */
  public String getEmailSettingSubject() {
    return emailSettingSubject;
  }

  /**
   * Sets the subject of email will be sent to user.
   * 
   * @param emailSettingSubject the new email setting subject
   */
  public void setEmailSettingSubject(String emailSettingSubject) {
    this.emailSettingSubject = emailSettingSubject;
  }

  /**
   * Gets the checks if is admin.
   * Return a String "True" if user is addmin and "False" if is not addmin
   * @return the checks if is admin
   */
  public String getIsAdmin() {
    return isAdmin;
  }

  /**
   * Sets the checks if is admin.
   * Input "True" if user is addmin and "False" if not
   * @param isAdmin the new checks if is admin
   */
  public void setIsAdmin(String isAdmin) {
    this.isAdmin = isAdmin;
  }

  /**
   * Checks user is admin or not. This function retrun <code>true</code>
   * if user is addmin and <code>false</code> if not
   * 
   * @return true, if is admin
   */
  public boolean isAdmin() {
    return ("TRUE".equalsIgnoreCase(isAdmin));
  }

  public boolean isSortQuestionByVote() {
    return sortQuestionByVote;
  }

  public void setSortQuestionByVote(boolean sortAnswerByVote) {
    this.sortQuestionByVote = sortAnswerByVote;
  }

  public boolean isEnanbleVotesAndComments() {
    return enableVotesAndComments;
  }

  public void setEnanbleVotesAndComments(boolean enanbleVotesAndComments) {
    this.enableVotesAndComments = enanbleVotesAndComments;
  }

  public boolean isEnableAnonymousSubmitQuestion() {
    return enableAnonymousSubmitQuestion;
  }

  public void setEnableAnonymousSubmitQuestion(boolean isSubmit) {
    this.enableAnonymousSubmitQuestion = isSubmit;
  }

  public boolean getIsDiscussForum() {
    return isDiscussForum;
  }

  public void setIsDiscussForum(boolean b) {
    this.isDiscussForum = b;
  }

  public String getIdNameCategoryForum() {
    return idNameCategoryForum;
  }

  public void setIdNameCategoryForum(String str) {
    this.idNameCategoryForum = str;
  }

  public boolean isEnableAutomaticRSS() {
    return enableAutomaticRSS;
  }

  public void setEnableAutomaticRSS(boolean enableAutomaticRSS) {
    this.enableAutomaticRSS = enableAutomaticRSS;
  }

  public boolean isEnableViewAvatar() {
    return enableViewAvatar;
  }

  public void setEnableViewAvatar(boolean enableViewAvatar) {
    this.enableViewAvatar = enableViewAvatar;
  }

  public String getEmailMoveQuestion() {
    return emailMoveQuestion;
  }

  public void setEmailMoveQuestion(String emailMoveQuestion) {
    this.emailMoveQuestion = emailMoveQuestion;
  }

  public String getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(String currentUser) {
    this.currentUser = currentUser;
  }

  /**
   * @param isPostQuestionInRootCategory the isPostQuestionInRootCategory to set
   */
  public void setPostQuestionInRootCategory(boolean isPostQuestionInRootCategory) {
    this.isPostQuestionInRootCategory = isPostQuestionInRootCategory;
  }

  /**
   * @return the isPostQuestionInRootCategory
   */
  public boolean isPostQuestionInRootCategory() {
    return isPostQuestionInRootCategory;
  }
}
