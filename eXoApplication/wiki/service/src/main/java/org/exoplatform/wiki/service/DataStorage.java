package org.exoplatform.wiki.service;

import java.io.InputStream;
import java.util.List;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.wiki.service.search.WikiSearchData;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.service.search.TemplateSearchData;
import org.exoplatform.wiki.service.search.TemplateSearchResult;

public interface DataStorage {
  public PageList<SearchResult> search(ChromatticSession session, WikiSearchData data) throws Exception ;
  public InputStream getAttachmentAsStream(String path, ChromatticSession session) throws Exception ;
  //public boolean renamePage(String pagePath, String newName, String newTitle, ChromatticSession session) throws Exception ;
  public List<SearchResult> searchRenamedPage(ChromatticSession session, WikiSearchData data) throws Exception ;
  //public void renamePageInTrash(String path, ChromatticSession session) throws Exception ;
  public List<TemplateSearchResult> searchTemplate(ChromatticSession session, TemplateSearchData data) throws Exception ;
}
