/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/expr/ExprXmlConcat.java /main/2 2011/05/17 03:26:06 anasrini Exp $ */

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
    skmishra    06/12/08 - cleaning equals
    skmishra    06/04/08 - bug
    skmishra    05/02/08 - adding unimplemented methods
    skmishra    05/02/08 - 
    mthatte     04/25/08 - Creation
 */
package oracle.cep.logplan.expr;

import java.util.List;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrUnNamed;

/**
 *  @version $Header: ExprXmlConcat.java 12-jun-2008.17:27:02 skmishra Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

public class ExprXmlConcat extends Expr implements Cloneable
{
  Expr[] concatExprs;

  
  public ExprXmlConcat(List<Expr> e)
  {
    super();
    this.concatExprs = new Expr[e.size()]; 
    this.concatExprs = (Expr[]) e.toArray(this.concatExprs);
  }
  
  public Expr[] getConcatExprs()
  {
    return this.concatExprs;
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs)
  {
    getAllReferencedAttrs(attrs, false);
  }

  @Override
  public void getAllReferencedAttrs(List<Attr> attrs, boolean includeAggrParams)
  {
    for(Expr e : concatExprs)
    {
      e.getAllReferencedAttrs(attrs, false);
    }
  }
  
  public boolean equals(Object other)
  {
    if(other == null)
      return false;
    if(this == other)
      return true;
    if(getClass()!=other.getClass())
      return false;
    
    ExprXmlConcat o = (ExprXmlConcat)other;
    
    for(int i = 0; i < concatExprs.length ; i++)
    {
        if(!concatExprs[i].equals(o.concatExprs[i]))
          return false;
    }
    return true;
  }

  public boolean check_reference(LogOpt op)
  {
    for(Expr e: concatExprs)
    {
    	if(!e.check_reference(op))
    		return false;
    }
    return true;
  }

  public Attr getAttr()
  {
    return new AttrUnNamed(getType());
  }

}