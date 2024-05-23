package oracle.cep.metadata;

import oracle.cep.common.Datatype;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.functions.IAttribute;
import oracle.cep.extensibility.functions.ISimpleFunction;
import oracle.cep.extensibility.functions.ISimpleFunctionMetadata;
import oracle.cep.extensibility.functions.UserDefinedFunction;
import oracle.cep.extensibility.type.IArray;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IConstructor;
import oracle.cep.extensibility.type.IConstructorMetadata;
import oracle.cep.extensibility.type.IField;
import oracle.cep.extensibility.type.IFieldMetadata;
import oracle.cep.extensibility.type.IMethod;
import oracle.cep.extensibility.type.IMethodMetadata;
import oracle.cep.extensibility.type.IType;

/**
 * FIXME Ideally we could use IType in the engine and avoid the casting...
 * 
 * @author Alex Alves
 *
 */
public class SynthesizedFunctionMetadata implements ISimpleFunctionMetadata
{
  private final IAttribute [] params;
  private final Datatype returnType;
  private final String name;
  private final ISimpleFunction impl;
  private String cartridgeName;
  
  public SynthesizedFunctionMetadata(final IMethodMetadata methodMetadata)
  {
    params = convertToAttribute(methodMetadata.getParameterTypes());
    returnType = 
      (Datatype) methodMetadata.getReturnType();
    name = methodMetadata.getName();
    cartridgeName = methodMetadata.getSchema();
    
    final IMethod method = 
      methodMetadata.getMethodImplementation();
    
    impl = new ISimpleFunction() {
      
      Object [] params = 
        new Object[SynthesizedFunctionMetadata.this.params.length];

      @Override
      public Object execute(final Object[] args, final ICartridgeContext context) 
      throws RuntimeInvocationException
      {
        if (methodMetadata.isStatic()) 
        {
          return method.invoke(null, args, context);
          
        } else 
        {
          for (int i = 0; i < params.length; i++) 
            params[i] = args[i+1];
          
          return method.invoke(args[0], params, context); 
        }
      }
      
      public String toString() 
      {
        return name;
      }
    };
  }

  public SynthesizedFunctionMetadata(final IFieldMetadata fieldMetadata)
  {
    assert fieldMetadata.hasGet();

    params = new IAttribute [0];
    returnType = 
      (Datatype) fieldMetadata.getType();
    name = fieldMetadata.getName();
    cartridgeName = fieldMetadata.getSchema();
    
    final IField field = 
      fieldMetadata.getFieldImplementation();
    
    impl = new ISimpleFunction() {
      
      @Override
      public Object execute(final Object[] args, final ICartridgeContext context) 
        throws RuntimeInvocationException
      {
        if (fieldMetadata.isGetStatic()) 
          return field.get(null, context);
        else
        {
          assert (args != null && args.length > 0) : "lvalue missing for non-static field access";
          
          return field.get(args[0], context);
        }
      }
      
      public String toString() 
      {
        return name;
      }
    };
  }
  
  public SynthesizedFunctionMetadata(IConstructorMetadata constructorMetadata)
  {
    params = convertToAttribute(constructorMetadata.getParameterTypes());
    returnType = 
      (Datatype) constructorMetadata.getInstanceType();
    name = constructorMetadata.getName();
    cartridgeName = constructorMetadata.getSchema();
    
    final IConstructor constructor = 
      constructorMetadata.getConstructorImplementation();
    impl = new ISimpleFunction() {
      
      @Override
      public Object execute(final Object[] args, final ICartridgeContext context) 
        throws RuntimeInvocationException
      {
        return constructor.instantiate(args, context);
      }
      
      public String toString() 
      {
        return name;
      }
    };
  }

  public SynthesizedFunctionMetadata(final IArrayType arrayType, final int index)
  {
    params = new IAttribute [0];
    returnType = 
      (Datatype) arrayType.getComponentType();
    name = arrayType.name();  
    
    final IArray array = 
      arrayType.getArrayImplementation();
    
    impl = new ISimpleFunction() {
      
      @Override
      public Object execute(final Object[] args, final ICartridgeContext context) 
        throws RuntimeInvocationException
      {
        // Arrays do not make use of contextual information.
        return array.get(args[0], index);
      }
      
      public String toString() 
      {
        return name;
      }
    };
  }

  @Override
  public int getNumParams()
  {
    return params.length;
  }

  @Override
  public IAttribute getParam(int pos) throws MetadataException
  {
    return params[pos];
  }

  @Override
  public Datatype getReturnType()
  {
    return returnType;
  }

  @Override
  public String getName()
  {
    return name;
  }
  
  @Override 
  public String getSchema() 
  {
    return cartridgeName;
  }

  @Override
  public UserDefinedFunction getImplClass()
  {
    return impl;
  }
  
  private IAttribute[] convertToAttribute(IType [] parameterTypes)
  {
    Attribute [] attributes = new Attribute[parameterTypes.length];
    
    for (int i = 0; i < parameterTypes.length; i++)
      attributes[i] = new Attribute("attr" + new Integer(i), 
          (Datatype) parameterTypes[i], 0);
    
    return attributes;
  }
  
}
