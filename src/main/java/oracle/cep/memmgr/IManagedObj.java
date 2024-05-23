/* $Header: IManagedObj.java 05-mar-2008.17:56:14 hopark   Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/05/08 - Creation
 */

/**
 *  @version $Header: IManagedObj.java 05-mar-2008.17:56:14 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

public interface IManagedObj
{
  void free(IAllocator<?> fac);
}
