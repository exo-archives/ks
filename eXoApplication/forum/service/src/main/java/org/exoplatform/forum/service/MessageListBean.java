/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Oct 15, 2008  
 */
public class MessageListBean {
  private List<MessageBean> lastMessages;
  
  public MessageListBean(List<MessageBean> list) {
    this.lastMessages = list;
  }
  
  public List<MessageBean> getLastMessages() {
    return lastMessages;
  }
  
  public void setLastMessages(List<MessageBean> messageList) {
    this.lastMessages = messageList;
  }
}
