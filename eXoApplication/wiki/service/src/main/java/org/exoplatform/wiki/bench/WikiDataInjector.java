/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.bench;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiService;

/**
 * <p>
 * Plugin for injecting Wiki data.
 * </p>
 * Created by The eXo Platform SAS
 * @Author : <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Jul 21, 2011  
 */
public class WikiDataInjector extends DataInjector {
  
  private static Log         log             = ExoLogger.getLogger(WikiDataInjector.class);
  
  private WikiService wikiService;
  
  /**
   * maximum number of children of a page
   */
  private int maxChildren = 3;
  
  /**
   * attachment size in KB
   */
  private int maxAttachmentSize = 1; // kb
  
  /**
   * maximum depths
   */
  private int maxDepths = 2;
  
  /**
   * setting of user is randomize or not.
   */
  private boolean randomize = false;
  
  private int maxTotalPages = 10000;
  
  private String wikiType;
  
  private String wikiOwner;
  
  private String titleOfMarkedPage = "markedpage123456789";
  
  private int numberOfPages;
  
  private Resource attachment;
  
  private Random       rand = new Random();
  
  private Stack<String> pagesStack = new Stack<String>();
  
  public WikiDataInjector(WikiService wikiService,  InitParams params) {
    this.wikiService = wikiService;
    initParams(params);
  }
  
  public void initParams(InitParams params) {
    try {
      ValueParam param = params.getValueParam("mC");
      if (param != null)
        maxChildren = Integer.parseInt(param.getValue());
      param = params.getValueParam("mA");
      if (param != null)
        maxAttachmentSize = Integer.parseInt(param.getValue());
      param = params.getValueParam("mD");
      if (param != null) 
        maxDepths = Integer.parseInt(param.getValue());
      param = params.getValueParam("rand");
      if (param != null)
        randomize = Boolean.parseBoolean(param.getValue());
      param = params.getValueParam("wo");
      if (param != null)
        wikiOwner = param.getValue();
      param = params.getValueParam("wt");
      if (param != null)
        wikiType = param.getValue().toLowerCase();
      param = params.getValueParam("mP");
      if (param != null)
        maxTotalPages = Integer.parseInt(param.getValue());;
    } catch (Exception e) {
      throw new RuntimeException("Could not initialize", e);
    }
  }

  private int maxDepths() {
    return (randomize) ? (rand.nextInt(maxDepths) + 1) : maxDepths;
  }
  
  private int maxChildren() {
    return (randomize) ? (rand.nextInt(maxChildren) + 1) : maxChildren;
  }
  
  private int maxAttachmentSize() {
    return (randomize) ? (rand.nextInt(maxAttachmentSize) + 1) : maxAttachmentSize;
  }
  
  private void createPage(PageImpl parent, int currentDepth) throws Exception {
    if (numberOfPages >= maxTotalPages) {
      // break because of exceeding allowed number.
      return;
    }
    String title = randomWords(5) + " " + IdGenerator.generate();
    String content = randomParagraphs(5);
    int maxDepth = maxDepths();
    PageImpl page = (PageImpl) wikiService.createPage(wikiType, wikiOwner, title, parent.getName());
    pagesStack.add(TitleResolver.getId(title, true));
    numberOfPages++;
    log.info(String.format("%1$" + (currentDepth*4) + "s Create page %2$s(th) in depth %3$s .......", " ", numberOfPages, currentDepth));
    page.getContent().setText(content);
    page.setPagePermission(defaultPermission);
    page.createAttachment("att" + IdGenerator.generate() + ".txt", createAttachmentResource());
    if (currentDepth < maxDepth) {
      // add children
      int childDepth = currentDepth +1;
      int numberOfChildren = maxChildren();
      for (int i = 0; i < numberOfChildren; i++) {
        createPage(page, childDepth);
      }
    }
  }
  
  private static HashMap<String, String[]> defaultPermission = new HashMap<String, String[]>();
  static {
    String[] permissions = new String[] {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.REMOVE, PermissionType.SET_PROPERTY};
    defaultPermission.put("any", permissions);
  }
  
  private Resource createAttachmentResource() {
    if (attachment == null) {
      attachment = Resource.createPlainText(createTextResource(maxAttachmentSize()));
    }
    return attachment;
  }
  
  @Override
  public boolean isInitialized() {
    // allow multi-injecting
    return false;
  }
  
  @Override
  public void inject() throws Exception {
    int pagesPerDepth = maxChildren();
    numberOfPages = 0;
    
    for (int i = 0; i < pagesPerDepth; i++) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        createPage((PageImpl) wikiService.getPageById(wikiType, wikiOwner, null), 1);
      } finally {
        RequestLifeCycle.end();
      }
    }

    if (log.isInfoEnabled())
      log.info(String.format("%s pages have been created", numberOfPages));
  }

  @Override
  public void reject() throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      while (!pagesStack.isEmpty()) {
        String pageId = pagesStack.pop();
        wikiService.deletePage(wikiType, wikiOwner, pageId);
      }
    } finally {
      RequestLifeCycle.end();
    }
  }

  @Override
  public Log getLog() {
    return log;
  }
  
  class TimeCounter {
    
    private long elapse = 0;
    
    private BigDecimal counter = new BigDecimal(0);
    
    private long startTime = 0;
    
    private boolean isStarted = false;
    
    public void start() {
      if (isStarted) 
        throw new IllegalStateException("The counter has been started!");
      isStarted = true;
      startTime = System.currentTimeMillis();
      counter = new BigDecimal(0);
    }
    
    public void pause() {
      if (!isStarted) 
        throw new IllegalStateException("The counter has not been started yet!");
      isStarted = false;
      long current = System.currentTimeMillis();
      counter.add(new BigDecimal(current - startTime));
    }
    
    public void resume() {
      if (isStarted) 
        throw new IllegalStateException("The counter has been started!");
      isStarted = true;
      startTime = System.currentTimeMillis();
    }
    
    public void stop() {
      if (!isStarted) 
        throw new IllegalStateException("The counter has not been started yet!");
      isStarted = false;
      long current = System.currentTimeMillis();
      counter.add(new BigDecimal(current - startTime));
      elapse = counter.longValue();
      counter = new BigDecimal(0);
      startTime = 0;
    }
    
    public long elapse() {
      return elapse;
    }
    
  }

}

