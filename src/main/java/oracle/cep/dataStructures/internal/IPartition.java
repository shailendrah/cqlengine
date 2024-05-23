/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/IPartition.java /main/4 2012/06/20 05:24:30 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    IPartition represents a partition in a partition window.
    It is IDoublyList<ITuplePtr, long>.
    However IDoublyList cannot be used directly, as Java does 
    not allow primitive types for generics and we do not want to
    create additional Long object for it.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       05/29/12 - added window size tracking methods
    hopark      02/28/08 - use ITupleDoublyList
    hopark      11/07/07 - change add
    najain      03/12/07 - bug fix
    najain      03/12/07 - bug fix
    najain      03/08/07 - cleanup
    najain      03/02/07 - 
    hopark      02/23/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/dataStructures/internal/IPartition.java /main/4 2012/06/20 05:24:30 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;

public interface IPartition extends ITupleDoublyList
{
  <T extends IPartitionNode> T  add(ITuplePtr tuple, long ts) 
                                                   throws ExecException;
  
  public void incrementWindowSize();
  
  public void decrementWindowSize();
  
  public int getWindowSize();
}

