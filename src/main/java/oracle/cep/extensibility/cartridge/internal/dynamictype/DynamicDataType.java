/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/internal/dynamictype/DynamicDataType.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    07/01/12 - Creation
 */

package oracle.cep.extensibility.cartridge.internal.dynamictype;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.IRuntimeInvocable;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.type.IComplexType;
import oracle.cep.extensibility.type.IConstructor;
import oracle.cep.extensibility.type.IConstructorMetadata;
import oracle.cep.extensibility.type.IField;
import oracle.cep.extensibility.type.IFieldMetadata;
import oracle.cep.extensibility.type.IMethodMetadata;
import oracle.cep.extensibility.type.IType;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/cartridge/internal/dynamictype/DynamicDataType.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class DynamicDataType extends Datatype implements IComplexType
{
  /**
   * serialVersionUID for this serializable class
   */ 
  private static final long serialVersionUID = -8504652143207678303L;
  
  private List<String>        fieldNames;
  private Map<String, IType>  fieldNameTypeMap;
  private Map<String, Object> fieldNameValueMap;
  
  /**
   * Construct a Dynamic Datatype
   * @param extensibleTypeName
   */
  public DynamicDataType(String extensibleTypeName)
  {
    super(extensibleTypeName, 
      oracle.cep.extensibility.cartridge.internal.dynamictype.DynamicDataType.class);
    fieldNameTypeMap = new HashMap<String, IType>();
    fieldNameValueMap = new HashMap<String, Object>();
    fieldNames = new LinkedList<String>(); 
  }
  
  public DynamicDataType(DynamicDataType baseType)
  {
    super(baseType.typeName, baseType.getImplementationType());
    fieldNames = baseType.fieldNames;
    fieldNameTypeMap = baseType.fieldNameTypeMap;
    fieldNameValueMap = new HashMap<String, Object>();
  }
  
  /**
   * Add a simple or a complex type to Dynamic data type
   * @param typeName
   * @param memberType
   */
  public void addField(String fieldName, IType fieldType)
  {
    fieldNameTypeMap.put(fieldName, fieldType);
    fieldNames.add(fieldName);
  }
  
  /**
   * Set Field Value
   * @param fieldName
   * @param value
   */
  public void setField(String fieldName, Object value)
  {
    fieldNameValueMap.put(fieldName, value);
  }

  /**
   * Get All Field Names
   * @return
   */
  public List<String> getFieldNames()
  {
    return fieldNames;
  }
  
  public Object getFieldValue(String fieldName)
  {
    return fieldNameValueMap.get(fieldName);
  }
  
  public IType getFieldType(String fieldName)
  {
    return fieldNameTypeMap.get(fieldName);
  }
  
  public void setFieldValue(String fieldName, Object fieldValue)
  {
    fieldNameValueMap.put(fieldName, fieldValue) ;
  }
  
  
  @Override
  public IFieldMetadata getField(String fieldName)
      throws MetadataNotFoundException
  {
    if(fieldNames.contains(fieldName))
      return new DynamicDatatypeMetadata(fieldName);
    else
      throw new MetadataNotFoundException(DynamicTypeCartridge.CARTRIDGE_ID, 
                                          fieldName);
  }
  
  
  @Override
  public IFieldMetadata[] getFields() throws MetadataNotFoundException
  {
    List<IFieldMetadata> fieldMetadataList = new LinkedList<IFieldMetadata>();
    for(String fieldName : fieldNames)
    {
      fieldMetadataList.add(new DynamicDatatypeMetadata(fieldName));
    }
    return fieldMetadataList.toArray(new IFieldMetadata[0]);
  }

  class DynamicDatatypeMetadata implements IFieldMetadata
  {
    String fieldName;
    IType  fieldType;
    
    public DynamicDatatypeMetadata(String fieldName)
    {
      this.fieldName = fieldName;
      this.fieldType = fieldNameTypeMap.get(fieldName);
    }
    
    @Override
    public String getName()
    {
      return fieldName;
    }

    @Override
    public String getSchema()
    {
      return DynamicTypeCartridge.CARTRIDGE_ID;
    }

    @Override
    public IType getType()
    {
      return fieldType;
    }

    @Override
    public boolean isGetStatic()
    {
      return false;
    }

    @Override
    public boolean isSetStatic()
    {
      return false;
    }

    @Override
    public boolean hasSet()
    {
      return true;
    }

    @Override
    public boolean hasGet()
    {
      return true;
    }

    @Override
    public IField getFieldImplementation()
    {
      return new FieldInvoker(fieldName);
    }
    
  }
  
  class FieldInvoker implements IField
  {
    String fieldName;
    
    FieldInvoker(String fieldName)
    {
      this.fieldName = fieldName;
    }
    
    @Override
    public Object get(Object obj, ICartridgeContext context)
        throws RuntimeInvocationException
    {
      DynamicDataType dynObj = (DynamicDataType)obj;
      return dynObj.getFieldValue(fieldName);
    }
    

    @Override
    public void set(Object obj, Object arg, ICartridgeContext context)
        throws RuntimeInvocationException
    {
      DynamicDataType dynObj = (DynamicDataType)obj;
      dynObj.setFieldValue(fieldName, arg);
    }    
  }


  @Override
  public IMethodMetadata getMethod(String methodName, IType... parameters)
      throws MetadataNotFoundException
  {
    // Dynamic data type is a complex type having only fields.
    // There will be no method metadata corresponding to this type.
    return null;
  }

  @Override
  public IConstructorMetadata getConstructor(IType... parameters)
      throws MetadataNotFoundException
  {
    try
    {
      return new DynamicConstructorMetadata(this, parameters);
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public Class<?> getJavaType(IType cqlType)
  {
    // The Java type-system is special, because the CQL engine is implemented using it.
    // Therefore, as we know all JavaDatatypes extend CQL's datatype, we can cast to it,
    //  and retrieve the underlying implementation class.
    // This approach would not work for other type systems, like Hadoop's for instance.
    //
    return ((Datatype) cqlType).getImplementationType();
  }

  class DynamicConstructorMetadata implements IConstructorMetadata
  {
    private String name;
    private IType returnType;
    private IType[] paramTypes;
    private IRuntimeInvocable invocable;
    
    public DynamicConstructorMetadata(DynamicDataType returnType,
                                      IType[] cqlParamTypes)     
    {
      this.name = returnType.typeName;
      this.returnType = returnType;
      this.invocable = new ConstructorInvoker(returnType);
      this.paramTypes = cqlParamTypes;
    }    

    @Override
    public String getName()
    {
      return name;
    }

    @Override
    public String getSchema()
    {
      return DynamicTypeCartridge.CARTRIDGE_ID;
    }

    @Override
    public IType[] getParameterTypes()
    {
      return paramTypes;
    }

    @Override
    public IType getInstanceType()
    {
      return returnType;
    }

    @Override
    public IConstructor getConstructorImplementation()
    {
      return (IConstructor) invocable;
    }
    
  }
  
  class ConstructorInvoker implements IConstructor 
  {
    private DynamicDataType baseType;
    
    
    ConstructorInvoker(DynamicDataType baseType)
    {
      this.baseType = baseType;
    }

    @Override
    public Object instantiate(Object[] args, ICartridgeContext context) 
    throws RuntimeInvocationException
    {
      try
      {
        DynamicDataType obj = new DynamicDataType(baseType);
        for(int count = 0; count < args.length; count++)
        {
          obj.setField(fieldNames.get(count), args[count]);          
        }
        
        return obj;
      } 
      catch (Exception e)
      {
        LogUtil.fine(LoggerType.TRACE, "Constructor Invocation Failed");
        LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, e);
        
        throw new RuntimeInvocationException(DynamicTypeCartridge.CARTRIDGE_ID, "constructor", e);
      } 
    }
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder(super.toString());
    for(String fieldName: fieldNames)
    {
      sb.append(fieldName + "=" + fieldNameValueMap.get(fieldName) + ":");
    }
    return sb.toString();
  }

  
}
