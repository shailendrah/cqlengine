/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRecognizeNode.java /main/14 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node corresponding to the pattern recognition clause

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    udeshmuk    03/05/10 - within clause support
    udeshmuk    02/02/09 - support for duration arith_expr in pattern
    skmishra    01/30/09 - add toQCXML, refactor duration
    udeshmuk    09/07/08 - 
    parujain    08/11/08 - error offset
    udeshmuk    07/12/08 - 
    rkomurav    07/04/08 - modify duration clause
    udeshmuk    06/16/08 - add boolean to indicate if default subset creation
                           is needed.
    rkomurav    05/09/08 - add duration clause
    rkomurav    03/11/08 - support subset
    anasrini    09/24/07 - ALL MATCHES support
    anasrini    06/25/07 - support for partition by clause
    anasrini    05/14/07 - support for MEASURES node
    anasrini    01/09/07 - Creation
    anasrini    01/09/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRecognizeNode.java /main/13 2010/03/23 01:50:14 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import java.util.List;

import oracle.cep.pattern.PatternSkip;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node corresponding to the pattern recognition clause
 *
 * @since 1.0
 */

public class CEPRecognizeNode implements CEPParseTreeNode {

  /** The partition by attribute list **/
  protected CEPAttrNode[]            partByAttrs;

  /** The measure list expressions and alias names **/
  protected CEPPatternMeasuresNode   measures;

  /** The SKIP clause **/
  protected PatternSkip              skipClause;

  /** The pattern **/
  protected CEPPatternNode           pattern;

  /** The definition of the correlation names **/
  protected CEPPatternDefinitionNode definitions;
  
  /** Subset Definitions */
  protected CEPSubsetDefNode[]       subsetDefs;
  
  /** duration Subclause */
  protected CEPPatternDurationNode   duration;
  
  /** within subclause */
  protected CEPPatternWithinNode     withinNode;
  
  /** true if DEFINE/MEASURES clause contains some non-qualified reference to a field */
  protected boolean                  defaultSubsetNeeded;
  
  protected int                      startOffset = 0;
  
  protected int                      endOffset = 0;

  protected String                   cqlProperty;
  
  protected String                   alias;
  /**
   * Constructor 
   * @param partByAttrs the attributes to partition by
   * @param measures the measure list
   * @param skipClause the SKIP clause
   * @param pattern the pattern string
   * @param definitions the definitions for the correlation names
   * @param subsetDefs subset definitions
   * @param duration used in non-event detection
   * @param within clause
   * @param defaultSubsetNeeded boolean to indicate if a default subset should be created
   */
  public CEPRecognizeNode(List<CEPAttrNode> partByAttrs,
                   CEPPatternMeasuresNode measures,
                   PatternSkip skipClause,
                   CEPPatternNode pattern, 
                   CEPPatternDefinitionNode definitions,
                   List<CEPSubsetDefNode> subsetDefs,
                   CEPPatternDurationNode durationNode,
                   CEPPatternWithinNode withinNode,
                   boolean defaultSubsetNeeded
                   ) 
  {
    this.pattern          = pattern;
    this.measures         = measures;
    this.skipClause       = skipClause;
    this.definitions      = definitions;
    this.partByAttrs      = null;
    this.subsetDefs       = null;
    
    if(durationNode != null)
    {
      this.duration       = durationNode;
    }
    
    if(withinNode != null)
    {
      this.withinNode = withinNode;
    }
    
    if (partByAttrs != null)
    {
      this.partByAttrs = partByAttrs.toArray(new CEPAttrNode[0]); 
      setStartOffset(partByAttrs.get(partByAttrs.size()-1).getStartOffset());
    }
    
    else if(measures != null)
      setStartOffset(measures.getStartOffset());
    if(subsetDefs != null)
    {
      this.subsetDefs = 
        (CEPSubsetDefNode[])subsetDefs.toArray(new CEPSubsetDefNode[0]);
    }
    this.defaultSubsetNeeded = defaultSubsetNeeded;
  }

  // getter methods

  /**
   * Get the parser representation of the pattern string
   * @return the parser representation of the pattern string
   */
  public CEPPatternNode getPatternString() {
    return pattern;
  }

  /**
   * Get the attributes in the partition by clause
   * @return array of attributes in the partition by clause (if present)
   *         If there is no partition by clause, then return null
   */
  public CEPAttrNode[] getPartitionByAttrs() {
    return partByAttrs;
  }

  /**
   * Get the measure list information
   * @return the measure list information
   */
  public CEPPatternMeasuresNode getMeasures() {
    return measures;
  }

  /**
   * Get the SKIP clause
   * @return the SKIP clause
   */
  public PatternSkip getSkipClause() {
    return skipClause;
  }

  public void setCqlProperty(String arg)
  {
    cqlProperty = arg;
  }
  
  public void setAlias(String arg)
  {
    alias = arg;
  }
  
  /**
   * Is the SKIP clause ALL MATCHES
   * @return true iff it is ALL MATCHES
   */
  public boolean isAllMatches() {
    return (skipClause == PatternSkip.ALL_MATCHES);
  }

  /**
   * Get the definitions of the correlation names
   * @return the definitions of the correlation names
   */
  public CEPPatternDefinitionNode getCorrDefinitions() {
    return definitions;
  }

  /**
   * Get the definitions of subsets
   * @return the subsetDefs
   */
  public CEPSubsetDefNode[] getSubsetDefs()
  {
    return subsetDefs;
  }

  /**
   * @return true if default subset (containing all correlation names) should be created
   */
  public boolean isDefaultSubsetNeeded()
  {
    return this.defaultSubsetNeeded;
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
  
  /**
   * @return Return the pattern duration node
   */
  public CEPPatternDurationNode getDuration()
  {
    return duration;
  }
  
  public CEPPatternWithinNode getWithinClause()
  {
    return withinNode;
  }
  
  public String toString()
  {
    StringBuilder myString = new StringBuilder(100);
    if(partByAttrs != null) {
      myString.append(" PARTITION BY ");
      for(CEPAttrNode a: partByAttrs)
        myString.append(a.toString() + ",");
      myString.deleteCharAt(myString.length()-1);
    }
    
    //MEASURES
    if(measures != null)
    {
      myString.append(" MEASURES ");
      myString.append(measures.toString());
    }
      
    // ?? ALL MATCHES ??
    myString.append(skipClause.toString());
    
    // INCLUDE TIMER EVENTS
    if(duration != null) 
    {
      myString.append(" INCLUDE TIMER EVENTS ");
    }
    
    // PATTERN
    if(pattern != null)
    {
      myString.append(pattern.toString());
    }
    
    if(subsetDefs!=null)
    {
      myString.append(" SUBSET ");
      for(CEPSubsetDefNode s : subsetDefs)
        myString.append(s.toString());
    }
    // DURATION
    if(duration != null)
    {
      myString.append(duration.toString());
    }
    
    if(withinNode != null)
    {
      myString.append(withinNode.toString());
    }
    
    if(definitions != null)
    {
      myString.append(definitions.toString());
    }
    
    return myString.toString();
  }
  
  //TODO: Incomplete.
  public int toQCXML(StringBuffer qXml, int operatorID) 
            throws UnsupportedOperationException
  {
    int rootID = operatorID + 1;
    int myID = operatorID;
    int inpID = operatorID - 1;
    
    StringBuilder myXmlString = new StringBuilder(100);
    StringBuilder tempStringBuilder = new StringBuilder(24);

    String inputsXml = "\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.inputTag, String.valueOf(inpID), null, null);
    inputsXml = "\n\t" + XMLHelper.buildElement(true, VisXMLHelper.inputsTag, inputsXml, null, null) + "\n";

    myXmlString.append(inputsXml);
    myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.cqlPropertyTag, cqlProperty, null, null));
    myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.patternSkipTag, skipClause.toVisualizerString(), null, null));

    if(partByAttrs != null)
    {
      for(CEPAttrNode c : partByAttrs)
      {
        tempStringBuilder.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.partitionAttrTag, c.toString().trim(), null, null));
      }
      myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.partitionByListTag, tempStringBuilder.toString().trim(), null, null));
      tempStringBuilder = null;
    }

    if(definitions != null)
    {
      myXmlString.append("\n\t" + definitions.toVisualizerXML());
    }
    
    if(measures != null)
    {
      myXmlString.append("\n\t" + measures.toVisualizerXML());
    }
    
    if(pattern != null)
    {
      myXmlString.append("\n\t" + pattern.toVisualizerXML());
    }

    if(subsetDefs != null)
    {
      tempStringBuilder = new StringBuilder(24);
      for(CEPSubsetDefNode s : subsetDefs)
        tempStringBuilder.append(s.toVisualizerXML());
      myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.subsetsTag, tempStringBuilder.toString().trim(), null, null));
    }

    if(duration != null)
    {
      myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.timerEventTag, duration.toVisualizerXml().trim(), null, null));
    }
    
    //TODO: Add WITHIN clause specific XML dump when it will have to be 
    //      supported in the visualizer. Currently raise exception.

    if(withinNode != null)
    {
      throw new UnsupportedOperationException("Not supported for a pattern query with WITHIN clause"); 
    }
    
    if(alias != null)
    {
      myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.aliasTag, this.alias, null, null));
    }
    
    qXml.append("\n" + XMLHelper.buildElement(true, VisXMLHelper.operatorTag, myXmlString.toString().trim(), new String[]{VisXMLHelper.operatorIdAttr,VisXMLHelper.operatorTypeAttr}, new String[] {String.valueOf(myID),VisXMLHelper.patternOperator}));
    return rootID;
  }
  
}
