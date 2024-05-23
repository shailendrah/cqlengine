/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/ExternalInstr.java /main/3 2012/10/09 05:16:40 sbishnoi Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/09/12 - XbranchMerge
                           sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0_11.1.1.7.0
                           from st_pcbpel_11.1.1.4.0
    sbishnoi    10/08/12 - XbranchMerge sbishnoi_bug-13251101_ps6_pt.11.1.1.7.0
                           from st_pcbpel_pt-11.1.1.7.0
    sbishnoi    10/01/12 - adding external source name parameter
    sbishnoi    12/04/08 - support for generic data source
    parujain    11/15/07 - External source access
    parujain    11/15/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/ExternalInstr.java /main/3 2012/10/09 05:16:40 sbishnoi Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals;

import java.util.LinkedList;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.datasource.IExternalPreparedStatement;

public class ExternalInstr {

  public int                   numArgs;
  LinkedList<Datatype>         argTypes;
  LinkedList<Integer>          argRoles;
  LinkedList<Integer>          argPos;  
  public IExternalPreparedStatement preparedStmt;
  private String               extSourceName;
  
  public ExternalInstr()
  {
    numArgs = 0;
    argTypes = new LinkedList<Datatype>();
    argRoles = new LinkedList<Integer>();
    argPos   = new LinkedList<Integer>();    
  }
  
  public void addExternalArg(Datatype type, int role, int pos)
  {
    numArgs++;
    argTypes.add(type);
    argRoles.add(new Integer(role));
    argPos.add(new Integer(pos));
  }
  
  /*public void setPreparedStmt(PreparedStatement stmt)
  {
    this.pstmt = stmt;
  }*/
  
  public void setPreparedStmt(IExternalPreparedStatement paramPreparedStmt)
  {
    preparedStmt = paramPreparedStmt;
  }
  
  public int getNumArgs()
  {
    return numArgs;
  }
  
  public LinkedList<Integer> getArgPos()
  {
    return this.argPos;
  }
  
  public LinkedList<Integer> getArgRoles()
  {
    return this.argRoles;
  }
  
  public LinkedList<Datatype> getArgTypes()
  {
    return this.argTypes;  
  }

  /**
   * @return the extSourceName
   */
  public String getExtSourceName()
  {
    return extSourceName;
  }

  /**
   * @param extSourceName the extSourceName to set
   */
  public void setExtSourceName(String extSourceName)
  {
    this.extSourceName = extSourceName;
  }
  
}
