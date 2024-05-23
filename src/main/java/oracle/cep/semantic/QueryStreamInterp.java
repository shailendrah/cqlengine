/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/QueryStreamInterp.java /main/4 2009/12/24 20:10:21 vikshukl Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 The interpreter for the CEPQueryStreamNode parse tree node

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    08/24/09 - semantic checks for ISTREAM (R) DIFFERENCE USING (..)
 skmishra    06/03/09 - adding check for xmltype in relation
 parujain    03/12/09 - make interpreters stateless
 najain      04/06/06 - cleanup
 anasrini    02/27/06 - fix NPE 
 anasrini    02/20/06 - Creation
 anasrini    02/20/06 - Creation
 anasrini    02/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/QueryStreamInterp.java /main/4 2009/12/24 20:10:21 vikshukl Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.common.RelToStrOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPAttrNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPIntConstExprNode;
import oracle.cep.parser.CEPQueryStreamNode;
import oracle.cep.parser.CEPRelationNode;

/**
 * The interpreter that is specific to the CEPQueryStreamNode parse tree node.
 * <p>
 * This is private to the semantic analysis module.
 * 
 * @since 1.0
 */

class QueryStreamInterp extends NodeInterpreter
{

  // NOTE: This class should be stateless in order to run DDLS in parallel
  // Ref bug.8290135
  void interpretNode(CEPParseTreeNode node, SemContext ctx) throws CEPException
  {

    RelToStrOp r2sop;
    CEPRelationNode relNode;
    CEPQueryStreamNode qsNode;
    NodeInterpreter relInterp;
    CEPExprNode[]   usingExprList;

    super.interpretNode(node, ctx);

    assert node instanceof CEPQueryStreamNode;
    qsNode = (CEPQueryStreamNode) node;
    r2sop = qsNode.getRelToStrOp();
    relNode = qsNode.getRelationNode();

    relInterp = InterpreterFactory.getInterpreter(relNode);
    relInterp.interpretNode(relNode, ctx);

    /* retrieve using clause expression list, if any */
    usingExprList = qsNode.getUsingClause();


    // check to see that the input to the xstream clause
    // does not contain xmltype.
    // Note:ref cql.yy --> xstream clause may have an SFW block or a binary
    // block
    // as input. however, the binary clause already has semantic checks for
    // xmltype
    // so, at this stage only check for sfw block.

    if (ctx.getSelectList() != null) {
      Expr[] selectExprs = ctx.getSelectList();
      for (Expr e : selectExprs) {
        if (e.getReturnType() == Datatype.XMLTYPE)
          throw new SemanticException(SemanticError.INVALID_XMLTYPE_USAGE,
                                      relNode.getStartOffset(), 
                                      relNode.getEndOffset(), new String[]
                                      { " Select list " });
      }
    }
    ctx.getSemQuery().setR2SOp(r2sop);

    if (usingExprList != null) {
      /* 
       * Make sure the expressions in the USING clause and SELECT list are
       * consistent.  Only aliases (attributes) and positions can be specified
       * in the USING clause, but they refer to their corresponding SELECT
       * list expressions via alias or ordinal positions.
       *
       * Expressions in USING clause can't be a superset of SELECT
       * expressions.
       *
       * If the query uses aliase, the columns in the USING clause must be
       * specified using aliases and not real column names.
       *
       * If the SELECT list has an item with a complex expression, it must be
       * aliased since expressions don't have well-defined names to compare
       * against that users can specify.
       *
       */
      int selIndex;
      for (int i = 0; i < usingExprList.length; i++)
      {
        if (usingExprList[i] instanceof CEPAttrNode) {
          selIndex = interpretUsingExprNode((CEPAttrNode)usingExprList[i],
                                        ctx);
        }
        else if (usingExprList[i] instanceof CEPIntConstExprNode) {
          selIndex = interpretUsingExprNode((CEPIntConstExprNode)usingExprList[i],
                                        ctx);
        }
        else {            
          throw new 
            SemanticException(SemanticError.USING_CLAUSE_EXPR_NOT_AN_ATTRIBUTE,
                              usingExprList[i].getStartOffset(),
                              usingExprList[i].getEndOffset());
        }
        /* add to semantic context the position of SELECT list item that this
         * USING clause expression refers to */
        ctx.addUsingClauseExprIndex(selIndex);
      }

      /* add it to SemQuery (and hence to SFWQuery and SetOpQuery) */
      ctx.getSemQuery().setUsingExprListMap(ctx.getUsingExprListMap());

    } /* end if: USING clause is specified */
  }


  private int interpretUsingExprNode(CEPIntConstExprNode exprNode,
                                     SemContext ctx) 
      throws CEPException
  {
    /* expressions in USING clause are specified using positions and not
     * attributes
     *
     * USING clause expression list must be a subset of the select list. So
     * current expression denoted with the position must be smaller than the
     * select list size.
     *
     */
    int pos = exprNode.getValue();
    Expr selectList[] = ctx.getSelectList();

    if (pos <= 0) {
      throw new 
        SemanticException(SemanticError.USING_CLAUSE_EXPR_NOT_A_VALID_POSITION,
                          exprNode.getStartOffset(),
                          exprNode.getEndOffset(),
                          new Object[]{pos});
    }

    if (selectList.length < pos) {
      throw new 
        SemanticException(SemanticError.USING_CLAUSE_EXPR_NOT_A_VALID_POSITION,
                          exprNode.getStartOffset(),
                          exprNode.getEndOffset(),
                          new Object[]{pos});
    }
    else if (selectList[pos -1].getReturnType() == Datatype.XMLTYPE) {
      throw new
        SemanticException(SemanticError.INVALID_XMLTYPE_USAGE,
                          exprNode.getStartOffset(), 
                          exprNode.getEndOffset(),
                          new Object[]{pos});
    }
    else {
      return pos-1;   /* we just need to record the offset of select
                       * expression */
    }
  }

  private int interpretUsingExprNode(CEPAttrNode exprNode,
                                     SemContext ctx)
      throws CEPException
  {
    /* all expressions (aliases and positions) must refer to valid SELECT list
     * expressions. 
     */
    Expr selectList[] = ctx.getSelectList();
    NodeInterpreter  exprInterp;
    int              selIndex = -1;

    for (int i = 0; i < selectList.length; i++) {
      /* if a match is found, return the index of SELECT expression, and
       * return.
       */
      if (selectList[i].getReturnType() == Datatype.XMLTYPE) {
        throw new
          SemanticException(SemanticError.INVALID_XMLTYPE_USAGE,
                            exprNode.getStartOffset(), 
                            exprNode.getEndOffset(),
                            new Object[]{i});
      }

      if (!(selectList[i] instanceof AttrExpr)) {
        /* for anything other than simple attributes, i.e. complex
         * expressions, the user must alias SELECT expression
         */
        if (!selectList[i].isUserSpecifiedName()) {
          throw new 
            SemanticException(SemanticError.USING_CLAUSE_SELEXPR_NOT_ALIASED);
        }
        else {
          /* user has specified an alias */ 
          if (selectList[i].getName().equals(exprNode.getAttrName())) {
            selIndex = i;
            break;  /* found a match, return index (always first match) */
          }
        }
      }
      else {
        /* select list item is an attribute. select items by now are qualified
         * with relation name.
         */

        /* if using attribute name is fully qualified, compare everything */
        if (exprNode.isFullyQualifiedName()) {
          if (!selectList[i].isUserSpecifiedName()) {
            /* split select item into relation and attribute name, 0 is
             * relation name and 1 is attribute name */
            String selName[] = (selectList[i].getName()).split("\\.");
            if (selName[0].equals(exprNode.getVarName()) &&
                selName[1].equals(exprNode.getAttrName())) {
              selIndex = i;
              break;
            }
          }
          else {
            if (selectList[i].getName().equals(exprNode.getAttrName())) {
              selIndex = i;
              break;
            }
          }
        }
        else {
          /* just compare attribute names in using clause */
          if (!selectList[i].isUserSpecifiedName()) {
            /* split select item into relation and attribute name, 0 is relation
             * name and 1 is attribute name */
            String selName[] = (selectList[i].getName()).split("\\.");
            if (selName[1].equals(exprNode.getAttrName())) {
              selIndex = i;
              break;
            }
          }
          else {
            if (selectList[i].getName().equals(exprNode.getAttrName())) {
              selIndex = i;
              break;
            }
          }
        }
      }
    } /* end: iteration over select list */

    if (selIndex == -1)  /* no matching SELECT expression found */
    {
      throw new 
        SemanticException(SemanticError.USING_CLAUSE_EXPR_NOT_A_VALID_SELEXPR,
                          exprNode.getStartOffset(),
                          exprNode.getEndOffset());
    }

    return selIndex;
  }
}
