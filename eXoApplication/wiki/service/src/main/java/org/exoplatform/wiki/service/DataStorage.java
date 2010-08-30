package org.exoplatform.wiki.service;

import java.io.InputStream;
import java.util.List;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.PageList;

public interface DataStorage {
  public PageList<SearchResult> search(ChromatticSession session, SearchData data) throws Exception ;
  public InputStream getAttachmentAsStream(String path, ChromatticSession session) throws Exception ;
  //public boolean renamePage(String pagePath, String newName, String newTitle, ChromatticSession session) throws Exception ;
  public List<SearchResult> searchRenamedPage(ChromatticSession session, SearchData data) throws Exception ;
  //public void renamePageInTrash(String path, ChromatticSession session) throws Exception ;
  
}
