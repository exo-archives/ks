/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.api.BBCodeService;
import org.exoplatform.ks.bbcode.core.BBCodeRenderer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Apr 28, 2009 - 9:55:17 AM  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIAddBBCodeForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAddBBCodeForm.SaveActionListener.class), 
      @EventConfig(listeners = UIAddBBCodeForm.PreviewActionListener.class),
      @EventConfig(listeners = UIAddBBCodeForm.ApplyActionListener.class),
      @EventConfig(listeners = UIAddBBCodeForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIAddBBCodeForm extends BaseForumForm implements UIPopupComponent {
  public static final String FIELD_TAGNAME_INPUT       = "TagName";

  public static final String FIELD_REPLACEMENT_TEXTARE = "Replacement";

  public static final String FIELD_DESCRIPTION_TEXTARE = "Description";

  public static final String FIELD_EXAMPLE_TEXTARE     = "Example";

  public static final String FIELD_USEOPTION_CHECKBOX  = "UseOption";

  public static final String PREVIEW                   = "priview";

  private boolean            isPriview                 = false;

  private BBCodeService      bbCodeService;

  private String             example                   = ForumUtils.EMPTY_STR;

  private List<BBCode>       listBBCode                = new ArrayList<BBCode>();

  private BBCode             bbcode                    = new BBCode();

  private static Log         log                       = ExoLogger.getLogger(UIAddBBCodeForm.class);

  public UIAddBBCodeForm() throws Exception {
    if (ForumUtils.isEmpty(this.getId()))
      this.setId("UIAddBBCodeForm");
    bbCodeService = (BBCodeService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(BBCodeService.class);
    UIFormStringInput tagNameInput = new UIFormStringInput(FIELD_TAGNAME_INPUT, FIELD_TAGNAME_INPUT, null);
    tagNameInput.addValidator(MandatoryValidator.class);
    UIFormTextAreaInput replacementInput = new UIFormTextAreaInput(FIELD_REPLACEMENT_TEXTARE, FIELD_REPLACEMENT_TEXTARE, null);
    replacementInput.addValidator(MandatoryValidator.class);
    UIFormTextAreaInput description = new UIFormTextAreaInput(FIELD_DESCRIPTION_TEXTARE, FIELD_DESCRIPTION_TEXTARE, null);
    UIFormTextAreaInput example = new UIFormTextAreaInput(FIELD_EXAMPLE_TEXTARE, FIELD_EXAMPLE_TEXTARE, null);
    example.addValidator(MandatoryValidator.class);
    UICheckBoxInput isOption = new UICheckBoxInput(FIELD_USEOPTION_CHECKBOX, FIELD_USEOPTION_CHECKBOX, false);
    addUIFormInput(tagNameInput);
    addUIFormInput(replacementInput);
    addUIFormInput(description);
    addUIFormInput(example);
    addUIFormInput(isOption);
    this.setActions(new String[] { "Save", "ResetField", "Cancel" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  protected boolean getPriview() {
    return isPriview;
  }

  public void setEditBBcode(BBCode bbcode) throws Exception {
    this.bbcode.setId(bbcode.getId());
    this.getUIStringInput(FIELD_TAGNAME_INPUT).setValue(bbcode.getTagName());
    UIFormTextAreaInput replacement = this.getUIFormTextAreaInput(FIELD_REPLACEMENT_TEXTARE);
    replacement.setValue(bbcode.getReplacement());
    if (bbcode.getTagName().equalsIgnoreCase("list")) {
      replacement.setReadOnly(true);
    }
    this.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTARE).setValue(bbcode.getDescription());
    this.getUIFormTextAreaInput(FIELD_EXAMPLE_TEXTARE).setValue(bbcode.getExample());
    this.getUICheckBoxInput(FIELD_USEOPTION_CHECKBOX).setChecked(bbcode.isOption());
  }

  private void setBBcode() throws Exception {
    String tagName = getUIStringInput(FIELD_TAGNAME_INPUT).getValue();
    String replacement = getUIFormTextAreaInput(FIELD_REPLACEMENT_TEXTARE).getValue();
    String description = getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTARE).getValue();
    String example = getUIFormTextAreaInput(FIELD_EXAMPLE_TEXTARE).getValue();
    boolean isOption = (Boolean) getUICheckBoxInput(FIELD_USEOPTION_CHECKBOX).getValue();
    if (ForumUtils.isEmpty(description))
      description = " ";
    bbcode.setTagName(tagName.toUpperCase());
    bbcode.setReplacement(replacement);
    bbcode.setDescription(description);
    bbcode.setExample(example);
    bbcode.setOption(isOption);
    if (bbcode.getId() == null)
      bbcode.setId(bbcode.getTagName() + ((bbcode.isOption()) ? "=" : ForumUtils.EMPTY_STR));
    this.example = example;
  }
  
  protected String getReplaceByBBCode() throws Exception {
    return (new BBCodeRenderer()).renderExample(example, bbcode);
  }

  static public class SaveActionListener extends EventListener<UIAddBBCodeForm> {
    public void execute(Event<UIAddBBCodeForm> event) throws Exception {
      UIAddBBCodeForm uiForm = event.getSource();
      uiForm.setBBcode();
      uiForm.listBBCode = new ArrayList<BBCode>();
      try {
        uiForm.listBBCode.addAll(uiForm.bbCodeService.getAll());
      } catch (Exception e) {
        if (log.isDebugEnabled()){
          log.debug("Failed to get all BB codes", e);
        }
      }
      for (BBCode code : uiForm.listBBCode) {
        if (uiForm.bbcode.getTagName().equals(code.getTagName()) && (uiForm.bbcode.isOption() == code.isOption()) && !uiForm.bbcode.getId().equals(code.getId())) {
          uiForm.warning("UIAddBBCodeForm.msg.addDuplicateBBCode");
          return;
        }
      }
      try {
        List<BBCode> bbcodes = new ArrayList<BBCode>();
        bbcodes.add(uiForm.bbcode);
        uiForm.bbCodeService.save(bbcodes);
      } catch (Exception e) {
        log.error("Can not save BBCode has name: " + uiForm.bbcode.getTagName(), e);
      }
      try {
        UIBBCodeManagerForm codeManagerForm = uiForm.getAncestorOfType(UIForumPortlet.class).findFirstComponentOfType(UIBBCodeManagerForm.class);
        codeManagerForm.loadBBCodes();
        event.getRequestContext().addUIComponentToUpdateByAjax(codeManagerForm);
      } catch (Exception e) {
        log.error("Can not update from: UIBBCodeManagerForm", e);
      }
      uiForm.cancelChildPopupAction();
    }
  }

  static public class PreviewActionListener extends EventListener<UIAddBBCodeForm> {
    public void execute(Event<UIAddBBCodeForm> event) throws Exception {
      UIAddBBCodeForm uiForm = event.getSource();
      String priview = event.getRequestContext().getRequestParameter(OBJECTID);
      if (priview.equals(PREVIEW)) {
        uiForm.isPriview = true;
        uiForm.setBBcode();
      } else {
        uiForm.isPriview = false;
        uiForm.listBBCode = new ArrayList<BBCode>();
        uiForm.getUIStringInput(FIELD_TAGNAME_INPUT).setValue(uiForm.bbcode.getTagName());
        uiForm.getUIFormTextAreaInput(FIELD_REPLACEMENT_TEXTARE).setValue(uiForm.bbcode.getReplacement());
        uiForm.getUIFormTextAreaInput(FIELD_DESCRIPTION_TEXTARE).setValue(uiForm.bbcode.getDescription());
        uiForm.getUICheckBoxInput(FIELD_USEOPTION_CHECKBOX).setChecked(uiForm.bbcode.isOption());
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class ApplyActionListener extends EventListener<UIAddBBCodeForm> {
    public void execute(Event<UIAddBBCodeForm> event) throws Exception {
      UIAddBBCodeForm uiForm = event.getSource();
      String example = uiForm.getUIFormTextAreaInput(FIELD_EXAMPLE_TEXTARE).getValue();
      uiForm.example = example;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  static public class CancelActionListener extends EventListener<UIAddBBCodeForm> {
    public void execute(Event<UIAddBBCodeForm> event) throws Exception {
      event.getSource().cancelChildPopupAction();
    }
  }
}
