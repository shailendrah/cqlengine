package oracle.cep.extensibility.type;

import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.IRuntimeInvocable;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;

public interface IField extends IRuntimeInvocable
{
  /**
   * Sets arg into field. Field must not be read-only.
   * 
   * If field is static, then <code>obj</code> is always null.
   * 
   * @param obj target field, null if static
   * @param arg
   * @param context cartridge context associated to this extensible object 
   * @throws RuntimeInvocationException
   */
  void set(Object obj, Object arg, ICartridgeContext context) 
    throws RuntimeInvocationException;
  
  /**
   * Gets arg from field.
   * 
   * If field is static, then <code>obj</code> is always null.
   * 
   * @param obj target field, null if static
   * @param context cartridge context associated to this extensible object 
   * @return Object
   * @throws RuntimeInvocationException
   */
  Object get(Object obj, ICartridgeContext context)
    throws RuntimeInvocationException;
  
}
