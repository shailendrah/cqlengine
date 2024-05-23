/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XMLConcatExpr.java /main/3 2012/07/30 19:52:55 pkali Exp $ */

/* Copyright (c) 2008, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       07/17/12 - meta data propagation for rewritten groupby expr
    pkali       05/09/12 - added getRewrittenExprForGroupBy method
    skmishra    06/12/08 - exprtype
    skmishra    06/06/08 - cleanup
    skmishra    05/02/08 - 
    mthatte     04/23/08 - Creation
 */

package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.common.Datatype;


/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XMLConcatExpr.java /main/3 2012/07/30 19:52:55 pkali Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

public class XMLConcatExpr extends Expr
{
  
  Expr[] concatExprs;
  
  public XMLConcatExpr(List<Expr> args)
  {
    this.concatExprs = (Expr[])args.toArray(new Expr[args.size()]);
  }
  
  public boolean equals(Object other)
  {
    
    if(other==null)
      return false;
    if(this==other)
      return true;
    if(getClass() != other.getClass())
      return false;
    
    XMLConcatExpr o = (XMLConcatExpr)other;
    if(this.concatExprs.length != o.concatExprs.length)
      return false;
    
    for(int i=0;i<concatExprs.length;i++)
    {
      if(!concatExprs[i].equals(o.concatExprs[i]))
        return false;
    }
      
    return true;
  }
  
  public Expr[] getConcatExprs()
  {
    return this.concatExprs;
  }
  

  public void getAllReferencedAggrs(List<AggrExpr> aggrs)
  {
    //Call the method on all children
    for(Expr e:concatExprs)
      e.getAllReferencedAggrs(aggrs);
  }

  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type)
  {
    getAllReferencedAttrs(attrs, type, false);
  }

  public void getAllReferencedAttrs(List<Attr> attrs, SemAttrType type,
      boolean includeAggrParams)
  {
    for(Expr e:concatExprs)
    {
      e.getAllReferencedAttrs(attrs, type, false);
    }
  }

  public Expr getRewrittenExprForGroupBy(Expr[] gbyExprs)
  {
    List<Expr> rwConcatExprs = new ArrayList<Expr>();
    for(int i=0;i<concatExprs.length;i++)
    {
      Expr rwConcatExpr = concatExprs[i].getRewrittenExprForGroupBy(gbyExprs);
      if(rwConcatExpr == null)
        return null;
      rwConcatExprs.add(rwConcatExpr);
    }
    XMLConcatExpr xmlConcatExpr = new XMLConcatExpr(rwConcatExprs);
    xmlConcatExpr.setName(this.getName(), 
                          this.isUserSpecifiedName(), this.isExternal());
    xmlConcatExpr.setAlias(this.getAlias());
    xmlConcatExpr.setbNull(this.isNull());
    return xmlConcatExpr;
  }
  
  public ExprType getExprType()
  {
    return ExprType.E_XML_CONCAT_EXPR;
  }

  public Datatype getReturnType()
  {
    return Datatype.XMLTYPE;
  }
  
}