package org.exoplatform.wiki.rendering.macro.anchor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

@Component("anchor")
public class AnchorMacro extends AbstractMacro<AnchorMacroParameters> {
  /**
   * The description of the macro.
   */
  private static final String DESCRIPTION = "Render an anchor. By access a anchor link, user can go to its position";

  public AnchorMacro() {
    super("Anchor", DESCRIPTION, AnchorMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_NAVIGATION);
  }

  @Override
  public List<Block> execute(AnchorMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {
   
    String anchorName = parameters.getName();
    String anchor = "H" + anchorName;
    DocumentResourceReference documentReference = new DocumentResourceReference(null);
    documentReference.setAnchor(anchor);
    Map<String, String> params = new HashMap<String, String>();
    params.put("name", anchor);
    List<Block> inner = new ArrayList<Block>();
    inner.add(new WordBlock(""));
    Block anchorBlock = new LinkBlock(inner, documentReference, false, params);    
    return Collections.singletonList(anchorBlock);
  }

  @Override
  public boolean supportsInlineMode() {
    return true;
  }

}
