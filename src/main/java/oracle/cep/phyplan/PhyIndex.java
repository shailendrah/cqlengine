/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyIndex.java /main/3 2008/12/10 18:55:56 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      08/01/07 - add getInst
    hopark      06/15/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyIndex.java /main/3 2008/12/10 18:55:56 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import oracle.cep.execution.indexes.Index;
import oracle.cep.logging.ILoggable;
import oracle.cep.planmgr.IPlanVisitable;
import oracle.cep.planmgr.IPlanVisitor;

public class PhyIndex implements IPlanVisitable
{
  Index m_index;
  
  PhyIndex(Index idx)
  {
    m_index = idx;
    assert (m_index instanceof ILoggable);
  }
  
  public int getId() {return m_index.getId();}

  public ILoggable getInst() {return (ILoggable) m_index;}
  
  /**
  *
  * @param visitor
  */
 public void accept(IPlanVisitor visitor) 
 {
   visitor.visit(this);    
 }
}