/* $Header: IDumpable.java 26-dec-2007.16:02:23 hopark   Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/26/07 - Creation
 */

/**
 *  @version $Header: IDumpable.java 26-dec-2007.16:02:23 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging;

public interface IDumpable
{
  void dump(IDumpContext dump);
}
