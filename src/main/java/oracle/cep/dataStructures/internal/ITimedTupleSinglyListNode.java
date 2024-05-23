/* $Header: ITimedTupleSinglyListNode.java 28-feb-2008.18:32:36 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/28/08 - use ITupleSinglyListNode
    hopark      11/29/07 - make it shared
    najain      03/12/07 - bug fix
    najain      03/12/07 - bug fix
    najain      03/02/07 - 
    hopark      02/19/07 - Creation
 */

/**
 *  @version $Header: ITimedTupleSinglyListNode.java 28-feb-2008.18:32:36 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal;

import oracle.cep.execution.ExecException;

/**
 * @version $Header: ITimedTupleSinglyListNode.java 28-feb-2008.18:32:36 hopark Exp $
 * @author parujain
 * @since release specific (what release of product did this appear in)
 */

public interface ITimedTupleSinglyListNode extends ITupleSinglyListNode
{
  void setTs(long ts)  throws ExecException;
  long getTs() throws ExecException;
  void setReaders(int n)  throws ExecException;
  int decrementAndGet() throws ExecException;
  int incrementAndGet() throws ExecException;
}
