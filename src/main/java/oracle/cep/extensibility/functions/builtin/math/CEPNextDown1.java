  package oracle.cep.extensibility.functions.builtin.math; 

  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;
  import java.lang.Math;

  public class CEPNextDown1 implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      float retVal;
      if ((args[0] == null)) return null;
      float val1 = ((Float)args[0]).floatValue();
      try {
        retVal = java.lang.Math.nextDown(val1);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPNextDown1");
      }
      return retVal;
    }
  }
  
