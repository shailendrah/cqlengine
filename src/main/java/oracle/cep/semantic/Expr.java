/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/Expr.java /main/14 2012/05/17 06:50:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Base interface for the different types of expressions

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/11/12 - add alias field
    pkali       04/17/12 - added getRewrittenExprForGroupBy method
    sbishnoi    08/27/11 - adding support for interval year to month
    sborah      06/17/09 - support for BigDecimal
    sborah      05/05/09 - add case for boolean in getExpectedExpr()
    skmishra    06/06/08 - adding ConstXmltypeExpr to getExpectedExpr()
    udeshmuk    02/20/08 - add the common getExpectedExpr method
    udeshmuk    01/11/08 - add boolean flag to indicate null.
    parujain    11/09/07 - external source
    parujain    10/25/07 - db join
    rkomurav    06/25/07 - cleanup
    rkomurav    05/11/07 - add isClassB
    rkomurav    05/28/07 - make getAllReferenced* abstract
    anasrini    05/27/07 - add getAllReferencedAggs, getAllReferencedAttrs
    parujain    10/12/06 - return type from Semantic layer
    anasrini    08/29/06 - 
    najain      08/28/06 - add name
    anasrini    02/23/06 - add javadoc comments 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/Expr.java /main/14 2012/05/17 06:50:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.math.BigDecimal;
import java.util.List;

import oracle.cep.common.Datatype;

/**
 * Post semantic analysis representation of an expression
 * <p>
 * This is the base interface for the different types of expressions
 *
 * @since 1.0
 */

public abstract class Expr {

  private String  name;
  //This field will have the alias value if it is provided in CQL query.
  //Else this field will be null.
  private String  alias = null;
  private boolean isUserSpecified;
  protected Datatype dt;
  protected boolean isExternal = false;
  /**
   * boolean flag to indicate if the expression has null value
   */
  protected boolean bNull = false;

  /**
   * Get the name of the expression
   * @return the name of the expression
   */
  public String getName()
  {
    return name;
  }

  /**
   * Is the name explicitly specified by the user
   * @return true if and only if the name is explicitly specified by the user
   */
  public boolean isUserSpecifiedName() {
    return isUserSpecified;
  }
  
  public String getAlias()
  {
    return this.alias;
  }
  
  public void setAlias(String alias)
  {
    this.alias = alias;
  }
  
  /**
   * Is the expression using an External Relation
   * @return true if and only if the expression is using an External Relation attribute
   */
  public boolean isExternal()
  {
    return isExternal;
  }
  
  public void setIsExternal(boolean is)
  {
    this.isExternal = is;
  }

  /**
   * Set the name of the expression
   * @param name the name of the expression
   * @param isUserSpecified is this name explicitly specified by the user
   *                        or is it implicitly derived
   *
   */
  public void setName(String name, boolean isUserSpecified, boolean external)
  {
    this.name            = name;
    this.isUserSpecified = isUserSpecified;
    this.isExternal      = external;
  }

  /**
   * Set the name of the expression
   * @param name the name of the expression
   * @param isUserSpecified is this name explicitly specified by the user
   *                        or is it implicitly derived
   *
   */
  public void setName(String name, boolean isUserSpecified)
  {
    this.name            = name;
    this.isUserSpecified = isUserSpecified;
  }
  
  /**
   * set the bNULL flag
   * @param isNull true when expr evaluates to null value
   */
  public void setbNull(boolean isNull)
  {
    this.bNull = isNull;
  }
  
  /**
   * check whether the expression evaluates to null 
   */
  public boolean isNull()
  {
    return this.bNull;
  }
  
  /**
   * Get the class of the expression
   * @return the class of the expression
   */
  public abstract ExprType getExprType();

  /**
   * Get the datatype that this expression evaluates to
   * @return the datatype that this expression evaluates to
   */
  public abstract Datatype getReturnType();
  
  /**
   * Get set of all aggregate expressions referenced by this expression
   * <p>
   * Add to the supplied list a referenced aggregate expression if and
   * only if it is not already there.
   * <p>
   * @param aggrs list to which referenced aggregate expressions are to be
   *              added, if not already present
   */
  public abstract void getAllReferencedAggrs(List<AggrExpr> aggrs);

  /**
   * Rewrites the expression as GroupByExpr if it is found in the 
   * GroupBy clause
   * <p>Default implementation returns the same expression</p>
   * @param gbyExprs expressions in the GroupBy clause
   */
  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    return this;
  }
  
  /**
   * Get set of all attributes of specified type referenced by this expression
   * <p>
   * Add to the supplied list a referenced attribute of specified type if and
   * only if it is not already there.
   * <p>
   * @param attrs list to which referenced attributes of specified type 
   *              are to be added, if not already present
   * @param type semattr type
   */
  public abstract void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type);
  
  /**
   * Get set of all attributes of specified type referenced by this expression
   * <p>
   * Add to the supplied list a referenced attribute of specified type if and
   * only if it is not already there.
   * <p>
   * @param attrs list to which referenced attributes of specified type 
   *              are to be added, if not already present
   * @param type semattr type
   * @param includeAggrParams whether to include aggr params or not
   */
  public abstract void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
                                             boolean includeAggrParams);
    
  public abstract boolean equals(Object other);
  
  /**
   * Called from variety of places.
   * Primary used in replacing the ConstNullExpr by a const expr of some type 
   * with null value as later layers such as logical, physical etc. don't handle 
   * ConstNullExpr
   * @param the datatype of which const expr is desired
   * @return a const expr of input datatype with bNull flag set to true.
   */
  public static Expr getExpectedExpr(Datatype todt)
  {
    Expr expectedExpr;
    switch(todt.getKind())
    {
      case INT: 
        expectedExpr = new ConstIntExpr(0);
        break;
      case BIGINT:
        expectedExpr = new ConstBigintExpr(0);
        break;
      case FLOAT:
        expectedExpr = new ConstFloatExpr(0);
        break;
      case DOUBLE:
        expectedExpr = new ConstDoubleExpr(0);
        break;
      case BIGDECIMAL:
        expectedExpr = new ConstBigDecimalExpr(BigDecimal.ZERO);
        break;
      case INTERVAL:
        expectedExpr = new ConstIntervalExpr(0, false);
        break;
      case INTERVALYM:
        expectedExpr = new ConstIntervalExpr(0, true);
        break;
      case CHAR:
        expectedExpr = new ConstCharExpr("");
        break;
      case BYTE:
        expectedExpr = new ConstByteExpr(new byte[]{0});
        break;
      case TIMESTAMP:
        expectedExpr = new ConstTimestampExpr(0);
        break;
      case XMLTYPE:
        expectedExpr = new ConstXmltypeExpr(null);
        break;
      case BOOLEAN:
        expectedExpr = new ConstBooleanExpr(false);
        break;
      default:
        expectedExpr = null;
        assert false: todt;
    }
    expectedExpr.setbNull(true);
    return expectedExpr;
  }
	
}
