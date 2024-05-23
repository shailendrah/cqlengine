/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILogArea.java /main/2 2008/12/10 18:55:57 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/03/08 - add isSystem
    hopark      01/09/08 - add cache area
    hopark      12/20/07 - fix fromValue
    hopark      08/01/07 - add spill
    hopark      06/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILogArea.java /main/2 2008/12/10 18:55:57 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging;

public interface ILogArea 
{
  String  getName();
  boolean isSystem();
  boolean isGlobal();
  int   getValue();
};
