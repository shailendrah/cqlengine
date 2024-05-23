package oracle.cep.extensibility.functions;

/**
 * 
 * @author Alex Alves
 *
 */
public interface ISimpleFunctionMetadata extends IUserFunctionMetadata 
{
  /**
   * Returns function implementation to be invoked at runtime during query execution.
   * 
   * @return runtime implementation
   */
   UserDefinedFunction getImplClass();
}
