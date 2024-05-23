/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SetOpSubqueryInterp.java /main/4 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2011, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/18/14 - overriding isDependentOnPartnStream
    pkali       07/05/12 - added isAssignableFrom for datatype comparison
    pkali       04/03/12 - included datatype arg in Attr instance
    vikshukl    09/26/11 - subquery support
    vikshukl    09/26/11 - Creation
 */

package oracle.cep.semantic;

import java.util.ArrayList;

import oracle.cep.common.Datatype;
import oracle.cep.common.RelSetOp;
import oracle.cep.common.RelToStrOp;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPQueryNode;
import oracle.cep.parser.CEPSetopSubqueryNode;


/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SetOpSubqueryInterp.java /main/4 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */

/**
 * The interpreter for CEPSetopSubqueryNode parse tree node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class SetOpSubqueryInterp extends QueryRelationInterp {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException 
  {
    RelSetOp        setop;    
    Datatype        attrType;
    Expr            attrExpr;
    String          attrName;
    CEPQueryNode    left, right;
    NodeInterpreter queryInterp;   
    
    ExecContext ec = ctx.getExecContext();
    assert node instanceof CEPSetopSubqueryNode;
    CEPSetopSubqueryNode setNode = (CEPSetopSubqueryNode)node;
    setop = setNode.getRelSetOp();
    
    /* post semantic presentation of the query for later phases */
    SetOpSubquery setQuery = new SetOpSubquery();
    ctx.setSemQuery(setQuery);
    setQuery.setSymTable(ctx.getSymbolTable());
    
    super.interpretNode(node, ctx);
    
    /* interpret the left query first */
    // TODO: convert it into a method
    SemContext leftctx = new SemContext(ec);
    leftctx.setQueryObj(ctx.getQueryObj());
    leftctx.setSchema(ctx.getSchema());    
    SymbolTable leftsymtab = new SymbolTable();
    leftctx.setSymbolTable(leftsymtab);    
    left = setNode.getLeft();
    queryInterp = InterpreterFactory.getInterpreter(left);
    queryInterp.interpretNode(left, leftctx);    
    /* now obtain the semantic presentation of left query */
    SemQuery leftSemQuery = leftctx.getSemQuery();   
    if (!leftSemQuery.isStreamQuery())
      if  (leftSemQuery.isMonotonicRel(ec))
        leftSemQuery.setR2SOp(RelToStrOp.ISTREAM);
    
    /* now interpret the right query */
    SemContext rightctx = new SemContext(ec);
    rightctx.setQueryObj(ctx.getQueryObj());
    rightctx.setSchema(ctx.getSchema());
    SymbolTable rightsymtab = new SymbolTable();
    rightctx.setSymbolTable(rightsymtab);    
    right = setNode.getRight();
    queryInterp = InterpreterFactory.getInterpreter(right);
    queryInterp.interpretNode(right, rightctx);    
    /* now obtain the semantic presentation of left query */
    SemQuery rightSemQuery = rightctx.getSemQuery();
    if (!rightSemQuery.isStreamQuery())
      if  (rightSemQuery.isMonotonicRel(ec))
        rightSemQuery.setR2SOp(RelToStrOp.ISTREAM);
    
    // make sure the schema matches for left and right set operands
    if ((setop != RelSetOp.NOT_IN) && (setop != RelSetOp.IN))
    {
      if (leftSemQuery.getSelectListSize() != rightSemQuery.getSelectListSize()) 
        throw new SemanticException(SemanticError.NUMBER_OF_ATTRIBUTES_MISMATCH,
                                    setNode.getLeft().getStartOffset(), 
                                    setNode.getRight().getEndOffset(),
                                    new Object[]{setNode.getLeft(),
                                                 setNode.getRight()});
      // now verify that the schemas match
      checkSchema(setNode, leftSemQuery, rightSemQuery);      
    }
    
    if (setNode.isUnionAll())
    { 
      // for 'union all' operation, the case of both stream operands is allowed 
      // other cases are not allowed
      if (leftSemQuery.isStreamQuery() && !rightSemQuery.isStreamQuery())
      {
        throw new SemanticException(SemanticError.NOT_A_RELATION_ERROR, 
        		                        setNode.getLeft().getStartOffset(), 
        		                        setNode.getLeft().getEndOffset(),
        		                        new Object[]{setNode.getLeft()});
      }
      
      if (!leftSemQuery.isStreamQuery() && rightSemQuery.isStreamQuery())
      {
        throw new SemanticException(SemanticError.NOT_A_RELATION_ERROR, 
        		                        setNode.getRight().getStartOffset(), 
        		                        setNode.getRight().getEndOffset(),
        		                        new Object[]{setNode.getRight()});
      }
    }
    else
    {
      if (leftSemQuery.isStreamQuery())
        throw new SemanticException(SemanticError.NOT_A_RELATION_ERROR,
                                    setNode.getLeft().getStartOffset(), 
                                    setNode.getLeft().getEndOffset(),
                                    new Object[]{setNode.getLeft()});
      
      if (rightSemQuery.isStreamQuery())
        throw new SemanticException(SemanticError.NOT_A_RELATION_ERROR, 
        	                          setNode.getRight().getStartOffset(), 
        	                          setNode.getRight().getEndOffset(),
        	                          new Object[]{setNode.getRight()});
    }
        
    /* add persistent entities referenced by these two operands */
    regsiterTables(setQuery, leftSemQuery, rightSemQuery);
       
    if ((setop == RelSetOp.NOT_IN) || (setop == RelSetOp.IN)) 
    {
    }
    
    // Now that things look ok semantically, set up the query block properly.
    setQuery.setLeft(leftSemQuery);
    setQuery.setRight(rightSemQuery);
    setQuery.setRelSetOp(setop);
    
    // Use alias of the left subquery (Oracle RDBMS does the same)
    Expr expr;
    for (int i = 0; i < leftSemQuery.getSelectListSize(); i++) 
    {
      expr = leftSemQuery.getSelectListExprs().get(i);
      attrName = expr.getName();
      attrName = attrName.substring(attrName.lastIndexOf('.') + 1);
      attrType = expr.getReturnType();      
      attrExpr = new AttrExpr(new Attr(0, i, attrName, attrType), attrType);
      attrExpr.setName(attrName, false, false);
      ctx.addSelectListExpr(attrExpr);
    }
    
    setQuery.setSelectListExprs(ctx.getSelectList());
    setQuery.setIsUnionAll(setNode.isUnionAll());
           
    // if any of the inputs is archived, set the flag to true.    
    if((leftSemQuery.isDependentOnArchivedRelation())
       ||(rightSemQuery.isDependentOnArchivedRelation()))
      setQuery.setIsDependentOnArchivedRelation(true);
    else
      setQuery.setIsDependentOnArchivedRelation(false);    
    
    if(leftSemQuery.isDependentOnPartnStream() || rightSemQuery.isDependentOnPartnStream())
      setQuery.setIsDependentOnPartnStream(true);
    else
      setQuery.setIsDependentOnPartnStream(false);
  }

  private void checkSchema(CEPSetopSubqueryNode setNode, 
                           SemQuery left,
                           SemQuery right)
  throws CEPException
  {
    // we have already verified that both left and right have the same
    // number of items in the project list
    int selectCount = left.getSelectListSize();    
    ArrayList<Expr> leftsel  =  left.getSelectListExprs();
    ArrayList<Expr> rightsel =  right.getSelectListExprs();
    Expr leftexpr, rightexpr;
    
    for (int i = 0; i < selectCount; i++) 
    {
      leftexpr = leftsel.get(i);
      rightexpr = rightsel.get(i);
      
      if (leftexpr.getReturnType() != rightexpr.getReturnType()
          && !leftexpr.getReturnType().isAssignableFrom(
                                         rightexpr.getReturnType()))
      {
        throw new 
          SemanticException(SemanticError.SCHEMA_MISMATCH_IN_SETOP,
                            setNode.getStartOffset(), 
                            setNode.getEndOffset(),
                            new Object[]{i+1, setNode.getLeft(), 
                            setNode.getRight()});
      }
      
      if((!setNode.isUnionAll()) && 
          (leftexpr.getReturnType()== Datatype.XMLTYPE)) 
      {
        throw new SemanticException(SemanticError.INVALID_XMLTYPE_USAGE,
                                    setNode.getStartOffset(), 
                                    setNode.getEndOffset(), 
                                    new Object[]{leftexpr.getName()});
      }
      
      /* 
       * Require subquery's project list items to be explicitly aliased using
       * expr AS <alias>. This is needed to deal with cases where set operations
       * are specified in the from clause and on top of which a pattern query is 
       * defined which mandates names.
       * Ideally a check of the left subquery is enough, but this is more 
       * conservative for now.  
       */
      checkalias(setNode.getLeft(), leftexpr, i);
      checkalias(setNode.getRight(), rightexpr, i);      
    }    
  }

  private void checkalias(CEPQueryNode query, Expr expr, int index) 
    throws SemanticException 
  {
    if (expr.getExprType() != ExprType.E_ATTR_REF)
    {
      if (!expr.isUserSpecifiedName())
        throw new 
        SemanticException(SemanticError.SUBQUERY_SELECT_EXPR_NOT_ALIASED,
            query.getStartOffset(), query.getEndOffset(),
            new Object[]{index+1});        
    }
    return;    
  }

  private void regsiterTables(SetOpSubquery setQuery, 
                              SemQuery left,
                              SemQuery right) 
  {
    ArrayList<Integer> tabids = left.getReferencedTables();
    int count = left.getNumRefTables();
    for (int i=0; i < count; i++)
      setQuery.addReferencedTable(tabids.get(i));

    /* now do the same for right */
    tabids = right.getReferencedTables();
    count = right.getNumRefTables();
    for (int i=0; i < count; i++)
      setQuery.addReferencedTable(tabids.get(i));    
    return;
  }
}