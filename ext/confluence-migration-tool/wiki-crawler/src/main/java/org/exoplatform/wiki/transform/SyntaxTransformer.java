package org.exoplatform.wiki.transform;

import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;

/**
 */
public class SyntaxTransformer {

  private static Logger LOG = LoggerFactory.getLogger(SyntaxTransformer.class.toString()); ;

  public static String transformContent(String content) {
    EmbeddableComponentManager ecm = new EmbeddableComponentManager();
    ecm.initialize(Thread.currentThread().getContextClassLoader());
    WikiPrinter printer = new DefaultWikiPrinter();
    try {
      Converter converter = ecm.lookup(Converter.class);
      converter.convert(new StringReader(content), Syntax.CONFLUENCE_1_0, Syntax.XWIKI_2_0, printer);
      return printer.toString();
    } catch (ComponentLookupException e) {
      LOG.error("TRANSFORMATION FAILURE: " + e.getMessage());
    } catch (ConversionException e) {
      LOG.error("TRANSFORMATION FAILURE: " + e.getMessage());
    }

    return content;
  }

}
