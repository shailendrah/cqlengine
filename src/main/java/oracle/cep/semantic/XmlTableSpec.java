/* $Header: XmlTableSpec.java 26-dec-2007.09:36:10 mthatte Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     12/26/07 - 
    najain      12/03/07 - Creation
 */

/**
 *  @version $Header: XmlTableSpec.java 26-dec-2007.09:36:10 mthatte Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

public class XmlTableSpec
{
  private SymbolTable   symTable;

  /** varId of the base stream on which XMLTABLE is applied */
  private int           baseVarId;

  // id corresponding to the xmlquery string
  private int           varId;
  private int           attrId;

  // list of column definitions
  XmlTableColumnNode[]   lstCols;

  XQryFuncExpr           expr;

  public XmlTableSpec(int baseVarId,  int varId, int attrId,
		      XQryFuncExpr expr,
		      XmlTableColumnNode[] lstCols, 
		      SymbolTable symTable)
  {
    this.baseVarId = baseVarId;
    this.varId     = varId;
    this.attrId    = attrId;
    this.lstCols   = lstCols;
    this.expr      = expr;
    this.symTable  = symTable;
  }

  public int getBaseVarId()
  {
    return baseVarId;
  }

  public int getVarId()
  {
    return varId;
  }

  public int getAttrId()
  {
    return attrId;
  }

  public int getBaseTableId()
  {
    SymbolTableSourceEntry sourceEntry;
    
    sourceEntry = symTable.lookupSource(baseVarId);
    return sourceEntry.getTableId();
  }

  public XQryFuncExpr getXQryFuncExpr()
  {
	return expr;  
  }
  
  public XmlTableColumnNode[] getListCols()
  {
    return lstCols;
  }
  
}

