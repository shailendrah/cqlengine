package oracle.cep.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import oracle.cep.common.Datatype;
import oracle.cep.common.ServiceNameHelper;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.functions.IUserFunctionMetadata;
import oracle.cep.extensibility.functions.IUserFunctionMetadataLocator;
import oracle.cep.extensibility.type.IArrayType;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.metadata.MetadataException;
import oracle.cep.service.ExecContext;
import oracle.cep.service.ICartridgeLocator;

/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CartridgeHelper.java /main/9 2012/07/10 07:42:31 mjames Exp $ */

/* Copyright (c) 2009, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
      alealves  09/30/10 - Check if cartridge registry exists
      udeshmuk  06/14/10 - api to get IType from typename
      alealves  05/25/10 - Remove NPE for constructor with link
      hopark    12/29/09 - fix ClassCastException
    alealves    Dec 1, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CartridgeHelper.java /main/9 2012/07/10 07:42:31 mjames Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
public class CartridgeHelper
{
  static Datatype getType(ExecContext execContext, CEPStringTokenNode id) 
    throws MetadataException 
  {
    List<CEPStringTokenNode> ids = new LinkedList<CEPStringTokenNode>();
    ids.add(id);
    
    return getType(execContext, ids);
  }
  
  static Datatype getType(ExecContext execContext, List<CEPStringTokenNode> qualifiedName)
  throws MetadataException
  {
    return getType(execContext, qualifiedName, false);
  }
  
  static Datatype getArrayType(ExecContext execContext, List<CEPStringTokenNode> qualifiedName)
  throws MetadataException
  {
    return getType(execContext, qualifiedName, true);
  }
  
  public static String getCartridgeNameFromType(Datatype type) 
  {
    String linkName = null;

    // FIXME: need to find a better way of getting the cartridge
    String typeName = type.toString();
    int index = typeName.indexOf('@');
    
    if (index != -1)
      linkName = typeName.substring(index + 1);
    
    return linkName;
  }
  
  public static IType getType(ExecContext execContext, String name, 
                              boolean isArray)
                              throws MetadataException
  {
    String typeName = null;
    String linkName = null;
    //split the supplied name on '@' to get typename and linkname
    String[] splitNames = name.split("@");
    if(splitNames.length == 1) 
    { // no @ present in the supplied name
      typeName = splitNames[0];
      linkName = null;
    }
    else if(splitNames.length == 2)
    {
      typeName = splitNames[0];
      linkName = splitNames[1];
    }
    else
    { // more than one @ present in the supplied name (malformed name)
      throw new MetadataException(MetadataError.TYPE_NOT_FOUND, 
                                  new String[]{name});
    }
    /* 
     * Code below is taken from the function 
     * getType(ExecContext, List<CEPStringTokenNode>, boolean)
     * in this class. Just the Datatype is changed to IType.
     */ 
    
    ITypeLocator typeLocator = findTypeLocator(execContext, linkName);
    
    IType type = null;
    try
    {     
      ICartridgeContext context = createCartridgeContext(execContext);
      
      if(isArray) 
        type = typeLocator.getArrayType(typeName, context);
      else 
      {
        // FIXME if String, convert to CHAR immediately. This is needed, 
        // because we convert String to CHAR.
        // when it is the return type of a function, otherwise we will get 
        // type error.
        // We need to fix this to convert everything to String rather 
        // than CHAR.
        if (typeName.equals(Datatype.CHAR.getImplementationType().getName())) 
          return Datatype.CHAR;
        type = typeLocator.getType(typeName, context);
      }
      
    } catch (MetadataNotFoundException e0)
    {
      throw new MetadataException(MetadataError.TYPE_NOT_FOUND,
                                  new String[] {typeName});
    } catch (AmbiguousMetadataException e1)
    {
      throw new MetadataException(MetadataError.AMBIGUOUS_TYPE,
                                  new String[] {e1.getMetadataName(), 
                                                e1.getMessage()});
    }
    
    return type;  
  }
  
  static Datatype getType(ExecContext execContext, List<CEPStringTokenNode> qualifiedName, boolean isArray) 
    throws MetadataException
  {
    CEPStringTokenNode link = null;
    
    StringBuilder builder = new StringBuilder();
    for (CEPStringTokenNode node : qualifiedName) 
    {
      if (node.isLink()) 
        link = node;
      else 
      {
        if (builder.length() != 0) 
          builder.append('.');
        
        builder.append(node.getValue());
      }
    }

    String typeName = builder.toString();

    ITypeLocator typeLocator = findTypeLocator(execContext, link);
    
    Datatype type = null;
    try
    {
      // REVIEW eventually we should replace usage of 'Datatype' by 'IType' in engine.
      
      ICartridgeContext context = createCartridgeContext(execContext);
      
      if (isArray) 
        type = (Datatype) typeLocator.getArrayType(typeName, context);
      else 
      {
        // FIXME if String, convert to CHAR immediately. This is needed, because we convert String to CHAR
        //  when it is the return type of a function, otherwise we will get type error.
        //  We need to fix this to convert everything to String rather than CHAR.
        if (typeName.equals(Datatype.CHAR.getImplementationType().getName())) 
          return Datatype.CHAR;
        
        type = (Datatype) typeLocator.getType(typeName, context);
      }
      
    } catch (MetadataNotFoundException e0)
    {
      throw new MetadataException(MetadataError.TYPE_NOT_FOUND,
          qualifiedName.get(0).getStartOffset(), 
          qualifiedName.get(qualifiedName.size() - 1).getEndOffset(),
          new String[] {typeName});
    } catch (AmbiguousMetadataException e1)
    {
      throw new MetadataException(MetadataError.AMBIGUOUS_TYPE,
          qualifiedName.get(0).getStartOffset(), 
          qualifiedName.get(qualifiedName.size() - 1).getEndOffset(),
          new String[] {e1.getMetadataName(), e1.getMessage()});
    }
    
    return type;
  }
  
  public static ITypeLocator findTypeLocator(ExecContext execContext,
      String linkName) throws MetadataException 
  {
    return findTypeLocator(execContext, linkName, -1, -1);
  }
  
  public static ITypeLocator findTypeLocator(ExecContext execContext,
      CEPStringTokenNode linkNode) throws MetadataException
  {
    if (linkNode != null)
      return findTypeLocator(execContext, linkNode.getValue(), linkNode.getStartOffset(), 
          linkNode.getEndOffset());
    else
      return findTypeLocator(execContext, null, -1, -1);
  }

  public static IUserFunctionMetadata findFunctionMetadata(
      ExecContext execContext, String linkName, String functionName,
      Datatype[] dts) throws MetadataException {
    return findFunctionMetadata(execContext, new CEPStringTokenNode(linkName),
        functionName, dts);
  }
  
  public static IUserFunctionMetadata 
    findFunctionMetadata(ExecContext execContext, CEPStringTokenNode linkNode, String functionName, 
        Datatype [] dts) throws MetadataException
  {
    ICartridge cartridge =
      findCartridge(execContext, linkNode.getValue(), linkNode.getStartOffset(), 
          linkNode.getEndOffset());
    
    try
    {
      IUserFunctionMetadataLocator funcLocator = 
        cartridge.getFunctionMetadataLocator();
      
      // If no locator is found, then raise exception as if function has not being found.
      if (funcLocator == null)
        throw new MetadataException(MetadataError.FUNCTION_NOT_FOUND, 
            new String [] {functionName});
      
      ICartridgeContext context = 
        CartridgeHelper.createCartridgeContext(execContext);
      
      IUserFunctionMetadata fn = funcLocator.getFunction(functionName, dts, context);
      
      if (fn == null) 
        throw new MetadataException(MetadataError.FUNCTION_NOT_FOUND, 
            new String [] {functionName});
      
      return fn;
      
    } catch (CartridgeException e)
    {
      throw new MetadataException(MetadataError.FUNCTION_NOT_FOUND, 
          new String [] {functionName});
    }
  }

  private static ITypeLocator findTypeLocator(ExecContext execContext,
      String linkName, int startOffset, int endOffset) throws MetadataException
  {
    ITypeLocator typeLocator = null;
    
    if (linkName != null)
    {
      ICartridge cartridge = 
        findCartridge(execContext, linkName, startOffset, endOffset);
      
      typeLocator = cartridge.getTypeLocator();
      
      if (typeLocator == null) 
      {
        if (startOffset != -1)
          throw new MetadataException(MetadataError.TYPELOCATOR_NOT_FOUND,
              startOffset, endOffset, new String [] {linkName});
        else
          throw new MetadataException(MetadataError.TYPELOCATOR_NOT_FOUND,
              new String [] {linkName});
      }
      
    } else 
    {
      // For type systems only, defaults to Java if available.
      // A embedded profile of the engine may not have even the Java cartridge.
      try {
        ICartridgeLocator cartridgeLocator =
          execContext.getServiceManager().getConfigMgr().
          getCartridgeLocator();

        // if there is no cartridge registry we will create a mock type locator
        if (cartridgeLocator==null)
        {
            return createMockTypeLocator();
        }

        typeLocator =
          cartridgeLocator.getJavaTypeSystem();
      } 
      catch (NullPointerException e) 
      {
        return createMockTypeLocator(); 
      }
    }
    
    return typeLocator;
  }

  private static ITypeLocator createMockTypeLocator() 
  {
    return new ITypeLocator() {

      @Override
      public IArrayType getArrayType(final String componentExtensibleTypeName,
          ICartridgeContext context) throws MetadataNotFoundException,
          AmbiguousMetadataException
      {
        throw new MetadataNotFoundException("<built-in>", componentExtensibleTypeName);
      }

      @Override
      public IType getType(final String extensibleTypeName,
          ICartridgeContext context) throws MetadataNotFoundException,
          AmbiguousMetadataException
      {
        throw new MetadataNotFoundException("<built-in>", extensibleTypeName);
      }
    };
  }


  public static ICartridge findCartridge(ExecContext execContext,
      String cartridgeName, int startOffset, int endOffset) throws MetadataException
  {
    ICartridge cartridge = null;
    
    ICartridgeLocator locator = 
      execContext.getServiceManager().getConfigMgr().getCartridgeLocator();
    
    try 
    {
      //
      // First try to get a global PID. If that fails, look for a application-scoped PID.
      //
      cartridge =
        locator.getCartridge(cartridgeName);
      
    } catch (CartridgeException e) 
    {
      String appName =
        ServiceNameHelper.getApplicationName(execContext.getServiceName());
        
      try 
      {
        cartridge = locator.getCartridge(appName, cartridgeName);
      }  catch (CartridgeException e2)
      {
        if (startOffset != -1)
          throw new MetadataException(MetadataError.CARTRIDGE_NOT_FOUND,
              startOffset, endOffset, new String [] {cartridgeName});
        else 
          throw new MetadataException(MetadataError.CARTRIDGE_NOT_FOUND,
              new String [] {cartridgeName});
      }
    }
    
    return cartridge;
  }
  
  private final static class CartridgeContextImpl implements ICartridgeContext
  {
    private final HashMap<String, Object> properties;
    private final String applicationName;
    
    CartridgeContextImpl(String serviceName) 
    {
      applicationName = ServiceNameHelper.getApplicationName(serviceName);
      properties = new HashMap<String,Object>(0);
    }
    
    @Override
    public String getApplicationName()
    {
      return applicationName;
    }

    @Override
    public Map<String, Object> getProperties()
    {
      return properties;
    }
  }
  
  public static ICartridgeContext createCartridgeContext(ExecContext execContext) 
  {
    return new CartridgeContextImpl(execContext != null ? execContext.getServiceName() : "standaloneservice");
  }
}
