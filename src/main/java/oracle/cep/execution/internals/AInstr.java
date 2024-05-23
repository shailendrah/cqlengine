/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/AInstr.java /main/18 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares AInstr in package oracle.cep.execution.internals.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    sbishnoi  01/13/12 - removing unused import
    alealves  11/27/09 - Data cartridge context, default package support
    skmishra  08/17/08 - adding xmlAggIndexPos[]
    skmishra  06/17/08 - adding xmlaggInstr
    skmishra  06/11/08 - adding xmlparseinstr, xmlconcatinstr
    skmishra  06/06/08 - cleanup
    skmishra  05/20/08 - adding handlers for xparse, xcomment
    parujain  05/02/08 - XMLElement support
    najain    11/01/07 - add xmltype
    parujain  11/15/07 - external source
    udeshmuk  10/17/07 - remove aggrInput and related functions.
    hopark    09/07/07 - refactor eval
    hopark    09/06/07 - optimize
    rkomurav  07/12/07 - restructure uda
    sbishnoi  06/12/07 - support for Multi-arg UDAs
    parujain  05/02/07 - Function Statistics
    parujain  03/30/07 - Support CASE
    najain    03/12/07 - bug fix
    rkomurav  01/04/07 - null support UDA
    rkomurav  12/13/06 - support expected count(expr) implementation
    anasrini  07/17/06 - support for user defined aggregations 
    anasrini  06/20/06 - support for functions 
    najain    04/27/06 - add user-defined functions
    anasrini  03/27/06 - add toString 
    anasrini  03/14/06 - expose fields 
    skaluska  02/12/06 - Creation
    skaluska  02/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/AInstr.java /main/17 2009/12/02 02:35:18 alealves Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals;

import oracle.cep.common.Datatype;
import oracle.cep.common.XMLParseKind;
import oracle.cep.execution.comparator.TupleComparator;
import oracle.cep.execution.internals.memory.EvalContext;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.UserDefinedFunction;
import oracle.xml.parser.v2.XMLDocument;


/**
 * @author skaluska
 *
 */
public class AInstr {
  /* operation */
  public AOp op;
  
  /* operand 1 */
  public int r1;
  public int c1;

  /* operand 2 */
  public int r2;
  public int c2;
  
  /* result */
  public int dr;
  public int dc;

  /* support for functions */
  public UserDefinedFunction   f;
  public Datatype              returnType;
  public int                   numArgs;
  public Datatype[]            argTypes;
  public int[]                 argRoles;
  public int[]                 argPos;
  public Object[]              args;
  
  /* for function statistics we need To keep Function Id */
  public int                   fnId;
  
  /* for implementing count(expr) */
  public IBEval countCond;
  
  /* for supporting CASE conditions */
  public CaseInstr caseCond;
  
  /* for supporting External Database access */
  public ExternalInstr extrInstr;
  
  /**TODO: Cleanup required. Extract common fields from xmlinstrs and create a 
   * superclass
   */
  /* for XMLELEMENT() */
  public XMLElementInstr xmlInstr;
  
  /*for XMLAGG() */
  public XMLAggInstr xmlAggInstr;
  
  /* used by alloc/reset/release index handler */
  private int[] xmlAggIndexPos;
  private TupleComparator[] comparators;
  
  /* for XMLCONCAT() */
  public XMLConcatInstr xmlConcatInstr;
  
  /* for XMLPARSE() */
  public XMLParseInstr xmlParseInstr;
  
  /** Aggregate Parameters */
  private AggrValue[] aggrInputs;
  
  /** Aggregate Result */
  private AggrValue aggrResult;

  private int[] udaPos;

  private IAggrFnFactory[] udaFactory;
  
  private IAggrFunction[] udaHandler;
  
  public void setAggrInput(AggrValue aggrInput, int aggrIndex)
  {
    this.aggrInputs[aggrIndex]  = aggrInput;
  }
  
  public void setAggrOutput(AggrValue aggrResult)
  {
    this.aggrResult = aggrResult;
  }
  
  public AggrValue[] getAggrInputs()
  {
    return aggrInputs;
  }
  
  public AggrValue getAggrResult()
  {
    return aggrResult;
  }
  
  public IAggrFnFactory[] getUdaFactory()
  {
    return udaFactory;
  }

  public int[] getUdaPos()
  {
    return udaPos;
  }

  public IAggrFunction[] getUdaHandler()
  {
    return udaHandler;
  }

  public void setFunctionId(int id)
  {
    this.fnId = id;
  }
  
  public int getFunctionId()
  {
    return fnId;
  }
  
  
  
  /**
   * @return the comparators
   */
  public TupleComparator[] getComparators()
  {
    return comparators;
  }

  /**
   * @param comparators the comparators to set
   */
  public void setComparators(TupleComparator[] comparators)
  {
    this.comparators = comparators;
  }

  /**
   * @return the xmlAggIndexPos
   */
  public int[] getXmlAggIndexPos()
  {
    return xmlAggIndexPos;
  }

  /**
   * @param xmlAggIndexPos the xmlAggIndexPos to set
   */
  public void setXmlAggIndexPos(int[] xmlAggIndexPos)
  {
    this.xmlAggIndexPos = xmlAggIndexPos;
  }

  public void setFunctionInstr(UserDefinedFunction f, int numArgs,
                               Datatype returnType) {
    this.op         = AOp.USR_FNC;
    this.f          = f;
    this.returnType = returnType;
    this.numArgs    = numArgs;
    
    argTypes = new Datatype[numArgs];
    argRoles = new int[numArgs];
    argPos   = new int[numArgs];
    args     = new Object[numArgs];
  }

  public void setXQRYFunctionInstr(UserDefinedFunction f, int numArgs,
				   Datatype returnType) 
  {
    this.f          = f;
    this.returnType = returnType;
    this.numArgs    = numArgs;
    
    argTypes = new Datatype[numArgs];
    argRoles = new int[numArgs];
    argPos   = new int[numArgs];
    args     = new Object[numArgs];
  }
  
  public void setXParseFunctionInstr(XMLDocument parentDoc, boolean _isWF, XMLParseKind _kind, int argRole, int argPos)
  {
    this.op = AOp.XML_PARSE;
    this.returnType = Datatype.XMLTYPE;
    this.xmlParseInstr = new XMLParseInstr(argRole, argPos,_isWF, _kind,parentDoc);
  }
  

  public void setXConcatFunctionInstr(XMLDocument parentDoc, int numArgs)
  {
    this.op = AOp.XML_CONCAT;
    this.returnType = Datatype.XMLTYPE;
    this.numArgs = numArgs;
    this.xmlInstr = new XMLElementInstr(parentDoc);
    
    argTypes = new Datatype[numArgs];
    argRoles = new int[numArgs];
    argPos   = new int[numArgs];
    args     = new Object[numArgs];
  }
  public void setAggrFunctionInstr(int numArgs,Datatype returnType, 
                                   boolean plus) {
    
    if(plus)
      this.op = AOp.UDA_PLUS_HANDLE;
    else
      this.op = AOp.UDA_MINUS_HANDLE;
    this.returnType = returnType;
    this.numArgs    = numArgs;
    argTypes        = new Datatype[numArgs];
    argRoles        = new int[numArgs];
    argPos          = new int[numArgs];
        
    this.aggrInputs = new AggrValue[numArgs];
  }
  
  public void setReleaseAggrHandlersInstr(int[] udaPos,
      IAggrFnFactory[] udaFactory) {
    
    this.op         = AOp.RELEASE_AGGR_HANDLERS;
    this.udaPos     = udaPos;
    this.udaFactory = udaFactory;
  }
  
  public void setResetAggrHandlersInstr(int [] udaPos) {
    this.op     = AOp.RESET_AGGR_HANDLERS;
    this.udaPos = udaPos;
  }
  
  public void setAllocAggrHandlersInstr(int[] udaPos,
      IAggrFnFactory[] udaFactory, IAggrFunction[] udaHandler) {
    this.op         = AOp.ALLOC_AGGR_HANDLERS;
    this.udaFactory = udaFactory;
    this.udaPos     = udaPos;
    this.udaHandler = udaHandler;
  }
  
  public void addFunctionArg(int argNo, Datatype typ, int role, int pos) {
    assert argNo < numArgs : argNo;

    argTypes[argNo] = typ;
    argRoles[argNo] = role;
    argPos[argNo]   = pos;
  }
  
  // toString
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<AInstr>");
    sb.append("<Operation op=\"" + op + "\" />");

    if (op == AOp.USR_FNC || op == AOp.XML_CDATA || op == AOp.XML_COMMENT
        || op == AOp.XML_CONCAT || op == AOp.XML_PARSE)
    {
      sb.append("<ReturnType type=\"" + returnType + "\"/>");
      for (int i = 0; i < numArgs; i++)
      {
        sb.append("<Operand argNo=\"" + i + "\" type=\"" + argTypes[i]
            + "\" + r=\"" + EvalContext.getRoleName(argRoles[i]) + "\" c=\""
            + argPos[i] + "\"/>");
      }
      if (op == AOp.USR_FNC)
        sb.append("<UserDefinedFunctionId id=\"" + fnId + "\"/>");
    } else
    {
      sb.append("<Operand r1=\"" + EvalContext.getRoleName(r1) + "\" c1=\""
          + c1 + "\"/>");
      sb.append("<Operand r2=\"" + EvalContext.getRoleName(r2) + "\" c2=\""
          + c2 + "\"/>");
    }

    sb.append("<Result dr=\"" + EvalContext.getRoleName(dr) + "\" dc=\"" + dc
        + "\"/>");
    sb.append("</AInstr>");
    return sb.toString();
  }
}
