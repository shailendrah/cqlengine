/* $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/ISortedArray.java /main/1 2008/09/19 00:00:39 skmishra Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    skmishra    08/15/08 - renaming to ISortedArray
    skmishra    07/23/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/ISortedArray.java /main/1 2008/09/19 00:00:39 skmishra Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals;

import oracle.cep.exceptions.CEPException;

public interface ISortedArray<T>
{
  public int insert(T a) throws CEPException;
  public boolean contains(T a) throws CEPException;
  public void clear() throws CEPException;
}