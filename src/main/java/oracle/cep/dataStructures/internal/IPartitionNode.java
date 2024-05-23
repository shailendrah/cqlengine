/* $Header: IPartitionNode.java 28-feb-2008.18:32:03 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/28/08 - use ITupleDoublyListNode
    hopark      11/07/07 - add setTs
    hopark      10/23/07 - Creation
 */

/**
 *  @version $Header: IPartitionNode.java 28-feb-2008.18:32:03 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;

/**
 * @version $Header: IPartitionNode.java 28-feb-2008.18:32:03 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */

public interface IPartitionNode extends ITupleDoublyListNode
{
  long getTs() throws ExecException;
  void setTs(long ts) throws ExecException;
}