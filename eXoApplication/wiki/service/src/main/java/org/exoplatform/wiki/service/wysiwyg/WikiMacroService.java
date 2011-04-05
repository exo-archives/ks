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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.gwt.wysiwyg.client.plugin.macro.MacroDescriptor;
import org.xwiki.gwt.wysiwyg.client.plugin.macro.MacroService;
import org.xwiki.gwt.wysiwyg.client.plugin.macro.ParameterDescriptor;
import org.xwiki.gwt.wysiwyg.client.plugin.macro.ParameterType;
import org.xwiki.rendering.macro.Macro;
import org.xwiki.rendering.macro.MacroCategoryManager;
import org.xwiki.rendering.macro.MacroId;
import org.xwiki.rendering.macro.MacroManager;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxFactory;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 18, 2010  
 */
public class WikiMacroService implements MacroService {

  private static Log                log = ExoLogger.getLogger("wiki:WikiMacroService");

  /**
   * The syntax factory used to create {@link Syntax} instances from string
   * syntax identifiers.
   */
  @Requirement
  private SyntaxFactory             syntaxFactory;

  /**
   * The macro manager used to retrieve macros.
   */
  @Requirement
  private MacroManager              macroManager;

  /**
   * The macro category manager used to retrieve macro categories.
   */
  @Requirement
  private MacroCategoryManager      categoryManager;

  /**
   * The component used to translate macro descriptors into the execution
   * context language.
   */
  @Requirement
  private MacroDescriptorTranslator macroDescriptorTranslator;

  /**
   * {@inheritDoc}
   * 
   * @see MacroService#getMacroDescriptor(String, String)
   */
  public MacroDescriptor getMacroDescriptor(String macroId, String syntaxId) {
    return macroDescriptorTranslator.translate(getUntranslatedMacroDescriptor(macroId, syntaxId));
  }

  /**
   * @param macroId the macro identifier
   * @param syntaxId the syntax identifier
   * @return the untranslated macro descriptor for the specified macro
   */
  private MacroDescriptor getUntranslatedMacroDescriptor(String macroId, String syntaxId) {
    try {
      MacroId macroIdObject = new MacroId(macroId, syntaxFactory.createSyntaxFromIdString(syntaxId));
      Macro<?> macro = macroManager.getMacro(macroIdObject);
      org.xwiki.rendering.macro.descriptor.MacroDescriptor descriptor = macro.getDescriptor();

      ParameterDescriptor contentDescriptor = null;
      if (descriptor.getContentDescriptor() != null) {
        contentDescriptor = new ParameterDescriptor();
        contentDescriptor.setId("content");
        contentDescriptor.setName("Content");
        contentDescriptor.setDescription(descriptor.getContentDescriptor().getDescription());
        // Just a hack to distinguish between regular strings and large strings.
        contentDescriptor.setType(createMacroParameterType(StringBuffer.class));
        contentDescriptor.setMandatory(descriptor.getContentDescriptor().isMandatory());
      }

      // We use a linked hash map to preserve the order of the macro parameters.
      Map<String, ParameterDescriptor> parameterDescriptorMap = new LinkedHashMap<String, ParameterDescriptor>();
      for (Map.Entry<String, org.xwiki.rendering.macro.descriptor.ParameterDescriptor> entry : descriptor.getParameterDescriptorMap()
                                                                                                         .entrySet()) {
        parameterDescriptorMap.put(entry.getKey(), createMacroParameterDescriptor(entry.getValue()));
      }

      MacroDescriptor result = new MacroDescriptor();
      result.setId(macroIdObject.getId());
      result.setName(descriptor.getName());
      result.setDescription(descriptor.getDescription());
      result.setSupportingInlineMode(macro.supportsInlineMode());
      // NOTE: we should set the category also, but we need a new method in
      // MacroCategoryManager.
      result.setContentDescriptor(contentDescriptor);
      result.setParameterDescriptorMap(parameterDescriptorMap);

      return result;
    } catch (Exception e) {
      log.error("Exception while retrieving macro descriptor.", e);
      throw new RuntimeException(e.getLocalizedMessage());
    }
  }

  /**
   * Creates a {@link ParameterDescriptor} from a
   * {@link org.xwiki.rendering.macro.descriptor.ParameterDescriptor}.
   * 
   * @param descriptor a macro parameter descriptor from the rendering package
   * @return a macro parameter descriptor from the WYSIWYG package
   */
  private ParameterDescriptor createMacroParameterDescriptor(org.xwiki.rendering.macro.descriptor.ParameterDescriptor descriptor) {
    ParameterDescriptor result = new ParameterDescriptor();
    result.setId(descriptor.getId());
    result.setName(descriptor.getName());
    result.setDescription(descriptor.getDescription());
    result.setType(createMacroParameterType(descriptor.getType()));
    Object defaultValue = descriptor.getDefaultValue();
    if (defaultValue != null) {
      result.setDefaultValue(String.valueOf(defaultValue));
    }
    result.setMandatory(descriptor.isMandatory());
    return result;
  }

  /**
   * NOTE: We can't send a {@link Class} instance to the client side because GWT
   * can't serialize it so we have to convert it to a {@link ParameterType}
   * instance.
   * 
   * @param parameterClass a {@link Class} that defines the values a macro
   *          parameter can have
   * @return the parameter type associated with the given {@link Class} instance
   */
  private ParameterType createMacroParameterType(Class<?> parameterClass) {
    ParameterType parameterType = new ParameterType();
    parameterType.setName(parameterClass.getName());
    if (parameterClass.isEnum()) {
      Object[] parameterClassConstants = parameterClass.getEnumConstants();
      Map<String, String> parameterTypeConstants = new LinkedHashMap<String, String>();
      for (int i = 0; i < parameterClassConstants.length; i++) {
        String constant = String.valueOf(parameterClassConstants[i]);
        // We leave the constant unlocalized for now.
        parameterTypeConstants.put(constant, constant);
      }
      parameterType.setEnumConstants(parameterTypeConstants);
    }
    return parameterType;
  }

  /**
   * {@inheritDoc}
   * 
   * @see MacroService#getMacroDescriptors(String)
   */
  public List<MacroDescriptor> getMacroDescriptors(String syntaxId) {
    try {
      Syntax syntax = syntaxFactory.createSyntaxFromIdString(syntaxId);
      List<MacroDescriptor> descriptors = new ArrayList<MacroDescriptor>();
      for (String category : categoryManager.getMacroCategories(syntax)) {
        for (MacroId macroId : categoryManager.getMacroIds(category, syntax)) {
          if (!"table".equals(macroId.getId()) && !"thead".equals(macroId.getId())
              && !"th".equals(macroId.getId()) && !"table-row".equals(macroId.getId())
              && !"table-cell".equals(macroId.getId()) && !"section".equals(macroId.getId())
              && !"column".equals(macroId.getId())) {
            MacroDescriptor descriptor = getUntranslatedMacroDescriptor(macroId.getId(), syntaxId);
            descriptor.setCategory(category);
            descriptors.add(macroDescriptorTranslator.translate(descriptor));
          }
        }
      }

      Collections.sort(descriptors, new Comparator<MacroDescriptor>() {
        public int compare(MacroDescriptor alice, MacroDescriptor bob) {
          return alice.getName().compareTo(bob.getName());
        }
      });

      return descriptors;
    } catch (Exception e) {
      log.error(String.format("Exception while retrieving the list of macro descriptors for syntax %s.",
                              syntaxId),
                e);
      throw new RuntimeException(e.getLocalizedMessage());
    }
  }

}
