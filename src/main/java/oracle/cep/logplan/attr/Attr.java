/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/Attr.java /main/3 2012/05/02 03:05:58 pkali Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Attribute definitions used by logical operators

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 pkali       04/03/12 - added datatype member
 udeshmuk    04/05/11 - propagate attrname
 najain      05/30/06 - add check_reference 
 najain      05/26/06 - add isSame 
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/attr/Attr.java /main/3 2012/05/02 03:05:58 pkali Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.attr;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;

/**
 * Attribute Class definitions used by logical operators
 */
public class Attr implements Cloneable {
  // Attribute kind
  AttrKind attrKind;

  //attribute name
  String name = null;
  
  //Attribute data type
  Datatype dt;

  // TODO: Not sure if this is needed, since the attrKind can always be
  // derived from the instance of the sub-class

  public AttrKind getAttrKind() {
    return attrKind;
  }

  public Attr clone() throws CloneNotSupportedException {
    Attr attr = (Attr) super.clone();
    return attr;
  }

  public String getActualName(){
    return this.name;
  }
  
  public void setActualName(String name)
  {
    this.name = name;
  }
  
  public Datatype getDatatype()
  {
    return this.dt;
  }
  
  public boolean isSame(Attr input) {
    if (attrKind != input.getAttrKind())
      return false;
    return true;
  }

  public boolean check_reference(LogOpt op) {
    // Should be implemented by the subclasses and never be called
    assert false;
    return false;
  }

  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<Attribute attrKind=\"" + attrKind + "\" />");

    return sb.toString();
  }

}
