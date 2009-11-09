/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.ws;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Dec 17, 2008  
 */
public class BanIP {
  private String ip ;
  

  public BanIP() {}

  public BanIP(String str) {
    ip = str ;
  }
  
  public void setIp(String str) {
    this.ip = str;
  }
  public String getIp() {
    return ip;
  }
   
}
