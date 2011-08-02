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

import java.util.Date;
import java.util.List;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * 
 * Data of category is stored in this class, it's used in processing 
 * add new category, edit category
 * 
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Category {
  public static final String CATEGORY_ID = "Category";
  
  private String   id;

  private long     index;

  private String   name;

  private String[] userPrivate         = null;

  private String   description;

  private boolean  isModerateQuestions = false;

  private boolean  isModerateAnswers   = false;

  private boolean  viewAuthorInfor     = false;

  private boolean  isView              = true;

  private String[] moderators;

  private Date     createdDate;

  private String   path;

  /**
   * Class constructor specifying id of object is created
   */
  public Category() {
    id =  CATEGORY_ID + IdGenerator.generate();
    // userPrivate = new String[] {" "};
  }

  public boolean isView() {
    return isView;
  }

  public void setView(boolean isView) {
    this.isView = isView;
  }

  /**
   * This method get id of category object
   * 
   * @return  category' id
   */
  public String getId() {
    return id;
  }

  /**
   * This method set an id for category object
   * 
   * @param id  id which you want set for category
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * This method get name of category object.
   * 
   * @return  name
   */
  public String getName() {
    return name;
  }

  /**
   * This method register the name to display on screen
   * 
   * @param  name of category with user input
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * This method get description of category 
   * 
   * @return description
   */
  public String getDescription() {
    return description;
  }

  /**
   * description will add information of category with user input  
   *  
   * @param description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * view users or group or membership will right(Users of FAQ Administrator) 
   * 
   * @return moderators
   */
  public String[] getModerators() {
    return moderators;
  }

  /**
   * This method set users or group or membership will administrator
   * of that category
   *    
   * @param mods is select in selector
   */
  public void setModerators(String[] mods) {
    this.moderators = mods;
  }

  /**
   * This method will get date of created date category
   * 
   * @return createDate is date create category
   */
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * This method set time system will time create current category
   * 
   * @param date is time computer system
   */
  public void setCreatedDate(Date date) {
    this.createdDate = date;
  }

  /**
   * This method set one category is moderate question
   * 
   * @param isMod  is true when create question in this category will default is not approved
   *               else is approved
   */
  public void setModerateQuestions(boolean isMod) {
    isModerateQuestions = isMod;
  }

  /**
   * This method get moderate question
   * 
   * @return <code>true</code> if check ModerateQuestions else <code>false</code>
   */
  public boolean isModerateQuestions() {
    return isModerateQuestions;
  }

  /**
   * This method get all user is administrator of category to field Moderator
   * 
   * @return list user
   * 
   * @throws Exception
   */
  public List<String> getModeratorsCategory() throws Exception {
    return FAQServiceUtils.getUserPermission(moderators);
  }

  public boolean isViewAuthorInfor() {
    return viewAuthorInfor;
  }

  public void setViewAuthorInfor(boolean viewAuthorInfor) {
    this.viewAuthorInfor = viewAuthorInfor;
  }

  public long getIndex() {
    return index;
  }

  public void setIndex(long index) {
    this.index = index;
  }

  public boolean isModerateAnswers() {
    return isModerateAnswers;
  }

  public void setModerateAnswers(boolean isModerateAnswers) {
    this.isModerateAnswers = isModerateAnswers;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }

  /**
   * @return the userPrivate
   */
  public String[] getUserPrivate() {
    return userPrivate;
  }

  /**
   * @param userPrivate the userPrivate to set
   */
  public void setUserPrivate(String[] userPrivate) {
    this.userPrivate = userPrivate;
  }
}
