/* $Header: pcbpel/cep/src/oracle/cep/phyplan/window/WinKind.java /main/3 2008/07/14 22:57:01 parujain Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    07/07/08 - value based windows
    hopark      10/12/07 - add PARTITION
    parujain    03/08/07 - Window Kind
    parujain    03/08/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/phyplan/window/WinKind.java /main/3 2008/07/14 22:57:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.window;

/**
 * Enumeration of kind of Windows
 *
 */

public enum WinKind {
  RANGE, EXTENSIBLE, PARTITION, VALUE;
}
