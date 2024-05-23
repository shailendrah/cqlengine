/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPWindowDefnNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    03/05/07 - Generic Window Definition
    parujain    03/05/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPWindowDefnNode.java /main/3 2008/08/25 19:27:24 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.LinkedList;
import java.util.List;

public class CEPWindowDefnNode implements CEPParseTreeNode {
  /** The name of the user defined window */
  protected String name;

  /** The parameter specification list */
  protected CEPAttrSpecNode[] paramSpec;
  
  /** The fully qualified java class name of the implementation */
  // This should be an of GenericTimeWindow type
  protected String className;
  
  protected int startOffset;
  
  protected int endOffset;
  
  /**
   * Constructor for Generic window with at least one parameter
   * @param name name of the generic window like rangeslide
   * @param paramSpecList list of parameter specification
   * @param className fully qualified class name of the implementation
   */
  public CEPWindowDefnNode(CEPStringTokenNode nameToken, List<CEPAttrSpecNode> paramSpecList,CEPStringTokenNode classNameToken) {

    this.name       = nameToken.getValue();
    this.paramSpec  = 
      (CEPAttrSpecNode[])(paramSpecList.toArray(new CEPAttrSpecNode[0]));
    this.className  = classNameToken.getValue();
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(classNameToken.getEndOffset());
  }

  /**
   * Constructor for Generic window with zero parameters
   * @param name name of the generic window like rangeslide
   * @param className fully qualified class name of the implementation
   */
  public CEPWindowDefnNode(CEPStringTokenNode nameToken, CEPStringTokenNode classNameToken) {
    this(nameToken, new LinkedList<CEPAttrSpecNode>(), classNameToken);
  }
  
  /**
   * Constructor for Generic window with one parameter
   * @param name name of the generic window like rangeslide
   * @param pSpec specification of the parameter
   * @param className fully qualified class name of the implementation
   */
  public CEPWindowDefnNode(CEPStringTokenNode nameToken, CEPAttrSpecNode pSpec, 
                    CEPStringTokenNode classNameToken) {

    this.name         = nameToken.getValue();
    this.paramSpec    = new CEPAttrSpecNode[1];
    this.paramSpec[0] = pSpec;
    this.className    = classNameToken.getValue();
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(classNameToken.getEndOffset());

  }
  
  /**
   * Get the name of the window
   * @return the name of the window
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
   * Get the number of parameters to this window
   * @return the number of parameters to this window
   */
  public int getNumParams() {
    if (paramSpec != null)
      return paramSpec.length;
    
    return 0;
  }
  
  /**
   * Get the name of the implementation class
   * @return the name of implementation class
   */
  public String getImplClassName() {
    return className;
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
