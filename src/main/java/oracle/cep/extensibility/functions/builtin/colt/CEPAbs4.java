package oracle.cep.extensibility.functions.builtin.colt;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

import java.math.BigDecimal;

public class CEPAbs4 implements SingleElementFunction {
    
  public Object execute(Object[] args) throws UDFException {
	  BigDecimal retVal;
      if ((args[0] == null)) return null;
      BigDecimal val1 = (BigDecimal)args[0];
      try {
        retVal = val1.abs();
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPAbs4");
      }
      return retVal;
    }
  }