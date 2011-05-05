/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.poll.service;

/**
 * Constants for Forum nodetypes and properties.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface PollNodeTypes {

  public static final String EXO_POLL           = "exo:poll";

  public static final String EXO_IS_AGAIN_VOTE  = "exo:isAgainVote";

  public static final String EXO_IS_MULTI_CHECK = "exo:isMultiCheck";

  public static final String EXO_USER_VOTE      = "exo:userVote";

  public static final String EXO_VOTE           = "exo:vote";

  public static final String EXO_OPTION         = "exo:option";

  public static final String EXO_QUESTION       = "exo:question";

  public static final String EXO_TIME_OUT       = "exo:timeOut";

  public static final String EXO_IS_CLOSED      = "exo:isClosed";

  public static final String EXO_ID             = "exo:id";

  public static final String EXO_MODIFIED_DATE  = "exo:modifiedDate";

  public static final String EXO_LASTVOTE       = "exo:lastVote";

  public static final String EXO_MODIFIED_BY    = "exo:modifiedBy";

  public static final String EXO_CREATED_DATE   = "exo:createdDate";

  public static final String EXO_OWNER          = "exo:owner";

  public static final String TEXT_HTML          = "text/html";

  public static final String JCR_ROOT           = "/jcr:root";

  public static final String EXO_IS_POLL        = "exo:isPoll";

  public static final String NT_UNSTRUCTURED    = "nt:unstructured";

  public static final String POLL               = "poll".intern();

  public static final String APPLICATION_DATA   = "ApplicationData".intern();

  public static final String EXO_POLLS          = "eXoPolls".intern();

  public static final String POLLS              = "Polls".intern();

  public static final String GROUPS             = "Groups".intern();

}
