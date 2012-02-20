package org.exoplatform.wiki.transform;

import java.util.Arrays;

import junit.framework.Assert;

import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.SectionBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;

public class TransformationTest {

  public void testTransformation() throws Exception {
    // Initialize Rendering components and allow getting instances
    final EmbeddableComponentManager cm = new EmbeddableComponentManager();
    cm.initialize(this.getClass().getClassLoader());

    SectionBlock sectionBlock = new SectionBlock(null);
    XDOM xdom = new XDOM(Arrays.<Block> asList(sectionBlock));

    Transformation transformation = cm.lookup(Transformation.class, "icon");
    TransformationContext txContext = new TransformationContext();
    transformation.transform(xdom, txContext);

    WikiPrinter printer = new DefaultWikiPrinter();
    BlockRenderer renderer = cm.lookup(BlockRenderer.class, Syntax.XWIKI_2_0.toIdString());
    renderer.render(xdom, printer);

    String expected = "image:emoticon_smile";

    Assert.assertEquals(expected, printer.toString());

  }
}
