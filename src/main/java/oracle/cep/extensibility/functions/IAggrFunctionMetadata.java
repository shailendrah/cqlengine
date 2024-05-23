package oracle.cep.extensibility.functions;

public interface IAggrFunctionMetadata 
extends IUserFunctionMetadata {

  IAggrFnFactory getAggrFactory();

  boolean supportsIncremental() ;

}
