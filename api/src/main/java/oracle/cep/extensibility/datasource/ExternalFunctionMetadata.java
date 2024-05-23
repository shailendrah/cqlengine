/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/ExternalFunctionMetadata.java /main/1 2010/03/22 08:42:29 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/28/10 - Creation
 */

package oracle.cep.extensibility.datasource;

import oracle.cep.common.Datatype;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/ExternalFunctionMetadata.java /main/1 2010/03/22 08:42:29 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public class ExternalFunctionMetadata
{
  private String     functionName;  
  private int        numParams;
  private Datatype   returnType;
  private Datatype[] functionSignature;
  
  

  /**
   * Constructor
   * @param functionName
   * @param numParams
   * @param paramTypes
   */
  public ExternalFunctionMetadata(String functionName,
      Datatype[] paramTypes)
  {
    this.functionName      = functionName;    
    this.returnType        = null;    
    this.functionSignature = paramTypes;
    if(this.functionSignature != null)
      this.numParams = this.functionSignature.length;
    else
      this.numParams = 0;
  }
 
  public int getNumParams()
  {
    return numParams;
  }

  public Datatype getReturnType()
  {
    return returnType;
  }

  public String getName()
  {
    return functionName;
  }

  public String getSchema()
  {
    return null;
  }
  
  public Datatype[] getFunctionSignature()
  {
    return functionSignature;
  }
  
  public boolean equals(Object otherObj)
  {
    if(!(otherObj instanceof ExternalFunctionMetadata))
      return false;
    
    ExternalFunctionMetadata other = (ExternalFunctionMetadata)otherObj;
    
    if(other.getFunctionSignature().length != other.getNumParams())
      return false;
    
    if(!(this.functionName.equalsIgnoreCase(other.getName())))
      return false; 
      
    if(this.numParams != other.getNumParams())
      return false;
    
    for(int i = 0; i < this.numParams; i++)
    {
      if(!(this.functionSignature[i].equals(other.getFunctionSignature()[i])))
        return false;
    }
    
    return true;
  }
}
