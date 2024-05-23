/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SetOpSubquery.java /main/3 2014/10/14 06:35:33 udeshmuk Exp $ */

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
    sbishnoi    09/18/14 - overriding isDependentOnPartnStream
    vikshukl    08/01/12 - archived dimension
    vikshukl    09/26/11 - subquery support
    vikshukl    09/26/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SetOpSubquery.java /main/3 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.ArrayList;

import oracle.cep.common.RelSetOp;
import oracle.cep.common.QueryType;
import oracle.cep.dataStructures.internal.ExpandableArray;
import oracle.cep.service.ExecContext;

/**
 * Post semantic analysis representation for a query with a set operation
 *
 * @since 1.0
 */

public class SetOpSubquery extends SemQuery {

  private RelSetOp        relSetKind;
  private ArrayList<Expr> selectExprs;
  private SemQuery        left;
  private SemQuery        right;
  private boolean         isUnionAll;
  private boolean         isDependentOnArchivedReln;
  private boolean         isDependentOnArchivedDim;
  private boolean         isDependentOnPartnStream;
  
  /* 
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
  public SetOpSubquery() {
    super();
    selectExprs               = new ExpandableArray<Expr>();
    r2sop                     = null;
    this.isUnionAll           = false;
    this.leftComparisonAttrs  = null;
    this.rightComparisonAttrs = null;
    this.numComparisonAttrs   = 0;
    this.isDependentOnArchivedReln = false;
    this.isDependentOnArchivedDim = false;
    this.isDependentOnPartnStream = false;
  }

  /** TODO: deprecate one of these constructors */
  public SetOpSubquery(SemQuery left, 
                       RelSetOp kind,
                       SemQuery right)
  {
    this.left = left;
    relSetKind = kind;
    this.right = right;
  }

  public QueryType getQueryType() {
    return QueryType.SET_SUBQUERY;
  }

  public RelSetOp getRelSetOp() {
    return relSetKind;
  }

  public void setRelSetOp(RelSetOp op)
  {
    this.relSetKind = op;
  }
  
  boolean isMonotonicRel(ExecContext ec) 
  {
    return left.isStreamQuery() && right.isStreamQuery(); 
  }

  // Called in QueryManager

  public ArrayList<Expr> getSelectListExprs() {
    return selectExprs;
  }
  
  public int getSelectListSize()
  {
    return selectExprs.size();
  }

  public void setSelectListExprs(Expr[] sel) 
  {
    for(int i=0;  i<sel.length; i++)
      selectExprs.set(i, sel[i]);
  }

  /**
   * @return the left operand
   */
  public SemQuery getLeft() {
    return left;
  }

  /**
   * @param left left operand to set
   */
  public void setLeft(SemQuery left) {
    this.left = left;
  }

  /**
   * @return the right
   */
  public SemQuery getRight() {
    return right;
  }

  /**
   * @param rightVarId the rightVarId to set
   */
  public void setRight(SemQuery right) {
    this.right = right;
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
  
  public void setIsDependentOnArchivedRelation(boolean flag)
  {
    this.isDependentOnArchivedReln = flag;
  } 
  
  public void setIsDependentOnArchivedDim(boolean flag)
  {
    this.isDependentOnArchivedDim = flag;
  }
  
  public boolean isDependentOnArchivedRelation()
  {
    return isDependentOnArchivedReln;  
  }

  public boolean isDependentOnArchivedDimension() 
  {
    return isDependentOnArchivedDim;
  }

  public void setIsDependentOnPartnStream(boolean flag)
  {
    this.isDependentOnPartnStream = flag;  
  }
  
  public boolean isDependentOnPartnStream()
  {
    return isDependentOnPartnStream;
  }
}
