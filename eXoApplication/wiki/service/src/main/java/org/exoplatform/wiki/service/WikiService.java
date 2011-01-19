/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.service;

import java.io.InputStream;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.listener.PageWikiListener;

/**
 * Created by The eXo Platform SARL.
 * <p>
 * WikiService is interface provide functions for processing database
 * with wikis and pages include: add, edit, remove and searching data
 * 
 * @author  exoplatform
 * @since   Mar 04, 2010
 */
public interface WikiService {
	
	public Page createPage(String wikiType, String wikiOwner, String title, String parentId) throws Exception ;	
	public void createDraftNewPage(String draftNewPageId) throws Exception ;
	public boolean deletePage(String wikiType, String wikiOwner, String pageId) throws Exception ;
	public void deleteDraftNewPage(String draftNewPageId) throws Exception ;
	public boolean renamePage(String wikiType, String wikiOwner, String pageName, String newName, String newTitle) throws Exception ;
	public boolean movePage(WikiPageParams currentLocationParams, WikiPageParams newLocationParams) throws Exception ;
	public List<PermissionEntry> getWikiPermission(String wikiType, String wikiOwner) throws Exception ;
	public void setWikiPermission(String wikiType, String wikiOwner, List<PermissionEntry> permissionEntries) throws Exception ;
	
	public Page getPageById(String wikiType, String wikiOwner, String pageId) throws Exception ;
	public Page getRelatedPage(String wikiType, String wikiOwner, String pageId) throws Exception;
	public Page getExsitedOrNewDraftPageById(String wikiType, String wikiOwner, String pageId) throws Exception ;
	public Page getPageByUUID(String uuid) throws Exception ;	
	
	public PageList<ContentImpl> searchContent(SearchData data) throws Exception ;
	public List<BreadcrumbData> getBreadcumb(String wikiType, String wikiOwner, String pageId) throws Exception ;
	public WikiPageParams getWikiPageParams(BreadcrumbData data) throws Exception;
	public PageList<SearchResult> search(SearchData data) throws Exception ;
	public List<SearchResult> searchRenamedPage(String wikiType, String wikiOwner, String pageId) throws Exception  ;
	public List<TitleSearchResult> searchDataByTitle(SearchData data) throws Exception;
	public Object findByPath(String path, String objectNodeType) throws Exception  ;
	public String getDefaultWikiSyntaxId();
	public String getPageTitleOfAttachment(String path) throws Exception ;
	public InputStream getAttachmentAsStream(String path) throws Exception ;
	public PageImpl getHelpSyntaxPage(String syntaxId) throws Exception;
	public boolean isExisting(String wikiType, String wikiOwner, String pageId) throws Exception ;
	
	/**
	 * register a {@link PageWikiListener}
	 * @param listener
	 */
	public void addComponentPlugin(ComponentPlugin plugin);
	/**
	 * @return list of {@link PageWikiListener}
	 */
	public List<PageWikiListener> getPageListeners();

}


