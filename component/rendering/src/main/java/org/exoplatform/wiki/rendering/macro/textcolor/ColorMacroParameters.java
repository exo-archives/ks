package org.exoplatform.wiki.rendering.macro.textcolor;

import org.apache.commons.lang.StringUtils;
import org.xwiki.properties.annotation.PropertyDescription;

public class ColorMacroParameters {
  /**
   * name of color. It can be real name such as 'red' or hex code like '#ff0000'  
   */
  private String name = StringUtils.EMPTY;

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  @PropertyDescription("name of color ('red', '#ff0000', etc)")
  public void setName(String name) {
    this.name = name;
  }
  
  
  
}
