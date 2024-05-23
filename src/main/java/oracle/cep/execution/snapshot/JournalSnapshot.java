/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/JournalSnapshot.java hopark_cqlsnapshot/3 2016/02/26 11:55:08 hopark Exp $ */

/* Copyright (c) 2015, 2016, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/15/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/JournalSnapshot.java hopark_cqlsnapshot/3 2016/02/26 11:55:08 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.snapshot;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.service.ExecContext;

public class JournalSnapshot extends OperatorTraversal
{
	boolean start;
	
	public JournalSnapshot(ExecContext ec)
	{
		super(ec);
	}
	
	public void startBatch() throws CEPException
	{
		start = true;
		traverse();
	}
	
	public void endBatch() throws CEPException
	{
		start = false;
		traverse();
	}

	protected void process(ExecOpt operator) throws CEPException
	{
		if (start)
			operator.startBatch(false);
		else
			operator.endBatch();
	}
	
}
