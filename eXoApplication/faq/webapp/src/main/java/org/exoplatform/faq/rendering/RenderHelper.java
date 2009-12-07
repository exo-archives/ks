/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.faq.rendering;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.faq.service.Answer;
import org.exoplatform.faq.service.Comment;
import org.exoplatform.faq.service.Question;
import org.exoplatform.ks.bbcode.core.BBCodeRenderer;
import org.exoplatform.ks.rendering.MarkupRenderingService;
import org.exoplatform.ks.rendering.spi.MarkupRenderDelegate;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class RenderHelper {

  private MarkupRenderingService markupRenderingService;
  
  
  public RenderHelper() {
  }

  /**
   * Render markup for a faq answer
   * 
   * @param answer
   * @return
   */
  public String renderAnswer(Answer answer) {
    try {
      return getMarkupRenderingService().delegateRendering(new AnswerDelegate(), answer);
    } catch (Exception e) {
      throw new RenderingException(e);
    }
  }
  
  public String renderComment(Comment comment) {
  	try {
  		return getMarkupRenderingService().delegateRendering(new CommentDelegate(), comment);
  	} catch (Exception e) {
  		throw new RenderingException(e);
  	}
  }
  
  public String renderQuestion(Question question) {
  	try {
  		return getMarkupRenderingService().delegateRendering(new QuestionDelegate(), question);
  	} catch (Exception e) {
  		throw new RenderingException(e);
  	}
  }

  
  
  static class AnswerDelegate implements MarkupRenderDelegate<Answer> {
    
    public String getMarkup(Answer answer) {
      return answer.getResponses();
    }

    public String getSyntax(Answer target) {
      return BBCodeRenderer.BBCODE_SYNTAX_ID;
    }

  }

  static class CommentDelegate implements MarkupRenderDelegate<Comment> {
  	
  	public String getMarkup(Comment answer) {
  		return answer.getComments();
  	}
  	
  	public String getSyntax(Comment target) {
  		return BBCodeRenderer.BBCODE_SYNTAX_ID;
  	}
  	
  }
  
  static class QuestionDelegate implements MarkupRenderDelegate<Question> {
  	
  	public String getMarkup(Question question) {
  		return question.getDetail();
  	}
  	
  	public String getSyntax(Question target) {
  		return BBCodeRenderer.BBCODE_SYNTAX_ID;
  	}
  	
  }
  
  
  public MarkupRenderingService getMarkupRenderingService() {
    if (this.markupRenderingService == null) {
      this.markupRenderingService = (MarkupRenderingService) ExoContainerContext.getCurrentContainer()
                                                                                   .getComponentInstanceOfType(MarkupRenderingService.class);
      /*
      
      BBCodeRenderer renderer = (BBCodeRenderer) markupRenderingService.getRenderer(BBCodeRenderer.BBCODE_SYNTAX_ID);
      renderer.setBbCodeProvider(new ExtendedBBCodeProvider());
      */
      
   }
    

    return this.markupRenderingService;
  }
  
  public void setMarkupRenderingService(MarkupRenderingService markupRenderingService) {
    this.markupRenderingService = markupRenderingService;
  }
  
}
