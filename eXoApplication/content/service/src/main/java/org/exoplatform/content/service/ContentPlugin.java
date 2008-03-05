package org.exoplatform.content.service;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.content.model.ContentItem;
import org.exoplatform.content.model.ContentNode;

public abstract class ContentPlugin extends BaseComponentPlugin {
  
	protected String type;	
  
  public String getType() { return type; }
  public void setType(String type) { this.type = type; }
	
  public abstract <T extends ContentItem>  PageList loadContentMeta(ContentNode node) throws Exception;

}
