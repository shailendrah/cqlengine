/* $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/IQSinglyListNode.java /main/4 2009/05/29 19:35:21 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/18/09 - fix tsorder
    hopark      02/28/08 - remove getTuple
    hopark      01/31/08 - change copyTo to get
    hopark      10/30/07 - remove IQueueElement
    najain      03/12/07 - bug fix
    najain      03/12/07 - bug fix
    najain      03/02/07 - 
    hopark      02/19/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/IQSinglyListNode.java /main/4 2009/05/29 19:35:21 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;

/**
 * @version $Header: pcbpel/cep/server/src/oracle/cep/dataStructures/internal/IQSinglyListNode.java /main/4 2009/05/29 19:35:21 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */

public interface IQSinglyListNode extends ITupleSinglyListNode
{
  /**
   * @return Returns the kind.
   */
  QueueElement.Kind getKind() throws ExecException;

  /**
   * @return Returns the ts.
   */
  long getTs() throws ExecException;

  boolean getTotalOrderingGuarantee();
  
  /**
   * @param kind
   *          The kind to set.
   */
  void setKind(QueueElement.Kind kind) throws ExecException;

  /**
   * @param ts
   *          The ts to set.
   */
  void setTs(long ts) throws ExecException;

  void setTotalOrderingGuarantee(boolean isGuaranteed);
  
  /**
   * Get queueElement from the node.
   * Buf may or may not be used depend on an implementation.
   * 
   * @param buf
   * @throws ExecException
   */
  QueueElement get(QueueElement buf) throws ExecException;
  void set(QueueElement buf, int readers) throws ExecException;
  
  void setReaders(int n);
  
  int decAndGetReaders(); 
}
