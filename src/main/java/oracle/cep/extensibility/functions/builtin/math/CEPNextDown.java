  package oracle.cep.extensibility.functions.builtin.math;

  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;

  import java.lang.Math;

  public class CEPNextDown implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      double retVal;
      if ((args[0] == null)) return null;
      double val1 = ((Double)args[0]).doubleValue();
      try {
        retVal = java.lang.Math.nextDown(val1);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPNextDown");
      }
      return retVal;
    }
  }
  
