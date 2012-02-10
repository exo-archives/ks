package org.exoplatform.wiki;

/**
 * Exception during migration
 */
public class ConfluenceCrawlerException extends RuntimeException {


  public ConfluenceCrawlerException(String s) {
    super(s);
  }

  public ConfluenceCrawlerException(String s, Throwable throwable) {
    super(s, throwable);
  }

}
