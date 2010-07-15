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
package org.exoplatform.poll.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.poll.service.DataStorage;
import org.exoplatform.poll.service.Poll;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

public class JCRDataStorage implements	DataStorage, PollNodeTypes {
	
	private NodeHierarchyCreator nodeHierarchyCreator_;

  private RepositoryService    repoService_;

  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repoService) {
    nodeHierarchyCreator_ = nodeHierarchyCreator;
    repoService_ = repoService;
  }
  
  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    return (Node) getSession(sessionProvider).getItem(nodePath);
  }
  
  public Session getSession(SessionProvider sprovider) throws Exception{
    ManageableRepository currentRepo = repoService_.getCurrentRepository() ;
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo) ;
  }
  
  // Path: /exo:applications/eXoPolls/   using for: $PORTAL/Polls
  private Node getPublicPollHomeNode(SessionProvider sProvider) throws Exception {
    Node publicApp = getNodeByPath(nodeHierarchyCreator_.getPublicApplicationNode(sProvider).getPath(), sProvider);
    try {
      return publicApp.getNode(EXO_POLLS);
    } catch (Exception e) {
       Node pollApp = publicApp.addNode(EXO_POLLS, NT_UNSTRUCTURED);
       publicApp.getSession().save();
      return pollApp;
    }
  }
  // Path: /Groups/   using for: $GROUP/ApplicationData/eXoPolls
  private Node getGroupPollHomeNode(SessionProvider sProvider) throws Exception {
  	Node privateApp = getNodeByPath(nodeHierarchyCreator_.getJcrPath("groupsPath"), sProvider);
  	return privateApp;
  }

  private Node getParentNode(SessionProvider sProvider, String parentId) throws Exception {
  	Node appNode = null;
		try { // id = /exo:applications/${forumId}/${topicId}/
			appNode = getNodeByPath(parentId, sProvider);
		} catch (Exception e) {
			if (e instanceof PathNotFoundException || e instanceof RepositoryException) {
				if(parentId.indexOf(APPLICATION_DATA) > 0) {// id = $GROUP/ApplicationData/eXoPolls
					return getNode(getGroupPollHomeNode(sProvider), parentId);
				} else if(parentId.indexOf(POLLS) > 0){// id = $PORTAL/Polls
					return getNode(getPublicPollHomeNode(sProvider), parentId);
				}
			} else 
					e.printStackTrace();
		}
  	return appNode;
  }
  
	public Poll getPoll(String pollId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			String parentId = "";
			if(pollId.lastIndexOf("/") > 0) {
				parentId = pollId.substring(0, pollId.lastIndexOf("/")+1);
				pollId = pollId.substring(pollId.lastIndexOf("/")+1);
			}
			Node appNode = getParentNode(sProvider, parentId);
			Node pollNode = appNode.getNode(pollId);
			return getPollNode(pollNode);	
		} catch (Exception e) {
			return getPollNode(getNodeById(sProvider, pollId));	
		} finally {
			sProvider.close();
		}
	}

	private Node getNodeById(SessionProvider sProvider, String pollId) throws Exception {
		QueryManager qm = getSession(sProvider).getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer(JCR_ROOT);
		queryString.append("//element(*,").append(EXO_POLL).append(")")
		.append("[fn:name() = '").append(pollId).append("']");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		NodeIterator iter = result.getNodes();
		if(iter.getSize() > 0) return iter.nextNode();
		return null;
	}
	
	private Poll getPollNode(Node pollNode) throws Exception {
		if(pollNode == null) return null;
		Poll pollNew = new Poll();
		pollNew.setId(pollNode.getName());
		pollNew.setParentPath(pollNode.getParent().getPath());
		if (pollNode.hasProperty(EXO_OWNER))
			pollNew.setOwner(pollNode.getProperty(EXO_OWNER).getString());
		if (pollNode.hasProperty(EXO_CREATED_DATE))
			pollNew.setCreatedDate(pollNode.getProperty(EXO_CREATED_DATE).getDate().getTime());
		if (pollNode.hasProperty(EXO_MODIFIED_BY))
			pollNew.setModifiedBy(pollNode.getProperty(EXO_MODIFIED_BY).getString());
		if (pollNode.hasProperty(EXO_MODIFIED_DATE))
			pollNew.setModifiedDate(pollNode.getProperty(EXO_MODIFIED_DATE).getDate().getTime());
		if (pollNode.hasProperty(EXO_LASTVOTE))
			pollNew.setLastVote(pollNode.getProperty(EXO_LASTVOTE).getDate().getTime());
		if (pollNode.hasProperty(EXO_TIME_OUT))
			pollNew.setTimeOut(pollNode.getProperty(EXO_TIME_OUT).getLong());
		if (pollNode.hasProperty(EXO_QUESTION))
			pollNew.setQuestion(pollNode.getProperty(EXO_QUESTION).getString());

		if (pollNode.hasProperty(EXO_OPTION))
			pollNew.setOption(valuesToArray(pollNode.getProperty(EXO_OPTION).getValues()));
		if (pollNode.hasProperty(EXO_VOTE))
			pollNew.setVote(valuesToArray(pollNode.getProperty(EXO_VOTE).getValues()));

		if (pollNode.hasProperty(EXO_USER_VOTE))
			pollNew.setUserVote(valuesToArray(pollNode.getProperty(EXO_USER_VOTE).getValues()));
		if (pollNode.hasProperty(EXO_IS_MULTI_CHECK))
			pollNew.setIsMultiCheck(pollNode.getProperty(EXO_IS_MULTI_CHECK).getBoolean());
		if (pollNode.hasProperty(EXO_IS_AGAIN_VOTE))
			pollNew.setIsAgainVote(pollNode.getProperty(EXO_IS_AGAIN_VOTE).getBoolean());
		if (pollNode.hasProperty(EXO_IS_CLOSED))
			pollNew.setIsClosed(pollNode.getProperty(EXO_IS_CLOSED).getBoolean());
		return pollNew ;
	}
	
	public List<Poll>getPagePoll() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		List<Poll> listPoll = new ArrayList<Poll>();
		try {
			NodeIterator iter = getIterNodePoll(sProvider);
			while (iter.hasNext()) {
				Node node = iter.nextNode();
				listPoll.add(getPollNode(node));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sProvider.close();
		}
		return listPoll;
	}
	
	private NodeIterator getIterNodePoll(SessionProvider sProvider) throws Exception {
		QueryManager qm = getSession(sProvider).getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer(JCR_ROOT);
		queryString.append("//element(*,").append(EXO_POLL).append(")")
		.append(" order by @").append(EXO_CREATED_DATE).append(" descending");
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		return result.getNodes();
	}
	
	public List<String>getListPollId() throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		List<String> listPoll = new ArrayList<String>();
		try {
			NodeIterator iter = getIterNodePoll(sProvider);
			while (iter.hasNext()) {
				Node node = iter.nextNode();
				listPoll.add(node.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sProvider.close();
		}
		return listPoll;
		
	}
	
	
	public Poll removePoll(String pollId) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		Poll poll = null;
		try {
			Node pollNode = null;
			if((pollId.lastIndexOf("/") > 0)) {
				pollNode = getNodeByPath(pollId, sProvider);
			} else {
				pollNode = getNodeById(sProvider, pollId);
			}
			poll = getPollNode(pollNode);
			Node parentNode = pollNode.getParent();
			pollNode.remove();
			if(parentNode.hasProperty(EXO_IS_POLL)) {
				parentNode.setProperty(EXO_IS_POLL, false);
			}
			if(parentNode.isNew()) parentNode.getSession().save();
			else parentNode.save();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sProvider.close();
		}
		return poll;
	}

	private Node getNode(Node nodeApp, String ids) throws Exception {
		Node node = null;
		if(ids.indexOf("/") < 0) node = nodeApp.addNode(ids);
		else {
			String []ar = ids.split("/");
			for (int i = 0; i < ar.length; i++) {
				try {
					node = nodeApp.getNode(ar[i]);
				} catch (PathNotFoundException e) {
					node = nodeApp.addNode(ar[i], NT_UNSTRUCTURED);
				}
				nodeApp = node;
			}
			if(nodeApp.isNew()) nodeApp.getSession().save();
			else nodeApp.getParent().save();
		}
		return node;
	}
	
	public void savePoll(Poll poll, boolean isNew, boolean isVote) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			Node pollNode;
			String pollId = poll.getId();
			Node parentNode = getParentNode(sProvider, (poll.getParentPath() != null)?poll.getParentPath():pollId);
			if (isVote) {
				pollNode = parentNode.getNode(pollId);
				pollNode.setProperty(EXO_VOTE, poll.getVote());
				pollNode.setProperty(EXO_USER_VOTE, poll.getUserVote());
				try {
					pollNode.setProperty(EXO_LASTVOTE, getGreenwichMeanTime());// new property 2.0 to 2.1
				} catch (RepositoryException e) {
				}
			} else {
				if (isNew) {
					if(parentNode.hasNode(pollId)) return;
					pollNode = parentNode.addNode(pollId, EXO_POLL);// add node
					pollNode.setProperty(EXO_ID, pollId);
					pollNode.setProperty(EXO_OWNER, poll.getOwner());
					pollNode.setProperty(EXO_USER_VOTE, new String[] {});
					pollNode.setProperty(EXO_CREATED_DATE, getGreenwichMeanTime());
					pollNode.setProperty(EXO_MODIFIED_DATE, getGreenwichMeanTime());
					if(parentNode.hasProperty(EXO_IS_POLL)){
						parentNode.setProperty(EXO_IS_POLL, true);
					}
				} else {
					pollNode = parentNode.getNode(pollId);
				}
				if (poll.getUserVote().length > 0) {
					pollNode.setProperty(EXO_USER_VOTE, poll.getUserVote());
				}
				pollNode.setProperty(EXO_VOTE, poll.getVote());
				pollNode.setProperty(EXO_MODIFIED_BY, poll.getModifiedBy());
				if (poll.getTimeOut() == 0) {
					pollNode.setProperty(EXO_MODIFIED_DATE, getGreenwichMeanTime());
				}
				pollNode.setProperty(EXO_TIME_OUT, poll.getTimeOut());
				pollNode.setProperty(EXO_QUESTION, poll.getQuestion());
				pollNode.setProperty(EXO_OPTION, poll.getOption());
				pollNode.setProperty(EXO_IS_MULTI_CHECK, poll.getIsMultiCheck());
				pollNode.setProperty(EXO_IS_CLOSED, poll.getIsClosed());
				pollNode.setProperty(EXO_IS_AGAIN_VOTE, poll.getIsAgainVote());
			}
			if(parentNode.isNew()) parentNode.getSession().save();
			else parentNode.save();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			sProvider.close();
		}
		
	}

	public void setClosedPoll(Poll poll) throws Exception {
		SessionProvider sProvider = SessionProvider.createSystemProvider();
		try {
			Node appNode = getParentNode(sProvider, poll.getParentPath());
			Node pollNode = appNode.getNode(poll.getId());
			pollNode.setProperty(EXO_IS_CLOSED, poll.getIsClosed());
			if (poll.getTimeOut() == 0) {
				pollNode.setProperty(EXO_MODIFIED_DATE, getGreenwichMeanTime());
				pollNode.setProperty(EXO_TIME_OUT, 0);
			}
			appNode.save();
		} catch (Exception e) {
		} finally {
			sProvider.close();
		}
	}
	
	public static Calendar getGreenwichMeanTime() {
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setLenient(false);
		int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset);
		return calendar;
	}
	
	public static boolean isEmpty(String s) {
		return (s == null || s.trim().length() <= 0)?true:false;
  }
	
	public String[] valuesToArray(Value[] Val) throws Exception {
    if (Val.length < 1) return new String[] {};
    List<String> list = new ArrayList<String>();
    String s;
    for (int i = 0; i < Val.length; ++i) {
    	 s = Val[i].getString();
    	 if(!isEmpty(s)) list.add(s);
    }
    return list.toArray(new String[list.size()]);
  }
	
	public List<String> valuesToList(Value[] values) throws Exception {
    List<String> list = new ArrayList<String>();
    if (values.length < 1) return list;
    String s;
    for (int i = 0; i < values.length; ++i) {
			s = values[i].getString();
			if (!isEmpty(s)) list.add(s);
    }
    return list;
  }

  
}
