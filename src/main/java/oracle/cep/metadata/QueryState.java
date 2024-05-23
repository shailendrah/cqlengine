/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/QueryState.java /main/2 2008/12/18 13:15:46 parujain Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
 DESCRIPTION
 Declares QueryState in package oracle.cep.metadata.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    parujain  12/12/08 - drop/stop redesign
    najain    05/10/06 - add more states
    skaluska  03/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/QueryState.java /main/2 2008/12/18 13:15:46 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.metadata;

/**
 * QueryState
 *
 * @author skaluska
 */
public enum QueryState
{
  // Created
  CREATE,
  // Typechecked
  TYPECHECKED,
  // Compiled
  READY,
  // Running
  RUN,
  // Compile error
  COMPILE_ERROR,
  // Runtime error
  RUNTIME_ERROR,
  // Stopped previously running
  STOPPED;
}
