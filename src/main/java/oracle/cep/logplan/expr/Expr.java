/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/Expr.java /main/8 2012/05/17 06:50:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Abstract Logical Operator Expression Class Definition

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    05/10/12 - add name/alias
 sborah      04/11/11 - getAllReferencedAttrs()
 skmishra    06/12/08 - getAttr impl
 skmishra    05/02/08 - adding unimplemented methods
 mthatte     04/28/08 - making getAttr abstract
 parujain    06/03/08 - check_refrence abstract
 udeshmuk    01/11/08 - add boolean flag to indicate if expr evaluates to null.
 parujain    11/06/07 - 
 mthatte     10/29/07 - adding onDemand flag
 rkomurav    11/30/06 - add abstract equals.method
 rkomurav    09/25/06 - expr in aggr
 najain      05/30/06 - add check_reference 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/Expr.java /main/8 2012/05/17 06:50:33 udeshmuk Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 * Abstract Logical Operator Expression Class Definition
 */
public abstract class Expr implements Cloneable {
  Datatype type;
  String alias;
  boolean isExternal = false; //Does this expression refer to an External relation?
  /**
   * boolean flag to indicate if expression evaluates to null
   */
  protected boolean bNull = false; 
  
  public boolean isExternal() {
    return isExternal;
  }

  public void setExternal(boolean isExternal) {
    this.isExternal = isExternal;
  }

  public Datatype getType() {
    return type;
  }

  public void setType(Datatype type) {
    this.type = type;
  }

  public void setbNull(boolean isNull)
  {
    this.bNull = isNull;  
  }
  
  public boolean isNull()
  {
    return this.bNull;
  }
  
  public String getAlias()
  {
    return alias;
  }
  
  public void setAlias(String alias)
  {
    this.alias = alias;
  }
  
  public Expr clone() throws CloneNotSupportedException {
    Expr exp = (Expr) super.clone();
    return exp;
  }
  
  /* only ExprAttr will override this */
  public Attr getAttr()
  {
    return new AttrUnNamed(getType());
  }
  
  public abstract boolean check_reference(LogOpt op);
  public abstract boolean equals(Object other);
  
  /**
   * Get set of all attributes referenced by this expression
   * <p>
   * Add to the supplied list a referenced attribute if and
   * only if it is not already there.
   * <p>
   * @param attrs list to which referenced attributes of specified type 
   *              are to be added, if not already present
   */
  public abstract void getAllReferencedAttrs(List<Attr> attrs);
  
  /**
   * Get set of all attributes of specified type referenced by this expression
   * <p>
   * Add to the supplied list a referenced attribute of specified type if and
   * only if it is not already there.
   * <p>
   * @param attrs list to which referenced attributes of specified type 
   *              are to be added, if not already present
   * @param includeAggrParams whether to include aggr params or not
   */
  public abstract void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams);
  
    
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<LogicalExpression>");
    sb.append("<Datatype type=\"" + type + "\" />");

    sb.append("</LogicalExpression>");
    return sb.toString();
  }

}
