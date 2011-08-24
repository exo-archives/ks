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

import org.exoplatform.container.xml.InitParams;
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

  private int[]               infoIject     = new int[] { 0, 0, 0, 0 };

  private String              SLASH         = "/".intern();

  private boolean             randomize     = false;

  private Random              rand;

  private Category            categoryRoot  = null;

  private FAQService          faqService;

  private FAQSetting          faqSetting    = new FAQSetting();

  private List<String>        categoryIds   = new ArrayList<String>();

  public AnswerDataInjector(FAQService faqService, InitParams params) {
    this.faqService = faqService;
    initDatas();
    initParams(params);
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

  private int getParam(InitParams initParams, String param, int df) throws Exception {
    try {
      return Integer.parseInt(initParams.getValueParam(param).getValue());
    } catch (Exception e) {
      return df;
    }
  }

  private boolean getParam(InitParams initParams, String param) throws Exception {
    try {
      return Boolean.parseBoolean(initParams.getValueParam(param).getValue());
    } catch (Exception e) {
      return false;
    }
  }

  public void initParams(InitParams initParams) {
    try {
      maxCategories = getParam(initParams, "mCt", maxCategories);
      maxDepth = getParam(initParams, "mDt", maxDepth);
      maxQuestions = getParam(initParams, "mQs", maxQuestions);
      maxAnswers = getParam(initParams, "mAs", maxAnswers);
      maxComments = getParam(initParams, "mCm", maxComments);
      randomize = getParam(initParams, "rand");
    } catch (Exception e) {
      throw new RuntimeException("Could not initialize ", e);
    }
  }

  private int getMaxItem(int maxType) {
    return (randomize) ? (rand.nextInt(maxType) + 1) : maxType;
  }

  private List<Category> findCategories() {
    List<Category> categories = new ArrayList<Category>();
    int maxCat = getMaxItem(maxCategories);
    if (maxCat > 0) {
      Category cat;
      String previousId = "";
      for (int i = 0; i < maxCat; i++) {
        cat = newCategory(previousId);
        categories.add(cat);
        previousId = cat.getId();
      }
    }
    return categories;
  }

  private List<Question> findQuestion(String catId) {
    List<Question> questions = new ArrayList<Question>();
    int maxQs = getMaxItem(maxQuestions);
    if (maxQs > 0) {
      for (int i = 0; i < maxQs; i++) {
        questions.add(newQuestion(catId));
      }
    }
    return questions;
  }

  private Answer[] findAnswers() {
    int maxAs = getMaxItem(maxAnswers);
    Answer[] answers = new Answer[maxAs];
    if (maxAs > 0) {
      for (int i = 0; i < maxAs; i++) {
        answers[i] = newAnswer();
      }
    }
    return answers;
  }

  private List<Comment> findComments() {
    List<Comment> comments = new ArrayList<Comment>();
    int maxCm = getMaxItem(maxComments);
    if (maxCm > 0) {
      for (int i = 0; i < maxCm; i++) {
        comments.add(newComment());
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
    String questionId;
    String s = getTabs(currentDepth);
    List<Question> questions = findQuestion(catId);
    int index_ = 0, size_ = questions.size();
    log.info(String.format(" %sCategory %s/%s with %s questions...", s, index, size, size_));
    faqService.saveCategory(parentId, cat, true);
    infoIject[0]++;
    infoIject[1] += size_;
    long t1;
    for (Question question : questions) {
      t1 = System.currentTimeMillis();
      faqService.saveQuestion(question, true, faqSetting);
      questionId = catId + SLASH + Utils.QUESTION_HOME + SLASH + question.getId();
      Answer[] answers = findAnswers();
      faqService.saveAnswer(questionId, answers);
      infoIject[2] += answers.length;
      List<Comment> comments = findComments();
      infoIject[3] += comments.size();
      for (Comment comment : comments) {
        faqService.saveComment(questionId, comment, true);
      }
      log.info(String.format(" %s  Question %s/%s with %s answers and %s comments in %sms",
                             s, ++index_, size_, answers.length, comments.size(), (System.currentTimeMillis() - t1)));
    }
  }

  private void createCategory(String parentId, Category me, int currentDepth,int index, int size) throws Exception {
    initDataForOneCategory(parentId, me, currentDepth, index, size);
    List<Category> cats = findCategories();
    int index_ = 1, size_ = cats.size();
    for (Category cat : cats) {
      if (currentDepth + 1 < maxDepth) {
        createCategory(parentId + SLASH + me.getId(), cat, currentDepth + 1, index_, size_);
        index_++;
      }
    }
  }

  @Override
  public void inject(HashMap<String, String> queryParams) throws Exception {
    String parentId = KSDataLocation.Locations.FAQ_CATEGORIES_HOME;
    infoIject = new int[] { 0, 0, 0, 0 };
    log.info("Start inject data for answer ....");
    long time = System.currentTimeMillis();
    List<Category> cats = findCategories();
    int size = cats.size(), index = 1;
    for (Category cat : cats) {
      categoryIds.add(parentId + SLASH + cat.getId());
      createCategory(parentId, cat, 0, index, size);
      index++;
    }
    time = System.currentTimeMillis() - time;
    log.info(String.format("INJECTED : categories=%s / questions=%s / answers=%s / comments=%s / time=%sms", 
                           infoIject[0], infoIject[1], infoIject[2], infoIject[3], time));
    saveHistoryInject();
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

  private Category newCategory(String previousId) {
    Category category = new Category();
    while (category.getId().equals(previousId)) {
      category = new Category();
    }
    category.setName(randomWords(10));
    category.setDescription(randomWords(20));
    category.setIndex(0);
    String[] mods = getCategoryRoot(false).getModerators();
    if (mods == null || mods.length == 0) {
      mods = new String[] { "root" };
    }
    category.setModerators(mods);
    category.setUserPrivate(new String[] { "" });
    return category;
  }

  private Question newQuestion(String catId) {
    Question question = new Question();
    question.setAuthor(randomUser());
    question.setCategoryId(catId);
    question.setDetail(randomParagraphs(2));
    question.setEmail("exo@exoplatform.com");
    question.setLanguage("English");
    question.setLink("");
    question.setTopicIdDiscuss("");
    question.setMarkVote(0.0);
    question.setQuestion(randomWords(10));
    question.setRelations(new String[] { "" });
    question.setUsersWatch(new String[] { "" });
    question.setEmailsWatch(new String[] { "" });
    return question;
  }

  private Answer newAnswer() {
    String other = randomUser();
    Answer answer = new Answer(other, true);
    answer.setFullName(getFullName(other));
    answer.setLanguage("English");
    answer.setMarksVoteAnswer(0.0);
    answer.setMarkVotes(0);
    answer.setResponses(randomParagraphs(3));
    answer.setNew(true);
    return answer;
  }

  private Comment newComment() {
    String other = randomUser();
    Comment comment = new Comment();
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
