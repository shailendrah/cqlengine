package oracle.cep.test.ha.server;

public class CQLProcessorWrapper
{
  private CQLProcessor processor;
  private static CQLProcessorWrapper singleton;
  
  private CQLProcessorWrapper()
  {    
    processor = new CQLProcessor();
  }

  public static CQLProcessor getCQLProcessor()
  {
    if(singleton == null)
      singleton = new CQLProcessorWrapper();
    return singleton.processor;
  }
}
