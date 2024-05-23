/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/AttrInterp.java /main/14 2013/12/11 05:32:56 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    The interpreter for the CEPAttrNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    12/09/13 - support of new keyword for pseudo column QUERYNAME
    udeshmuk    01/15/13 - XbranchMerge udeshmuk_bug-15962424_ps6 from
                           st_pcbpel_11.1.1.4.0
    pkali       04/03/12 - included datatype arg in Attr instance
    udeshmuk    04/01/11 - propagate name of attr
    sbishnoi    02/24/10 - set external flag for attributes by checking its
                           source
    parujain    03/12/09 - make interpreters stateless
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/04/08 - maintain offsets
    parujain    08/27/08 - semantic offset
    udeshmuk    02/05/08 - paramterize error.
    parujain    11/09/07 - External source
    parujain    10/25/07 - db join
    parujain    10/25/07 - db join
    anasrini    05/25/07 - corr attr support
    anasrini    05/22/07 - symbol table reorg
    anasrini    08/30/06 - set name in AttrExpr
    najain      05/16/06 - support views 
    najain      04/06/06 - cleanup
    anasrini    02/22/06 - Creation
    anasrini    02/22/06 - Creation
    anasrini    02/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/AttrInterp.java /main/14 2013/12/11 05:32:56 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPAttrNode;
import oracle.cep.service.ExecContext;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.metadata.MetadataException;

/**
 * The interpreter that is specific to the CEPAttrNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class AttrInterp extends NodeInterpreter {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    // Convert  a syntactic attribute  to a  semantic attribute.   A syntactic
    // attribute (e.g.,  "R.A" or  "A") has an  optional variable  name string
    // ("R") and a non-optional  attribute name ("A").  The semantic attribute
    // is of the  form <varId, attrId> is the unique variable  id of the table
    // instance to  which "A" belongs, and  attrId is the  identifier for this
    // attribute within the table.

    CEPAttrNode          attrNode;
    String               varName;
    String               attrName;
    int                  varId   = 0;
    int                  attrId  = 0;
    AttrExpr             attrExpr;
    Datatype             attrType;
    String               exprName;
    String               actualName;
    String               tabName;
    SymbolTableAttrEntry attrEntry;
    SymbolTableEntry     tabEntry;
    SymbolTableCorrEntry corrEntry;
    SymbolTableSourceEntry srcEntry;
    SymTableEntryType    entryType;
    Attr                 attr;
   //  check if attribute belongs to an External table
    boolean isExternal = false;

    assert node instanceof CEPAttrNode;
    attrNode = (CEPAttrNode)node;

    super.interpretNode(node, ctx);

    varName  = attrNode.getVarName();
    attrName = attrNode.getAttrName();

    if (attrNode.isFullyQualifiedName()) {
      // Variable name explicit (e.g., "R.A")
      try{
        //If we are currently interpreting partition parallel expr then we 
        //should look up the attr only in entries of type SOURCE. 
        //CORR entries should not be considered.
        if(ctx.isInterpretingPartnExpr())
          attrEntry =
            ctx.getSymbolTable().lookupAttrInSource(ctx.getExecContext(),
                                                    varName, attrName);
        else
          attrEntry = 
            ctx.getSymbolTable().lookupAttr(ctx.getExecContext(), varName,
                                            attrName);
      }catch(CEPException e)
      {
        e.setStartOffset(attrNode.getStartOffset());
        e.setEndOffset(attrNode.getEndOffset());      
        throw e;
      }
    }
    else {
      // Variable name implicit: for all possible variable id's check if the
      // attribute belongs to the table corr. to the variable id.

      try {
        //If we are currently interpreting partition parallel expr then we 
        //should look up the attr only in entries of type SOURCE. 
        //CORR entries should not be considered.
        if(ctx.isInterpretingPartnExpr())
          attrEntry = 
            ctx.getSymbolTable().lookupAttrInSource(ctx.getExecContext(), 
                                                    attrName);
        else
          attrEntry = 
            ctx.getSymbolTable().lookupAttr(ctx.getExecContext(), attrName);
      }
      catch (CEPException e) {
        if (e.getErrorCode() == SemanticError.UNKNOWN_VAR_ERROR)
          throw new SemanticException(SemanticError.UNKNOWN_ATTR_ERROR,
                                      attrNode.getStartOffset(), attrNode.getEndOffset(),
                                      new Object[]{attrName});
        else{
            e.setStartOffset(attrNode.getStartOffset());
            e.setEndOffset(attrNode.getEndOffset());
         
            throw e;
        }
      }
    }

    tabEntry  = ctx.getSymbolTable().lookup(attrEntry.getInlineViewVarId());
    varName   = tabEntry.getVarName();
    // tableName will be same as varname for correlations
    tabName   = varName; 
    entryType = tabEntry.getEntryType();
    exprName  = varName + "." + attrName;
    
    varId     = attrEntry.getInlineViewVarId();
    attrId    = attrEntry.getAttrId();
    attrType  = attrEntry.getAttrType();

    if (entryType == SymTableEntryType.CORR) 
    {
      corrEntry = (SymbolTableCorrEntry)tabEntry;
      attr      = new SemCorrAttr(varId, attrId, corrEntry.getTableVarId(),
                                  tabName+"."+attrName, attrType);
    }
    else 
    {
      assert (entryType == SymTableEntryType.SOURCE);
      srcEntry = (SymbolTableSourceEntry)tabEntry;
      tabName = srcEntry.getTableName();
      
      if(srcEntry.getSourceType() == SymbolTableSourceType.PERSISTENT)
      {
        try{
          ExecContext ec = ctx.getExecContext();
            isExternal = ec.getTableMgr().isExternal(tabName, ctx.getSchema());
        }catch(MetadataException me)
        {
          me.setStartOffset(attrNode.getStartOffset());
          me.setEndOffset(attrNode.getEndOffset());
          throw me;
        }
      }
      else if(srcEntry.getSourceType() == SymbolTableSourceType.INLINE_VIEW)
      {
        isExternal = ((SymbolTableSourceEntry)tabEntry).isExternal();        
      }
      
      // If the attribute referenced is a pseudo column and attribute value will
      // remain constant for each input tuple, then set the semantic expression
      // to a constant expression of relevant type
      if(attrEntry.getEntryType() == SymTableEntryType.PSEUDO)
      {
        if(attrEntry instanceof SymbolTableConstPseudoAttrEntry)
        {
          Expr constPseudoExpr 
            = ((SymbolTableConstPseudoAttrEntry)attrEntry).getConstExpr(ctx);
          constPseudoExpr.setName(exprName, false, isExternal);
          constPseudoExpr.setAlias(tabName + "." + attrName);
          ctx.setExpr(constPseudoExpr);
          return;
        }
      }
      attr = new Attr(varId, attrId, tabName+"."+attrName, attrType);
    }

    attrExpr = new AttrExpr(attr, attrType);
    
    actualName = tabName + "." + attrName;
    attrExpr.setName(exprName, false, isExternal);
    attrExpr.setActualName(actualName);
    ctx.setExpr(attrExpr);
  }
}
