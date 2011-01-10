package org.exoplatform.wiki.service.listener;

import org.exoplatform.container.component.BaseComponentPlugin;

public abstract class PageWikiListener extends BaseComponentPlugin {

  public abstract void postAddPage(final String wikiType, final String wikiOwner, final String pageId) throws Exception;

  public abstract void postUpdatePage(final String wikiType, final String wikiOwner, final String pageId) throws Exception;

  public abstract void postDeletePage(final String wikiType, final String wikiOwner, final String pageId) throws Exception;
}
