/* $Header: pcbpel/cep/server/src/oracle/cep/interfaces/JavaDriverContext.java /main/4 2009/02/17 17:42:52 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      01/28/09 - add usage of output dest id
    hopark      10/09/08 - remove statics
    sbishnoi    02/18/08 - support for xml type epr
    sbishnoi    12/11/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/interfaces/JavaDriverContext.java /main/4 2009/02/17 17:42:52 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.service.ExecContext;

public class JavaDriverContext extends InterfaceDriverContext
{
  private static final String CLASSNAME_KEY = "ClassName";
  private static final String DESTID_KEY = "Id";
  
  private boolean  hasDestId;
  private String   destId;
  private String   className;
  private Object   eprArguments;
  
  /**
   * Constructor for creation of JavaDriver with Class Name given
   * through EPR in URI format
   * @param className
   */
  public JavaDriverContext(ExecContext ec, String val, boolean destinationId)
  {
    super(ec, InterfaceType.JAVA);
    if (destinationId)
      setDestinationId(val);
    else 
      setClassName(val);
  }
  
  /**
   * Constructor for creation of JavaDriver with Class Name and URL given
   * through EPR in XML format
   * @param classNameWithArgs
   */
  public JavaDriverContext(ExecContext ec, InterfaceDriver.KeyValue[] parsedEprObjects)
    throws CEPException
  {
    super(ec, InterfaceType.JAVA);
    int len = parsedEprObjects.length;
    assert len > 1;
    if (parsedEprObjects[1].getKey().equals(CLASSNAME_KEY))
    {
      assert parsedEprObjects[1].getValue() instanceof String;
      setClassName((String)parsedEprObjects[1].getValue());
    } else if (parsedEprObjects[1].getKey().equals(DESTID_KEY))
    {
      assert parsedEprObjects[1].getValue() instanceof String;
      setDestinationId((String)parsedEprObjects[1].getValue());
    } else {
      throw new CEPException(InterfaceError.INVALID_JAVADESTINATION_TYPE, parsedEprObjects[1].getKey());
    }
    if(len > 2)
      eprArguments = parsedEprObjects[2].getValue();
  }
  
  private void setClassName(String className)
  {
    hasDestId = false;
    this.className = className;
  }
  
  private void setDestinationId(String id)
  {
    hasDestId = true;
    destId = id;
  }
  
  /**
   * Get Class Name
   * @return className for particular JavaDriverContext
   */
  public String getClassName()
  {
    return className;
  }
  
  public String getDestinationId()
  {
    return destId;
  }
  
  public boolean hasDestinationId()
  {
    return hasDestId;
  }
  
  /**
   * Get Class Arguments
   * @return arguments for particular JavaDriverContext
   */
  public Object getArguments()
  {
    return eprArguments;
  }
  
  public boolean hasArguments()
  {
    if(eprArguments == null)
      return false;
    else
      return true;
  }
}