package oracle.cep.semantic;

/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SymbolTableConstPseudoAttrEntry.java /main/1 2013/12/11 05:32:56 sbishnoi Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    12/09/13 - new symbol table entry type for pseudo column
    sbishnoi    12/09/13 - Creation
 */

import oracle.cep.common.Datatype;
import oracle.cep.common.StreamPseudoColumn;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SymbolTableConstPseudoAttrEntry.java /main/1 2013/12/11 05:32:56 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class SymbolTableConstPseudoAttrEntry extends SymbolTablePseudoAttrEntry
{
  /** Constant Value for the pseudo column entry */
  private StreamPseudoColumn spc;
  
  /**
   * Construct a symbol table entry for pseudo column
   * @param varName
   * @param inlineViewVarId the varId of the inline view of which this is
   *                        an attribute
   * @param attrId id of the attribute. It will be a value between 0 .. n-1
   *               where n is the number of attributes of the inline view
   * @param dt the datatype of this attribute
   * @param len the maximum length of this attribute if this is a variable
   *            length attribute   
   */
  SymbolTableConstPseudoAttrEntry(String varName, int inlineViewVarId, int attrId,
      Datatype dt, int len, StreamPseudoColumn spc) 
  {
    super(varName, inlineViewVarId, attrId, dt, len);
    this.spc = spc;
  }
  
  /**
   * Construct a symbol table entry for pseudo column
   * @param inlineViewVarId the varId of the inline view of which this is
   *                        an attribute
   * @param attrId id of the attribute. It will be a value between 0 .. n-1
   *               where n is the number of attributes of the inline view
   * @param dt the datatype of this attribute
   * @param len the maximum length of this attribute if this is a variable
   *            length attribute
   * @param precision the precision value of the attribute if it is of 
   *                  type bigdecimal
   *@param scale the scale value of the attribute if it is of 
   *             type bigdecimal
   */
  SymbolTableConstPseudoAttrEntry(String varName, int inlineViewVarId, int attrId,
      Datatype dt, int len, int precision, int scale, StreamPseudoColumn spc) 
  {
   super(varName, inlineViewVarId, attrId, dt, len, precision, scale);
   this.spc = spc;
  }

  /**
   * Get the constant expression for this pseudo column
   * @param ctx
   * @return
   */
  public Expr getConstExpr(SemContext ctx) 
  {
    switch(spc)
    {
      case QUERY_ID:
        String queryName = ctx.getQueryObj().getName();
        return new ConstCharExpr(queryName);
      default:
        return null;
    }
  }

}