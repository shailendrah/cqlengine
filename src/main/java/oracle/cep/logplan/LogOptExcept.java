/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptExcept.java /main/3 2009/02/23 06:47:35 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      12/16/08 - handle constants
    hopark      12/06/06 - add validate
    dlenkov     06/12/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptExcept.java /main/3 2009/02/23 06:47:35 sborah Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;


public class LogOptExcept extends LogOpt {

  public LogOptExcept( LogOpt left, LogOpt right) {

    super( LogOptKind.LO_EXCEPT);

    // Always false
    setIsStream( false);

    // Schema is the schema of the left input (whcih should be identical
    // to the schema of the right input
    assert (left.getNumOutAttrs() == right.getNumOutAttrs());
    numOutAttrs = left.getNumOutAttrs();
    setOutAttrs( left.getOutAttrs());

    setNumInputs( 2);
    setInput( 0, left);
    setInput( 1, right);

    setOutput( null);
	
    left.setOutput( this);
    right.setOutput( this);
  }

  @Override
  protected void validate() throws LogicalPlanException
  {
    checkUnboundStream();
  }

  public LogOptExcept() {
    super();
  }
}
