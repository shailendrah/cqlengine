/* $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/Sink.java /main/5 2008/10/24 15:50:18 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 Declares Sink in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 hopark    10/09/08 - remove statics
 parujain  10/04/07 - delete op
 parujain  06/26/07 - mutable state
 najain    03/12/07 - bug fix
 skaluska  03/14/06 - query manager 
 skaluska  02/06/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/operators/Sink.java /main/5 2008/10/24 15:50:18 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.operators;

import oracle.cep.service.ExecContext;

/**
 * Sink
 * 
 * @author skaluska
 */
public class Sink extends ExecOpt
{

  /**
   * Constructor for Sink
   * @param ec TODO
   */
  Sink(ExecContext ec)
  {
    super(ExecOptType.EXEC_SINK, null, ec);
  }

  /*
   * (non-Javadoc)
   * 
   * @see oracle.cep.execution.operators.ExecOpt#run(int)
   */
  @Override
  public int run(int timeslice)
  {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see oracle.cep.execution.operators.ExecOpt#deleteOp()
   */
  @Override
  public void deleteOp()
  {
    // TODO Auto-generated method stub
    
  }

}
