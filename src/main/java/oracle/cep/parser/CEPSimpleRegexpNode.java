/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSimpleRegexpNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node corresponding to a simple regular expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    mthatte     02/05/09 - adding toString
    parujain    08/12/08 - error offset
    rkomurav    03/18/08 - change return type of collectcorrnames
    rkomurav    03/02/08 - add getAllreferencedCorrAttrs
    rkomurav    09/26/07 - support correlation string names
    anasrini    01/09/07 - Creation
    anasrini    01/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPSimpleRegexpNode.java /main/6 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import java.util.List;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node corresponding to a simple regular expression. 
 * <p>
 * A simple regular expression is an name from the set of names
 * over which this regular expression is composed
 *
 * @since 1.0
 */

public class CEPSimpleRegexpNode extends CEPRegexpNode {
  
  /** The name **/
  protected String name;

  /**
   * Constructor
   * @param name the name that is this simple regular expression
   */
  public CEPSimpleRegexpNode(CEPStringTokenNode name) {
    this.name = name.getValue();
    setStartOffset(name.getStartOffset());
    setEndOffset(name.getEndOffset());
  }

  // getter methods

  /**
   * Get the name that makes up this simple regular expression
   * @return the name that makes up this simple regular expression
   */
  public String getName() {
    return name;
  }
  
  public String toString()
  {
    return name;
  }

  /**
   * Get all the referenced correlation names
   */
  public boolean getAllReferencedCorrNames(List<String> corrs)
  {
    if(corrs.contains(this.name))
      return true;
    else
    {
      corrs.add(this.name);
      return false;
    }
  }
  
  public String toVisualizerXml()
  {
    return "\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.patternAttrTag, toString().trim(), null, null);
  }
}
