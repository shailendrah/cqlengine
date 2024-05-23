/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/OuterJoinExprInterp.java /main/2 2010/06/09 02:50:11 sborah Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      05/31/10 - invalid outer join condition
    parujain    05/18/09 - ansi outer join support
    parujain    05/18/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/OuterJoinExprInterp.java /main/2 2010/06/09 02:50:11 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPBooleanExprNode;
import oracle.cep.parser.CEPOuterJoinRelationNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPRelationNode;
import oracle.cep.parser.CEPRightOuterJoinNode;

public class OuterJoinExprInterp extends NodeInterpreter
{
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {
	  
    assert node instanceof CEPOuterJoinRelationNode;
    CEPOuterJoinRelationNode outernode = (CEPOuterJoinRelationNode)node;
    
    super.interpretNode(node, ctx);
    
    CEPRelationNode             leftNode   = outernode.getLeftRelation();
    List<CEPRightOuterJoinNode> rightNodes = outernode.getRightRelation();
   
    // Interpret left relation node
    NodeInterpreter       nodeInterp;
    nodeInterp = InterpreterFactory.getInterpreter(leftNode);
    
    try 
    {
      assert !(leftNode instanceof CEPOuterJoinRelationNode);
      nodeInterp.interpretNode(leftNode, ctx);
    }
    catch (CEPException e) 
    {
      OuterJoinHelper.handleException(e, ctx);
    }    
    OuterJoinHelper.updateFromClauseTables(ctx);
    
    RelationSpec leftSpec = ctx.getRelationSpec();
    
    // Maintain a list of VarIds of the sources participating in the Outer Join
    HashMap<Integer, Integer> listOfVarIdsInOuterJoin 
      = new HashMap<Integer, Integer>();
    listOfVarIdsInOuterJoin.put(leftSpec.getVarId(), leftSpec.getVarId());
        
    // Interpret list of right nodes
    RelationSpec      rightSpec = null;
    OuterRelationSpec outerSpec = null;
    
    // There should be atLeast one right relation to perform a join
    assert rightNodes.size() > 0;
    
    Iterator<CEPRightOuterJoinNode> iter = rightNodes.iterator();
    while(iter.hasNext())
    {
      CEPRightOuterJoinNode rightNode = iter.next();
      
      // Interpret right node
      nodeInterp = InterpreterFactory.getInterpreter(rightNode.getRightNode());
      try
      {
        nodeInterp.interpretNode(rightNode.getRightNode(), ctx);
      }
      catch(CEPException e)
      {
        OuterJoinHelper.handleException(e, ctx);
      }
      OuterJoinHelper.updateFromClauseTables(ctx);
            
      rightSpec = ctx.getRelationSpec();
      
      // update the list with the varId of the right node spec.
      listOfVarIdsInOuterJoin.put(rightSpec.getVarId(), rightSpec.getVarId());
      
      // Interpret condition expression
      CEPBooleanExprNode cond = rightNode.getCondition();
      nodeInterp = InterpreterFactory.getInterpreter(cond);
      nodeInterp.interpretNode(cond, ctx);
      
      BExpr condition = (BExpr)ctx.getExpr();
      
      // Check if the referenced attrs in the On Condition expression 
      // all point to the sources participating
      // in the outer join.
      List<Attr> attrs = new ArrayList<Attr>();
      condition.getAllReferencedAttrs(attrs, SemAttrType.NAMED);
      for(Attr a : attrs)
      {
        if(listOfVarIdsInOuterJoin.get(a.getVarId()) == null)
        {
          // Attribute belongs to a source which is not participating 
          // in the outer join condition.
          // Throw an exception for this. 
          // See bug : 9691954
          throw new SemanticException(
              SemanticError.INVALID_OUTER_JOIN_CONDITION, cond.getStartOffset(), 
              cond.getEndOffset(), new Object[]{cond.toString()});
        }
      }
      
      // construct an outerRelationSpec using leftSpec, rightSpec and condition
      outerSpec = new OuterRelationSpec(leftSpec, 
                                        rightNode.getOuterJoinType(), 
                                        rightSpec.getVarId(), 
                                        condition);
      // newly constructed outerRelationSpec will become leftChild of the
      // next relationSpec (if outerJoin is made on more than two join)      
      leftSpec = outerSpec;
    }
    
    // outerSpec will have the RelationSpec for complete OuterJoinRelationNode
    ctx.setRelationSpec(outerSpec);
   
  }
}
