/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSubsetDefNode.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
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
    skmishra    02/09/09 - toQCXML support
    parujain    08/11/08 - error offset
    udeshmuk    06/16/08 - change visibility of constructor to 'public' so that
                           it is accesible from PatternStreamInterp.
    rkomurav    03/11/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPSubsetDefNode.java /main/5 2009/02/27 14:19:31 skmishra Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.Iterator;
import java.util.List;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

public class CEPSubsetDefNode implements CEPParseTreeNode
{
  /** Name of the subset being defined*/
  protected String   subsetName;
  
  /** List of correlation names which the subset refers */
  protected String[] corrs;
  
  protected int startOffset = 0;
  
  protected int endOffset = 0;
  
  /**
   * Constructor
   * @param subsetName
   * @param corrList
   */
  public CEPSubsetDefNode(CEPStringTokenNode name, List<CEPStringTokenNode> corrList)
  {
    this.subsetName = name.getValue();
    this.corrs  = new String[corrList.size()];
    int i =0;
    Iterator<CEPStringTokenNode> it =  corrList.iterator();
    while(it.hasNext())
    {
      this.corrs[i] = it.next().getValue();
      i++;
    }
    setStartOffset(name.getStartOffset());
    if(!corrList.isEmpty())
      setEndOffset(corrList.get(corrList.size()-1).getEndOffset());
    else
      setEndOffset(name.getEndOffset());
  }

  /**
   * Constructor
   * @param subsetName
   * @param corrList
   */
  public CEPSubsetDefNode(String subsetName, List<String> corrList)
  {
    this.subsetName = subsetName;
    this.corrs      = (String[])corrList.toArray(new String[0]);
    setStartOffset(0);
    setEndOffset(0);
  }

  /**
   * Get the referenced correlation name
   * @return the corrs
   */
  public String[] getCorrs()
  {
    return corrs;
  }

  /**
   * Get the subset name
   * @return the subsetName
   */
  public String getSubsetName()
  {
    return subsetName;
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

  private String getCorrCSV()
  {
    StringBuilder myString = new StringBuilder();
    for(String c:getCorrs())
      myString.append(c+",");
    myString.deleteCharAt(myString.length() - 1);
    return myString.toString().trim();
  }
  
  public String toString()
  {
    return " " + subsetName + "=(" + getCorrCSV() + ") ";
  }
  
  public String toVisualizerXML()
  {
    StringBuilder myXmlString = new StringBuilder(20);
    myXmlString.append("\n\t\t\t" + XMLHelper.buildElement(true, VisXMLHelper.subsetNameTag, this.subsetName, null, null));
    myXmlString.append("\n\t\t\t" + XMLHelper.buildElement(true, VisXMLHelper.corrAttrNamesTag, "(" + getCorrCSV() + ")", null, null));
    return XMLHelper.buildElement(true, VisXMLHelper.subsetTag, myXmlString.toString().trim(), null, null);
  }
}
