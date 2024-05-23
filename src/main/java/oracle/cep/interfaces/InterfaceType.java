/* $Header: InterfaceType.java 07-mar-2008.06:16:08 udeshmuk Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares SourceType in package oracle.cep.interfaces.input.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sbishnoi  03/10/08 - adding DB
    udeshmuk  03/07/08 - add entry for multi-line-field file.
    sbishnoi  12/11/07 - add Class as a destination type
    parujain  11/09/07 - external source
    anasrini  10/25/06 - Add FABRIC as a type
    anasrini  08/17/06 - support for JMS 
    skaluska  03/24/06 - Creation
    skaluska  03/24/06 - Creation
    skaluska  03/21/06 - Creation
    skaluska  03/21/06 - Creation
 */

/**
 *  @version $Header: InterfaceType.java 07-mar-2008.06:16:08 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.interfaces;

/**
 * Interface Types that are supported
 *
 * @author skaluska
 */
public enum InterfaceType
{
  // Tuples move over the network
  NETWORK,
  // Tuples are read/written from/to a file
  FILE,
  // Tuples are read/written from a file. The tuples can contain field spanning multiple lines
  MLFFILE,
  // Tuples move over Fabric
  FABRIC,
  // Tuples are communicated over a web-service
  WEB_SERVICE,
  // Tuples are written onto a JMS bus
  JMS,
  // External Source
  EXTERNAL,
  // Tuples are processed by Class Object
  JAVA,
  // Tuples are written into a database
  DB;
}
