  package oracle.cep.extensibility.functions.builtin.math; 

  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;
  import java.lang.Math;

  public class CEPFloorMod implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      int retVal;
      if ((args[0] == null)||(args[1] == null)) return null;
      int val1 = ((Integer)args[0]).intValue();
      int val2 = ((Integer)args[1]).intValue();
      try {
        retVal = java.lang.Math.floorMod(val1,val2);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPFloorMod");
      }
      return retVal;
    }
  }
  
