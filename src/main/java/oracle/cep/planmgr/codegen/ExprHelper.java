/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExprHelper.java /main/58 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Helper class dealing with the code generation related to expressions

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    01/17/12 - adding new to_char datetime functions
 anasrini    09/09/11 - XbranchMerge anasrini_bug-12943064_ps5 from
                        st_pcbpel_11.1.1.4.0
 anasrini    09/07/11 - separate builtIn and nativeImpl concepts
 sbishnoi    08/27/11 - adding support for interval year to month
 vikshukl    05/26/11 - XbranchMerge vikshukl_bug-11736605_ps5 from
                        st_pcbpel_11.1.1.4.0
 vikshukl    05/11/11 - short circuit eval for logical operators
 sborah      02/02/11 - compile evals after adding instructions
 udeshmuk    11/11/10 - support for to_bigint(timestamp)
 sborah      04/08/10 - char to number functions
 sborah      02/15/10 - is_null_support for xml and obj
 sborah      02/09/10 - equality op for xmltype
 alealves    11/27/09 - Data cartridge context, default package support
 udeshmuk    11/23/09 - call createDocInstance from instExprDest
 sborah      10/14/09 - support for bigdecimal
 udeshmuk    09/09/09 - make the class public.
 sborah      06/21/09 - support for BigDecimal
 sborah      06/01/09 - support for xmltype in to_char
 sborah      05/05/09 - pass isNull() flag to boolean case
 hopark      03/16/09 - add OBJ_EQ
 udeshmuk    03/17/09 - fix problem in prev function
 hopark      02/17/09 - support boolean as external datatype
 hopark      02/17/09 - objtype support
 sborah      02/10/09 - support for is_not_null
 sbishnoi    12/24/08 - adding support for to_timestamp(bigint)
 hopark      10/10/08 - remove statics
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 skmishra    08/13/08 - adding order_by_expr to instExprDest
 sbishnoi    08/06/08 - support for nanosecond; changing prev() signature
 sbishnoi    06/20/08 - support of to_char for other datatypes
 skmishra    06/11/08 - adding xmlconcat, xmlparse
 sbishnoi    06/19/08 - support for to_char(int)
 parujain    05/19/08 - evalname
 parujain    05/01/08 - XMLElement support
 sbishnoi    04/21/08 - support for mod function
 hopark      03/05/08 - xml spill
 udeshmuk    02/21/08 - add constTimestamp 
 najain      02/04/08 - bug fix
 udeshmuk    01/31/08 - support for double data type.
 sbishnoi    01/20/08 - adding support for char functions
 udeshmuk    01/13/08 - pass bNull flag to ConstTupleSpec in processConstExpr.
 najain      11/01/07 - xmltype support
 najain      10/23/07 - add XMLT_CPY
 udeshmuk    10/17/07 - removing getUDAop, s_UDAPlusMapping, s_UDAMinusMapping
 udeshmuk    10/16/07 - commenting code that supports sum(interval).
 udeshmuk    10/12/07 - support for max and min on char and byte data types.
 rkomurav    09/19/07 - support prev(a.ce.,n,n)
 hopark      09/04/07 - eval optimize
 rkomurav    09/06/07 - support prev(A.c1,n)
 rkomurav    05/22/07 - add APIs for dynamic start role support
 rkomurav    05/22/07 - add APIs for dynamic start role support
 parujain    05/02/07 - UserDef Functions Stats
 parujain    03/30/07 - Support CASE
 rkomurav    03/16/07 - PREV support
 sbishnoi    02/27/07 - nvl support for interval data type
 parujain    01/18/07 - fix bigint
 rkomurav    12/14/06 - add getNULLOp, getSUMOp
 parujain    11/20/06 - XOR implementation
 parujain    11/16/06 - OR/NOT Operator implementation
 hopark      11/16/06 - add bigint datatype
 parujain    11/09/06 - Logical Operators implementation
 parujain    11/02/06 - Base/Complex Boolean Expr
 dlenkov     10/16/06 - byte datatype support
 parujain    10/12/06 - interval timestamp operations
 rkomurav    10/08/06 - make instExpr public
 anasrini    10/09/06 - support for SYSTIMESTAMP
 parujain    10/05/06 - Generic timestamp datatype
 parujain    10/02/06 - Support for like
 parujain    09/28/06 - is null implementation
 parujain    09/25/06 - NVL Implementation
 dlenkov     09/22/06 - conversion support
 parujain    09/21/06 - To_timestamp built-in function
 najain      09/22/06 - function overloading
 najain      09/13/06 - support built-in functions
 najain      09/11/06 - add concatenate
 parujain    08/11/06 - cleanup planmgr
 parujain    08/10/06 - Timestamp datatype
 anasrini    07/16/06 - support for user defined aggregations 
 anasrini    07/06/06 - user function factorization 
 anasrini    06/19/06 - support for functions 
 najain      06/16/06 - cleanup
 anasrini    05/30/06 - add method getEqOp 
 najain      04/27/06 - support for user-defined functions 
 najain      04/24/06 - input takes in a array of roles
 anasrini    04/07/06 - make getCopyOp package scope
 anasrini    03/30/06 - add instBoolExpr 
 anasrini    03/14/06 - Creation
 anasrini    03/14/06 - Creation
 anasrini    03/14/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/ExprHelper.java /main/51 2010/11/22 07:07:06 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import oracle.cep.common.ArithOp;
import oracle.cep.common.AttributeMetadata;
import oracle.cep.common.CompOp;
import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.common.LogicalOp;
import oracle.cep.common.UnaryOp;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CodeGenError;
import oracle.cep.execution.internals.AInstr;
import oracle.cep.execution.internals.AOp;
import oracle.cep.execution.internals.BInstr;
import oracle.cep.execution.internals.BOp;
import oracle.cep.execution.internals.CaseCondition;
import oracle.cep.execution.internals.CaseInstr;
import oracle.cep.execution.internals.Column;
import oracle.cep.execution.internals.IAEval;
import oracle.cep.execution.internals.IBEval;
import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.execution.internals.XMLConcatInstr;
import oracle.cep.execution.internals.XMLElementInstr;
import oracle.cep.execution.internals.XmlAttrName;
import oracle.cep.execution.internals.factory.AEvalFactory;
import oracle.cep.execution.internals.factory.BEvalFactory;
import oracle.cep.execution.xml.PreparedXQuery;
import oracle.cep.execution.xml.XMLItem;
import oracle.cep.execution.xml.XmlManager;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UserDefinedFunction;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.SimpleFunction;
import oracle.cep.phyplan.attr.Attr;
import oracle.cep.phyplan.attr.CorrAttr;
import oracle.cep.phyplan.expr.BaseBoolExpr;
import oracle.cep.phyplan.expr.BoolExpr;
import oracle.cep.phyplan.expr.ComplexBoolExpr;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.expr.ExprAttr;
import oracle.cep.phyplan.expr.ExprBigDecimal;
import oracle.cep.phyplan.expr.ExprBigint;
import oracle.cep.phyplan.expr.ExprBoolean;
import oracle.cep.phyplan.expr.ExprByte;
import oracle.cep.phyplan.expr.ExprCaseComparison;
import oracle.cep.phyplan.expr.ExprCaseCondition;
import oracle.cep.phyplan.expr.ExprChar;
import oracle.cep.phyplan.expr.ExprComplex;
import oracle.cep.phyplan.expr.ExprDouble;
import oracle.cep.phyplan.expr.ExprElement;
import oracle.cep.phyplan.expr.ExprFloat;
import oracle.cep.phyplan.expr.ExprInt;
import oracle.cep.phyplan.expr.ExprInterval;
import oracle.cep.phyplan.expr.ExprObject;
import oracle.cep.phyplan.expr.ExprOrderBy;
import oracle.cep.phyplan.expr.ExprSearchCase;
import oracle.cep.phyplan.expr.ExprSimpleCase;
import oracle.cep.phyplan.expr.ExprTimestamp;
import oracle.cep.phyplan.expr.ExprUserDefFunc;
import oracle.cep.phyplan.expr.ExprXQryFunc;
import oracle.cep.phyplan.expr.ExprXQryFuncKind;
import oracle.cep.phyplan.expr.ExprXmlAttr;
import oracle.cep.phyplan.expr.ExprXmlColAttVal;
import oracle.cep.phyplan.expr.ExprXmlConcat;
import oracle.cep.phyplan.expr.ExprXmlForest;
import oracle.cep.phyplan.expr.ExprXmlParse;
import oracle.cep.phyplan.expr.ExprXmltype;
import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLNode;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper class dealing with the code generation (instantiation) related to
 * expressions
 *
 * @since 1.0
 */

public class ExprHelper
{

  public static class Addr
  {
    public int role;

    public int pos;

    Addr(int role, int pos)
    {
      this.role = role;
      this.pos = pos;
    }
  }
  
  /** This address is mainly used by boolean expressions */
  public static class BoolAddr extends Addr
  {
    int bitPos;
  
    BoolAddr(int role, int pos, int bitPos)
    {
      super(role, pos);
      this.bitPos = bitPos;
    }
  }

  /**
   * The purpose of this method is to encode the provided arithmetic expression
   * as a sequence of instructions as supported by AEval.
   * <p>
   * These instructions being similar in style to assembly language
   * instructions, specifying the instruction involves specifying the
   * "addresses" where the operands may be found, the operation to perform on
   * the operands and the "address" where the result is to be placed.
   * <p>
   * An address is specified by a (role, pos) pair where role refers to an index
   * into array of tuples and pos refers to the position of the attribute in the
   * tuple.
   * <p>
   * The array of tuples has tuples performing various "roles". This array will
   * be part of an evaluation context at runtime. There are roles for tuples
   * serving as scratch or temporary space needed for intermediate steps of the
   * computation, a tuple which will host all the constants involved in the
   * expressions, tuples representing the input tuples (probably from child
   * operators) and a tuple to hold the output of the expression evaluation.
   * <p>
   * A large part of expression instantiation goes in determining the scratch
   * requirements as well as the tuple specification of the scratch and constant
   * tuples as well as instantiating the constant tuple and filling in the
   * constant values
   * 
   * @param expr
   *          the physical layer representation of the expression
   * @param eval
   *          the execution representation of the expression as a sequence of
   *          simple instructions to evaluate the expression
   * @param evalCtx
   *          context information used to compute scratch and constant
   *          requirements
   * @param outRole
   *          the role part of the destination address. This is where the result
   *          of evaluating the input expression should be stored at run time.
   * @param outPos
   *          the position part of the destination address.
   * @param inpRoles
   *          the role indexes corresponding to the tuples that this operator
   *          gets from its inputs from.
   */
  static void instExprDest(ExecContext ec, Expr expr, IAEval eval, EvalContextInfo evalCtx,
      int outRole, int outPos, int[] inpRoles) throws CEPException
  {
    //dynamic start role is not applicatble here
    evalCtx.createDocInstance();
    instExprDest(ec, expr, eval, evalCtx, outRole, outPos, inpRoles, -1, -1);
  }
  
  /**
   * All the parameters same as instExprDest() without the dynamic start role
   * @param expr
   * @param eval
   * @param evalCtx
   * @param outRole
   * @param outPos
   * @param inpRoles
   * @param dynStartRole
   *          This is the starting role number of the set of roles created 
   *          dynamically for pattern. its value is -1, when its not relevant
   * @param dynPrevRole
   *          This is the starting role number of the set of roles created
   *          dynamically for PREV function. its value is -1 when irrelevant
   * @throws CEPException
   */
  static void instExprDest(ExecContext ec, Expr expr, IAEval eval, EvalContextInfo evalCtx,
      int outRole, int outPos, int[] inpRoles, int dynStartRole,
      int dynPrevRole) throws CEPException
  {
    if(evalCtx.getDummyDoc() == null)
      evalCtx.createDocInstance();
    Datatype dt = expr.getType();
    ConstTupleSpec ct = evalCtx.ct;
    ExprKind kind;
    int inpRole;
    int pos;
    AInstr instr = new AInstr();
    Attr attr;

    if (eval == null)
      eval = AEvalFactory.create(ec);

    kind = expr.getKind();

    if (kind == ExprKind.CONST_VAL)
    {
      pos = processConstExpr(ec, expr, ct, evalCtx);

      instr.op = getCopyOp(dt);
      instr.r1 = IEvalContext.CONST_ROLE;
      instr.c1 = pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;

      eval.addInstr(instr);
    } 
    else if (kind == ExprKind.ATTR_REF)
    {
      attr = ((ExprAttr) expr).getAValue();
      
      if(attr instanceof CorrAttr && dynStartRole > 0)
      {
        CorrAttr corrAttr = (CorrAttr) attr;
        inpRole = dynStartRole + corrAttr.getBindPos();
      }
      else
        inpRole = inpRoles[attr.getInput()];
      
      pos = attr.getPos();

      instr.op = getCopyOp(dt);
      instr.r1 = inpRole;
      instr.c1 = pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;

      eval.addInstr(instr);
    } 
    else if (kind == ExprKind.COMP_EXPR)
    {
      processComplexExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
          dynStartRole, dynPrevRole);
    } 
    else if (kind == ExprKind.SEARCH_CASE)
    {
      processSearchCaseExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
          dynStartRole, dynPrevRole);
    }
    else if (kind == ExprKind.SIMPLE_CASE)
    {
      processSimpleCaseExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
          dynStartRole, dynPrevRole);
    }
    else if (kind == ExprKind.USER_DEF)
    {
      processUserDefFuncExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
          dynStartRole, dynPrevRole);
    }
    else if (kind == ExprKind.XQRY_FUNC)
    {
      processXQryFuncExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
                          dynStartRole, dynPrevRole);
    }
    else if (kind == ExprKind.XML_CONCAT_EXPR)
    {
      processXmlConcatExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
          dynStartRole, dynPrevRole);
    }
    else if (kind == ExprKind.XML_PARSE_EXPR)
    {
      processXmlParseExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
          dynStartRole, dynPrevRole);
    } 
    else if(kind == ExprKind.XMLELEMENT_EXPR)
    {
      processXMLElementExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
                            dynStartRole, dynPrevRole);
    }
    else if(kind == ExprKind.XMLFOREST_EXPR)
    {
      processXMLForestExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
                           dynStartRole, dynPrevRole);
    }
    else if(kind == ExprKind.XMLCOLATTVAL_EXPR)
    {
      processXMLColAttValExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
                              dynStartRole, dynPrevRole);
    }
    else if(kind == ExprKind.ORDER_BY_EXPR)
    {
      processOrderByExpr(ec, expr, eval, evalCtx, outRole, outPos, inpRoles,
          dynStartRole, dynPrevRole);
    }
    
    //never reach here
    else
      assert false;
  }


  static void instBoolExpr(ExecContext ec, BoolExpr expr, IBEval eval, EvalContextInfo evalCtx,
      boolean valid, int[] inpRoles, int dynStartRole, int dynPrevRole) throws CEPException
  {
    ExprKind kind;

    if (eval == null)
      eval = BEvalFactory.create(ec);

    kind = expr.getKind();

    if(kind == ExprKind.COMP_BOOL_EXPR)
    {
      processComplexBoolExpr(ec, expr, eval, evalCtx, Constants.INVALID_VALUE, Constants.INVALID_VALUE, 
                             Constants.INVALID_VALUE, valid, inpRoles, dynStartRole, dynPrevRole);
    }
    else if (kind == ExprKind.BASE_BOOL_EXPR)
    {
      processBaseBoolExpr(ec, expr, eval, evalCtx, Constants.INVALID_VALUE, Constants.INVALID_VALUE, 
                          Constants.INVALID_VALUE, valid, inpRoles, dynStartRole, dynPrevRole);
    } 

  }
  
  static void instBoolExpr(ExecContext ec, BoolExpr expr, IBEval eval,
                           EvalContextInfo evalCtx, boolean valid,
                           int[] inpRoles)
  throws CEPException
  {
    // dynamic start role is not relevant here
    instBoolExpr(ec, expr, eval, evalCtx, valid, inpRoles, -1, -1);
  }


  public static BoolAddr instBoolExprDest(ExecContext ec, BoolExpr expr,
                                          IBEval eval, EvalContextInfo evalCtx,
                                          int[] inpRoles, int dynStartRole,
                                          int dynPrevRole)
  throws CEPException
  {

    Datatype dt = expr.getType();
    TupleSpec st = evalCtx.st;
    ExprKind kind;
    int[] positions;
    int role = 0;
    int pos = 0;
    int bitPos = 0;
    boolean flag = true;

    kind = expr.getKind();

    //  only boolean
    assert (dt == Datatype.BOOLEAN ) : dt;
    role = IEvalContext.SCRATCH_ROLE;

    positions = st.addBooleanAttr();
    pos = positions[0];
    bitPos = positions[1];
   if (kind == ExprKind.COMP_BOOL_EXPR)
    {
      processComplexBoolExpr(ec, expr, eval, evalCtx, role, pos, bitPos, flag, 
          inpRoles, dynStartRole, dynPrevRole);
    } 
    else if(kind == ExprKind.BASE_BOOL_EXPR)
    {
      processBaseBoolExpr(ec, expr, eval, evalCtx, role, pos, bitPos, flag, 
          inpRoles, dynStartRole, dynPrevRole);
    }

    return new BoolAddr(role, pos, bitPos);
  }
  
  public static BoolAddr instBoolExprDest(ExecContext ec, BoolExpr expr, IBEval eval, EvalContextInfo evalCtx,
      int[] inpRoles) throws CEPException
  {
    //dynStartRole and dynPrevRole is not relevant here
    return instBoolExprDest(ec, expr, eval, evalCtx, inpRoles, -1, -1);
  }

  /**
   * The purpose of this method is to encode the provided boolean expression as
   * a sequence of instructions as supported by BEval.
   * 
   * @param bexpr
   *          the physical layer representation of a boolean expression tree -
   *          one with logical operators
   * @param eval
   *          the execution representation of a boolean expression as a sequence
   *          of simple instructions to evaluate the expression
   * @param evalCtx
   *          context information used to compute scratch 
   * @param inpRoles
   *          the role indexes corresponding to the tuples that this operator
   *          gets from its inputs from.
   * @param dynStartRole
   *          start role number of set of roles created dynamically
   *          this is needed for example pattern classB
   * @param dynPrevRole
   *          start role number of set of roles created dynamically
   *          for prev function
   */
  private static void processComplexBoolExpr(ExecContext ec, BoolExpr bexpr,
                                             IBEval eval,
                                             EvalContextInfo evalCtx,
                                             int outRole, int outPos,
                                             int bitPos, boolean valid,
                                             int[] inpRoles,
                                             int dynStartRole, int dynPrevRole)
  throws CEPException
  {
    BoolExpr left;
    BoolExpr right;
    BoolAddr leftAddr;
    BoolAddr rightAddr = null;
    BInstr instr;
    int jumploc = -1, parentloc, leftroot; // location of the jump instruction
    boolean jump = false;
    
    assert bexpr instanceof ComplexBoolExpr;
    ComplexBoolExpr expr = (ComplexBoolExpr)bexpr;

    BOp bop = getLogicalBOp(expr.getOper());
       
    if (eval == null)
      eval = BEvalFactory.create(ec);

    left = (BoolExpr)expr.getLeft();
    right = (BoolExpr)expr.getRight();

    assert left != null;
    if(right != null)
     assert left.getType() == right.getType() : left.getType() + " , "
        + right.getType();

    leftAddr = instBoolExprDest(ec, left, eval, evalCtx, inpRoles, dynStartRole,
        dynPrevRole);
    leftroot = (eval.getNumInstrs())-1;

    /* Insert a conditional JUMP instruction after left subtree
     * evaluation. The central idea is to insert conditional jump instructions
     * as in assembly language. For OR predicates the idea is to short circuit
     * the evaluation/execution of the instruction of the entire right subtree
     * in case the left substree evals to true and set that as the return
     * value of the entire tree.
     */
    if (bop == BOp.BOOL_OR || bop == BOp.BOOL_AND)
    {
      BInstr jinstr = new BInstr();
      jinstr.op = (bop == BOp.BOOL_OR) ? BOp.JUMP_IF_TRUE : BOp.JUMP_IF_FALSE;
      jinstr.addr = -1; // need to backfill once we find the instruction to
                        // jump to.
      jinstr.valid = valid;
      BInstr leftinst = eval.getInstrAtLoc(leftroot);
      jinstr.dr = leftinst.dr;
      jinstr.dc = leftinst.dc;
      jinstr.db = leftinst.db;
      eval.addInstr(jinstr);
      jumploc = (eval.getNumInstrs()) - 1;
      jump = true;
    }

    if(right != null)
      rightAddr = instBoolExprDest(ec, right, eval, evalCtx, inpRoles,
        dynStartRole, dynPrevRole);

    instr = new BInstr();
    instr.op = getLogicalBOp(expr.getOper());
    instr.r1 = leftAddr.role;
    instr.c1 = new Column(leftAddr.pos);
    instr.b1 = leftAddr.bitPos;
    if(rightAddr != null)
    {
      instr.r2 = rightAddr.role;
      instr.c2 = new Column(rightAddr.pos);
      instr.b2 = rightAddr.bitPos;
    }
    else
    {
      instr.r2 = 0;
      instr.c2 = null;
      instr.b2 = Constants.INVALID_VALUE;
    }
    instr.dr = outRole;
    instr.dc = outPos;
    instr.db = bitPos;
    instr.valid = valid;
    
    eval.addInstr(instr);

    // next backpatch the jump-to location in the jump instruction.  get the
    // location of the instruction just added. This instruction evaluates the
    // entire tree and hence is the root of left and right.
    if (jump) {
      // a jump instructon was inserted after leftAddr. (only for AND and OR)
      parentloc = (eval.getNumInstrs()) - 1;
      BInstr jinstr = eval.getInstrAtLoc(jumploc);
      jinstr.addr = parentloc;
    }
  }
  
  /**
   * The purpose of this method is to encode the provided boolean expression as
   * a sequence of instructions as supported by BEval.
   * 
   * @param expr
   *          the physical layer representation of an atomic boolean expression -
   *          one without logical operators
   * @param eval
   *          the execution representation of a boolean expression as a sequence
   *          of simple instructions to evaluate the expression
   * @param evalCtx
   *          context information used to compute scratch and constant
   *          requirements
   * @param inpRoles
   *          the role indexes corresponding to the tuples that this operator
   *          gets from its inputs from.
   * @param dynStartRole
   *          start role position for set of roles created dynamically
   *          needed for example to handle pattern class B
   * @param dynPrevRole
   *          start role position for set of roles created dynamically
   *          for prev function
   */
  private static void processBaseBoolExpr(ExecContext ec, BoolExpr bexpr,
                                          IBEval eval, EvalContextInfo evalCtx,
                                          int outRole, int outPos, int bitPos,
                                          boolean valid, int[] inpRoles, 
                                          int dynStartRole, int dynPrevRole)
  throws CEPException
  {

    Datatype dt;
    Expr left;
    Expr right;
    IAEval leftEval;
    IAEval rightEval;
    Addr leftAddr;
    Addr rightAddr;
    BInstr instr;
    
    assert bexpr instanceof BaseBoolExpr;
    BaseBoolExpr expr = (BaseBoolExpr)bexpr;

    // if it is unary operator
    if(expr.getUnary() != null)
    {
      instUnaryExpr(ec, expr, eval, evalCtx, outRole, outPos, bitPos, valid,
          inpRoles, dynStartRole, dynPrevRole);
      return;
    }
    
    if (eval == null)
      eval = BEvalFactory.create(ec);

    left = expr.getLeft();
    right = expr.getRight();

    assert left != null;
    assert right != null;
    assert left.getType() == right.getType() : left.getType() + " , "
        + right.getType();

    dt = left.getType();

    leftEval = AEvalFactory.create(ec);
    leftAddr = instExpr(ec, left, leftEval, evalCtx, inpRoles,
                        dynStartRole, dynPrevRole);
    rightEval = AEvalFactory.create(ec);
    rightAddr = instExpr(ec, right, rightEval, evalCtx, inpRoles,
                         dynStartRole, dynPrevRole);

    leftEval.compile();
    rightEval.compile();
    
    instr = new BInstr();
    instr.op = getBOp(dt, expr.getOper(), leftEval, rightEval);
    instr.r1 = leftAddr.role;
    instr.c1 = new Column(leftAddr.pos);
    instr.e1 = leftEval.isNoOP() ? null : leftEval;
    instr.r2 = rightAddr.role;
    instr.c2 = new Column(rightAddr.pos);
    instr.e2 = rightEval.isNoOP() ? null : rightEval; 
    instr.dr = outRole;
    instr.dc = outPos;
    instr.db = bitPos;
    instr.valid = valid;
 
    if((instr.op.compareTo(BOp.CHR_LIKE) == 0) ||
       (instr.op.compareTo(BOp.C_CHR_LIKE) == 0))
    {
      if(right instanceof ExprChar)
      {
        String patternString = new String(((ExprChar) right).getCValue());
        try
        {
          instr.pattern = Pattern.compile(patternString);
        }
        catch(PatternSyntaxException e)
        {
          LogUtil.severe(LoggerType.TRACE, "Failed to compile LIKE condition clause because pattern string is invalid regular expression."
            + "pattern=" + patternString + " error-message="+ e.getMessage());
          throw new CEPException(CodeGenError.INVALID_PATTERN_SYNTAX, patternString);
        }
      }
      else
        instr.pattern = null;
    }
    else
    {
      instr.pattern = null;
    }

    eval.addInstr(instr);
  }
  
  /**
   * The purpose of this method is to encode the provided boolean unary expression 
   * as a sequence of instructions as supported by IBEval.
   * 
   * @param expr
   *          the physical layer representation of an atomic boolean expression -
   *          one without logical operators
   * @param eval
   *          the execution representation of a boolean expression as a sequence
   *          of simple instructions to evaluate the expression
   * @param evalCtx
   *          context information used to compute scratch and constant
   *          requirements
   * @param inpRoles
   *          the role indexes corresponding to the tuples that this operator
   *          gets from its inputs from.
   * @param dynStartRole
   *          start role number of roles created dynamiccaly
   *          needed for eg. in processing of Pattern Class b
   * @param dynPrevRole
   *          start role number of roles created dynamiccaly
   *          needed for prev function
   * @throws CEPException
   */
  static void instUnaryExpr(ExecContext ec, BoolExpr bexpr, IBEval eval, EvalContextInfo evalCtx,
  		int outRole, int outPos, int bitPos, boolean valid, int[] inpRoles,
      int dynStartRole, int dynPrevRole)
      throws CEPException
  {

    Datatype dt;
    Expr unary;
    IAEval unaryEval;
    Addr unaryAddr;
    BInstr instr;
    
    assert bexpr instanceof BaseBoolExpr;
    BaseBoolExpr expr = (BaseBoolExpr)bexpr;
    
    if (eval == null)
      eval = BEvalFactory.create(ec);

    unary = expr.getUnary();

    assert unary != null;

    dt = unary.getType();

    unaryEval = AEvalFactory.create(ec);
    unaryAddr = instExpr(ec, unary, unaryEval, evalCtx, inpRoles, dynStartRole,
        dynPrevRole);

    unaryEval.compile();
    
    instr = new BInstr();
    instr.op = getUnaryOp(dt, expr.getUnaryOper(), unaryEval);
    instr.r1 = unaryAddr.role;
    instr.c1 = new Column(unaryAddr.pos);
    instr.e1 = unaryEval.isNoOP() ? null : unaryEval;
    instr.r2 = 0;
    instr.c2 = null;
    instr.e2 = null ;
    instr.dr = outRole;
    instr.dc = outPos;
    instr.db = bitPos;
    instr.valid = valid;

    eval.addInstr(instr);
  }

  static void instUnaryExpr(ExecContext ec, BoolExpr bexpr, IBEval eval, EvalContextInfo evalCtx,
      int outRole, int outPos, int bitPos, boolean valid, int[] inpRoles) throws CEPException
  {
    //dynStartRole and dynPrevRole is not relevant here
    instUnaryExpr(ec, bexpr, eval, evalCtx, outRole, outPos, bitPos, valid,
        inpRoles, -1, -1);
  }

  /**
   * Get the arithmetic copy operation code for the specified datatype
   * 
   * @param dt
   *          the specified datatype
   * @return the arithmetic copy operation code for the specified datatype
   */
  static AOp getCopyOp(Datatype dt)
  {
    AOp aop = null;

    switch (dt.getKind())
    {
      case INT:
        aop = AOp.INT_CPY;
        break;
      case BIGINT:
        aop = AOp.BIGINT_CPY;
        break;
      case FLOAT:
        aop = AOp.FLT_CPY;
        break;
      case DOUBLE:
        aop = AOp.DBL_CPY;
        break;
      case BIGDECIMAL:
        aop = AOp.BIGDECIMAL_CPY;
        break;
      case CHAR:
        aop = AOp.CHR_CPY;
        break;
      case BYTE:
        aop = AOp.BYT_CPY;
        break;
      case TIMESTAMP:
        aop = AOp.TIM_CPY;
        break;
      case INTERVAL:
        aop = AOp.INTERVAL_CPY;
        break;
      case INTERVALYM:
        aop = AOp.INTERVALYM_CPY;
        break;
      case XMLTYPE:
        aop = AOp.XMLT_CPY;
        break;
      case OBJECT:
        aop = AOp.OBJ_CPY;
        break;
      case BOOLEAN:
        aop = AOp.BOOLEAN_CPY;
        break;
      default:
        assert false : dt;

    }
    return aop;
  }

  /**
   * Get the arithmetic average operation code for the specified datatype
   * 
   * @param dt
   *          the specified datatype
   * @return the arithmetic average operation for the specified datatype
   */
  static AOp getAvgOp(Datatype dt)
  {
    AOp aop = null;

    switch (dt.getKind())
    {
      case INT:
        aop = AOp.INT_AVG;
        break;
      case BIGINT:
        aop = AOp.BIGINT_AVG;
        break;
      case FLOAT:
        aop = AOp.FLT_AVG;
        break;
      case DOUBLE:
        aop = AOp.DBL_AVG;
        break;
      case BIGDECIMAL:
        aop = AOp.BIGDECIMAL_AVG;
        break;
      case INTERVAL:
        aop = AOp.INTERVAL_AVG;
        break;
      case INTERVALYM:
        aop = AOp.INTERVALYM_AVG;
        break;
      default:
        assert false : dt;

    }
    return aop;
  }

  /**
   * Get the addition or subtraction operation code for the specified datatype
   * 
   * @param dt
   *          the specified datatype
   * @return the addition or subtraction operation for the specified datatype
   */
  static AOp getAddSubOp(Datatype dt, boolean plus)
  {
    AOp aop = null;

    switch (dt.getKind())
    {
      case INT:
        if (plus)
          aop = AOp.INT_ADD;
        else
          aop = AOp.INT_SUB;
        break;
      case BIGINT:
        if (plus)
          aop = AOp.BIGINT_ADD;
        else
          aop = AOp.BIGINT_SUB;
        break;
      case FLOAT:
        if (plus)
          aop = AOp.FLT_ADD;
        else
          aop = AOp.FLT_SUB;
        break;
      case DOUBLE:
        if (plus)
          aop = AOp.DBL_ADD;
        else
          aop = AOp.DBL_SUB;
        break;
      case BIGDECIMAL:
        if (plus)
          aop = AOp.BIGDECIMAL_ADD;
        else
          aop = AOp.BIGDECIMAL_SUB;
        break;
      case INTERVAL:
        if (plus)
          aop = AOp.INTERVAL_ADD;
        else
          aop = AOp.INTERVAL_SUB;
        break;      
      case INTERVALYM:
        if (plus)
          aop = AOp.INTERVALYM_ADD;
        else
          aop = AOp.INTERVALYM_SUB;
        break;      
      default:
        assert false : dt;

    }
    return aop;
  }
  
  /**
   * Get the addition or subtraction operation code for SUM aggregate
   * for the specified datatype
   * 
   * @param dt
   *          the specified datatype
   * @param plus
   *          to decidec ADD or SUB operator
   * @return the addition or subtraction operation for the specified datatype
   */
  static AOp getSumOp(Datatype dt, boolean plus)
  {
    AOp aop = null;

    switch (dt.getKind())
    {
      case INT:
        if (plus)
          aop = AOp.INT_SUM_ADD;
        else
          aop = AOp.INT_SUM_SUB;
        break;
      case FLOAT:
        if (plus)
          aop = AOp.FLT_SUM_ADD;
        else
          aop = AOp.FLT_SUM_SUB;
        break;
      case DOUBLE:
        if (plus)
          aop = AOp.DBL_SUM_ADD;
        else
          aop = AOp.DBL_SUM_SUB;
        break;
      case BIGDECIMAL:
        if (plus)
          aop = AOp.BIGDECIMAL_SUM_ADD;
        else
          aop = AOp.BIGDECIMAL_SUM_SUB;
        break;
      case BIGINT:
        if (plus)
          aop = AOp.BIGINT_SUM_ADD;
        else
          aop = AOp.BIGINT_SUM_SUB;
        break;
      case INTERVAL:
        if (plus)
          aop = AOp.INTERVAL_SUM_ADD;
        else
          aop = AOp.INTERVAL_SUM_SUB;
        break;
      case INTERVALYM:
        if (plus)
          aop = AOp.INTERVALYM_SUM_ADD;
        else
          aop = AOp.INTERVALYM_SUM_SUB;
        break;      
      default:
        assert false : dt;

    }
    return aop;
  }

  /**
   * Get the arithmetic max operation code for the specified datatype
   * 
   * @param dt
   *          the specified datatype
   * @return the arithmetic max operation for the specified datatype
   */
  static AOp getMaxOp(Datatype dt)
  {
    AOp aop = null;

    switch (dt.getKind())
    {
      case INT:
        aop = AOp.INT_UMX;
        break;
      case BIGINT:
        aop = AOp.BIGINT_UMX;
        break;
      case FLOAT:
        aop = AOp.FLT_UMX;
        break;
      case DOUBLE:
        aop = AOp.DBL_UMX;
        break;
      case BIGDECIMAL:
        aop = AOp.BIGDECIMAL_UMX;
        break;
      case TIMESTAMP:
        aop = AOp.TIM_UMX;
        break;
      case INTERVAL:
        aop = AOp.INTERVAL_UMX;
        break;
      case INTERVALYM:
        aop = AOp.INTERVALYM_UMX;
        break;
      case CHAR:
        aop = AOp.CHR_UMX;
        break;
      case BYTE:
        aop = AOp.BYT_UMX;
        break;
      default:
        assert false : dt;
    }
    return aop;
  }

  /**
   * Get the min operation code for the specified datatype
   * 
   * @param dt
   *          the specified datatype
   * @return the min operation for the specified datatype
   */
  static AOp getMinOp(Datatype dt)
  {
    AOp aop = null;

    switch (dt.getKind())
    {
      case INT:
        aop = AOp.INT_UMN;
        break;
      case BIGINT:
        aop = AOp.BIGINT_UMN;
        break;
      case FLOAT:
        aop = AOp.FLT_UMN;
        break;
      case DOUBLE:
        aop =AOp.DBL_UMN;
        break;
      case BIGDECIMAL:
        aop =AOp.BIGDECIMAL_UMN;
        break;
      case TIMESTAMP:
        aop = AOp.TIM_UMN;
        break;
      case INTERVAL:
        aop = AOp.INTERVAL_UMN;
        break;
      case INTERVALYM:
        aop = AOp.INTERVALYM_UMN;
        break;
      case CHAR:
        aop = AOp.CHR_UMN;
        break;
      case BYTE:
        aop = AOp.BYT_UMN;
        break;
      default:
        assert false : dt;
    }
    return aop;
  }

  /**
   * Get the boolean equality comparison operation code for the specified
   * datatype
   * 
   * @param dt
   *          the specified datatype
   * @return the boolean equality comparison operation code for the specified
   *         datatype
   */
  static BOp getEqOp(Datatype dt)
  {
    BOp bop = null;

    switch (dt.getKind())
    {
      case INT:
        bop = BOp.INT_EQ;
        break;
      case BIGINT:
        bop = BOp.BIGINT_EQ;
        break;
      case FLOAT:
        bop = BOp.FLT_EQ;
        break;
      case DOUBLE:
        bop = BOp.DBL_EQ;
        break;
      case BIGDECIMAL:
        bop = BOp.BIGDECIMAL_EQ;
        break;
      case CHAR:
        bop = BOp.CHR_EQ;
        break;
      case BYTE:
        bop = BOp.BYT_EQ;
        break;
      case TIMESTAMP:
        bop = BOp.TIM_EQ;
        break;
      case INTERVAL:
        bop = BOp.INTERVAL_EQ;
        break;
      case INTERVALYM:
        bop = BOp.INTERVALYM_EQ;
        break;
      case BOOLEAN:
        bop = BOp.BOOLEAN_EQ;
        break;
      case OBJECT:
        bop = BOp.OBJ_EQ;
        break;
      case XMLTYPE:
        bop = BOp.XMLTYPE_EQ;
        break;
      default:
        assert false : dt;
      
    }
    return bop;
  }

  /**
   * Get the boolean Unequality comparison operation code for the specified
   * datatype
   * 
   * @param dt
   *          the specified datatype
   * @return the boolean equality comparison operation code for the specified
   *         datatype
   */
  static BOp getNEOp(Datatype dt)
  {
    BOp bop = null;
    
    switch (dt.getKind())
    {
      case INT:
        bop = BOp.INT_NE;
        break;
      case BIGINT:
        bop = BOp.BIGINT_NE;
        break;
      case FLOAT:
        bop = BOp.FLT_NE;
        break;
      case DOUBLE:
        bop = BOp.DBL_NE;
        break;
      case BIGDECIMAL:
        bop = BOp.BIGDECIMAL_NE;
        break;
      case CHAR:
        bop = BOp.CHR_NE;
        break;
      case BYTE:
        bop = BOp.BYT_NE;
        break;
      case TIMESTAMP:
        bop = BOp.TIM_NE;
        break;
      case INTERVAL:
        bop = BOp.INTERVAL_NE;
        break;
      case INTERVALYM:
        bop = BOp.INTERVALYM_NE;
        break;
      case BOOLEAN:
        bop = BOp.BOOLEAN_NE;
        break;
      case OBJECT:
        bop = BOp.OBJ_NE;
        break;
      default:
        assert false : dt;
      
    }
    return bop;
  }
  
  /**
   * Get the boolean is NULL check operation code for the specified
   * datatype
   * 
   * @param dt
   *          the specified datatype
   * @param input
   *          aeval to decide whether a complex null check needed or a primitive null check
   * @return the boolean is NULL check operation code for the specified
   *         datatype
   */
  static BOp getNullOp(Datatype dt, IAEval input)
  {
    boolean isComplex = true;
    if (input.isNoOP())
      isComplex = false;

    switch (dt.getKind())
    {
      case INT:
        return isComplex ? BOp.C_INT_IS_NULL : BOp.INT_IS_NULL;
      case FLOAT:
        return isComplex ? BOp.C_FLT_IS_NULL : BOp.FLT_IS_NULL;
      case DOUBLE:
        return isComplex ? BOp.C_DBL_IS_NULL : BOp.DBL_IS_NULL;
      case BIGDECIMAL:
        return isComplex ? BOp.C_BIGDECIMAL_IS_NULL : BOp.BIGDECIMAL_IS_NULL;
      case CHAR:
        return isComplex ? BOp.C_CHR_IS_NULL : BOp.CHR_IS_NULL;
      case BYTE:
        return isComplex ? BOp.C_BYT_IS_NULL : BOp.BYT_IS_NULL;
      case TIMESTAMP:
        return isComplex ? BOp.C_TIM_IS_NULL: BOp.TIM_IS_NULL;
      case INTERVAL:
        return isComplex ? BOp.C_INTERVAL_IS_NULL : BOp.INTERVAL_IS_NULL;
      case INTERVALYM:
        return isComplex ? BOp.C_INTERVALYM_IS_NULL : BOp.INTERVALYM_IS_NULL;
      case BIGINT:
        return isComplex ? BOp.C_BIGINT_IS_NULL : BOp.BIGINT_IS_NULL;
      case BOOLEAN:
        return isComplex ? BOp.C_BOOL_IS_NULL : BOp.BOOL_IS_NULL;
      case XMLTYPE:
        return isComplex ? BOp.C_XMLTYPE_IS_NULL : BOp.XMLTYPE_IS_NULL;
      case OBJECT : 
        return isComplex ? BOp.C_OBJ_IS_NULL : BOp.OBJ_IS_NULL;
      default:
        assert false : dt;

    }
    //this is never reached as the assertion above fails
    return null;
  }

  /**
   * Gets the Opcode when NVL built-in function is used
   * 
   * @param firstInp
   *          First Argument Datatype
   * @param secInp
   *          Second Argument Datatype
   * @return Opcode
   */
  static AOp getNVLOp(Datatype firstInp, Datatype secInp)
  {
    AOp aop = null;

    switch (firstInp.getKind())
    {
      case INT:
        if (secInp == Datatype.FLOAT)
          aop = AOp.FLT_NVL;
        else if (secInp == Datatype.DOUBLE)
          aop = AOp.DBL_NVL;
        else if (secInp == Datatype.BIGDECIMAL)
          aop = AOp.BIGDECIMAL_NVL;
        else if (secInp == Datatype.BIGINT)
          aop = AOp.BIGINT_NVL;
        else
        {
          assert secInp == Datatype.INT;
          aop = AOp.INT_NVL;
        }
        break;

      case BIGINT:
        if (secInp == Datatype.FLOAT)
          aop = AOp.FLT_NVL;
        else if (secInp == Datatype.DOUBLE)
          aop = AOp.DBL_NVL;
        else if (secInp == Datatype.BIGDECIMAL)
          aop = AOp.BIGDECIMAL_NVL;
        else
        {
          assert ((secInp == Datatype.BIGINT) || (secInp == Datatype.INT));
          aop = AOp.BIGINT_NVL;
        }
        break;

      case FLOAT:
        if (secInp == Datatype.DOUBLE)
          aop = AOp.DBL_NVL;
        else if (secInp == Datatype.BIGDECIMAL)
          aop = AOp.BIGDECIMAL_NVL;
        else
        {
          assert ((secInp == Datatype.FLOAT) || (secInp == Datatype.INT) 
                  || (secInp == Datatype.BIGINT));
          aop = AOp.FLT_NVL;
        }
        break;
        
      case DOUBLE:
        assert ((secInp == Datatype.DOUBLE) || (secInp == Datatype.INT)
                || (secInp == Datatype.FLOAT) || (secInp == Datatype.BIGINT));
        aop = AOp.DBL_NVL;
        break;
        
      case BIGDECIMAL:
        assert ((secInp == Datatype.DOUBLE) || (secInp == Datatype.INT)
                || (secInp == Datatype.FLOAT) || (secInp == Datatype.BIGINT)
                || (secInp == Datatype.BIGDECIMAL));
        aop = AOp.BIGDECIMAL_NVL;
        break;

      case CHAR:
        assert secInp == Datatype.CHAR;
        aop = AOp.CHR_NVL;
        break;

      case BYTE:
        assert secInp == Datatype.BYTE;
        aop = AOp.BYT_NVL;
        break;

      case TIMESTAMP:
        assert secInp == Datatype.TIMESTAMP;
        aop = AOp.TIM_NVL;
        break;
        
      case INTERVAL:
        assert secInp == Datatype.INTERVAL : secInp;
        aop = AOp.INTERVAL_NVL;
        break;
        
      case INTERVALYM:
        assert secInp == Datatype.INTERVALYM;
        aop = AOp.INTERVALYM_NVL;
        break;

      case BOOLEAN:
        assert secInp == Datatype.BOOLEAN;
        aop = AOp.BOOLEAN_NVL;
        break;
        
      default:
        assert false;
    }

    return aop;
  }

  /**
   * The objective of this method is to return the "address" in the evaluation
   * context where the result corresponding to the input expression can be found
   * at run time.
   * <p>
   * Further, this method does the following -
   * <ul>
   * <li> For non-complex expressions, this method returns the address of the
   * constant or attribute in the constant or input tuple respectively </li>
   * <li> For a complex/function expression, this method reserves a slot in the
   * scratch space and also encodes the complex/function expression into a
   * sequence of instructions required to evaluate it. The address returned is
   * the address of this newly reserved slot in the scratch space </li>
   * </ul>
   * 
   * @param expr
   *          the physical layer representation of the expression
   * @param eval
   *          the execution representation of the expression as a sequence of
   *          simple instructions to evaluate the expression
   * @param evalCtx
   *          context information used to compute scratch and constant
   *          requirements
   * @param inpRoles
   *          the role indexes corresponding to the tuples that this operator
   *          gets from its inputs from.
   * @param dynStartRole
   *          start role number of roles created dynamically
   *          this is needed for eg. in processing of pattern class B
   * @param dynPrevRole
   *          start role number of roles created dynamically
   *          for prev function
   * @return address where the result corresponding to the input expression will
   *         be available at run time
   */
  public static Addr instExpr(ExecContext ec, Expr expr, IAEval eval, EvalContextInfo evalCtx,
      int[] inpRoles, int dynStartRole, int dynPrevRole) throws CEPException
  {

    Datatype dt = expr.getType();
    TupleSpec st = evalCtx.st;
    ConstTupleSpec ct = evalCtx.ct;
    ExprKind kind;
    int role;
    int pos;
    Attr attr;

    kind = expr.getKind();

    if (kind == ExprKind.CONST_VAL)
    {
      role = IEvalContext.CONST_ROLE;
      pos = processConstExpr(ec, expr, ct, evalCtx);
    } 
    else if (kind == ExprKind.ATTR_REF)
    {
      attr = ((ExprAttr) expr).getAValue();
      if(attr instanceof CorrAttr && dynStartRole > 0)
      {
        CorrAttr corrAttr = (CorrAttr) attr;
        role = dynStartRole + corrAttr.getBindPos();
      }
      else
        role = inpRoles[attr.getInput()];
      pos = attr.getPos();
    } 
    else if (kind == ExprKind.COMP_EXPR || kind == ExprKind.SEARCH_CASE || kind == ExprKind.SIMPLE_CASE)
    {
      // only integer, float, double, char, and byte return types are supported for now
      assert (dt == Datatype.INT || dt == Datatype.BOOLEAN ||dt == Datatype.FLOAT ||
              dt == Datatype.CHAR || dt == Datatype.BYTE ||
              dt == Datatype.TIMESTAMP || dt == Datatype.INTERVAL ||
              dt == Datatype.INTERVALYM ||
              dt == Datatype.BIGINT || dt == Datatype.DOUBLE ||
              dt == Datatype.BIGDECIMAL) : dt;

      role = IEvalContext.SCRATCH_ROLE;

      // reserve max_length for char
      if (dt == Datatype.CHAR)
        pos = st.addAttr(new AttributeMetadata(dt, Constants.MAX_CHAR_LENGTH, 0, 0));
      else if (dt == Datatype.BYTE)
        pos = st.addAttr(new AttributeMetadata(dt, Constants.MAX_BYTE_LENGTH, 0, 0));
      else if (dt == Datatype.BIGDECIMAL)
        pos = st.addAttr(new AttributeMetadata(dt, 0, dt.getPrecision(), 0));
      else
        pos = st.addAttr(dt);

      instExprDest(ec, expr, eval, evalCtx, role, pos, inpRoles, dynStartRole,
          dynPrevRole);
    } 
    else
    {
      //      assert kind == ExprKind.USER_DEF : kind;

      // We support variable length return types such as CHAR and BYTE
      if (dt == Datatype.CHAR)
        pos = st.addAttr(new AttributeMetadata(dt, Constants.MAX_CHAR_LENGTH, 0, 0));
      else if (dt == Datatype.BYTE)
        pos = st.addAttr(new AttributeMetadata(dt, Constants.MAX_BYTE_LENGTH, 0, 0));
      else if (dt == Datatype.BIGDECIMAL)
        pos = st.addAttr(new AttributeMetadata(dt, 0, dt.getPrecision(), 0));
      else
        pos = st.addAttr(dt);

      role = IEvalContext.SCRATCH_ROLE;
      instExprDest(ec, expr, eval, evalCtx, role, pos, inpRoles, dynStartRole,
          dynPrevRole);
    }

    return new Addr(role, pos);
  }
  
  public static Addr instExpr(ExecContext ec, Expr expr, IAEval eval, EvalContextInfo evalCtx,
      int[] inpRoles) throws CEPException
  {
    return instExpr(ec, expr, eval, evalCtx, inpRoles, -1, -1);
  }

  private static int processConstExpr(ExecContext ec, Expr expr, ConstTupleSpec ct, EvalContextInfo evalCtx)
      throws CEPException
  {

    Datatype dt = expr.getType();
    int pos = 0;

    switch (dt.getKind())
    {
      case INT:
        pos = ct.addInt(((ExprInt) expr).getIValue(), expr.isNull());
        break;
      case BIGINT:
        pos = ct.addBigint(((ExprBigint) expr).getLValue(), expr.isNull());
        break;
      case FLOAT:
        pos = ct.addFloat(((ExprFloat) expr).getFValue(), expr.isNull());
        break;
      case DOUBLE:
        pos = ct.addDouble(((ExprDouble) expr).getDValue(), expr.isNull());
        break;
      case BIGDECIMAL:
        pos = ct.addBigDecimal(((ExprBigDecimal) expr).getNValue(), expr.isNull());
        break;  
      case CHAR:
        pos = ct.addChar(((ExprChar) expr).getCValue(), expr.isNull());
        break;
      case BYTE:
        pos = ct.addByte(((ExprByte) expr).getBValue(), expr.isNull());
        break;
      case TIMESTAMP:
        pos = ct.addTimeStamp(((ExprTimestamp) expr).getTValue(), expr.isNull());
        break;
      case INTERVAL:
        pos = ct.addInterval(((ExprInterval)expr).getVValue(), expr.isNull(), 
                             false, ((ExprInterval)expr).getFormat());
        break;
      case INTERVALYM:
        pos = ct.addInterval(((ExprInterval)expr).getVValue(), expr.isNull(), 
                             true, ((ExprInterval)expr).getFormat());
        break;
      case OBJECT:
        pos = ct.addObject(((ExprObject)expr).getOValue(), expr.isNull());
        break;
      case BOOLEAN:
    	pos = ct.addBoolean(((ExprBoolean)expr).getBValue(), expr.isNull());
    	break;
      case XMLTYPE:
        XMLItem res = importToParentDoc(ec,
            ((ExprXmltype) expr).getValue(), evalCtx.getDummyDoc());
        pos = ct.addXmltype(res);
        break;
      default:
        assert false : dt;
    }
    return pos;
  }
  
  private static XMLItem importToParentDoc(ExecContext ec, Node x, XMLDocument parentDoc)
  {
    Node n = x;
    Node resFrag = parentDoc.createDocumentFragment();
    XmlManager xmlMgr = ec.getXmlMgr();
    XMLItem result = new XMLItem(xmlMgr);
    Node resNode = null;
    
    if(n.getNodeType() == Node.DOCUMENT_NODE)
    {
      NodeList nl = n.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
        // create a deep copy for our use.
        resFrag.appendChild(parentDoc.importNode(nl.item(i), true));
      }
      result.setNode((XMLNode) resFrag);
    }

    else
    {
      resNode = parentDoc.importNode(n, true);
      result.setNode((XMLNode) resNode);
    }
    
    return result;
  }

  private static void processSimpleCaseExpr(ExecContext ec, Expr expr, IAEval eval, 
      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles,
      int dynStartRole, int dynPrevRole) throws CEPException
  {
    ExprSimpleCase simpleExpr = (ExprSimpleCase)expr;
   
    if(eval == null)
      eval = AEvalFactory.create(ec);
    
    AInstr instr = new AInstr();
    
    // The expression which needs to be compared to every expr in WHEN clause
    IAEval compEval = AEvalFactory.create(ec);
    Addr compAddr = instExpr(ec, simpleExpr.getCompExpr(), compEval, evalCtx,
        inpRoles, dynStartRole, dynPrevRole);
    
    compEval.compile();
    
    Datatype dt = simpleExpr.getCompExpr().getType();
    
    int num = simpleExpr.getNumComparisons();
    assert num > 0;
    ExprCaseComparison[] comparisons = simpleExpr.getComparisons();
    CaseInstr caseInstr = new CaseInstr(num);
    caseInstr.compEval = compEval;
    
//  Process each -- WHEN cond THEN result --of CASE
    for(int i=0; i<num; i++)
    {
      CaseCondition caseCondition = new CaseCondition();
      IBEval cond = BEvalFactory.create(ec);
      IAEval rightEval = AEvalFactory.create(ec);
      Addr right = instExpr(ec, comparisons[i].getComparisonExpr(), rightEval,
          evalCtx, inpRoles, dynStartRole, dynPrevRole);
      
      rightEval.compile();
      
      BInstr binstr = new BInstr();
      binstr.op = getBOp(dt, CompOp.EQ, compEval, rightEval);
      binstr.r1 = compAddr.role;
      binstr.c1 = new Column(compAddr.pos);
      binstr.e1 = null;
      binstr.r2 = right.role;
      binstr.c2 = new Column(right.pos);
      binstr.e2 = rightEval.isNoOP() ? null : rightEval;
      binstr.dr = Constants.INVALID_VALUE;
      binstr.dc = Constants.INVALID_VALUE;
      binstr.db = Constants.INVALID_VALUE;
      binstr.valid = false;
      
      cond.addInstr(binstr);
      cond.compile();
      
      caseCondition.setCondition(cond);
      if(comparisons[i].getResultExpr() != null)
      {
        IAEval res = AEvalFactory.create(ec);
        instExprDest(ec, comparisons[i].getResultExpr(), res, evalCtx, outRole,
            outPos, inpRoles, dynStartRole, dynPrevRole);
        
        res.compile();
        
        caseCondition.setResult(res);
      }
      else
      {
        caseCondition.setResult(null);
      }
      caseInstr.conditions[i] = caseCondition;
    }
    
    // Process Else ExprprocessSimpleCaseExpr
    if(simpleExpr.getElseExpr() != null)
    {
      IAEval res = AEvalFactory.create(ec);
      instExprDest(ec, simpleExpr.getElseExpr(), res, evalCtx, outRole, outPos,
          inpRoles, dynStartRole, dynPrevRole);
      res.compile();
      caseInstr.elseResult = res;
    }
    
    instr.op = AOp.CASE_EXPR;
    instr.caseCond = caseInstr;
    instr.dr = outRole;
    instr.dc = outPos;
    eval.addInstr(instr);
  }
  
  
  private static void processSearchCaseExpr(ExecContext ec, Expr expr, IAEval eval, 
      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles,
      int dynStartRole, int dynPrevRole)
      throws CEPException
  {
    ExprSearchCase searchExpr = (ExprSearchCase)expr;
    
    if(eval == null)
      eval = AEvalFactory.create(ec);
    
    AInstr instr = new AInstr();
    
    // No of Case condition WHEN cond THEN result
    int num = searchExpr.getNumConditions();
    assert num > 0;
    ExprCaseCondition[] conditions = searchExpr.getCaseConditions();
    CaseInstr caseInstr = new CaseInstr(num);
    
    // Process each -- WHEN cond THEN result --of CASE
    for(int i=0; i<num; i++)
    {
      CaseCondition caseCondition = new CaseCondition();
      IBEval cond = BEvalFactory.create(ec);
      instBoolExpr(ec, conditions[i].getConditionExpr(), cond, evalCtx, false,
          inpRoles, dynStartRole, dynPrevRole);
      
      cond.compile();
      
      caseCondition.setCondition(cond);
      if(conditions[i].getResultExpr() != null)
      {
        IAEval res = AEvalFactory.create(ec);
        instExprDest(ec, conditions[i].getResultExpr(), res, evalCtx, outRole,
            outPos, inpRoles, dynStartRole, dynPrevRole);
        res.compile();
        caseCondition.setResult(res);
      }
      else
      {
        caseCondition.setResult(null);
      }
      caseInstr.conditions[i] = caseCondition;
    }
    
    // Process ELSE clause
    if(searchExpr.getElseExpr() != null)
    {
      IAEval res = AEvalFactory.create(ec);
      instExprDest(ec, searchExpr.getElseExpr(), res, evalCtx, outRole, outPos,
          inpRoles, dynStartRole, dynPrevRole);
      res.compile();
      caseInstr.elseResult = res;
    }
    
    instr.op = AOp.CASE_EXPR;
    instr.caseCond = caseInstr;
    instr.dr = outRole;
    instr.dc = outPos;
    eval.addInstr(instr);
  }
  
  private static void processComplexExpr(ExecContext ec, Expr expr, IAEval eval,
      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles, 
      int dynStartRole, int dynPrevRole)
      throws CEPException
  {

    ExprComplex compExpr = (ExprComplex) expr;
    Expr left = compExpr.getLeft();
    Expr right = compExpr.getRight();
    int leftRole;
    int leftPos;
    int rightRole;
    int rightPos;
    Addr leftAddr;
    Addr rightAddr;
    AInstr instr = new AInstr();
    Datatype dt = expr.getType();
    AOp aop;
    
    if(right != null)
     aop = getAOp(dt, left.getType(),right.getType(),compExpr.getOper());
    else
     aop = getAOp(dt, left.getType(),null,compExpr.getOper());

    leftAddr = instExpr(ec, left, eval, evalCtx, inpRoles, dynStartRole,
        dynPrevRole);
    leftRole = leftAddr.role;
    leftPos = leftAddr.pos;

    if (right == null) {
      rightRole = 0;
      rightPos = 0;
    }
    else {
      rightAddr = instExpr(ec, right, eval, evalCtx, inpRoles, dynStartRole, 
          dynPrevRole);
      rightRole = rightAddr.role;
      rightPos = rightAddr.pos;
    }

    instr.op = aop;
    instr.r1 = leftRole;
    instr.c1 = leftPos;
    instr.r2 = rightRole;
    instr.c2 = rightPos;
    instr.dr = outRole;
    instr.dc = outPos;

    eval.addInstr(instr);
  }

  private static void processNativeImplFuncs(ExecContext ec, Expr expr,
                                             IAEval eval,
                                             EvalContextInfo evalCtx,
                                             int outRole, int outPos,
                                             int[] inpRoles,
                                             SimpleFunction sf,
                                             int dynStartRole, int dynPrevRole)
    throws CEPException
  {
    ExprUserDefFunc userExpr = (ExprUserDefFunc) expr;
    Expr[] args = userExpr.getArgs();
    int numArgs = userExpr.getNumArgs();

    AInstr instr = new AInstr();
    
    if ((sf.getName().equalsIgnoreCase(new String("length"))) ||
        (sf.getName().equalsIgnoreCase(new String("length(char)"))) ||
        (sf.getName().equalsIgnoreCase(new String("length(byte)"))))
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.CHAR) ||
             (args[0].getType() == Datatype.BYTE) : args[0].getType();

      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      if (args[0].getType() == Datatype.CHAR)
        instr.op = AOp.CHR_LEN;
      else
      instr.op = AOp.BYT_LEN;
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if((sf.getName().equalsIgnoreCase(new String("lower"))) ||
            (sf.getName().equalsIgnoreCase(new String("lower(char)"))))
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();

      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      instr.op = AOp.LOWER;
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if((sf.getName().equalsIgnoreCase(new String("upper"))) ||
            (sf.getName().equalsIgnoreCase(new String("upper(char)"))))
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();
    
      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
    
      instr.op = AOp.UPPER;
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if((sf.getName().equalsIgnoreCase(new String("initcap"))) ||
            (sf.getName().equalsIgnoreCase(new String("initcap(char)")))
           )
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();
            
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      instr.op =  AOp.INITCAP;
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
      
    }
    else if((sf.getName().equalsIgnoreCase(new String("ltrim"))) ||
        (sf.getName().equalsIgnoreCase(new String("ltrim(char)"))) ||
        (sf.getName().equalsIgnoreCase(new String("ltrim(char,char)")))
       )
    {
      assert (numArgs == 1 || numArgs == 2): numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      
      if(numArgs == 2)
      {
        assert (args[1].getType() == Datatype.CHAR) : args[1].getType();
        Addr addr2 = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
            dynPrevRole);
        instr.op = AOp.LTRIM2;
        instr.r2 = addr2.role;
        instr.c2 = addr2.pos;
      }
      else
        instr.op = AOp.LTRIM1;
      
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
      
    }
    else if((sf.getName().equalsIgnoreCase(new String("rtrim"))) ||
        (sf.getName().equalsIgnoreCase(new String("rtrim(char)"))) ||
        (sf.getName().equalsIgnoreCase(new String("rtrim(char,char)")))
       )
    {
      assert (numArgs == 1 || numArgs == 2): numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      
      if(numArgs == 2)
      {
        assert (args[1].getType() == Datatype.CHAR) : args[1].getType();
        Addr addr2 = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
            dynPrevRole);
        instr.op = AOp.RTRIM2;
        instr.r2 = addr2.role;
        instr.c2 = addr2.pos;
      }
      else
        instr.op = AOp.RTRIM1;
      
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
      
    }
    else if((sf.getName().equalsIgnoreCase(new String("substr"))) ||
        (sf.getName().equalsIgnoreCase(new String("substr(char,int)")))
       )
    {
      assert numArgs == 2: numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();
      assert (args[1].getType() == Datatype.INT) : args[1].getType();
      
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      Addr addr2 = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      instr.op = AOp.SUBSTR;
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.r2 = addr2.role;
      instr.c2 = addr2.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
      
    }
    else if((sf.getName().equalsIgnoreCase(new String("lpad"))) ||
        (sf.getName().equalsIgnoreCase(new String("lpad(char,int)")))
       )
    {
      assert numArgs == 2: numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();
      assert (args[1].getType() == Datatype.INT) : args[1].getType();
      
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      Addr addr2 = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      instr.op = AOp.LPAD;
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.r2 = addr2.role;
      instr.c2 = addr2.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
      
    }
    else if((sf.getName().equalsIgnoreCase(new String("rpad"))) ||
        (sf.getName().equalsIgnoreCase(new String("rpad(char,int)")))
       )
    {
      assert numArgs == 2: numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();
      assert (args[1].getType() == Datatype.INT) : args[1].getType();
      
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      Addr addr2 = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      instr.op = AOp.RPAD;
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.r2 = addr2.role;
      instr.c2 = addr2.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
      
    }
    else if ((sf.getName().equalsIgnoreCase(new String("concat"))) ||
             (sf.getName().equalsIgnoreCase(new String("concat(char,char)"))) ||
             (sf.getName().equalsIgnoreCase(new String("concat(byte,byte)"))))
    {
      assert numArgs == 2 : numArgs;
      assert ((args[0].getType() == Datatype.CHAR) &&
              (args[1].getType() == Datatype.CHAR)) ||
             ((args[0].getType() == Datatype.BYTE) &&
              (args[1].getType() == Datatype.BYTE));

      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      Addr addr2 = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      if (args[0].getType() == Datatype.CHAR)
        instr.op = AOp.CHR_CONCAT;
      else
      instr.op = AOp.BYT_CONCAT;
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.r2 = addr2.role;
      instr.c2 = addr2.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("to_int"))) ||
        (sf.getName().equalsIgnoreCase(new String("to_int(char)"))) )
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();
      
      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      
      
      instr.op = AOp.CHR_TO_INT;
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("to_float"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_float(int)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_float(bigint)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_float(char)"))) )
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.INT || 
              args[0].getType() == Datatype.BIGINT ||
              args[0].getType() == Datatype.CHAR) : args[0].getType();

      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      switch(args[0].getType().getKind())
      {
        case INT:
          instr.op = AOp.INT_TO_FLT;
          break;
        case BIGINT:
          instr.op = AOp.BIGINT_TO_FLT;
          break;
        case CHAR:
          instr.op = AOp.CHR_TO_FLT;
          break;
      }
    
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("to_bigint"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_bigint(int)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_bigint(char)")))|| 
             (sf.getName().equalsIgnoreCase(new String("to_bigint(timestamp)"))) )
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.INT ||
              args[0].getType() == Datatype.CHAR ||
              args[0].getType() == Datatype.TIMESTAMP) : args[0].getType();

      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      switch(args[0].getType().getKind())
      {
        case INT:
          instr.op = AOp.INT_TO_BIGINT;
          break;
        case CHAR:
          instr.op = AOp.CHR_TO_BIGINT;
          break;
        case TIMESTAMP:
        	instr.op = AOp.TIMESTAMP_TO_BIGINT;
        	break;
      }
      
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("to_double"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_double(int)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_double(bigint)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_double(float)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_double(char)"))) )
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.INT || 
              args[0].getType() == Datatype.BIGINT ||
              args[0].getType() == Datatype.FLOAT || 
              args[0].getType() == Datatype.CHAR) : args[0].getType();

      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      switch(args[0].getType().getKind())
      {
        case INT :
          instr.op = AOp.INT_TO_DBL;
          break;
        case BIGINT :
          instr.op = AOp.BIGINT_TO_DBL;
          break;
        case FLOAT :
          instr.op = AOp.FLT_TO_DBL;
          break;
        case CHAR :
          instr.op = AOp.CHR_TO_DBL;
          break;
      }
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("to_number"))) ||
        (sf.getName().equalsIgnoreCase(new String("to_number(int)"))) ||
        (sf.getName().equalsIgnoreCase(new String("to_number(bigint)"))) ||
        (sf.getName().equalsIgnoreCase(new String("to_number(float)"))) ||
        (sf.getName().equalsIgnoreCase(new String("to_number(double)"))) ||
        (sf.getName().equalsIgnoreCase(new String("to_number(char)")))
        )
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.INT || 
          args[0].getType() == Datatype.BIGINT ||
          args[0].getType() == Datatype.FLOAT ||
          args[0].getType() == Datatype.DOUBLE ||
          args[0].getType() == Datatype.CHAR) : args[0].getType();
      
      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      
      switch(args[0].getType().getKind())
      {
        case INT :
          instr.op = AOp.INT_TO_BIGDECIMAL;
          break;
        case BIGINT :
          instr.op = AOp.BIGINT_TO_BIGDECIMAL;
          break;
        case FLOAT :
          instr.op = AOp.FLT_TO_BIGDECIMAL;
          break;
        case DOUBLE:
          instr.op = AOp.DBL_TO_BIGDECIMAL;
          break;
        case CHAR:
          instr.op = AOp.CHR_TO_BIGDECIMAL;
          break;
      }
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("to_boolean"))) ||
        (sf.getName().equalsIgnoreCase(new String("to_boolean(int)"))) ||
        (sf.getName().equalsIgnoreCase(new String("to_boolean(bigint)"))) )
    {
     assert numArgs == 1 : numArgs;
     assert (args[0].getType() == Datatype.INT || 
             args[0].getType() == Datatype.BIGINT ) : args[0].getType();
    
     Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
         dynPrevRole);
    
     switch(args[0].getType().getKind())
     {
       case INT :
         instr.op = AOp.INT_TO_BOOLEAN;
         break;
       case BIGINT :
         instr.op = AOp.BIGINT_TO_BOOLEAN;
         break;
     }
     instr.r1 = addr.role;
     instr.c1 = addr.pos;
     instr.r2 = 0;
     instr.c2 = 0;
     instr.dr = outRole;
     instr.dc = outPos;
     eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("to_char"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(int)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(bigint)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(float)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(double)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(number)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(timestamp)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(timestamp,char)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(interval)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(intervalym)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(boolean)"))) ||
             (sf.getName().equalsIgnoreCase(new String("to_char(xmltype)")))
            )
    {
      assert (numArgs >= 1): numArgs;
      Datatype arg0Type = args[0].getType();
      assert (arg0Type == Datatype.INT ||
              arg0Type == Datatype.BIGINT ||
              arg0Type == Datatype.FLOAT ||
              arg0Type == Datatype.DOUBLE ||
              arg0Type == Datatype.BIGDECIMAL ||
              arg0Type == Datatype.TIMESTAMP ||
              arg0Type == Datatype.INTERVAL ||
              arg0Type == Datatype.INTERVALYM ||
              arg0Type == Datatype.BOOLEAN ||
              arg0Type == Datatype.XMLTYPE) : args[0].getType();
      
      assert (numArgs == 1) || (arg0Type == Datatype.TIMESTAMP && numArgs > 0 && numArgs < 3) : numArgs;

      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      Addr addr1 = null;
      
      switch(arg0Type.getKind())
      {
      case INT:
        instr.op = AOp.INT_TO_CHR;
        break;
      case BIGINT:
        instr.op = AOp.BIGINT_TO_CHR;
        break;        
      case FLOAT:
        instr.op = AOp.FLT_TO_CHR;
        break;
      case DOUBLE:
        instr.op = AOp.DBL_TO_CHR;
        break;
      case BIGDECIMAL:
        instr.op = AOp.BIGDECIMAL_TO_CHR;
        break;
      case TIMESTAMP:        
        if(numArgs == 1)
        {
          instr.op = AOp.TIMESTAMP_TO_CHR1;  
        }
        if(numArgs == 2)
        {          
          instr.op = AOp.TIMESTAMP_TO_CHR2;
          addr1 
            = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
                       dynPrevRole);
        }       
        break;
      case INTERVAL:
        instr.op = AOp.INTERVAL_TO_CHR;     
        break;
      case INTERVALYM:
        instr.op = AOp.INTERVALYM_TO_CHR;     
        break;
      case BOOLEAN:
        instr.op = AOp.BOOLEAN_TO_CHR;     
        break;
      case XMLTYPE:
        instr.op = AOp.XMLTYPE_TO_CHR;
        break;
      }

      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      if(addr1 != null)
      {
        instr.r2 = addr1.role;
        instr.c2 = addr1.pos;
      }
      else
      {
        instr.r2 = 0;
        instr.c2 = 0;
      }
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("hextoraw"))) ||
             (sf.getName().equalsIgnoreCase(new String("hextoraw(char)"))))
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.CHAR) : args[0].getType();

      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      instr.op = AOp.HEX_TO_BYT;
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("rawtohex"))) ||
             (sf.getName().equalsIgnoreCase(new String("rawtohex(byte)"))))
    {
      assert numArgs == 1 : numArgs;
      assert (args[0].getType() == Datatype.BYTE) : args[0].getType();

      Addr addr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      instr.op = AOp.BYT_TO_HEX;
      instr.r1 = addr.role;
      instr.c1 = addr.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if(sf.getName().equalsIgnoreCase(new String("to_timestamp(bigint)")))
    {
      // BIGINT_TO_TIMESTAMP should only support one argument
      // Right now, we dont't support second argument(format) in this operation
      assert numArgs == 1 : numArgs;
      
      Datatype argType = args[0].getType();
      //Datatype must be bigint; no other numeric datatype allowed
      assert argType == Datatype.BIGINT : argType;
      
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
            
      instr.op = AOp.BIGINT_TO_TIMESTAMP;
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if ((sf.getName().equalsIgnoreCase(new String("to_timestamp"))) ||
        (sf.getName().equalsIgnoreCase(new String("to_timestamp(char)")))||
        (sf.getName().equalsIgnoreCase(new String("to_timestamp(char,char)"))))
    {
      // Since format is optional, so num of args can be either 1 or 2
      assert numArgs <= 2 : numArgs;
      assert numArgs >= 1 : numArgs;
      assert args[0].getType() == Datatype.CHAR : args[0].getType();

      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      if (numArgs == 2)
      {
        assert args[1].getType() == Datatype.CHAR : args[1].getType();
        Addr addr2 = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
            dynPrevRole);
        instr.r2 = addr2.role;
        instr.c2 = addr2.pos;
      } 
      else
      {
        instr.r2 = 0;
        instr.c2 = 0;
      }

      instr.op = AOp.CHR_TO_TIMESTAMP;
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }    
   //TODO: bigdecimal ?? multiple combinations
    else if ((sf.getName().equalsIgnoreCase(new String("nvl")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(char,char)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(byte,byte)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(int,int)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(float,float)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(double,double)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(number,number)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(boolean,boolean)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(int,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(int,float)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(int,double)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(int,number)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(bigint,int)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(bigint,float)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(bigint,double)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(bigint,number)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(float,int)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(float,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(float,double)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(float,number)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(double,int)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(double,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(double,float)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(double,number)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(number,int)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(number,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(number,float)")))
        || (sf.getName().equalsIgnoreCase(new String("nvl(number,double)")))
        || (sf.getName()
            .equalsIgnoreCase(new String("nvl(timestamp,timestamp)")))
        || (sf.getName()
            .equalsIgnoreCase(new String("nvl(interval,interval)")))
        || (sf.getName()
            .equalsIgnoreCase(new String("nvl(intervalym,intervalym)"))))
    {
      assert numArgs == 2 : numArgs;

      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      Addr addr2 = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);

      instr.op = getNVLOp(args[0].getType(), args[1].getType());
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.r2 = addr2.role;
      instr.c2 = addr2.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
  
    else if ((sf.getName().equalsIgnoreCase(new String("prev(int)")))
	     || (sf.getName().equalsIgnoreCase(new String("prev(float)")))
	     || (sf.getName().equalsIgnoreCase(new String("prev(double)")))
	     || (sf.getName().equalsIgnoreCase(new String("prev(number)")))
	     || (sf.getName().equalsIgnoreCase(new String("prev(bigint)")))
	     || (sf.getName().equalsIgnoreCase(new String("prev(timestamp)")))
	     || (sf.getName().equalsIgnoreCase(new String("prev(interval)")))
	     || (sf.getName().equalsIgnoreCase(new String("prev(intervalym)")))
	     || (sf.getName().equalsIgnoreCase(new String("prev(byte)")))
	     || (sf.getName().equalsIgnoreCase(new String("prev(char)"))))
    {
      assert numArgs == 1: numArgs;

      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      instr.op = getCopyOp(args[0].getType());
      instr.r1 = dynPrevRole;
      instr.c1 = addr1.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    
    else if((sf.getName().equalsIgnoreCase(new String("prev(int,int)")))
       || (sf.getName().equalsIgnoreCase(new String("prev(bigint,int)")))
       || (sf.getName().equalsIgnoreCase(new String("prev(float,int)")))
       || (sf.getName().equalsIgnoreCase(new String("prev(double,int)")))
       || (sf.getName().equalsIgnoreCase(new String("prev(number,int)")))
       || (sf.getName().equalsIgnoreCase(new String("prev(timestamp,int)")))
       || (sf.getName().equalsIgnoreCase(new String("prev(interval,int)")))
       || (sf.getName().equalsIgnoreCase(new String("prev(intervalym,int)")))
       || (sf.getName().equalsIgnoreCase(new String("prev(byte,int)")))
       || (sf.getName().equalsIgnoreCase(new String("prev(char,int)"))))
    {
      assert numArgs == 2: numArgs;
      
      assert args[1] instanceof ExprInt;
      ExprInt prevArg2 = (ExprInt)args[1];
      int prevIndex = prevArg2.getIValue();
      
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole); 
      instr.op = getCopyOp(args[0].getType());
      instr.r1 = dynPrevRole + (prevIndex - 1);
      instr.c1 = addr1.pos;
      instr.r2 = 0;
      instr.c2 = 0;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }    
    //TODO : bigdecimal ?yst
    else if((sf.getName().equalsIgnoreCase(new String("prev(int,int,bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("prev(bigint,int,bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("prev(float,int,bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("prev(double,int,bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("prev(number,int,bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("prev(timestamp,int,bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("prev(interval,int,bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("prev(intervalym,int,bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("prev(byte,int,bigint,bigint)")))
        || (sf.getName().equalsIgnoreCase(new String("prev(char,int,bigint,bigint)"))))
    {
      assert numArgs == 4: numArgs;
      
      assert args[1] instanceof ExprInt;
      assert args[2] instanceof ExprBigint;
      ExprInt prevArg2 = (ExprInt)args[1];
      ExprBigint prevArg3 = (ExprBigint)args[2];
      int prevIndex = prevArg2.getIValue();
      long range    = prevArg3.getLValue();
      CaseInstr caseInstr = new CaseInstr(1);
      CaseCondition caseCond = new CaseCondition();
      
      // create a case instruction
      // when(prevTuple.elementTime + range >= inputTuple.elementTime)
      // then copy prev tuple from prev role.
      // else copy null input tuple from --- role.
      
      // BEval for the condition..
      
      Addr addr2  = instExpr(ec, args[3], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      
      IBEval cond = BEvalFactory.create(ec);
      BInstr bInstr = new BInstr();
      
      IAEval left = AEvalFactory.create(ec);
      AInstr leftInstr = new AInstr();
      
      leftInstr.op = AOp.BIGINT_ADD;
      leftInstr.r1 = dynPrevRole + (prevIndex - 1);      
      leftInstr.c1 = addr2.pos;
      leftInstr.r2 = IEvalContext.CONST_ROLE;
      leftInstr.c2 = evalCtx.ct.addBigint(range, false);
      leftInstr.dr = IEvalContext.SCRATCH_ROLE;
      int pos = evalCtx.st.addAttr(Datatype.BIGINT);
      leftInstr.dc = pos;
      left.addInstr(leftInstr);
      
      left.compile();
      
      bInstr.op = BOp.C_BIGINT_GT;
      bInstr.r1 = IEvalContext.SCRATCH_ROLE;
      bInstr.c1 = new Column(pos);
      bInstr.e1 = left;
      bInstr.r2 = inpRoles[0];
      bInstr.c2 = new Column(addr2.pos);
      bInstr.e2 = null;
      cond.addInstr(bInstr);
      cond.compile();
      caseCond.setCondition(cond);
      
      //AEval for the result
      IAEval resEval  = AEvalFactory.create(ec);
      AInstr instrRes = new AInstr();
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      
      instrRes.op = getCopyOp(args[0].getType());
      instrRes.r1 = dynPrevRole + (prevIndex - 1);
      instrRes.c1 = addr1.pos;
      instrRes.r2 = 0;
      instrRes.c2 = 0;
      instrRes.dr = outRole;
      instrRes.dc = outPos;
      resEval.addInstr(instrRes);
      
      resEval.compile();
      caseCond.setResult(resEval);
      caseInstr.conditions[0] = caseCond;
      
      //AEval for the else
      IAEval elseEval  = AEvalFactory.create(ec);
      AInstr instrElse = new AInstr();
      instrElse.op = getCopyOp(args[0].getType());
      instrElse.r1 = IEvalContext.NULL_INPUT_ROLE;
      instrElse.c1 = addr1.pos;
      instrElse.r2 = 0;
      instrElse.c2 = 0;
      instrElse.dr = outRole;
      instrElse.dc = outPos;
      elseEval.addInstr(instrElse);
      
      elseEval.compile();
      
      caseInstr.elseResult = elseEval;
      
      instr.op       = AOp.CASE_EXPR;
      instr.caseCond = caseInstr;
      instr.dr       = outRole;
      instr.dc       = outPos;
      eval.addInstr(instr);
    }
    else if(sf.getName()
            .equalsIgnoreCase(new String("systimestamp()"))) 
    {
      assert numArgs == 0: numArgs;
      instr.op = AOp.SYSTIMESTAMP;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }
    else if(sf.getName()
            .equalsIgnoreCase(new String("systimestamp(char)")))
    {
      assert numArgs == 1: numArgs;
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
              dynPrevRole);
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.op = AOp.SYSTIMESTAMPZ;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr);
    }

    else if(sf.getName().equalsIgnoreCase(new String("mod(int,int)"))
        || sf.getName().equalsIgnoreCase(new String("mod(float,float)"))
        || sf.getName().equalsIgnoreCase(new String("mod(bigint,bigint)"))
        || sf.getName().equalsIgnoreCase(new String("mod(double,double)"))
        || sf.getName().equalsIgnoreCase(new String("mod(number,number)")))
        
    {
      assert numArgs == 2 : numArgs;
      
      Addr addr1 = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      Addr addr2 = instExpr(ec, args[1], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      switch(args[0].getType().getKind())
      {
      case INT:
        instr.op = AOp.INT_MOD;
        break;
      case FLOAT:
        instr.op = AOp.FLOAT_MOD;
        break;
      case BIGINT:
        instr.op = AOp.BIGINT_MOD;
        break;
      case DOUBLE:
        instr.op = AOp.DOUBLE_MOD;
        break;
      case BIGDECIMAL:
        instr.op = AOp.BIGDECIMAL_MOD;
        break;
      default:
        assert false;
      }
      instr.r1 = addr1.role;
      instr.c1 = addr1.pos;
      instr.r2 = addr2.role;
      instr.c2 = addr2.pos;
      instr.dr = outRole;
      instr.dc = outPos;
      eval.addInstr(instr); 
      
    }
    
    else if(sf.getName().equalsIgnoreCase(new String("xmlcomment(char)")))
    {
      assert numArgs == 1;
      Addr argAddr;
      argAddr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      instr.op = AOp.XML_COMMENT;
      
      instr.numArgs = 1;
      instr.argRoles = new int[1];
      instr.argPos = new int[1];
      instr.argTypes = new Datatype[1];
      instr.xmlInstr = new XMLElementInstr(evalCtx.getDummyDoc());
      
      instr.addFunctionArg(0, args[0].getType(), argAddr.role, argAddr.pos);

      instr.dr = outRole;
      instr.dc = outPos;

      eval.addInstr(instr);
    }
    else if(sf.getName().equalsIgnoreCase(new String("xmlcdata(char)")))
    {
      assert numArgs == 1;
      Addr argAddr;
      argAddr = instExpr(ec, args[0], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      instr.op = AOp.XML_CDATA;
      
      instr.numArgs = 1;
      instr.argRoles = new int[1];
      instr.argPos = new int[1];
      instr.argTypes = new Datatype[1];
      instr.xmlInstr = new XMLElementInstr(evalCtx.getDummyDoc());
      
      instr.addFunctionArg(0, args[0].getType(), argAddr.role, argAddr.pos);

      instr.dr = outRole;
      instr.dc = outPos;

      eval.addInstr(instr);
    }
    // rest is unsupported
    else
      assert false : sf.getName();
  }
  
  
  private static void processOrderByExpr(ExecContext ec, Expr expr, IAEval eval,
      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles,
      int dynStartRole, int dynPrevRole) throws CEPException
  {
    ExprOrderBy o = (ExprOrderBy) expr;
    Expr orderByExpr = o.getOrderbyExpr();
    AInstr instr = new AInstr();
    Addr orderByExprAddr; 
    
    orderByExprAddr = instExpr(ec, orderByExpr, eval, evalCtx, inpRoles);
    instr.op = getCopyOp(orderByExpr.getType());
    
    instr.r1 = orderByExprAddr.role;
    instr.c1  = orderByExprAddr.pos;
    
    instr.dr = outRole;
    instr.dc = outPos;
    
    eval.addInstr(instr);
 }
  
  private static void processXmlConcatExpr(ExecContext ec, Expr expr, IAEval eval,
      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles,
      int dynStartRole, int dynPrevRole) throws CEPException
  {
    ExprXmlConcat xmlExpr = (ExprXmlConcat) expr;
    Expr[] concatExprs = xmlExpr.getConcatExprs();
    int numArgs = concatExprs.length;

    AInstr instr = new AInstr();
    Addr argAddr;
    instr.xmlConcatInstr = new XMLConcatInstr(evalCtx.getDummyDoc(),numArgs);
    instr.op = AOp.XML_CONCAT;
    
    // iterates over args and adds them to instr.
    for (int i = 0; i < numArgs; i++)
    {
      argAddr = instExpr(ec, concatExprs[i], eval, evalCtx, inpRoles, dynStartRole,
          dynPrevRole);
      instr.xmlConcatInstr.addArgument(i, argAddr.role,argAddr.pos);
    }

    instr.dr = outRole;
    instr.dc = outPos;

    eval.addInstr(instr);
  }
  
  /**
   * Creates an Ainstr for XML_PARSE
   * 
   * @author mthatte
   */
  private static void processXmlParseExpr(ExecContext ec, Expr expr, IAEval eval,
      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles,
      int dynStartRole, int dynPrevRole) throws CEPException
  {
    ExprXmlParse xExpr = (ExprXmlParse) expr;
    Expr argExpr = xExpr.getValue();
    
    //Where is the argument coming from?
    Addr argAddr;
    argAddr = instExpr(ec, argExpr, eval, evalCtx, inpRoles, dynStartRole,
        dynPrevRole);
    
    //Create an instr and set the relevant information.
    AInstr instr = new AInstr();
    instr.setXParseFunctionInstr(evalCtx.getDummyDoc(), xExpr.isWellformed(),
        xExpr.getParseKind(), argAddr.role, argAddr.pos);
    instr.dr = outRole;
    instr.dc = outPos;

    eval.addInstr(instr);
  }

  private static void processXQryFuncExpr(ExecContext ec, Expr expr, IAEval eval,
      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles,
      int dynStartRole, int dynPrevRole) throws CEPException
  {
    ExprXQryFunc userExpr = (ExprXQryFunc) expr;
    int fnId = userExpr.getFuncId();
    
    SimpleFunction sf = ec.getUserFnMgr().getSimpleFunction(fnId);
    AInstr instr = new AInstr();
    if (userExpr.getXmlQuery() == ExprXQryFuncKind.PO_EXPR_XQRY)
      instr.op = AOp.XQRY_FNC;
    else if (userExpr.getXmlQuery() == ExprXQryFuncKind.PO_EXPR_XEXTS)
      instr.op = AOp.XEXISTS_FNC;
    else
    {
      assert userExpr.getXmlQuery() == ExprXQryFuncKind.PO_EXPR_XMLTBL;
      instr.op = AOp.XMLTBL_FNC;
    }

    Expr[] params         = userExpr.getParams();
    int    numParams      = userExpr.getNumParams();
    String[] names        = userExpr.getNames();
    
    Addr argAddr;
    SingleElementFunction f = sf.getImplClass();
    
    instr.setXQRYFunctionInstr(f, 2*numParams+1, userExpr.getType());
    //set the function id in the instruction
    instr.setFunctionId(sf.getId());
    for (int i = 0; i < numParams; i++)
    {
      argAddr = instExpr(ec, params[i], eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
      instr.addFunctionArg(i, params[i].getType(), argAddr.role, argAddr.pos);
    }

    for (int i = 0; i < numParams; i++)
    {
      argAddr = instExpr(ec, new ExprChar(names[i].toCharArray()), eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
      instr.addFunctionArg(numParams + i, Datatype.CHAR, argAddr.role, argAddr.pos);
    }
    XmlManager xmlMgr = ec.getXmlMgr();
    PreparedXQuery ixq = xmlMgr.createPreparedXQuery(userExpr.getXQryStr());
    
    argAddr = instExpr(ec, new ExprObject(ixq), eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
    instr.addFunctionArg(2*numParams, Datatype.OBJECT, argAddr.role, argAddr.pos);
    
    instr.dr = outRole;
    instr.dc = outPos;
    
    eval.addInstr(instr);
  }
  
  
  private static void processXMLColAttValExpr(ExecContext ec, Expr expr, IAEval eval,
                      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles,
                      int dynStartRole, int dynPrevRole) throws CEPException
  {  
    ExprXmlColAttVal colAttExpr = (ExprXmlColAttVal)expr;
    AInstr instr = new AInstr();
    // Reusing instr since once colattval will result into multiple elements
    // each element within one element instr is like an attribute which
    // can have its own evalname
    // The only diff is that element will not be generated at runtime if
    // the value of the expression is null
       
    Expr[] colExprs = colAttExpr.getColAttExprs();
    int numExprs = colAttExpr.getNumColAttExprs();
    // name in this case is null

    XMLElementInstr eInstr = processXMLAttrs(ec, colAttExpr, colExprs, numExprs, eval, 
                                              evalCtx, inpRoles, 
                                              dynStartRole, dynPrevRole);
   
    instr.op = AOp.XML_COLATTVAL;
    instr.xmlInstr = eInstr;
    instr.dr = outRole;
    instr.dc = outPos;

    eval.addInstr(instr);
  }
  
  private static void processXMLForestExpr(ExecContext ec, Expr expr, IAEval eval,
                      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles,
                      int dynStartRole, int dynPrevRole) throws CEPException
  {  
    ExprXmlForest forestExpr = (ExprXmlForest)expr;
    AInstr instr = new AInstr();
    // Reusing instr since once forest will result into multiple elements
    // each element within one element instr is like an attribute which
    // can have its own evalname
    // The only diff is that element will not be generated at runtime if
    // the value of the expression is null
    
    Expr[] forestExprs = forestExpr.getForestExprs();
    int numExprs = forestExpr.getNumForestExprs();
    // name in this case is null
   
    XMLElementInstr eInstr = processXMLAttrs(ec, forestExpr, forestExprs, numExprs, eval, 
                                             evalCtx, inpRoles, 
                                             dynStartRole, dynPrevRole);
        
    instr.op = AOp.XML_FOREST;
    instr.xmlInstr = eInstr;
    instr.dr = outRole;
    instr.dc = outPos;

    eval.addInstr(instr);
  }

  private static XMLElementInstr processXMLAttrs(ExecContext ec, Expr expr, Expr[] childExprs, int numExprs,
                                 IAEval eval, EvalContextInfo evalCtx, int[] inpRoles,
                                 int dynStartRole, int dynPrevRole) throws CEPException
  {
    XMLElementInstr  eInstr = new XMLElementInstr(null, expr.getType(), numExprs, 0);
    for(int i=0; i<numExprs; i++)
    {
      ExprXmlAttr attrExpr = (ExprXmlAttr)childExprs[i];
      XmlAttrName attrname;
      if(attrExpr.getAttrName() != null)
        attrname = new XmlAttrName(attrExpr.getAttrName());
      else
      {
        Addr nameAddr = instExpr(ec, attrExpr.getAttrNameExpr(), eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
        attrname = new XmlAttrName(nameAddr.role, nameAddr.pos);
      }
      eInstr.attrNames[i] = attrname;
      eInstr.attrTypes[i] = attrExpr.getAttrExpr().getType();
      Addr argAddr = instExpr(ec, attrExpr.getAttrExpr(), eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
      eInstr.attrPos[i] = argAddr.pos;
      eInstr.attrRoles[i] = argAddr.role;
    }
    
    eInstr.setDocument(evalCtx.getDummyDoc());
    return eInstr;  
  }

  private static void processXMLElementExpr(ExecContext ec, Expr expr, IAEval eval,
	      EvalContextInfo evalCtx, int outRole, int outPos, int[] inpRoles,
	      int dynStartRole, int dynPrevRole) throws CEPException
  {  
    ExprElement elementExpr = (ExprElement)expr;
    
    AInstr instr = new AInstr();
    
    Expr[]  attrs = elementExpr.getAttrExprs();
    int numAttrs = elementExpr.getNumAttrs();
    
    Expr[] kids = elementExpr.getChildExprs();
    int numKids = elementExpr.getNumChildren();
    
    Addr argAddr;
    
    XMLElementInstr eInstr;
    
    if(elementExpr.getElementName() != null)
    {
      eInstr = new XMLElementInstr(elementExpr.getElementName(), elementExpr.getType(),
                                   numAttrs, numKids);
    }
    else
    {
      argAddr = instExpr(ec, elementExpr.getElementNameExpr(), eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
      eInstr = new XMLElementInstr(argAddr.role, argAddr.pos, elementExpr.getType(), numAttrs, numKids);
    }
    
    for(int i=0; i<numAttrs; i++)
    {
      ExprXmlAttr attrExpr = (ExprXmlAttr)attrs[i];
      XmlAttrName attrname;
      if(attrExpr.getAttrName() != null)
        attrname = new XmlAttrName(attrExpr.getAttrName());
      else
      {
        Addr nameAddr = instExpr(ec, attrExpr.getAttrNameExpr(), eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
        attrname = new XmlAttrName(nameAddr.role, nameAddr.pos);
      }
      eInstr.attrNames[i] = attrname;
      eInstr.attrTypes[i] = attrExpr.getAttrExpr().getType();
      argAddr = instExpr(ec, attrExpr.getAttrExpr(), eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
      eInstr.attrPos[i] = argAddr.pos;
      eInstr.attrRoles[i] = argAddr.role;
    }
    
    for(int j=0; j<numKids; j++)
    {
      eInstr.childTypes[j] = kids[j].getType();
      argAddr = instExpr(ec, kids[j], eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
      eInstr.childPos[j] = argAddr.pos;
      eInstr.childRoles[j] = argAddr.role;
    }
    
    eInstr.setDocument(evalCtx.getDummyDoc());
    
    instr.op = AOp.XML_ELEMENT;
    instr.xmlInstr = eInstr;
    instr.dr = outRole;
    instr.dc = outPos;

    eval.addInstr(instr);
  }
  
  private static void processUserDefFuncExpr(ExecContext ec, Expr expr, 
                                             IAEval eval,
                                             EvalContextInfo evalCtx,
                                             int outRole, int outPos,
                                             int[] inpRoles,
                                             int dynStartRole, int dynPrevRole)
    throws CEPException
  {
    ExprUserDefFunc userExpr = (ExprUserDefFunc) expr;
    int fnId = userExpr.getFuncId();
    ISimpleFunctionMetadata sf; 
    UserDefinedFunction f = userExpr.getFuncImpl();

    // Simple functions provided by cartridges should be cached in expression.
    // Builtin functions are handled differently.
    // They must be retrieved from the local function manager.
    if (f == null) 
    {
      sf = ec.getUserFnMgr().getSimpleFunction(fnId);
      f = sf.getImplClass();
      
      // Functions with native implementation are handled separately.
      if (sf instanceof SimpleFunction) 
        if (((SimpleFunction) sf).isImplNative())
        {
          processNativeImplFuncs(ec, expr, eval, evalCtx, outRole, outPos,
                                 inpRoles, ((SimpleFunction) sf),
                                 dynStartRole, dynPrevRole);
          return;
        } else
          assert false:
          "all non native implementation simple functions should have " +
            "associated impl";
    }

    Expr[] args = userExpr.getArgs();
    int numArgs = userExpr.getNumArgs();

    AInstr instr = new AInstr();
    Addr argAddr;

    instr.setFunctionInstr(f, numArgs, userExpr.getType());
    //set the function id in the instruction
    instr.setFunctionId(fnId);
    for (int i = 0; i < numArgs; i++)
    {
      argAddr = instExpr(ec, args[i], eval, evalCtx, inpRoles, dynStartRole, dynPrevRole);
      instr.addFunctionArg(i, args[i].getType(), argAddr.role, argAddr.pos);
    }
    instr.dr = outRole;
    instr.dc = outPos;

    eval.addInstr(instr);
  }

  
  private static AOp get2AOp(Datatype leftType, Datatype rightType, ArithOp oper)
  {
       switch(oper)
        {
          case ADD:
             if((leftType == Datatype.INTERVAL) && (rightType == Datatype.TIMESTAMP))
                  return AOp.INTERVAL_TIM_ADD;
             else if((leftType == Datatype.TIMESTAMP) && (rightType == Datatype.INTERVAL))
                  return AOp.TIM_INTERVAL_ADD;
             else if((leftType == Datatype.INTERVALYM) && (rightType == Datatype.TIMESTAMP))
               return AOp.INTERVALYM_TIM_ADD;
             else if((leftType == Datatype.TIMESTAMP) && (rightType == Datatype.INTERVALYM))
               return AOp.TIM_INTERVALYM_ADD;
             else
                  assert false ;
             break;
         case SUB:
            if((leftType == Datatype.TIMESTAMP) && (rightType == Datatype.INTERVAL))
              return AOp.TIM_INTERVAL_SUB;
            else if((leftType == Datatype.TIMESTAMP) && (rightType == Datatype.INTERVALYM))
              return AOp.TIM_INTERVALYM_SUB;
            else
                 assert false ;
            break;
        default:
            assert false : oper;
      }
      return null;
  }
  
  private static AOp getAOp(Datatype dt, Datatype left, Datatype right, ArithOp oper)
  {
    //  In case of unary operators right will be null
    if (!((right == null) || (left == right) ))
     return get2AOp(left,right,oper);
  
    switch (dt.getKind())
    {
      case INT:
        switch (oper)
        {
          case ADD:
            return AOp.INT_ADD;
          case SUB:
            return AOp.INT_SUB;
          case MUL:
            return AOp.INT_MUL;
          case DIV:
            return AOp.INT_DIV;
          default:
            assert false : oper;
        }
        break;
      case BIGINT:
        switch (oper)
        {
          case ADD:
            return AOp.BIGINT_ADD;
          case SUB:
            return AOp.BIGINT_SUB;
          case MUL:
            return AOp.BIGINT_MUL;
          case DIV:
            return AOp.BIGINT_DIV;
          case ITOL:
            return AOp.INT_TO_BIGINT;
          default:
            assert false : oper;
        }
        break;
      case FLOAT:
        switch (oper)
        {
          case ADD:
            return AOp.FLT_ADD;
          case SUB:
            return AOp.FLT_SUB;
          case MUL:
            return AOp.FLT_MUL;
          case DIV:
            return AOp.FLT_DIV;
          case ITOF:
            return AOp.INT_TO_FLT;
          case LTOF:
            return AOp.BIGINT_TO_FLT;
          default:
            assert false : oper;
        }
        break;
      case DOUBLE:
        switch (oper)
        {
          case ADD:
            return AOp.DBL_ADD;
          case SUB:
            return AOp.DBL_SUB;
          case MUL:
            return AOp.DBL_MUL;
          case DIV:
            return AOp.DBL_DIV;
          case ITOD:
            return AOp.INT_TO_DBL;
          case LTOD:
            return AOp.BIGINT_TO_DBL;
          case FTOD:
            return AOp.FLT_TO_DBL;
          default:
            assert false : oper;
        }
        break;
      case BIGDECIMAL:
        switch (oper)
        {
          case ADD:
            return AOp.BIGDECIMAL_ADD;
          case SUB:
            return AOp.BIGDECIMAL_SUB;
          case MUL:
            return AOp.BIGDECIMAL_MUL;
          case DIV:
            return AOp.BIGDECIMAL_DIV;
          /*conversions to BigDecimal*/
          case ITON:
            return AOp.INT_TO_BIGDECIMAL;
          case LTON:
            return AOp.BIGINT_TO_BIGDECIMAL;
          case FTON:
            return AOp.FLT_TO_BIGDECIMAL;
          case DTON:
            return AOp.DBL_TO_BIGDECIMAL;
          default:
            assert false : oper;
        }
        break;
      case BOOLEAN:
        switch (oper)
        {
          case ITOB:
            return AOp.INT_TO_BOOLEAN;
          case LTOB:
            return AOp.BIGINT_TO_BOOLEAN;
          default:
            assert false : oper;
        }
        break;
      case INTERVAL:
        switch (oper)
        {
          case ADD:
            return AOp.INTERVAL_ADD;
          case SUB:
            if(left == Datatype.TIMESTAMP)
              return AOp.INTERVAL_TIM_SUB;
            else
              return AOp.INTERVAL_SUB;
          case MUL:
            return AOp.INTERVAL_MUL;
          case DIV:
            return AOp.INTERVAL_DIV;
          default:
            assert false : oper;
        }        
        break;
      case INTERVALYM:
        switch (oper)
        {
          case ADD:
            return AOp.INTERVALYM_ADD;
          case SUB:
            if(left == Datatype.TIMESTAMP)
              return AOp.INTERVALYM_TIM_SUB;
            else
              return AOp.INTERVALYM_SUB;
          case MUL:
            return AOp.INTERVALYM_MUL;
          case DIV:
            return AOp.INTERVALYM_DIV;
          default:
            assert false : oper;
        }
      case CHAR:
        switch (oper)
        {
          case CONCAT:
            return AOp.CHR_CONCAT;
          default:
            assert false : oper;
        }
        break;
      case BYTE:
        switch (oper)
        {
          case CONCAT:
            return AOp.BYT_CONCAT;
          default:
            assert false : oper;
        }
        break;
      case TIMESTAMP:
        switch(oper)
        {
         case CTOT:
           return AOp.CHR_TO_TIMESTAMP;
         default :
           assert false : oper;
        }
       break;
      default:
        assert false : dt;
    }

    // should not come here
    return null;
  }

  private static BOp getUnaryOp(Datatype dt, UnaryOp oper, IAEval unary)
  {
    boolean isComplex = true;
    if (unary.isNoOP())
      isComplex = false;
    
    switch(dt.getKind())
    {
      case INT: 
        switch(oper)
        {
          case IS_NULL : 
            return isComplex ? BOp.C_INT_IS_NULL : BOp.INT_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_INT_IS_NOT_NULL : BOp.INT_IS_NOT_NULL;
          default : 
            assert false : oper;
        }
        break;
      case BIGINT: 
        switch(oper)
        {
          case IS_NULL : 
            return isComplex ? BOp.C_BIGINT_IS_NULL : BOp.BIGINT_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_BIGINT_IS_NOT_NULL : BOp.BIGINT_IS_NOT_NULL;
          default : 
            assert false : oper;
        }
        break;
      case FLOAT:
        switch(oper)
        {
           case IS_NULL:
             return isComplex ? BOp.C_FLT_IS_NULL : BOp.FLT_IS_NULL;
           case IS_NOT_NULL : 
             return isComplex ? BOp.C_FLT_IS_NOT_NULL : BOp.FLT_IS_NOT_NULL;
           default :
             assert false : oper; 
        }
        break;
      case DOUBLE:
        switch(oper)
        {
          case IS_NULL :
            return isComplex ? BOp.C_DBL_IS_NULL : BOp.DBL_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_DBL_IS_NOT_NULL : BOp.DBL_IS_NOT_NULL;
          default:
            assert false: oper;
        }
        break;
      case BIGDECIMAL:
        switch(oper)
        {
          case IS_NULL :
            return isComplex ? BOp.C_BIGDECIMAL_IS_NULL : BOp.BIGDECIMAL_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_BIGDECIMAL_IS_NOT_NULL : BOp.BIGDECIMAL_IS_NOT_NULL;
          default:
            assert false: oper;
        }
        break;
      case CHAR:
        switch(oper)
        {
          case IS_NULL:
            return isComplex ? BOp.C_CHR_IS_NULL : BOp.CHR_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_CHR_IS_NOT_NULL : BOp.CHR_IS_NOT_NULL;
          default :
            assert false : oper;
        } 
      case TIMESTAMP:
        switch(oper)
        {
          case IS_NULL:
            return isComplex ? BOp.C_TIM_IS_NULL : BOp.TIM_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_TIM_IS_NOT_NULL : BOp.TIM_IS_NOT_NULL;
          default:
            assert false : oper;
        }
        break;
      case BYTE:
        switch(oper)
        {
          case IS_NULL:
            return isComplex ? BOp.C_BYT_IS_NULL : BOp.BYT_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_BYT_IS_NOT_NULL : BOp.BYT_IS_NOT_NULL;
          default:
            assert false :oper;
        }
        break;
      case INTERVAL: 
        switch(oper)
        {
          case IS_NULL : 
            return isComplex ? BOp.C_INTERVAL_IS_NULL : BOp.INTERVAL_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_INTERVAL_IS_NOT_NULL : BOp.INTERVAL_IS_NOT_NULL;
          default : 
            assert false : oper;
        }
        break;
      case INTERVALYM: 
        switch(oper)
        {
          case IS_NULL : 
            return isComplex ? BOp.C_INTERVALYM_IS_NULL : BOp.INTERVALYM_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_INTERVALYM_IS_NOT_NULL : BOp.INTERVALYM_IS_NOT_NULL;
          default : 
            assert false : oper;
        }
        break;
      case BOOLEAN: 
        switch(oper)
        {
          case IS_NULL : 
            return isComplex ? BOp.C_BOOL_IS_NULL : BOp.BOOL_IS_NULL;
          case IS_NOT_NULL : 
            return isComplex ? BOp.C_BOOL_IS_NOT_NULL : BOp.BOOL_IS_NOT_NULL;
          default : 
            assert false : oper;
        }
        break;
      case XMLTYPE:
        switch(oper)
        {
          case IS_NULL :
            return isComplex ? BOp.C_XMLTYPE_IS_NULL : BOp.XMLTYPE_IS_NULL;
          case IS_NOT_NULL :
            return isComplex ? BOp.C_XMLTYPE_IS_NOT_NULL : BOp.XMLTYPE_IS_NOT_NULL;
          default :
            assert false : oper;
        }
        break;
      case OBJECT:
        switch(oper)
        {
          case IS_NULL :
            return isComplex ? BOp.C_OBJ_IS_NULL : BOp.OBJ_IS_NULL;
          case IS_NOT_NULL :
            return isComplex ? BOp.C_OBJ_IS_NOT_NULL : BOp.OBJ_IS_NOT_NULL;
          default :
            assert false : oper;
        }
        break;
      default :
         assert false : dt; 
    }
    return null;
    
  }
  
  private static BOp getLogicalBOp(LogicalOp oper)
  {
    switch(oper)
    {
     case AND:
       return BOp.BOOL_AND;
     case OR:
       return BOp.BOOL_OR;
     case NOT:
       return BOp.BOOL_NOT;
     case XOR:
       return BOp.BOOL_XOR;
     default :
       assert false : oper;
    }
    return null;
  }
  
  private static BOp getBOp(Datatype dt, CompOp oper, IAEval left, IAEval right)
  {

    boolean isComplex = true;
    if (left.isNoOP() && right.isNoOP())
      isComplex = false;

    switch (dt.getKind())
    {
      case INT:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_INT_LT : BOp.INT_LT;
          case LE:
            return isComplex ? BOp.C_INT_LE : BOp.INT_LE;
          case GT:
            return isComplex ? BOp.C_INT_GT : BOp.INT_GT;
          case GE:
            return isComplex ? BOp.C_INT_GE : BOp.INT_GE;
          case EQ:
            return isComplex ? BOp.C_INT_EQ : BOp.INT_EQ;
          case NE:
            return isComplex ? BOp.C_INT_NE : BOp.INT_NE;
          default:
            assert false : oper;
        }
        break;
      case BIGINT:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_BIGINT_LT : BOp.BIGINT_LT;
          case LE:
            return isComplex ? BOp.C_BIGINT_LE : BOp.BIGINT_LE;
          case GT:
            return isComplex ? BOp.C_BIGINT_GT : BOp.BIGINT_GT;
          case GE:
            return isComplex ? BOp.C_BIGINT_GE : BOp.BIGINT_GE;
          case EQ:
            return isComplex ? BOp.C_BIGINT_EQ : BOp.BIGINT_EQ;
          case NE:
            return isComplex ? BOp.C_BIGINT_NE : BOp.BIGINT_NE;
          default:
            assert false : oper;
        }
        break;
      case FLOAT:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_FLT_LT : BOp.FLT_LT;
          case LE:
            return isComplex ? BOp.C_FLT_LE : BOp.FLT_LE;
          case GT:
            return isComplex ? BOp.C_FLT_GT : BOp.FLT_GT;
          case GE:
            return isComplex ? BOp.C_FLT_GE : BOp.FLT_GE;
          case EQ:
            return isComplex ? BOp.C_FLT_EQ : BOp.FLT_EQ;
          case NE:
            return isComplex ? BOp.C_FLT_NE : BOp.FLT_NE;
          default:
            assert false : oper;
        }
        break;
      case DOUBLE:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_DBL_LT : BOp.DBL_LT;
          case LE:
            return isComplex ? BOp.C_DBL_LE : BOp.DBL_LE;
          case GT:
            return isComplex ? BOp.C_DBL_GT : BOp.DBL_GT;
          case GE:
            return isComplex ? BOp.C_DBL_GE : BOp.DBL_GE;
          case EQ:
            return isComplex ? BOp.C_DBL_EQ : BOp.DBL_EQ;
          case NE:
            return isComplex ? BOp.C_DBL_NE : BOp.DBL_NE;
          default:
            assert false : oper;
        }
        break;
      case BIGDECIMAL:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_BIGDECIMAL_LT : BOp.BIGDECIMAL_LT;
          case LE:
            return isComplex ? BOp.C_BIGDECIMAL_LE : BOp.C_BIGDECIMAL_LE;
          case GT:
            return isComplex ? BOp.C_BIGDECIMAL_GT : BOp.C_BIGDECIMAL_GT;
          case GE:
            return isComplex ? BOp.C_BIGDECIMAL_GE : BOp.C_BIGDECIMAL_GE;
          case EQ:
            return isComplex ? BOp.C_BIGDECIMAL_EQ : BOp.C_BIGDECIMAL_EQ;
          case NE:
            return isComplex ? BOp.C_BIGDECIMAL_NE : BOp.C_BIGDECIMAL_NE;
          default:
            assert false : oper;
        }
        break;
      case CHAR:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_CHR_LT : BOp.CHR_LT;
          case LE:
            return isComplex ? BOp.C_CHR_LE : BOp.CHR_LE;
          case GT:
            return isComplex ? BOp.C_CHR_GT : BOp.CHR_GT;
          case GE:
            return isComplex ? BOp.C_CHR_GE : BOp.CHR_GE;
          case EQ:
            return isComplex ? BOp.C_CHR_EQ : BOp.CHR_EQ;
          case NE:
            return isComplex ? BOp.C_CHR_NE : BOp.CHR_NE;
          case LIKE:
            return isComplex ? BOp.C_CHR_LIKE : BOp.CHR_LIKE;
          default:
            assert false : oper;
        }
        break;
      case BYTE:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_BYT_LT : BOp.BYT_LT;
          case LE:
            return isComplex ? BOp.C_BYT_LE : BOp.BYT_LE;
          case GT:
            return isComplex ? BOp.C_BYT_GT : BOp.BYT_GT;
          case GE:
            return isComplex ? BOp.C_BYT_GE : BOp.BYT_GE;
          case EQ:
            return isComplex ? BOp.C_BYT_EQ : BOp.BYT_EQ;
          case NE:
            return isComplex ? BOp.C_BYT_NE : BOp.BYT_NE;
          default:
            assert false : oper;
        }
        break;
      case TIMESTAMP:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_TIM_LT : BOp.TIM_LT;
          case LE:
            return isComplex ? BOp.C_TIM_LE : BOp.TIM_LE;
          case GT:
            return isComplex ? BOp.C_TIM_GT : BOp.TIM_GT;
          case GE:
            return isComplex ? BOp.C_TIM_GE : BOp.TIM_GE;
          case EQ:
            return isComplex ? BOp.C_TIM_EQ : BOp.TIM_EQ;
          case NE:
            return isComplex ? BOp.C_TIM_NE : BOp.TIM_NE;
        }
        break;
      case INTERVAL:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_INTERVAL_LT : BOp.INTERVAL_LT;
          case LE:
            return isComplex ? BOp.C_INTERVAL_LE : BOp.INTERVAL_LE;
          case GT:
            return isComplex ? BOp.C_INTERVAL_GT : BOp.INTERVAL_GT;
          case GE:
            return isComplex ? BOp.C_INTERVAL_GE : BOp.INTERVAL_GE;
          case EQ:
            return isComplex ? BOp.C_INTERVAL_EQ : BOp.INTERVAL_EQ;
          case NE:
            return isComplex ? BOp.C_INTERVAL_NE : BOp.INTERVAL_NE;
          default:
            assert false : oper;
        }
        break;
      case INTERVALYM:
        switch (oper)
        {
          case LT:
            return isComplex ? BOp.C_INTERVALYM_LT : BOp.INTERVALYM_LT;
          case LE:
            return isComplex ? BOp.C_INTERVALYM_LE : BOp.INTERVALYM_LE;
          case GT:
            return isComplex ? BOp.C_INTERVALYM_GT : BOp.INTERVALYM_GT;
          case GE:
            return isComplex ? BOp.C_INTERVALYM_GE : BOp.INTERVALYM_GE;
          case EQ:
            return isComplex ? BOp.C_INTERVALYM_EQ : BOp.INTERVALYM_EQ;
          case NE:
            return isComplex ? BOp.C_INTERVALYM_NE : BOp.INTERVALYM_NE;
          default:
            assert false : oper;
        }
        break;
      case BOOLEAN:
        switch (oper)
        {
          case EQ:
            return isComplex ? BOp.C_BOOLEAN_EQ : BOp.BOOLEAN_EQ;  
          case NE:
            return isComplex ? BOp.C_BOOLEAN_NE : BOp.BOOLEAN_NE;  
          default:
            assert false : oper;
        }
        break;
      case OBJECT:
        switch (oper)
        {
          case EQ:
            return isComplex ? BOp.C_OBJ_EQ : BOp.OBJ_EQ;  
          case NE:
            return isComplex ? BOp.C_OBJ_NE : BOp.OBJ_NE;  
          default:
            assert false : oper;
        }
        break;
      case XMLTYPE:
        switch(oper)
        {
          case EQ:
            return isComplex ? BOp.C_XMLTYPE_EQ : BOp.XMLTYPE_EQ;          
          default:
            assert false : oper;  
        }
        break;
        
      default:
        assert false : dt;
    }

    // Should not come here
    return null;
  }

}
