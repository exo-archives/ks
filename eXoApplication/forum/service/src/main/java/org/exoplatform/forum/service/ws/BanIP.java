/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.ws;

/**
 * Represents a banned IP address.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class BanIP {
  private String ip;

  public BanIP() {
  }

  public BanIP(String str) {
    ip = str;
  }

  public void setIp(String str) {
    this.ip = str;
  }

  public String getIp() {
    return ip;
  }

}
