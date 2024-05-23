/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPTableMonitorNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

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
    sborah      11/24/08 - support for altering base timeline
    parujain    08/11/08 - error offset
    parujain    05/08/07 - Set systimestamped for stats
    parujain    05/08/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPTableMonitorNode.java /main/3 2008/11/25 22:04:21 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

public class CEPTableMonitorNode implements CEPParseTreeNode {

  /** The name of the stream */
  protected String name;

  protected boolean isEnabled;
  
  /**
   * boolean indicates whether to use milliseconds or nanoseconds 
   * as base timeline. 
   * true = use Millisecond as base timeline
   * false = use Nanosecond as base timeline
   * Default is Nanosecond
   */
  protected boolean isBaseTimelineMillisecond;
  
  protected int startOffset;
  
  protected int endOffset;

  
  /**
   * Constructor 
   * @param name 
   *         Name of Stream
   * @param isEnabled
   *          Whether enabled or disabled
   */
   public CEPTableMonitorNode( CEPStringTokenNode nameToken, boolean isEnabled) 
   {
     this.name     = nameToken.getValue();
     this.isEnabled = isEnabled;
     this.setBaseTimelineMillisecond(false);
     setStartOffset(nameToken.getStartOffset());
     setEndOffset(nameToken.getEndOffset());
   }
 /**
  * Constructor 
  * @param name 
  *         Name of Stream
  * @param isEnabled
  *          Whether enabled or disabled
  * @param isBaseTimelineMillisecond 
  *           Whether base timeline used is millisecond or nanosecond
  */
  public CEPTableMonitorNode( CEPStringTokenNode nameToken, boolean isEnabled, 
      boolean isBaseTimelineMillisecond) 
  {
    this.name     = nameToken.getValue();
    this.isEnabled = isEnabled;
    this.setBaseTimelineMillisecond(isBaseTimelineMillisecond);
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }

  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }
  
  
  /**
   * 
   * @return Returns whether Enabled or disabled
   */
  public boolean getIsEnabled()
  {
    return isEnabled;
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
   * @return the isBaseTimelineMillisecond
   */
  public boolean getIsBaseTimelineMillisecond()
  {
    return isBaseTimelineMillisecond;
  }

  /**
   * @param isBaseTimelineMillisecond the isBaseTimelineMillisecond to set
   */
  public void setBaseTimelineMillisecond(boolean isBaseTimelineMillisecond)
  {
    this.isBaseTimelineMillisecond = isBaseTimelineMillisecond;
  }


}

