  package oracle.cep.extensibility.functions.builtin.math;

  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;
  import java.lang.Math;

  public class CEPNegateExact implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      long retVal;
      if ((args[0] == null)) return null;
      long val1 = ((Long)args[0]).longValue();
      try {
        retVal = java.lang.Math.negateExact(val1);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPNegateExact");
      }
      return retVal;
    }
  }
  
