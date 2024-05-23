/* $Header: ChangeType.java 15-mar-2006.10:37:51 skaluska Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares ChangeType in package oracle.cep.metadata.cache.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    skaluska  03/12/06 - Creation
    skaluska  03/12/06 - Creation
 */

/**
 *  @version $Header: ChangeType.java 15-mar-2006.10:37:51 skaluska Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.metadata.cache;

/**
 * ChangeType
 *
 * @author skaluska
 */
public enum ChangeType
{
  NONE,
  CREATED,
  UPDATED,
  DELETED;
}
