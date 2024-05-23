  package oracle.cep.extensibility.functions.builtin.math;

  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;
  import java.lang.Math;

  public class CEPMultiplyExact1 implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      long retVal;
      if ((args[0] == null)||(args[1] == null)) return null;
      long val1 = ((Long)args[0]).longValue();
      long val2 = ((Long)args[1]).longValue();
      try {
        retVal = java.lang.Math.multiplyExact(val1,val2);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPMultiplyExact1");
      }
      return retVal;
    }
  }
  
