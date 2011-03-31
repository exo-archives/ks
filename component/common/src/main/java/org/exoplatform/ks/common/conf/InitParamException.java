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
 * UNchecked exception for problems in initparams. This is typically fired when a component is not happy with the {@link InitParams} it receives.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class InitParamException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = 6033741581889176606L;

  protected InitParams      params;

  protected Class<?>        target;

  protected InitParamException(InitParams params, Class<?> target) {
    this.params = params;
    this.target = target;
  }

  public InitParamException(InitParams params, Class<?> target, String message, Throwable t) {
    this(message, t);
    this.params = params;
    this.target = target;
  }

  public InitParamException() {
  }

  public InitParamException(String arg0) {
    super(arg0);

  }

  public InitParamException(Throwable arg0) {
    super(arg0);

  }

  public InitParamException(String arg0, Throwable arg1) {
    super(arg0, arg1);

  }

  public InitParams getParams() {
    return params;
  }

  public void setParams(InitParams params) {
    this.params = params;
  }

  public Object getTarget() {
    return target;
  }

  public void setTarget(Class<?> target) {
    this.target = target;
  }

}
