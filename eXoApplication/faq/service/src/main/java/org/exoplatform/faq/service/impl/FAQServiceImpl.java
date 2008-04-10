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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.faq.service.impl;

import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Question;
import org.exoplatform.faq.service.QuestionPageList;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008  
 */
public class FAQServiceImpl implements FAQService{
	private JCRDataStorage jcrData_ ;
	
	public FAQServiceImpl(NodeHierarchyCreator nodeHierarchy) throws Exception {
		jcrData_ = new JCRDataStorage(nodeHierarchy) ;
	}
	public List<Category> getAllCategories(SessionProvider sProvider) throws Exception {
		return jcrData_.getAllCategories(sProvider);
	}

	public QuestionPageList getAllQuestions(SessionProvider sProvider) throws Exception {
		return jcrData_.getAllQuestions(sProvider);
	}

	public Category getCategoryById(String categoryId, SessionProvider sProvider) throws Exception {
		return jcrData_.getCategoryById(categoryId, sProvider);
	}

	public Question getQuestionById(String questionId, SessionProvider sProvider) throws Exception {
		return jcrData_.getQuestionById(questionId, sProvider);
	}

	public QuestionPageList getQuestionsByCatetory(String categoryId, SessionProvider sProvider) throws Exception {
		return jcrData_.getQuestionsByCatetory(categoryId, sProvider);
	}

	public List<Category> getSubCategories(String categoryId, SessionProvider sProvider) throws Exception {
		return jcrData_.getSubCategories(categoryId, sProvider);
	}

	public void moveQuestions(List<String> questions, String destCategoryId, SessionProvider sProvider) throws Exception {
		jcrData_.moveQuestions(questions, destCategoryId, sProvider) ;
	}

	public void removeCategory(String categoryId, SessionProvider sProvider) throws Exception {
		jcrData_.removeCategory(categoryId, sProvider) ;
	}

	public void removeQuestion(String questionId, SessionProvider sProvider) throws Exception {
		jcrData_.removeQuestion(questionId, sProvider) ;
	}

	public void saveCategory(String parentId, Category cat, boolean isAddNew, SessionProvider sProvider) throws Exception {
		jcrData_.saveCategory(parentId, cat, isAddNew, sProvider) ;
	}

	public void saveQuestion(Question question, boolean isAddNew, SessionProvider sProvider) throws Exception {
		jcrData_.saveQuestion(question, isAddNew, sProvider) ;
	}
}