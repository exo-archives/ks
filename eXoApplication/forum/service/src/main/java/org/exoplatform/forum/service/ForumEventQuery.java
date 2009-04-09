package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.commons.utils.ISO8601;


public class ForumEventQuery {
	long userPermission = 0;
	List<String> listOfUser = null;
	private String type ;
	private String keyValue ;
	private String valueIn ;
	private String path;
	private String byUser ;
	private String isLock;
	private String isClosed;
	private String topicCountMin;
	private String postCountMin;
	private String viewCountMin;
	private String moderator;
	private String remain;
	private Calendar fromDateCreated ;
	private Calendar toDateCreated ;
	private Calendar fromDateCreatedLastPost ;
	private Calendar toDateCreatedLastPost ;

	private boolean isAnd = false ;
	private boolean isEmpty = true ;
	public void setListOfUser(List<String> listOfUser){
		this.listOfUser = new ArrayList<String>();
		this.listOfUser.addAll(listOfUser);
	}
	
	public long getUserPermission() {
  	return userPermission;
  }

	public void setUserPermission(long userPermission) {
  	this.userPermission = userPermission;
  }
	
	public ForumEventQuery() {}
	
	public String getType() {
  	return type;
  }
	public void setType(String type) {
  	this.type = type;
  }
	public String getKeyValue() {
  	return keyValue;
  }
	public void setKeyValue(String keyValue) {
  	this.keyValue = keyValue;
  }
	public String getValueIn() {
  	return valueIn;
  }
	public void setValueIn(String valueIn) {
  	this.valueIn = valueIn;
  }
	public String getPath() {
  	return path;
  }
	public void setPath(String path) {
  	this.path = path;
  }
	public String getByUser() {
  	return byUser;
  }
	public void setByUser(String byUser) {
  	this.byUser = byUser;
  }
	public String getIsLock() {
  	return isLock;
  }
	public void setIsLock(String isLock) {
  	this.isLock = isLock;
  }
	public String getIsClose() {
		return isClosed;
	}
	public void setIsClose(String isClosed) {
		this.isClosed = isClosed;
	}
	public String getTopicCountMin() {
  	return topicCountMin;
  }
	public void setTopicCountMin(String topicCountMin) {
  	this.topicCountMin = topicCountMin;
  }
	public String getPostCountMin() {
  	return postCountMin;
  }
	public void setPostCountMin(String postCountMin) {
  	this.postCountMin = postCountMin;
  }
	public String getViewCountMin() {
  	return viewCountMin;
  }
	public void setViewCountMin(String viewCountMin) {
  	this.viewCountMin = viewCountMin;
  }
	public String getModerator() {
  	return moderator;
  }
	public void setModerator(String moderator) {
  	this.moderator = moderator;
  }
	public String getRemain() {
  	return remain;
  }
	public void setRemain(String remain) {
  	this.remain = remain;
  }
	public Calendar getFromDateCreated() {
  	return fromDateCreated;
  }
	public void setFromDateCreated(Calendar fromDateCreated) {
  	this.fromDateCreated = fromDateCreated;
  }
	public Calendar getToDateCreated() {
  	return toDateCreated;
  }
	public void setToDateCreated(Calendar toDateCreated) {
  	this.toDateCreated = toDateCreated;
  }
	public Calendar getFromDateCreatedLastPost() {
  	return fromDateCreatedLastPost;
  }
	public void setFromDateCreatedLastPost(Calendar fromDateCreatedLastPost) {
  	this.fromDateCreatedLastPost = fromDateCreatedLastPost;
  }
	public Calendar getToDateCreatedLastPost() {
  	return toDateCreatedLastPost;
  }
	public void setToDateCreatedLastPost(Calendar toDateCreatedLastPost) {
  	this.toDateCreatedLastPost = toDateCreatedLastPost;
  }
	public boolean getIsEmpty() {
	  return this.isEmpty ;
  }
	
	public String getPathQuery(List<String> listIds) {
		isAnd = false ; isEmpty = true;
		StringBuffer queryString = new StringBuffer() ;
    if(path != null && path.length() > 0) queryString.append("/jcr:root").append(path).append("//element(*,exo:").append(type).append(")") ;
    else  queryString.append("//element(*,exo:").append(type).append(")") ;
    StringBuffer stringBuffer = new StringBuffer() ;
    stringBuffer.append("[");
    if(valueIn.equals("title")) {
	    if(keyValue != null && keyValue.length() > 0 ) {
	    	stringBuffer.append("(jcr:contains(@exo:name, '").append(keyValue).append("'))") ;
	    	isAnd = true ;
	    }
    } else {
    	if(keyValue != null && keyValue.length() > 0 ) {
    		stringBuffer.append("(jcr:contains(., '").append(keyValue).append("'))") ;
    		isAnd = true ;
    	}
    }
    if(byUser != null && byUser.length() > 0) {
    	if(isAnd) stringBuffer.append(" and ");
    	stringBuffer.append("(");
    	byUser = byUser.replaceAll(";", ",") ;
    	String[] users = byUser.split(",") ;
    	int i = 0;
    	for (String string : users) {
    		string = string.trim();
    		if(i > 0) stringBuffer.append(" or ") ;
    		stringBuffer.append("(@exo:owner='").append(string).append("')") ;
    		++i;
      }
    	stringBuffer.append(")");
			isAnd = true ;
		}
    if(isAnd) isEmpty = false;
    if(isClosed != null && isClosed.length() > 0) {
    	if(userPermission == 1){
	    	if(type.equals("forum")){
	    		if(isAnd) stringBuffer.append(" and ");
	    		if(isClosed.equals("all")) {
		    		stringBuffer.append("(@exo:isClosed='false'");
		    		for(String str : listOfUser){
		    			stringBuffer.append(" or @exo:moderators='").append(str).append("'");
		    		}
		    		stringBuffer.append(")") ;
	    		} else if(isClosed.equals("false")){
	    			stringBuffer.append("(@exo:isClosed='").append(isClosed).append("')") ;
	    		} else if(isClosed.equals("true")){
	    			stringBuffer.append("(@exo:isClosed='").append(isClosed).append("' and (@exo:moderators='").
	    										append(listOfUser.get(0)).append("'") ;
	    			for(String str : listOfUser){
		    			stringBuffer.append(" or @exo:moderators='").append(str).append("'");
		    		}
	    			stringBuffer.append("))") ;
	    		}
	    		isAnd = true ;
	    	} else {
	    		if(!isClosed.equals("all")){
	    			if(isAnd) stringBuffer.append(" and ");
	    			stringBuffer.append("(@exo:isClosed='").append(isClosed).append("')") ;
	    			isAnd = true ;
	    		}
	    	}
    	} else {
    		if(!isClosed.equals("all")){
    			if(isAnd) stringBuffer.append(" and ");
    			stringBuffer.append("(@exo:isClosed='").append(isClosed).append("')") ;
    			isAnd = true ;
    		}
    	}
    }
    if(isLock != null && isLock.length() > 0) {
  		if(!isLock.equals("all")){
  			if(isAnd) stringBuffer.append(" and ");
  			stringBuffer.append("(@exo:isLock='").append(isLock).append("')") ;
  			isAnd = true ; isEmpty = false;
  		}
    }
    if(remain != null && remain.length() > 0) {
    	if(isAnd) stringBuffer.append(" and ");
    	stringBuffer.append("(").append(remain).append(")") ;
    	isAnd = true ;
    }
    if(moderator != null && moderator.length() > 0) {
    	if(isAnd) stringBuffer.append(" and ");
    	stringBuffer.append("(@exo:moderators='").append(moderator).append("')") ;
    	isAnd = true ;isEmpty = false;
    }
    String temp = setValueMin(topicCountMin, "topicCount") ;
    if(temp != null && temp.length() > 0) { 
    	stringBuffer.append(temp) ;
    }
    temp = setValueMin(postCountMin, "postCount") ;
    if(temp != null && temp.length() > 0) { 
    	stringBuffer.append(temp) ;
    }
    temp = setValueMin(viewCountMin, "viewCount") ;
    if(temp != null && temp.length() > 0) { 
    	stringBuffer.append(temp) ;
    }
    temp = setDateFromTo(fromDateCreated, toDateCreated, "createdDate") ;
    if(temp != null && temp.length() > 0) { 
    	stringBuffer.append(temp) ;
    }
    temp = setDateFromTo(fromDateCreatedLastPost, toDateCreatedLastPost, "lastPostDate") ;
    if(temp != null && temp.length() > 0) { 
    	stringBuffer.append(temp) ;
    }
    // add to search for user and moderator:
    if(type.equals("topic") && userPermission > 1){
			if(isAnd) stringBuffer.append(" and ");
			stringBuffer.append("(@exo:isApproved='true' and @exo:isActive='true' and @exo:isWaiting='false' and @exo:isActiveByForum='true')");
		} else if(type.equals("post")){
    	if(isAnd) stringBuffer.append(" and ");
    	stringBuffer.append("(@exo:userPrivate='exoUserPri'");
			for(String currentUser : listOfUser){
				stringBuffer.append(" or @exo:userPrivate='").append(currentUser).append("'");
			}
			stringBuffer.append(") and (@exo:isFirstPost='false')");
			if(userPermission > 1){
				stringBuffer.append(" and (@exo:isApproved='true' and @exo:isActiveByTopic='true' and @exo:isHidden='false')");
			}
    }
    
    if(listIds != null && listIds.size() > 0){
    	stringBuffer.append(" and (");
    	int size = listIds.size();
    	String searchBy = null;
    	if(type.equals(Utils.CATEGORY) || type.equals(Utils.FORUM)) searchBy = "fn:name()";
    	else searchBy = "@exo:path";
    	for(int i = 0; i < size; i ++){
    		stringBuffer.append(searchBy).append(" = '").append(listIds.get(i)).append("'");
    		if(i < size - 1) stringBuffer.append(" or ");
    	}
    	stringBuffer.append(")");
    }
    
    stringBuffer.append("]");
    if(isAnd) queryString.append(stringBuffer.toString()) ;
	  return queryString.toString();
  }
	
	private String setValueMin(String min, String property) {
		StringBuffer queryString = new StringBuffer() ;
    	if(Integer.parseInt(min) > 0) {
    		if(isAnd) queryString.append(" and ") ;
    		queryString.append("(@exo:").append(property).append(">=").append(min).append(")") ;
    		isAnd = true ;isEmpty = false;
    	}
		return queryString.toString() ;
	}

	private String setDateFromTo(Calendar fromDate, Calendar toDate, String property) {
		StringBuffer queryString = new StringBuffer() ;
		if(fromDate != null && toDate != null) {
			if(isAnd) queryString.append(" and ") ;
			queryString.append("((@exo:").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("')) and ") ;
			queryString.append("(@exo:").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))) ") ;
			isAnd = true ;isEmpty = false;
		} else if(fromDate != null){
			if(isAnd) queryString.append(" and ") ;
			queryString.append("(@exo:").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("'))") ;
			isAnd = true ;isEmpty = false;
		} else if(toDate != null){
			if(isAnd) queryString.append(" and ") ;
			queryString.append("(@exo:").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))") ;
			isAnd = true ;isEmpty = false;
		}
		return queryString.toString() ;
	}
}
