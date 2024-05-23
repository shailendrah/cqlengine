/* (c) 2009-2010 Oracle Corporation.  All rights reserved. */
package oracle.cep.service;

/**
 * Service for providing custom fault handlers.
 * Service must be set in environment's configuration.
 * 
 * @see IEnvConfig
 */
public interface IFaultHandler
{
  /**
   * This method is called-back informing handler of a fault that happened
   *  while processing an event, or a batch of events, allowing client to
   *  provide custom handling of faults.
   *<br><br> 
   * Handler may consume fault, or re-throw fault, or throw a new exception.
   * If fault is not consumed, then it may cause related queries to be stopped, or 
   *  culprit destinations to be dropped.
   *<br><br>
   * Fault may be a CEP exception, or even a JVM Error.    
   * 
   * @param fault CEP Exception, or JVM Error
   * @param serviceName Service name
   * @param context Execution context
   * @throws Throwable 
   */
  void handleFault(Throwable fault, String serviceName, String context) throws Throwable;
  
}
