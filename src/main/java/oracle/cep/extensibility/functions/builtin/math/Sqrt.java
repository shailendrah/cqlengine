/**Don't Edit this file. This is a Generated Java File */

package oracle.cep.extensibility.functions.builtin.math;

import java.math.BigDecimal;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

/**
 * SQRT returns the square root of n.
 * This function takes as an argument any numeric data type.
 * The function returns the value of type double.
 * 
 * @author sbishnoi
 *
 */
public class Sqrt implements SingleElementFunction
{
  public Object execute(Object[] args) throws UDFException
  {
    // SQLRT returns NULL if input is null 
    if ((args[0] == null))
      return null;

    // Semantic Layer will ensure that only numeric type params are allowed. 
    assert args[0] instanceof Number;
    Number param1 = (Number)args[0];
    
    if(param1 instanceof Integer || param1 instanceof Float || 
       param1 instanceof Long  || param1 instanceof Double)
    {
      double inp = param1.doubleValue();
      if(Math.signum(inp) == -1.0d)
        throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION_PARAMETRIZED, inp, "sqrt");
      else
        return Math.sqrt(inp);
    }
    else if(param1 instanceof BigDecimal)
    {
      // TODO: Implement an algorithm for square root of bigdecimal values.
      // Converting to double will loose precision.
      BigDecimal inp = (BigDecimal)param1;
      if(inp.signum() == -1)
        throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION_PARAMETRIZED, inp, "sqrt");
      else
        return BigDecimal.valueOf(Math.sqrt(inp.doubleValue()));
    }
    else
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR);
  }
}
