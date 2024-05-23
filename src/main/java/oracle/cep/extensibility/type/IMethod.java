package oracle.cep.extensibility.type;

import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.IRuntimeInvocable;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;

public interface IMethod extends IRuntimeInvocable
{
  /**
   * Invoke method on obj using args.
   * 
   * @param obj target method
   * @param args
   * @param context cartridge context associated to this extensible object
   * @return
   * @throws RuntimeInvocationException
   */
  public Object invoke(Object obj, Object[] args, ICartridgeContext context) 
    throws RuntimeInvocationException;
}
