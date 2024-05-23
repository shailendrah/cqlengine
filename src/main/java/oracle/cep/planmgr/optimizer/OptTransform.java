/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/optimizer/OptTransform.java /main/23 2013/11/27 21:53:23 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 CEP Optimizer

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    11/18/13 - bug 17709899
 vikshukl    06/14/13 - remove unused import
 vikshukl    01/31/13 - skip join-project optimization.,
 udeshmuk    08/30/12 - set outer and inner attrs when creating
                        phyoptjoinproject
 vikshukl    08/14/12 - archived dimension
 pkali       05/31/12 - merge select with partwin in presence of predicate
 vikshukl    08/26/11 - short circuit subquery operator
 sbishnoi    09/30/09 - table function support
 sbishnoi    05/27/09 - ansi syntax support for outer join; copy join type
 sbishnoi    05/26/09 - stop merging select into OuterJoinOperator; support for
                        ansi standard
 sborah      03/20/09 - modify copy for element_time
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 anasrini    09/14/08 - remove useless projects
 skmishra    05/02/08 - adding opt for xmlconcat
 parujain    04/28/08 - xmlelement support
 parujain    11/15/07 - external relation
 sbishnoi    07/12/07 - modified ViewStrmSrc and ViewRelnSrc checks
 parujain    04/05/07 - CASE Expression
 rkomurav    12/03/06 - add complete expr list to transtoJoinExpr
 najain      11/13/06 - bug fix
 parujain    11/07/06 - complex boolean exprs
 najain      11/10/06 - merge root if possible
 parujain    10/31/06 - Complex/Base Boolean Exprs
 najain      08/29/06 - bug fix
 rkomurav    08/29/06 - fix for deleting Phyopts
 najain      08/23/06 - bug fix: views
 najain      06/21/06 - query deletion support 
 najain      05/30/06 - stream join project support 
 najain      05/26/06 - stream join support 
 najain      05/25/06 - add more functionality 
 najain      05/02/06 - bug fix 
 dlenkov     04/05/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/optimizer/OptTransform.java /main/23 2013/11/27 21:53:23 sbishnoi Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.optimizer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptJoin;
import oracle.cep.phyplan.PhyOptJoinProject;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyOptOutputIter;
import oracle.cep.phyplan.PhyOptProject;
import oracle.cep.phyplan.PhyOptPrtnWin;
import oracle.cep.phyplan.PhyOptSelect;
import oracle.cep.phyplan.PhyOptStrJoin;
import oracle.cep.phyplan.PhyOptStrJoinProject;
import oracle.cep.phyplan.PhysicalPlanException;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.expr.BaseBoolExpr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.ComplexBoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.phyplan.expr.ExprCaseComparison;
import oracle.cep.phyplan.expr.ExprCaseCondition;
import oracle.cep.phyplan.expr.ExprComplex;
import oracle.cep.phyplan.expr.ExprElement;
import oracle.cep.phyplan.expr.ExprSearchCase;
import oracle.cep.phyplan.expr.ExprSimpleCase;
import oracle.cep.phyplan.expr.ExprUserDefFunc;
import oracle.cep.phyplan.expr.ExprXmlAttr;
import oracle.cep.phyplan.expr.ExprXmlColAttVal;
import oracle.cep.phyplan.expr.ExprXmlConcat;
import oracle.cep.phyplan.expr.ExprXmlForest;
import oracle.cep.phyplan.expr.ExprXmlParse;
import oracle.cep.service.ExecContext;

/**
 * OptTransform
 * 
 * @author
 */
public class OptTransform
{
  /**
   * Constructor for OptTransfom. The constructor has been kept private for the
   * same reasons as the constructor for PlanManager. See comments the
   * PlanManager constructor.
   */
  public OptTransform()
  {
    // No initialization is required, so far
  }

  /**
   * Remove this operator from a plan, and directly connect its input to all its
   * outputs. As a side effect, it frees all references to op. It is assumed
   * that this operator has just one input and one or more outputs
   * @param ec TODO
   */
  private void shortCircuitInputOutput(ExecContext ec, PhyOpt op) 
  throws PhysicalPlanException
  {
    PhyOpt inop;
    int numOutputs = op.getNumOutputs();

    assert (op.getNumInputs() == 1);

    inop = op.getInputs()[0];
    PhyOptOutputIter iter = inop.getOutputsIter();

    for (int i = 0; i < inop.getNumOutputs(); i++)
    {
      PhyOpt outOp = iter.getNext();
      assert outOp != null;

      if (outOp == op)
      {
        iter.remove();
        break;
      }
    }

    iter = op.getOutputsIter();
    for (int i = 0; i < numOutputs; i++)
    {
      PhyOpt outOp = iter.getNext();
      assert outOp != null;

      int iidx = op.getInputIndex(outOp);
      outOp.getInputs()[iidx] = inop;

      inop.addOutput(outOp);
    }

    assert ((iter == null) || (iter.getNext() == null));
    ec.getPlanMgr().removePhyOpt(op);
  }

  /**
   * Transform attribute
   */
  private void transToJoinAttr(Attr attr, PhyOpt joinOp)
  {
    assert (attr.getInput() == 0);
    assert (attr.getPos() < joinOp.getNumAttrs());

    // We only deal with binary joins
    assert (joinOp != null);
    assert (joinOp.getNumInputs() == 2);
    assert (joinOp.getInputs()[0] != null);
    assert (joinOp.getInputs()[1] != null);
    assert ((joinOp.getOperatorKind() == PhyOptKind.PO_JOIN) || (joinOp
        .getOperatorKind() == PhyOptKind.PO_STR_JOIN));

    if (joinOp.getOperatorKind() == PhyOptKind.PO_JOIN)
    {
      PhyOptJoin opJoin = (PhyOptJoin) joinOp;
      if (opJoin.getNumOuterAttrs() <= attr.getPos())
      {
        attr.setInput(1);
        attr.setPos(attr.getPos() - opJoin.getNumOuterAttrs());
      }
    }
    else
    {
      PhyOptStrJoin opStrJoin = (PhyOptStrJoin) joinOp;
      if (opStrJoin.getNumOuterAttrs() <= attr.getPos())
      {
        attr.setInput(1);
        attr.setPos(attr.getPos() - opStrJoin.getNumOuterAttrs());
      }
    }
  }

  /**
   * Transform a expression in an operator above a join to an equiv. predicate
   * within the join. The transformation is "in-place".
   * 
   * @param expr
   *          expression to be transformed
   * @param joinOp
   *          join operator
   */
  private void transToJoinExpr(Expr expr, PhyOpt joinOp)
  {
    ExprKind kind;

    assert (expr != null);

    kind = expr.getKind();

    if (kind == ExprKind.CONST_VAL)
    {
      // do nothing
    }
    else if (kind == ExprKind.COMP_EXPR)
    {
      transToJoinExpr(((ExprComplex) expr).getLeft(), joinOp);
      transToJoinExpr(((ExprComplex) expr).getRight(), joinOp);
    }
    else if (kind == ExprKind.ATTR_REF)
    {
      transToJoinAttr(((ExprAttr) expr).getAValue(), joinOp);
    }
    else if (kind == ExprKind.COMP_BOOL_EXPR)
    {
      transToJoinExpr(((ComplexBoolExpr) expr).getLeft(), joinOp);
      if(((ComplexBoolExpr) expr).getRight() != null)
        transToJoinExpr(((ComplexBoolExpr) expr).getRight(), joinOp);
    }
    else if (kind == ExprKind.BASE_BOOL_EXPR)
    {
      BaseBoolExpr boolPred = (BaseBoolExpr)expr;
      if(boolPred.getOper() != null)
      {
        transToJoinExpr(((BaseBoolExpr) expr).getLeft(), joinOp);
        transToJoinExpr(((BaseBoolExpr) expr).getRight(), joinOp);
      }
      else
      {
        //     unary operator
        assert boolPred.getUnary() != null;
        transToJoinExpr(boolPred.getUnary(), joinOp);
      }
    }
    else if (kind == ExprKind.USER_DEF)
    {
      ExprUserDefFunc exprUserDef = (ExprUserDefFunc)expr;
      for(int i = 0; i < exprUserDef.getNumArgs(); i++)
        transToJoinExpr(exprUserDef.getArgs()[i], joinOp);
    }
    else if (kind == ExprKind.SEARCH_CASE)
    {
      ExprSearchCase search = (ExprSearchCase)expr;
      ExprCaseCondition[] conds = search.getCaseConditions();
      for(int i=0; i<search.getNumConditions(); i++)
        transToJoinExpr(conds[i], joinOp);
      if(search.getElseExpr() != null)
        transToJoinExpr(search.getElseExpr(), joinOp);
    }
    else if(kind == ExprKind.CASE_CONDITION)
    {
      ExprCaseCondition cond = (ExprCaseCondition)expr;
      transToJoinExpr(cond.getConditionExpr(), joinOp);
      if(cond.getResultExpr() != null)
        transToJoinExpr(cond.getResultExpr(), joinOp);
    }
    else if(kind == ExprKind.SIMPLE_CASE)
    {
      ExprSimpleCase simple = (ExprSimpleCase)expr;
      ExprCaseComparison[] comp = simple.getComparisons();
      transToJoinExpr(simple.getCompExpr(), joinOp);
      for(int i=0; i<simple.getNumComparisons(); i++)
        transToJoinExpr(comp[i], joinOp);
      if(simple.getElseExpr() != null)
        transToJoinExpr(simple.getElseExpr(), joinOp);
    }
    else if(kind == ExprKind.CASE_COMPARISON)
    {
      ExprCaseComparison comp = (ExprCaseComparison)expr;
      transToJoinExpr(comp.getComparisonExpr(), joinOp);
      if(comp.getResultExpr() != null)
        transToJoinExpr(comp.getResultExpr(), joinOp);
    }
    else if(kind == ExprKind.XML_CONCAT_EXPR)
    {
      ExprXmlConcat xExpr = (ExprXmlConcat)expr;
      for(Expr e:xExpr.getConcatExprs())
      {
        transToJoinExpr(e, joinOp);  
      }
    }
    else if(kind == ExprKind.XML_PARSE_EXPR)
    {
      ExprXmlParse xExpr = (ExprXmlParse) expr;
      transToJoinExpr(xExpr.getValue(), joinOp);
    }
    else if(kind == ExprKind.XMLELEMENT_EXPR)
    {
      ExprElement element = (ExprElement)expr;
      Expr[] child = element.getChildExprs();
      Expr[] attributes = element.getAttrExprs();
      if(element.getElementNameExpr() != null)
        transToJoinExpr(element.getElementNameExpr(), joinOp);
      for(int i=0; i<element.getNumAttrs(); i++)
      {
         transToJoinExpr(attributes[i], joinOp);      
      }
      for(int j=0; j<element.getNumChildren(); j++)
      {
         transToJoinExpr(child[j], joinOp);
      }
    }
    else if(kind == ExprKind.XMLATTR_EXPR)
    {
      ExprXmlAttr xmlattr = (ExprXmlAttr)expr;
      if(xmlattr.getAttrNameExpr() != null)
        transToJoinExpr(xmlattr.getAttrNameExpr(), joinOp);
      transToJoinExpr(xmlattr.getAttrExpr(), joinOp);
    }
    else if(kind == ExprKind.XMLFOREST_EXPR)
    {
      ExprXmlForest forest = (ExprXmlForest)expr;
      Expr[] exprs = forest.getForestExprs();
      for(int i=0; i<exprs.length; i++)
        transToJoinExpr(exprs[i],joinOp);
    }
    else if(kind == ExprKind.XMLCOLATTVAL_EXPR)
    {
      ExprXmlColAttVal colAtt = (ExprXmlColAttVal)expr;
      Expr[] exprs = colAtt.getColAttExprs();
      for(int i=0; i<exprs.length; i++)
        transToJoinExpr(exprs[i],joinOp);
    }
    else
      assert false;
  }

  /**
   * Transform a predicate in an operator above a join to an equiv. predicate
   * within the join. The transformation is "in-place".
   * 
   * @param preds
   *          predicates to be transformed
   * @param joinOp
   *          join operator
   */
  private void transToJoinPreds(LinkedList<BoolExpr> preds, PhyOpt joinOp)
  {
    ListIterator<BoolExpr> iter;
    BoolExpr pred;

    if (preds.size() == 0)
      return;

    iter = preds.listIterator();
    while (iter.hasNext())
    {
      pred = (BoolExpr) (iter.next());
      transToJoinExpr(pred, joinOp);
    }
  }

  /**
   * Merge select operator. It assumes that the number of input equals 1.
   */
  private boolean mergeSelect(PhyOptSelect op)
  {
    PhyOpt inop;

    assert (op.getNumInputs() == 1);

    inop = op.getInputs()[0];

    // We cannot merge if someone else is reading from the child operator
    if (inop.getNumOutputs() != 1)
      return false;

    // We cannot merge if child operator is a view root
    if (inop.getIsView())
      return false;
    
    if (inop.getOperatorKind() == PhyOptKind.PO_SELECT)
    {
      ((PhyOptSelect) inop).appendPreds(op.getPreds());
      return true;
    }

    if (inop.getOperatorKind() == PhyOptKind.PO_JOIN)
    {      
      // Transform the predicate in the select to an equivalent
      // predicate in the join.
      
      // Note: If the input PhyOptJoin operator represents a outer join
      // Never merge the where clause predicate with Join's ON Predicate.
      if(((PhyOptJoin)inop).getOuterJoinType() != null)
        return false;
      transToJoinPreds(op.getPreds(), inop);
      ((PhyOptJoin) inop).appendPreds(op.getPreds());
      return true;
    }

    if (inop.getOperatorKind() == PhyOptKind.PO_STR_JOIN)
    {
      // Transform the predicate in the select to an equivalent
      // predicate in the stream join.
      transToJoinPreds(op.getPreds(), inop);

      ((PhyOptStrJoin) inop).appendPreds(op.getPreds());
      return true;
    }

    // Move the predicate evaluation from select operator
    // to the partition window operator for memory optimization
    if(inop.getOperatorKind() == PhyOptKind.PO_PARTN_WIN)
    {
      ((PhyOptPrtnWin) inop).setPredicates(op.getPreds());
      return true;
    }
    return false;
  }

  /**
   * merge selects
   * @param ec TODO
   */
  private PhyOpt mergeSelects(ExecContext ec, PhyOpt op) throws PhysicalPlanException
  {
    PhyOpt root = op;

    // cant go below the views
    if (op.getIsView())
      return root;

    for (int i = 0; i < op.getNumInputs(); i++)
      mergeSelects(ec, op.getInputs()[i]);

    if ((op.getOperatorKind() == PhyOptKind.PO_SELECT)
        && mergeSelect((PhyOptSelect)op))
    {
      if (op.getNumOutputs() == 0)
        root = op.getInputs()[0];
      shortCircuitInputOutput(ec, op); // frees all references to op
    }
    
    return root;
  }

  private void transJoinToJoinProject(ExecContext ec, PhyOptJoin op)
      throws PhysicalPlanException
  {
    PhyOptJoinProject newOp = new PhyOptJoinProject(ec);
    assert op.getNumOutputs() == 1;

    PhyOptOutputIter iter = op.getOutputsIter();
    PhyOpt outOp = iter.getNext();
    assert outOp != null;
    assert outOp instanceof PhyOptProject;
    PhyOptProject project = (PhyOptProject) outOp;
    assert project.getNumInputs() == 1;

    // copy the predicates
    newOp.setPreds(op.getPreds());
    
    // copy the outer join type(if any)
    newOp.setOuterJoinType(op.getOuterJoinType());

    // Is it a stream
    newOp.setIsStream(op.getIsStream());
    
    // Is it referencing an external relation
    newOp.setExternal(op.isExternal());
    
    // Is it dependent on archived dimension
    newOp.setArchivedDim(op.isArchivedDim());
    
    // copy the table function information    
    newOp.setTableFunctionInfo(op.getTableFunctionInfo());

    // copy the expressions
    newOp.setProjs(project.getExprs());
    
    // copy the outer attrs and inner attrs
    newOp.setOuterAttrs(op.getOuterAttrs());
    newOp.setInnerAttrs(op.getInnerAttrs());
    
    newOp.copy(project);

    // copy the inputs and outputs
    project.getInputs()[0] = newOp;
    newOp.addOutput(project);

    int numInputs = op.getNumInputs();
    assert numInputs == 2;
    newOp.setNumInputs(numInputs);

    PhyOpt[] oldInputs = op.getInputs();
    PhyOpt[] newInputs = newOp.getInputs();
    for (int i = 0; i < numInputs; i++)
    {
      iter = oldInputs[i].getOutputsIter();
      while (true)
      {
        PhyOpt out = iter.getNext();
        assert out != null;
        if (out == op)
        {
          iter.set(newOp);
          break;
        }
      }
      newInputs[i] = oldInputs[i];
    }
    
    // copy the referenced queryIds
    Iterator<Integer> itrQryIds = op.getQryIds().iterator();
    assert itrQryIds != null;
    
    while (itrQryIds.hasNext())
    {
      Integer qId = itrQryIds.next();
      newOp.addQryId(qId);
    }

    assert (newOp.getIsStream() == project.getIsStream());
    ec.getPlanMgr().removePhyOpt(op);
  }

  private void transStrJoinToStrJoinProject(ExecContext ec, PhyOptStrJoin op)
      throws PhysicalPlanException
  {
    PhyOptStrJoinProject newOp = new PhyOptStrJoinProject(ec);
    assert op.getNumOutputs() == 1;

    PhyOptOutputIter iter = op.getOutputsIter();
    PhyOpt outOp = iter.getNext();
    assert outOp != null;
    assert outOp instanceof PhyOptProject;

    PhyOptProject project = (PhyOptProject) outOp;
    assert project.getNumInputs() == 1;

    // copy the predicates
    newOp.setPreds(op.getPreds());

    // Is it a stream
    newOp.setIsStream(op.getIsStream());

    // copy the expressions
    newOp.setProjs(project.getExprs());
    
    //newOp.copy(project);
    newOp.copy(project, true);
    
    // copy the inputs and outputs
    project.getInputs()[0] = newOp;
    newOp.addOutput(project);
    
    // copy external
    newOp.setExternal(op.isExternal());
    
    // copy the table function info
    newOp.setTableFunctionInfo(op.getTableFunctionInfo());

    int numInputs = op.getNumInputs();
    assert numInputs == 2;
    newOp.setNumInputs(numInputs);

    PhyOpt[] oldInputs = op.getInputs();
    PhyOpt[] newInputs = newOp.getInputs();
    for (int i = 0; i < numInputs; i++)
    {
      iter = oldInputs[i].getOutputsIter();
      while (true)
      {
        PhyOpt out = iter.getNext();
        assert out != null;
        if (out == op)
        {
          iter.set(newOp);
          break;
        }
      }
      newInputs[i] = oldInputs[i];
    }

    // copy the referenced queryIds
    Iterator<Integer> itrQryIds = op.getQryIds().iterator();
    assert itrQryIds != null;
    
    while (itrQryIds.hasNext())
    {
      Integer qId = itrQryIds.next();
      newOp.addQryId(qId);
    }
    
    // copy the ordering constraint
    newOp.setOrderingConstraint(op.getOrderingConstraint());

    assert (newOp.getIsStream() == project.getIsStream());
    ec.getPlanMgr().removePhyOpt(op);
  }

  private boolean mergeProject(ExecContext ec, PhyOptProject project)
      throws PhysicalPlanException
  {
    assert (project != null);
    assert (project.getNumInputs() == 1);
    PhyOpt inOp = project.getInputs()[0];
    assert (inOp != null);

    // Remove PROJECT if it is useless
    if (project.isUseless())
      return true;

    // We cannot merge if someone is reading from the child operator.
    if (inOp.getNumOutputs() > 1)
      return false;

    if ((inOp.getOperatorKind() == PhyOptKind.PO_JOIN)
        && (inOp.getIsView()))
       return false;
    
    if(inOp.getOperatorKind() == PhyOptKind.PO_JOIN)
    {
      PhyOptJoin inOpJoin = (PhyOptJoin) inOp;

      // Transform and copy the projections
      for (int a = 0; a < project.getNumAttrs(); a++)
        transToJoinExpr(project.getExprs()[a], inOp);

      // transform the input operator to a join-project
      transJoinToJoinProject(ec, inOpJoin);

      return true;
    }

    if (inOp.getOperatorKind() == PhyOptKind.PO_STR_JOIN)
    {
      PhyOptStrJoin inOpStrJoin = (PhyOptStrJoin) inOp;

      // Transform and copy the projections
      for (int a = 0; a < project.getNumAttrs(); a++)
        transToJoinExpr(project.getExprs()[a], inOp);

      // transform the input operator to a join-project
      transStrJoinToStrJoinProject(ec, inOpStrJoin);

      return true;
    }

    return false;
  }

  /** merge projects to join 
   * @param ec TODO*/
  private PhyOpt mergeProjectsToJoin(ExecContext ec, PhyOpt op) throws PhysicalPlanException
  {
    PhyOpt root = op;

    // cant go below the views
    if (op.getIsView())
      return root;

    for (int i = 0; i < op.getNumInputs(); i++)
      mergeProjectsToJoin(ec, op.getInputs()[i]);

    if ((op.getOperatorKind() == PhyOptKind.PO_PROJECT)
        && mergeProject(ec, (PhyOptProject) op))
    {
      if (op.getNumOutputs() == 0)
        root = op.getInputs()[0];
      shortCircuitInputOutput(ec, op); // frees all references to op
    }

    return root;
  }


  
  /**
   * Short circuit input and output(s) of subquery operators
   * @param ec Execution context    
   * @param op Root of the operator tree 
   * @return Root of the new optimized/transformed tree.
   * @throws PhysicalPlanException
   *  
   */
  private PhyOpt mergeSubquerySources(ExecContext ec, PhyOpt op) 
  throws PhysicalPlanException 
  {
    PhyOpt root = op;
    
    // can't go below the view
    if (op.getIsView())
      return root;
    
    for (int i = 0; i < op.getNumInputs(); i++) 
      mergeSubquerySources(ec, op.getInputs()[i]);

    if ((op.getOperatorKind() == PhyOptKind.PO_SUBQUERY_SRC))
    {
      int id = op.getId();
      assert op.getNumInputs() == 1;   // exactly one input 
      root = op.getInputs()[0];

      // Merge subquery operator with underlying sub query operator if both
      // operator are equal
      if(root.getOperatorKind() == PhyOptKind.PO_SUBQUERY_SRC)
      {
        if(root.equals(op))
        {
          shortCircuitInputOutput(ec, op);
          return root;
        }
      }
      
      // If the subquery produces a stream, then the subquery operator has an
      // additional psuedo column for ELEMENT_TIME.
      // So Copy over the attribute metadata of subquery operator to input 
      // operator 
      if(op.getIsStream())
      {
        root = op;
      }
      else
      {
        shortCircuitInputOutput(ec, op); // frees all references to op
      }
      LogUtil.fine(LoggerType.TRACE,"** Subquery operator " + id + " removed **");
    }   
    return root;
  }

  
  /**
   * Performs optimizations implemented in this class
   * @param ec TODO
   */

  public PhyOpt optimizePlan(ExecContext ec, PhyOpt op) throws PhysicalPlanException
  {
    /**
     *  remove subquery nodes
     */
    op = mergeSubquerySources(ec, op);
    
    /**
     * merge selects
     */
    op = mergeSelects(ec, op);
    

    /** merge join projects */
    op = mergeProjectsToJoin(ec, op);

    
    return op;
  }


}
