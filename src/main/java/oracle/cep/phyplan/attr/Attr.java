/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/attr/Attr.java /main/7 2012/10/22 14:42:18 vikshukl Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Attribute definitions used by physical operators

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    08/17/12 - return fully qualified name
 vikshukl    08/28/12 - augment attr to return a fully qualifier name
 udeshmuk    06/15/12 - add datatype, needed for archived relation
 udeshmuk    06/22/11 - support getSQLEquivalent
 udeshmuk    04/05/11 - store attrname
 sborah      04/24/09 - add getSignature
 rkomurav    10/10/06 - add equals method
 rkomurav    08/23/06 - add getXMLPlan2
 anasrini    05/03/06 - add constructor 
 najain      02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/attr/Attr.java /main/7 2012/10/22 14:42:18 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.attr;

import oracle.cep.common.Datatype;
import oracle.cep.service.ExecContext;

/**
 *  Attribute Class definition used by physical operators
 */
public class Attr {
  /** The input operator that produces the attribute */
  int input;

  /** Position of the attribute in the output schema of the (input) operator */
  int pos;
  
  /** Actual name of the attribute */
  String name = "not set";
  
  /** datatype of the attr */
  Datatype type = null;

  /**
   * Default Constructor
   */
  public Attr() {
  }

  /**
   * Constructor with input and pos specified
   * @param input refers to the input operator that produces the attribute
   * @param pos position of the attribute in output schema of input operator
   */
  public Attr(int input, int pos) {
    this.input = input;
    this.pos   = pos;
  }

  public int getInput() {
    return input;
  }

  public int getPos() {
    return pos;
  }

  public void setInput(int input) {
    this.input = input;
  }

  public void setPos(int pos) {
    this.pos = pos;
  }
  
  public Datatype getType()
  {
    return this.type;
  }

  public void setType(Datatype dt)
  {
    this.type = dt;
  }
  
  public String getActualName()
  {
    return this.name;
  }
  
  public void setActualName(String name)
  {
    this.name = name;
  }
  
  public boolean equals(Object otherObject) {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    Attr other = (Attr)otherObject;
    return (input == other.getInput() && pos == other.getPos());
  }
  
  /**
   * Method to calculate a concise String representation
   * of the attribute based on its input and position value.
   * @return 
   *      A concise String representation of the attribute.
   */
  public String getSignature()
  {
    return  "(" + this.input + "," + this.pos + ")";
  }
  
  public String getSQLEquivalent(ExecContext ec)
  {
    if (name != null)
    {
      if(!ec.shouldReturnFullyQualifiedAttrName())
      {
        int idx = name.lastIndexOf(".");
        if(idx != -1)
          return name.substring(idx+1);
      }
      return name;
    }
    else
      return null;
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorAttribute>");

    sb.append("<InputOperator input=\"" + input + "\" />");
    sb.append("<Position pos=\"" + pos + "\" />");

    sb.append("</PhysicalOperatorAttribute>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML plan
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append("[");
    xml.append(input);
    xml.append(",");
    xml.append(pos);
    xml.append("]");
    return xml.toString();
  }

}
