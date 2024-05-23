/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprXmltype.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

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
    sborah      04/11/11 - override getAllReferencedAttrs()
    skmishra    06/05/08 - cleanup
    skmishra    05/16/08 - changing representation to node
    skmishra    05/12/08 - Creation
 */

package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

import org.w3c.dom.Node;
/**
 *  @version $Header: ExprXmltype.java 05-jun-2008.17:33:23 skmishra Exp $
 *  @author  skmishra
 *  @since   release specific (what release of product did this appear in)
 */

public class ExprXmltype extends Expr
{

  Node item;
  
  public ExprXmltype(Node i)
  {
    setType(Datatype.XMLTYPE);
    item = i;
  }
  
  /**returns item without checking */
  public Node getValue()
  {
    return item;
  }
  
  public boolean check_reference(LogOpt op)
  {
    return true;
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs, true);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    attrs.add(getAttr());
  }

  public boolean equals(Object otherObject)
  {
    if (this == otherObject)
      return true;

    if (otherObject == null)
      return false;

    if (getClass() != otherObject.getClass())
      return false;
    
    ExprXmltype other = (ExprXmltype)otherObject;
    return item.equals(other.getValue()); 
  }
  
  public Attr getAttr()
  {
    AttrUnNamed attr = new AttrUnNamed(getType());
    return (Attr) attr;
  }
}
