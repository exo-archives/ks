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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extract macro list from Confluence Content
 */
public final class MacroExtractor {

  private MacroExtractor() {
  }

  public static Map<String, Integer> extractMacro(Map<String, Integer> macrosMap, String body) {
    Map<String, Integer> foundMacros = new HashMap<String, Integer>();

    String newbody = cleanupBody(body);

    String macros[] = newbody.split("\\{");
    String previousPiece = ""; // To check for escaped macros
    for (String piece : macros) {
      if (piece.contains("}") && !previousPiece.endsWith("\\")) {
        // get content before }
        String submacros[] = piece.split("}");
        String extractedMacro = submacros.length > 0 ? submacros[0] : "";

        // get content before :
        submacros = extractedMacro.split(":");
        extractedMacro = submacros.length > 0 ? submacros[0] : "";
        extractedMacro = extractedMacro.trim();
        extractedMacro = extractedMacro.replaceAll("\n", "");
        extractedMacro = extractedMacro.replaceAll("\r", "");

        // Uniq map
        MacroMap.addMacro(macrosMap, extractedMacro);
        MacroMap.addMacro(foundMacros, extractedMacro);
      }
      previousPiece = piece;
    }
    return foundMacros;
  }

  private static String cleanupBody(String body) {
    // Remove {code}content{code}
    String newbody = body;

    //Cleanup {{noformat text}}
    newbody = newbody.replaceAll("\\{\\{.*\\}\\}", "");


    newbody = removeBlocks("code", newbody);
    newbody = removeBlocks("csv", newbody);
    newbody = removeBlocks("noformat", newbody);
    newbody = removeBlocks("style", newbody);
    return newbody;
  }

  public static Set<String> extractMacroWithParams(String body) {
    Set<String> foundMacros = new HashSet<String>();

    // Remove {code}content{code}
    String newbody = cleanupBody(body);

    String macros[] = newbody.split("\\{");
    String previousPiece = ""; // To check for escaped macros
    for (String piece : macros) {
      if (piece.contains("}") && !previousPiece.endsWith("\\")) {
        // get content before }
        String submacros[] = piece.split("}");
        String extractedMacro = submacros.length > 0 ? submacros[0] : "";

        // get content before :
        extractedMacro = extractedMacro.trim();
        extractedMacro = extractedMacro.replaceAll("\n", "");
        extractedMacro = extractedMacro.replaceAll("\r", "");

        foundMacros.add(extractedMacro);
      }
      previousPiece = piece;
    }
    return foundMacros;
  }

  public static String removeBlocks(String tag, String result) {
    String[] split = result.split("\\{" + tag);
    boolean matching = true;
    boolean starting = true;
    StringBuilder content = new StringBuilder();
    for (String piece : split) {
      if (matching) {
        if (!starting) {
          // remove content until }
          piece = piece.substring(piece.indexOf("}") + 1);
        }
        content.append(piece);
      } else {
        //leave one instance of the tag
        content.append("{").append(tag).append("}");
      }
      matching = !matching;
      starting = false;
    }
    return content.toString();
  }
}
