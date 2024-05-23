/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/ViewState.java /main/2 2009/01/09 15:21:31 parujain Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    01/09/09 - remove create state
    parujain    11/24/08 - view state
    parujain    11/24/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/ViewState.java /main/2 2009/01/09 15:21:31 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata;

public enum ViewState
{
  // Running
  STARTED,
  // Stopped and previous state was running
  STOPPED;
}
