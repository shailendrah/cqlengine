/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrUnNamed.java /main/2 2012/05/02 03:05:59 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Unnamed Attribute Class definitions used by logical operators

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 pkali       04/03/12 - moved datatype member to Attr class
 pkali       03/27/12 - removed the type checking in equals method
 najain      05/30/06 - add check_reference 
 najain      05/26/06 - add isSame 
 najain      02/14/06 - add constructor etc. 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/AttrUnNamed.java /main/2 2012/05/02 03:05:59 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.attr;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;

/**
 * Unnamed Attribute Class definitions used by logical operators
 */
public class AttrUnNamed extends Attr {

  public AttrUnNamed() {
    attrKind  = AttrKind.UNNAMED;
  }

  public AttrUnNamed(Datatype type) {
    this.dt = type;
    attrKind  = AttrKind.UNNAMED;
  }

  public boolean equals(Object otherObject)
  {
    if (this == otherObject) return true;

    if (otherObject == null) return false;

    if (getClass() != otherObject.getClass())
      return false;

    //If both objects are different and type is same is it equal?
    //AttrUnNamed other = (AttrUnNamed)otherObject;

    //return (type == other.type);
    return false;
  }
  
  public Datatype getType() {
    return this.dt;
  }

  public void setType(Datatype type) {
    this.dt = type;
  }

  public boolean isSame(Attr input) {
    return false;
  }

  public boolean check_reference(LogOpt op) {
    return false;
  }

}
