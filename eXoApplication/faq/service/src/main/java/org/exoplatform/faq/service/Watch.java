package org.exoplatform.faq.service;

public class Watch {
  /** user name of user who add watch */
  private String user;

  /** email of user which is use to get email of FAQ system */
  private String emails;

  private String RSS = " ";

  /**
   * get user name of user who added watch into question or category
   * @return user name of user
   */
  public String getUser() {
    return user;
  }

  /**
   * Register user name for watch
   * @param userName  user name of user
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Get email address of user
   * @return  email address of user which is used to get email
   */
  public String getEmails() {
    return emails;
  }

  /**
   * Register email address to get email
   * @param emails
   */
  public void setEmails(String emails) {
    this.emails = emails;
  }

  public String getRSS() {
    return RSS;
  }

  public void setRSS(String rss) {
    RSS = rss;
  }
}
