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
import java.util.Locale;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 22, 2009 - 2:11:20 AM  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIAutoPruneSettingForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAutoPruneSettingForm.SaveActionListener.class),
      @EventConfig(listeners = UIAutoPruneSettingForm.RunActionListener.class),
      @EventConfig(listeners = UIAutoPruneSettingForm.CloseActionListener.class, phase=Phase.DECODE)
    }
)
public class UIAutoPruneSettingForm extends BaseForumForm implements UIPopupComponent {
  public static final String FIELD_INACTIVEDAY_INPUT     = "inActiveDay";

  public static final String FIELD_INACTIVEDAY_SELECTBOX = "inActiveDayType";

  public static final String FIELD_JOBDAY_INPUT          = "jobDay";

  public static final String FIELD_JOBDAY_SELECTBOX      = "jobDayType";

  public static final String FIELD_VALUEDAY              = "Day";

  public static final String FIELD_VALUEWEEKS            = "Weeks";

  public static final String FIELD_VALUEMONTHS           = "Months";

  private PruneSetting       pruneSetting;

  private Locale             locale;

  private long               topicOld                    = 0;

  private boolean            isTest                      = false;

  private boolean            isActivate                  = false;

  public UIAutoPruneSettingForm() throws Exception {
    if (getId() == null)
      setId("UIAutoPruneSettingForm");
    UIFormStringInput inActiveDay = new UIFormStringInput(FIELD_INACTIVEDAY_INPUT, FIELD_INACTIVEDAY_INPUT, null);
    inActiveDay.addValidator(PositiveNumberFormatValidator.class);
    UIFormStringInput jobDay = new UIFormStringInput(FIELD_JOBDAY_INPUT, FIELD_JOBDAY_INPUT, null);
    jobDay.addValidator(PositiveNumberFormatValidator.class);

    UIFormSelectBox inActiveDayType = getSelectBox(FIELD_INACTIVEDAY_SELECTBOX, false);
    UIFormSelectBox jobDayType = getSelectBox(FIELD_JOBDAY_SELECTBOX, true);

    addUIFormInput(inActiveDay);
    addUIFormInput(inActiveDayType);
    addUIFormInput(jobDay);
    addUIFormInput(jobDayType);
    setActions(new String[] { "Save", "Close" });
  }

  private void setLocale() throws Exception {
    PortalRequestContext portalContext = Util.getPortalRequestContext();
    Locale locale = portalContext.getLocale();
    if (this.locale == null || !locale.getLanguage().equals(this.locale.getLanguage())) {
      getSelectBox(FIELD_INACTIVEDAY_SELECTBOX, false);
      getSelectBox(FIELD_JOBDAY_SELECTBOX, true);
      this.locale = locale;
    }
  }

  private UIFormSelectBox getSelectBox(String field, boolean isJobDay) {
    List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
    list.add(new SelectItemOption<String>(getLabel(FIELD_VALUEDAY), FIELD_VALUEDAY + ((isJobDay) ? "_Id" : ForumUtils.EMPTY_STR)));
    list.add(new SelectItemOption<String>(getLabel(FIELD_VALUEWEEKS), FIELD_VALUEWEEKS + ((isJobDay) ? "_Id" : ForumUtils.EMPTY_STR)));
    list.add(new SelectItemOption<String>(getLabel(FIELD_VALUEMONTHS), FIELD_VALUEMONTHS + ((isJobDay) ? "_Id" : ForumUtils.EMPTY_STR)));
    UIFormSelectBox selectBox = getUIFormSelectBox(field);
    if (selectBox == null) {
      selectBox = new UIFormSelectBox(field, field, list);
      selectBox.setDefaultValue(FIELD_VALUEDAY + ((isJobDay) ? "_Id" : ForumUtils.EMPTY_STR));
    } else {
      selectBox.setOptions(list);
    }
    selectBox.setHTMLAttribute("title", getLabel("SelectDateTime"));
    return selectBox;
  }

  public boolean isActivate() {
    return isActivate;
  }

  public void setActivate(boolean isActivate) {
    this.isActivate = isActivate;
  }

  protected void setInitForm() throws Exception {
    if (!isTest) {
      long i = pruneSetting.getInActiveDay();
      String type = FIELD_VALUEDAY;
      if (i != 0) {
        if (i % 7 == 0) {
          i = i / 7;
          type = FIELD_VALUEWEEKS;
        } else if (i % 30 == 0) {
          i = i / 30;
          type = FIELD_VALUEMONTHS;
        }
      }
      getUIStringInput(FIELD_INACTIVEDAY_INPUT).setValue(String.valueOf(i));
      getSelectBox(FIELD_INACTIVEDAY_SELECTBOX, false).setValue(type);
      i = pruneSetting.getPeriodTime();
      type = FIELD_VALUEDAY;
      if (i != 0) {
        if (i % 7 == 0) {
          i = i / 7;
          type = FIELD_VALUEWEEKS;
        } else if (i % 30 == 0) {
          i = i / 30;
          type = FIELD_VALUEMONTHS;
        }
      }
      type = type + "_Id";
      getUIStringInput(FIELD_JOBDAY_INPUT).setValue(String.valueOf(i));
      getSelectBox(FIELD_JOBDAY_SELECTBOX, true).setValue(type);
      isTest = false;
    } else {
      setLocale();
    }
  }

  public void setPruneSetting(PruneSetting pruneSetting) {
    this.pruneSetting = pruneSetting;
  }

  public long getTopicOld() {
    return topicOld;
  }

  public void setTopicOld(long topicOld) {
    this.topicOld = topicOld;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  private long convertDay(String type, long date) throws Exception {
    if (type.equals(FIELD_VALUEMONTHS) || type.equals(FIELD_VALUEMONTHS + "_Id"))
      date = date * 30;
    else if (type.equals(FIELD_VALUEWEEKS) || type.equals(FIELD_VALUEWEEKS + "_Id"))
      date = date * 7;
    return date;
  }

  static public class SaveActionListener extends EventListener<UIAutoPruneSettingForm> {
    public void execute(Event<UIAutoPruneSettingForm> event) throws Exception {
      UIAutoPruneSettingForm uiform = event.getSource();
      boolean isInactiveDay = false;
      try {
        String date_ = uiform.getUIStringInput(FIELD_INACTIVEDAY_INPUT).getValue();
        String type = uiform.getUIFormSelectBox(FIELD_INACTIVEDAY_SELECTBOX).getValue();
        uiform.pruneSetting.setInActiveDay(uiform.convertDay(type, Long.parseLong(date_)));
        isInactiveDay = true;
        date_ = uiform.getUIStringInput(FIELD_JOBDAY_INPUT).getValue();
        type = uiform.getUIFormSelectBox(FIELD_JOBDAY_SELECTBOX).getValue();
        uiform.pruneSetting.setPeriodTime(uiform.convertDay(type, Long.parseLong(date_)));
        if (uiform.isActivate) {
          uiform.pruneSetting.setActive(true);
          uiform.isActivate = false;
        }
        uiform.getForumService().savePruneSetting(uiform.pruneSetting);
        UIAutoPruneForm autoPruneForm = uiform.getAncestorOfType(UIForumPortlet.class).findFirstComponentOfType(UIAutoPruneForm.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(autoPruneForm);
        uiform.cancelChildPopupAction();
      } catch (NumberFormatException e) {
        String[] args = new String[] { uiform.getLabel(FIELD_INACTIVEDAY_INPUT) };
        if (isInactiveDay)
          args = new String[] { uiform.getLabel(FIELD_JOBDAY_INPUT) };
        uiform.warning("NameValidator.msg.Invalid-number", args);
        return;
      }
    }
  }

  static public class RunActionListener extends EventListener<UIAutoPruneSettingForm> {
    public void execute(Event<UIAutoPruneSettingForm> event) throws Exception {
      UIAutoPruneSettingForm uiform = event.getSource();
      String date_ = uiform.getUIStringInput(FIELD_INACTIVEDAY_INPUT).getValue();
      String type = uiform.getUIFormSelectBox(FIELD_INACTIVEDAY_SELECTBOX).getValue();
      long date = uiform.convertDay(type, Long.parseLong(date_));
      PruneSetting setting = uiform.pruneSetting;
      setting.setInActiveDay(date);
      uiform.topicOld = uiform.getForumService().checkPrune(setting);
      uiform.pruneSetting.setInActiveDay(date);
      uiform.isTest = true;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiform);
    }
  }

  static public class CloseActionListener extends EventListener<UIAutoPruneSettingForm> {
    public void execute(Event<UIAutoPruneSettingForm> event) throws Exception {
      event.getSource().cancelChildPopupAction();
    }
  }
}
