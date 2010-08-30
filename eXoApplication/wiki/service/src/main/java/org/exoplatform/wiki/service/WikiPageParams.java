package org.exoplatform.wiki.service;

import java.util.HashMap;
import java.util.Map;

public class WikiPageParams {

  public static final String  WIKI_HOME  = "WikiHome";

  private String              type;

  private String              owner;

  private String              pageId;

  private String              attachmentName;

  private Map<String, String[]> parameters = new HashMap<String, String[]>();

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getOwner() {
    return owner;
  }

  public void setPageId(String pageId) {
    this.pageId = pageId;
  }

  public String getPageId() {
    return pageId;
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public void setAttachmentName(String attachmentName) {
    this.attachmentName = attachmentName;
  }

  public void setParameter(String key, String[] values) {
    parameters.put(key, values);
  }

  public String getParameter(String name) {
    String[] values = parameters.get(name);
    return (values == null) ? null : values[0];
  }

}
