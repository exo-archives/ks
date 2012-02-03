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
package org.exoplatform.faq.service;

import java.util.List;

/**
 * Abstract class JCRPageList provide functions for pagination when view
 * question content in web page.
 * 
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since Mar 08, 2008
 */
abstract public class JCRPageList {

  private long                       pageSize_;

  protected long                     available_     = 0;

  protected long                     availablePage_ = 1;

  protected long                     currentPage_   = 1;

  protected List<Question>           currentListPage_;

  protected List                     currentListObject_;

  protected List<Category>           currentListCategory_;

  protected List<ObjectSearchResult> currentListResultSearch_;

  protected List<Watch>              currentListWatch_;

  private String                     objectId_      = null;

  private long                       pageJump_      = 0;

  /**
   * Constructor set pagesize for JCRPageList, pagesize is number of objects per page
   * for example: pagesize = 10 it's mean view 10 object per page
   * 
   * @param pageSize  the number of object per page
   */
  public JCRPageList(long pageSize) {
    pageSize_ = pageSize;
  }

  public String getObjectId() {
    return objectId_;
  }

  public void setObjectId(String id) {
    this.objectId_ = id;
  }

  /**
   * Get page size, return number of quesitons per page
   * 
   * @return  number of object per page
   */
  public long getPageSize() {
    return pageSize_;
  }

  /**
   * Set pagesize for JCRPageList, pagesize is number of objects per page
   * for example: pagesize = 10 it's mean view 10 object per page
   * @param pageSize  the number of object per page
   */
  public void setPageSize(long pageSize) {
    pageSize_ = pageSize;
    setAvailablePage(available_);
  }

  /**
   * Get index of current page which is viewing
   * @return
   */
  public long getCurrentPage() {
    return currentPage_;
  }

  /**
   * Get total of questions in list questions are contained
   * 
   * @return  total of questions
   */
  public long getAvailable() {
    return available_;
  }

  /**
   * Get total pages
   * @return  total pages
   */
  public long getAvailablePage() {
    return availablePage_;
  }

  /**
   * Get objects (question objects) is viewed in current page
   * 
   * @param username    the name of current user
   * @return            list quesitons are viewed in current page
   * @throws Exception  the exception
   */
  public List<Question> currentPage(String username) throws Exception {
    if (currentListPage_ == null) {
      populateCurrentPage(currentPage_, username);
    }
    return currentListPage_;
  }

  /**
   * Set questions for current page.
   * If <code>isUpdate</code> is <code>true</code> then change list question in current page 
   * by list quetions are specified, else delete all quesiton is viewed in current page
   * 
   * @param questions   List questions is used to update
   * @param isUpdate    is <code>true</code> if want update questions in current page
   *                    is <code>false</code> if want delete questions in current page
   * @throws Exception  the exception
   */
  public void setQuestion(List<Question> questions, boolean isUpdate) throws Exception {
    if (currentListPage_ == null)
      return;
    for (Question qt : questions) {
      for (int i = 0; i < currentListPage_.size(); i++) {
        if (currentListPage_.get(i).getId().endsWith(qt.getId())) {
          if (isUpdate) {
            currentListPage_.set(i, qt);
          } else {
            currentListPage_.remove(i);
          }
          break;
        }
      }
    }

  }

  /**
   * Abstract funtion, get list question to view in current page
   * @param page        index of page is viewed
   * @param username    the name of user
   * @throws Exception  the exception
   */
  abstract protected void populateCurrentPage(long page, String username) throws Exception;

  /**
   * get list questions are viewed in page which have index is specified.
   * The first check index of page if it's less than max and larger than 0 
   * then get questions in this page else throw exception
   * 
   * @param page        the index of page want process
   * @param username    the name of user
   * @return            list quesiton is view in the page which is specified
   * @throws Exception  if index of page is less than 0 or larger than max
   */
  public List<Question> getPage(long page, String username) throws Exception {
    checkAndSetPage(page);
    populateCurrentPage(currentPage_, username);
    return currentListPage_;
  }

  // Created by Vu Duy Tu
  abstract protected void populateCurrentPageItem(long page) throws Exception;

  @SuppressWarnings("unchecked")
  public List getPageItem(long page) throws Exception {
    checkAndSetPage(page);
    populateCurrentPageItem(currentPage_);
    return currentListObject_;
  }

  abstract protected void populateCurrentPageResultSearch(long page, String username) throws Exception;

  public List<ObjectSearchResult> getPageResultSearch(long page, String username) throws Exception {
    checkAndSetPage(page);
    populateCurrentPageResultSearch(currentPage_, username);
    return currentListResultSearch_;
  }

  abstract protected void populateCurrentPageCategoriesSearch(long page, String username) throws Exception;

  public List<Category> getPageResultCategoriesSearch(long page, String username) throws Exception {
    checkAndSetPage(page);
    populateCurrentPageCategoriesSearch(currentPage_, username);
    return currentListCategory_;
  }

  abstract protected void populateCurrentPageQuestionsSearch(long page, String username) throws Exception;

  public List<Question> getPageResultQuestionsSearch(long page, String username) throws Exception {
    checkAndSetPage(page);
    populateCurrentPageQuestionsSearch(currentPage_, username);
    return currentListPage_;
  }

  abstract protected void populateCurrentPageCategoriesQuestionsSearch(long page, String username) throws Exception;

  public List<Object> getPageListCategoriesQuestions(long page, String username) throws Exception {
    checkAndSetPage(page);
    populateCurrentPageCategoriesQuestionsSearch(currentPage_, username);
    return currentListObject_;
  }

  abstract protected void populateCurrentPageWatch(long page, String username) throws Exception;

  public List<Watch> getPageListWatch(long page, String username) throws Exception {
    checkAndSetPage(page);
    populateCurrentPageWatch(currentPage_, username);
    return currentListWatch_;
  }

  /**
   * abstract function to get all question.
   * 
   * @return            list of quesitons
   * @throws Exception  if question node not found
   */
  abstract public List<Question> getAll() throws Exception;

  /**
   * abtract funtion, set list question to view
   * 
   * @param questions list question
   */
  abstract public void setList(List<Question> questions);

  /**
   * Check the index of page, if <code>page</code> is less than max and larger than 0
   * then set <code>page</code> to be current page else throw exception
   * 
   * @param page        index of page
   */
  protected void checkAndSetPage(long page){
    if (page < 0) {
      page = 1;
    } else if (page > availablePage_) {
      page = availablePage_;
    }
    currentPage_ = page;
  }

  /**
   * Sets the available page. Base on total objects <code>available</code> to set
   * total pages for pagination.
   * 
   * @param available the number of objects
   */
  protected void setAvailablePage(long available) {
    available_ = available;
    if (available == 0) {
      availablePage_ = 1;
      currentPage_ = 1;
    } else {
      long pages = available / pageSize_;
      if (available % pageSize_ > 0)
        pages++;
      availablePage_ = pages;
      // currentPage_ = 1 ;
    }
  }

  public void setPageJump(long pageJump) {
    this.pageJump_ = pageJump;
  }

  public long getPageJump() {
    return pageJump_;
  }

  /*
   * public long getFrom() { return (currentPage_ - 1) * pageSize_ ; } public long getTo() { long to = currentPage_ * pageSize_ ; if (to > available_ ) to = available_ ; return to ; }
   */
}
