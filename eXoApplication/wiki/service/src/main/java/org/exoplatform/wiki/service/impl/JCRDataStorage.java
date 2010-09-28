package org.exoplatform.wiki.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.service.TitleSearchResult;
import org.exoplatform.wiki.service.DataStorage;
import org.exoplatform.wiki.service.SearchData;
import org.exoplatform.wiki.service.SearchResult;

public class JCRDataStorage implements DataStorage{
  private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);
  
  public PageList<SearchResult> search(ChromatticSession session, SearchData data) throws Exception {
    List<SearchResult> resultList = new ArrayList<SearchResult>() ;
    String statement = data.getStatement() ;
    QueryManager qm = session.getJCRSession().getWorkspace().getQueryManager();
    Query q = qm.createQuery(statement, Query.SQL);
    QueryResult result = q.execute();
    RowIterator iter = result.getRows();
    while(iter.hasNext()) {
      try{resultList.add(getResult(iter.nextRow())) ;} catch(Exception e){}
    }
    return new ObjectPageList<SearchResult>(resultList, 5) ;
  }  
  
  private SearchResult getResult(Row row) throws Exception {
    String type = row.getValue("jcr:primaryType").getString() ;
    String path = row.getValue("jcr:path").getString() ;
    String excerpt = row.getValue("rep:excerpt(.)").getString() ;
    String title = (row.getValue("title")== null ? null : row.getValue("title").getString()) ;
    SearchResult result = new SearchResult(excerpt,title, path, type) ;
    return result ;
  }
  
  private TitleSearchResult getTitleSearchResult(Row row) throws Exception {
    String type = row.getValue("jcr:primaryType").getString();
    String path = row.getValue("jcr:path").getString();
    String title = (row.getValue("title") == null ? null : row.getValue("title").getString());
    TitleSearchResult result = new TitleSearchResult(title, path, type);
    return result;
  }
  
  public List<SearchResult> searchRenamedPage(ChromatticSession session, SearchData data) throws Exception {
    List<SearchResult> resultList = new ArrayList<SearchResult>() ;
    String statement = data.getStatementForRenamedPage() ;
    QueryManager qm = session.getJCRSession().getWorkspace().getQueryManager();
    Query q = qm.createQuery(statement, Query.SQL);
    QueryResult result = q.execute();
    NodeIterator iter = result.getNodes() ;
    while(iter.hasNext()) {      
      try{resultList.add(getResult(iter.nextNode())) ;} catch(Exception e){ e.printStackTrace() ;}
    }
    return resultList ;
  }
  
  private SearchResult getResult(Node node)throws Exception {
    SearchResult result = new SearchResult() ;
    result.setNodeName(node.getName()) ;
    String title = node.getNode(WikiNodeType.Definition.CONTENT).getProperty(WikiNodeType.Definition.TITLE).getString();
    String content = node.getNode(WikiNodeType.Definition.CONTENT).getProperty(WikiNodeType.Definition.TEXT).getString();
    if(content.length() > 100) content = content.substring(0, 100) + "...";
    result.setExcerpt(content) ;
    result.setTitle(title) ;
    return result ;
  }
  
  public InputStream getAttachmentAsStream(String path, ChromatticSession session) throws Exception {
    Node attContent = (Node)session.getJCRSession().getItem(path) ;
    return attContent.getProperty("jcr:data").getStream() ;    
  }
  
  public List<TitleSearchResult> searchDataByTitle(ChromatticSession session, SearchData data) throws Exception {
    List<TitleSearchResult> resultList = new ArrayList<TitleSearchResult>();
    String statement = data.getStatementForTitle();

    QueryManager qm = session.getJCRSession().getWorkspace().getQueryManager();
    Query q = qm.createQuery(statement, Query.SQL);
    QueryResult result = q.execute();
    RowIterator iter = result.getRows();

    while (iter.hasNext()) {
      try {
        resultList.add(getTitleSearchResult(iter.nextRow()));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return resultList;
  }  
  
  /*public boolean deletePage(String pagePath, String wikiPath, ChromatticSession session) throws Exception {
    try {
      Node deletePage = (Node)session.getJCRSession().getItem(pagePath) ;
      deletePage.addMixin(WikiNodeType.WIKI_REMOVED) ;
      deletePage.setProperty("removedBy", Utils.getCurrentUser()) ;
      Calendar calendar = GregorianCalendar.getInstance() ;
      deletePage.setProperty("removedDate", calendar) ;
      deletePage.setProperty("parentPath", deletePage.getParent().getPath()) ;
      deletePage.save() ;
      Node wiki = (Node)session.getJCRSession().getItem(wikiPath) ;
      Node trashNode ;
      try{
        trashNode = wiki.getNode(WikiNodeType.Definition.TRASH_NAME) ;        
      }catch(PathNotFoundException e) {
        trashNode = wiki.addNode(WikiNodeType.Definition.TRASH_NAME, WikiNodeType.WIKI_TRASH) ;
        wiki.save() ;
      }
      trashNode.getSession().getWorkspace().move(deletePage.getPath(), trashNode.getPath() + "/" + deletePage.getName()) ;    
      
      return true ;
    } catch(Exception e) {
      log.error("Could not delete page: " + pagePath) ;
      return false ;
    }   
  }  */
  
  /*public boolean renamePage(String pagePath, String newName, String newTitle, ChromatticSession session) throws Exception {
    try {
      Node currentPage = (Node)session.getJCRSession().getItem(pagePath) ;
      List<String> ids;
      if(currentPage.isNodeType("wiki:renamed")){
        Value[] values = currentPage.getProperty("oldPageIds").getValues() ;
        ids = valuesToList(values) ;
        ids.add(currentPage.getName()) ;
        currentPage.setProperty("oldPageIds", ids.toArray(new String[]{})) ;
      }else {
        ids = new ArrayList<String>() ;
        ids.add(currentPage.getName()) ;
        currentPage.addMixin("wiki:renamed") ;
        currentPage.save() ;
        currentPage.setProperty("oldPageIds", ids.toArray(new String[]{})) ;
      }           
      currentPage.save() ;
      String newPath = pagePath.substring(0, pagePath.lastIndexOf("/") + 1) + newName ;
      currentPage.getSession().getWorkspace().move(pagePath, newPath) ;
      
      
      Node newPage = (Node)session.getJCRSession().getItem(newPath) ;
      newPage.getNode(WikiNodeType.Definition.CONTENT).setProperty("title", newTitle) ;
      newPage.save() ;
      return true ;
    }catch(Exception e) {
      log.error("Can't rename page '" + pagePath + "' to '" + newName + "' ", e) ;
    }    
    return false ;
  }
  
  public void renamePageInTrash(String path, ChromatticSession session) throws Exception{
    Node oldRemoved = (Node)session.getJCRSession().getItem(path) ;
    String removedDate = oldRemoved.getProperty("removedDate").getString().replaceAll(" ", "-").replaceAll(":", "-");
    String newName = oldRemoved.getParent().getPath() + "/" + oldRemoved.getName() + "_" + removedDate ;
    session.getJCRSession().getWorkspace().move(path, newName) ;    
  }*/
  
  private List<String> valuesToList(Value[] values) throws Exception {
    List<String> list = new ArrayList<String>();
    if (values.length < 1) return list;
    String s;
    for (int i = 0; i < values.length; ++i) {
      s = values[i].getString();
      if (s != null && s.trim().length() > 0) list.add(s);
    }
    return list;
  }
}
