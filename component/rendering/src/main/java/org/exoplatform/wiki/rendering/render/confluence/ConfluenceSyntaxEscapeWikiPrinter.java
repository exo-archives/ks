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

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xwiki.rendering.renderer.printer.LookaheadWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 2, 2010  
 */

/**
 * A Wiki printer that knows how to escape characters that would otherwise mean something different in Confluence wiki
 * syntax. For example if we have "*" as special symbols (and not as a Bold Format block) we need to escape them to
 * "~*" as otherwise they'd be considered bold after being rendered.
 */
public class ConfluenceSyntaxEscapeWikiPrinter extends LookaheadWikiPrinter {
  
  private static final Pattern VERBATIM_PATTERN = Pattern.compile("(\\{\\{\\{)|(\\}\\}\\})");

  private ConfluenceSyntaxListenerChain listenerChain;

  private ConfluenceSyntaxEscapeHandler escapeHandler;

  private boolean escapeLastChar;

  private Pattern escapeFirstIfMatching;

  private String lastPrinted;

  public ConfluenceSyntaxEscapeWikiPrinter(WikiPrinter printer, ConfluenceSyntaxListenerChain listenerChain) {
    
    super(printer);

    this.escapeHandler = new ConfluenceSyntaxEscapeHandler();

    this.listenerChain = listenerChain;
  }

  public ConfluenceSyntaxEscapeHandler getEscapeHandler() {
    return escapeHandler;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.printer.LookaheadWikiPrinter#printInternal(java.lang.String)
   */
  @Override
  protected void printInternal(String text) {
    super.printInternal(text);

    int length = text.length();

    if (length > 0) {
      this.escapeHandler.setOnNewLine(text.charAt(length - 1) == '\n');
    }

    this.lastPrinted = text;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.printer.LookaheadWikiPrinter#println(java.lang.String)
   */
  @Override
  protected void printlnInternal(String text) {
    super.printlnInternal(text);

    this.escapeHandler.setOnNewLine(true);

    this.lastPrinted = "\n";
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.xwiki.rendering.renderer.printer.LookaheadWikiPrinter#flush()
   */
  @Override
  public void flush() {
    if (getBuffer().length() > 0) {
      this.escapeHandler.escape(getBuffer(), this.listenerChain, this.escapeLastChar, this.escapeFirstIfMatching);
      super.flush();
    }
    this.escapeLastChar = false;
    this.escapeFirstIfMatching = null;
  }

  public void printBeginBold() {
    flush();

    boolean isOnNewLine = this.escapeHandler.isOnNewLine();

    print("*");

    if (isOnNewLine) {
      this.escapeFirstIfMatching = ConfluenceSyntaxEscapeHandler.STARLISTEND_PATTERN;
    }
  }

  public void setEscapeLastChar(boolean escapeLastChar) {
    this.escapeLastChar = escapeLastChar;
  }

  public void setBeforeLink(boolean beforeLink) {
    this.escapeHandler.setBeforeLink(beforeLink);
  }

  public void setOnNewLine(boolean onNewLine) {
    this.escapeHandler.setOnNewLine(onNewLine);
  }

  public boolean isOnNewLine() {
    return this.escapeHandler.isOnNewLine();
  }

  public boolean isAfterWhiteSpace() {
    return isOnNewLine()
        || Character.isWhitespace(getLastPrinted().charAt(getLastPrinted().length() - 1));
  }

  public String getLastPrinted() {
    return this.lastPrinted;
  }

  public void printBeginItalic() {
    // If the lookahead buffer is not empty and the last character is ":" then
    // we need to escape it since otherwise we would get "://" which could be confused for a URL.
    if (getBuffer().length() > 0 && getBuffer().charAt(getBuffer().length() - 1) == ':') {
      this.escapeLastChar = true;
    }

    print("_");
  }

  public void printEndItalic() {
    // If the lookahead buffer is not empty and the last character is ":" then
    // we need to escape it since otherwise we would get "://" which could be confused for a URL.
    if (getBuffer().length() > 0 && getBuffer().charAt(getBuffer().length() - 1) == ':') {
      this.escapeLastChar = true;
    }

    print("_");
  }

  public void printInlineMacro(String confluenceSyntaxText) {
    // If the lookahead buffer is not empty and the last character is "{" then
    // we need to escape it since otherwise we would get "{{{" which could be confused for a verbatim block.
    if (getBuffer().length() > 0 && getBuffer().charAt(getBuffer().length() - 1) == '{') {
      this.escapeLastChar = true;
    }

    print(confluenceSyntaxText);
  }

  public void printVerbatimContent(String verbatimContent) {
    StringBuffer result = new StringBuffer();

    Stack<StringBuffer> subVerbatimStack = new Stack<StringBuffer>();
    boolean printEndVerbatim = false;

    Matcher matcher = VERBATIM_PATTERN.matcher(verbatimContent);
    int currentIndex = 0;
    for (; matcher.find(); currentIndex = matcher.end()) {
      String before = verbatimContent.substring(currentIndex, matcher.start());

      if (printEndVerbatim) {
        if (before.startsWith("}")) {
          result.append("~}~}~}");
        } else {
          result.append("~}}}");
        }
      }

      if (subVerbatimStack.size() == 0) {
        result.append(before);
      } else {
        subVerbatimStack.peek().append(before);
      }

      if (matcher.group(1) != null) {
        subVerbatimStack.push(new StringBuffer());
      } else {
        if (subVerbatimStack.size() == 0) {
          printEndVerbatim = true;
        } else {
          StringBuffer subVerbatim = subVerbatimStack.pop();

          if (subVerbatimStack.size() == 0) {
            result.append("{{{");
            result.append(subVerbatim);
            result.append("}}}");
          } else {
            subVerbatimStack.peek().append("{{{");
            subVerbatimStack.peek().append(subVerbatim);
            subVerbatimStack.peek().append("}}}");
          }
        }
      }
    }

    if (currentIndex == 0) {
      print(verbatimContent);
      return;
    }

    String end = verbatimContent.substring(currentIndex);

    if (printEndVerbatim) {
      if (end.length() == 0 || end.charAt(0) == '}') {
        result.append("~}~}~}");
      } else {
        result.append("~}}}");
      }
    }

    if (subVerbatimStack.size() > 0) {
      // Append remaining string
      subVerbatimStack.peek().append(end);

      // Escape not closed verbatim blocks
      while (subVerbatimStack.size() > 0) {
        StringBuffer subVerbatim = subVerbatimStack.pop();

        if (subVerbatimStack.size() == 0) {
          if (subVerbatim.length() > 0 && subVerbatim.charAt(0) == '{') {
            result.append("~{~{~{");
          } else {
            result.append("~{{{");
          }
          result.append(subVerbatim);
        } else {
          if (subVerbatim.length() > 0 && subVerbatim.charAt(0) == '{') {
            subVerbatimStack.peek().append("~{~{~{");
          } else {
            subVerbatimStack.peek().append("~{{{");
          }
          subVerbatimStack.peek().append(subVerbatim);
        }
      }
    } else {
      // Append remaining string
      result.append(end);
    }

    print(result.toString());
  }
}
