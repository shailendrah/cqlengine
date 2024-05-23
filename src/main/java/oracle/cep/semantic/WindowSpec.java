/* $Header: WindowSpec.java 24-feb-2006.01:48:12 anasrini Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Base interface for the different types of window expressions.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    02/24/06 - add javadoc comments 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: WindowSpec.java 24-feb-2006.01:48:12 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.common.WindowType;

/**
 * Post semantic analysis representation of a window expression.
 * <p>
 * This is the base interface for the different types of window expressions.
 *
 * @since 1.0
 */

public interface WindowSpec {

  /**
   * Get the type of window specification
   * @return the type of window specification
   */
  public WindowType getWindowType();
}
