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
package org.exoplatform.faq.bench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 2, 2011  
 */
public class AnswerDataInjector extends DataInjector {
  private static Log          log           = ExoLogger.getLogger(AnswerDataInjector.class);

  private Map<String, String> fullNameData  = new HashMap<String, String>();

  private int                 maxCategories = 3;

  private int                 maxDepth      = 3;

  private int                 maxQuestions  = 4;

  private int                 maxAnswers    = 10;

  private int                 maxComments   = 10;

  private String              preCategories = "";

  private String              preQuestions  = "";

  private String              preAnswers    = "";

  private String              preComments   = "";

  private String[]            perCanView    = new String[] { "" };

  private String[]            perCanEdit    = new String[] { "root" };
  
  private int[]               infoIject     = new int[] { 0, 0, 0, 0 };

  private String              SLASH         = "/".intern();

  private boolean             randomize     = false;

  private Random              rand;

  private Category            categoryRoot  = null;

  private FAQService          faqService;

  private FAQSetting          faqSetting    = new FAQSetting();

  private List<String>        categoryIds   = new ArrayList<String>();

  public static final String  ARRAY_SPLIT   = ",";

  enum CONSTANTS {
    TYPE, PERM, DATA
  };
  
  public AnswerDataInjector(FAQService faqService) {
    this.faqService = faqService;
    initDatas();
  }

  private void initDatas() {
    rand = new Random();
    faqSetting.setDisplayMode("");
    faqSetting.setEmailSettingSubject("eXo Answers Notification");
    faqSetting.setEmailSettingContent("<p>We have a new question or answer by injector datas in category " +
    		"<strong>&categoryName_</strong></p><p><em>&questionContent_</em></p>");
    List<String> users = Arrays.asList(new String[] { "root", "demo", "mary", "john" });
    List<String> userFullNames = Arrays.asList(new String[] { "Root Root", "Demo", "Mary Kelly", "John Anthony" });
    for (int i = 0; i < users.size(); ++i) {
      fullNameData.put(users.get(i), userFullNames.get(i));
    }
  }

  @Override
  public Log getLog() {
    return log;
  }

  private Category getCategoryRoot(boolean isUpdate) {
    try {
      if (isUpdate || categoryRoot == null) {
        categoryRoot = faqService.getCategoryById(KSDataLocation.Locations.FAQ_CATEGORIES_HOME);
      }
      return categoryRoot;
    } catch (Exception e) {
      return null;
    }
  }

  private int getParam(String[] param, int index, int df) throws Exception {
    try {
      return Integer.parseInt(param[index].trim());
    } catch (Exception e) {
      return df;
    }
  }

  private String getParam(String[] param, int index) throws Exception {
    try {
      return param[index].trim();
    } catch (Exception e) {
      return randomWords(4);
    }
  }

  private String[] getValues(HashMap<String, String> queryParams, String key) throws Exception {
    try {
      return queryParams.get(key).split(ARRAY_SPLIT);
    } catch (Exception e) {
      return new String[]{};
    }
  }

  private void readQuantities(HashMap<String, String> queryParams) throws Exception {
    String[] quantities = getValues(queryParams, "q");
    maxCategories = getParam(quantities, 0, maxCategories);
    maxDepth = getParam(quantities, 1, maxDepth);
    maxQuestions = getParam(quantities, 2, maxQuestions);
    maxAnswers = getParam(quantities, 3, maxAnswers);
    maxComments = getParam(quantities, 4, maxComments);
  }
  
  private void readPrefixes(HashMap<String, String> queryParams) throws Exception {
    String[] prefixes = getValues(queryParams, "pre") ;
    preCategories = getParam(prefixes, 0);
    preQuestions  = getParam(prefixes, 1);
    preAnswers    = getParam(prefixes, 2);
    preComments   = getParam(prefixes, 3);
  }

  private void readPermissions(HashMap<String, String> queryParams) throws Exception {
    String[] perCanEdit = getValues(queryParams, "edit");
    String[] perCanView = getValues(queryParams, "view");
    String[] mods = getCategoryRoot(false).getModerators();
    if (mods == null || mods.length <= 0 || mods[0].trim().length() == 0) {
      mods = new String[] { "root" };
    }
    if (perCanEdit.length > 0 || !perCanEdit[0].equals("any")) {
      this.perCanEdit = new String[perCanEdit.length + 1];
      this.perCanEdit[0] = mods[0];
      System.arraycopy(perCanEdit, 0, this.perCanEdit, 1, perCanEdit.length);
    } else {
      this.perCanEdit = mods;
    }
    if (perCanView.length > 0 || !perCanView[0].equals("any")) {
      this.perCanView = new String[perCanView.length + 1];
      this.perCanView[0] = mods[0];
      System.arraycopy(perCanView, 0, this.perCanView, 1, perCanView.length);
    } else {
      this.perCanView = new String[] { "" };
    }
  }
  
  private String makeName(String prefix, int leve, int order) {
    return prefix + "_" + leve + "_" + order;
  }

  private String makeId(String prefix, String type, int leve, int order) {
    return type + prefix + leve + order;
  }

  private int getMaxItem(int maxType) {
    return (randomize) ? (rand.nextInt(maxType) + 1) : maxType;
  }

  private List<Category> findCategories(int leve) {
    List<Category> categories = new ArrayList<Category>();
    int maxCat = getMaxItem(maxCategories);
    if (maxCat > 0) {
      Category cat;
      String catName = "", catId = "";
      for (int i = 0; i < maxCat; i++) {
        catId = makeId(preCategories, Category.CATEGORY_ID, leve, i + 1);
        catName = makeName(preCategories, leve, i + 1);
        cat = newCategory(catId, catName, i + 1);
        categories.add(cat);
      }
    }
    return categories;
  }

  private List<Question> findQuestion(String catId, int leve) {
    List<Question> questions = new ArrayList<Question>();
    int maxQs = getMaxItem(maxQuestions);
    if (maxQs > 0) {
      String queName = "", queId = "";
      for (int i = 0; i < maxQs; i++) {
        queId = makeId(preQuestions, Question.QUESTION_ID, leve, i + 1);
        queName = makeName(preQuestions, leve, i + 1);
        questions.add(newQuestion(catId, queId, queName));
      }
    }
    return questions;
  }

  private List<Answer> findAnswers() {
    int maxAs = getMaxItem(maxAnswers);
    List<Answer> answers = new ArrayList<Answer>();
    if (maxAs > 0) {
      String asId = "";
      for (int i = 0; i < maxAs; i++) {
        asId = makeId(preAnswers, Answer.ANSWER_ID, 1, i);
        answers.add(newAnswer(asId));
      }
    }
    return answers;
  }

  private List<Comment> findComments() {
    List<Comment> comments = new ArrayList<Comment>();
    int maxCm = getMaxItem(maxComments);
    if (maxCm > 0) {
      String cmId = "";
      for (int i = 0; i < maxCm; i++) {
        cmId = makeId(preComments, Answer.ANSWER_ID, 1, i);
        comments.add(newComment(cmId));
      }
    }
    return comments;
  }

  private String getTabs(int currentDepth) {
    String s = "";
    for (int i = 0; i < currentDepth; i++) {
      s += "    ";
    }
    return s;
  }
  
  private void initDataForOneCategory(String parentId, Category cat, int currentDepth, int index, int size) throws Exception {
    String catId = parentId + SLASH + cat.getId();
    boolean isExist = faqService.isExisting(catId);
    String questionId, stt = "Update";
    String s = getTabs(currentDepth);
    List<Question> questions = findQuestion(catId, currentDepth);
    int index_ = 0, size_ = questions.size(), as = 0, cm = 0;
    if (!isExist) {
      stt = "Add new";
      faqService.saveCategory(parentId, cat, true);
      infoIject[0]++;
    }
    log.info(String.format(" %s %s Category %s/%s with %s questions...", s, stt, index, size, size_));
    long t1;
    for (Question question : questions) {
      t1 = System.currentTimeMillis();
      questionId = catId + SLASH + Utils.QUESTION_HOME + SLASH + question.getId();
      isExist = faqService.isExisting(questionId);
      stt = "Update";
      if (!isExist) {
        faqService.saveQuestion(question, true, faqSetting);
        infoIject[1] += 1;
        stt = "Add new";
      }
      List<Answer> answers = findAnswers();
      for (Answer answer : answers) {
        isExist = faqService.isExisting(questionId + SLASH + Utils.ANSWER_HOME + SLASH + answer.getId());
        if (!isExist) {
          faqService.saveAnswer(questionId, answer, true);
          infoIject[2] += 1;
          as += 1;
        }
      }
      List<Comment> comments = findComments();
      for (Comment comment : comments) {
        isExist = faqService.isExisting(questionId + SLASH + Utils.COMMENT_HOME + SLASH + comment.getId());
        if (!isExist) {
          faqService.saveComment(questionId, comment, true);
          infoIject[3] += 1;
          cm += 1;
        }
      }
      log.info(String.format(" %s %s Question %s/%s  with %s new answer(s) and %s new comment(s) in %sms",
                             s, stt,++index_, size_, as, cm, (System.currentTimeMillis() - t1)));
    }
  }

  private void createCategory(String parentId, Category me, int currentDepth,int index, int size) throws Exception {
    initDataForOneCategory(parentId, me, currentDepth, index, size);
    List<Category> cats = findCategories(currentDepth+1);
    int index_ = 1, size_ = cats.size();
    for (Category cat : cats) {
      if (currentDepth + 1 < maxDepth) {
        createCategory(parentId + SLASH + me.getId(), cat, currentDepth + 1, index_, size_);
        index_++;
      }
    }
  }

  private void injectData(HashMap<String, String> queryParams) throws Exception {
    readQuantities(queryParams);
    readPrefixes(queryParams);
    String parentId = KSDataLocation.Locations.FAQ_CATEGORIES_HOME;
    infoIject = new int[] { 0, 0, 0, 0 };
    log.info("Start inject data for answer ....");
    long time = System.currentTimeMillis();
    List<Category> cats = findCategories(0);
    int size = cats.size(), index = 1;
    for (Category cat : cats) {
      categoryIds.add(parentId + SLASH + cat.getId());
      createCategory(parentId, cat, 0, index, size);
      index++;
    }
    time = System.currentTimeMillis() - time;
    log.info(String.format("INJECTED : new categories=%s / new questions=%s / new answers=%s / new comments=%s / time=%sms", 
                           infoIject[0], infoIject[1], infoIject[2], infoIject[3], time));
    saveHistoryInject();
  }
  
  @Override
  public void inject(HashMap<String, String> queryParams) throws Exception {
    String type = queryParams.get(CONSTANTS.TYPE.toString().toLowerCase());
    boolean runInject = false;
    if (CONSTANTS.DATA.toString().equalsIgnoreCase(type)) {
      this.perCanEdit = getCategoryRoot(false).getModerators();
      if (this.perCanEdit == null || this.perCanEdit.length <= 0 || this.perCanEdit[0].trim().length() == 0) {
        this.perCanEdit = new String[] { "root" };
      }
      runInject = true;
    } else if (CONSTANTS.PERM.toString().equalsIgnoreCase(type)) {
      readPermissions(queryParams);
      runInject = true;
    }
    if (runInject) {
      log.info(String.format("Injecting by type: %s ...", type));
      injectData(queryParams);
    } else {
      log.info(String.format("Do not support type %s for injector...", type));
    }
  }

  private void removeData() throws Exception {
    try {
      if (categoryIds.isEmpty()) {
        categoryIds.addAll(getHistoryInject());
      }
      for (String categoryId : categoryIds) {
        faqService.removeCategory(categoryId);
      }
      log.info("Completely remove the datas inject !");
      categoryIds.clear();
      Category category = getCategoryRoot(false);
      category.setDescription("");
      faqService.saveCategory(null, category, false);
    } catch (Exception e) {
      log.warn("Failed to remove data injected....");
    }
  }

  @Override
  public void reject(HashMap<String, String> queryParams) throws Exception {
    log.info("Start remove data injected....");
    removeData();
  }

  private List<String> getHistoryInject() {
    try {
      return convertStringToList(getCategoryRoot(true).getDescription());
    } catch (Exception e) {
      return new ArrayList<String>();
    }
  }

  private void saveHistoryInject() {
    try {
      String s = getCategoryRoot(true).getDescription();
      if (s != null && s.trim().length() > 0) {
        categoryIds.addAll(convertStringToList(s));
      }
      Category category = getCategoryRoot(false);
      category.setDescription(categoryIds.toString());
      faqService.saveCategory(null, category, false);
    } catch (Exception e) {
    }
  }

  public static List<String> convertStringToList(String s) {
    s = s.replace("[", "").replace("]", "");
    s = s.trim().replaceAll("(,\\s*)", ",").replaceAll("(\\s*,)", ",");
    String[] strs = s.split(",");
    return new ArrayList<String>(Arrays.asList(strs));
  }

  private Category newCategory(String catId, String catName, int order) {
    Category category = new Category();
    category.setId(catId);
    category.setName(catName);
    category.setDescription(randomWords(20));
    category.setIndex(order);
    category.setModerators(perCanEdit);
    category.setUserPrivate(perCanView);
    return category;
  }

  private Question newQuestion(String catId, String id, String name) {
    Question question = new Question();
    question.setId(id);
    question.setQuestion(name);
    question.setAuthor(randomUser());
    question.setCategoryId(catId);
    question.setDetail(randomParagraphs(2));
    question.setEmail("exo@exoplatform.com");
    question.setLanguage("English");
    question.setLink("");
    question.setTopicIdDiscuss("");
    question.setMarkVote(0.0);
    question.setRelations(new String[] { "" });
    question.setUsersWatch(new String[] { "" });
    question.setEmailsWatch(new String[] { "" });
    return question;
  }

  private Answer newAnswer(String id) {
    String other = randomUser();
    Answer answer = new Answer(other, true);
    answer.setId(id);
    answer.setFullName(getFullName(other));
    answer.setLanguage("English");
    answer.setMarksVoteAnswer(0.0);
    answer.setMarkVotes(0);
    answer.setResponses(randomParagraphs(3));
    answer.setNew(true);
    return answer;
  }

  private Comment newComment(String id) {
    String other = randomUser();
    Comment comment = new Comment();
    comment.setId(id);
    comment.setCommentBy(other);
    comment.setComments(randomParagraphs(3));
    comment.setFullName(getFullName(other));
    comment.setPostId("");
    comment.setNew(true);
    return comment;
  }

  private String getFullName(String userName) {
    try {
      return fullNameData.get(userName);
    } catch (Exception e) {
      return "No Name";
    }
  }

  @Override
  public Object execute(HashMap<String, String> arg0) throws Exception {
    return new Object();
  }
}
