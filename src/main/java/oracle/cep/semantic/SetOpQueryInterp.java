/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SetOpQueryInterp.java /main/23 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - set partitioned stream flag
    vikshukl    07/31/12 - archived dimension
    pkali       04/03/12 - included datatype arg in Attr instance
    udeshmuk    07/12/11 - support for archived relation
    udeshmuk    04/01/11 - store name of attr
    parujain    03/12/09 - make interpreters stateless
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/04/08 - maintain offsets
    parujain    08/26/08 - semantic exception offset
    parujain    06/06/08 - xmltype checks
    parujain    11/09/07 - external source
    mthatte     10/30/07 - change TableMgr to SourceMgr
    parujain    10/25/07 - if using ondemand
    udeshmuk    10/30/07 - construct list of attrs in left relation not only
                           for IN but for every setop.
    udeshmuk    10/20/07 - For IN prepare all leftattr list to be set as
                           comparison attr list for outer minus operator.
    udeshmuk    10/04/07 - remove import "import
                           com.sun.tools.javac.code.Source".
    udeshmuk    10/01/07 - support for intersect and in operators.
    rkomurav    09/20/07 - Allowing both stream operands for union all
                           operation
    udeshmuk    09/18/07 - Changing the error thrown when an argument of type
                           stream occurs in operations like union.
    udeshmuk    09/13/07 - Including checks to ensure that both operands of
                           union all are relations.
    sbishnoi    09/04/07 - support for notin set operation
    anasrini    05/30/07 - bug fix
    anasrini    05/22/07 - symbol table reorg
    sbishnoi    04/03/07 - support for union all
    rkomurav    02/22/07 - cleanup reftables
    anasrini    09/07/06 - set name for select list expressions
    dlenkov     06/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SetOpQueryInterp.java /main/23 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.Hashtable;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSetopQueryNode;
import oracle.cep.service.ExecContext;
import oracle.cep.common.RelSetOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.metadata.MetadataException;
import oracle.cep.common.Datatype;

/**
 * The interpreter for CEPSetopQueryNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class SetOpQueryInterp extends QueryRelationInterp {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    RelSetOp setType;
    int numAttrs;
    Datatype attrType;
    Expr attrExpr;
    String attrName;
    String varName;
    int left,right;
    boolean isExternal;
    
    ExecContext ec = ctx.getExecContext();
    assert node instanceof CEPSetopQueryNode;
    CEPSetopQueryNode setNode = (CEPSetopQueryNode)node;
    setType = setNode.getRelSetOp();

    SetOpQuery setQuery = new SetOpQuery(setType);
    ctx.setSemQuery(setQuery);
    setQuery.setSymTable(ctx.getSymbolTable());

    super.interpretNode(node, ctx);
    registerTables(ctx, setNode, setQuery);
    
    setQuery.setIsUnionAll(setNode.isUnionAll());
    
    left = setQuery.getTableId(setQuery.getLeftVarId());
    try{
    numAttrs = ec.getSourceMgr().getNumAttrs( left);
   
    
    // For IN and NOT IN number of attrs. and schema need not match 
    if ((setType != RelSetOp.NOT_IN) && (setType != RelSetOp.IN))
    {
      if (numAttrs != ec.getSourceMgr()
         .getNumAttrs(setQuery.getTableId(setQuery.getRightVarid())))
        throw new SemanticException(SemanticError.NUMBER_OF_ATTRIBUTES_MISMATCH,
          setNode.getStartOffset(), setNode.getEndOffset(),
          new Object[]{setNode.getLeftTable(),setNode.getRightTable()});
      // Verify if schema matches
      checkSchema(ec, setNode, setQuery);
    }
    right=setQuery.getTableId(setQuery.getRightVarid());

    if (setQuery.isUnionAll())
    { 
      // for 'union all' operation, the case of both stream operands is allowed 
      // other cases are not allowed
      if ((ec.getSourceMgr().isStream(left)) && 
          (!(ec.getSourceMgr().isStream(right))))
      {
        throw new SemanticException(SemanticError.NOT_A_RELATION_ERROR, 
                           setNode.getStartOffset(), setNode.getEndOffset(),
                               new Object[]{setNode.getLeftTable()});
      }
      if ((!(ec.getSourceMgr().isStream(left))) &&
          (ec.getSourceMgr().isStream(right)))
      {
        throw new SemanticException(SemanticError.NOT_A_RELATION_ERROR, 
                          setNode.getStartOffset(), setNode.getEndOffset(),
                               new Object[]{setNode.getRightTable()});
      }
    }
    else
    {
      if (ec.getSourceMgr().isStream(left))
        throw new SemanticException(SemanticError.NOT_A_RELATION_ERROR,
                              setNode.getStartOffset(), setNode.getEndOffset(),
                               new Object[]{setNode.getLeftTable()});
      if (ec.getSourceMgr().isStream(right))
        throw new SemanticException(SemanticError.NOT_A_RELATION_ERROR, 
                            setNode.getStartOffset(), setNode.getEndOffset(),
                               new Object[]{setNode.getRightTable()});
    }
    
    // isExternal if any of left or right is ondemand
    isExternal = (ec.getSourceMgr().isExternal(left) ||
            ec.getSourceMgr().isExternal(right));
    
    Attr[] leftSrcAttrs = new Attr[numAttrs];
    
    varName =  setNode.getLeftTable();
    for (int i = 0; i < numAttrs; i++) {
      attrName = ec.getSourceMgr().getAttrName(left, i);
      attrType = ec.getSourceMgr().getAttrType(left, i);
      Attr attr= new Attr(left, i, varName+"."+attrName, attrType);
      leftSrcAttrs[i] = attr;
      attrExpr = new AttrExpr( new Attr( 0, i, varName+"."+attrName, attrType)
                                          , attrType);
      attrExpr.setName(varName + "." + attrName, false, isExternal);
      ctx.addSelectListExpr( attrExpr);
    }
    
    setQuery.setLeftAttrs(leftSrcAttrs);
    setQuery.setSelectListExprs( ctx.getSelectList());
    
    if ((setType == RelSetOp.NOT_IN)||(setType == RelSetOp.IN))
    {
      // Obtain Comparison Attributes
      makeComparisonAttrLists(ec, setNode, setQuery);
      if(setQuery.getNumComparisonAttrs() == 0)
        throw new SemanticException(SemanticError.INVALID_COMPARISON_ATTRS,
            setNode.getStartOffset(), setNode.getEndOffset());
    }
    }catch(MetadataException me)
    {
      me.setStartOffset(setNode.getStartOffset());
      me.setEndOffset(setNode.getEndOffset());
      throw me;
    }
    
    //If any of the inputs is archived, set the flag to true.
    if((ec.getSourceMgr().isArchived(left))
       ||(ec.getSourceMgr().isArchived(right)))
      setQuery.setIsDependentOnArchivedRelation(true);
    else
      setQuery.setIsDependentOnArchivedRelation(false);

    if((ec.getSourceMgr().isPartnStream(left))
       ||(ec.getSourceMgr().isPartnStream(right)))
      setQuery.setIsDependentOnPartnStream(true);
    else
      setQuery.setIsDependentOnPartnStream(false);
  }

  void registerTables(SemContext ctx, CEPSetopQueryNode setNode,
                      SetOpQuery setQuery) 
  throws CEPException
  {
    String tName;
    String vName;
    int    leftid = 0;
    int    rightid = 0;
    
    tName = setNode.getLeftTable();
    assert tName != null;
    vName = tName;
    try {
      leftid = ctx.getSymbolTable().
                 addPersistentSourceEntry(ctx.getExecContext(), tName, 
                                            vName, ctx.getSchema());
      setQuery.setLeftVarId(leftid);
      setQuery.addReferencedTable(ctx.getSymbolTable().
                                    lookupSource(leftid).getTableId());
    }catch(CEPException ce)
    {
      ce.setStartOffset(setNode.getStartOffset());
      ce.setEndOffset(setNode.getEndOffset());
      throw ce;
    }
    

    tName = setNode.getRightTable();
    assert tName != null;
    vName = tName;

    try {
      rightid = ctx.getSymbolTable().
                     addPersistentSourceEntry(ctx.getExecContext(), tName, 
                                                 vName, ctx.getSchema());
    }
    catch (CEPException e) {
      if (e.getErrorCode() != SemanticError.AMBIGUOUS_TABLE_ERROR)
      {
        e.setStartOffset(setNode.getStartOffset());
        e.setEndOffset(setNode.getEndOffset());
        throw e;
      }
      else
        // FIXME: why is this needed?
        rightid = leftid;
    }
    setQuery.setRightVarid(rightid);
    setQuery.addReferencedTable(ctx.getSymbolTable().
                                  lookupSource(rightid).getTableId());
    
    return;
  }
  
  /**
   * Construct Comparison Attribute Lists inside Set operation IN/NOT IN 
   * @throws CEPException
   */
  private void makeComparisonAttrLists(ExecContext ec, CEPSetopQueryNode setNode,
                                       SetOpQuery setQuery) 
  throws CEPException
  {
    String   attrName;
    
    int rightAttrId;
    
    Datatype leftAttrType;
    Datatype rightAttrType;
    
    Attr leftComparisonAttr;
    Attr rightComparisonAttr;
    
    int leftTable  = setQuery.getTableId(setQuery.getLeftVarId());
    int rightTable = setQuery.getTableId(setQuery.getRightVarid());
 
    // Size of comparison attr lists
    int leftAttrListSize  = 
      ec.getSourceMgr().getNumAttrs(leftTable);
    int rightAttrListSize =
      ec.getSourceMgr().getNumAttrs(rightTable);
    
    Hashtable< String, Datatype > rightParams = 
      new Hashtable< String, Datatype >(rightAttrListSize);
    
    // Construct Hash Table by
    // inserting comparison attributes from right list to Hash table
    for(int i=0; i < rightAttrListSize; i++)
    {
      attrName      =
        ec.getSourceMgr().getAttrName(rightTable, i);
      rightAttrType = 
        ec.getSourceMgr().getAttrType(rightTable, i);
      rightParams.put(attrName, rightAttrType);
    }
    
    setQuery.setComparisonAttrs((leftAttrListSize < rightAttrListSize) ? 
                                leftAttrListSize : rightAttrListSize);
    
    // Iterate through Left Attr List
    // Update SetOpQuery's Comparison Attr List if 
    // leftComparisonAttr.name = rightComparisonAttr.name and
    // leftComparisonAttr.Datatype = rightComparisonAttr.Datatype
    
    for(int i=0; i < leftAttrListSize; i++)
    {
      attrName   =
        ec.getSourceMgr().getAttrName(leftTable, i);
      leftAttrType = 
        ec.getSourceMgr().getAttrType(leftTable, i);
      
      // Fetch right attr from Hastable having same name as left attr
      rightAttrType = (Datatype)(rightParams.get(attrName));
      
      if(leftAttrType == rightAttrType)          
      {
        rightAttrId = 
          ec.getSourceMgr().getAttrId(rightTable, attrName);
        leftComparisonAttr  = new Attr(leftTable, i, attrName, leftAttrType);
        rightComparisonAttr = new Attr(rightTable,rightAttrId, 
                                            attrName, rightAttrType);
        if(leftAttrType == Datatype.XMLTYPE)
           throw new SemanticException(SemanticError.INVALID_XMLTYPE_USAGE,
                setNode.getStartOffset(), setNode.getEndOffset(),
                                  new Object[]{attrName});
        setQuery.addComparisonAttr(leftComparisonAttr, rightComparisonAttr);
      }
    }
   
  }
  
  
  /**
   * Verifies whether schemas of the left and right input to 
   * INTERSECTION, UNION and MINUS set operations match or not
   * @throws CEPException
   */
  private void checkSchema(ExecContext ec, CEPSetopQueryNode setNode,
                           SetOpQuery setQuery) 
  throws CEPException
  {
    Datatype leftAttrType;
    Datatype rightAttrType;
    int leftTable  = setQuery.getTableId(setQuery.getLeftVarId());
    int rightTable = setQuery.getTableId(setQuery.getRightVarid());
    
    // Here number of attrs in left and right table are equal
    int numAttrs   = ec.getSourceMgr().getNumAttrs(leftTable);
    
    // Comparing corresponding attrs in the input relations
    for (int i=0; i < numAttrs; i++)
    {
      leftAttrType  =
        ec.getSourceMgr().getAttrType(leftTable, i);
      rightAttrType =
        ec.getSourceMgr().getAttrType(rightTable, i);
      if (!leftAttrType.equals(rightAttrType)) 
        throw new SemanticException(SemanticError.SCHEMA_MISMATCH_IN_SETOP,
           setNode.getStartOffset(), setNode.getEndOffset(),
          new Object[]{i+1, setNode.getLeftTable(), setNode.getRightTable()});
      if((!setQuery.isUnionAll()) && (leftAttrType == Datatype.XMLTYPE))
        throw new SemanticException(SemanticError.INVALID_XMLTYPE_USAGE,
          setNode.getStartOffset(), setNode.getEndOffset(),  
          new Object[]{ec.getSourceMgr().getAttrName(leftTable, i)});
    }
    
  }
  
}
