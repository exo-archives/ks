/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.faq.webui.viewer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 24, 2009 - 4:32:48 AM  
 */

@ComponentConfig(
		template =	"app:/templates/faq/webui/UIViewer.gtmpl",
		events = {
		  	 @EventConfig(listeners = UIViewer.ChangePathActionListener.class)
		}
)
public class UIViewer extends UIContainer {
	private FAQService fAqService;
	private String path = Utils.CATEGORY_HOME;
	public UIViewer() {
		 fAqService = (FAQService)PortalContainer.getComponent(FAQService.class) ;
  }
		
	static public class ChangePathActionListener extends EventListener<UIViewer> {
		public void execute(Event<UIViewer> event) throws Exception {
			String path = event.getRequestContext().getRequestParameter(OBJECTID);
			UIViewer viewer = event.getSource();
			viewer.path = path;
			event.getRequestContext().addUIComponentToUpdateByAjax(viewer);
		}
	}
	
	@SuppressWarnings("unused")
  private List<String> arrangeList(List<String> list) {
		List<String> newList = new ArrayList<String>();
		for (int i = (list.size()-1); i >= 0; i--) {
			newList.add(list.get(i));
		}
		return newList;
	} 
	
	@SuppressWarnings("unused")
  private CategoryInfo getCategoryInfo() throws Exception {
		CategoryInfo categoryInfo = new CategoryInfo();
		try {
			categoryInfo = this.fAqService.getCategoryInfo(this.path);
    } catch (Exception e) {
    	e.printStackTrace();
    }
		/*
		SubCategoryInfo childCategoryInfo;
		QuestionInfo questionInfo = new QuestionInfo();
		SubCategoryInfo subCategoryInfo = new SubCategoryInfo();
		List<SubCategoryInfo> subCateList = new ArrayList<SubCategoryInfo>();
		List<SubCategoryInfo> childCateList = new ArrayList<SubCategoryInfo>();
		List<String> answers = new ArrayList<String>();
		List<QuestionInfo> questionInfos =  new ArrayList<QuestionInfo>();
		
		for (int i = 0; i < 5; i++) {
			questionInfo = new QuestionInfo();
			questionInfo.setId("quesitonId" + i);
			questionInfo.setQuestion("Question number " + (i+1) + " of this cate");
			answers = new ArrayList<String>();
			for (int j = 0; j < 4; j++) {
				answers.add(" Answer number " + (j+1) + " of question "+(i+1)+" of this category ");
			}
			questionInfo.setAnswers(answers);
			questionInfos.add(questionInfo);
    }
		//category add question
		categoryInfo.setQuestionInfos(questionInfos);
		
		List<String> pathName = new ArrayList<String>();
		pathName.add("Cate Parent0");
		pathName.add("Cate Parent1");
		pathName.add("Cate Parent2");
		path = "cate0/cate1/cate2/cate";// set
		//set PathName
		categoryInfo.setPathName(pathName);
		categoryInfo.setPath(path);
		for (int i = 0; i < 3; i++) {
			childCategoryInfo = new SubCategoryInfo();
			childCategoryInfo.setId("categoryId" + i);
			childCategoryInfo.setPath(path+"/"+"categoryId" + i);
			childCategoryInfo.setName("Category " + (i+1));
			// get Question list of child category
			questionInfos = new ArrayList<QuestionInfo>();
			for (int j = 0; j < 3; j++) {
				questionInfo = new QuestionInfo();
				questionInfo.setId("quesitonId" + i + "" + j);
				questionInfo.setQuestion("Question number " + (j+1) + " of cate" + (i+1));
				// get Answers of this question
				answers = new ArrayList<String>();
				for (int t = 0; t < 4; t++) {
					answers.add(" Answer number " + (t+1) + " of question number " + (j+1));
        }
				questionInfo.setAnswers(answers);
				questionInfos.add(questionInfo);
			}
			childCategoryInfo.setQuestionInfos(questionInfos);
			// get SubCategory list of child category
			childCateList = new ArrayList<SubCategoryInfo>();
			for (int j = 0; j < 3; j++) {
				subCategoryInfo = new SubCategoryInfo();
				subCategoryInfo.setName("Sub cate " + (j+1) + " of cate " + (i+1));
				subCategoryInfo.setPath(path+"/" + "childCat"+j);
				childCateList.add(subCategoryInfo);
      }
			childCategoryInfo.setSubCateInfos(childCateList);
			
			subCateList.add(childCategoryInfo);
		}
		categoryInfo.setSubCateInfos(subCateList);
		*/
		return categoryInfo;
	}
	
	
}
