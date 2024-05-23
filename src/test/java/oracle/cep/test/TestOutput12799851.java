package oracle.cep.test;

import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.interfaces.output.QueryOutputBase;
import oracle.cep.exceptions.ExecutionError;
import oracle.cep.service.ExecContext;
import oracle.cep.exceptions.CEPException;

public class TestOutput12799851 extends QueryOutputBase
{
  private boolean isFirstEvent = true;
  
  public TestOutput12799851(ExecContext ec)
  {
    super(ec);
  }

  public void putNext(TupleValue tv, QueueElement.Kind k) throws CEPException
  {
    System.out.println("Received " + k + " element : " + tv.toString());
    if(isFirstEvent)
    {
      isFirstEvent = false;
      throw new CEPException(ExecutionError.DOWNSTREAM_CHANNEL_SOFT_EXCEPTION, 
                             new Object[]{this.getClass().getName()});
    }
  }

  public void start()
  {}

  public void end()
  {}
}
