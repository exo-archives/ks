/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.ks.bench;

import java.util.HashMap;

import org.exoplatform.ks.bench.AnswerDataInjector.CONSTANTS;
import org.exoplatform.faq.service.Category;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 29, 2011  
 */
public class InjectInfo {
  public static final String ARRAY_SPLIT   = ",".intern();

  private int                categories    = 3;

  private int                depth         = 3;

  private int                questions     = 4;

  private int                answers       = 10;

  private int                comments      = 10;

  private int                maxAtt        = 0;

  private int                attCp         = 100;

  private int                txtCp         = 0;

  private String             type          = "";

  private String             preCategories = "";

  private String             preQuestions  = "";

  private String             preAnswers    = "";

  private String             preComments   = "";

  private String[]           perCanView    = new String[] { "" };

  private String[]           perCanEdit    = new String[] { "root" };

  private Category           rootCategory;

  HashMap<String, String>    queryParams;

  public InjectInfo() {
  }

  public InjectInfo(HashMap<String, String> queryParams, Category category) throws Exception {
    this.queryParams = queryParams;
    this.rootCategory = category;
    setQueryParams(queryParams);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getCategories() {
    return categories;
  }

  public void setCategories(int categories) {
    this.categories = categories;
  }

  public int getDepth() {
    return depth;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public int getQuestions() {
    return questions;
  }

  public void setQuestions(int questions) {
    this.questions = questions;
  }

  public int getAnswers() {
    return answers;
  }

  public void setAnswers(int answers) {
    this.answers = answers;
  }

  public int getComments() {
    return comments;
  }

  public void setComments(int comments) {
    this.comments = comments;
  }

  public int getMaxAtt() {
    return maxAtt;
  }

  public void setMaxAtt(int maxAtt) {
    this.maxAtt = maxAtt;
  }

  public int getAttCp() {
    return attCp;
  }

  public void setAttCp(int attCp) {
    this.attCp = attCp;
  }

  public int getTxtCp() {
    return txtCp;
  }

  public void setTxtCp(int txtCp) {
    this.txtCp = txtCp;
  }

  public String getPreCategories() {
    return preCategories;
  }

  public void setPreCategories(String preCategories) {
    this.preCategories = preCategories;
  }

  public String getPreQuestions() {
    return preQuestions;
  }

  public void setPreQuestions(String preQuestions) {
    this.preQuestions = preQuestions;
  }

  public String getPreAnswers() {
    return preAnswers;
  }

  public void setPreAnswers(String preAnswers) {
    this.preAnswers = preAnswers;
  }

  public String getPreComments() {
    return preComments;
  }

  public void setPreComments(String preComments) {
    this.preComments = preComments;
  }

  public String[] getPerCanView() {
    return perCanView;
  }

  public void setPerCanView(String[] perCanView) {
    this.perCanView = perCanView;
  }

  public String[] getPerCanEdit() {
    return perCanEdit;
  }

  public void setPerCanEdit(String[] perCanEdit) {
    this.perCanEdit = perCanEdit;
  }

  private void readPermissions(HashMap<String, String> queryParams) throws Exception {
    String[] perCanEdit = getValues(queryParams, AnswerDataInjector.CONSTANTS.EDIT.getName());
    String[] perCanView = getValues(queryParams, AnswerDataInjector.CONSTANTS.VIEW.getName());
    String[] mods = rootCategory.getModerators();
    if (mods == null || mods.length <= 0 || mods[0].trim().length() == 0) {
      mods = new String[] { "root" };
    }
    if (CONSTANTS.PERM.getName().equalsIgnoreCase(type) && perCanEdit.length > 0 && 
        !perCanEdit[0].equals(AnswerDataInjector.CONSTANTS.ANY.getName())) {
      this.perCanEdit = new String[perCanEdit.length + 1];
      this.perCanEdit[0] = mods[0];
      System.arraycopy(perCanEdit, 0, this.perCanEdit, 1, perCanEdit.length);
    } else {
      this.perCanEdit = mods;
    }
    if (CONSTANTS.PERM.getName().equalsIgnoreCase(type) && perCanView.length > 0 && 
        !perCanView[0].equals(AnswerDataInjector.CONSTANTS.ANY.getName())) {
      this.perCanView = new String[perCanView.length + 1];
      this.perCanView[0] = mods[0];
      System.arraycopy(perCanView, 0, this.perCanView, 1, perCanView.length);
    } else {
      this.perCanView = new String[] { "" };
    }
  }

  public void setQueryParams(HashMap<String, String> queryParams) throws Exception {
    String[] quantities = getValues(queryParams, AnswerDataInjector.CONSTANTS.Q.getName());
    categories = getParam(quantities, 0, categories);
    depth = getParam(quantities, 1, depth);
    questions = getParam(quantities, 2, questions);
    answers = getParam(quantities, 3, answers);
    comments = getParam(quantities, 4, comments);
    String[] prefixes = getValues(queryParams, AnswerDataInjector.CONSTANTS.PRE.getName());
    preCategories = getParam(prefixes, 0);
    preQuestions = getParam(prefixes, 1);
    preAnswers = getParam(prefixes, 2);
    preComments = getParam(prefixes, 3);
    type = queryParams.get(AnswerDataInjector.CONSTANTS.TYPE.getName());
    this.maxAtt = getParam(queryParams, AnswerDataInjector.CONSTANTS.ATT.getName(), 0);
    if (maxAtt > 0) {
      this.attCp = getParam(queryParams, AnswerDataInjector.CONSTANTS.ATTCP.getName(), 100);
    }
    this.txtCp = getParam(queryParams, AnswerDataInjector.CONSTANTS.TXTCP.getName(), 0);
    readPermissions(queryParams);
  }

  private int getParam(String[] param, int index, int df) throws Exception {
    try {
      return Integer.parseInt(param[index].trim());
    } catch (Exception e) {
      return df;
    }
  }

  private int getParam(HashMap<String, String> queryParams, String key, int df) throws Exception {
    try {
      return Integer.parseInt(queryParams.get(key));
    } catch (Exception e) {
      return df;
    }
  }

  private String getParam(String[] param, int index) throws Exception {
    try {
      return param[index].trim();
    } catch (Exception e) {
      return "";
    }
  }

  private String[] getValues(HashMap<String, String> queryParams, String key) throws Exception {
    try {
      return queryParams.get(key).split(ARRAY_SPLIT);
    } catch (Exception e) {
      return new String[] {};
    }
  }

}
