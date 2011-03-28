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
package org.exoplatform.forum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 18, 2009 - 7:49:34 AM  
 */
public class SettingPortletPreference {
  private int          forumNewPost         = 1;

  private boolean      enableIPLogging      = false;

  private boolean      enableIPFiltering    = false;

  private boolean      isShowForumActionBar = false;

  private boolean      isShowForumJump      = false;

  private boolean      isShowPoll           = false;

  private boolean      isShowModerators     = false;

  private boolean      isShowQuickReply     = false;

  private boolean      isShowIconsLegend    = false;

  private boolean      isShowRules          = false;

  private boolean      isShowStatistics     = false;

  private boolean      useAjax              = true;

  private List<String> invisibleForums      = new ArrayList<String>();

  private List<String> invisibleCategories  = new ArrayList<String>();

  /*
   * [ ] Forum Jump [ ] Polls [ ] Moderators [ ] Quick Reply [ ] Icons Legend [ ] Rules [ ] Statistics
   */
  public SettingPortletPreference() {
  }

  public int getForumNewPost() {
    return forumNewPost;
  }

  public void setForumNewPost(int forumNewPost) {
    this.forumNewPost = forumNewPost;
  }

  public boolean isEnableIPLogging() {
    return enableIPLogging;
  }

  public void setEnableIPLogging(boolean enableIPLogging) {
    this.enableIPLogging = enableIPLogging;
  }

  public boolean isEnableIPFiltering() {
    return enableIPFiltering;
  }

  public void setEnableIPFiltering(boolean enableIPFiltering) {
    this.enableIPFiltering = enableIPFiltering;
  }

  public boolean isShowForumActionBar() {
    return isShowForumActionBar;
  }

  public void setShowForumActionBar(boolean isShowForumActionBar) {
    this.isShowForumActionBar = isShowForumActionBar;
  }

  public boolean isShowForumJump() {
    return isShowForumJump;
  }

  public void setShowForumJump(boolean isShowForumJump) {
    this.isShowForumJump = isShowForumJump;
  }

  public boolean isShowPoll() {
    return isShowPoll;
  }

  public void setShowPoll(boolean isShowPoll) {
    this.isShowPoll = isShowPoll;
  }

  public boolean isShowModerators() {
    return isShowModerators;
  }

  public void setShowModerators(boolean isShowModerators) {
    this.isShowModerators = isShowModerators;
  }

  public boolean isShowQuickReply() {
    return isShowQuickReply;
  }

  public void setShowQuickReply(boolean isShowQuickReply) {
    this.isShowQuickReply = isShowQuickReply;
  }

  public boolean isShowIconsLegend() {
    return isShowIconsLegend;
  }

  public void setShowIconsLegend(boolean isShowIconsLegend) {
    this.isShowIconsLegend = isShowIconsLegend;
  }

  public boolean isShowRules() {
    return isShowRules;
  }

  public void setShowRules(boolean isShowRules) {
    this.isShowRules = isShowRules;
  }

  public boolean isShowStatistics() {
    return isShowStatistics;
  }

  public void setShowStatistics(boolean isShowStatistics) {
    this.isShowStatistics = isShowStatistics;
  }

  public boolean isUseAjax() {
    return useAjax;
  }

  public void setUseAjax(boolean useAjax) {
    this.useAjax = useAjax;
  }

  public List<String> getInvisibleForums() {
    return invisibleForums;
  }

  public void setInvisibleForums(List<String> invisibleForums) {
    this.invisibleForums = invisibleForums;
  }

  public List<String> getInvisibleCategories() {
    return invisibleCategories;
  }

  public void setInvisibleCategories(List<String> invisibleCategories) {
    this.invisibleCategories = invisibleCategories;
  }
}
