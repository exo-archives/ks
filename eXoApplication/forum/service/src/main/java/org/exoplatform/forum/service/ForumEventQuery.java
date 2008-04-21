package org.exoplatform.forum.service;

import java.util.Calendar;

import org.exoplatform.commons.utils.ISO8601;


public class ForumEventQuery {
	private String type ;
	private String keyValue ;
	private String valueIn ;
	private String path;
	private String byUser ;
	private String isLock;
	private String isClosed;
	private String topicCountMin;
	private String topicCountMax;
	private String postCountMin;
	private String postCountMax;
	private String viewCountMin;
	private String viewCountMax;
	private String moderator;
	private Calendar fromDateCreated ;
	private Calendar toDateCreated ;
	private Calendar fromDateCreatedLastPost ;
	private Calendar toDateCreatedLastPost ;
	
	private boolean isAnd = false ;
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
	public String getTopicCountMax() {
  	return topicCountMax;
  }
	public void setTopicCountMax(String topicCountMax) {
  	this.topicCountMax = topicCountMax;
  }
	public String getPostCountMin() {
  	return postCountMin;
  }
	public void setPostCountMin(String postCountMin) {
  	this.postCountMin = postCountMin;
  }
	public String getPostCountMax() {
  	return postCountMax;
  }
	public void setPostCountMax(String postCountMax) {
  	this.postCountMax = postCountMax;
  }
	public String getViewCountMin() {
  	return viewCountMin;
  }
	public void setViewCountMin(String viewCountMin) {
  	this.viewCountMin = viewCountMin;
  }
	public String getViewCountMax() {
  	return viewCountMax;
  }
	public void setViewCountMax(String viewCountMax) {
  	this.viewCountMax = viewCountMax;
  }
	public String getModerator() {
  	return moderator;
  }
	public void setModerator(String moderator) {
  	this.moderator = moderator;
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
	
	public String getPathQuery() {
		isAnd = false ;
		StringBuffer queryString = new StringBuffer() ;
    if(path != null && path.length() > 0) queryString.append("/jcr:root").append(path).append("//element(*,exo:").append(type).append(")") ;
    else  queryString.append("//element(*,").append(type).append(")") ;
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
			stringBuffer.append("(@exo:owner='").append(byUser).append("')") ;
			isAnd = true ;
		}
    if(isClosed != null && isClosed.length() > 0 && !isClosed.equals("all")) {
    	if(isAnd) stringBuffer.append(" and ");
    	stringBuffer.append("(@exo:isClosed='").append(isClosed).append("')") ;
    	isAnd = true ;
    }
    if(isLock != null && isLock.length() > 0 && !isLock.equals("all")) {
    	if(isAnd) stringBuffer.append(" and ");
    	stringBuffer.append("(@exo:isLock='").append(isLock).append("')") ;
    	isAnd = true ;
    }
    if(moderator != null && moderator.length() > 0) {
    	if(isAnd) stringBuffer.append(" and ");
    	stringBuffer.append("(@exo:moderators='").append(moderator).append("')") ;
    	isAnd = true ;
    }
    String temp = setMaxAndMin(topicCountMax, topicCountMin, "topicCount") ;
    if(temp != null && temp.length() > 0) { 
    	stringBuffer.append(temp) ;
    }
    temp = setMaxAndMin(postCountMax, postCountMin, "postCount") ;
    if(temp != null && temp.length() > 0) { 
    	stringBuffer.append(temp) ;
    }
    temp = setMaxAndMin(viewCountMax, viewCountMin, "viewCount") ;
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
    stringBuffer.append("]");
    if(isAnd) queryString.append(stringBuffer.toString()) ;
	  return queryString.toString();
  }
	
	private String setMaxAndMin(String max, String min, String property) {
		StringBuffer queryString = new StringBuffer() ;
		if(max !=null && min != null){
    	if(Integer.parseInt(max) > Integer.parseInt(min)) {
    		if(isAnd) queryString.append(" and ");
    		queryString.append("(@exo:").append(property).append(" <= '").append(max).append("' and @exo:").append(property).append(" >= '").append(min).append("'");
    		isAnd = true ;
    	} else if(Integer.parseInt(max) == Integer.parseInt(min)) {
    		if(isAnd) queryString.append(" and ");
    		queryString.append("(@exo:").append(property).append("='").append(max).append("'");
    		isAnd = true ;
    	}
    } else if(max != null) {
    	if(Integer.parseInt(max) > 0) {
    		if(isAnd) queryString.append(" and ");
    		queryString.append("(@exo:").append(property).append(" <= '").append(max).append("')") ;
    		isAnd = true ;
    	}
    } else if(min != null) {
    	if(Integer.parseInt(min) > 0) {
    		if(isAnd) queryString.append(" and ") ;
    		queryString.append("(@exo:").append(property).append(" >= ").append(min).append("')") ;
    		isAnd = true ;
    	}
    }
		return queryString.toString() ;
	}

	private String setDateFromTo(Calendar fromDate, Calendar toDate, String property) {
		StringBuffer queryString = new StringBuffer() ;
		if(fromDate != null && toDate != null) {
			if(isAnd) queryString.append(" and ") ;
			queryString.append("((@exo:").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("')) and ") ;
			queryString.append("(@exo:").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))) ") ;
			isAnd = true ;
		} else if(fromDate != null){
			queryString.append("(@exo:").append(property).append(" >= xs:dateTime('").append(ISO8601.format(fromDate)).append("'))") ;
			isAnd = true ;
		} else if(toDate != null){
			queryString.append("(@exo:").append(property).append(" <= xs:dateTime('").append(ISO8601.format(toDate)).append("'))") ;
			isAnd = true ;
		}
		return queryString.toString() ;
	}
}



















