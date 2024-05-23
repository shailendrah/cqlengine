  package oracle.cep.extensibility.functions.builtin.math;

  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;
  import java.lang.Math;

  public class CEPDecrementExact1 implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      int retVal;
      if ((args[0] == null)) return null;
      int val1 = ((Integer)args[0]).intValue();
      try {
        retVal = java.lang.Math.decrementExact(val1);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPDecrementExact1");
      }
      return retVal;
    }
  }
  
