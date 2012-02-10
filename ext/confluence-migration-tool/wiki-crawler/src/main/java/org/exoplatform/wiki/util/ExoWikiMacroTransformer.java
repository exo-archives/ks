/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.wiki.util;

/**
 * Utility class to fix some macro content from confluence to eXoWiki format
 */
public final class ExoWikiMacroTransformer {

  private ExoWikiMacroTransformer() {
  }

  /**
   *
   * @param macroContent  text to be changed
   * @return content compatible with eXo Wiki features support
   */
  public static String transformMacroContent(String macroContent) {
    if (!macroContent.startsWith("{iframe")) {
      return macroContent;
    }

    String[] macros = macroContent.substring(1, macroContent.length() - 1).split(":");
    String head = macros[0];
    StringBuilder result = new StringBuilder("{").append(head);
    if (macros.length > 1) {
      String[] macros2 = macros[1].split("\\|");
      boolean start = true;
      for (String param : macros2) {
        if (start) {
          result.append(":");
        } else {
          result.append("|");
        }
        start = false;
        result.append(fixParam(head, param));
      }
    }
    result.append("}");
    return result.toString();
  }

  protected static String fixParam(String head, String param) {
    if ("iframe".equals(head) && param.startsWith("src=") && !param.startsWith("src=\"")) {
      return "src=\"" + param.substring(4) + "\"";
    }
    return param;
  }
}
