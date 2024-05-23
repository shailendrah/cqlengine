/* $Header: pcbpel/cep/server/src/oracle/cep/interfaces/MLFFileDriverContext.java /main/4 2009/02/17 17:42:52 hopark Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/04/09 - use keyvalue
    hopark      10/09/08 - remove statics
    sbishnoi    08/05/08 - adding support for convertTs in EPR
    udeshmuk    03/10/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/interfaces/MLFFileDriverContext.java /main/4 2009/02/17 17:42:52 hopark Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLElement;

/**
 *  Context needed for initializing a MLFFileDriver
 */

public class MLFFileDriverContext extends InterfaceDriverContext{
  
  /** object name of interest */
  private String object_name;
  
  /** stream or relation id that is interested */
  private int id;
  
  /** field separator char, default ',' */
  private char fs;
  
  /** multi-line-field encapsulator char, default '"' */
  private char ec;
  
  /** a flag to check whether to apply TS Conversion (ns->ms or ms->ts)*/
  private boolean convertTs;
  
  public MLFFileDriverContext(ExecContext ec, String objName, int id){
    super(ec, InterfaceType.MLFFILE);
    this.object_name = objName;
    this.id = id;
    this.fs = ',';
    this.ec = '"';
    this.convertTs = true;
  }
  
  public MLFFileDriverContext(ExecContext exc, String objName, int id, char fs, char ec){
    super(exc, InterfaceType.MLFFILE);
    this.object_name = objName;
    this.id = id;
    this.fs = fs;
    this.ec = ec;
    this.convertTs = true;
  }
  
  public MLFFileDriverContext(ExecContext ec, InterfaceDriver.KeyValue[] parsedEprObjects, int id){
    super(ec, InterfaceType.MLFFILE);
    this.id = id;
    int len = parsedEprObjects.length;
    assert len > 1;
    assert parsedEprObjects[1].getValue() instanceof String;
    this.object_name = (String) parsedEprObjects[1].getValue();
    this.convertTs = true;
    if (len > 2) {
      assert parsedEprObjects[2].getValue() instanceof XMLElement;
      XMLElement eprArgument = (XMLElement) parsedEprObjects[2].getValue();
      
      NodeList lst = eprArgument.getChildrenByTagName("fs");
      Node elem    = lst.item(0);
      lst  = elem.getChildNodes();
      elem = lst.item(0);
      this.fs = elem.getNodeValue().charAt(0);
     
      lst  = eprArgument.getChildrenByTagName("ec");
      elem = lst.item(0);
      lst  = elem.getChildNodes();
      elem = lst.item(0);
      this.ec = elem.getNodeValue().charAt(0);
    }
    else
    {
      this.fs = ',';
      this.ec = '"';
    }
  }
  
  public String getObject_name(){
    return this.object_name;
  }
  
  public int getId()
  {
    return id;
  }
  
  public char getFS(){
    return this.fs;
  }
  
  public char getEC(){
    return this.ec;  
  }
  
  /**
   * Sets the flag whether we want to convert time-stamp unit
   * @param convertTs
   */
  public void setConvertTs(boolean convertTs){
    this.convertTs=  convertTs;
  }
  
  /**
   * Return the flag whether we should convert time-stamp unit
   */
  public boolean getConvertTs(){
    return this.convertTs;
  }
}
