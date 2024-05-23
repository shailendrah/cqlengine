  package oracle.cep.extensibility.functions.builtin.math;

  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;
  import java.lang.Math;

  public class CEPIncrementExact implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      long retVal;
      if ((args[0] == null)) return null;
      long val1 = ((Long)args[0]).longValue();
      try {
        retVal = java.lang.Math.incrementExact(val1);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPIncrementExact");
      }
      return retVal;
    }
  }
  