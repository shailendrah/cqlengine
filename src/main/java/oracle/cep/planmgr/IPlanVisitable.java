/* $Header: IPlanVisitable.java 05-jun-2007.18:01:35 hopark   Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/05/07 - Creation
 */

/**
 *  @version $Header: IPlanVisitable.java 05-jun-2007.18:01:35 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr;

public interface IPlanVisitable
{
  void accept(IPlanVisitor visitor);
}
