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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service;


/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * 28-08-2008 - 02:45:31	
 */
public class JobWattingForModerator {
	private JCRPageList topicUnApproved;
	private JCRPageList topicWaiting;
	private JCRPageList postsHidden;
	private JCRPageList postsUnApproved;
	
	public JCRPageList getTopicUnApproved() {
		return topicUnApproved;
	}

	public void setTopicUnApproved(JCRPageList topicUnApproved) {
		this.topicUnApproved = topicUnApproved;
	}

	public JCRPageList getTopicWaiting() {
		return topicWaiting;
	}

	public void setTopicWaiting(JCRPageList topicWaiting) {
		this.topicWaiting = topicWaiting;
	}

	public JCRPageList getPostsHidden() {
		return postsHidden;
	}

	public void setPostsHidden(JCRPageList postsHidden) {
		this.postsHidden = postsHidden;
	}

	public JCRPageList getPostsUnApproved() {
		return postsUnApproved;
	}

	public void setPostsUnApproved(JCRPageList postsUnApproved) {
		this.postsUnApproved = postsUnApproved;
	}

	public JobWattingForModerator() {
	}
}
