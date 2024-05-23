package oracle.cep.extensibility.type;

import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.IRuntimeInvocable;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;

public interface IConstructor extends IRuntimeInvocable
{
  /**
   * Instantiates object using args.
   * 
   * @param args
   * @param context cartridge context associated to this extensible object
   * @return Object
   * @throws RuntimeInvocationException
   */
  public Object instantiate(Object[] args, ICartridgeContext context) 
    throws RuntimeInvocationException;
}
