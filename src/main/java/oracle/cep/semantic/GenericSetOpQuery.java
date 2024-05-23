/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/GenericSetOpQuery.java /main/5 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2011, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - set partitioned stream flag
    sbishnoi    12/20/13 - bug 17600010
    vikshukl    08/01/12 - archived dimension
    udeshmuk    07/12/11 - archived relation support
    vikshukl    03/11/11 - support for n-ary set operators
    vikshukl    03/11/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/GenericSetOpQuery.java /main/5 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.HashMap;

import oracle.cep.common.RelSetOp;
import oracle.cep.common.QueryType;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;


public class GenericSetOpQuery extends SemQuery
{
  private ArrayList<Expr>     selectExprs;
  private int                 left;        // id of the left table.
  private ArrayList<Integer>  operands;    // tables/views involved in this query
                                           // going from left to right.
  private ArrayList<RelSetOp> operators;
  private ArrayList<Boolean>  isUnionAll;
  private boolean isDependentOnArchivedReln;
  private boolean isDependentOnArchivedDim;
  private boolean isDependentOnPartnStream;
  
  /** A Map of table id to derived time specification 
   *  If a table (corresponding to tableid) is derived timestamped, then the
   *  map will return the derived timestamp specification for that table.
   */
  private HashMap<Integer, DerivedTimeSpec> mapTableIDDerivedTimeSpec;
        
  public GenericSetOpQuery()
  {
    selectExprs = new ExpandableArray<Expr>();
    operands        = new ArrayList<Integer>();     
    operators       = new ArrayList<RelSetOp>();
    isUnionAll      = new ArrayList<Boolean>();
    isDependentOnArchivedReln = false;
    isDependentOnArchivedDim  = false;
    isDependentOnPartnStream  = false;
    mapTableIDDerivedTimeSpec = new HashMap<Integer, DerivedTimeSpec>();
  }
        
  // See comments in SemQuery
  // We call this a streaming query/monotonic relation only if 
  // all operands evaluate to a stream.
  boolean isMonotonicRel(ExecContext ec) {
  boolean ret = false;
  int     rightTableId;   
  int     leftTableId  = symTable.lookupSource(left).getTableId();
    
  for (int i=0; i < operands.size(); i++) 
  {
    rightTableId = symTable.lookupSource(operands.get(i)).getTableId();
        
    try {
      if (ec.getSourceMgr().isStream(leftTableId) &&
          ec.getSourceMgr().isStream(rightTableId))
      {
        ret = true;
      }
      else {
        ret = false;
        break;
      }
    }
    catch (CEPException e) {
      ret = false;
    }
  }
  return ret;
  }
        
  /**
   * @return the operands
   */
  public ArrayList<Integer> getOperands() {
    return operands;
  }
        
  /**
   * @return the operators
   */
  public ArrayList<RelSetOp> getOperators() {
    return operators;
  }
        
  /**
   * @return the isUnionAll
   */
  public ArrayList<Boolean> getIsUnionAll() {
    return isUnionAll;
  }


  @Override
  public QueryType getQueryType() {
    return QueryType.NARY_OP;
  }

  @Override
  public ArrayList<Expr> getSelectListExprs() {
    return selectExprs;
  }

  public void setSelectListExprs( Expr[] sels) 
  {
    for(int i=0;  i<sels.length; i++)
      selectExprs.set(i, sels[i]);
  }
        
  @Override
  public int getSelectListSize() {
    return selectExprs.size();
  }       
        
  // return the id of the left table.
  public int getLeft()
  {
    return left;
  }

  /**
   * @param left the left to set
   */
  public void setLeft(int left) {
    this.left = left;
  }
        
  public void addOperand(int id)
  {
    operands.add(id);
  }
        
  public void addOperator(RelSetOp e)
  {
    operators.add(e);
  }

  /**
   * @param isUnionAll the isUnionAll to set
   */
  public void addIsUnionAll(Boolean unionAll) {
    isUnionAll.add(unionAll);
  }
  
  public void setIsDependentOnPartnStream(boolean flag)
  {
    this.isDependentOnPartnStream = flag;
  }
  
  public boolean isDependentOnPartnStream()
  {
    return this.isDependentOnPartnStream;
  }
        
  public void setIsDependentOnArchivedRelation(boolean flag)
  {
    this.isDependentOnArchivedReln = flag;
  }
  
  public boolean isDependentOnArchivedRelation()
  {
    return this.isDependentOnArchivedReln;
  }

  public void setIsDependentOnArchivedDimension(boolean flag)
  {
    this.isDependentOnArchivedDim = flag;
  }
  
  
  public boolean isDependentOnArchivedDimension() 
  {
    return this.isDependentOnArchivedDim;
  }
  
  public DerivedTimeSpec getDerivedTsSpec(int tableId)
  {
    return this.mapTableIDDerivedTimeSpec.get(tableId);
  }
  
  public void setDerivedTsSpec(int tableId, DerivedTimeSpec derivedTsSpec) {
    this.mapTableIDDerivedTimeSpec.put(tableId, derivedTsSpec);
  }
}
