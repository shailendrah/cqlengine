/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprXmlParse.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

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
    sborah     04/11/11 - override getAllReferencedAttrs()
    skmishra   06/12/08 - cleaning equals
    skmishra   06/03/08 - 
    mthatte    05/19/08 - Creation
 */

package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.common.XMLParseKind;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 *  @version $Header: ExprXmlParse.java 12-jun-2008.17:27:02 skmishra Exp $
 *  @author  mthatte
 *  @since   release specific (what release of product did this appear in)
 */

public class ExprXmlParse extends Expr
{
  Expr value;
  boolean isWellformed;
  XMLParseKind kind;
   
  public ExprXmlParse(Expr value, boolean _isWF, XMLParseKind _kind)
  {
    super();
    this.value = value;
    this.isWellformed = _isWF;
    this.kind = _kind;
    setType(Datatype.XMLTYPE);
  }

  public boolean isWellformed()
  {
    return isWellformed;
  }

  public void setWellformed(boolean isWellformed)
  {
    this.isWellformed = isWellformed;
  }

  public XMLParseKind getKind()
  {
    return kind;
  }

  public void setKind(XMLParseKind kind)
  {
    this.kind = kind;
  }

  public Expr getValue()
  {
    return value;
  }
  
  public void setValue(Expr _val)
  {
    this.value = _val;
  }

  public boolean check_reference(LogOpt op)
  {
    return value.check_reference(op);
  }
  
  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    value.getAllReferencedAttrs(attrs);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    value.getAllReferencedAttrs(attrs, includeAggrParams);
  }

  public boolean equals(Object other)
  {
    if(other == null)
      return false;
    if(this==other)
      return true;
    if(getClass()!=other.getClass())
      return false;
    
    ExprXmlParse o = (ExprXmlParse) other;
    return value.equals(o.getValue());
  }

  public Attr getAttr()
  {
    return new AttrUnNamed(getType());
  }
}