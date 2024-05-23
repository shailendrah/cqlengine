/* $Header: pcbpel/cep/src/oracle/cep/common/WindowType.java /main/3 2008/07/14 22:57:01 parujain Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Enumeration of the different window types

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    06/26/08 - value based windows
    parujain    03/06/07 - Extensible Time Windows
    najain      05/30/06 - add NOW 
    dlenkov     05/16/06 - added NOW window
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/common/WindowType.java /main/3 2008/07/14 22:57:01 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.common;

/**
 * Enumeration of the different window types
 *
 * @since 1.0
 */

public enum WindowType {
  RANGE, ROW, PARTITION, NOW, EXTENSIBLE, VALUE;
}
