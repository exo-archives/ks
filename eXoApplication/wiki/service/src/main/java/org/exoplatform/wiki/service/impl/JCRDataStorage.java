package org.exoplatform.wiki.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import org.chromattic.common.IO;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.Template;
import org.exoplatform.wiki.service.DataStorage;
import org.exoplatform.wiki.service.search.ContentSearchData;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.service.search.TemplateSearchData;
import org.exoplatform.wiki.service.search.TemplateSearchResult;
import org.exoplatform.wiki.service.search.TitleSearchResult;
import org.exoplatform.wiki.utils.Utils;

public class JCRDataStorage implements DataStorage{
  private static final Log log = ExoLogger.getLogger(JCRDataStorage.class);
  
  private static final int searchSize = 10;
  
  public PageList<SearchResult> search(ChromatticSession session, ContentSearchData data) throws Exception {
    List<SearchResult> resultList = new ArrayList<SearchResult>();
    search(session, data, resultList, false);
    return new ObjectPageList<SearchResult>(resultList, searchSize);
  }
  
  private void search(ChromatticSession session,
                      ContentSearchData data,
                      List<SearchResult> resultList,
                      boolean onlyHomePages) throws Exception {
    String statement = data.getStatement(onlyHomePages);
    QueryManager qm = session.getJCRSession().getWorkspace().getQueryManager();
    Query q = qm.createQuery(statement, Query.SQL);
    QueryResult result = q.execute();
    RowIterator iter = result.getRows();
    while (iter.hasNext()) {
      SearchResult tempResult = getResult(iter.nextRow());
      // If contains, merges with the exist
      if (!isContains(resultList, tempResult)) {
        resultList.add(tempResult);
      }
    }
  }
  
  private SearchResult getResult(Row row) throws Exception {
    String type = row.getValue("jcr:primaryType").getString();

    String path = row.getValue("jcr:path").getString();
    String title = (row.getValue("title") == null ? null : row.getValue("title").getString());
    String excerpt = null;
    Calendar updateDate = GregorianCalendar.getInstance();
    Calendar createdDate = GregorianCalendar.getInstance();
    if (WikiNodeType.WIKI_ATTACHMENT_CONTENT.equals(type)) {
      // Transform to Attachment result
      type = WikiNodeType.WIKI_ATTACHMENT.toString();
      excerpt = row.getValue("rep:excerpt(.)").getString();
      path = path.substring(0, path.lastIndexOf("/"));
      if(!path.endsWith(WikiNodeType.Definition.CONTENT)){
      AttachmentImpl searchAtt = (AttachmentImpl) org.exoplatform.wiki.utils.Utils.getObject(path,
                                                                                             WikiNodeType.WIKI_ATTACHMENT);
      updateDate = searchAtt.getUpdatedDate();
      PageImpl page = searchAtt.getParentPage();
      createdDate.setTime(page.getCreatedDate());
      title = searchAtt.getTitle();
      } else {
        String pagePath = path.substring(0, path.lastIndexOf("/" + WikiNodeType.Definition.CONTENT));
        type = WikiNodeType.WIKI_PAGE_CONTENT.toString();

        PageImpl page = (PageImpl) org.exoplatform.wiki.utils.Utils.getObject(pagePath,
                                                                              WikiNodeType.WIKI_PAGE);
        title = page.getTitle();
        updateDate.setTime(page.getUpdatedDate());
        createdDate.setTime(page.getCreatedDate());
      }
    } else if (WikiNodeType.WIKI_ATTACHMENT.equals(type)) {
      AttachmentImpl searchAtt = (AttachmentImpl) org.exoplatform.wiki.utils.Utils.getObject(path,
                                                                                             WikiNodeType.WIKI_ATTACHMENT);
      updateDate = searchAtt.getUpdatedDate();
      PageImpl page = searchAtt.getParentPage();
      createdDate.setTime(page.getCreatedDate());
    } 
    SearchResult result = new SearchResult(excerpt, title, path, type, updateDate, createdDate);
    return result;
  }
  
  private TitleSearchResult getTitleSearchResult(Row row) throws Exception {
    String type = row.getValue("jcr:primaryType").getString();
    String path = row.getValue("jcr:path").getString();
    String fullTitle = (row.getValue(WikiNodeType.Definition.FILE_TYPE) == null ? row.getValue(WikiNodeType.Definition.TITLE)
                                                                                 .getString()
                                                                           : row.getValue(WikiNodeType.Definition.TITLE)
                                                                                .getString()
                                                                                .concat(row.getValue(WikiNodeType.Definition.FILE_TYPE)
                                                                                           .getString()));
    TitleSearchResult result = new TitleSearchResult(fullTitle, path, type);
    return result;
  }
  
  public List<SearchResult> searchRenamedPage(ChromatticSession session, ContentSearchData data) throws Exception {
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
    String title = node.getProperty(WikiNodeType.Definition.TITLE).getString();
    InputStream data = node.getNode(WikiNodeType.Definition.CONTENT).getNode("jcr:content").getProperty("jcr:data").getStream();
    byte[] bytes = IO.getBytes(data);
    String content = new String(bytes, "UTF-8");
    if(content.length() > 100) content = content.substring(0, 100) + "...";
    result.setExcerpt(content) ;
    result.setTitle(title) ;
    return result ;
  }
  
  public InputStream getAttachmentAsStream(String path, ChromatticSession session) throws Exception {
    Node attContent = (Node)session.getJCRSession().getItem(path) ;
    return attContent.getProperty("jcr:data").getStream() ;    
  }
  
  public List<TitleSearchResult> searchDataByTitle(ChromatticSession session, ContentSearchData data) throws Exception {
    List<TitleSearchResult> resultList = new ArrayList<TitleSearchResult>();
    searchDataByTitle(session, data, resultList, true);
    searchDataByTitle(session, data, resultList, false);
    return resultList;
  }  
  
  private void searchDataByTitle(ChromatticSession session,
                                 ContentSearchData data,
                                 List<TitleSearchResult> resultList,
                                 boolean onlyHomePages) throws Exception {
    String statement = data.getStatementForTitle(onlyHomePages);
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
  }
  
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
 
  private boolean isContains(List<SearchResult> list, SearchResult result) throws Exception {
    AttachmentImpl att = null;
    PageImpl page = null;
    if (WikiNodeType.WIKI_ATTACHMENT.equals(result.getType())) {
      att = (AttachmentImpl) Utils.getObject(result.getPath(), WikiNodeType.WIKI_ATTACHMENT);
    } else if (WikiNodeType.WIKI_ATTACHMENT_CONTENT.equals(result.getType())) {
      String attPath = result.getPath().substring(0, result.getPath().lastIndexOf("/"));
      att = (AttachmentImpl) Utils.getObject(attPath, WikiNodeType.WIKI_ATTACHMENT);
    } else if(WikiNodeType.WIKI_PAGE.equals(result.getType()) || WikiNodeType.WIKI_HOME.equals(result.getType())){
      page = (PageImpl) Utils.getObject(result.getPath(), WikiNodeType.WIKI_PAGE);
    }
    if (att != null || page != null) {
      for (int i = 0; i < list.size(); i++) {
        SearchResult child = list.get(i);
        if (WikiNodeType.WIKI_ATTACHMENT.equals(child.getType()) || WikiNodeType.WIKI_PAGE_CONTENT.equals(child.getType())) {
          AttachmentImpl tempAtt = (AttachmentImpl) Utils.getObject(child.getPath(),
                                                                    WikiNodeType.WIKI_ATTACHMENT);
          if (att != null && att.equals(tempAtt)) {
            // Merge data
            if (child.getExcerpt()==null && result.getExcerpt()!=null ){
              child.setExcerpt(result.getExcerpt());
            }
            return true;
          }
          if (page != null && page.getName().equals(tempAtt.getParentPage())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public List<TemplateSearchResult> searchTemplate(ChromatticSession session,
                                                       TemplateSearchData data) throws Exception {
    // TODO Auto-generated method stub
    List<TemplateSearchResult> resultList = new ArrayList<TemplateSearchResult>();
    String statement = data.getStatement();
    QueryManager qm = session.getJCRSession().getWorkspace().getQueryManager();
    Query q = qm.createQuery(statement, Query.SQL);
    QueryResult result = q.execute();
    RowIterator iter = result.getRows();
    while (iter.hasNext()) {
      TemplateSearchResult tempResult = getTemplateResult(iter.nextRow());
      resultList.add(tempResult);
    }
   return resultList;
  }

  private TemplateSearchResult getTemplateResult(Row row) throws Exception {
    String type = row.getValue("jcr:primaryType").getString();

    String path = row.getValue("jcr:path").getString();
    String title = (row.getValue("title") == null ? null : row.getValue("title").getString());
    
    Template template = (Template) org.exoplatform.wiki.utils.Utils.getObject(path,
                                                                              WikiNodeType.WIKI_PAGE);
    String description = template.getDescription();
    TemplateSearchResult result = new TemplateSearchResult(template.getName(),
                                                           title,
                                                           path,
                                                           type,
                                                           null,
                                                           null,
                                                           description);
    return result;
  }
}
