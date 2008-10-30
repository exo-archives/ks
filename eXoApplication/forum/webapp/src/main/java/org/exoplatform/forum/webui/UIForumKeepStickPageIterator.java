/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 30-10-2008 - 10:35:05  
 */
public class UIForumKeepStickPageIterator extends UIForm {

	private JCRPageList pageList ;
	private long page = 1 ;
	private int endTabPage = 0;
	private int beginTabPage = 0;
	private Map<Long, List<String>> pageCheckedList = new HashMap<Long, List<String>>();
	public UIForumKeepStickPageIterator () throws Exception {
	}
	
	public List<String> getListChecked(long page) {
	  return pageCheckedList.get(page);
  }
	
	public void cleanCheckedList() {
	  this.pageCheckedList.clear();
  }
	
	public void updatePageList(JCRPageList pageList ) {
		this.pageList = pageList ;
	}
	
	public List<String> getTotalpage() throws	Exception {
		int max_Page = (int)pageList.getAvailablePage() ;
		if(this.page > max_Page) this.page = max_Page ;
		long page = this.page ;
		if(page <= 3) {
			beginTabPage = 1 ;
			if(max_Page <= 7)
				endTabPage = max_Page ;
			else endTabPage = 7 ;
		} else {
			if(max_Page > (page + 3)) {
				endTabPage = (int) (page + 3) ;
				beginTabPage = (int) (page - 3) ;
			} else {
				endTabPage = max_Page ;
				if(max_Page > 7) beginTabPage = max_Page - 6 ;
				else beginTabPage = 1 ;
			}
		}
		List<String> temp = new ArrayList<String>() ;
		for (int i = beginTabPage; i <= endTabPage; i++) {
			temp.add("" + i) ;
		}
		return temp ;
	}

	public List<Long> getInfoPage() throws	Exception {
		List<Long> temp = new ArrayList<Long>() ;
		temp.add(pageList.getPageSize()) ;//so item/trang
		temp.add(pageList.getCurrentPage()) ;//so trang hien tai
		temp.add(pageList.getAvailable()) ;//tong so item
		temp.add(pageList.getAvailablePage()) ;// so trang toi da
		return temp ;
	} 
	
	public void setSelectPage(long page) {
		this.page = page;
	}
	
	public long getPageSelected() {
		return this.page ;
	}
		
	static public class GoPageActionListener extends EventListener<UIForumKeepStickPageIterator> {
		@SuppressWarnings("unchecked")
    public void execute(Event<UIForumKeepStickPageIterator> event) throws Exception {
			UIForumKeepStickPageIterator keepStickPageIter = event.getSource() ;
			UIComponent component = keepStickPageIter;
			List<String> checkedList =  new ArrayList<String>();
			try{
				List<UIComponent> children = keepStickPageIter.getChildren() ;
				for(UIComponent child : children) {
					if(child instanceof UIFormCheckBoxInput) {
						if(((UIFormCheckBoxInput)child).isChecked()) {
							checkedList.add(child.getId()) ;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(component instanceof UITopicDetail) {
				UITopicDetail topicDetail = (UITopicDetail) component;
				topicDetail.setIdPostView("top") ;
			}
			String stateClick = event.getRequestContext().getRequestParameter(OBJECTID).trim() ;
			long maxPage = keepStickPageIter.pageList.getAvailablePage() ;
			long presentPage	= keepStickPageIter.page ;
			if(stateClick.equalsIgnoreCase("next")) {
				if(presentPage < maxPage){
					keepStickPageIter.page = presentPage + 1 ;
				}
			} else if(stateClick.equalsIgnoreCase("previous")){
				if(presentPage > 1){
					keepStickPageIter.page = presentPage - 1 ;
				}
			} else if(stateClick.equalsIgnoreCase("last")) {
				if(presentPage != maxPage) {
					keepStickPageIter.page = maxPage ;
				}
			} else if(stateClick.equalsIgnoreCase("first")) {
				if(presentPage != 1) {
					keepStickPageIter.page = 1 ;
				}
			} else {
				long temp = Long.parseLong(stateClick) ;
				if(temp > 0 && temp <= maxPage && temp != presentPage) {
					keepStickPageIter.page = temp ;
				}
			}
			keepStickPageIter.pageCheckedList.put(presentPage, checkedList);
			event.getRequestContext().addUIComponentToUpdateByAjax(component) ;
		}
	}
}
