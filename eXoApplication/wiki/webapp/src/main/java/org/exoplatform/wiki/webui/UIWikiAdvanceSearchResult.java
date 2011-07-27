/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.webui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.LinkEntry;
import org.exoplatform.wiki.mow.core.api.wiki.LinkRegistry;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.utils.Utils;
import org.exoplatform.wiki.webui.core.UIAdvancePageIterator;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(lifecycle = Lifecycle.class, template = "app:/templates/wiki/webui/UIWikiAdvanceSearchResult.gtmpl")
public class UIWikiAdvanceSearchResult extends UIContainer {

  private String keyword;
  
  LinkRegistry registry = null;

  public UIWikiAdvanceSearchResult() throws Exception {
    addChild(UIAdvancePageIterator.class, null, "SearchResultPageIterator");
  }

  public void setResult(PageList<SearchResult> results) throws Exception {
    UIAdvancePageIterator pageIterator = this.getChild(UIAdvancePageIterator.class);
    pageIterator.setPageList(results);
    pageIterator.getPageList().getPage(1);
  }

  public PageList<SearchResult> getResults() {
    UIAdvancePageIterator pageIterator = this.getChild(UIAdvancePageIterator.class);
    return pageIterator.getPageList();
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  private String getKeyword() {
    return keyword;
  }

  private String getDateFormat(Calendar cal) throws Exception {
    Locale currentLocale = Util.getPortalRequestContext().getLocale();
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, currentLocale);
    return df.format(cal.getTime());
  }
  
  private Wiki getWiki(SearchResult result) throws Exception {
    Wiki searchWiki = null;
    try {
      if (WikiNodeType.WIKI_PAGE_CONTENT.equals(result.getType()) || WikiNodeType.WIKI_ATTACHMENT.equals(result.getType())) {
        AttachmentImpl searchContent = (AttachmentImpl) org.exoplatform.wiki.utils.Utils.getObject(result.getPath(), WikiNodeType.WIKI_ATTACHMENT);
        searchWiki = searchContent.getParentPage().getWiki();
      } else if (WikiNodeType.WIKI_PAGE.equals(result.getType()) || WikiNodeType.WIKI_HOME.equals(result.getType())) {
        PageImpl page = (PageImpl) org.exoplatform.wiki.utils.Utils.getObject(result.getPath(), WikiNodeType.WIKI_PAGE);
        searchWiki = page.getWiki();
      }
    } catch (Exception e) {
    }
    return searchWiki;
  }

  private String getPageSearchName(Wiki wiki, String pageTitle) throws Exception {
    if (pageTitle.indexOf(keyword) >= 0) return "";
    if(registry == null) {
      registry = ((WikiImpl) wiki).getLinkRegistry();
    }
    Map<String, LinkEntry> linkEntries = registry.getLinkEntries();
    String titleBefore, titleAfter;
    List<LinkEntry> linkEntrys = new ArrayList<LinkEntry>();
    List<String> alias = new ArrayList<String>();
    for (LinkEntry linkEntry : linkEntries.values()) {
      if (alias.contains(linkEntry.getAlias())) continue;
      while (true) {
        alias.add(linkEntry.getAlias());
        titleAfter = linkEntry.getTitle();
        linkEntrys.add(linkEntry);
        linkEntry = linkEntry.getNewLink();
        if(linkEntry == null) break;
        titleBefore = linkEntry.getTitle();
        if(!CommonUtils.isEmpty(titleBefore) && 
            titleBefore.equals(pageTitle) && titleAfter.equals(titleBefore)) {
          for (LinkEntry entry : linkEntrys) {
            if (entry.getTitle().indexOf(keyword) >= 0) {
              return entry.getTitle();
            }
          }
          break;
        }
        if (CommonUtils.isEmpty(titleBefore) || titleAfter.equals(titleBefore)) {
          linkEntrys.clear();
          break;
        }
      }
    }
    return "";
  }

  private String getWikiNodeUri(Wiki wiki) throws Exception {
    String wikiType = wiki.getType();
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    StringBuilder sb = new StringBuilder(portalRequestContext.getPortalURI());
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedUserNode().getURI();
    sb.append(pageNodeSelected);
    if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(wikiType)) {
      sb.append("/");
      sb.append(wikiType);
      sb.append("/");
      sb.append(Utils.validateWikiOwner(wikiType, wiki.getOwner()));
    }
    return sb.toString();
  }
}

