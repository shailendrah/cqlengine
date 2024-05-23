
/* Copyright (c) 2009, 2012, Oracle and/or its affiliates. 
All rights reserved. */

package oracle.cep.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.cep.extensibility.cartridge.CartridgeContextDelegate;
import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.internal.dynamictype.DynamicTypeCartridge;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;

public class BaseCartridgeRegistry
  implements ICartridgeRegistry,
             ICartridgeLocator
{
  // Cartridge name -> cartridge
  public Map<String, ICartridge> cartridges;
  // Cartridge name -> contexts
  public Map<String, List<QualifiedContext>> cartridgeToContexts
                       = new HashMap<String, List<QualifiedContext>>();
  // Use namedContexts to get to context easily from fully qualified names
  public Map<String, QualifiedContext> namedContexts;
  
  /**
   * Being Java the implementation language for the CQL engine, we need to special-case it here. 
   * No other type system, nor cartridges have this same treatment. 
   */
  public ITypeLocator javaTypeSystem;
  
  public BaseCartridgeRegistry()
  {
    try
    {
      cartridges = new HashMap<String, ICartridge>();
      namedContexts = new HashMap<String, QualifiedContext>();
      instantiateInternalCartridges();
    }
    catch(CartridgeException ce)
    {
      LogUtil.fine(LoggerType.TRACE, "FAILURE: Cartridge Registration Failed"+ 
        " for Internal Cartridges. Please check the failed stack trace.");
      LogUtil.logStackTrace(ce);      
    }
  }

  /**
   * Instantiate and Register All Internal Cartridges
   */
  private void instantiateInternalCartridges() throws CartridgeException
  {
    // Instantiate and Register Dynamic Type Cartridge
    
    //1) Create Dynamic Type Cartridge
    ICartridge dynCartridge = new DynamicTypeCartridge();

    //2) Register Cartridge
    registerInternalCartridge(DynamicTypeCartridge.CARTRIDGE_ID, dynCartridge); 

    //3) Register Server Context
    registerServerContext(DynamicTypeCartridge.CONTEXT_ID, 
                          DynamicTypeCartridge.CARTRIDGE_ID,
                          null);
    //4) Log Success Message
    LogUtil.fine(LoggerType.TRACE, "Internal Dynamic Type Cartridge" +
      "(CARTRIDGE_ID= "+ DynamicTypeCartridge.CARTRIDGE_ID +
      ";CONTEXT_ID=" + DynamicTypeCartridge.CONTEXT_ID +
      " registered successfully.");
    
    //.Test Purpose ///////////////////////
    /*
    ITypeLocator typeLocator = dynCartridge.getTypeLocator();
    DynamicTypeLocator dynTypeLocator = (DynamicTypeLocator)typeLocator;
    IType testType = dynTypeLocator.createType();
    DynamicDataType dynTestType = (DynamicDataType)testType;
    dynTestType.addField("c1", Datatype.INT);
    */
    ///////////////////////////////////////
  }

  /**
   * Checks/actions, if any, that need to happen before the cartridge type
   * is registered
   */
  protected void preRegisterTypeActions(String cartridgeID,
                                            ICartridge metadata)
    throws CartridgeException {}

  /**
   * Checks/actions, if any, that need to happen before the cartridge type
   * is unregistered
   */
  protected void preUnregisterTypeActions(String cartridgeID)
    throws CartridgeException {}

  public synchronized void registerCartridge(String cartridgeID, ICartridge metadata)
    throws CartridgeException
  {
    if (cartridges.get(cartridgeID) != null)
    {
      throw new CartridgeException("Duplicate cartridge ID [" + cartridgeID + "]"); 
    }
    
    preRegisterTypeActions(cartridgeID, metadata);

    cartridges.put(cartridgeID, metadata);
    cartridgeToContexts.put(cartridgeID, new ArrayList<QualifiedContext>());
  }

  public synchronized void unregisterCartridge(String name)
    throws CartridgeException
  {
    if (cartridges.get(name) == null)
    {
      throw new CartridgeException("Cartridge [" + name + "] not found."); 
    }

    preUnregisterTypeActions(name);

    List<QualifiedContext> ctxList = cartridgeToContexts.get(name);
    if (ctxList!=null)
    {
      for(QualifiedContext ctx : ctxList)
      {
          namedContexts.remove(getQualifiedContextName(ctx.getApplicationName(),
                                                       ctx.getContextID()));
      }
    }
    cartridgeToContexts.remove(name);
    cartridges.remove(name);
  }

  public synchronized void registerInternalCartridge(String cartridgeID, ICartridge metadata)
    throws CartridgeException
  {
    if (cartridges.get(cartridgeID) != null)
    {
      throw new CartridgeException("Duplicate cartridge ID [" + cartridgeID + "]"); 
    }
    
    cartridges.put(cartridgeID, metadata);
    cartridgeToContexts.put(cartridgeID, new ArrayList<QualifiedContext>());
  }

  @Override
  public ITypeLocator getJavaTypeSystem()
  {
    return javaTypeSystem;
  }
  
  public void setJavaTypeSystem(ITypeLocator javaTypeSystem)
  {
    this.javaTypeSystem = javaTypeSystem;
  }

  @Override
  public synchronized void registerServerContext(String contextID,
                                                String cartridgeID,
                                                Map<String, Object> properties)
    throws CartridgeException
  {
      registerApplicationContext(null, contextID, cartridgeID, properties);
  }
  
  @Override
  public synchronized void registerApplicationContext(String appName,
                                                String contextID,
                                                String cartridgeID,
                                                Map<String, Object> props)
      throws CartridgeException
  {
    ICartridge cartridge = cartridges.get(cartridgeID);
    if (cartridge==null)
        throw new CartridgeException(cartridgeID, "No cartridge by name " + cartridgeID);

    // See if a context already exists for this
    String fullName = getQualifiedContextName(appName, contextID);
    if (namedContexts.get(fullName)!=null)
        throw new CartridgeException(contextID, "A context already exists for " + fullName);

    CartridgeContextDelegate deleg = new CartridgeContextDelegate(cartridge,
                                                                  props);
    QualifiedContext ctx = new QualifiedContext(appName, contextID, props, deleg);
    namedContexts.put(fullName, ctx);
    List<QualifiedContext> contextList = cartridgeToContexts.get(cartridgeID);
    contextList.add(ctx);
  }

  public synchronized void unregisterServerContext(String cartridgeID,
                                                   String contextID)
    throws CartridgeException
  {
      unregisterApplicationContext(cartridgeID, null, contextID);
  }

  public synchronized void unregisterApplicationContext(String cartridgeID,
                                                        String appName,
                                                        String contextID)
    throws CartridgeException
  {
      String fullName = getQualifiedContextName(appName, contextID);
      QualifiedContext ctx = namedContexts.remove(fullName);
      if (ctx==null)
        throw new CartridgeException("Context [" + fullName + "] not found."); 

      List<QualifiedContext> contextList = cartridgeToContexts.get(cartridgeID);
      if (contextList==null)
        return;

      contextList.remove(ctx);
  }
  
  @Override
  public ICartridge getCartridge(String contextID)
    throws CartridgeException
  {
      return getCartridge(null, contextID);
  }

  @Override
  public ICartridge getInternalCartridge(String cartridgeID)
    throws CartridgeException
  {
    return cartridges.get(cartridgeID);
  }

  public synchronized ICartridge getCartridge(String appName, String contextID)
    throws CartridgeException
  {
      String fullName = getQualifiedContextName(appName, contextID);
      QualifiedContext savedContext = namedContexts.get(fullName);
      if (savedContext==null)
          throw new CartridgeException(fullName, "Cartridge not found");

      return savedContext.getCartridge();
  }

  public List<String> getServerContexts(String cartridgeID)
  {
      return getApplicationContexts(cartridgeID, null);
  }

  public synchronized List<String> getApplicationContexts(String cartridgeID, String appName)
  {
      List<QualifiedContext> ctxList = cartridgeToContexts.get(cartridgeID);
      if (ctxList==null)
        return Collections.emptyList();
      
      List<String> contexts = new ArrayList<String>();
      for (QualifiedContext ctx : ctxList)
      {
          if ((appName!=null && appName.equals(ctx.getApplicationName())) ||
              (appName==null && ctx.getApplicationName()==null))
              contexts.add(ctx.getContextID());
      }

      return contexts;
  }

  public synchronized ICartridgeContext getContext(String appName, String contextID)
  {
      String fullName = getQualifiedContextName(appName, contextID);
      return namedContexts.get(fullName);
  }

  private String getQualifiedContextName(String appName, String contextID)
  {
    return (appName==null ? "" : appName)+":"+contextID;
  }
}

