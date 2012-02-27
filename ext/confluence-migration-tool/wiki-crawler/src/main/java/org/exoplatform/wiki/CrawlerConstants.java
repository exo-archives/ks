package org.exoplatform.wiki;

/**
 */
public interface CrawlerConstants {
  static String TYPE_EXOWIKI                          = "exowiki";

  static String TYPE_WIKBOOK                          = "wikbook";

  static String ENV_VARIABLE_WIKI_SOURCE_PWD          = "wiki.source.pwd";

  static String ENV_VARIABLE_WIKI_TARGET_PWD          = "wiki.target.pwd";

  static String ENV_VARIABLE_MIGRATION_FILE           = "wiki.migration.file";

  static String PARAMETER_SOURCE_HOST                 = "sourceHost";

  static String PARAMETER_SOURCE_SPACE                = "sourceSpace";

  static String PARAMETER_SOURCE_PAGE                 = "sourcePage";

  static String PARAMETER_SOURCE_USER                 = "sourceUser";

  static String PARAMETER_SOURCE_PWD                  = "sourcePwd";

  static String PARAMETER_TARGET_HOST                 = "targetHost";

  static String PARAMETER_TARGET_SPACE                = "targetSpace";

  static String PARAMETER_TARGET_PAGE                 = "targetPage";

  static String PARAMETER_TARGET_SYNTAX               = "targetSyntax";

  static String PARAMETER_TARGET_USER                 = "targetUser";

  static String PARAMETER_TARGET_PWD                  = "targetPwd";

  static String PARAMETER_OPTION_RECURSIVE            = "recurseOnChildren";

  static String PARAMETER_OPTION_STOP_ON_FAIL         = "stopOnFailure";

  static String PARAMETER_OPTION_TRANSFER_ATTACHMENTS = "transferAttachments";

  static String PARAMETER_OPTION_TRANSFER_COMMENTS    = "transferComments";

  static String PARAMETER_OPTION_TRANSFER_LABELS      = "transferLabels";

  static String PARAMETER_OPTION_MAX_ATT_SIZE         = "maxAttachmentSize";

  static String PARAMETER_OPTION_UPLOAD_TYPE          = "uploadType";
  static String OPTION_UPLOAD_TYPE_ATTACHEMENT        = "attachment";
  static String OPTION_UPLOAD_TYPE_DOCUMENT           = "document";

  static String PARAMETER_ACTIONS                     = "migrationActions";

  static String ACTION_CHECK                          = "check";
  static String ACTION_PERFORM                        = "perform";
  static String ACTION_SYNC_ATTACHMENTS               = "sync";

  static String PARAMETER_TARGET_TYPE                 = "targetType";
}
