package oracle.cep.test.java;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import junit.framework.Assert;
import oracle.cep.extensibility.cartridge.AmbiguousMetadataException;
import oracle.cep.extensibility.cartridge.CartridgeException;
import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.MetadataNotFoundException;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;
import oracle.cep.extensibility.functions.ISimpleFunction;
import oracle.cep.service.ICartridgeRegistry;

import com.oracle.cep.cartridge.java.AbstractJavaCartridge;
import com.oracle.cep.cartridge.java.CartridgeContextHolder;
import com.oracle.cep.cartridge.java.FunctionMetadata;

public class MyCartridge extends AbstractJavaCartridge
{
  private static final String NAME = "oracle.cep.test.myCartridge";

  Map<String, FunctionMetadata> functions = 
    new HashMap<String, FunctionMetadata>();
  
  MyCartridge(ICartridgeRegistry registry) throws CartridgeException {
    super("myCartridge", registry);

    MyFunction f1 = new MyFunction(0);
    functions.put(f1.getName(), f1);
    
    Echo f2 = new Echo();
    functions.put(f2.getName(), f2);
    
    Trim f3 = new Trim();
    functions.put(f3.getName(), f3);
    
    registry.registerCartridge(NAME, this);

    // Register cartridge contexts
    registry.registerServerContext("myCartridge", NAME, null);

    Map<String, Object> prop = new HashMap<String,Object>();
    prop.put("VALUE", 1);
    registry.registerServerContext("myCartridge1", NAME, prop);
    
    prop = new HashMap<String,Object>();
    prop.put("VALUE", 2);
    registry.registerServerContext("myCartridge2", NAME, prop);
    
    prop = new HashMap<String,Object>();
    prop.put("VALUE", 3);
    registry.registerServerContext("myCartridge3", NAME, prop);
    registry.registerApplicationContext("cql_external", "myCartridge3", NAME, prop);
  }
  
  static class MyFunction implements FunctionMetadata 
  {
    private final Integer value;
    public static final String NAME = "myFunction";

    MyFunction(Integer value) 
    {
      this.value = value;
    }
    
    @Override
    public Class<?> getReturnType()
    {
      return String.class;
    }

    public String getName()
    {
      return NAME;
    }

    @Override
    public ISimpleFunction getFunctionImplementation()
    {
      return new ISimpleFunction() {

        @Override
        public Object execute(Object[] args, ICartridgeContext context) throws RuntimeInvocationException
        {
          Assert.assertEquals(value, context.getProperties().get("VALUE"));
          return "result-" + value;
        }};
    }

    @Override
    public Class<?>[] getParameterTypes()
    {
      return new Class [] {};
    }
  }
  
  static class Echo implements FunctionMetadata 
  {
    @Override
    public Class<?> getReturnType()
    {
      return String.class;
    }

    public String getName()
    {
      return "echo";
    }

    @Override
    public ISimpleFunction getFunctionImplementation()
    {
      return new ISimpleFunction() {

        @Override
        public Object execute(Object[] args, ICartridgeContext context) throws RuntimeInvocationException
        {
          return args[0];
        }};
    }

    @Override
    public Class<?>[] getParameterTypes()
    {
      return new Class<?> [] { String.class };
    }
  }
  
  class Trim implements FunctionMetadata 
  {
    @Override
    public Class<?> getReturnType()
    {
      return String.class;
    }

    public String getName()
    {
      return "trim";
    }

    @Override
    public ISimpleFunction getFunctionImplementation()
    {
      return new ISimpleFunction() {

        @Override
        public Object execute(Object[] args, ICartridgeContext context) throws RuntimeInvocationException
        {
          return ((String) args[0]).trim();
        }};
    }

    @Override
    public Class<?>[] getParameterTypes()
    {
      return new Class<?> [] { String.class };
    }
  }

  @Override
  public FunctionMetadata getFunction(String name, Class<?>[] paramTypes, ICartridgeContext context)
      throws MetadataNotFoundException, AmbiguousMetadataException
  {
    if (name.equals(MyFunction.NAME))
    {
      if (context != null) 
        return new MyFunction((Integer) context.getProperties().get("VALUE"));
    }
    
    return functions.get(name);
  }

  @SuppressWarnings("unchecked")
  public List<FunctionMetadata> getAllFunctions(ICartridgeContext context)
  {
      List<FunctionMetadata> allFuncs = new ArrayList(functions.values());
      allFuncs.add(new MyFunction((Integer) context.getProperties().get("VALUE")));
      return allFuncs;
  }
  
  public static int myStaticMethod() 
  {
    ICartridgeContext ctx = CartridgeContextHolder.get();
    
    if (ctx != null)
      return (Integer) ctx.getProperties().get("VALUE");
    else 
      return 0;
  }

  public void destroy() throws CartridgeException
  {
    super.destroy();
    if (getRegistry() != null)
      getRegistry().unregisterCartridge(NAME);
  }
}
