/* $Header: ElementKind.java 07-jun-2007.14:48:42 hopark Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares ElementKind in package oracle.cep.execution.queues.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    hopark    06/07/07 - move fromOrdinal to EnumUtil
    skaluska  02/09/06 - Creation
    skaluska  02/09/06 - Creation
 */

/**
 *  @version $Header: ElementKind.java 07-jun-2007.14:48:42 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.execution.queues;

/**
 * @author skaluska
 *
 */
public enum ElementKind
{
  E_PLUS, E_MINUS, E_HEARTBEAT;
}
