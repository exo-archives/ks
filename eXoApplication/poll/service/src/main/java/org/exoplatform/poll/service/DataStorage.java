package org.exoplatform.poll.service;

import java.util.List;


public interface DataStorage {
	
	Poll getPoll(String pollId) throws Exception;

  void savePoll(Poll poll, boolean isNew, boolean isVote) throws Exception;
  
  Poll removePoll(String pollId) throws Exception;
  
  void setClosedPoll(Poll poll) throws Exception;
  
  List<Poll>getPagePoll() throws Exception;
  List<String>getListPollId() throws Exception;
}
