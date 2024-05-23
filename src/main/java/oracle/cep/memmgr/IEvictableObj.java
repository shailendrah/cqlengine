/* $Header: IEvictable.java 03-nov-2007.09:21:06 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/15/07 - Creation
 */

/**
 *  @version $Header: IEvictable.java 03-nov-2007.09:21:06 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

import oracle.cep.execution.ExecException;

public interface IEvictableObj
{
  public boolean evict() throws ExecException;
}
