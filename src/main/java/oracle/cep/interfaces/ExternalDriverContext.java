/* $Header: pcbpel/cep/server/src/oracle/cep/interfaces/ExternalDriverContext.java /main/2 2008/10/24 15:50:15 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    parujain    11/09/07 - external source context
    parujain    11/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/interfaces/ExternalDriverContext.java /main/2 2008/10/24 15:50:15 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.interfaces;

import oracle.cep.service.ExecContext;

public class ExternalDriverContext extends InterfaceDriverContext
{
  private String connectString;
  
  /** stream or relation id that is interested */
  private int id;
  
  public ExternalDriverContext(ExecContext ec){
    super(ec, InterfaceType.EXTERNAL);
  }
  
  public ExternalDriverContext(ExecContext ec, String connectMe, int tblId) {
   super(ec, InterfaceType.EXTERNAL);
   this.connectString = connectMe;
   this.id = tblId;
  }
  
  public String getConnectString()
  {
    return this.connectString;
  }
  
  public void setConnectString(String connectStr)
  {
    this.connectString = connectStr;
  }
  
  public int getTableId()
  {
    return id;
  }

}
