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

package org.exoplatform.poll.service.impl;

import java.util.Date;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.poll.service.PollService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.picocontainer.Startable;

@NameTemplate(@Property(key="service", value="poll"))
public class PollServiceImpl implements Startable, PollService {
	private JCRDataStorage            storage_;
	
  public PollServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService reposervice) throws Exception {
  	storage_ = new JCRDataStorage(nodeHierarchyCreator, reposervice);
  }

  public void start() {
  	try {
			autodata();
		} catch (Exception e) {
			e.printStackTrace();
		}
  }

	public void stop() {
		
	}

	private void autodata() throws Exception {
		String portalName = "ksdemo";
		try {
			portalName = ExoContainerContext.getCurrentContainer().getContext().getName() ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Poll poll = new Poll();
		poll.setId("Poll8d83228a7f000101009a0a9f03743278");
		poll.setCreatedDate(new Date());
		poll.setIsAgainVote(false);
		poll.setIsClosed(false);
		poll.setIsMultiCheck(false);
		poll.setModifiedBy("root");
		poll.setOwner("root");
		poll.setVote(new String[]{"0","0","0"});
		poll.setParentPath(portalName + "/Polls");
		poll.setQuestion("Muon roi, muon roi phai khong em ?");
		poll.setOption(new String[]{"Anh noi se dua em di xuat cuoc doi", "Ma sao ko dua duoc doan duong em di", "Anh noi se om em khi gio dong ve"});
		storage_.savePoll(poll, true, false);
	}
	
	public Poll getPoll(String pollId) throws Exception {
		return storage_.getPoll(pollId);
	}

	public Poll removePoll(String pollId) throws Exception {
		return storage_.removePoll(pollId);
	}

	public void savePoll(Poll poll, boolean isNew, boolean isVote) throws Exception {
		storage_.savePoll(poll, isNew, isVote);
	}

	public void setClosedPoll(Poll poll) throws Exception {
		storage_.setClosedPoll(poll);
	}

	public List<Poll> getPagePoll() throws Exception {
		return storage_.getPagePoll();
	}

	public List<String> getListPollId() throws Exception {
		return storage_.getListPollId();
	}

  
}
