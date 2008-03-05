/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.content.service;

import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.content.model.ContentData;
import org.exoplatform.content.model.ContentItem;
import org.exoplatform.content.model.ContentNavigation;
import org.exoplatform.content.model.ContentNode;
/**
 * Created by The eXo Platform SARL        .
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Date: Jun 14, 2003
 * Time: 1:12:22 PM
 */
public interface ContentDAO {
  
  public void create(ContentNavigation navigation) throws Exception;
  
  public void save(ContentNavigation navigation) throws Exception;
  
  public void remove(String owner) throws Exception;
  
  public ContentNavigation get(String owner) throws Exception;  
  
  public ContentData getData(String id) throws Exception;
  
  public void removeCache(String id) throws Exception;
  
  public void removeData(String id) throws Exception;
  
  public void removeData(String owner, String type) throws Exception; 
  
  public void initListener(ComponentPlugin listener);
  
  public void addPlugin(ComponentPlugin plugin);
  
  public List<String> getTypes();
  
  public <T extends ContentItem> PageList getContentData(ContentNode node) throws Exception;
  
  public String toXML(Object object) throws Exception;
  
  public Object fromXML(String xml, Class<?> type) throws Exception;
  
}