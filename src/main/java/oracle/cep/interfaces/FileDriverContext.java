/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/FileDriverContext.java /main/6 2010/02/23 07:04:39 hopark Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/22/10 - add initialDelay
    hopark      01/29/09 - api change
    hopark      10/09/08 - remove statics
    sbishnoi    08/04/08 - support for nanosecond
    rkomurav    03/27/07 - delay scale
    anasrini    09/12/06 - remove warnings
    najain      03/29/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/FileDriverContext.java /main/6 2010/02/23 07:04:39 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.interfaces;

import oracle.cep.service.ExecContext;

/**
 * Context needed for instantiating a file driver
 *
 * @author najain
 */
public class FileDriverContext extends InterfaceDriverContext
{
  /** object name of interest */
  private String object_name;

  /** stream or relation id that is interested */
  private int id;
  
  private int delayScale;
  
  private int startDelay;
  
  /** a flag to check whether to apply TS Conversion (ns->ms or ms->ts)*/
  private boolean convertTs;

  /**
   * Constructor for FileDriverContext
   * @param object_name name of interest
   */
  public FileDriverContext(ExecContext ec, String object_name)
  {
    super(ec, InterfaceType.FILE);
    this.object_name = object_name;
  }

  /**
   * Constructor for FileDriverContext
   * @param object_name name of interest
   * @param id id of interested stream/relation
   */
  public FileDriverContext(ExecContext ec, String object_name, int id, int delayScale)
  {
    super(ec, InterfaceType.FILE);
    this.object_name = object_name;
    this.id = id;
    this.delayScale = delayScale;
  }

  /**
   * Getter for object_name in FileDriverContext
   * @return Returns the object_name
   */
  public String getObject_name()
  {
    return object_name;
  }

  /**
   * Setter for object_name in FileDriverContext
   * @param object_name The object_name to set.
   */
  public void setObject_name(String object_name)
  {
    this.object_name = object_name;
  }

  /**
   * Getter for id in FileDriverContext
   * @return Returns the id
   */
  public int getId()
  {
    return id;
  }

  /**
   * Setter for id in FileDriverContext
   * @param id The id to set.
   */
  public void setId(int id)
  {
    this.id = id;
  }

  /**
   * @return the delayScale
   */
  public int getDelayScale() {
    return delayScale;
  }
  /**
   * Return a flag to check whether we should change timestamp unit
   * @return
   */
  public boolean getConvertTs(){
    return this.convertTs;
  } 
  
  /**
   * Set a flag to decide whether we should change timestamp unit
   * @param convertTs
   */
  public void setConvertTs(boolean convertTs){
    this.convertTs = convertTs;
  }

  /**
   * Return the start delay
   * @return
   */
  public int getStartDelay(){
    return this.startDelay;
  } 
  
  /**
   * Set the start delay
   * @param startDelay
   */
  public void setStartDelay(int n){
    this.startDelay = n;
  }
}

