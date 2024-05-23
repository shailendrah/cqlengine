
/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmltype.java /main/6 2011/07/09 08:53:44 udeshmuk Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    06/20/11 - support getSQLEquivalent
 udeshmuk    11/08/09 - API to get all referenced attrs
 sborah      04/20/09 - define getSignature
 hopark      10/10/08 - remove statics
 skmishra    06/05/08 - cleanup
 skmishra    05/15/08 - changing toString()
 skmishra    05/13/08 - Creation
 */
package oracle.cep.phyplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.execution.xml.XMLItem;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLNode;

import org.w3c.dom.Node;


/**
 * @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/expr/ExprXmltype.java /main/4 2009/12/03 21:27:59 udeshmuk Exp $
 * @author skmishra
 * @since release specific (what release of product did this appear in)
 */

public class ExprXmltype extends Expr
{
  Node node;

  public ExprXmltype(Node _node)
  {
    super(ExprKind.CONST_VAL);
    setType(Datatype.XMLTYPE);
    this.node = _node;
  }
  
  public Node getValue()
  {
    return node;
  }

  public void setValue(Node xn)
  {
    this.node = xn;
  }

   /**
   * Method to calculate a concise String representation
   * of the Expression. The String is built recursively by calling 
   * this method on each type of Expr object which forms the expression.
   * @return 
   *      The value of the Expr in String format
   */
  public String getSignature()
  {
    return XMLItem.XMLNodetoString((XMLNode)this.getValue());
  }
  
  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;

    ExprXmltype other = (ExprXmltype) otherObject;
    Node _node = other.getValue();
    return _node.equals(node);
  }

  // toString method override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalOperatorXmltypeExpression>");
    sb.append(super.toString());
    sb.append("<Value>");
    sb.append("<![CDATA[");
    sb.append(XMLItem.XMLNodetoString((XMLNode)node));
    sb.append("]]>");
    sb.append("</PhysicalOperatorXmltypeExpression>");
    return sb.toString();
  }

  // Generate and return visualiser compatible XML plan
  public String getXMLPlan2()
  {
    return XMLItem.XMLNodetoString((XMLNode)node);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    return;
  }

  @Override
  public String getSQLEquivalent(ExecContext ec)
  {
    // TODO Auto-generated method stub
    return null;
  }

}
