/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPFunctionDefnNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node corresponding to DDL for a user defined function

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    alealves    02/02/09 - support for user function instances in addition to class name
    parujain    08/11/08 - error offset
    sbishnoi    06/07/07 - fix xlint warning 
    anasrini    06/12/06 - getter methods 
    anasrini    06/09/06 - Creation
    anasrini    06/09/06 - Creation
    anasrini    06/09/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPFunctionDefnNode.java /main/4 2009/02/12 03:52:33 alealves Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import java.util.List;
import java.util.LinkedList;

import oracle.cep.common.Datatype;

/**
 * Parse tree node corresponding to DDL for a user defined function
 *
 * @since 1.0
 */

public class CEPFunctionDefnNode implements CEPParseTreeNode {
  
  public enum NameType {
    CLASS_NAME,
    INSTANCE_NAME
  }

  /** The name of the user defined function */
  protected String name;

  /** The parameter specification list */
  protected CEPAttrSpecNode[] paramSpec;

  /** The return type of the function */
  protected Datatype returnType;

  /** The fully qualified java class name of the implementation */
  final protected String className;
  final protected String instanceName;
  
  protected int startOffset;
  
  protected int endOffset;


  /**
   * Constructor for built-in function with at least one parameter
   * 
   * @param name name of the user defined function
   * @param paramSpecList list of parameter specification
   * @param returnType return type of the function
   * @param className fully qualified java class name of the implementation
   */
  public CEPFunctionDefnNode(String name, List<CEPAttrSpecNode> paramSpecList, Datatype returnType,
                      CEPStringTokenNode classNameToken) {

    this.name       = name;
    this.paramSpec  = 
      (CEPAttrSpecNode[])(paramSpecList.toArray(new CEPAttrSpecNode[0]));
    this.returnType = returnType;
    this.className  = classNameToken.getValue();
    this.instanceName = null;
    setStartOffset(paramSpecList.get(0).getStartOffset());
    setEndOffset(classNameToken.getEndOffset());
  }

  /**
   * Constructor for function with at least one parameter
   * @param name name of the user defined function
   * @param paramSpecList list of parameter specification
   * @param returnType return type of the function
   * @param className fully qualified java class name of the implementation
   */
  public CEPFunctionDefnNode(CEPStringTokenNode token, List<CEPAttrSpecNode> paramSpecList, 
      Datatype returnType, CEPStringTokenNode nameToken, NameType nameType) {

    this.name       = token.getValue();
    this.paramSpec  = 
      (CEPAttrSpecNode[])(paramSpecList.toArray(new CEPAttrSpecNode[0]));
    this.returnType = returnType;
    if (nameType == NameType.CLASS_NAME) { 
      this.className  = nameToken.getValue();
      this.instanceName = null;
    } else {
      assert nameType == NameType.INSTANCE_NAME;
      this.className = null;
      this.instanceName = nameToken.getValue();
    }
    setStartOffset(token.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }

  /**
   * Constructor for function with zero parameters
   * @param name name of the user defined function
   * @param returnType return type of the function
   * @param className fully qualified java class name of the implementation
   */
  public CEPFunctionDefnNode(CEPStringTokenNode name, Datatype returnType, CEPStringTokenNode nameToken, NameType nameType) {
    this(name, new LinkedList<CEPAttrSpecNode>(), returnType, nameToken, nameType);
  }

  /**
   * Constructor for function with one parameter
   * @param name name of the user defined function
   * @param pSpec specification of the sole parameter
   * @param returnType return type of the function
   * @param className fully qualified java class name of the implementation
   * 
   */
  public CEPFunctionDefnNode(CEPStringTokenNode token, CEPAttrSpecNode pSpec, 
                      Datatype returnType, CEPStringTokenNode nameToken, NameType nameType) {
    this(token, buildAttrSpecList(pSpec), returnType, nameToken, nameType);
  }

  /**
   * Constructor for function with one parameter
   * @param name name of the user defined function
   * @param pSpec specification of the sole parameter
   * @param returnType return type of the function
   * @param className fully qualified java class name of the implementation
   */
  public CEPFunctionDefnNode(String name, CEPAttrSpecNode pSpec, 
                      Datatype returnType, CEPStringTokenNode classNameToken) {
    this(name, buildAttrSpecList(pSpec), returnType, classNameToken);
  }
  
  private static List<CEPAttrSpecNode> buildAttrSpecList(CEPAttrSpecNode pSpec) {
    List<CEPAttrSpecNode> attrSpecs = new LinkedList<CEPAttrSpecNode>();
    attrSpecs.add(pSpec);
    
    return attrSpecs;
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
   * Get the return type of the function
   * @return the return type of the function
   */
  public Datatype getReturnType() {
    return returnType;
  }

  /**
   * A UDF may be associated to either a class or an instance.
   * This method returns the implementation class name for the UDF.
   * Returns null if an instance name is being used.
   * 
   * @return the name of implementation class
   */
  public String getImplClassName() {
    return className;
  }
  
  /**
   * A UDF may be associated to either a class or an instance.
   * This method returns the implementation logical instance name for the UDF.
   * Returns null if a class is being used instead.
   * 
   * @return logical instance name
   */
  public String getImplInstanceName() {
    return instanceName;
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
