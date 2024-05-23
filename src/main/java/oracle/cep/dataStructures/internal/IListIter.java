/* $Header: IListIter.java 28-dec-2007.11:01:24 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      11/07/07 - change release
    hopark      10/31/07 - Creation
 */

/**
 *  @version $Header: IListIter.java 28-dec-2007.11:01:24 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;
import java.util.Iterator;

import oracle.cep.execution.ExecException;

public interface IListIter<E>
{
  void initialize(IList<E> list) throws ExecException;
  void release(IList<E> list) throws ExecException;
  E next();
}

