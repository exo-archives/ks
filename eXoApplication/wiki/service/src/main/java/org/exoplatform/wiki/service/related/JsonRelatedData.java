package org.exoplatform.wiki.service.related;

public class JsonRelatedData {
  private String name;
  private String title;
  /**
   * string for making url
   */
  private String identity;
  
  public JsonRelatedData(String name, String title, String identity) {
    this.name = name;
    this.title = title;
    this.identity = identity;
  }
  
  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return the identity
   */
  public String getIdentity() {
    return identity;
  }

  /**
   * @param identity the identity to set
   */
  public void setIdentity(String identity) {
    this.identity = identity;
  }
   
  
}
