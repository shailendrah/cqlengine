package oracle.cep.test.ha.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.execution.ExecException;
import oracle.cep.service.IFaultHandler;

/**
 * CQSFaultHandler does the fault handling for CQService. Using CQSFaultHandler 
 * specific exceptions can be caught and appropriate actions can be taken. 
 * The exceptions which are not handled by fault handler will have the 
 * default behavior. 
 * The CQSFaultHandler need to be registered with EnvConfig so that CQL can 
 * provide the callback.  
 */
public class CQSFaultHandler implements IFaultHandler 
{
   private List<Throwable> exceptions = new ArrayList<Throwable>();  
   private static CQSFaultHandler s_instance = null;
   public static synchronized CQSFaultHandler getInstance() {
       if (s_instance == null) {
           s_instance = new CQSFaultHandler();
       }
       return s_instance;
   }
   
   public void clearExceptions() { exceptions.clear(); }
   public List<Throwable> getExceptions() { return exceptions;}
   
  /* When the CQSFaultHandler is registered with EnvConfig, CQL engine will
   * provide a callback to this method along with the exception and the context
   * info. The context string will contain the physical operator id and query
   * names separated with commas (eg: phyOpt:<id>;queries:q1,q2).
   * Right now, we are handling only for the case of 'Archived dimension change 
   * detection' exception */
  
  @Override
  public void handleFault(Throwable exception, String serviceName, 
                                           String context) throws Throwable
  {
    if (exception instanceof ExecException)
    {
      ExecException execExcep = (ExecException) exception;
      switch ((ExecutionError) execExcep.getErrorCode())
      {
      //The below exception happens when : 
      //relation table marked as dimension -> logical view with join (LDO) -> 
      //no state maintained on fact side -> so when a dimension table changes -> 
      //exception is thrown => the queries referring those LDO are restarted 
      case ARCHIVED_DIMENSION_CHANGE_DETECTED:
        restartQueries(exception, serviceName, context);
        //TODO: what exception need to be thrown if unable to restart the query?
        return;
      }
    }
    exceptions.add(exception);
    throw exception;
  }

  private void restartQueries(Throwable exception, String serviceName, 
                                        String context) throws Exception
  {
	/*
    String match = "queries:";
    int matchIndex = context.indexOf(match);
    if(matchIndex >= 0)
    {
      String queriesStr = context.substring(matchIndex + match.length());
      String[] queryStr = queriesStr.split(",");
      Set<Query> queriesToRestart = new HashSet<Query>();
      for(String queryName : queryStr)
      {
        if(queryName != null && queryName.length() > 0)
        {
          Set<String> tableNames = CQSFaultContext.getTableNames(queryName);
          if(tableNames != null)
          {
            for(String tableName : tableNames)
            {
              Set<Query> queries = CQSFaultContext.getQueries(tableName);
              if(queries != null)
              {
                for(Query query : queries)
                {
                  queriesToRestart.add(query);
                }
              }
            }
          }
        }
      }
      for(Query query: queriesToRestart)
      {
        if(query != null && query.isStarted())
        {
          CQLQueryContext ctx = CQSFaultContext.getQueryContext(
                                 QualifiedName.getName(query.getName()));
          if(ctx != null)
          {
            //restart the query
            query.restart(ctx);
          }
        }
      }
    }
    */
  }
}
