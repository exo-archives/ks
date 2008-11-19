/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.service;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *					tuan.nguyen@exoplatform.com
 * Jul 2, 2007	
 * Editer by Vu Duy Tu
 * 				tu.duy@exoplatform.com
 * July 20, 2007
 */
public class TopicView {
	private Topic topic ;
	private JCRPageList pagePosts ;
	
	public TopicView() {}
	
	public Topic getTopic() { return topic;	}
	public void setTopicView(Topic topic) {	this.topic = topic; }
	
	public JCRPageList getPageList() { return pagePosts; }
	public void setPageList(JCRPageList pagePosts) {this.pagePosts = pagePosts; }
	
//	public List<Post> getAllPost(Session session) throws Exception {
//		JCRPageList	pageList = this.pagePosts;
//		List<Post> posts = new ArrayList<Post>();
//		int t = 1, j = 0;
//		long k = pageList.getPageSize();
//		for (int i = 0; i < pageList.getAvailable(); i++) {
//			if(k*t <= i){ ++t; j = 0;}
//			posts.add((Post)pageList.getPage(t, session).get(j));
//			++j;
//		}
//		return posts;
//	}
	
}
