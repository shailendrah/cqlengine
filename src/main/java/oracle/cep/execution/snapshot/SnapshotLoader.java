/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/SnapshotLoader.java hopark_cqlsnapshot/3 2016/02/26 11:55:08 hopark Exp $ */

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
    sbishnoi    12/21/15 - adding support for ha snapshot generation
    hopark      12/15/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/snapshot/SnapshotLoader.java hopark_cqlsnapshot/3 2016/02/26 11:55:08 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.snapshot;

import java.io.ObjectInputStream;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.service.ExecContext;

public class SnapshotLoader extends OperatorTraversal
{
	ObjectInputStream input;
	boolean fullSnapshot;
	public SnapshotLoader(ExecContext ec)
	{
		super(ec);
	}
	
	public void loadSnapshot(ObjectInputStream input, boolean fullSnapshot) throws CEPException
	{
		this.input = input;
		this.fullSnapshot = fullSnapshot;
		traverse();
	}

	protected void process(ExecOpt operator) throws CEPException
	{
	  LogUtil.fine(LoggerType.TRACE, "SnapshotLoader is processing operator: " + operator.getOptName());
		operator.loadSnapshot(input, fullSnapshot);
	}
}
