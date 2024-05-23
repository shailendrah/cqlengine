/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/ITupleAllocator.java /main/1 2008/10/17 15:45:36 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/16/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/ITupleAllocator.java /main/1 2008/10/17 15:45:36 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.execution.internals.TupleSpec;

public interface ITupleAllocator extends IAllocator<ITuplePtr>
{
  TupleSpec getTupleSpec();
}

