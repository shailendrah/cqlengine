/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/InterpreterFactory.java /main/36 2012/07/13 02:49:24 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Factory for the parse tree node specific interpreters

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       06/26/12 - interpreter for multi stream list
                           (CEPMultiStreamNode)
    sbishnoi    05/17/12 - support slide without window; adding
                           CEPSlideExprNode interpreter
    vikshukl    09/26/11 - subquery set operations
    vikshukl    07/11/11 - subquery support
    vikshukl    03/07/11 - support n-ary set operators
    sborah      07/27/10 - support for table specific star clause in select
    sbishnoi    09/15/09 - adding entry for TABLE function relation interp
    sborah      06/24/09 - support for bigdecimal
    parujain    05/18/09 - outer join support
    sbishnoi    03/17/09 - adding interpreter for order by node
    parujain    03/16/09 - stateless interp
    parujain    07/01/08 - value based windows
    parujain    06/25/08 - value based windows
    udeshmuk    06/03/08 - addd xmlaggnode mapping.
    skmishra    05/13/08 - adding xmlcomment
    skmishra    05/05/08 - adding xmlparsenode
    mthatte     04/17/08 - adding interpreter for XMLConcat
    parujain    04/23/08 - XMLElement support
    rkomurav    04/01/08 - add FIRSTLASTMULTIEXPR node
    najain      01/02/08 - 
    udeshmuk    01/30/08 - support for double data type.
    udeshmuk    01/11/08 - add mapping for nullexpr node.
    sbishnoi    12/18/07 - add RelationConstraintInterpreter
    najain      10/26/07 - add xmltype
    udeshmuk    09/21/07 - Adding the interpreters for newly defined builtin
                           aggr function nodes.
    rkomurav    09/05/07 - add previnterp
    parujain    06/26/07 - order by support
    sbishnoi    06/28/07 - support for Decode
    hopark      05/29/07 - add LoggingNode
    parujain    03/28/07 - Searched Case Support
    parujain    03/06/07 - Extensible windows support
    rkomurav    02/06/07 - add pattern interpreters
    anasrini    01/09/07 - Interpreter for CEPPatternStreamNode
    hopark      12/05/06 - add bigint datatype
    parujain    10/31/06 - Boolean Interpreters
    parujain    10/05/06 - Generic timestamp datatype
    dlenkov     08/18/06 - support of named queries
    ayalaman    07/31/06 - add partition window interpreter
    anasrini    06/13/06 - support for function expressions 
    dlenkov     06/08/06 - support for binary operations
    najain      04/06/06 - cleanup
    anasrini    03/02/06 - fix compiler unchecked warnings 
    anasrini    02/23/06 - register more interpreters 
    anasrini    02/23/06 - register more interpreters 
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/InterpreterFactory.java /main/36 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.HashMap;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPArithExprNode;
import oracle.cep.parser.CEPAttrNode;
import oracle.cep.parser.CEPBaseBooleanExprNode;
import oracle.cep.parser.CEPBaseRelationNode;
import oracle.cep.parser.CEPBaseStreamNode;
import oracle.cep.parser.CEPBigDecimalConstExprNode;
import oracle.cep.parser.CEPBigintConstExprNode;
import oracle.cep.parser.CEPBooleanConstExprNode;
import oracle.cep.parser.CEPCaseComparisonExprNode;
import oracle.cep.parser.CEPCaseConditionExprNode;
import oracle.cep.parser.CEPCoalesceExprNode;
import oracle.cep.parser.CEPComplexBooleanExprNode;
import oracle.cep.parser.CEPComplexRegexpNode;
import oracle.cep.parser.CEPCountCorrStarNode;
import oracle.cep.parser.CEPCountStarNode;
import oracle.cep.parser.CEPDecodeExprNode;
import oracle.cep.parser.CEPDoubleConstExprNode;
import oracle.cep.parser.CEPElementExprNode;
import oracle.cep.parser.CEPExtensibleWindowExprNode;
import oracle.cep.parser.CEPFirstLastExprNode;
import oracle.cep.parser.CEPFirstLastMultiExprNode;
import oracle.cep.parser.CEPFloatConstExprNode;
import oracle.cep.parser.CEPFunctionExprNode;
import oracle.cep.parser.CEPIntConstExprNode;
import oracle.cep.parser.CEPIntervalConstExprNode;
import oracle.cep.parser.CEPMultiStreamNode;
import oracle.cep.parser.CEPNullConstExprNode;
import oracle.cep.parser.CEPObjExprNode;
import oracle.cep.parser.CEPOrderByExprNode;
import oracle.cep.parser.CEPOrderByNode;
import oracle.cep.parser.CEPOtherAggrExprNode;
import oracle.cep.parser.CEPOuterJoinRelationNode;
import oracle.cep.parser.CEPPREVExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPPartnWindowExprNode;
import oracle.cep.parser.CEPPatternStreamNode;
import oracle.cep.parser.CEPQueryDefnNode;
import oracle.cep.parser.CEPQueryStreamNode;
import oracle.cep.parser.CEPRelationConstraintNode;
import oracle.cep.parser.CEPRelationStarNode;
import oracle.cep.parser.CEPRelationSubqueryNode;
import oracle.cep.parser.CEPRowsWindowExprNode;
import oracle.cep.parser.CEPSFWQueryNode;
import oracle.cep.parser.CEPSearchedCaseExprNode;
import oracle.cep.parser.CEPSelectListNode;
import oracle.cep.parser.CEPSetopQueryNode;
import oracle.cep.parser.CEPGenericSetOpNode;
import oracle.cep.parser.CEPSetopSubqueryNode;
import oracle.cep.parser.CEPSimpleCaseExprNode;
import oracle.cep.parser.CEPSimpleRegexpNode;
import oracle.cep.parser.CEPSlideExprNode;
import oracle.cep.parser.CEPStreamSubqueryNode;
import oracle.cep.parser.CEPStringConstExprNode;
import oracle.cep.parser.CEPTimeWindowExprNode;
import oracle.cep.parser.CEPTableFunctionRelationNode;
import oracle.cep.parser.CEPValueWindowExprNode;
import oracle.cep.parser.CEPWindowRelationNode;
import oracle.cep.parser.CEPXExistsFunctionExprNode;
import oracle.cep.parser.CEPXMLAggNode;
import oracle.cep.parser.CEPXMLConcatExprNode;
import oracle.cep.parser.CEPXMLParseExprNode;
import oracle.cep.parser.CEPXQryFunctionExprNode;
import oracle.cep.parser.CEPXmlAttrExprNode;
import oracle.cep.parser.CEPXmlColAttValExprNode;
import oracle.cep.parser.CEPXmlForestExprNode;
import oracle.cep.parser.CEPXmlTableStreamNode;

/**
 * Factory for the parse tree node specific interpreters
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class InterpreterFactory {

  private static HashMap<String, NodeInterpreter> interpMap;

  static {
    populateInterpMap();
  }

  static void populateInterpMap() {
    interpMap = new HashMap<String, NodeInterpreter>();

    interpMap.put(CEPBaseRelationNode.class.getName(), new BaseRelInterp());
    interpMap.put(CEPBaseStreamNode.class.getName(), new BaseStreamInterp());
    interpMap.put(CEPQueryStreamNode.class.getName(), new QueryStreamInterp());
    interpMap.put(CEPPatternStreamNode.class.getName(), 
                  new PatternStreamInterp());
    interpMap.put(CEPMultiStreamNode.class.getName(), 
                  new MultiStreamInterp());
    interpMap.put(CEPXmlTableStreamNode.class.getName(), 
                  new XmlTableStreamInterp());
    interpMap.put(CEPSFWQueryNode.class.getName(), new SFWQueryInterp());
    interpMap.put(CEPSetopQueryNode.class.getName(), new SetOpQueryInterp());
    interpMap.put(CEPGenericSetOpNode.class.getName(), new GenericSetOpInterp());
    interpMap.put(CEPWindowRelationNode.class.getName(),
                  new WindowRelInterp());
    interpMap.put(CEPTimeWindowExprNode.class.getName(),
                  new TimeWindowInterp());
    interpMap.put(CEPRowsWindowExprNode.class.getName(),
                  new RowsWindowInterp());
    interpMap.put(CEPPartnWindowExprNode.class.getName(),
                  new PartnWindowInterp());
    interpMap.put(CEPExtensibleWindowExprNode.class.getName(), 
                  new ExtensibleWindowInterp());
    interpMap.put(CEPValueWindowExprNode.class.getName(), 
                  new ValueWindowInterp());
    interpMap.put(CEPBaseBooleanExprNode.class.getName(), 
                  new BaseBoolExprInterp());
    interpMap.put(CEPComplexBooleanExprNode.class.getName(), 
    		  new ComplexBoolExprInterp());
    interpMap.put(CEPArithExprNode.class.getName(), new ArithExprInterp());
    interpMap.put(CEPObjExprNode.class.getName(), new ObjExprInterp());
    interpMap.put(CEPAttrNode.class.getName(), new AttrInterp());
    interpMap.put(CEPStringConstExprNode.class.getName(),
                  new ConstCharInterp());
    interpMap.put(CEPIntConstExprNode.class.getName(), new ConstIntInterp());
    interpMap.put(CEPBigintConstExprNode.class.getName(), new ConstBigintInterp());
    interpMap.put(CEPFloatConstExprNode.class.getName(),
                  new ConstFloatInterp());
    interpMap.put(CEPDoubleConstExprNode.class.getName(), 
                  new ConstDoubleInterp());
    interpMap.put(CEPBigDecimalConstExprNode.class.getName(), 
        new ConstBigDecimalInterp());
    interpMap.put(CEPIntervalConstExprNode.class.getName(), 
    		  new ConstIntervalInterp());
    interpMap.put(CEPBooleanConstExprNode.class.getName(),
      new ConstBooleanInterp());
    interpMap.put(CEPSelectListNode.class.getName(), new SelectListInterp());
    //interpMap.put(CEPAggrExprNode.class.getName(), new AggrExprInterp());
    interpMap.put(CEPCountStarNode.class.getName(), new CountStarInterp());
    interpMap.put(CEPCountCorrStarNode.class.getName(), new CountCorrStarInterp());
    interpMap.put(CEPFirstLastExprNode.class.getName(), new FirstLastExprInterp());
    interpMap.put(CEPOtherAggrExprNode.class.getName(), new OtherAggrExprInterp());
    interpMap.put(CEPXMLAggNode.class.getName(), new XMLAggExprInterp());
    interpMap.put(CEPFunctionExprNode.class.getName(), new FuncExprInterp());
    interpMap.put(CEPXQryFunctionExprNode.class.getName(), new XQryFuncExprInterp());
    interpMap.put(CEPXExistsFunctionExprNode.class.getName(), new XQryFuncExprInterp());
    interpMap.put(CEPPREVExprNode.class.getName(), new PrevExprInterp());
    interpMap.put(CEPQueryDefnNode.class.getName(), new QueryDefnInterp());
    interpMap.put(CEPSimpleRegexpNode.class.getName(), new SimpleRegexpInterp());
    interpMap.put(CEPComplexRegexpNode.class.getName(), new ComplexRegexpInterp());
    interpMap.put(CEPSearchedCaseExprNode.class.getName(), new SearchedCaseInterp());
    interpMap.put(CEPCoalesceExprNode.class.getName(), new SearchedCaseInterp());
    interpMap.put(CEPCaseConditionExprNode.class.getName(), new CaseConditionExprInterp());
    interpMap.put(CEPSimpleCaseExprNode.class.getName(), new SimpleCaseInterp());
    interpMap.put(CEPCaseComparisonExprNode.class.getName(), new CaseComparisonExprInterp());
    interpMap.put(CEPOrderByExprNode.class.getName(), new OrderByExprInterp());
    interpMap.put(CEPDecodeExprNode.class.getName(), new DecodeExprInterp());
    interpMap.put(CEPRelationConstraintNode.class.getName(), new RelationConstraintInterp());
    interpMap.put(CEPRelationStarNode.class.getName(), new RelationStarInterp());
    interpMap.put(CEPNullConstExprNode.class.getName(),new ConstNullInterp());
    interpMap.put(CEPFirstLastMultiExprNode.class.getName(), new FirstLastMultiExprInterp());
    interpMap.put(CEPXMLConcatExprNode.class.getName(),new XMLConcatExprInterp());
    interpMap.put(CEPXMLParseExprNode.class.getName(), new XMLParseExprInterp());
    interpMap.put(CEPElementExprNode.class.getName(), new ElementExprInterp());
    interpMap.put(CEPXmlAttrExprNode.class.getName(), new XmlAttrExprInterp());
    interpMap.put(CEPXmlForestExprNode.class.getName(), new XmlForestExprInterp());
    interpMap.put(CEPXmlColAttValExprNode.class.getName(), new XmlColAttValExprInterp());
    interpMap.put(CEPOrderByNode.class.getName(), new OrderByNodeInterp());
    interpMap.put(CEPOuterJoinRelationNode.class.getName(), new OuterJoinExprInterp());
    interpMap.put(CEPTableFunctionRelationNode.class.getName(), new TableFunctionRelationInterp());
    interpMap.put(CEPStreamSubqueryNode.class.getName(), new SubqueryStreamInterp());
    interpMap.put(CEPRelationSubqueryNode.class.getName(), new SubqueryRelationInterp());
    interpMap.put(CEPSetopSubqueryNode.class.getName(), new SetOpSubqueryInterp());
    interpMap.put(CEPSlideExprNode.class.getName(), new SlideExprInterp());
  }

  static NodeInterpreter getInterpreter(CEPParseTreeNode node) 
    throws CEPException {

    NodeInterpreter ni;
    ni = interpMap.get(node.getClass().getName());

    assert ni != null : node.getClass().getName();

    return ni;
  }

}
