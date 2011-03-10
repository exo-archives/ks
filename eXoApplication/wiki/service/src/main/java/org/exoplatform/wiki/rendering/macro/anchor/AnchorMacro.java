package org.exoplatform.wiki.rendering.macro.anchor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.exoplatform.wiki.service.WikiContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

@Component("anchor")
public class AnchorMacro extends AbstractMacro<AnchorMacroParameters> {
  /**
   * The description of the macro.
   */
  private static final String DESCRIPTION = "render an anchor";

  @Requirement
  private Execution           execution;

  public AnchorMacro() {
    super("Anchor", DESCRIPTION, AnchorMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_NAVIGATION);
  }

  @Override
  public List<Block> execute(AnchorMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {
    ExecutionContext ec = execution.getContext();
    String pageName = null;
    if (ec != null) {
      WikiContext wikiContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
      pageName = wikiContext.getPageId();
    }
    String anchorName = parameters.getName();
    DocumentResourceReference documentReference = new DocumentResourceReference(pageName);
    documentReference.setAnchor(anchorName);
    Block anchorBlock = new LinkBlock(new ArrayList<Block>(), documentReference, true);
    return Collections.singletonList(anchorBlock);
  }

  @Override
  public boolean supportsInlineMode() {
    return true;
  }

}
