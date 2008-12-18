/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.webservice.ks.forum;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Oct 15, 2008  
 */
public class BeanToJsons {
  private List<Object> jsonList;
  
  public BeanToJsons(List<Object> list) {
    this.jsonList = list;
  }
  
  public List<Object> getJsonList() {
    return jsonList;
  }
  
  public void setJsonList(List<Object> objectList) {
    this.jsonList = objectList;
  }
}
