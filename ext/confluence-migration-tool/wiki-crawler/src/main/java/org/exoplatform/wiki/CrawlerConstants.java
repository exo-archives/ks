package org.exoplatform.wiki;

/**
 */
public interface CrawlerConstants {
  public static final String TYPE_EXOWIKI = "exowiki";
  public static final String TYPE_WIKBOOK = "wikbook";

  public static final String ENV_VARIABLE_WIKI_SOURCE_PWD = "wiki.source.pwd";
  public static final String ENV_VARIABLE_WIKI_TARGET_PWD = "wiki.target.pwd";
  public static final String ENV_VARIABLE_MIGRATION_FILE = "wiki.migration.file";

  public static final String PARAMETER_SOURCE_HOST = "sourceHost";
  public static final String PARAMETER_SOURCE_SPACE = "sourceSpace";
  public static final String PARAMETER_SOURCE_PAGE = "sourcePage";
  public static final String PARAMETER_SOURCE_USER = "sourceUser";
  public static final String PARAMETER_SOURCE_PWD = "sourcePwd";
  public static final String PARAMETER_TARGET_HOST = "targetHost";
  public static final String PARAMETER_TARGET_SPACE = "targetSpace";
  public static final String PARAMETER_TARGET_PAGE = "targetPage";
  public static final String PARAMETER_TARGET_USER = "targetUser";
  public static final String PARAMETER_TARGET_PWD = "targetPwd";

  public static final String PARAMETER_OPTION_RECURSIVE = "recurseOnChildren";
  public static final String PARAMETER_OPTION_STOP_ON_FAIL = "stopOnFailure";
  public static final String PARAMETER_OPTION_TRANSFER_ATTACHMENTS = "transferAttachments";
  public static final String PARAMETER_OPTION_TRANSFER_COMMENTS = "transferComments";
  public static final String PARAMETER_OPTION_TRANSFER_LABELS = "transferLabels";
  public static final String PARAMETER_OPTION_MAX_ATT_SIZE = "maxAttachmentSize";

  public static final String PARAMETER_ACTIONS = "migrationActions";

  public static final String ACTION_CHECK = "check";
  public static final String ACTION_PERFORM = "perform";

  public static final String PARAMETER_TARGET_TYPE = "targetType";
}
