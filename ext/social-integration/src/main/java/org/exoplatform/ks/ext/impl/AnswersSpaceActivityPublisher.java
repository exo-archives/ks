/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.ks.ext.impl;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.impl.AnswerEventListener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class AnswersSpaceActivityPublisher extends AnswerEventListener {

  private static Log LOG = ExoLogger.getExoLogger(AnswerEventListener.class);

  @Override
  public void saveAnswer(String questionId, Answer answer) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
      FAQService faqS = (FAQService) PortalContainer.getInstance()
                                                    .getComponentInstanceOfType(FAQService.class);
      Question q = faqS.getQuestionById(questionId);
      //TODO resource bundle needed 
      String msg = "@"+answer.getResponseBy() + " has answered: <a href=" + q.getLink() + ">"
          + q.getQuestion() + "</a>";
      String body = q.getLink();
      Category cal = faqS.getCategoryById(q.getCategoryId());
      String spaceId = cal.getPath().split(org.exoplatform.faq.service.Utils.CATEGORY_PREFIX)[1].split("/")[0];
      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance()
                                                                    .getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                   .getComponentInstanceOfType(ActivityManager.class);
      // SpaceService spaceS = (SpaceService)
      // PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
      // Space space = spaceS.getSpaceById(spaceId) ;
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                              spaceId,
                                                              false);
      activityM.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, msg, body);

    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("Please check the integrated project does the social deploy? " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when post answer " + e.getMessage());
    }

  }

  @Override
  public void saveComment(String questionId, Comment comment) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
      FAQService faqS = (FAQService) PortalContainer.getInstance()
                                                    .getComponentInstanceOfType(FAQService.class);
      Question q = faqS.getQuestionById(questionId);
      //TODO resource bundle needed 
      String msg = "@"+comment.getCommentBy() + " has commented: <a href=" + q.getLink() + ">"
          + q.getQuestion() + "</a>";
      String body = q.getLink();
      Category cal = faqS.getCategoryById(q.getCategoryId());
      String spaceId = cal.getPath().split(org.exoplatform.faq.service.Utils.CATEGORY_PREFIX)[1].split("/")[0];
      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance()
                                                                    .getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                   .getComponentInstanceOfType(ActivityManager.class);
      // SpaceService spaceS = (SpaceService)
      // PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
      // Space space = spaceS.getSpaceById(spaceId) ;
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                              spaceId,
                                                              false);
      activityM.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, msg, body);

    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("Please check the integrated project does the social deploy? " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when add comment " + e.getMessage());
    }
  }

  @Override
  public void saveQuestion(Question question) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
    //TODO resource bundle needed 
      String msg = "@"+question.getAuthor() + " has been asked: <a href=" + question.getLink() + ">"
          + question.getQuestion() + "</a>";
      String body = question.getLink();
      FAQService faqS = (FAQService) PortalContainer.getInstance()
                                                    .getComponentInstanceOfType(FAQService.class);
      Category cal = faqS.getCategoryById(question.getCategoryId());
      String spaceId = cal.getPath().split(org.exoplatform.faq.service.Utils.CATEGORY_PREFIX)[1].split("/")[0];
      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance()
                                                                    .getComponentInstanceOfType(IdentityManager.class);
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                   .getComponentInstanceOfType(ActivityManager.class);
      // SpaceService spaceS = (SpaceService)
      // PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
      // Space space = spaceS.getSpaceById(spaceId) ;
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                              spaceId,
                                                              false);
      activityM.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, msg, body);
    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("Please check the integrated project does the social deploy? " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when add new questin " + e.getMessage());
    }

  }

  @Override
  public void saveAnswer(String questionId, Answer[] answers) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager");
      FAQService faqS = (FAQService) PortalContainer.getInstance()
                                                    .getComponentInstanceOfType(FAQService.class);
      Question q = faqS.getQuestionById(questionId);
      if (answers.length == 1) {
        Answer answer = answers[0];
      //TODO resource bundle needed 
        String msg = "@"+answer.getResponseBy() + " has answered: <a href=" + q.getLink() + ">"
            + q.getQuestion() + "</a>";
        String body = q.getLink();
        Category cal = faqS.getCategoryById(q.getCategoryId());
        String spaceId = cal.getPath().split(org.exoplatform.faq.service.Utils.CATEGORY_PREFIX)[1].split("/")[0];
        IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance()
                                                                      .getComponentInstanceOfType(IdentityManager.class);
        ActivityManager activityM = (ActivityManager) PortalContainer.getInstance()
                                                                     .getComponentInstanceOfType(ActivityManager.class);
        // SpaceService spaceS = (SpaceService)
        // PortalContainer.getInstance().getComponentInstanceOfType(SpaceService.class);
        // Space space = spaceS.getSpaceById(spaceId) ;
        Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                spaceId,
                                                                false);
        activityM.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, msg, body);
      }
    } catch (ClassNotFoundException e) {
      if (LOG.isDebugEnabled())
        LOG.debug("Please check the integrated project does the social deploy? " + e.getMessage());
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when post answer " + e.getMessage());
    }
  }

}
