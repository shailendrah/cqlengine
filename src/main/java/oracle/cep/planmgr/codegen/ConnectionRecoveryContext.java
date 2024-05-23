package oracle.cep.planmgr.codegen;

import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.extensibility.datasource.IExternalConnection;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement;
import oracle.cep.extensibility.datasource.Predicate;
import oracle.cep.interfaces.input.ExtSource;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ConnectionRecoveryContext.java /main/1 2015/11/04 04:57:19 udeshmuk Exp $ */

/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/31/15 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ConnectionRecoveryContext.java /main/1 2015/11/04 04:57:19 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

/**
 * This context will be used by external relations (ExternalSynopsisImpl) for
 * recovering the connection in case the existing connection is broken.
 * Connection can get broken due to various issues such as db shutdown, 
 * firewall killing idle connections etc. 
 */

public class ConnectionRecoveryContext 
{
  //Exec context
  ExecContext ec;
  
  //Predicate
  Predicate pred;
  
  //Join operator
  PhyOpt op;
  
  //PreparedInstr used for stmtEval in ExternalSynopsisImpl
  AInstr preparedInstr;

  //External source  
  ExtSource extSource;
  
  //Relation name
  String tableName;
  
  //relation attrs
  List<String> relAttrs;
  
  //retry interval for obtaining connection - hard-coded for now.
  long retryInterval = 3000l;
  
  public ConnectionRecoveryContext(Predicate pred,
	PhyOpt op, AInstr preparedInstr)
  {
	//this.ec = ec;
	this.pred = pred;
	this.op = op;
	this.preparedInstr = preparedInstr;
	this.extSource = null;
  }
  
  public ExtSource getExtSource() {
	return extSource;
  }

  public void setExtSource(ExtSource extSource) {
	if(extSource != null)
	  this.extSource = extSource;
  }
  
  public String getTableName() {
	return tableName;
}

public void setTableName(String tableName) {
	this.tableName = tableName;
}

public List<String> getRelAttrs() {
	return relAttrs;
}

public void setRelAttrs(List<String> relAttrs) {
	this.relAttrs = relAttrs;
}

public ExecContext getEc() {
	return ec;
}

public void setEc(ExecContext ec) {
	this.ec = ec;
}

public Predicate getPred() {
	return pred;
}

public void setPred(Predicate pred) {
	this.pred = pred;
}

public PhyOpt getOp() {
	return op;
}

public void setOp(PhyOpt op) {
	this.op = op;
}

public AInstr getPreparedInstr() {
	return preparedInstr;
}

public void setPreparedInstr(AInstr preparedInstr) {
	this.preparedInstr = preparedInstr;
}

/**
   * This method will keep trying until it gets a valid connection
   * to external source.
   * @return
   */
  public void renewConnection() throws CEPException
  {
	IExternalConnection conn = null;
	
	do
	{
	  try
	  {
		LogUtil.warning(LoggerType.TRACE, "Waiting for "+retryInterval+" milliseconds " +
		  "before attempting for a new connection..");
		Thread.currentThread().sleep(retryInterval);
		conn = extSource.createConnection(false);
	    if(conn == null) 
	    {
          continue;		   
	    }
	  }
	  catch (InterruptedException e) {
		LogUtil.warning(LoggerType.TRACE, "Interrupted while trying to renew connection");
		e.printStackTrace();
		break;
	  }	
    }while(conn == null);
	
	
  }
  
  public IExternalPreparedStatement renewPrepStmt() throws CEPException
  {
	IExternalPreparedStatement pStmt = null;
	
	do
	{
	  try{
		pStmt = ExternalQryHelper.getPreparedStmt(op, ec, pred, this, tableName, relAttrs);
	  } catch (CEPException e) {
		//getPreparedStmt throws error when connection is not present in ExtSource,
		//or when runaway predicate is not supported or when a particular predicate
	    //is not supported. None of these is possible when it is being called from 
		//this method. 
		//Only error scenario that can be thought of is when connection obtained by
		//renewConnection() becomes defunct (due to any issue like db shutdown) before
	    //prepStmt is renewed. So calling renewConnection() here.
		LogUtil.warning(LoggerType.TRACE, "Error in renewing prepared stmt. "+e.getMessage()+" Retrying ..");
		renewConnection();
	  }
	}while(pStmt == null);
	
	//update instr in stmtEval
	preparedInstr.extrInstr.setPreparedStmt(pStmt);
	return pStmt;
  }
}
