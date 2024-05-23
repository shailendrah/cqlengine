/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/ObjectFactoryContext.java /main/2 2008/10/24 15:50:21 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/10/08 - remove statics
 najain      06/27/06 - add object 
 najain      06/16/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/ObjectFactoryContext.java /main/2 2008/10/24 15:50:21 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import oracle.cep.phyplan.PhyOpt;
import oracle.cep.service.ExecContext;

/**
 * Object Allocations Factory Context
 * 
 * @since 1.0
 */

public class ObjectFactoryContext
{
  ExecContext execContext;
  String objectType;
  PhyOpt opt;
  Object obj;

  public ObjectFactoryContext(ExecContext ec)
  {
    this.execContext = ec;
    this.objectType = null;
    this.opt        = null;
  }

  public ObjectFactoryContext(ExecContext ec, String objectType)
  {
    this.execContext = ec;
    this.objectType = objectType;
    this.opt        = null;
  }

  public ObjectFactoryContext(ExecContext ec, String objectType, PhyOpt opt)
  {
    this.execContext = ec;
    this.objectType = objectType;
    this.opt        = opt;
  }
  
  public String getObjectType()
  {
    return objectType;
  }

  public void setObjectType(String objectType)
  {
    this.objectType = objectType;
  }

  public Object getObject()
  {
    return obj;
  }

  public void setObject(Object obj)
  {
    this.obj = obj;
  }

  public PhyOpt getOpt()
  {
    return opt;
  }

  public void setOpt(PhyOpt opt)
  {
    this.opt = opt;
  }

  public ExecContext getExecContext()
  {
    return execContext;
  }
  
}
