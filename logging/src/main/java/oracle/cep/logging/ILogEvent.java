/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILogEvent.java /main/2 2009/02/26 21:32:10 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      01/26/09 - add getName
    hopark      01/15/08 - metadata logging for cache objects
    hopark      01/08/08 - Add Events
    hopark      09/27/07 - add SPILL_EVICTFAC
    hopark      08/01/07 - add dump
    hopark      06/11/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILogEvent.java /main/2 2009/02/26 21:32:10 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging;

public interface ILogEvent
{
  ILogArea getLogArea();
  int getOpDSIndex();
  int getValue();
  String getName();
}

