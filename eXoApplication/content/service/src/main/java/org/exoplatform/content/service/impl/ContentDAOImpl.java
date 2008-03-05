/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.content.service.impl;

import java.util.Calendar;
import java.util.Date;

import javax.jcr.Node;

import org.exoplatform.content.model.ContentData;
import org.exoplatform.content.model.ContentNavigation;
import org.exoplatform.content.service.BaseContentService;
import org.exoplatform.content.service.ContentDAO;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL        .
 * Author : Le Bien Thuy  
 *          lebienthuy@gmail.com
 * Date: 3/5/2007
 * Time: 1:12:22 PM
 */
public class ContentDAOImpl extends BaseContentService implements ContentDAO {
  
  final public static String USER_TYPE = "user";
  final public static String GROUP_TYPE = "group";
  final public static String PORTAL_TYPE = "portal";
  
  final private static String NODE_NAME = "contentNavigation.xml";
  
  final private static String ID = "id" ;
  final private static String OWNER = "ownerId" ;
  final private static String DATA_TYPE = "dataType" ;
  final private static String DATA = "data";
  final private static String CREATED_DATE = "createdDate";
  final private static String MODIFIED_DATE = "modifiedDate";
  
  final private static String DATA_NODE_TYPE = "exo:content";
  
  final public static String APPLICATION_NAME = "ContentService";
  private NodeHierarchyCreator nodeCreator_ ;
  
  public ContentDAOImpl(CacheService cservice, NodeHierarchyCreator creator) throws Exception {
    super(cservice);
    nodeCreator_ = creator ;
  }
  
  private Node createNode(Node parent, String name) throws Exception {
    if(parent.hasNode(name)) return parent.getNode(name) ;
    Node node = parent.addNode(name) ;
    parent.save() ;
    return node ;
  }
  
  private Node createApplicationNode(SessionProvider sessionProvider, String userName) throws Exception {
    Node userAppsNode = nodeCreator_.getUserApplicationNode(sessionProvider, userName) ;
    Node appNode = createNode(userAppsNode, APPLICATION_NAME) ;    
    return appNode ;
  }
  
  private Node getApplicationNode(SessionProvider sessionProvider, String userName) throws Exception {
    Node userAppsNode = nodeCreator_.getUserApplicationNode(sessionProvider, userName) ;
    if(userAppsNode.hasNode(APPLICATION_NAME)) return userAppsNode.getNode(APPLICATION_NAME) ;
    return null ;
  }
  
  public void create(final ContentNavigation navigation) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Node appNode = createApplicationNode(sessionProvider, navigation.getOwner()) ;
    ContentData data = new ContentData();
    data.setDataType(ContentNavigation.class.getName());    
    data.setId(navigation.getOwner()+"::"+ContentNavigation.class.getName());
    data.setOwner(navigation.getOwner());
    data.setData(toXML(navigation));
    saveData(appNode, data);
    sessionProvider.close() ;
  }
  
  public void save(ContentNavigation navigation) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Node appNode = getApplicationNode(sessionProvider, navigation.getOwner()) ;
    if(appNode == null) {
      sessionProvider.close() ;
      create(navigation);
      return;
    }
    ContentData data = new ContentData();
    data.setDataType(ContentNavigation.class.getName());    
    data.setId(navigation.getOwner()+"::"+ContentNavigation.class.getName());
    data.setOwner(navigation.getOwner());
    data.setData(toXML(navigation));
    saveData(appNode, data);
    sessionProvider.close() ;
  }
  
  public ContentNavigation get(String owner) throws Exception {
    ContentData data = getDataByOwner(owner);
    if(data == null) return null;
    return (ContentNavigation)fromXML(data.getData(), ContentNavigation.class);
  }
  
  public void remove(String owner) throws Exception { removeData(owner, NODE_NAME); }
  
  public ContentData getData(String id) throws Exception {
    String owner = id.substring(0, id.indexOf(':'));
    return getDataByOwner(owner);
  }
  
  private ContentData getDataByOwner(String owner) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Node appNode = getApplicationNode(sessionProvider, owner) ;
    if(appNode == null || appNode.hasNode(NODE_NAME) == false){
      sessionProvider.close() ;
      return null;    
    }
    Node node = appNode.getNode(NODE_NAME);
    ContentData contentData = nodeToContentData(node);
    sessionProvider.close() ;
    return contentData;
  }
  
  public void removeData(String id) throws Exception {
    removeDataByOwner(id.substring(0, id.indexOf(':')));
  }
  
  @SuppressWarnings("unused")
  public void removeData(String owner, String type) throws Exception {
    removeDataByOwner(owner); 
  } 
  
  private void removeDataByOwner(String owner) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    Node appNode = getApplicationNode(sessionProvider, owner) ;
    if(appNode.hasNode(NODE_NAME) == false) {
      sessionProvider.close() ;
      return ;
    }
    Node node = appNode.getNode(NODE_NAME);
    node.remove();
    appNode.save();
    sessionProvider.close() ;
  }
  
  private void saveData(Node parentNode, ContentData data) throws Exception {
    Node node;
    Date time = Calendar.getInstance().getTime();
    data.setModifiedDate(time);
    if(data.getCreatedDate() == null) data.setCreatedDate(time);
    if(parentNode.hasNode(NODE_NAME)) {
      node = parentNode.getNode(NODE_NAME);
      contentDataToNode(data, node);
      node.save();
    } else {
      node = parentNode.addNode(NODE_NAME, DATA_NODE_TYPE);
      contentDataToNode(data, node);
      parentNode.save();
    }
  }

  private ContentData nodeToContentData(Node node) throws Exception {
    ContentData data = new ContentData();
    if(!node.hasProperty(ID)) return null;
    data.setId(node.getProperty(ID).getString()); 
    if(!node.hasProperty(OWNER)) return null; 
    data.setOwner(node.getProperty(OWNER).getString()); 
    data.setDataType(node.getProperty(DATA_TYPE).getString());
    data.setData(node.getProperty(DATA).getString());
    data.setCreatedDate(node.getProperty(CREATED_DATE).getDate().getTime());
    data.setModifiedDate(node.getProperty(MODIFIED_DATE).getDate().getTime());
    return data;
  }
  
  private void contentDataToNode(ContentData data, Node node) throws  Exception {
    node.setProperty(ID, data.getId());
    node.setProperty(OWNER, data.getOwner());
    node.setProperty("ownerType", "user");
    node.setProperty(DATA_TYPE, data.getDataType());
    node.setProperty(DATA, data.getData());
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(data.getCreatedDate());
    node.setProperty(CREATED_DATE, calendar);
    calendar = Calendar.getInstance();
    calendar.setTime(data.getModifiedDate());
    node.setProperty(MODIFIED_DATE, calendar);    
  }
  
}