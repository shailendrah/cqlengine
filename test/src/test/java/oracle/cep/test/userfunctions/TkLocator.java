package oracle.cep.test.userfunctions;

import java.util.HashMap;
import java.util.Map;

import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.service.IUserFunctionLocator;

public class TkLocator implements IUserFunctionLocator
{
  Map<String, Object> registry = new HashMap<String, Object>();
  
  public TkLocator() 
  {
    registry.put("counterObject", new Counter());
    registry.put("cloneObject", new Counter());
    registry.put("sumObject", new MultiParamIntSum());
    registry.put("aggrSumObject", new AggrSumFact());
  }

  @Override
  public SingleElementFunction getUserFunction(String name)
  {
    return (SingleElementFunction) registry.get(name);
  }

  @Override
  public IAggrFnFactory getUserAggrFunction(String name)
  {
    return (IAggrFnFactory) registry.get(name);
  }
  
  static class Counter implements SingleElementFunction 
  {
    private int counter = 0;

    @Override
    public Object execute(Object[] args) throws UDFException
    {
      return counter++;
    }
    
  }
  
  static class Clone implements SingleElementFunction 
  {

    @Override
    public Object execute(Object[] args) throws UDFException
    {
      return args[0];
    }
    
  }
  
  static class MultiParamIntSum implements SingleElementFunction 
  {

    @Override
    public Object execute(Object[] args) throws UDFException
    {
      Integer param1= (Integer) args[0];
      Integer param2= (Integer) args[0];
      
      return param1.intValue() + param2.intValue();
    }
    
  }
  
  static class AggrSumFact implements IAggrFnFactory 
  {

    @Override
    public void freeAggrFunctionHandler(IAggrFunction handler)
        throws UDAException
    {
      // NOP
    }

    @Override
    public IAggrFunction newAggrFunctionHandler() throws UDAException
    {
      return new AggrSum();
    }
    
  }
  
  static class AggrSum implements IAggrFunction
  {
    int sum = 0;
    private int numberInputs;

    @Override
    public void handleMinus(AggrValue[] args, AggrValue result)
        throws UDAException
    {
      if (!args[0].isNull()) 
      {
        Integer param1 = ((AggrInteger) args[0]).getValue();
        
        if (args.length == 2) 
        {
          param1 -= ((AggrInteger) args[1]).getValue();
        }
        
        numberInputs--;
        sum -= param1;
      }
      
      if (numberInputs == 0)
      {
        result.setNull(true);
      }
      else
      {
        ((AggrInteger) result).setValue(sum);
      }
    }

    @Override
    public void handlePlus(AggrValue[] args, AggrValue result)
        throws UDAException
    {
      if (!args[0].isNull()) 
      {
        Integer param1 = ((AggrInteger) args[0]).getValue();
        
        if (args.length == 2) 
        {
          param1 += ((AggrInteger) args[1]).getValue();
        }
        
        numberInputs++;
        sum += param1;
      }
      
      if (numberInputs == 0)
      {
        result.setNull(true);
      }
      else
      {
        ((AggrInteger) result).setValue(sum);
      }
    }

    @Override
    public void initialize() throws UDAException
    {
      sum = 0;
      numberInputs = 0;
    }
    
  }

}
