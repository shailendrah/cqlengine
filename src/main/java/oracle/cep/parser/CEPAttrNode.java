/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPAttrNode.java /main/8 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
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
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/10/09 - adding alias to toString
    parujain    08/13/08 - error offset
    mthatte     04/07/08 - adding toString()
    anasrini    02/22/06 - add getter methods 
    anasrini    12/20/05 - parse tree node for a referenced attribute 
    anasrini    12/20/05 - parse tree node for a referenced attribute 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPAttrNode.java /main/7 2009/08/31 10:57:02 alealves Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.Constants;


/**
 * Parse tree node for a referenced attribute, where a attribute in this case
 * is a literal.
 */

public class CEPAttrNode extends CEPExprNode {

  /** Relation/Stream name/alias */
  protected String varName;

  /** Attribute name */
  protected String attrName;

  /**
   * Constructor for a fully qualified attribute name R.A
   * @param entityName relation or stream name or alias
   * @param attrName attribute name
   */
  public CEPAttrNode(CEPStringTokenNode varNameToken, CEPStringTokenNode attrNameToken) {
    this.varName  = varNameToken.getValue();
    setStartOffset(varNameToken.getStartOffset());
    if(attrNameToken != null)
    {
      this.attrName = attrNameToken.getValue();
      setEndOffset(attrNameToken.getEndOffset());
    }
    else
    {
      this.attrName = null;
      setEndOffset(varNameToken.getEndOffset());
    }
  }
  
  /**
   * Constructor for a fully qualified attribute name R.A
   * @param entityName relation or stream name or alias
   * @param attrName attribute name
   */
  public CEPAttrNode(String varName, CEPStringTokenNode attrNameToken) {
    this.varName  = varName;
    if(attrNameToken != null)
    {
      this.attrName = attrNameToken.getValue();
      setStartOffset(attrNameToken.getStartOffset());
      setEndOffset(attrNameToken.getEndOffset());
    }
    else
    {
      this.attrName = null;
      setStartOffset(0);
      setEndOffset(0);
    }
  }

  /**
   * Constructor for a fully qualified attribute name R.A
   * @param entityName relation or stream name or alias
   * @param attrName attribute name
   */
  public CEPAttrNode(CEPStringTokenNode varNameToken, String attrName) {
    this.varName  = varNameToken.getValue();
    this.attrName = attrName;
    setStartOffset(varNameToken.getStartOffset());
    setEndOffset(varNameToken.getEndOffset());
  }


  /**
   * Constructor for an attribute name of the form A
   * @param attrName attribute name
   */
  public CEPAttrNode(CEPStringTokenNode attrNameToken) {
    this.attrName   = attrNameToken.getValue();
    setStartOffset(attrNameToken.getStartOffset());
    setEndOffset(attrNameToken.getEndOffset());
  }

  /**
   * Constructor for an attribute name of the form A
   * @param attrName attribute name
   */
  public CEPAttrNode(String attrName) {
    this.attrName   = attrName;
    setStartOffset(0);
    setEndOffset(0);
  }


  /**
   * Is this a fully qualified attribute name 
   * @return true if name is fully qualified, else false
   */
  public boolean isFullyQualifiedName() {
    return (varName != null);
  }

  // getter methods

  /**
   * Get the variable name
   * @return the variable name
   */
  public String getVarName() {
    return varName;
  }

  /**
   * Get the attribute name
   * @return the attribute name
   */
  public String getAttrName() {
    return attrName;
  }
  
  private boolean isDefaultSubsetVar()
  {
    if(varName != null)
      return varName.equals(Constants.DEFAULT_SUBSET_NAME);
    else
      return false;
  }
  
  public String getExpression()
  {
    if(isFullyQualifiedName() && !isDefaultSubsetVar())
    {
      if(attrName == null)
        return " " + this.varName + ".*";
      else
        return " " + this.varName + "." + this.attrName + " ";
    }
      else
      return " " + this.attrName + " ";
  }
  
  public String toString() 
  {
    if(alias != null)
    {
      if(isFullyQualifiedName() && !isDefaultSubsetVar()) 
      {
        if(attrName == null)
          return " " + this.varName + ".*" + " AS " + this.alias + " ";
        else
          return " " + this.varName + "." + this.attrName + " AS " + this.alias + " ";
      }
      else
        return " " + this.attrName + " AS " + this.alias + " ";
    }
    
    else
    {
      if(isFullyQualifiedName() && !isDefaultSubsetVar())
      {
        if(attrName == null)
          return " " + this.varName + ".*" + " ";
        else
          return " " + this.varName + "." + this.attrName + " ";
        
      }
      else
        return " " + this.attrName + " ";
    }
  }
}
