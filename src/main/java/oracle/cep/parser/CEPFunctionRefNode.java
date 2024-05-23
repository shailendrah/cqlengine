/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPFunctionRefNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    parujain    08/11/08 - error offset
    sbishnoi    06/07/07 - fixing xlint warning
    parujain    01/31/07 - drop function
    parujain    01/31/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPFunctionRefNode.java /main/3 2008/08/25 19:27:24 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.List;
import java.util.LinkedList;

public class CEPFunctionRefNode implements CEPParseTreeNode {
  /** The name of the user defined function */
  protected String name;

  /** The parameter specification list */
  protected CEPAttrSpecNode[] paramSpec;
  
  protected int startOffset;
  
  protected int endOffset;

  /**
   * Constructor for function with at least one parameter
   * @param name name of the user defined function
   * @param paramSpecList list of parameter specification
   */
  public CEPFunctionRefNode(CEPStringTokenNode nameToken, List<CEPAttrSpecNode> paramSpecList) {
    this.name       = nameToken.getValue();
    this.paramSpec  = 
      (CEPAttrSpecNode[])(paramSpecList.toArray(new CEPAttrSpecNode[0]));
    setStartOffset(nameToken.getStartOffset());
    if(paramSpecList.size() > 0)
      setEndOffset(paramSpecList.get(paramSpecList.size()-1).getEndOffset());
    else
      setEndOffset(nameToken.getEndOffset());
  }

  /**
   * Constructor for function with zero parameters
   * @param name name of the user defined function
   */
  public CEPFunctionRefNode(CEPStringTokenNode nameToken) {
    this(nameToken, new LinkedList<CEPAttrSpecNode>());
  }

  /**
   * Constructor for function with one parameter
   * @param name name of the user defined function
   * @param pSpec specification of the sole parameter
   */
  public CEPFunctionRefNode(CEPStringTokenNode nameToken, CEPAttrSpecNode pSpec) { 
    this.name         = nameToken.getValue();
    this.paramSpec    = new CEPAttrSpecNode[1];
    this.paramSpec[0] = pSpec;
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(pSpec.getEndOffset());
  }
// Getter methods

  /**
   * Get the name of the function
   * @return the name of the function
   */
  public String getName() {
    return name;
  }

  /**
   * Get the parameter specification list
   * @return the parameter specification list
   */
  public CEPAttrSpecNode[] getParamSpecList() {
    return paramSpec;
  }

  /**
   * Get the number of parameters to this function
   * @return the number of parameters to this function
   */
  public int getNumParams() {
    if (paramSpec != null)
      return paramSpec.length;
    
    return 0;
  }

  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }


}
