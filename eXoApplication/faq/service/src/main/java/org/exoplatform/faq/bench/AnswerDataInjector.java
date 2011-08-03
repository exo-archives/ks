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
import java.util.Date;
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
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 2, 2011  
 */
public class AnswerDataInjector extends DataInjector {
  private static Log          log            = ExoLogger.getLogger(AnswerDataInjector.class);

  private Map<String, String> fullNameData   = new HashMap<String, String>();

  private int                 maxCategories  = 3;

  private int                 maxDepth       = 3;

  private int                 maxQuestions   = 4;

  private int                 maxAnswers     = 10;

  private int                 maxComments    = 10;

  private String              fistCategoryId = Category.CATEGORY_ID + "randomId412849127491";

  private String              SLASH          = "/".intern();

  private boolean             randomize      = false;

  private Random              rand;

  private Category            categoryRoot   = null;

  private FAQService          faqService;

  private FAQSetting          faqSetting     = new FAQSetting();

  private List<String>        categoryIds    = new ArrayList<String>();

  public AnswerDataInjector(FAQService faqService, IDGeneratorService uidGenerator, OrganizationService organizationService, InitParams params) {
    this.faqService = faqService;
    initDatas();
    initParams(params);
  }

  private void initDatas() {
    rand = new Random();
    faqSetting.setDisplayMode("");
    faqSetting.setEmailSettingSubject("eXo Answers Notification");
    faqSetting.setEmailSettingContent("<p>We have a new question or answer by injector datas in category <strong>&categoryName_</strong></p><p><em>&questionContent_</em></p>");
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

  private Category getCategoryRoot() {
    try {
      if (categoryRoot == null) {
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

  @Override
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

  @Override
  public boolean isInitialized() {
    try {
      Category category = faqService.getCategoryById(KSDataLocation.Locations.FAQ_CATEGORIES_HOME + SLASH + fistCategoryId);
      return (category != null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private int getMaxItem(int maxType) {
    rand = new Random(maxType);
    return (randomize) ? (rand.nextInt(maxType) + 1) : maxType;
  }

  private List<Category> findCategories(boolean isRoot) {
    List<Category> categories = new ArrayList<Category>();
    int maxCat = getMaxItem(maxCategories);
    if (maxCat > 0) {
      Category cat = newCategory("");
      if (isRoot) {
        cat.setId(fistCategoryId);
        categories.add(cat);
        maxCat = maxCat - 1;
      }
      String previousId = fistCategoryId;
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

  private void initDataForOneCategory(String parentId, Category cat) throws Exception {
    faqService.saveCategory(parentId, cat, true);
    String catId = parentId + SLASH + cat.getId();
    String questionId;
    for (Question question : findQuestion(catId)) {
      faqService.saveQuestion(question, true, faqSetting);
      questionId = catId + SLASH + Utils.QUESTION_HOME + SLASH + question.getId();
      faqService.saveAnswer(questionId, findAnswers());
      for (Comment comment : findComments()) {
        faqService.saveComment(questionId, comment, true);
      }
    }
  }

  private void createCategory(String parentId, Category me, int currentDepth) throws Exception {
    initDataForOneCategory(parentId, me);
    for (Category cat : findCategories(false)) {
      if (currentDepth - 1 > 0) {
        createCategory(parentId + SLASH + me.getId(), cat, currentDepth - 1);
      }
    }
  }

  @Override
  public void inject() throws Exception {
    String parentId = KSDataLocation.Locations.FAQ_CATEGORIES_HOME;
    for (Category cat : findCategories(true)) {
      categoryIds.add(parentId + SLASH + cat.getId());
      createCategory(parentId, cat, maxDepth);
    }
  }

  private void removeData() throws Exception {
    for (String categoryId : categoryIds) {
      faqService.removeCategory(categoryId);
    }
    categoryIds.clear();
  }

  @Override
  public void reject() throws Exception {
    removeData();
  }

  private Category newCategory(String previousId) {
    Category category = new Category();
    while (category.getId().equals(previousId)) {
      category = new Category();
    }
    category.setName(randomWords(10));
    category.setDescription(randomWords(20));
    category.setIndex(0);
    category.setModerators(getCategoryRoot().getModerators());
    category.setUserPrivate(new String[] { "" });
    return category;
  }

  private Question newQuestion(String catId) {
    Question question = new Question();
    question.setAuthor(randomUser());
    question.setCategoryId(catId);
    question.setDetail(randomParagraphs(2));
    question.setEmail("exotest@exoplatform.com");
    question.setLanguage("English");
    question.setLink("");
    question.setTopicIdDiscuss("");
    question.setMarkVote(0.0);
    question.setQuestion(randomWords(10));
    question.setRelations(new String[] { "" });
    question.setUsersWatch(new String[] { "" });
    question.setEmailsWatch(new String[] { "" });
    question.setCreatedDate(new Date());
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
    answer.setDateResponse(new Date());
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
    comment.setDateComment(new Date());
    return comment;
  }

  private String getFullName(String userName) {
    try {
      return fullNameData.get(userName);
    } catch (Exception e) {
      return "No Name";
    }
  }
}
