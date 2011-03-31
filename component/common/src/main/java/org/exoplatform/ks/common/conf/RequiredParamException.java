/*
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
 */
package org.exoplatform.ks.common.conf;

import org.exoplatform.container.xml.InitParams;

/**
 * Exception thrown by components to indicate that a param is missing in configuration.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class RequiredParamException extends InitParamException {

  /**
   * 
   */
  private static final long serialVersionUID = -1652969473404169213L;

  private String            paramName;

  private Class<?>          expectedType;

  public RequiredParamException(InitParams params, Class<?> target, String paramName, Class<?> expectedType) {
    super(params, target);
    this.paramName = paramName;
    this.expectedType = expectedType;
  }

  public String getMessage() {
    String msg = "A required param of type " + expectedType + " is required for " + paramName + " as init-param of " + target + ". Received: " + params;
    return msg;
  }

  public String getParamName() {
    return paramName;
  }

  public void setParamName(String paramName) {
    this.paramName = paramName;
  }

  public Class<?> getExpectedType() {
    return expectedType;
  }

  public void setExpectedType(Class<?> expectedType) {
    this.expectedType = expectedType;
  }
}
