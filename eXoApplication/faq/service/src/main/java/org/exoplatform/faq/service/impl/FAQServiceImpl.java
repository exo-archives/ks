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

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008  
 */
public class FAQServiceImpl implements FAQService{

	public List<Category> getAllCategories() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Question> getAllQuestions() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Category getCategoryById(String categoryId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public Question getQuestionById(String questionId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Question> getQuestionsByCatetory(String categoryId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Category> getSubCategories(String categoryId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void moveQuestions(List<String> questions, String srcCategoryId, String destCategoryId) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void removeCategory(String categoryId) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void removeQuestion(String categoryId, String questionId) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void saveCategory(Category cat, boolean isAddNew) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void saveQuestion(Question question, boolean isAddNew) throws Exception {
		// TODO Auto-generated method stub
		
	}
  
  
}