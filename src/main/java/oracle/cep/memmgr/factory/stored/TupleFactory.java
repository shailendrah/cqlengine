/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/stored/TupleFactory.java /main/18 2008/10/24 15:50:20 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      06/17/08 - fix needRefCounts
    hopark      02/28/08 - tupleptr serialization optimization
    hopark      12/07/07 - cleanup spill
    hopark      10/03/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/factory/stored/TupleFactory.java /main/18 2008/10/24 15:50:20 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr.factory.stored;

import oracle.cep.dataStructures.internal.stored.TuplePtr;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.service.CEPManager;

public class TupleFactory 
  extends oracle.cep.memmgr.factory.paged.TupleFactory
{
  public TupleFactory(CEPManager cepMgr, TupleSpec spec, int id, int initPageTableSize)
  {
    super(cepMgr, spec, id, initPageTableSize);
  }

  protected TuplePtr allocTuplePtr() {return new TuplePtr();}
}
