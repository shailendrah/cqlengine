/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptUnion.java /main/3 2011/10/11 14:04:18 vikshukl Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    09/28/11 - subquery support
    sbishnoi    04/12/07 - add validate check for unbounded stream
    sbishnoi    04/04/07 - support for union all
    dlenkov     06/12/06 - Creation
 */

/**
 *  @version $Header: LogOptUnion.java 12-apr-2007.00:10:22 sbishnoi Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;

public class LogOptUnion extends LogOpt {
	
  private boolean isUnionAll;

  public LogOptUnion(LogOpt left, LogOpt right, boolean isUnionAll)
  {
    super(LogOptKind.LO_UNION);

    setIsStream(left.getIsStream() && right.getIsStream());

    // Schema is the schema of the left input (which should be identical
    // to the schema of the right input
    assert (left.getNumOutAttrs() == right.getNumOutAttrs());
    numOutAttrs = left.getNumOutAttrs();
    setOutAttrs(left.getOutAttrs());
	
    setNumInputs(2);
    setInput(0, left);
    setInput(1, right);
    setIsUnionAll(isUnionAll);
    
    setOutput(null);
	
    left.setOutput(this);
    right.setOutput(this);
  }

  public LogOptUnion() {
    super();
  }
  
  public boolean isUnionAll() {
	return isUnionAll;
  }
  
  public void setIsUnionAll(boolean isUnionAll) {
	this.isUnionAll = isUnionAll;  
  }
  
  @Override
  protected void validate() throws LogicalPlanException
  {
	if(!this.isUnionAll())
	  checkUnboundStream();	
  }
}
