package org.exoplatform.wiki.rendering.macro.anchor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

@Component("anchor")
public class AnchorMacro extends AbstractMacro<AnchorMacroParameters> {
  /**
   * The description of the macro.
   */
  private static final String DESCRIPTION = "render an anchor";
  public AnchorMacro() {
    super("Anchor", DESCRIPTION, new DefaultContentDescriptor(), AnchorMacroParameters.class);
  }
  
  @Override
  public List<Block> execute(AnchorMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {
    String anchorName = parameters.getName();
    Map<String, String> params = new LinkedHashMap<String, String>();
    // add prefix 'H' to the anchor name for eXo Wiki convention.
    params.put("name", "H" + anchorName);
    Block anchorBlock = new LinkBlock(new ArrayList<Block>(), new Link(), true, params);
    return Collections.singletonList(anchorBlock);
  }

  @Override
  public boolean supportsInlineMode() {
    return true;
  }

}
