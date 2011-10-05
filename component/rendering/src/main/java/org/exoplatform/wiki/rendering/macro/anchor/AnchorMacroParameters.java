package org.exoplatform.wiki.rendering.macro.anchor;

import org.xwiki.properties.annotation.PropertyDescription;

public class AnchorMacroParameters {
  
  private String name;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  @PropertyDescription("name of anchor")
  public void setName(String name) {
    this.name = name;
  }
  
  
  
}
