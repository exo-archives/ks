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
package org.exoplatform.wiki.rendering.render.confluence;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.HeaderLevel;
import org.xwiki.rendering.listener.ListType;
import org.xwiki.rendering.listener.MetaData;
import org.xwiki.rendering.listener.chaining.BlockStateChainingListener;
import org.xwiki.rendering.listener.chaining.ListenerChain;
import org.xwiki.rendering.listener.chaining.StackableChainingListener;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.renderer.AbstractChainingPrintRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.VoidWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.reference.ResourceReferenceSerializer;
import org.xwiki.rendering.transformation.icon.IconTransformationConfiguration;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 2, 2010  
 */

/**
 * Convert listener events to Confluence Syntax 1.0 output.
 */
public class ConfluenceSyntaxChainingRenderer extends AbstractChainingPrintRenderer implements StackableChainingListener {

  private ConfluenceSyntaxLinkRenderer linkRenderer;

  private ConfluenceSyntaxImageRenderer imageRenderer;
  
  private ConfluenceSyntaxIconRenderer  iconRenderer;

  private ConfluenceSyntaxMacroRenderer macroPrinter;

  private ResourceReferenceSerializer linkReferenceSerializer;

  // Custom States

  private boolean isFirstElementRendered = false;

  private StringBuffer listStyle = new StringBuffer();

  private Map<String, String> previousFormatParameters;
  
  private IconTransformationConfiguration iconTransformationConfiguration;

  public ConfluenceSyntaxChainingRenderer(ListenerChain listenerChain,
                                          ResourceReferenceSerializer linkReferenceSerializer,
                                          IconTransformationConfiguration iconTransformationConfiguration) {

    setListenerChain(listenerChain);

    this.linkReferenceSerializer = linkReferenceSerializer;
    this.iconTransformationConfiguration = iconTransformationConfiguration;
    this.linkRenderer = new ConfluenceSyntaxLinkRenderer(getConfluenceSyntaxListenerChain(), linkReferenceSerializer);
    this.imageRenderer = new ConfluenceSyntaxImageRenderer();
    this.iconRenderer = new ConfluenceSyntaxIconRenderer(iconTransformationConfiguration);
    this.macroPrinter = new ConfluenceSyntaxMacroRenderer();
  }

  // State

  private BlockStateChainingListener getBlockState() {
    return getConfluenceSyntaxListenerChain().getBlockStateChainingListener();
  }

  /**
   * {@inheritDoc}
   * 
   * @see StackableChainingListener#createChainingListenerInstance()
   */
  public StackableChainingListener createChainingListenerInstance() {
    ConfluenceSyntaxChainingRenderer renderer = new ConfluenceSyntaxChainingRenderer(getListenerChain(),
                                                                                     this.linkReferenceSerializer,
                                                                                     this.iconTransformationConfiguration);
    renderer.setPrinter(getPrinter());
    return renderer;
  }

  private ConfluenceSyntaxListenerChain getConfluenceSyntaxListenerChain() {
    return (ConfluenceSyntaxListenerChain) getListenerChain();
  }

  private ConfluenceSyntaxLinkRenderer getLinkRenderer() {
    return this.linkRenderer;
  }

  private ConfluenceSyntaxImageRenderer getImageRenderer() {
    return this.imageRenderer;
  }
  
  private ConfluenceSyntaxIconRenderer getIconRenderer() {
    return this.iconRenderer;
  }

  private ConfluenceSyntaxMacroRenderer getMacroPrinter() {
    return this.macroPrinter;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#beginGroup(Map)
   */
  @Override
  public void beginGroup(Map<String, String> parameters) {
    if (!getBlockState().isInLine()) {
      printEmptyLine();
    }

    if (parameters.size() > 0) {
      printBeginParameters(parameters, true);
    }

    // Create a new listener stack in order to preserve current states, to
    // handle the group.
    getListenerChain().pushAllStackableListeners();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endDocument(java.util.Map)
   */
  @Override
  public void endDocument(MetaData metaData) {
    // Ensure that all data in the escape printer have been flushed
    getConfluencePrinter().flush();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endGroup(Map)
   */
  @Override
  public void endGroup(Map<String, String> parameters) {

    // Restore previous listeners that were stacked
    getListenerChain().popAllStackableListeners();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#beginLink(org.xwiki.rendering.listener.Link,
   *      boolean, java.util.Map)
   */
  @Override
  public void beginLink(ResourceReference link, boolean isFreeStandingURI, Map<String, String> parameters) {
    // Flush test content before the link.
    // TODO: improve the block state renderer to be able to make the difference between what is bufferized
    // before the link and what in the label link
    getConfluencePrinter().setBeforeLink(true);
    // escape open link syntax when before a link
    if (getLinkRenderer().forceFullSyntax(getConfluencePrinter(), isFreeStandingURI, parameters)
        && getConfluencePrinter().getBuffer().length() > 0
        && getConfluencePrinter().getBuffer()
                                 .charAt(getConfluencePrinter().getBuffer().length() - 1) == '[') {
      getConfluencePrinter().setEscapeLastChar(true);
    }
    getConfluencePrinter().flush();
    getConfluencePrinter().setBeforeLink(false);

    int linkDepth = getBlockState().getLinkDepth();

    // If we are at a depth of 2 or greater it means we're in a link inside a link and in this case we
    // shouldn't output the nested link as a link unless it's a free standing link.
    if (linkDepth < 2) {
      getLinkRenderer().beginRenderLink(getConfluencePrinter(), link, isFreeStandingURI, parameters);

      ConfluenceSyntaxEscapeWikiPrinter linkLabelPrinter = new ConfluenceSyntaxEscapeWikiPrinter(new DefaultWikiPrinter(),
                                                                                                 getConfluenceSyntaxListenerChain());

      // Make sure the escape handler knows there is already characters before
      linkLabelPrinter.setOnNewLine(getConfluencePrinter().isOnNewLine());

      // Defer printing the link content since we need to gather all nested
      // elements
      pushPrinter(linkLabelPrinter);
    } else if (isFreeStandingURI) {
      print(getLinkRenderer().serialize(link));
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#endLink(org.xwiki.rendering.listener.Link,
   *      boolean, java.util.Map)
   */
  @Override
  public void endLink(ResourceReference link, boolean isFreeStandingURI, Map<String, String> parameters) {
    // The links in a top level link label are not rendered as link (only the
    // label is printed)
    if (getBlockState().getLinkDepth() == 1) {
      ConfluenceSyntaxEscapeWikiPrinter linkBlocksPrinter = getConfluencePrinter();
      linkBlocksPrinter.flush();
      String content = linkBlocksPrinter.toString();
      popPrinter();

      getLinkRenderer().renderLinkContent(getConfluencePrinter(), content);
      getLinkRenderer().endRenderLink(getConfluencePrinter(), link, isFreeStandingURI, parameters);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#beginFormat(org.xwiki.rendering.listener.Format,
   *      java.util.Map)
   */
  @Override
  public void beginFormat(Format format, Map<String, String> parameters) {
    // If the previous format had parameters and the parameters are different
    // from the current ones then close them
    if (this.previousFormatParameters != null) {
      if (parameters.isEmpty()) {
        // print("(%%)");
        // this.previousFormatParameters = null;
      } else if (!this.previousFormatParameters.equals(parameters)) {
        this.previousFormatParameters = null;
        printBeginParameters(parameters, false);
      } else {
        this.previousFormatParameters = null;
      }
    } else if (this.previousFormatParameters == null) {
      printBeginParameters(parameters, false);
    }

    switch (format) {
    case BOLD:
      // Handle empty formatting parameters.
      if (this.previousFormatParameters != null) {
        getPrinter().print("(%%)");
        this.previousFormatParameters = null;
      }

      getConfluencePrinter().printBeginBold();
      break;
    case ITALIC:
      // Handle empty formatting parameters.
      if (this.previousFormatParameters != null) {
        getPrinter().print("(%%)");
        this.previousFormatParameters = null;
      }

      getConfluencePrinter().printBeginItalic();
      break;
    case STRIKEDOUT:
      print("-");
      break;
    case UNDERLINED:
      print("+");
      break;
    case SUPERSCRIPT:
      print("^");
      break;
    case SUBSCRIPT:
      print("~");
      break;
    case MONOSPACE:
      print("{{");
      break;
    }

  }

  /**
   * {@inheritDoc}
   * 
   * @see AbstractChainingPrintRenderer#endFormat(org.xwiki.rendering.listener.Format,
   *      java.util.Map)
   */
  @Override
  public void endFormat(Format format, Map<String, String> parameters) {
    switch (format) {
    case BOLD:
      print("*");
      break;
    case ITALIC:
      getConfluencePrinter().printEndItalic();
      break;
    case STRIKEDOUT:
      print("-");
      break;
    case UNDERLINED:
      print("+");
      break;
    case SUPERSCRIPT:
      print("^");
      break;
    case SUBSCRIPT:
      print("~");
      break;
    case MONOSPACE:
      print("}}");
      break;
    }

    StringBuffer parametersStr = new StringBuffer();
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      String value = entry.getValue();
      String key = entry.getKey();

      if (key != null && value != null) {
        if ("style".equals(key))
          parametersStr.append("{span}");
      }
    }
    print(parametersStr.toString());
    if (!parameters.isEmpty()) {
      this.previousFormatParameters = parameters;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.PrintRenderer#beginParagraph(java.util.Map)
   */
  @Override
  public void beginParagraph(Map<String, String> parameters) {
    printEmptyLine();
    printBeginParameters(parameters);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.PrintRenderer#endParagraph(java.util.Map)
   */
  @Override
  public void endParagraph(Map<String, String> parameters) {
    this.previousFormatParameters = null;
    StringBuffer parametersStr = new StringBuffer();
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      String value = entry.getValue();
      String key = entry.getKey();

      if (key != null && value != null) {
        if ("style".equals(key))
          parametersStr.append("{div}");
      }
    }
    print(parametersStr.toString());
    // Ensure that any not printed characters are flushed.
    // TODO: Fix this better by introducing a state listener to handle escapes
    getConfluencePrinter().flush();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.PrintRenderer#onNewLine()
   */
  @Override
  public void onNewLine() {
    // - If we're inside a table cell, a paragraph, a list or a section header then if we have already outputted
    // a new line before then this new line should be a line break in order not to break the table cell,
    // paragraph, list or section header.

    // - If the new line is the last element of the paragraph, list or section header then it should be a line break
    // as otherwise it'll be considered as an empty line event next time the generated syntax is read by the Confluence
    // parser.

    if (getBlockState().isInLine()) {
      if (getBlockState().isInTableCell()) {
        print("\\\\");
      } else if (getConfluenceSyntaxListenerChain().getConsecutiveNewLineStateChainingListener()
                                            .getNewLineCount() > 1) {
        print("\\\\");
      } else if (getConfluenceSyntaxListenerChain().getLookaheadChainingListener().getNextEvent().eventType.isInlineEnd()) {
        print("\\\\");
      } else {
        print("\n");
      }
    } else {
      print("\n");
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see AbstractChainingPrintRenderer#onMacro(String, java.util.Map, String,
   *      boolean)
   */
  @Override
  public void onMacro(String id, Map<String, String> parameters, String content, boolean isInline) {
    if (!isInline) {
      printEmptyLine();
      print(getMacroPrinter().renderMacro(id, parameters, content, isInline));
    } else {
      getConfluencePrinter().printInlineMacro(getMacroPrinter().renderMacro(id, parameters, content, isInline));
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#beginHeader(org.xwiki.rendering.listener.HeaderLevel,
   *      String, java.util.Map)
   */
  @Override
  public void beginHeader(HeaderLevel level, String id, Map<String, String> parameters) {
    printEmptyLine();
    print("h" + level.getAsInt() + ". ");
    printBeginParameters(parameters, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#endHeader(org.xwiki.rendering.listener.HeaderLevel,
   *      String, java.util.Map)
   */
  @Override
  public void endHeader(HeaderLevel level, String id, Map<String, String> parameters) {
    printEndParameters(parameters, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see AbstractChainingPrintRenderer#onWord(String)
   */
  @Override
  public void onWord(String word) {
    printDelayed(word);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.PrintRenderer#onSpace()
   */
  @Override
  public void onSpace() {
    printDelayed(" ");
  }

  /**
   * {@inheritDoc}
   * 
   * @see AbstractChainingPrintRenderer#onSpecialSymbol(char)
   */
  @Override
  public void onSpecialSymbol(char symbol) {
    printDelayed("" + symbol);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#beginList(org.xwiki.rendering.listener.ListType,
   *      java.util.Map)
   */
  @Override
  public void beginList(ListType listType, Map<String, String> parameters) {
    if (getBlockState().getListDepth() == 1) {
      if (!getBlockState().isInLine()) {
        print("\n");
      } else
        printEmptyLine();
    } else {
      getPrinter().print("\n");
    }

    if (listType == ListType.BULLETED) {
      this.listStyle.append("*");
    } else {
      this.listStyle.append("#");
    }
    printBeginParameters(parameters);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.PrintRenderer#beginListItem()
   */
  @Override
  public void beginListItem() {
    if (getBlockState().getListItemIndex() > 0) {
      getPrinter().print("\n");
    }

    print(this.listStyle.toString());
    if (StringUtils.contains(this.listStyle.toString(), '#')) {
      //print(".");
    }
    print(" ");
  }

  /**
   * {@inheritDoc}
   * 
   * @see AbstractChainingPrintRenderer#endList(org.xwiki.rendering.listener.ListType,
   *      java.util.Map)
   */
  @Override
  public void endList(ListType listType, Map<String, String> parameters) {
    this.listStyle.setLength(this.listStyle.length() - 1);

    // Ensure that any not printed characters are flushed.
    // TODO: Fix this better by introducing a state listener to handle escapes
    getConfluencePrinter().flush();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endListItem()
   */
  @Override
  public void endListItem() {
    this.previousFormatParameters = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#beginMacroMarker(String,
   *      java.util.Map, String, boolean)
   */
  @Override
  public void beginMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline) {
    if (!isInline) {
      printEmptyLine();
    }

    // When we encounter a macro marker we ignore all other blocks inside since we're going to use the macro
    // definition wrapped by the macro marker to construct the confluence syntax.
    pushPrinter(new ConfluenceSyntaxEscapeWikiPrinter(VoidWikiPrinter.VOIDWIKIPRINTER, getConfluenceSyntaxListenerChain()));
  }

  /**
   * {@inheritDoc}
   * 
   * @see AbstractChainingPrintRenderer#endMacroMarker(String, java.util.Map,
   *      String, boolean)
   */
  @Override
  public void endMacroMarker(String name, Map<String, String> parameters, String content, boolean isInline) {
    
    this.previousFormatParameters = null;

    popPrinter();

    print(getMacroPrinter().renderMacro(name, parameters, content, isInline));
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#onId(String)
   */
  @Override
  public void onId(String name) {
    print("{{id name=\"" + name + "\"}}");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.Renderer#onHorizontalLine(Map)
   */
  @Override
  public void onHorizontalLine(Map<String, String> parameters) {
    printEmptyLine();
    printBeginParameters(parameters);
    print("----");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.Renderer#onVerbatim(String, boolean, Map)
   */
  @Override
  public void onVerbatim(String protectedString, boolean isInline, Map<String, String> parameters) {
    if (!isInline) {
      printEmptyLine();
    }
    print("\\");
    getConfluencePrinter().printVerbatimContent(protectedString);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.Renderer#onEmptyLines(int)
   */
  @Override
  public void onEmptyLines(int count) {
    print(StringUtils.repeat("\n", count));
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#beginDefinitionList(java.util.Map)
   */
  @Override
  public void beginDefinitionList(Map<String, String> parameters) {
    if (getBlockState().getDefinitionListDepth() == 1 && !getBlockState().isInList()) {
      printEmptyLine();
    } else {
      print("\n");
    }
    printBeginParameters(parameters);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#beginDefinitionTerm()
   */
  @Override
  public void beginDefinitionTerm() {
    if (getBlockState().getDefinitionListItemIndex() > 0) {
      getPrinter().print("\n");
    }

    if (this.listStyle.length() > 0) {
      print(this.listStyle.toString());
      if (this.listStyle.charAt(0) == '#') {
        //print(".");
      }
    }
    print(StringUtils.repeat(":", getBlockState().getDefinitionListDepth() - 1));
    print("; ");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#beginDefinitionDescription()
   */
  @Override
  public void beginDefinitionDescription() {
    if (getBlockState().getDefinitionListItemIndex() > 0) {
      getPrinter().print("\n");
    }

    if (this.listStyle.length() > 0) {
      print(this.listStyle.toString());
      if (this.listStyle.charAt(0) == '#') {
        //print(".");
      }
    }
    print(StringUtils.repeat(":", getBlockState().getDefinitionListDepth() - 1));
    print(": ");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endDefinitionDescription()
   */
  @Override
  public void endDefinitionDescription() {
    this.previousFormatParameters = null;

    getConfluencePrinter().flush();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endDefinitionTerm()
   */
  @Override
  public void endDefinitionTerm() {
    this.previousFormatParameters = null;

    getConfluencePrinter().flush();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#beginQuotation(java.util.Map)
   */
  @Override
  public void beginQuotation(Map<String, String> parameters) {
    if (!getBlockState().isInQuotationLine()) {
      printEmptyLine();
    }

    if (!parameters.isEmpty()) {
      printBeginParameters(parameters);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#beginQuotationLine()
   */
  @Override
  public void beginQuotationLine() {
    if (getBlockState().getQuotationLineIndex() > 0) {
      getPrinter().print("\n");
    }
    print("bq. ");
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.chaining.AbstractChainingListener#endQuotationLine()
   */
  @Override
  public void endQuotationLine() {
    this.previousFormatParameters = null;
    getConfluencePrinter().flush();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#beginTable(java.util.Map)
   */
  @Override
  public void beginTable(Map<String, String> parameters) {
    printEmptyLine();
    if (!parameters.isEmpty()) {
      printBeginParameters(parameters);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#beginTableCell(java.util.Map)
   */
  @Override
  public void beginTableCell(Map<String, String> parameters) {
    print("|");
    printBeginParameters(parameters, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#beginTableHeadCell(java.util.Map)
   */
  @Override
  public void beginTableHeadCell(Map<String, String> parameters) {
    print("||");
    printBeginParameters(parameters, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#beginTableRow(java.util.Map)
   */
  @Override
  public void beginTableRow(Map<String, String> parameters) {
    if (getBlockState().getCellRow() > 0) {
      print("\n");
    }

    printBeginParameters(parameters, false);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#endTableCell(java.util.Map)
   */
  @Override
  public void endTableCell(Map<String, String> parameters) {
    this.previousFormatParameters = null;

    // Ensure that any not printed characters are flushed.
    // TODO: Fix this better by introducing a state listener to handle escapes
    getConfluencePrinter().flush();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#endTableHeadCell(java.util.Map)
   */
  @Override
  public void endTableHeadCell(Map<String, String> parameters) {
    this.previousFormatParameters = null;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.listener.Listener#onImage(org.xwiki.rendering.listener.Image,
   *      boolean, Map)
   */
  @Override
  public void onImage(ResourceReference image, boolean isFreeStandingURI, Map<String, String> parameters) {    
    if (ResourceType.ICON.equals(image.getType())) {
      getIconRenderer().renderIcon(getConfluencePrinter(), image);
    } else{
      getImageRenderer().beginRenderImage(getConfluencePrinter());
      getImageRenderer().renderImageContent(getConfluencePrinter(), getImageRenderer().renderImage(image));
      getImageRenderer().endRenderImage(getConfluencePrinter(), parameters);  
    }
  }

  protected void printBeginParameters(Map<String, String> parameters) {
    printBeginParameters(parameters, true);
  }
  
  protected void printEndParameters(Map<String, String> parameters) {
    printEndParameters(parameters, true);
  }
  
  protected void printEndParameters(Map<String, String> parameters, boolean newLine) {
    StringBuffer parametersStr = new StringBuffer();
    List<Map.Entry<String, String>> list = new LinkedList<Map.Entry<String,String>>(parameters.entrySet());
    Collections.reverse(list);
    for (Map.Entry<String, String> entry : list) {
      String value = entry.getValue();
      String key = entry.getKey();
      if (key != null && value != null) {
        if ("style".equals(key))
          if (!newLine) {
            parametersStr.append("{span}");
          }
          else {
            parametersStr.append("{div}");
          }
      }
    }
    print(parametersStr.toString());
  }
  
  protected void printBeginParameters(Map<String, String> parameters, boolean newLine) {
    StringBuffer parametersStr = new StringBuffer();
    for (Map.Entry<String, String> entry : parameters.entrySet()) {
      String value = entry.getValue();
      String key = entry.getKey();

      if (key != null && value != null) {
        if ("style".equals(key))
          if (!newLine) {
            parametersStr.append("{span:").append("style=\"").append(value).append("\"}");
          }
          else {
            parametersStr.append("{div:").append("style=\"").append(value).append("\"}");
          }
      }
    }
    print(parametersStr.toString());

  }

  private void printDelayed(String text) {
    print(text, true);
  }

  private void print(String text) {
    print(text, false);
  }

  private void print(String text, boolean isDelayed) {
    // Handle empty formatting parameters.
    if (this.previousFormatParameters != null) {      
      this.previousFormatParameters = null;
    }

    if (isDelayed) {
      getConfluencePrinter().printDelayed(text);
    } else {
      getPrinter().print(text);
    }
  }

  private void printEmptyLine() {
    if (this.isFirstElementRendered) {
      print("\n\n");
    } else {
      this.isFirstElementRendered = true;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.AbstractChainingPrintRenderer#setPrinter(org.xwiki.rendering.renderer.printer.WikiPrinter)
   */
  public void setPrinter(WikiPrinter printer) {
    // If the printer is already a Confluence Syntax Escape printer don't wrap it again. This case happens when
    // the createChainingListenerInstance() method is called, ie when this renderer's state is stacked
    // (for example when a Group event is being handled).
    if (printer instanceof ConfluenceSyntaxEscapeWikiPrinter) {
      super.setPrinter(printer);
    } else {
      super.setPrinter(new ConfluenceSyntaxEscapeWikiPrinter(printer, (ConfluenceSyntaxListenerChain) getListenerChain()));
    }
  }

  /**
   * Allows exposing the additional methods of {@link ConfluenceSyntaxEscapeWikiPrinter}, namely the ability to delay
   * printing some text and the ability to escape characters that would otherwise have a meaning in Confluence syntax.
   */
  public ConfluenceSyntaxEscapeWikiPrinter getConfluencePrinter() {
    return (ConfluenceSyntaxEscapeWikiPrinter) super.getPrinter();
  }

  @Override
  protected void popPrinter() {
    // Ensure that any not printed characters are flushed
    getConfluencePrinter().flush();

    super.popPrinter();
  }

}
