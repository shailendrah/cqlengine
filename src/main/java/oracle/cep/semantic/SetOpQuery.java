/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SetOpQuery.java /main/12 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    Post semantic analysis representation for a query with a set operation

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - set partitioned stream flag
    vikshukl    08/01/12 - archived dimension
    udeshmuk    07/12/11 - archived relation support
    sborah      11/21/08 - handle constants
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    udeshmuk    10/30/07 - add setLeftAttrs method and remove the earlier
                           methods for this task.
    udeshmuk    10/20/07 - add field leftAttrs for IN functionality.
    sbishnoi    09/04/07 - support for notin set operation
    anasrini    05/22/07 - symbol table reorg
    sbishnoi    04/04/07 - support for union all
    rkomurav    03/05/07 - implement gettableid in semquery
    rkomurav    02/22/07 - cleanup reftables.
    dlenkov     06/07/06 - fixed bin types
    najain      05/12/06 - add getSelectListExprs 
    anasrini    02/21/06 - make it a class 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SetOpQuery.java /main/12 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.ArrayList;

import oracle.cep.common.RelSetOp;
import oracle.cep.common.QueryType;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

/**
 * Post semantic analysis representation for a query with a set operation
 *
 * @since 1.0
 */

public class SetOpQuery extends SemQuery {

  private RelSetOp        relSetKind;
  private ArrayList<Expr> selectExprs;
  private int             leftVarId;
  private int             rightVarId;
  private boolean         isUnionAll;
  private boolean         isDependentOnArchivedReln;
  private boolean         isDependentOnArchivedDim;
  private boolean         isDependentOnPartnStream;

  /** 
   * V1(c1 integer, c2 float, c3 integer) IN / NOT IN V2(d1 integer, c2 float)
   * Here leftComparisonAttrs = {V1.c2}
   * and rightComparisonAttrs = {V2.c2}
   */
  private Attr[]         leftComparisonAttrs;
  private Attr[]         rightComparisonAttrs;
  private int            numComparisonAttrs;
  /*
   * leftAttrs will be used for IN operation. 
   * It will contain all the operators in the left relation and this will be
   * treated as the comparison attributes list for the outer LopOptMinus operator. 
   */
  private Attr[]         leftAttrs;

  // Constructors

  public SetOpQuery() {
    super();
  }

  public SetOpQuery( RelSetOp kind) {
    relSetKind                = kind;
    selectExprs               = new ExpandableArray<Expr>();
    r2sop                     = null;
    this.isUnionAll           = false;
    this.leftComparisonAttrs  = null;
    this.rightComparisonAttrs = null;
    this.numComparisonAttrs   = 0;
    this.isDependentOnArchivedReln = false;
    this.isDependentOnArchivedDim  = false;
    this.isDependentOnPartnStream = false;
  }

  // Getter methods

  public QueryType getQueryType() {
    return QueryType.BINARY_OP;
  }

  public RelSetOp getRelSetOp() {
    return relSetKind;
  }

  boolean isMonotonicRel(ExecContext ec) {

    boolean ret;
    int     leftTableId  = symTable.lookupSource(leftVarId).getTableId();
    int     rightTableId = symTable.lookupSource(rightVarId).getTableId();

    try {
      ret = ec.getSourceMgr().isStream(leftTableId) &&
        ec.getSourceMgr().isStream(rightTableId);
    }
    catch (CEPException e) {
      ret = false;
    }
    return ret;
  }

  // Called in QueryManager

  public ArrayList<Expr> getSelectListExprs() {
    return selectExprs;
  }
  
  public int getSelectListSize()
  {
    return selectExprs.size();
  }

  public void setSelectListExprs( Expr[] sels) 
  {
    for(int i=0;  i<sels.length; i++)
      selectExprs.set(i, sels[i]);
  }

  /**
   * @return the leftVarId
   */
  public int getLeftVarId() {
    return leftVarId;
  }

  /**
   * @param leftVarId the leftVarId to set
   */
  public void setLeftVarId(int leftVarId) {
    this.leftVarId = leftVarId;
  }

  /**
   * @return the rightVarId
   */
  public int getRightVarid() {
    return rightVarId;
  }

  /**
   * @param rightVarId the rightVarId to set
   */
  public void setRightVarid(int rightVarId) {
    this.rightVarId = rightVarId;
  }

  /**
   * Check for 'union all'
   * @return true if operation is 'union all'
   */
  public boolean isUnionAll() {
    return isUnionAll;
  }

  /**
   * set flag if operation is 'union all'
   * @param isUnionAll will be true if operation is 'union all'
   */
  public void setIsUnionAll(boolean isUnionAll) {
    this.isUnionAll = isUnionAll;
  }
  
  /**
   * Create Array of Comparison Attributes
   * @param size is number of comparable attributes 
   */
  public void setComparisonAttrs(int size)
  {
    this.leftComparisonAttrs  = new Attr[size];
    this.rightComparisonAttrs = new Attr[size];
  }
  
  /**
   * Add left and right Comparison Attributes to their respective Arrays  
   * @param leftComparisonAttr is left Comparison Attribute
   * @param rightComparisonAttr is right Comparison Attribute
   */
  public void addComparisonAttr(Attr leftComparisonAttr, Attr rightComparisonAttr)
  {
    this.leftComparisonAttrs[numComparisonAttrs]  = leftComparisonAttr;
    this.rightComparisonAttrs[numComparisonAttrs] = rightComparisonAttr;
    numComparisonAttrs++;
  }
  
  /**
   * Get Left Comparison Attributes
   * @return left Comparison Attribute Array
   */
  public Attr[] getLeftComparisonAttrs()
  {
    return this.leftComparisonAttrs;
  }
  
  /**
   * Get Right Comparison Attributes
   * @return right Comparison Attribute array
   */
  public Attr[] getRightComparisonAttrs()
  {
    return this.rightComparisonAttrs;
  }
  
  /**
   * Get number of comparison Attributes
   * @return number of Comparison Attributes
   */
  public int getNumComparisonAttrs()
  {
    return this.numComparisonAttrs;
  }
  
  /**
   * Get left attrs. Will be used for IN only.
   * @return leftAttrs array
   */
  public Attr[] getLeftAttrs()
  {
    return this.leftAttrs;
  }
  
  /**
   * Set left attrs. Done for all setops but currently used for IN only.
   * @param attrs array of attrs in left relation.
   */
  public void setLeftAttrs(Attr[] attrs)
  {
    this.leftAttrs = new Attr[attrs.length];
    for (int i = 0; i < attrs.length; i++)
    {
      this.leftAttrs[i] = attrs[i];
    }
  }
  
  public void setIsDependentOnPartnStream(boolean flag)
  {
    this.isDependentOnPartnStream = flag;
  }
  
  public void setIsDependentOnArchivedRelation(boolean flag)
  {
    this.isDependentOnArchivedReln = flag;
  } 
  
  public void setIsDependentOnArchivedDimension(boolean flag)
  {
    this.isDependentOnArchivedDim = flag;
  } 
  
  public boolean isDependentOnPartnStream()
  {
    return isDependentOnPartnStream;  
  }
  
  public boolean isDependentOnArchivedRelation()
  {
    return isDependentOnArchivedReln;  
  }

  public boolean isDependentOnArchivedDimension() {
    return isDependentOnArchivedDim;
  }
}
