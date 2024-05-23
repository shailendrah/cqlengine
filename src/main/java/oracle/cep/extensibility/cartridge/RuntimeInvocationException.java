package oracle.cep.extensibility.cartridge;

public class RuntimeInvocationException extends CartridgeException
{
  private static final long serialVersionUID = -7452766513322290385L;
  
  private String invocableName;

  public RuntimeInvocationException(String cartridgeName, String invocableName, 
      String message, Throwable cause)
  {
    super(cartridgeName, message, cause);
    this.invocableName = invocableName;
  }

  public RuntimeInvocationException(String cartridgeName, String invocableName, 
      String message)
  {
    super(cartridgeName, message);
    this.invocableName = invocableName;
  }

  public RuntimeInvocationException(String cartridgeName, String invocableName, 
      Throwable cause)
  {
    super(cartridgeName, cause);
    this.invocableName = invocableName;
  }

  public RuntimeInvocationException(String cartridgeName, String invocableName)
  {
    super(cartridgeName);
    this.invocableName = invocableName;
  }
  
  public String getInvocableName() 
  {
    return invocableName;
  }
  
}
