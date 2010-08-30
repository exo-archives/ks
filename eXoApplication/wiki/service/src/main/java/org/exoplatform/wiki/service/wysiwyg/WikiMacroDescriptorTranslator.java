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
package org.exoplatform.wiki.service.wysiwyg;

import java.util.Map.Entry;

import org.xwiki.gwt.wysiwyg.client.plugin.macro.MacroDescriptor;
import org.xwiki.gwt.wysiwyg.client.plugin.macro.ParameterDescriptor;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 18, 2010  
 */
public class WikiMacroDescriptorTranslator implements MacroDescriptorTranslator {
  /**
   * The name field.
   */
  private static final String FIELD_NAME        = ".name";

  /**
   * The description field.
   */
  private static final String FIELD_DESCRIPTION = ".description";

  /**
   * The prefix used for all the translation keys.
   */
  private static final String KEY_RENDERING     = "rendering";

  /**
   * {@inheritDoc}
   * 
   * @see MacroDescriptorTranslator#translate(MacroDescriptor)
   */
  public MacroDescriptor translate(MacroDescriptor macroDescriptor) {

    String macroKey = KEY_RENDERING + ".macro." + macroDescriptor.getId();
    macroDescriptor.setName(translate(macroKey + FIELD_NAME, macroDescriptor.getName()));
    macroDescriptor.setDescription(translate(macroKey + FIELD_DESCRIPTION,
                                             macroDescriptor.getDescription()));

    String macroCategoryKey = KEY_RENDERING + ".macroCategory." + macroDescriptor.getCategory();
    macroDescriptor.setCategory(translate(macroCategoryKey, macroDescriptor.getCategory()));

    ParameterDescriptor contentDescriptor = macroDescriptor.getContentDescriptor();
    if (contentDescriptor != null) {
      contentDescriptor.setName(translate(KEY_RENDERING + ".macroContent",
                                          contentDescriptor.getName()));
      contentDescriptor.setDescription(translate(macroKey + ".content.description",
                                                 contentDescriptor.getDescription()));
    }

    for (ParameterDescriptor paramDescriptor : macroDescriptor.getParameterDescriptorMap().values()) {
      String paramKey = macroKey + ".parameter." + paramDescriptor.getId();
      paramDescriptor.setName(translate(paramKey + FIELD_NAME, paramDescriptor.getName()));
      paramDescriptor.setDescription(translate(paramKey + FIELD_DESCRIPTION,
                                               paramDescriptor.getDescription()));

      if (paramDescriptor.getType().isEnum()) {
        for (Entry<String, String> entry : paramDescriptor.getType().getEnumConstants().entrySet()) {
          String paramValueKey = paramKey + ".value." + entry.getKey();
          entry.setValue(translate(paramValueKey, entry.getValue()));
        }
      }
    }

    return macroDescriptor;
  }

  /**
   * Looks up the given translation key returning the specified default value if
   * no value is found.
   * 
   * @param key the translation key to look up
   * @param defaultValue the value to return when there's no value associated
   *          with the given key
   * @return the value associated with the given key if there is one, otherwise
   *         the default value
   */
  private String translate(String key, String defaultValue) {
    return defaultValue;
  }

}
